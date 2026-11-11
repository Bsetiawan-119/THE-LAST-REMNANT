// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Unit test GameStateManager (State pattern)
//  PJ : Budiman Setiawan  ·  Minggu : W2
// =============================================================================
package com.kelompok7.lastremnant.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.utils.Array;
import org.junit.Before;
import org.junit.Test;

public class GameStateManagerTest {

    private Array<String> log;
    private GameStateManager gsm;

    /** State palsu yang mencatat setiap pemanggilan. */
    private class FakeState implements GameState {
        final String name;
        final boolean consumesKeys;
        float updated;

        FakeState(String name, boolean consumesKeys) {
            this.name = name;
            this.consumesKeys = consumesKeys;
        }

        @Override public void enter() { log.add(name + ".enter"); }
        @Override public void exit() { log.add(name + ".exit"); }
        @Override public void pause() { log.add(name + ".pause"); }
        @Override public void resume() { log.add(name + ".resume"); }
        @Override public void update(float delta) { updated += delta; }
        @Override public boolean keyDown(int keycode) { return consumesKeys; }
    }

    @Before
    public void setUp() {
        log = new Array<>();
        gsm = new GameStateManager();
    }

    @Test
    public void urutanPushPopBenar() {
        FakeState explore = new FakeState("explore", false);
        FakeState dialog = new FakeState("dialog", true);
        gsm.set(explore);
        gsm.push(dialog);
        gsm.pop();
        assertEquals("[explore.enter, explore.pause, dialog.enter, dialog.exit, explore.resume]", log.toString());
        assertSame(explore, gsm.current());
    }

    @Test
    public void hanyaStatePalingAtasYangDiUpdate() {
        FakeState explore = new FakeState("explore", false);
        FakeState journal = new FakeState("journal", true);
        gsm.set(explore);
        gsm.push(journal);
        gsm.update(0.5f);
        assertEquals(0f, explore.updated, 0f);
        assertEquals(0.5f, journal.updated, 0f);
    }

    @Test
    public void tombolDiarahkanKeStateAktif() {
        gsm.set(new FakeState("explore", false));
        assertFalse(gsm.keyDown(1));
        gsm.push(new FakeState("dialog", true));
        assertTrue(gsm.keyDown(1));
        assertTrue(gsm.isCurrent(FakeState.class));
    }

    @Test
    public void setMembuangSemuaState() {
        gsm.set(new FakeState("a", false));
        gsm.push(new FakeState("b", false));
        log.clear();
        gsm.set(new FakeState("c", false));
        assertEquals("[b.exit, a.exit, c.enter]", log.toString());
        assertEquals(1, gsm.depth());
    }

    @Test(expected = IllegalStateException.class)
    public void popSaatKosongError() {
        gsm.pop();
    }

    @Test
    public void managerKosongAman() {
        assertNull(gsm.current());
        gsm.update(1f);
        assertFalse(gsm.keyDown(0));
    }

    @Test
    public void stateBolehMemopDirinyaSaatEnter() {
        // kasus nyata: dialog kosong langsung selesai ketika dibuka
        FakeState explore = new FakeState("explore", false);
        gsm.set(explore);
        gsm.push(new GameState() {
            @Override public void enter() { gsm.pop(); }
            @Override public void exit() { log.add("instant.exit"); }
            @Override public void update(float delta) { }
        });
        assertSame(explore, gsm.current());
        assertEquals(1, gsm.depth());
    }
}
