package com.inventory.management.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Device {
    private int id;
    private String ui;
    private LocalDate dt;
    private String hersteller;
    private String bezeichnung;
    private String kategorie;
    private String seriennummer;
    private LocalDate kaufdatum;
    private BigDecimal kaufpreis;
    private String status;
    private String userUi;

    // Конструктор по умолчанию
    public Device() {}

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUi() { return ui; }
    public void setUi(String ui) { this.ui = ui; }

    public LocalDate getDt() { return dt; }
    public void setDt(LocalDate dt) { this.dt = dt; }

    public String getHersteller() { return hersteller; }
    public void setHersteller(String hersteller) { this.hersteller = hersteller; }

    public String getBezeichnung() { return bezeichnung; }
    public void setBezeichnung(String bezeichnung) { this.bezeichnung = bezeichnung; }

    public String getKategorie() { return kategorie; }
    public void setKategorie(String kategorie) { this.kategorie = kategorie; }

    public String getSeriennummer() { return seriennummer; }
    public void setSeriennummer(String seriennummer) { this.seriennummer = seriennummer; }

    public LocalDate getKaufdatum() { return kaufdatum; }
    public void setKaufdatum(LocalDate kaufdatum) { this.kaufdatum = kaufdatum; }

    public BigDecimal getKaufpreis() { return kaufpreis; }
    public void setKaufpreis(BigDecimal kaufpreis) { this.kaufpreis = kaufpreis; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserUi() { return userUi; }
    public void setUserUi(String userUi) { this.userUi = userUi; }

    @Override
    public String toString() {
        return "ID: " + id +
                ", " + hersteller +
                " " + bezeichnung +
                ", SN: " + seriennummer;
    }
}