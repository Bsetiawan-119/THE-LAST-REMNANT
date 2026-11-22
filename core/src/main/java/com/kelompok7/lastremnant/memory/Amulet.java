package com.kelompok7.lastremnant.memory;

/** ID unik tiga bagian kunci Kalpataru. */
public enum Amulet {
    KENANG("Kenang", "Daun perunggu dari radio Pak Darto."),
    SESAL("Sesal", "Akar perak dari brankas di Perpustakaan Arsip."),
    RINDU("Rindu", "Bunga emas yang digenggam Kruk di inti Sang Exitium.");

    private final String title;
    private final String text;

    Amulet(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String title() { return title; }
    public String text() { return text; }
}
