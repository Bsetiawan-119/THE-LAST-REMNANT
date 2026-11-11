// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : DialogueRepository.java
//  Bagian  : Dialogue & story (pemuat naskah dari JSON)
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W2  (naskah ditulis di W1, sekarang dipindah ke assets/data/dialogues.json)
//  Test    : core/src/test/.../dialogue/DialogueRepositoryTest.java
// =============================================================================
package com.kelompok7.lastremnant.dialogue;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Semua dialog game, dibaca dari JSON. Penulis naskah cukup mengedit
 * {@code assets/data/dialogues.json}, tidak perlu menyentuh kode Java.
 * <pre>
 * { "dialogues": [
 *   { "id": "sari_intro", "start": "a", "nodes": [
 *     { "id": "a", "speaker": "sari", "text": "...", "next": "b", "actions": ["flag:met_sari"] },
 *     { "id": "b", "speaker": "iwan", "text": "...", "choices": [
 *         { "text": "...", "next": "c", "if": "!asked", "actions": ["flag:asked"] } ] }
 *   ]}
 * ]}
 * </pre>
 */
public class DialogueRepository {

    private final ObjectMap<String, Dialogue> dialogues = new ObjectMap<>();

    public static DialogueRepository fromJson(String json) {
        DialogueRepository repo = new DialogueRepository();
        JsonValue root = new JsonReader().parse(json);
        JsonValue list = root.get("dialogues");
        if (list == null) throw new IllegalArgumentException("JSON dialog harus punya array 'dialogues'");
        for (JsonValue d : list) {
            repo.add(parseDialogue(d));
        }
        return repo;
    }

    private static Dialogue parseDialogue(JsonValue d) {
        String id = d.getString("id");
        Array<DialogueNode> nodes = new Array<>();
        for (JsonValue n : d.get("nodes")) {
            Array<DialogueChoice> choices = new Array<>();
            JsonValue cs = n.get("choices");
            if (cs != null) {
                for (JsonValue c : cs) {
                    choices.add(new DialogueChoice(
                            c.getString("text"),
                            c.getString("next", null),
                            c.getString("if", null),
                            parseActions(c.get("actions"))));
                }
            }
            nodes.add(new DialogueNode(
                    n.getString("id"),
                    n.getString("speaker", "narrator"),
                    n.getString("text", ""),
                    n.getString("next", null),
                    choices,
                    parseActions(n.get("actions"))));
        }
        String start = d.getString("start", nodes.isEmpty() ? null : nodes.first().getId());
        Dialogue dialogue = new Dialogue(id, start, nodes);
        dialogue.validate();
        return dialogue;
    }

    private static Array<DialogueAction> parseActions(JsonValue arr) {
        Array<DialogueAction> result = new Array<>();
        if (arr == null) return result;
        for (JsonValue a : arr) result.add(DialogueAction.parse(a.asString()));
        return result;
    }

    public void add(Dialogue dialogue) {
        if (dialogues.containsKey(dialogue.getId())) {
            throw new IllegalArgumentException("id dialog dobel: " + dialogue.getId());
        }
        dialogues.put(dialogue.getId(), dialogue);
    }

    public Dialogue get(String id) {
        Dialogue d = dialogues.get(id);
        if (d == null) throw new IllegalArgumentException("Dialog '" + id + "' tidak ada di naskah");
        return d;
    }

    public boolean has(String id) {
        return dialogues.containsKey(id);
    }

    public int size() {
        return dialogues.size;
    }
}
