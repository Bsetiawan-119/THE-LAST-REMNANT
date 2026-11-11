// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : MemoryDatabase.java
//  Bagian  : Puzzle, memory, quest (pemuat data gema & catatan dari JSON)
//  PJ      : Azzam Insan Kamil
//  Minggu  : W2
// =============================================================================
package com.kelompok7.lastremnant.memory;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/** Membaca {@code assets/data/memories.json}: daftar gema benda & catatan jurnal. */
public class MemoryDatabase {

    private final ObjectMap<String, MemoryEcho> echoes = new ObjectMap<>();
    private final ObjectMap<String, JournalEntry> notes = new ObjectMap<>();

    public static MemoryDatabase fromJson(String json) {
        MemoryDatabase db = new MemoryDatabase();
        JsonValue root = new JsonReader().parse(json);
        JsonValue es = root.get("echoes");
        if (es != null) {
            for (JsonValue e : es) {
                db.echoes.put(e.getString("id"), new MemoryEcho(
                        e.getString("id"),
                        e.getString("name"),
                        e.getString("sprite"),
                        e.getString("inspect"),
                        e.getString("echo"),
                        e.getString("journalTitle"),
                        e.getString("journalText")));
            }
        }
        JsonValue ns = root.get("notes");
        if (ns != null) {
            for (JsonValue n : ns) {
                String id = n.getString("id");
                db.notes.put(id, new JournalEntry("note_" + id, JournalEntry.Category.NOTE,
                        n.getString("title"), n.getString("text")));
            }
        }
        return db;
    }

    public MemoryEcho getEcho(String id) {
        MemoryEcho e = echoes.get(id);
        if (e == null) throw new IllegalArgumentException("Gema memori '" + id + "' tidak ada di memories.json");
        return e;
    }

    /** Entri catatan baru (objek baru tiap panggilan, karena status "dibaca" milik jurnal). */
    public JournalEntry createNote(String id) {
        JournalEntry n = notes.get(id);
        if (n == null) throw new IllegalArgumentException("Catatan '" + id + "' tidak ada di memories.json");
        return new JournalEntry(n.getId(), n.getCategory(), n.getTitle(), n.getBody());
    }

    public int echoCount() {
        return echoes.size;
    }
}
