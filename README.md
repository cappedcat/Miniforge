# MiniForge - App Builder with Your Own AI

## Overview

MiniForge is a revolutionary AI-powered app creation platform that puts you in complete control. Instead of paying subscription fees to a middleman or being locked in
to one provider, you bring your own API keys from the AI platforms scribe what you want to build, and MiniForge uses your chosen AI to ge
nerate fully functional, self-contained apps instantly.

**Core Philosophy:** Your data. Your AI. Your apps. Your control.

---

## Why MiniForge?

### 🔑 Bring Your Own Key
The defining feature of MiniForge is freedom. Connect your own API keys from any major AI provider:
- **Claude** (Anthropic) - Intelligent, capable AI for complex tasks
- **OpenAI** (GPT-4, GPT-4 Turbo) - Powerful general-purpose AI
- **Mistral** - Fast, efficient European AI
- **Groq** - Ultra-fast inference engine
- **Gemini** (Google) - Multimodal AI capabilities
- **OpenRouter** - Access to 100+ models through one API
- **Custom endpoints** - Use any compatible API provider

No subscriptions. No platform fees. No markup. You pay only for the API calls you make, directly to the provider.

### 💰 Cost Control
- **No middleman fees** — Your money goes directly to your chosen AI provider
- **Transparent pricing** — You see exactly what you're paying
- **Scale as you grow** — Pay only for what you use
- **Budget your way** — Some providers offer free tiers or credits to start

### 🔒 Privacy & Security
- **Your data stays yours** — API calls go directly to your provider, not through MiniForge servers
- **No data harvesting** — We don't store, sell, or analyze your app content
- **Your API keys stay local** — Encrypted on your device, never sent elsewhere
- **Offline-first architecture** — Apps run locally on your Android

### 🎯 Complete Control
- **Choose your AI** — Switch providers, experiment with different models, find what works best
- **No vendor lock-in** — Export your apps as HTML and run them anywhere
- **Own your creations** — Your apps belong to you, not to a platform
- **Iterate freely** — Refine and improve apps without worrying about API quotas

---

## Getting Started

### Step 1: Add Your AI Provider

1. Open **Settings** → **AI Providers**
2. Tap the **+** button
3. Choose a provider (Claude, OpenAI, Mistral, etc.) or select **Custom** for a custom endpoint
4. Enter your API key (find it at your provider's console)
5. Optionally customize the base URL or default model
6. Tap **Save Provider**

**Getting API Keys:**
- **Claude**: Visit [console.anthropic.com/account/keys](console.anthropic.com/account/keys)
- **OpenAI**: Visit [platform.openai.com/account/api-keys](https://platform.openai.com/account/api-keys)
- **Mistral**: Visit [console.mistral.ai/api-keys](https://console.mistral.ai/api-keys)
- **Groq**: Visit [console.groq.com/keys](https://console.groq.com/keys)
- **Gemini**: Visit [aistudio.google.com/app/apikey](https://aistudio.google.com/app/apikey)
- **OpenRouter**: Visit [openrouter.ai/keys](https://openrouter.ai/keys)

Each provider has a help button (📘) in Settings that links directly to their setup page.

### Step 2: Create Your First App

1. Go to **Create** tab
2. Select your AI **Provider** (which one will generate your app)
3. Choose a **Model** from that provider (auto-fetched from their API)
4. Enter your app's **Name** (e.g., "Todo List")
5. Add a **Description** (optional, but helpful)
6. Describe what your app should do in **"What should it do?"**
   - Example: "Create a simple calculator that can add, subtract, multiply, and divide two numbers with a clean interface"
7. Tap **Generate** and watch as AI builds your app in real-time

The streaming generation shows you exactly what the AI is creating, character by character.

### Step 3: Use Your App

1. Go to **My Apps**
2. Tap an app to **open** it and use it immediately
3. Your app runs locally—no internet required after generation

---

## Core Features

### 📱 App Management
- **View all apps** in your personal library
- **Open and use** any app instantly
- **Edit apps** with refinement prompts (tap the pencil icon)
- **Delete apps** (long-press to confirm)
- **Centered, organized display** of all your creations

### ✏️ App Refinement
The power of MiniForge lies in iteration:
1. Open an existing app
2. Tap the **Edit** (pencil) button
3. Select the AI **Provider** and **Model** to use for refinement
4. Describe what you want to change (e.g., "Add a dark mode", "Make the buttons bigger")
5. Tap **Update** and watch the AI refine your app
6. The updated version saves automatically

You can refine your apps as many times as you want—each refinement uses your API key and generates a new version.

### 🌐 Share on Local Network
Start a localhost server and share your apps with anyone on the same Wi-Fi:

1. Go to **My Apps**
2. Tap **Start** in the Server banner
3. The server runs at `http://<your-device-ip>:8080`
4. Share the URL or scan the **QR code** on any app card
5. Friends can use your apps in their browser without installing anything

### 📤 Export & Share
Each app can be exported as a standalone HTML file:
1. Open an app
2. Tap the **Share** (📤) icon
3. Choose how to share (email, messaging, cloud storage, etc.)
4. The app runs anywhere HTML is supported

### 🌍 Multi-Language Support

Language automatically switches based on your Android device's system language setting.

---

## How It Works Under the Hood

### Architecture

```
┌─────────────────┐
│   Your Device   │
├─────────────────┤
│  MiniForge App  │
│   (Android)     │
└────────┬────────┘
         │
         │ Your API Key
         │ (encrypted locally)
         ↓
┌─────────────────┐
│  AI Provider    │
│  (Claude, GPT,  │
│   Mistral, etc) │
└─────────────────┘
         │
         │ Generated HTML
         ↓
┌─────────────────┐
│   Your App      │
│  (Runs Locally) │
└─────────────────┘
```

### Generation Flow

1. **You describe** your app idea in plain English
2. **MiniForge sends** your description + system prompt to your AI provider via your API key
3. **AI generates** the complete HTML/CSS/JavaScript for your app
4. **MiniForge streams** the response back in real-time so you see it being built
5. **HTML is saved** to your device's local storage
6. **App launches** and runs completely locally without any external dependencies

### Refinement Flow

1. **You describe changes** to your existing app
2. **MiniForge includes** the current HTML as context
3. **AI receives**: "Here's the current app HTML. User wants: [your changes]"
4. **AI generates** the updated HTML with your requested changes
5. **New version saves** and overwrites the old version

### Model Auto-Fetching

MiniForge automatically fetches available models from each provider
- Models are cached for 24 hours (so subsequent opens are instant)
- Cache automatically refreshes every day
- If API fails, you can still use the provider's default model
- Supports model selection within each provider (different models = different capabilities and costs)

---

## Advanced Features

### Provider Setup Guide

Each provider has a dedicated help button in Settings:
- **Step-by-step instructions** for getting API keys
- **Direct links** to provider consoles
- **Pricing information** and free tier details
- **Model recommendations** for different app types

### Server Management

The localhost server:
- **Runs on port 8080** by default
- **Auto-detects your device IP** on your local network
- **Serves apps at**: `http://<IP>:8080/{appId}`
- **Generates QR codes** for easy sharing
- **Survives app pause/resume** (stays running in background)

### QR Code Sharing

Each app has a QR code showing its localhost URL:
1. Start the server
2. Tap the QR icon on any app
3. A dialog shows the QR code and full URL
4. Friends on the same Wi-Fi can scan or manually enter the URL
5. App loads and runs in their browser

### HTML Export

Export any app as a standalone HTML file:
- Single, self-contained HTML file (no external dependencies)
- Includes all CSS and JavaScript inline
- Runs in any modern web browser
- Can be emailed, stored in cloud, or shared via any method
- Updates with each refinement

---

## Workflow Example

### Creating a Pomodoro Timer

1. **Setup Phase**
   - Go to Settings → AI Providers
   - Add Claude API key
   - Return to Create tab

2. **Generation Phase**
   - Select **Claude** as provider
   - Select **claude-sonnet-4-6** as model
   - Name: "Pomodoro Timer"
   - Description: "A work timer app"
   - What should it do: "Create a Pomodoro timer with 25-minute work sessions, 5-minute breaks, customizable durations, play a sound when time is up, and a clean inte
rface with start/pause/reset buttons"
   - Tap **Generate**
   - Watch the AI build your app in real-time (takes ~10 seconds)

3. **Testing Phase**
   - Tap the app to open it
   - Test: start timer, pause, reset
   - Check if sound works and interface is clean

4. **Refinement Phase** (if needed)
   - Tap Edit
   - Select Claude again
   - Say: "Add a dark mode toggle and make the timer display larger
   - Tap Update
   - The app now has dark mode and a bigger display

5. **Sharing Phase**
   - Start the server
   - Tap the QR icon on the Pomodoro Timer
   - Share QR code with friend on your Wi-Fi
   - Friend scans it and uses your app in their browser

---

## Cost Breakdown

### Example: Creating 10 Apps

Using Claude (Anthropic) with ~500 tokens per app:
- **10 apps × 500 tokens × $0.003/1K tokens = $0.015** (~1.5 cents)
- Plus refinements: Each refinement costs based on tokens (usually under $0.01)
- **Total for 10 apps with refinements: ~$0.20-0.50**

Compare to:
- **Platform subscription**: $10-20/month (whether you build 1 app or 100)
- **MiniForge with your key**: Pay only for what you generate

---

## Data & Privacy

### What MiniForge Stores
- **App metadata**: Name, description, creation date (on your device)
- **App HTML**: The generated code (on your device)
- **API key**: Encrypted and stored locally on your device only

### What MiniForge Does NOT Do
- ❌ Store or view your app content
- ❌ Track what apps you create
- ❌ Analyze your generation patterns
- ❌ Share data with third parties
- ❌ Require login or account creation
- ❌ Send data to MiniForge servers

### Your API Keys
- Stored securely on your device in encrypted storage
- **Never sent anywhere except directly to your chosen provider**
- Only used when you explicitly tap **Generate**
- You control when they're used and which provider uses them

---

## Troubleshooting

### "No providers added yet"
**Solution**: Go to Settings → AI Providers → Tap **+** → Add your first API key

### "Generation failed" or "API error"
**Common causes**:
- Invalid API key (check it's correct in Settings)
- API quota exceeded (check your provider's dashboard)
- No internet connection (generation requires internet)
- Provider is down (check provider's status page)

**Solution**: Verify your API key and internet, check your provider's dashboard for quota, and try again

### "App won't load after edit"
**Cause**: Rare cases where AI generates invalid HTML

**Solution**:
- Go back and try refining with different wording
- Or re-generate the app from scratch
- Or try a different AI model

### Models not showing up
**Cause**:
- Provider's API is slow to respond
- Your internet connection is slow
- Provider doesn't support model enumeration

**Solution**:
- Wait a few seconds for models to load
- Try switching providers
- Manually enter a model name you know the provider supports

---

## Tips & Best Practices

### For Best Results

1. **Be specific** in your descriptions
   - ❌ "Make a game"
   - ✅ "Create a simple matching game where users flip cards to fids total and a score counter"

2. **Start simple, refine complex**
   - Generate a basic version first
   - Then refine to add features
   - Each refinement is cheaper than regenerating from scratch

3. **Match AI model to task complexity**
   - Simple apps (calculator, timer): Smaller models work fine
   - Complex apps (game with AI, data visualization): Use larger models

4. **Test on your device first**
   - Open the app immediately after generation
   - Check that UI looks good and functions work
   - Refine if needed before sharing

5. **Share responsibly**
   - Apps are HTML/JavaScript—they can do anything a web app can do
   - Only share apps you've tested
   - Users on your Wi-Fi network can access your shared apps

### Money-Saving Tips

1. **Use smaller models for simple tasks** (Mistral, Groq are fast and cheap)
2. **Batch your app creation** (create multiple apps in one session
3. **Refine rather than regenerate** (editing an app is usually cheaper)
4. **Check provider free tiers** (many offer free credits or usage)
5. **Use OpenRouter to compare models** (switch between providers in one place)

---

## Limitations & What MiniForge Can't Do

- **Can't create native Android apps** — Generated apps are HTML/web apps (still very powerful)
- **Can't access device hardware** (camera, microphone, sensors) — re access
- **Can't work offline** (generation requires internet and API calls)
- **Can't create games with complex graphics** (but simple 2D games
- **Limited to what the AI generates** (if the AI makes a mistake, the app will too)

---

## Made by Shefbi with Love 😉

MiniForge was created to give developers, creators, and dreamers the power to build their ideas without being locked into expensive platforms or giving up control of
their data.

**Philosophy**: Simple, honest, user-first technology that respectsnd your independence.

---

## Get Started Now

1. **Download MiniForge** on your Android device
2. **Get an API key** from your favorite AI provider
3. **Add it to Settings**
4. **Describe your app idea**
5. **Watch it come to life**

Your ideas. Your API. Your apps. Your control.

**Welcome to MiniForge.** 🚀
