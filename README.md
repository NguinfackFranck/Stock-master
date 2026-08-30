# **StockMaster: Professional Retail Inventory & Sales System**

**StockMaster** is a robust, offline-first Android application developed in Java. It is designed to modernize inventory management for small-to-medium retail businesses, specifically optimized for the Central African market (supporting FCFA and offline reliability). 

This project was developed as part of a final-year thesis to evaluate the impact of digital tracking systems on retail efficiency.

---

## **🚀 Key Features**

*   **📦 Intelligent Inventory Tracking:** Decoupled product metadata and physical stock levels with support for custom reorder thresholds.
*   **🛒 Atomic Sales Engine:** Real-time transaction processing that ensures data integrity by synchronizing sales records with instant stock deductions.
*   **🔔 Proactive Background Alerts:** Utilizes **Android WorkManager** to monitor stock levels in the background, firing system notifications for low-stock items even when the app is closed.
*   **📊 Business Analytics & Reporting:** Comprehensive dashboard with real-time revenue tracking and a CSV export engine for generated financial reports.
*   **🌍 Localized UX:** Native support for **English/French** and a flexible currency bridge (defaulting to **FCFA** with proper local formatting).
*   **🎨 Modern UI/UX:** Built with **Material 3**, featuring a responsive design, Dark Mode support, and a guided onboarding experience for new users.

---

## **🛠️ Technical Stack**

*   **Language:** Java 11 (Android SDK 23+)
*   **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
*   **Persistence:** Room ORM (SQLite) with Sync Logging
*   **Background Processing:** Jetpack WorkManager
*   **Reactive UI:** LiveData & Lifecycle-aware components
*   **Build System:** Gradle KTS
*   **External Libraries:** Lombok, UUID-Creator, Material Components

---

## **🏗️ Architecture & Design Patterns**

The project adheres to modern Android development standards:
*   **Single Source of Truth:** All data flows through the Repository layer to ensure the UI is always in sync with the database.
*   **Thread Safety:** Utilizes `ExecutorService` for asynchronous database writes to prevent UI jank and ANR (Application Not Responding) errors.
*   **Decoupled Logic:** Strict separation between UI components and business logic via ViewModels, making the codebase highly testable and maintainable.
*   **Cloud-Ready:** The schema is pre-configured with `sync_log` tables and status flags, ready for integration with a Spring Boot or similar cloud backend.

---

## **📥 Installation & Setup**

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/NguinfackFranck/Stock-master.git
    ```
2.  **Open in Android Studio:**
    *   Open Android Studio (Hedgehog 2023.1.1 or newer recommended).
    *   Select `Open` and navigate to the project folder.
3.  **Sync Gradle:**
    *   Allow the project to sync dependencies via Gradle KTS.
4.  **Run the App:**
    *   Connect a physical device or start an emulator (API 23 / Android 6.0 or higher).
    *   Click the `Run` button.

---

## **🎓 Thesis Context**
This application serves as the core technical artifact for the thesis: **"Evaluation of Digital Inventory Systems for Retail Efficiency."** It demonstrates the transition from traditional manual bookkeeping to a data-driven, automated mobile solution.

---

## **📄 License**
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
