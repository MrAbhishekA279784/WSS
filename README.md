# Women Safety System

A **Java 21 Swing** desktop application built with **FlatLaf**, **SQLite (JDBC)**, and **Maven**, implementing a portrait-oriented personal safety companion UI with full local database persistence.

---

## Technology Stack

- **Language**: Java 21
- **GUI**: Java Swing, Java AWT (`CardLayout`, `GridBagLayout`, custom Java 2D painting & vector icons)
- **Theme**: FlatLaf (`com.formdev:flatlaf`)
- **Database**: SQLite (`org.xerial:sqlite-jdbc`) with JDBC `PreparedStatement`, `ResultSet`, and foreign-key enforcement (`PRAGMA foreign_keys = ON`)
- **Security**: Java built-in `PBKDF2WithHmacSHA256` password hashing with 16-byte random salt
- **Build Tool**: Apache Maven

---

## Project Architecture

```text
src/main/java/com/womensafety/
├── Main.java                         # SwingUtilities EDT entry point
├── config/
│   ├── AppConfig.java                # Viewport constants, configurable emergency numbers, gateway checks
│   └── DatabaseConfig.java           # SQLite JDBC URL & database path resolution
├── database/
│   ├── DatabaseManager.java          # Connection provider with PRAGMA foreign_keys = ON
│   └── DatabaseInitializer.java      # Auto-creates tables on startup without deleting data
├── models/                           # User, EmergencyContact, SOSRecord, LocationRecord, UserSettings
├── repositories/                     # PreparedStatement JDBC repositories
├── services/                         # AuthService, UserService, ContactService, LocationService,
│                                     # SOSService, HistoryService, NotificationService, SessionManager
├── ui/
│   ├── Theme.java                    # Fixed hex palette & typography
│   ├── AppFrame.java                 # CardLayout desktop window (420x840)
│   ├── SplashScreen.java             # Screen 01
│   ├── LoginScreen.java              # Screen 02
│   ├── RegisterScreen.java           # Account registration flow
│   ├── DashboardScreen.java          # Screen 03
│   ├── SOSActivationScreen.java      # Screen 04
│   ├── LocationScreen.java           # Screen 05
│   ├── ContactsScreen.java           # Screen 06
│   ├── HistoryScreen.java            # Screen 07
│   ├── EmergencySupportScreen.java   # Screen 08
│   ├── ProfileScreen.java            # Screen 09
│   ├── components/                   # SOSButton, NavigationBar, ContactCard, EmergencyCard,
│   │                                 # RoundedPanel, PrimaryButton, IconInputField, VectorIcons
│   └── dialogs/                      # SOSConfirmationDialog, AddContactDialog, EditContactDialog,
│                                     # ChangePasswordDialog, EditProfileDialog, SOSDetailsDialog,
│                                     # AppSettingsDialog, ForgotPasswordDialog, NotificationsDialog
└── utils/                            # PasswordUtils, ValidationUtils, MapUtils, DateTimeUtils
```

---

## Database Schema (`women_safety.db`)

Tables created automatically on startup by `DatabaseInitializer`:
1. `users` (`id`, `full_name`, `email`, `password_hash`, `created_at`)
2. `emergency_contacts` (`id`, `user_id`, `contact_name`, `phone_number`, `relationship`, `created_at`)
3. `sos_history` (`id`, `user_id`, `status`, `latitude`, `longitude`, `location_type`, `created_at`, `cancelled_at`)
4. `location_history` (`id`, `user_id`, `latitude`, `longitude`, `address`, `location_type`, `created_at`)
5. `user_settings` (`id`, `user_id`, `theme`, `notification_enabled`, `location_sharing_enabled`)

---

## Setup & Run Instructions

### Prerequisites
- **Java Development Kit (JDK) 21**
- **Apache Maven 3.8+**
- **IntelliJ IDEA** (optional IDE)

### Run via Maven (Recommended)
```bash
mvn clean compile exec:java
```

### Run Automated Unit & SQLite Integration Tests
```bash
mvn test
```

### Run in IntelliJ IDEA
1. Open the project folder (`pom.xml`) as a Maven project in IntelliJ IDEA.
2. Ensure Project SDK is set to **Java 21**.
3. Wait for Maven dependencies (`sqlite-jdbc`, `flatlaf`) to sync.
4. Run `com.womensafety.Main`.

---

## Honest External Integration Behavior

- **Location**: Because standard desktop JVMs do not expose GPS hardware, `Live Location` clearly identifies itself as **Simulated Location Mode** (`SIMULATED` / `MANUAL`), allows updating coordinates, persists snapshots to `location_history`, copies shareable Google Maps links (`https://www.google.com/maps?q=LAT,LNG`), and opens them in your default browser via `Desktop.getDesktop().browse(...)`.
- **SOS Notifications**: If `WSS_SMS_WEBHOOK_URL` is not configured in the environment, the SOS Activation screen honestly displays that external notification delivery is **not configured** while still persisting the SOS event to SQLite.
- **Phone Calls**: Clicking call on Trusted Contacts or Emergency Support prompts for confirmation and dispatches a `tel:` URI to the OS handler, reporting honestly if the desktop OS has no registered dialer.
- **Configurable Emergency Numbers**: Override defaults via JVM system properties if needed:
  `-Dwss.emergency.police=100 -Dwss.emergency.women=181 -Dwss.emergency.ambulance=108 -Dwss.emergency.all=112`

---

## Feature Testing Checklist

- [ ] **Splash Screen**: Launches cleanly, initializes `women_safety.db`, transitions to Login.
- [ ] **Registration**: Validates name, email, password strength, password match, rejects duplicate email, hashes with PBKDF2.
- [ ] **Login & Forgot Password**: Authenticates against SQLite; rejects wrong credentials; resets password by email.
- [ ] **Dashboard**: Displays logged-in user's first name, glowing SOS button, 2x2 action cards, and real database notification badge.
- [ ] **SOS Workflow**: Opens confirmation dialog, executes 3 progress steps, saves `ACTIVE` record to `sos_history`, allows cancelling to `CANCELLED` with `cancelled_at` timestamp.
- [ ] **Live Location**: Shows clearly labelled simulated coordinates, updates coordinates, copies Google Maps URL, opens Google Maps in browser.
- [ ] **Trusted Contacts**: Starts empty (no fake contacts), adds/edits/deletes contacts in SQLite, prevents duplicate phone numbers, confirms before calling.
- [ ] **SOS History**: Lists real `sos_history` entries with status icons & opens `SOSDetailsDialog`.
- [ ] **Emergency Support**: Displays Police (100), Women Helpline (181), Ambulance (108), Emergency (112) with confirmation before OS call dispatch.
- [ ] **Profile & Persistence**: Updates profile name/email, changes password securely, updates `user_settings`, logs out, and preserves all data across application restarts.
