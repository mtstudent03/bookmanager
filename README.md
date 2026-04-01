# 📚 Reading Habit Tracker

A command-line Java application that tracks users' reading habits. Data is loaded from an Excel spreadsheet and stored in a local SQLite database, with a simple interactive menu for querying and managing records.

---

## Features

- Load users and reading habits from an Excel file (`.xlsx`) on startup
- Persistent storage via a local SQLite database
- Interactive CLI menu to:
  - View all reading habits or filter by user
  - View all users
  - Add new users
  - Update book titles
  - Delete reading habit entries
  - View statistics (mean user age, total pages read, users per book, multi-book readers)

---

## Tech Stack

| Component        | Technology              |
|-----------------|-------------------------|
| Language         | Java 17+                |
| Build Tool       | Maven                   |
| Database         | SQLite (via JDBC)       |
| Excel Parsing    | Apache POI (XSSF)       |

---

## Project Structure

```
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── tracker/
│                   └── App.java        # Main application, all classes
├── reading_habits_dataset.xlsx         # Input data (required at runtime)
├── my_database.db                      # SQLite DB (auto-created on first run)
├── pom.xml                             # Maven build config
└── README.md
```

---

## Prerequisites

- Java 17 or higher
- Maven 3.6+

---

## Getting Started

**1. Clone the repository**

```bash
git clone https://github.com/your-username/reading-habit-tracker.git
cd reading-habit-tracker
```

**2. Place the Excel file**

Ensure `reading_habits_dataset.xlsx` is in the project root directory. The file must have:
- **Sheet 1** — Reading habits (columns: habitID, userID, pagesRead, book)
- **Sheet 2** — Users (columns: userID, age, gender, name)

**3. Build the project**

```bash
mvn clean package
```

**4. Run the application**

```bash
java -jar target/tracker-1.0-SNAPSHOT.jar
```

> ⚠️ The database tables are **dropped and recreated** on every startup, and data is re-loaded from the Excel file.

---

## Usage

Once running, you will see an interactive menu:

```
--- Reading Habit Tracker ---
1.  View all reading habits
2.  View all users
3.  View habits by user ID
4.  Add a user
5.  Update book title
6.  Delete a habit
7.  Provide mean age of users
8.  Provide total users that read a specific book
9.  Provide total pages read by all users
10. Provide total users that read more than one book
11. Exit
Choose an option:
```

Enter the number of your desired action and follow the prompts.

---

## Database Schema

**`users`**

| Column   | Type    | Notes              |
|----------|---------|--------------------|
| id       | INTEGER | Auto-increment PK  |
| userID   | INTEGER | Unique             |
| age      | INTEGER |                    |
| gender   | TEXT    |                    |
| Name     | TEXT    |                    |

**`reading_habits`**

| Column         | Type    | Notes                        |
|----------------|---------|------------------------------|
| id             | INTEGER | Auto-increment PK            |
| habitID        | INTEGER |                              |
| userID         | INTEGER | Foreign key → users(userID)  |
| pagesRead      | INTEGER |                              |
| book           | TEXT    |                              |
| SubmissionTime | TEXT    |                              |

---

## Known Limitations

- The database is fully reset on every application start; manually added users/habits are not persisted across restarts unless the Excel file is updated.
- `SubmissionTime` is recorded as the current timestamp at load time, not sourced from the Excel file.

---

## License

This project is for educational purposes. Feel free to use and adapt it.
