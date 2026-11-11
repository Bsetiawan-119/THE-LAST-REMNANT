// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : CharacterDatabase.java
//  Bagian  : Dialogue & story (pemuat data tokoh dari JSON)
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W2
// =============================================================================
package com.kelompok7.lastremnant.story;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/** Membaca {@code assets/data/characters.json}. */
public class CharacterDatabase {

    private static final CharacterInfo NARRATOR =
            new CharacterInfo("narrator", "", null, null, 1f, CharacterInfo.Style.NARRATION, null);

    private final ObjectMap<String, CharacterInfo> characters = new ObjectMap<>();

    public static CharacterDatabase fromJson(String json) {
        CharacterDatabase db = new CharacterDatabase();
        JsonValue root = new JsonReader().parse(json);
        for (JsonValue c : root.get("characters")) {
            Array<CharacterInfo.DialogueRule> rules = new Array<>();
            JsonValue rs = c.get("dialogues");
            if (rs != null) {
                for (JsonValue r : rs) {
                    rules.add(new CharacterInfo.DialogueRule(r.getString("if", null), r.getString("id")));
                }
            }
            CharacterInfo.Style style = CharacterInfo.Style.valueOf(c.getString("style", "normal").toUpperCase());
            db.characters.put(c.getString("id"), new CharacterInfo(
                    c.getString("id"),
                    c.getString("name", ""),
                    c.getString("sprite", null),
                    c.getString("portrait", null),
                    c.getFloat("voice", 1f),
                    style,
                    rules));
        }
        return db;
    }

    /** Selalu mengembalikan objek (narator) supaya UI tidak perlu cek null. */
    public CharacterInfo get(String id) {
        CharacterInfo c = characters.get(id);
        return c != null ? c : NARRATOR;
    }

    public boolean has(String id) {
        return characters.containsKey(id);
    }
}
