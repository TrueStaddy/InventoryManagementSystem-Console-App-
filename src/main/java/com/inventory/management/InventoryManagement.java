package com.inventory.management;

import com.inventory.management.model.Device;
import com.inventory.management.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InventoryManagement {
    private static String currentUserEmail = "";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            System.out.println("Willkommen zum Geräteverwaltungssystem");

            while (true) {
                if (currentUserEmail.isEmpty()) {
                    System.out.print("Bitte geben Sie Ihre E-Mail-Adresse ein (oder 'exit' zum Beenden): ");
                    String input = scanner.nextLine().trim();

                    if ("exit".equalsIgnoreCase(input)) {
                        break;
                    }

                    if (!loginUser(input)) {
                        System.out.println("Ungültige E-Mail-Adresse. Bitte versuchen Sie es erneut.");
                        continue;
                    }
                }

                if (!showMainMenu()) {
                    break;
                }
            }

            System.out.println("Programm wird beendet. Auf Wiedersehen!");
        } finally {
            scanner.close();
        }
    }

    private static boolean loginUser(String email) {
        String sql = "SELECT COUNT(*) FROM tbl_Users WHERE Email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                currentUserEmail = email;
                System.out.println("Erfolgreich angemeldet als: " + email);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Datenbankfehler: " + e.getMessage());
        }
        return false;
    }

    private static boolean showMainMenu() {
        while (true) {
            System.out.println("\nHauptmenü - Angemeldet als: " + currentUserEmail);
            System.out.println("1. Gerät zuweisen");
            System.out.println("2. Gerät zurückgeben");
            System.out.println("3. Anderen Benutzer anmelden");
            System.out.println("4. Beenden");
            System.out.print("Bitte wählen Sie eine Option (1-4): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> assignDevice();
                case "2" -> returnDevice();
                case "3" -> {
                    currentUserEmail = "";
                    return true;
                }
                case "4" -> {
                    return false;
                }
                default -> System.out.println("Ungültige Auswahl. Bitte versuchen Sie es erneut.");
            }
        }
    }

    private static void assignDevice() {
        List<Device> devices = getUnassignedDevices();
        if (devices.isEmpty()) {
            System.out.println("Keine verfügbaren Geräte vorhanden.");
            return;
        }

        System.out.println("\nVerfügbare Geräte:");
        devices.forEach(System.out::println);

        System.out.print("\nGeben Sie die Geräte-ID ein (oder 'abbrechen'): ");
        String input = scanner.nextLine().trim();

        if ("abbrechen".equalsIgnoreCase(input)) {
            return;
        }

        try {
            int deviceId = Integer.parseInt(input);
            if (devices.stream().anyMatch(d -> d.getId() == deviceId)) {
                assignDeviceToUser(deviceId, currentUserEmail);
            } else {
                System.out.println("Ungültige Geräte-ID.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ungültige Eingabe. Bitte geben Sie eine Nummer ein.");
        }
    }

    private static void returnDevice() {
        List<Device> devices = getUserDevices(currentUserEmail);
        if (devices.isEmpty()) {
            System.out.println("Sie haben keine zugewiesenen Geräte.");
            return;
        }

        System.out.println("\nIhre zugewiesenen Geräte:");
        devices.forEach(System.out::println);

        System.out.print("\nGeben Sie die Geräte-ID ein (oder 'abbrechen'): ");
        String input = scanner.nextLine().trim();

        if ("abbrechen".equalsIgnoreCase(input)) {
            return;
        }

        try {
            int deviceId = Integer.parseInt(input);
            if (devices.stream().anyMatch(d -> d.getId() == deviceId)) {
                returnDeviceFromUser(deviceId, currentUserEmail);
            } else {
                System.out.println("Ungültige Geräte-ID.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ungültige Eingabe. Bitte geben Sie eine Nummer ein.");
        }
    }

    private static List<Device> getUnassignedDevices() {
        List<Device> devices = new ArrayList<>();
        String sql = "SELECT _id, Hersteller, Bezeichnung, Seriennummer, Status FROM tbl_Devices WHERE user_ui IS NULL AND Status = 'FUNKTIONSFAEHIG'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Device device = new Device();
                device.setId(rs.getInt("_id"));
                device.setHersteller(rs.getString("Hersteller"));
                device.setBezeichnung(rs.getString("Bezeichnung"));
                device.setSeriennummer(rs.getString("Seriennummer"));
                device.setStatus(rs.getString("Status"));
                devices.add(device);
            }
        } catch (SQLException e) {
            System.out.println("Datenbankfehler: " + e.getMessage());
        }
        return devices;
    }

    private static List<Device> getUserDevices(String email) {
        List<Device> devices = new ArrayList<>();
        String sql = """
            SELECT tbl_Devices._id, tbl_Devices.Hersteller, tbl_Devices.Bezeichnung, 
                   tbl_Devices.Seriennummer, tbl_Devices.Status
            FROM tbl_Devices
            WHERE user_ui IN (SELECT ui FROM tbl_Users WHERE Email = ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = new Device();
                device.setId(rs.getInt("_id"));
                device.setHersteller(rs.getString("Hersteller"));
                device.setBezeichnung(rs.getString("Bezeichnung"));
                device.setSeriennummer(rs.getString("Seriennummer"));
                device.setStatus(rs.getString("Status"));
                devices.add(device);
            }
        } catch (SQLException e) {
            System.out.println("Datenbankfehler: " + e.getMessage());
        }
        return devices;
    }

    private static void assignDeviceToUser(int deviceId, String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Get user_ui
                String userUi;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT ui FROM tbl_Users WHERE Email = ?")) {
                    stmt.setString(1, email);
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) throw new SQLException("Benutzer nicht gefunden");
                    userUi = rs.getString(1);
                }

                // Update device
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE tbl_Devices SET user_ui = ? WHERE _id = ? AND user_ui IS NULL AND Status = 'FUNKTIONSFAEHIG'")) {
                    stmt.setString(1, userUi);
                    stmt.setInt(2, deviceId);
                    if (stmt.executeUpdate() > 0) {
                        // Add usage record
                        try (PreparedStatement usageStmt = conn.prepareStatement("""
                            INSERT INTO tbl_GeraeteVerwendung (device_ui, user_ui, ZuweisungsDatum) 
                            SELECT ui, ?, GETDATE() 
                            FROM tbl_Devices 
                            WHERE _id = ?
                            """)) {
                            usageStmt.setString(1, userUi);
                            usageStmt.setInt(2, deviceId);
                            usageStmt.executeUpdate();
                        }
                        conn.commit();
                        System.out.println("Gerät erfolgreich zugewiesen");
                    } else {
                        throw new SQLException("Gerät nicht verfügbar");
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Fehler bei der Zuweisung: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Datenbankfehler: " + e.getMessage());
        }
    }

    private static void returnDeviceFromUser(int deviceId, String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update device
                try (PreparedStatement stmt = conn.prepareStatement("""
                    UPDATE tbl_Devices SET user_ui = NULL
                    WHERE _id = ? AND user_ui IN (
                        SELECT ui FROM tbl_Users WHERE Email = ?
                    )
                    """)) {
                    stmt.setInt(1, deviceId);
                    stmt.setString(2, email);

                    if (stmt.executeUpdate() > 0) {
                        try (PreparedStatement usageStmt = conn.prepareStatement("""
                            UPDATE tbl_GeraeteVerwendung SET RueckgabeDatum = GETDATE()
                            WHERE device_ui IN (
                                SELECT ui FROM tbl_Devices WHERE _id = ?
                            )
                            AND user_ui IN (
                                SELECT ui FROM tbl_Users WHERE Email = ?
                            )
                            AND RueckgabeDatum IS NULL
                            """)) {
                            usageStmt.setInt(1, deviceId);
                            usageStmt.setString(2, email);
                            usageStmt.executeUpdate();
                        }
                        conn.commit();
                        System.out.println("Gerät erfolgreich zurückgegeben");
                    } else {
                        throw new SQLException("Gerät nicht gefunden oder nicht Ihnen zugewiesen");
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Fehler bei der Rückgabe: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Datenbankfehler: " + e.getMessage());
        }
    }
}