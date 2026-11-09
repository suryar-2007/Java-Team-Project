# Commercial Airline Fleet Fuel Efficiency & Maintenance Analytics Engine

A production-grade, local desktop Java Swing & MySQL analytics application designed for commercial airline fleet management, flight fuel efficiency monitoring, ICAO carbon intensity compliance, three-phase telemetry ingestion, and automated engine vibration maintenance alerting.

---

## ✈️ System Overview & Architecture

The **Commercial Airline Fleet Fuel Efficiency & Maintenance Analytics Engine** is architected to give airline fleet directors, chief maintenance engineers, and fuel analysts real-time visibility into aircraft health and operational efficiency across three core commercial categories:
1. **Long-Haul International** (e.g., Boeing 777-300ER, Airbus A350-900, Boeing 787-8)
2. **Short-Haul Regional** (e.g., Boeing 737-MAX8, Airbus A320neo, Embraer E195-E2)
3. **Cargo Freight** (e.g., Boeing 777F Freighter, Boeing 747-8F Freighter)

### Core Technologies
- **Programming Language**: Java (Java 17+ / Java 26 compatible)
- **GUI Framework**: Java Swing & Java2D (Modern Navy Blue, Light Blue & Crisp White Theme)
- **Database Engine**: MySQL Server 8.0+ (`airline_fleet_db`)
- **Database Connectivity**: JDBC with strict `PreparedStatement` parameterized queries
- **Concurrency & Multithreading**: Java `ExecutorService` (4 background worker threads)
- **Build & Dependency System**: Apache Maven (`pom.xml`) & standalone compilation scripts
- **Development Environment**: Visual Studio Code (VS Code) on Windows
- **Version Control**: Git & GitHub

---

## 📁 Project Directory Structure

```
java team/
├── .vscode/
│   ├── launch.json               # 1-Click F5 debug & run configuration for VS Code
│   └── settings.json             # Java project source paths & referenced libraries
├── database/
│   └── fleet_schema.sql          # Complete DDL & DML schema with seed fleet data
├── lib/
│   └── mysql-connector-j-8.4.0.jar # Official MySQL JDBC Connector driver
├── reports/                      # Destination directory for generated TXT audit reports
├── sample-data/
│   └── flights.csv               # Sample multi-phase flight operations data
├── src/
│   ├── main/
│   │   ├── java/com/airline/fleet/
│   │   │   ├── DBConnection.java       # Centralized JDBC connection manager
│   │   │   ├── FleetDAO.java           # Data Access Object for CRUD & aggregates
│   │   │   ├── TelemetryWorker.java    # Multithreaded ExecutorService processor
│   │   │   ├── AuditExporter.java      # Formatted TXT audit report generator
│   │   │   ├── MainApp.java            # Main entry point & look-and-feel loader
│   │   │   ├── model/
│   │   │   │   ├── Aircraft.java       # Aircraft fleet inventory entity
│   │   │   │   ├── Flight.java         # Flight entity with regex validation
│   │   │   │   ├── TelemetryData.java  # 3-phase telemetry sensor readings
│   │   │   │   ├── MaintenanceFlag.java# Automated maintenance anomaly alert entity
│   │   │   │   ├── User.java           # User account & role entity
│   │   │   │   ├── FleetCategory.java  # Enum: Long-Haul, Short-Haul, Cargo
│   │   │   │   ├── FlightPhase.java    # Enum: Takeoff & Climb, Cruise, Descent & Landing
│   │   │   │   └── FleetGrade.java     # Enum: Grade A, B, C, Outlier
│   │   │   ├── service/
│   │   │   │   ├── AnalyticsService.java # Aviation math formulas (SFC, CO2, grading)
│   │   │   │   └── CsvImportService.java # Batch CSV parser with regex checks
│   │   │   └── ui/
│   │   │       ├── Theme.java          # Navy, White & Light Blue UI theme
│   │   │       ├── LoginFrame.java     # User authentication dialog
│   │   │       ├── MainDashboardFrame.java # Sidebar navigation host
│   │   │       ├── components/
│   │   │       │   ├── KpiCard.java    # Metric summary card widget
│   │   │       │   └── ChartPanel.java # High-res Java2D bar & doughnut charts
│   │   │       └── panels/
│   │   │           ├── OverviewPanel.java   # Executive dashboard & charts
│   │   │           ├── AircraftPanel.java   # Full Aircraft CRUD management
│   │   │           ├── FlightPanel.java     # Flight CRUD & 3-phase entry
│   │   │           ├── TelemetryPanel.java  # Multithreaded live sensor stream
│   │   │           ├── MaintenancePanel.java# Anomaly flags & engineer resolve
│   │   │           ├── AnalyticsPanel.java  # Aviation formulas & simulator
│   │   │           ├── DataExchangePanel.java# CSV import & TXT report export
│   │   │           └── SettingsPanel.java   # MySQL credentials & schema runner
│   │   └── resources/
│   │       ├── db.properties           # Database connection configuration
│   │       └── config.properties       # Configurable scoring & vibration limits
│   └── test/
│       └── java/com/airline/fleet/
│           ├── FleetCalculationTest.java # JUnit tests for SFC, CO2, and vibration
│           ├── RegexValidationTest.java  # JUnit tests for regex [A-Z]{2,3}\d{3,4}
│           └── TestRunner.java           # Standalone console test suite
├── .gitignore                    # Git exclusions (build artifacts, secrets)
├── pom.xml                       # Maven build configuration
├── run.bat                       # Windows Batch 1-click build & run launcher
├── run.ps1                       # Windows PowerShell launch script
└── README.md                     # Comprehensive documentation & presentation guide
```

---

## 📐 Mathematical Calculations & Aviation Standards

### 1. Specific Fuel Consumption (SFC)
Calculates the operational fuel efficiency per unit of payload carried across the stage length:
$$\text{SFC} = \frac{\text{Total Fuel Burned (kg)}}{\text{Payload Weight (kg)} \times \text{Distance (km)}}$$
- **Units**: $\text{kg fuel} / (\text{kg payload} \times \text{km})$
- **Interpretation**: Lower numerical SFC values denote superior aerodynamic and turbofan efficiency.

### 2. Fleet Carbon Intensity
Evaluates greenhouse gas emissions per passenger transport capacity, conforming to ICAO Annex 16 Volume IV recommendations:
$$\text{Fleet Carbon Intensity} = \frac{\text{Fuel Burned (kg)} \times 3.16}{\text{ASK}}$$
- **3.16**: The ICAO standard stoichiometric emission factor for Jet-A / Jet-A1 aviation fuel ($3.16\text{ kg CO}_2$ emitted per $1.0\text{ kg fuel}$ combusted).
- **ASK (Available Seat Kilometers)**:
  $$\text{ASK} = \text{Available Seats} \times \text{Distance (km)}$$
- **For Dedicated Cargo Freighters** (0 passenger seats):
  $$\text{ASK}_{\text{equiv}} = \left(\frac{\text{Payload Capacity in kg}}{100\text{ kg}}\right) \times \text{Distance (km)}$$
  *(Standard ICAO passenger allowance equivalency: $100\text{ kg} = 1\text{ passenger equivalent}$).*

### 3. Engine Vibration Safety Limits
Vibration readings are continuously monitored in **mils peak-to-peak** ($1\text{ mil} = 0.001\text{ inch}$ displacement):
- **Normal Operating Range**: $< 2.5\text{ mils}$ (Green status)
- **Caution / Advisory Range**: $2.5\text{ mils} \le \text{Vib} \le 4.0\text{ mils}$ (Amber status - Advisory recorded)
- **Critical Exceedance Limit**: $> 4.0\text{ mils}$ (Red status - **Automatic CRITICAL Maintenance Flag triggered** in MySQL for immediate engineering inspection)

### 4. Fleet Efficiency Grading
- **Grade A (Optimal)**: $\text{SFC} \le 0.000038\text{ kg}/(\text{kg}\cdot\text{km})$
- **Grade B (Nominal)**: $0.000038 < \text{SFC} \le 0.000048\text{ kg}/(\text{kg}\cdot\text{km})$
- **Grade C (Sub-optimal)**: $\text{SFC} > 0.000048\text{ kg}/(\text{kg}\cdot\text{km})$
- **OUTLIER (Anomaly Flagged)**: Fuel consumption deviation $> +25\%$ above aircraft category baseline. Flagged for airframe aerodynamic or engine degradation checks.
*(All thresholds are dynamically configurable in `system_config` table and the Analytics tab).*

---

## 🛠️ Required Java Implementations

| Class Name | Exact Role in Project |
| :--- | :--- |
| **`DBConnection.java`** | Manages MySQL JDBC connections, loads `db.properties`, provides diagnostic test functions, and executes `fleet_schema.sql` automatically. |
| **`FleetDAO.java`** | Complete Data Access Object using `PreparedStatement` for full CRUD on Aircraft, Flights, 3-Phase Telemetry, Maintenance Alerts, and KPI aggregates. |
| **`TelemetryWorker.java`** | Multithreaded background engine leveraging `ExecutorService` (4 worker threads) to process sensor streams, evaluate vibration thresholds, and trigger automated alerts. |
| **`AuditExporter.java`** | Compliance reporting engine utilizing `String.format()` and `substring()` string manipulations to export formatted TXT maintenance audit reports into `reports/`. |
| **`MainApp.java`** | Application bootstrap entry point; configures native Windows DPI/font anti-aliasing and launches the Swing Event Dispatch Thread (EDT). |

---

## 🗄️ Database Setup Instructions (MySQL)

### Step 1: Verify MySQL Service on Windows
Ensure MySQL Server is running. In PowerShell:
```powershell
Get-Service -Name MySQL80
```
If stopped, start it:
```powershell
Start-Service -Name MySQL80
```

### Step 2: Initialize Database Schema
You can initialize the database using any of the following 3 methods:

#### Method A: From inside the Application GUI (Easiest)
1. Run the application (`run.bat` or `F5` in VS Code).
2. Go to the **⚙ MySQL Configuration** tab.
3. Enter your MySQL root password and click **Save & Reconnect**.
4. Click **▶ Execute database/fleet_schema.sql**. The database and seed data will be created automatically!

#### Method B: MySQL Command Line Client
```bash
mysql -u root -p < "database/fleet_schema.sql"
```

#### Method C: MySQL Workbench
1. Open MySQL Workbench.
2. Open `database/fleet_schema.sql`.
3. Click the lightning bolt icon (Execute).

### Step 3: Configure `db.properties`
Open `src/main/resources/db.properties` and verify your credentials:
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/airline_fleet_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true
db.user=root
db.password=YOUR_MYSQL_PASSWORD
```

---

## 💻 Visual Studio Code Setup & Execution

### 1. Recommended Extensions
Open VS Code (`Ctrl+Shift+X`) and install:
1. **Extension Pack for Java** (by Microsoft)
2. **Project Manager for Java** (by Microsoft)
3. **Database Client** or **MySQL** (Optional, for browsing MySQL inside VS Code)

### 2. Opening the Project
1. Launch Visual Studio Code.
2. Select **File -> Open Folder...**
3. Select the folder named **`java team`**.
   *(Path: `C:\Users\SURYA R\OneDrive\Desktop\java team`)*

### 3. Running the Application in VS Code

#### Option 1: Press `F5` (Pre-configured Launch)
Press **`F5`** or go to the **Run & Debug** panel (`Ctrl+Shift+D`) and click **"Launch Fleet Analytics Engine"**.

#### Option 2: Run via Terminal Script
Open the integrated terminal in VS Code (`Ctrl+\``) and execute:
```powershell
.\run.bat
```
or
```powershell
.\run.ps1
```

#### Option 3: Run via Maven
```bash
mvn clean compile exec:java
```

---

## 🧪 Testing and Verification Suite

### Running Standalone Verification Suite
To run the pre-built test suite verifying all 9 engineering modules:
```powershell
javac -encoding UTF-8 -cp "bin;lib/*;src/main/resources" -d bin src/test/java/com/airline/fleet/TestRunner.java
java -cp "bin;lib/*;src/main/resources" com.airline.fleet.TestRunner
```

**Expected Output**:
```
==================================================================
   COMMERCIAL AIRLINE FLEET ANALYTICS ENGINE - TEST SUITE         
==================================================================
[PASS] 1. SFC Calculation: 1.578898486114981E-4 kg/(kg*km)
[PASS] 2. Fleet Carbon Intensity: 0.061106561106561104 kg CO2/ASK
[PASS] 3. Engine Vibration Limits (< 2.5 Normal, 2.5-4.0 Caution, > 4.0 Critical)
[PASS] 4. Fleet Efficiency Grading: Verified Grades A, B, C, and Outlier
[PASS] 5. Regex Flight Number Validation [A-Z]{2,3}\d{3,4} strictly verified
[PASS] 6. Sample CSV file exists at sample-data\flights.csv (1418 bytes)
[PASS] 7. SQL schema file exists at database\fleet_schema.sql (12933 bytes)
[PASS] 8. Multithreaded TelemetryWorker initialized with ExecutorService pool
[PASS] 9. TXT Maintenance Report generated at reports/fleet_maintenance_report_*.txt
==================================================================
  TEST SUMMARY: 9 PASSED, 0 FAILED
==================================================================
```

---

## 🐙 Git and GitHub Submission Workflow

Initialize Git, commit all verified source files, and push to your GitHub repository:

```powershell
# 1. Initialize git repository inside the "java team" folder
git init

# 2. Stage all project files (ignoring target/ and compiled binaries)
git add .

# 3. Create initial commit
git commit -m "feat: complete commercial airline fleet fuel efficiency & maintenance analytics engine"

# 4. Link to your GitHub repository
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git

# 5. Push to main branch
git branch -M main
git push -u origin main
```

---

## 🎓 College Presentation & Viva Guide (For Professors)

| Question | Answer Explanation to Professor |
| :--- | :--- |
| **Q1: Why is Specific Fuel Consumption (SFC) calculated using Payload and Distance?** | *"In commercial aviation economics, calculating fuel burn per passenger-km or tonne-km isolates aerodynamic and engine efficiency from varying flight lengths and payload weights, enabling direct comparison across Long-Haul, Short-Haul, and Cargo categories."* |
| **Q2: What is the origin of the 3.16 factor in Carbon Intensity?** | *"3.16 is the standard ICAO Annex 16 stoichiometric combustion constant: 1 kg of aviation kerosene (Jet-A/Jet-A1) chemically reacts with atmospheric oxygen to produce 3.16 kg of carbon dioxide ($CO_2$)."* |
| **Q3: How does the multithreaded TelemetryWorker operate?** | *"The `TelemetryWorker` utilizes a Java `ExecutorService` with a fixed thread pool of 4 worker threads. When high-frequency three-phase sensor packets arrive, worker threads concurrently compute vibration peak levels and anomaly deviations without freezing or blocking the Java Swing Event Dispatch Thread (EDT)."* |
| **Q4: How does the Engine Vibration alert mechanism protect aircraft?** | *"Vibration is tracked in mils pk-pk across Engine 1 and Engine 2. If vibration exceeds the caution threshold (2.5 mils), a Warning advisory is recorded. If it crosses the critical limit (4.0 mils), an automatic CRITICAL Maintenance Flag is persisted into MySQL and visually alerts the chief engineer for immediate borescope inspection."* |
| **Q5: What regex pattern validates Flight Numbers?** | *"The regex `^[A-Z]{2,3}\\d{3,4}$` requires a 2 to 3 letter IATA/ICAO airline designator (e.g., 'AA', 'BA', 'DL', 'UAE') followed by 3 to 4 numeric route digits (e.g., '101', '1402')."* |
