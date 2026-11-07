# Myzum

A native Android app, written in Kotlin and Jetpack Compose, that turns a smartphone into a contactless payment terminal (**SoftPOS / Tap to Pay**).

The goal is for merchants and freelancers to accept contactless cards and mobile wallets (Google Pay, Apple Pay) by tapping them on the back of the phone, without renting or buying a dedicated card reader. On top of payments, Myzum adds an AI layer built on the Gemini API that:

- generates natural-language receipts,
- answers questions about sales,
- helps look up products before charging.

> **Project status:** in progress. The app foundation and the AI features are implemented. **Payment processing is not integrated yet**, so the app cannot accept real payments at this stage. See [Roadmap](#roadmap).

---

## Features

| Feature | Status |
|---|---|
| Jetpack Compose UI: amount input, transaction status, AI responses | ✅ Implemented |
| NFC hardware availability check on startup | ✅ Implemented |
| Gemini integration (asynchronous calls from the ViewModel) | ✅ Implemented |
| AI-generated receipts, sales summaries and catalog assistant | ✅ Implemented (AI layer) |
| Reading cards and wallets over NFC through a certified payment SDK | 🚧 In progress |
| Backend for payment intents and secure gateway communication | 🚧 Planned |
| End-to-end testing with test cards | 🚧 Planned |

### AI use cases

- **Smart receipts.** Digital receipts with a clear, natural-language summary of the purchase, ready to share by email or messaging apps.
- **Sales summaries.** The merchant can ask questions in plain language, such as *"How much did I sell today, and what was my best hour?"*
- **Catalog assistant.** Quick product and price lookups by text before entering a charge.

---

## Architecture

The app is split into two independent flows that share the same sale data:

```
                    ┌──────────────────────────────┐
                    │     Android device (Myzum)   │
                    └──────────────┬───────────────┘
                                   │
              ┌────────────────────┴────────────────────┐
              ▼                                         ▼
  ┌───────────────────────┐                 ┌───────────────────────┐
  │  NFC reader / POS     │                 │    Gemini client      │
  │  (payment SDK)  🚧    │                 │ (GenerativeViewModel) │
  └───────────┬───────────┘                 └───────────┬───────────┘
              ▼                                         ▼
  ┌───────────────────────┐                 ┌───────────────────────┐
  │  Backend + payment    │                 │   Google AI (Gemini)  │
  │  gateway        🚧    │                 │                       │
  └───────────────────────┘                 └───────────────────────┘
```

- **Payment flow (in progress).**
  - The NFC antenna (`IsoDep`, ISO 14443) reads the card or wallet.
  - A certified payment SDK (Stripe Terminal, Adyen or SumUp; not chosen yet) will handle encryption, tokenization and authorization, in line with PCI MPoC/CPoC requirements.
  - A small backend (Node.js or Ktor) will create payment intents and keep secret keys off the device.
- **AI flow (implemented).** `GenerativeViewModel` manages the UI state and calls the Gemini API (`generateContent()`) to produce receipts, summaries and catalog answers.

**Data separation.** The AI layer only receives structured sale data such as amounts, times and products. Card data never leaves the payment layer, and nothing sensitive is sent to Gemini.

### Main files

| File | Purpose |
|---|---|
| `MainActivity.kt` | Entry point and NFC availability check |
| `GenerativeViewModel.kt` | UI state management and asynchronous calls to Gemini |
| `HomeScreen.kt` | Compose UI: amount input, transaction status and AI output |

---

## Tech stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Card reading | Android NFC (`IsoDep` / ISO 14443) |
| AI | Google Gen AI SDK (Gemini API) |
| Payment processing | Stripe Terminal, Adyen or SumUp SDK *(planned)* |
| Backend | Node.js or Ktor *(planned)* |

---

## Getting started

### Requirements

- Android Studio (recent version)
- An Android device with NFC (Android 8.0 or later)
- A Gemini API key from [Google AI Studio](https://aistudio.google.com/)

NFC features need real hardware; emulators don't support them. The payment SDK, once integrated, may require a newer Android version.

### Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/<your-user>/myzum-android.git
   ```

2. Open the project in Android Studio.

3. Add your Gemini API key to `local.properties` in the project root. This file is not committed to version control:

   ```properties
   apiKey=YOUR_GEMINI_API_KEY
   ```

4. Connect an NFC-enabled device and run the app.

### Permissions

The app declares the following in `AndroidManifest.xml`:

- `android.permission.NFC`
- `android.hardware.nfc` (required feature)
- `android.permission.INTERNET`

---

## Roadmap

- [x] App foundation in Kotlin + Jetpack Compose
- [x] NFC availability check
- [x] Gemini integration: receipts, sales summaries and catalog assistant
- [ ] Choose and integrate the payment SDK (Stripe Terminal, Adyen or SumUp)
- [ ] Backend (Node.js or Ktor) for payment intents and secure gateway communication
- [ ] End-to-end testing with test cards, from tap to AI-generated receipt

---

## Security notes

- **No card data is stored or processed by the app itself.** All sensitive payment handling is delegated to a certified payment SDK.
- **Gateway secret keys will live only on the backend,** never on the device.
- **The Gemini API key is kept in `local.properties`** for development and excluded from version control.

---

## Disclaimer

Myzum is a personal project under development. It is not a certified payment product and must not be used to process real payments.
