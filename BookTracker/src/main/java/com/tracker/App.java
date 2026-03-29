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

public class App {

    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:my_database.db");

        conn.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS reading_habits (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        habitID INTEGER,
                        userID INTEGER,
                        pagesRead INTEGER,
                        book TEXT,
                        SubmissionTime DATE
                    )
                """);

        conn.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        userID INTEGER,
                        age INTEGER,
                        gender INTEGER,
                        FOREIGN KEY (userID) REFERENCES reading_habits(userID)
                    )
                """);

        FileInputStream fis = new FileInputStream("reading_habits_dataset.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);
        XSSFSheet userSheet = workbook.getSheetAt(1);

        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO reading_habits (habitID, userID, pagesRead, book, SubmissionTime) VALUES (?, ?, ?, ?, ?)");

        boolean firstRow = true;
        for (var row : sheet) {
            if (firstRow) {
                firstRow = false;
                continue;
            }

            ps.setInt(1, (int) Double.parseDouble(row.getCell(0).toString())); // habitID
            ps.setInt(2, (int) Double.parseDouble(row.getCell(1).toString())); // userID
            ps.setInt(3, (int) Double.parseDouble(row.getCell(2).toString())); // pagesRead
            ps.setString(4, row.getCell(3).toString()); // book
            ps.setString(5, row.getCell(4).toString());

            ps.executeUpdate();
        }
        PreparedStatement userPs = conn.prepareStatement(
                "INSERT INTO users (userID, age, gender) VALUES (?, ?, ?)");

        boolean firstUserRow = true;
        for (var row : userSheet) {
            if (firstUserRow) {
                firstUserRow = false;
                continue;
            } // skip header

            userPs.setInt(1, (int) Double.parseDouble(row.getCell(0).toString())); // userID
            userPs.setInt(2, (int) Double.parseDouble(row.getCell(1).toString())); // age
            userPs.setString(3, row.getCell(2).toString()); // gender
            userPs.executeUpdate();
        }

        workbook.close();

        // Read it back
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM reading_habits");
        while (rs.next()) {
            System.out.println(
                    rs.getInt("habitID") + " | " +
                            rs.getInt("userID") + " | " +
                            rs.getInt("pagesRead") + " | " +
                            rs.getString("book") + " | " +
                            rs.getString("SubmissionTime"));
        }

        ResultSet resultUser = conn.createStatement().executeQuery("SELECT * FROM users");
        while (resultUser.next()) {
            System.out.println(
                    resultUser.getInt("userID") + " | " +
                            resultUser.getInt("age") + " | " +
                            resultUser.getString("gender") + " | " +
                            resultUser.getInt("id"));
        }

        conn.close();
    }

}