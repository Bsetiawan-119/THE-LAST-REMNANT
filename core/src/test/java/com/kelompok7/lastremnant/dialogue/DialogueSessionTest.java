// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Unit test DialogueSession & Conditions
//  PJ : Carlos Ricardo Dwi Saputra  ·  Minggu : W2
// =============================================================================
package com.kelompok7.lastremnant.dialogue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import org.junit.Before;
import org.junit.Test;

public class DialogueSessionTest {

    private ObjectSet<String> flags;
    private Array<String> fired;

    private static final String JSON = "{ dialogues: [ { id: test, start: a, nodes: ["
            + "{ id: a, speaker: sari, text: \"Halo\", next: hub, actions: [\"flag:met\"] },"
            + "{ id: hub, speaker: sari, text: \"Tanya apa?\", choices: ["
            + "   { text: \"Soal Teddy\", next: t, if: \"!asked\", actions: [\"flag:asked\"] },"
            + "   { text: \"Rahasia\", next: s, if: \"vip\" },"
            + "   { text: \"Dah\", next: null } ] },"
            + "{ id: t, speaker: sari, text: \"Brown Teddy...\", next: hub },"
            + "{ id: s, speaker: sari, text: \"Psst\" } ] } ] }";

    private DialogueSession session;

    @Before
    public void setUp() {
        flags = new ObjectSet<>();
        fired = new Array<>();
        Dialogue d = DialogueRepository.fromJson(JSON).get("test");
        session = new DialogueSession(d, flags::contains, a -> {
            fired.add(a.toString());
            if (a.getType().equals("flag")) flags.add(a.getValue());
        });
        session.start();
    }

    @Test
    public void mulaiDariNodeAwalDanMenjalankanAksi() {
        assertEquals("a", session.getCurrentNode().getId());
        assertTrue(fired.contains("flag:met", false));
        assertFalse(session.isWaitingForChoice());
    }

    @Test
    public void pilihanDisaringSyarat() {
        session.advance();
        assertTrue(session.isWaitingForChoice());
        assertEquals(2, session.getVisibleChoices().size);   // 'Rahasia' butuh flag vip
        assertEquals("Soal Teddy", session.getVisibleChoices().get(0).getText());
    }

    @Test
    public void advanceDiabaikanSaatHarusMemilih() {
        session.advance();
        assertFalse(session.advance());
        assertEquals("hub", session.getCurrentNode().getId());
    }

    @Test
    public void pilihanYangSudahDitanyaHilangSaatKembaliKeHub() {
        session.advance();
        assertTrue(session.choose(0));                   // Soal Teddy
        assertEquals("t", session.getCurrentNode().getId());
        session.advance();                                // kembali ke hub
        assertEquals("hub", session.getCurrentNode().getId());
        assertEquals(1, session.getVisibleChoices().size);
        assertEquals("Dah", session.getVisibleChoices().get(0).getText());
    }

    @Test
    public void pilihanTanpaNextMengakhiriDialog() {
        session.advance();
        session.choose(1);                                // Dah
        assertTrue(session.isFinished());
        assertNull(session.getCurrentNode());
    }

    @Test
    public void indexPilihanTidakValidDitolak() {
        session.advance();
        assertFalse(session.choose(-1));
        assertFalse(session.choose(9));
        assertEquals("hub", session.getCurrentNode().getId());
    }

    @Test
    public void dialogLinearSelesaiSetelahBarisTerakhir() {
        DialogueSession s = new DialogueSession(Dialogue.linear("x", "iwan", "satu", "dua"), null, null);
        s.start();
        assertEquals("satu", s.getCurrentNode().getText());
        s.advance();
        assertEquals("dua", s.getCurrentNode().getText());
        s.advance();
        assertTrue(s.isFinished());
        assertFalse(s.advance());
    }

    @Test
    public void syaratGabungan() {
        flags.add("a");
        assertTrue(Conditions.test(null, flags::contains));
        assertTrue(Conditions.test("a", flags::contains));
        assertFalse(Conditions.test("a,b", flags::contains));
        assertTrue(Conditions.test("a, !b", flags::contains));
        assertFalse(Conditions.test("!a", flags::contains));
    }

    @Test
    public void parseAksi() {
        DialogueAction a = DialogueAction.parse("objective:Antar paket: cepat");
        assertEquals("objective", a.getType());
        assertEquals("Antar paket: cepat", a.getValue());   // titik dua kedua tetap bagian nilai
        assertEquals("", DialogueAction.parse("end").getValue());
    }
}
