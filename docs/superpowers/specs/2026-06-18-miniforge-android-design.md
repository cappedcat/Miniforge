# MiniForge — Android Mini App Maker Design Spec
**Date:** 2026-06-18  
**Status:** Approved

---

## Overview

MiniForge is an Android app that lets users create, run, and share mini apps using AI. Users bring their own API keys (BYOK) from any supported provider. Mini apps are self-contained HTML/CSS/JS files that run in a sandboxed WebView. Apps can be saved locally or published to a cloud marketplace powered by Supabase. The app must be super lightweight — target APK size under 12MB.

---

## Architecture

**Stack:**
- Language: Kotlin
- UI: Jetpack Compose (host shell only — no Compose inside mini apps)
- Mini App Runtime: Android WebView (sandboxed)
- Local Storage: Room DB (metadata) + internal file storage (HTML files)
- Cloud: Supabase (auth, marketplace DB)
- HTTP: Ktor client (AI API calls + Supabase REST)
- Min SDK: Android 12 (API 31)

**Layers:**
```
┌─────────────────────────────────────┐
│         Compose UI Shell            │  Screens, navigation, host chrome
├─────────────────────────────────────┤
│         ViewModel / State           │  Business logic, state holders
├─────────────────────────────────────┤
│         Repository Layer            │  Local (Room) + Remote (Supabase)
├─────────────────────────────────────┤
│   AI Service  │  WebView Runtime    │  BYOK API calls | Mini app execution
└─────────────────────────────────────┘
```

**Key principle:** MiniForge is a shell. AI calls go directly from the user's device to their chosen provider — no proxy, no middleman. HTML files are stored locally and rendered in WebView.

---

## Screens & Navigation

Single-activity app, Compose Navigation, bottom tab bar (icons only, no labels).

```
Home Tabs
├── My Apps          — grid of local mini apps, tap to launch
├── Marketplace      — browse/search published apps (anonymous ok)
│   └── App Detail   — WebView preview, author, downloads, install button
└── Settings
    ├── AI Providers  — add/edit/delete keys + custom endpoints
    └── Account       — Google sign-in, profile

Create Flow (separate back-stack)
├── New App Screen   — name, description, prompt
├── Generator Screen — live WebView preview as AI streams HTML
│   └── Refine FAB   — floating button → chat modal slides up
│       └── Chat Modal — refine via conversation, preview updates live
└── Save/Publish     — save locally and/or publish to marketplace

Mini App Runner
└── Full-screen WebView + top bar (back, share, refine, options menu)
```

No splash screen. App opens directly to My Apps.

---

## AI & BYOK System

### Supported Providers

| Provider | API Format | Base URL |
|---|---|---|
| Claude (Anthropic) | Anthropic | `api.anthropic.com` |
| OpenAI | OpenAI-compat | `api.openai.com` |
| Gemini | OpenAI-compat | `generativelanguage.googleapis.com` |
| OpenRouter | OpenAI-compat | `openrouter.ai/api` |
| Routeway AI | OpenAI-compat | *(their endpoint)* |
| Custom | User picks either | User-defined |

**Two API format adapters:**
- **OpenAI-compatible:** `POST /chat/completions` with `messages[]`
- **Anthropic-compatible:** `POST /messages` with `messages[]` + `system`

Custom providers let the user specify the base URL and choose the format. Both adapters produce the same streaming output internally.

**Key storage:** Android `EncryptedSharedPreferences` (hardware-backed where available). Keys never leave the device via our servers.

### App Generation Flow

1. User fills name, description, prompt → taps **Generate**
2. AI service sends system prompt + user prompt to selected provider (streaming)
3. Response is streamed; HTML block is extracted as it arrives
4. HTML loads into WebView immediately on extraction complete
5. User taps **Refine FAB** → chat modal opens
6. Each refinement message appends to conversation context
7. AI returns updated full HTML → WebView reloads

### AI System Prompt for Mini App Generation

The system prompt sent with every generation request enforces:

```
You are a mini app generator. Output a single, complete, self-contained HTML file.

Rules:
- ONE file only. All CSS and JavaScript must be inline (no external links).
- No external CDN links, no external fonts, no external images.
- Images: use CSS gradients, SVG, or base64-encoded data URIs only.
- No eval(), no document.cookie, no localStorage for sensitive data.
- Mobile-first. Touch-friendly. Minimum tap target 44px.
- Semantic HTML5. Works in Android WebView (Chromium).
- Keep output under 300KB. Hard limit: 500KB. Be concise.
- Wrap the entire HTML in a single ```html code block.
- Do not include explanations outside the code block.
```

For refinements, the full HTML is included in the conversation context so the AI can edit it directly.

---

## Data & Storage

### Local (Room DB)

```sql
mini_apps
  id              TEXT PRIMARY KEY
  name            TEXT
  description     TEXT
  html_file_path  TEXT       -- path in internal storage
  created_at      INTEGER
  updated_at      INTEGER
  marketplace_id  TEXT NULL  -- set if published

chat_history
  id              TEXT PRIMARY KEY
  app_id          TEXT
  role            TEXT       -- "user" | "assistant" | "system"
  content         TEXT
  created_at      INTEGER

ai_providers
  id              TEXT PRIMARY KEY
  name            TEXT
  base_url        TEXT
  api_format      TEXT       -- "openai" | "anthropic"
  model           TEXT
  is_default      INTEGER    -- boolean
```

HTML files stored at `/files/miniapps/<id>.html` in app internal storage.

### Cloud (Supabase)

```sql
profiles
  id          UUID PRIMARY KEY  -- matches Supabase auth uid
  username    TEXT
  avatar_url  TEXT
  created_at  TIMESTAMPTZ

published_apps
  id           UUID PRIMARY KEY
  author_id    UUID REFERENCES profiles(id)
  name         TEXT
  description  TEXT
  html_content TEXT             -- full HTML stored in DB (target <300KB)
  downloads    INTEGER DEFAULT 0
  created_at   TIMESTAMPTZ
  updated_at   TIMESTAMPTZ
```

HTML stored directly in Supabase DB rows (not a storage bucket) — simple, queryable, no extra bucket config.

### Sync Logic

Strictly one-way:
- **Local → Cloud:** when user explicitly publishes
- **Cloud → Local:** when user downloads from marketplace
- No automatic sync. User controls everything.

---

## Marketplace & Auth

### Authentication

Google Sign-In via Supabase Auth. Session persisted in `EncryptedSharedPreferences`.

| Action | Auth required? |
|---|---|
| Browse marketplace | No |
| Search marketplace | No |
| Preview app in marketplace | No |
| Download / install app | Yes |
| Publish app | Yes |
| Create / use local mini apps | No |

### Marketplace Flows

**Browse (anonymous):**
- Grid of published apps, sorted by newest / most downloaded
- Search by name or description (Supabase full-text search)
- Tap → detail screen: WebView preview, author, download count
- Download button prompts sign-in if not authenticated

**Publish (authenticated):**
- Triggered from Mini App Runner or My Apps → long-press menu
- User confirms name + description (pre-filled)
- HTML uploaded to Supabase `published_apps`
- Success screen shows shareable link + QR code

### Share & P2P

**Android Share Sheet:**  
Exports the raw `.html` file via Android's standard share intent. Recipients can open in any browser or import into MiniForge.

**QR Code:**
- Apps < 50KB: QR encodes `miniforge://app/local?data=<base64-html>` — works fully offline, no internet needed
- Apps ≥ 50KB: QR encodes `miniforge://app/<marketplace_id>` — requires internet, app must be published first

**Deep Link fallback:**  
If MiniForge is not installed, the deep link resolves to a static web page (hosted via Supabase or GitHub Pages) showing the app name and an install prompt.

---

## Error Handling

| Scenario | Behavior |
|---|---|
| AI generation fails | Inline error + retry button, prompt preserved |
| Invalid / expired API key | Error with direct link to Settings → AI Providers |
| WebView crash | Catch `onRenderProcessGone`, show reload button, HTML file safe |
| Supabase offline | Marketplace shows cached state or empty + "No connection" banner |
| Generated HTML > 500KB | Warn user, offer regenerate with lighter instruction appended |
| QR encode too large | Fall back to share sheet automatically |

---

## Testing Strategy

- **Unit:** AI request adapters (both formats), HTML extraction, Room DAOs
- **Integration:** Supabase auth flow, publish/download round-trip
- **UI:** Create flow happy path, provider setup, marketplace browse
- **Manual:** WebView rendering on multiple screen sizes, deep link handling, QR import

No CI at launch. Tests run locally before each release.

---

## Constraints Summary

- APK target: < 12MB
- Mini app hard size limit: 500KB HTML (target 300KB)
- No external dependencies inside mini apps (no CDN, no fonts)
- API keys never touch MiniForge servers
- All mini app execution sandboxed in WebView
- Offline-first for local mini apps
