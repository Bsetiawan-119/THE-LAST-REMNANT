package com.kelompok7.lastremnant.memory;

/** Tiga bagian kunci Kalpataru memakai slot MemoryDevice yang sudah ada. */
public enum Amulet {
    KENANG("kenang", "Kenang", "Liontin perunggu berbentuk daun dari radio Pak Darto."),
    SESAL("sesal", "Sesal", "Amulet dari brankas di Perpustakaan Arsip."),
    RINDU("rindu", "Rindu", "Amulet yang dibawa Kruk.");

    private final MemoryFragment fragment;

    Amulet(String id, String name, String description) {
        fragment = new MemoryFragment(id, "Amulet " + name, description);
    }

    public MemoryFragment fragment() { return fragment; }

    public static Amulet fromId(String id) {
        for (Amulet amulet : values()) {
            if (amulet.fragment.getId().equals(id)) return amulet;
        }
        throw new IllegalArgumentException("Amulet tidak dikenal: " + id);
    }
}
