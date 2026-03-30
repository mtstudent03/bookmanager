package com.tracker;

import java.sql.*;
import java.util.Date;
import java.util.Scanner;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;

class ReadingHabit {
    public int habitId;
    public int pagesRead;
    public String book;
    public Date SubmissionTime;
    public int userId;

    public ReadingHabit(int habitId, int pagesRead, String book, Date SubmissionTime, int userId) {
        this.habitId = habitId;
        this.pagesRead = pagesRead;
        this.book = book;
        this.SubmissionTime = SubmissionTime;
        this.userId = userId;
    }
}

class User {
    int userId;
    int age;
    String gender;

    public User(int userId, int age, String gender) {
        this.userId = userId;
        this.age = age;
        this.gender = gender;
    }
}

class DatabaseManager {
    private Connection conn;

    public DatabaseManager() throws SQLException {
        this.conn = DriverManager.getConnection("jdbc:sqlite:my_database.db");
    }

    public void createTables() throws SQLException {
        conn.createStatement().execute("PRAGMA foreign_keys = ON");
        conn.createStatement().execute("DROP TABLE IF EXISTS reading_habits"); // child first!
        conn.createStatement().execute("DROP TABLE IF EXISTS users");

        conn.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        userID INTEGER UNIQUE,
                        age INTEGER,
                        gender TEXT
                    )
                """);

        conn.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS reading_habits (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        habitID INTEGER,
                        userID INTEGER,
                        pagesRead INTEGER,
                        book TEXT,
                        SubmissionTime TEXT,
                        FOREIGN KEY (userID) REFERENCES users(userID)
                    )
                """);

    }

    public void insertReadingHabit(ReadingHabit habit) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO reading_habits (habitID, userID, pagesRead, book, SubmissionTime) VALUES (?, ?, ?, ?, ?)");
        ps.setInt(1, habit.habitId);
        ps.setInt(2, habit.userId);
        ps.setInt(3, habit.pagesRead);
        ps.setString(4, habit.book);
        ps.setString(5, habit.SubmissionTime.toString());
        ps.executeUpdate();
    }

    public void updateBookTitle(String oldTitle, String newTitle) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE reading_habits SET book = ? WHERE book = ?");
        ps.setString(1, newTitle);
        ps.setString(2, oldTitle);
        ps.executeUpdate();
        System.out.println("All rows with that title have been updated!");
    }

    public void deleteHabit(int habitId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM reading_habits WHERE habitID = ?");
        ps.setInt(1, habitId);
        ps.executeUpdate();
        System.out.println("Habit deleted!");
    }

    public void addUser(int userId, int age, String gender) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (userID, age, gender) VALUES (?, ?, ?)");
        ps.setInt(1, userId);
        ps.setInt(2, age);
        ps.setString(3, gender);
        ps.executeUpdate();
    }

    public void readAllUsers() throws SQLException {
        ResultSet resultUser = conn.createStatement().executeQuery("SELECT * FROM users");
        while (resultUser.next()) {
            System.out.println(
                    resultUser.getInt("userID") + " | " +
                            resultUser.getInt("age") + " | " +
                            resultUser.getString("gender") + " | " +
                            resultUser.getInt("id"));
        }

    }

    public void readAllReadingHabits() throws SQLException {
        ResultSet resultHabit = conn.createStatement().executeQuery("SELECT * FROM reading_habits");
        while (resultHabit.next()) {
            System.out.println(
                    resultHabit.getInt("habitID") + " | " +
                            resultHabit.getInt("userID") + " | " +
                            resultHabit.getInt("pagesRead") + " | " +
                            resultHabit.getString("book") + " | " +
                            resultHabit.getString("SubmissionTime") + " | " +
                            resultHabit.getInt("id"));
        }
    }

    public void readHabitsByUser(int userId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM reading_habits WHERE userID = ?");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            System.out.println(
                    rs.getInt("habitID") + " | " +
                            rs.getInt("pagesRead") + " | " +
                            rs.getString("book") + " | " +
                            rs.getString("SubmissionTime"));
        }
    }

    public void close() throws SQLException {
        conn.close();
    }
}

class ExcelLoader {

    public static void load(DatabaseManager db, String filePath) throws Exception {
        FileInputStream fis = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);

        // Load users from sheet 2
        XSSFSheet userSheet = workbook.getSheetAt(1);
        boolean firstUserRow = true;
        for (var row : userSheet) {
            if (firstUserRow) {
                firstUserRow = false;
                continue;
            }
            db.addUser(
                    (int) Double.parseDouble(row.getCell(0).toString()),
                    (int) Double.parseDouble(row.getCell(1).toString()),
                    row.getCell(2).toString());
        }

        // Load reading habits from sheet 1
        XSSFSheet habitSheet = workbook.getSheetAt(0);
        boolean firstRow = true;
        for (var row : habitSheet) {
            if (firstRow) {
                firstRow = false;
                continue;
            }

            ReadingHabit habit = new ReadingHabit(
                    (int) Double.parseDouble(row.getCell(0).toString()), // habitId
                    (int) Double.parseDouble(row.getCell(2).toString()), // pagesRead
                    row.getCell(3).toString(), // book
                    new Date(), // SubmissionTime
                    (int) Double.parseDouble(row.getCell(1).toString()) // userId
            );
            db.insertReadingHabit(habit);
        }

        workbook.close();
    }
}

public class App {

    public static void main(String[] args) throws Exception {
        DatabaseManager db = new DatabaseManager();
        db.createTables();
        ExcelLoader.load(db, "reading_habits_dataset.xlsx");

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n--- Reading Habit Tracker ---");
            System.out.println("1. View all reading habits");
            System.out.println("2. View all users");
            System.out.println("3. View habits by user ID");
            System.out.println("4. Add a user");
            System.out.println("5. Update book title");
            System.out.println("6. Delete a habit");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    db.readAllReadingHabits();
                    break;

                case 2:
                    db.readAllUsers();
                    break;

                case 3:
                    System.out.print("Enter user ID: ");
                    int userId = scanner.nextInt();
                    db.readHabitsByUser(userId);
                    break;

                case 4:
                    System.out.print("Enter user ID: ");
                    int newUserId = scanner.nextInt();
                    System.out.print("Enter age: ");
                    int age = scanner.nextInt();
                    System.out.print("Enter gender: ");
                    String gender = scanner.next();
                    db.addUser(newUserId, age, gender);
                    System.out.println("User added!");
                    break;

                case 5:

                    System.out.print("Enter current book title: ");
                    scanner.nextLine(); // clears the buffer
                    String oldTitle = scanner.nextLine();
                    System.out.print("Enter new book title: ");
                    String newTitle = scanner.nextLine();
                    db.updateBookTitle(oldTitle, newTitle);
                    break;

                case 6:
                    System.out.print("Enter habit ID to delete: ");
                    int habitId = scanner.nextInt();
                    db.deleteHabit(habitId);
                    break;

                case 7:
                    running = false;
                    System.out.println("Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option, try again.");
            }
        }

        scanner.close();
        db.close();
    }
}
