# Myzum

An Android app, written in Kotlin and Jetpack Compose, that simulates a smartphone-based payment terminal (**SoftPOS / Tap to Pay**) for small businesses and freelancers.

Myzum covers the full merchant experience of a card terminal:

- account registration and login,
- charging an amount by tapping an NFC tag on the phone,
- payment history and full or partial refunds,
- a product catalog,
- a sales statistics dashboard with automatic insights.

> **Project status:** in progress. All features run end to end, but **payments are simulated**: the app detects an NFC tap and processes it through a mock payment provider. No real card data is read and no real money moves. Integrating a certified payment SDK is the next milestone (see [Roadmap](#roadmap)).

---

## Features

### Accounts and security
- Registration for **individual** and **merchant** accounts: personal data, business data and payout IBAN.
- Login, plus optional **biometric unlock** (fingerprint / face) on app start.
- Payout sources: add bank cards or accounts and filter the history by source.

### Charging
- Numeric keypad to enter the amount, or build the charge from the **product catalog**.
- "Waiting for payment" screen that listens for an **NFC tap** through Android's foreground dispatch.
- Processing, approved and rejected states, with sound, vibration and a system notification on success.

### History and refunds
- Full transaction history with a detail view for each payment.
- **Full and partial refunds** with validation:
  - only approved or partially refunded payments can be refunded,
  - the amount can't exceed what is left to refund.
- Each refund updates the original payment's status and is also recorded as a separate negative movement.

### Business statistics
- Period selector: today, last 7 days, last 30 days.
- Gross sales, refunds, net sales, number of transactions and average ticket.
- Daily sales evolution and breakdown by payment method.
- Comparison with the previous period (% variation).
- **Rule-based insights**, such as growth or drop alerts, best day, high average ticket and dominant payment method.

### Settings
- Terminal name, notifications and biometric security.

---

## Architecture

The app follows **MVVM** with a **repository layer** and a local **Room** database, using Kotlin **Coroutines and Flow** to keep the UI reactive.

```
UI (Jetpack Compose screens)
        │  observes StateFlow
        ▼
ViewModels (User, Payment, Refund, Catalog, FundingSource, Statistics)
        │
        ▼
Repositories ───────────────► PaymentProvider (interface)
        │                            └─ FakePaymentProvider (current)
        ▼                            └─ real SDK provider (planned)
Room database (payments, refunds, users, products, funding sources)

NfcManager / NfcRepository ──► emits tap events to PaymentViewModel
```

- **`PaymentProvider` abstraction.** Payment and refund logic depends on an interface, not on a concrete gateway. `FakePaymentProvider` simulates network latency and approves every operation. A real provider (Stripe Terminal, Adyen or SumUp) can replace it without touching the UI or the repositories.
- **NFC.** `NfcManager` enables foreground dispatch while the terminal is waiting for a payment and exposes detected taps as a `SharedFlow`.
- **Statistics.**
  - `StatisticsRepository` aggregates payments for the selected period and the previous one.
  - `BusinessInsightEngine` turns those figures into a short list of prioritized messages.
- **Navigation.** A single activity with Navigation Compose.

### Project structure

```
app/src/main/java/com/example/myzum/
├── MainActivity.kt          # Single activity, navigation graph, NFC and biometric wiring
├── model/                   # Domain models (User, Payment, Refund, Product, statistics…)
├── nfc/                     # NfcManager, NfcRepository
├── payment/                 # PaymentProvider interface, FakePaymentProvider, result/state types
├── repository/              # Repositories
│   └── local/               # Room database, entities, DAOs, type converters
├── statistics/              # BusinessInsightEngine
├── viewmodel/               # ViewModels
├── ui/                      # Compose screens, grouped by feature
│   ├── registration/        #   welcome, account type, sign-up, login
│   ├── catalog/             #   product catalog
│   ├── funding/             #   cards and bank accounts
│   ├── refund/              #   refund flow
│   ├── statistics/          #   business dashboard
│   ├── settings/            #   settings, notifications, security
│   └── security/            #   biometric prompt
└── util/                    # Currency formatting
```

---

## Tech stack

| Area | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Architecture | MVVM, repository pattern, Coroutines + Flow |
| Persistence | Room (SQLite) |
| Hardware | Android NFC (foreground dispatch), Biometric API, vibration |
| Payments | `PaymentProvider` interface with a mock implementation *(real SDK planned)* |

---

## Getting started

### Requirements

- Android Studio (recent version)
- Android 10 (API 29) or later
- An NFC-enabled device to trigger the tap flow. Any NFC tag or contactless card works as a trigger, because the payment is simulated.

### Run

```bash
git clone https://github.com/Fyced/myzum-android.git
```

1. Open the project in Android Studio.
2. Let Gradle sync.
3. Run the app on a device.

No API keys or external services are required.

### Permissions

- `android.permission.NFC`: detect taps on the terminal.
- `android.permission.VIBRATE`: haptic feedback when a payment is approved.
- `android.permission.POST_NOTIFICATIONS`: payment notifications.
- `android.hardware.nfc` is declared as **not required**, so the app installs on devices without NFC, but they can't trigger payments.

---

## Roadmap

- [x] Accounts, login and biometric unlock
- [x] Charging flow with NFC tap detection (simulated payment)
- [x] Product catalog
- [x] Payment history, full and partial refunds
- [x] Statistics dashboard with rule-based insights
- [ ] Integrate a certified payment SDK (Stripe Terminal, Adyen or SumUp) as a real `PaymentProvider`
- [ ] Backend to create payment intents and keep gateway secrets off the device
- [ ] Hash passwords and encrypt sensitive local data (IBAN, ID numbers)
- [ ] Store amounts as integer cents (or `BigDecimal`) instead of `Double`
- [ ] Proper Room migrations instead of destructive migration
- [ ] Unit tests for refunds, statistics and insights

---

## Known limitations

- **Simulated payments.** `FakePaymentProvider` approves every operation, so rejection paths are not exercised yet.
- **Local-only accounts.** Users are stored in the on-device database. Passwords and personal data are not hashed or encrypted yet.
- **Single currency.** Amounts are handled in EUR.
- **Destructive migrations.** Schema changes wipe the local database (`fallbackToDestructiveMigration`).

---

## Disclaimer

Myzum is a personal learning project. It is not a certified payment product and must not be used to process real payments.
