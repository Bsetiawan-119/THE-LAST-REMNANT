// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : JournalEntry.java
//  Bagian  : Puzzle, memory, quest (satu catatan di jurnal)
//  PJ      : Azzam Insan Kamil
//  Minggu  : W2
// =============================================================================
package com.kelompok7.lastremnant.memory;

/** Satu halaman jurnal Iwan. */
public final class JournalEntry {

    public enum Category {
        NOTE("CATATAN"),
        ECHO("GEMA"),
        FRAGMENT("FRAGMEN");

        public final String label;

        Category(String label) {
            this.label = label;
        }
    }

    private final String id;
    private final Category category;
    private final String title;
    private final String body;
    private boolean read;

    public JournalEntry(String id, Category category, String title, String body) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.body = body;
    }

    public String getId() { return id; }
    public Category getCategory() { return category; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public boolean isRead() { return read; }

    void markRead() {
        read = true;
    }
}
