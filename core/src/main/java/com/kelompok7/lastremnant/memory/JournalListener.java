// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : JournalListener.java
//  Bagian  : Puzzle, memory, quest
//  PJ      : Azzam Insan Kamil
//  Minggu  : W2
//  Pattern : OBSERVER (interface pengamat). Hud & JournalUI (Setiawan) mendengarkan.
// =============================================================================
package com.kelompok7.lastremnant.memory;

@FunctionalInterface
public interface JournalListener {
    void onEntryAdded(JournalEntry entry);
}
