// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Unit test Journal & MemoryDatabase
//  PJ : Azzam Insan Kamil  ·  Minggu : W2
// =============================================================================
package com.kelompok7.lastremnant.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.utils.Array;
import org.junit.Test;

public class JournalTest {

    private static JournalEntry note(String id) {
        return new JournalEntry(id, JournalEntry.Category.NOTE, "Judul " + id, "Isi");
    }

    @Test
    public void entriDobelDitolak() {
        Journal j = new Journal();
        assertTrue(j.addEntry(note("a")));
        assertFalse(j.addEntry(note("a")));
        assertEquals(1, j.size());
    }

    @Test
    public void pengamatDiberiTahu() {
        Journal j = new Journal();
        Array<String> got = new Array<>();
        j.addListener(e -> got.add(e.getId()));
        j.addEntry(note("a"));
        j.addEntry(note("a"));
        j.addEntry(note("b"));
        assertEquals(2, got.size);
    }

    @Test
    public void hitungBelumDibaca() {
        Journal j = new Journal();
        j.addEntry(note("a"));
        j.addEntry(note("b"));
        assertEquals(2, j.getUnreadCount());
        j.markAllRead();
        assertEquals(0, j.getUnreadCount());
        j.addEntry(note("c"));
        assertEquals(1, j.getUnreadCount());
    }

    @Test
    public void getEntriesMengembalikanSalinan() {
        Journal j = new Journal();
        j.addEntry(note("a"));
        Array<JournalEntry> copy = j.getEntries();
        copy.clear();
        assertEquals(1, j.size());
    }

    @Test
    public void databaseMembuatCatatanBaruTiapPanggilan() {
        MemoryDatabase db = MemoryDatabase.fromJson(
                "{ echoes: [], notes: [ { id: delivery, title: Paket, text: Isi } ] }");
        JournalEntry a = db.createNote("delivery");
        JournalEntry b = db.createNote("delivery");
        assertNotSame(a, b);
        assertEquals("note_delivery", a.getId());
        assertEquals(JournalEntry.Category.NOTE, a.getCategory());
    }
}
