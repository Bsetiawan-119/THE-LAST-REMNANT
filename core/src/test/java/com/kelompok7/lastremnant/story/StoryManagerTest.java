// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Unit test StoryManager & CharacterDatabase
//  PJ : Carlos Ricardo Dwi Saputra  ·  Minggu : W2
// =============================================================================
package com.kelompok7.lastremnant.story;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.utils.Array;
import org.junit.Before;
import org.junit.Test;

public class StoryManagerTest {

    private StoryManager story;
    private Array<String> events;

    @Before
    public void setUp() {
        story = new StoryManager();
        events = new Array<>();
        story.addListener(new StoryListener() {
            @Override
            public void onFlagChanged(String flag, boolean value) {
                events.add((value ? "+" : "-") + flag);
            }

            @Override
            public void onObjectiveChanged(String objective) {
                events.add("obj:" + objective);
            }
        });
    }

    @Test
    public void flagDisetDanPengamatDiberiTahuSekali() {
        assertTrue(story.setFlag("met_sari"));
        assertFalse(story.setFlag("met_sari"));
        assertTrue(story.hasFlag("met_sari"));
        assertEquals(1, events.size);
        assertEquals("+met_sari", events.first());
    }

    @Test
    public void flagBisaDihapus() {
        story.setFlag("x");
        assertTrue(story.clearFlag("x"));
        assertFalse(story.hasFlag("x"));
        assertFalse(story.clearFlag("x"));
        assertEquals("-x", events.peek());
    }

    @Test
    public void objectiveSamaTidakMemicuUlang() {
        story.setObjective("Antar paket");
        story.setObjective("Antar paket");
        story.setObjective("Cari Brown Teddy");
        assertEquals(2, events.size);
        assertEquals("Cari Brown Teddy", story.getObjective());
    }

    @Test
    public void flagKosongDiabaikan() {
        assertFalse(story.setFlag(""));
        assertFalse(story.setFlag(null));
        assertEquals(0, story.getFlagCount());
    }

    @Test
    public void aturanDialogNpcSesuaiProgres() {
        CharacterDatabase db = CharacterDatabase.fromJson("{ characters: [ { id: sari, name: \"Bu Sari\", sprite: sari,"
                + " dialogues: [ { if: has_remnant, id: after }, { if: met_sari, id: repeat }, { id: intro } ] } ] }");
        CharacterInfo sari = db.get("sari");
        assertEquals("intro", sari.pickDialogue(story));
        story.setFlag("met_sari");
        assertEquals("repeat", sari.pickDialogue(story));
        story.setFlag("has_remnant");
        assertEquals("after", sari.pickDialogue(story));
    }

    @Test
    public void tokohTidakDikenalJadiNarator() {
        CharacterDatabase db = CharacterDatabase.fromJson("{ characters: [] }");
        CharacterInfo who = db.get("siapa");
        assertEquals("", who.getName());
        assertEquals(CharacterInfo.Style.NARRATION, who.getStyle());
        assertNull(who.pickDialogue(story));
    }
}
