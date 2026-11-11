// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : JournalState.java
//  Bagian  : UI & game flow (state: jurnal terbuka, dunia berhenti)
//  PJ      : Budiman Setiawan
//  Minggu  : W2
//  Pattern : STATE (concrete state)
// =============================================================================
package com.kelompok7.lastremnant.state;

import com.badlogic.gdx.Input.Keys;
import com.kelompok7.lastremnant.screens.GameScreen;

/** Overlay jurnal. Dunia dibekukan (update() kosong) sampai jurnal ditutup. */
public class JournalState implements GameState {

    private final GameScreen screen;

    public JournalState(GameScreen screen) {
        this.screen = screen;
    }

    @Override
    public void enter() {
        screen.getInputHandler().setEnabled(false);
        screen.getPlayer().stop();
        screen.setFocus(null);
        screen.getHud().setExplorationVisible(false);
        screen.getJournalUI().show();
        screen.getJournal().markAllRead();    // setelah show(), supaya penanda "baru" sempat tampil
        screen.getAssets().play("journal", 0.6f, 1f);
    }

    @Override
    public void exit() {
        screen.getJournalUI().hide();
        screen.getHud().setExplorationVisible(true);
        screen.getAssets().play("journal", 0.5f, 0.8f);
    }

    @Override
    public void update(float delta) {
        // dunia sengaja tidak di-update: waktu berhenti saat membaca jurnal
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Keys.W:
            case Keys.UP:
                screen.getJournalUI().moveSelection(-1);
                break;
            case Keys.S:
            case Keys.DOWN:
                screen.getJournalUI().moveSelection(1);
                break;
            case Keys.J:
            case Keys.TAB:
            case Keys.ESCAPE:
            case Keys.BACKSPACE:
                screen.getStateManager().pop();
                break;
            default:
                break;
        }
        return true;
    }
}
