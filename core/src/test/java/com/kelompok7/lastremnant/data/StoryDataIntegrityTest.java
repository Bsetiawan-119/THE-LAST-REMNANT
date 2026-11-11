// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Test integrasi data naskah (W2)
//  PJ : Carlos (naskah) + Insan (memori)  ·  dijaga bersama saat integrasi
//
//  Memastikan file JSON di assets/data saling cocok:
//   - semua dialog valid (next/choice menunjuk node yang ada)
//   - semua aturan dialog NPC menunjuk dialog yang ada
//   - semua gema memori menunjuk dialog inspect/echo yang ada
//   - semua pembicara di naskah terdaftar di characters.json
//   - semua aksi memakai tipe yang dikenal GameScreen
//  Jika ada yang salah ketik, test ini gagal SEBELUM game dijalankan.
// =============================================================================
package com.kelompok7.lastremnant.data;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.kelompok7.lastremnant.dialogue.DialogueRepository;
import com.kelompok7.lastremnant.memory.MemoryDatabase;
import com.kelompok7.lastremnant.story.CharacterDatabase;
import com.kelompok7.lastremnant.story.CharacterInfo;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;

public class StoryDataIntegrityTest {

    private static final Set<String> KNOWN_ACTIONS =
            new HashSet<>(Arrays.asList("flag", "unflag", "objective", "device", "note", "sfx", "toast"));
    private static final Set<String> KNOWN_SFX =
            new HashSet<>(Arrays.asList("blip", "select", "confirm", "scan", "journal"));

    private static String dialoguesJson;
    private static String charactersJson;
    private static String memoriesJson;

    @BeforeClass
    public static void load() throws IOException {
        Path dir = findDataDir();
        dialoguesJson = read(dir.resolve("dialogues.json"));
        charactersJson = read(dir.resolve("characters.json"));
        memoriesJson = read(dir.resolve("memories.json"));
    }

    /** Gradle menjalankan test dari folder core/, IDE kadang dari root. */
    private static Path findDataDir() {
        for (String candidate : new String[]{"../assets/data", "assets/data"}) {
            Path p = Paths.get(candidate);
            if (Files.isDirectory(p)) return p;
        }
        throw new IllegalStateException("Folder assets/data tidak ditemukan");
    }

    private static String read(Path p) throws IOException {
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    @Test
    public void semuaDialogValid() {
        DialogueRepository repo = DialogueRepository.fromJson(dialoguesJson);   // validate() di dalamnya
        assertTrue(repo.has("intro"));
    }

    @Test
    public void aturanNpcMenunjukDialogYangAda() {
        DialogueRepository repo = DialogueRepository.fromJson(dialoguesJson);
        JsonValue root = new JsonReader().parse(charactersJson);
        CharacterDatabase db = CharacterDatabase.fromJson(charactersJson);
        for (JsonValue c : root.get("characters")) {
            CharacterInfo info = db.get(c.getString("id"));
            for (CharacterInfo.DialogueRule r : info.getRules()) {
                assertTrue(info.getId() + " -> dialog '" + r.dialogueId + "' tidak ada", repo.has(r.dialogueId));
            }
        }
    }

    @Test
    public void gemaMemoriMenunjukDialogYangAda() {
        DialogueRepository repo = DialogueRepository.fromJson(dialoguesJson);
        MemoryDatabase mem = MemoryDatabase.fromJson(memoriesJson);
        for (JsonValue e : new JsonReader().parse(memoriesJson).get("echoes")) {
            String id = e.getString("id");
            assertTrue("inspect " + id, repo.has(mem.getEcho(id).getInspectDialogueId()));
            assertTrue("echo " + id, repo.has(mem.getEcho(id).getEchoDialogueId()));
        }
    }

    @Test
    public void pembicaraDanAksiDikenal() {
        CharacterDatabase chars = CharacterDatabase.fromJson(charactersJson);
        MemoryDatabase mem = MemoryDatabase.fromJson(memoriesJson);
        Array<String> problems = new Array<>();
        for (JsonValue d : new JsonReader().parse(dialoguesJson).get("dialogues")) {
            String did = d.getString("id");
            for (JsonValue n : d.get("nodes")) {
                String where = did + "/" + n.getString("id");
                String speaker = n.getString("speaker", "narrator");
                if (!chars.has(speaker)) problems.add(where + ": pembicara '" + speaker + "' tidak terdaftar");
                if (n.getString("text", "").length() > 180) problems.add(where + ": teks > 180 karakter");
                checkActions(n.get("actions"), where, mem, problems);
                JsonValue cs = n.get("choices");
                if (cs != null) {
                    for (JsonValue c : cs) checkActions(c.get("actions"), where + "/pilihan", mem, problems);
                }
            }
        }
        if (problems.size > 0) fail("Masalah naskah:\n" + problems.toString("\n"));
    }

    private static void checkActions(JsonValue actions, String where, MemoryDatabase mem, Array<String> problems) {
        if (actions == null) return;
        for (JsonValue a : actions) {
            String raw = a.asString();
            int sep = raw.indexOf(':');
            String type = sep < 0 ? raw : raw.substring(0, sep);
            String value = sep < 0 ? "" : raw.substring(sep + 1);
            if (!KNOWN_ACTIONS.contains(type)) problems.add(where + ": aksi tidak dikenal '" + raw + "'");
            if (type.equals("sfx") && !KNOWN_SFX.contains(value)) problems.add(where + ": sfx '" + value + "' tidak ada");
            if (type.equals("note")) {
                try {
                    mem.createNote(value);
                } catch (IllegalArgumentException e) {
                    problems.add(where + ": " + e.getMessage());
                }
            }
        }
    }
}
