# **StockMaster: Mobile Stock Tracking** 📦

**StockMaster** is a robust, offline-first Android application developed in Java. It is designed to modernize inventory management for small-to-medium retail businesses, specifically optimized for the Central African market (supporting FCFA and offline reliability).

---

## 📑 Table of Contents

- [Key Features](#key-features)
- [Technical Stack](#technical-stack)
- [Architecture & Design Patterns](#architecture--design-patterns)
- [Screenshots](#screenshots)
- [Installation & Setup](#installation--setup)
- [License](#license)

---

## 🚀 Key Features

| Feature | Description |
|---|---|
| 📦 **Intelligent Inventory Tracking** | Decoupled product metadata and physical stock levels with support for custom reorder thresholds. |
| 🛒 **Atomic Sales Engine** | Real-time transaction processing that ensures data integrity by synchronizing sales records with instant stock deductions. |
| 🔔 **Proactive Background Alerts** | Utilizes **Android WorkManager** to monitor stock levels in the background, firing system notifications for low-stock items even when the app is closed. |
| 📊 **Business Analytics & Reporting** | Comprehensive dashboard with real-time revenue tracking and a CSV export engine for generated financial reports. |
| 🌍 **Localized UX** | Native support for **English/French** and a flexible currency bridge (defaulting to **FCFA** with proper local formatting). |
| 🎨 **Modern UI/UX** | Built with **Material 3**, featuring a responsive design, Dark Mode support, and a guided onboarding experience for new users. |

---

## 🛠️ Technical Stack

| Category | Technology |
|---|---|
| **Language** | Java 11 (Android SDK 23+) |
| **Architecture** | MVVM (Model-View-ViewModel) + Repository Pattern |
| **Persistence** | Room ORM (SQLite) with Sync Logging |
| **Background Processing** | Jetpack WorkManager |
| **Reactive UI** | LiveData & Lifecycle-aware components |
| **Build System** | Gradle KTS |
| **External Libraries** | Lombok, UUID-Creator, Material Components |

---

## 🏗️ Architecture & Design Patterns

The project adheres to modern Android development standards:

- **Single Source of Truth:** All data flows through the Repository layer to ensure the UI is always in sync with the database.
- **Thread Safety:** Utilizes `ExecutorService` for asynchronous database writes to prevent UI jank and ANR (Application Not Responding) errors.
- **Decoupled Logic:** Strict separation between UI components and business logic via ViewModels, making the codebase highly testable and maintainable.
- **Cloud-Ready:** The schema is pre-configured with `sync_log` tables and status flags, ready for integration with a Spring Boot or similar cloud backend.

---

## 📸 Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/b884ab22-d744-49c4-a4b9-a951b473465e" width="180" alt="Screenshot 1" />
  <img src="https://github.com/user-attachments/assets/03714dc9-c4c3-475e-896d-ab67c7318b6c" width="180" alt="Screenshot 2" />
  <img src="https://github.com/user-attachments/assets/e373793a-8e4b-4c54-8400-79e27d3c6918" width="180" alt="Screenshot 3" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/ace75797-86fd-44e0-bace-c5029b800b78" width="180" alt="Screenshot 4" />
  <img src="https://github.com/user-attachments/assets/f4f613ab-dd4d-4bf4-a533-be830a6bfee9" width="180" alt="Screenshot 5" />
  <img src="https://github.com/user-attachments/assets/cacb4875-3dc0-4984-b48e-ae2b2107de70" width="180" alt="Screenshot 6" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/f856114b-b03d-4e21-9fec-0a3b5ee2364b" width="180" alt="Screenshot 7" />
  <img src="https://github.com/user-attachments/assets/a4a73c0c-854f-4c25-89d9-21b93f8ed577" width="180" alt="Screenshot 8" />
  <img src="https://github.com/user-attachments/assets/e228fc77-cdbc-4100-b822-f0ce377db715" width="180" alt="Screenshot 9" />
</p>

---

## 📥 Installation & Setup

### 1. Clone the repository

```bash
git clone https://github.com/NguinfackFranck/Stock-master.git
```

### 2. Open in Android Studio

- Open Android Studio (Hedgehog 2023.1.1 or newer recommended).
- Select `Open` and navigate to the project folder.

### 3. Sync Gradle

- Allow the project to sync dependencies via Gradle KTS.

### 4. Run the App

- Connect a physical device or start an emulator (API 23 / Android 6.0 or higher).
- Click the `Run` button.

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

### ⭐ If you find this project useful, consider giving it a star! ⭐

Made with ❤️ for the Central African retail community

</div>

---


