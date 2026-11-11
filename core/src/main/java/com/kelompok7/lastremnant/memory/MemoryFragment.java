// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : MemoryFragment.java
//  Bagian  : Puzzle, memory, quest (satu dari tiga fragmen memori utama)
//  PJ      : Azzam Insan Kamil
//  Minggu  : W2 = struktur data & slot di MemoryDevice
//            W3 = diberikan oleh Puzzle (2 buah) dan quest NPC (1 buah)
// =============================================================================
package com.kelompok7.lastremnant.memory;

/** Fragmen memori tentang asal-usul Brown Teddy. */
public final class MemoryFragment {

    private final String id;
    private final String title;
    private final String text;

    public MemoryFragment(String id, String title, String text) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id fragmen kosong");
        this.id = id;
        this.title = title;
        this.text = text;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getText() { return text; }
}
