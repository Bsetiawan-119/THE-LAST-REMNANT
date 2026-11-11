// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Journal.java
//  Bagian  : Puzzle, memory, quest (jurnal Iwan: catatan, gema, fragmen)
//  PJ      : Azzam Insan Kamil
//  Minggu  : W2
//  OOP     : ENCAPSULATION (daftar entri hanya bisa dibaca, ditambah lewat addEntry)
//  Pattern : OBSERVER (subject)
//  Test    : core/src/test/.../memory/JournalTest.java
// =============================================================================
package com.kelompok7.lastremnant.memory;

import com.badlogic.gdx.utils.Array;

/** Kumpulan catatan Iwan, urut sesuai waktu ditemukan. */
public class Journal {

    private final Array<JournalEntry> entries = new Array<>();
    private final Array<JournalListener> listeners = new Array<>();

    /** @return false bila entri dengan id yang sama sudah ada (tidak ditambah dua kali). */
    public boolean addEntry(JournalEntry entry) {
        if (entry == null || contains(entry.getId())) return false;
        entries.add(entry);
        for (JournalListener l : listeners) l.onEntryAdded(entry);
        return true;
    }

    public boolean contains(String id) {
        for (JournalEntry e : entries) {
            if (e.getId().equals(id)) return true;
        }
        return false;
    }

    /** Salinan baca-saja; mengubahnya tidak memengaruhi jurnal. */
    public Array<JournalEntry> getEntries() {
        return new Array<>(entries);
    }

    public JournalEntry get(int index) {
        return entries.get(index);
    }

    public int size() {
        return entries.size;
    }

    public int getUnreadCount() {
        int n = 0;
        for (JournalEntry e : entries) {
            if (!e.isRead()) n++;
        }
        return n;
    }

    public void markAllRead() {
        for (JournalEntry e : entries) e.markRead();
    }

    public void addListener(JournalListener l) {
        if (!listeners.contains(l, true)) listeners.add(l);
    }

    public void removeListener(JournalListener l) {
        listeners.removeValue(l, true);
    }
}
