// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : DialogueState.java
//  Bagian  : UI & game flow (state: percakapan sedang berlangsung)
//  PJ      : Budiman Setiawan
//  Minggu  : W2
//  Pattern : STATE (concrete state)
// =============================================================================
package com.kelompok7.lastremnant.state;

import com.badlogic.gdx.Input.Keys;
import com.kelompok7.lastremnant.dialogue.DialogueSession;
import com.kelompok7.lastremnant.screens.GameScreen;
import com.kelompok7.lastremnant.ui.DialogueUI;

/**
 * Selama dialog: Iwan tidak bisa berjalan, dunia tetap hidup (NPC & partikel jalan),
 * semua tombol diarahkan ke DialogueUI. Saat sesi selesai, state ini mem-pop dirinya.
 */
public class DialogueState implements GameState {

    private final GameScreen screen;
    private final DialogueSession session;
    private final Runnable onFinished;
    private boolean closedByUi;

    public DialogueState(GameScreen screen, DialogueSession session, Runnable onFinished) {
        this.screen = screen;
        this.session = session;
        this.onFinished = onFinished;
    }

    @Override
    public void enter() {
        screen.getInputHandler().setEnabled(false);
        screen.getPlayer().stop();
        screen.setFocus(null);
        screen.getDialogueUI().show(session, () -> {
            closedByUi = true;
            // dipanggil oleh DialogueUI saat sesi selesai -> kembali ke state sebelumnya
            if (screen.getStateManager().current() == this) screen.getStateManager().pop();
        });
    }

    @Override
    public void exit() {
        if (!closedByUi) screen.getDialogueUI().forceHide();
        if (onFinished != null) onFinished.run();
    }

    @Override
    public void update(float delta) {
        screen.updateWorld(delta);
        screen.getDialogueUI().update(delta);
    }

    @Override
    public boolean keyDown(int keycode) {
        DialogueUI ui = screen.getDialogueUI();
        switch (keycode) {
            case Keys.W:
            case Keys.UP:
                ui.moveSelection(-1);
                break;
            case Keys.S:
            case Keys.DOWN:
                ui.moveSelection(1);
                break;
            case Keys.E:
            case Keys.SPACE:
            case Keys.ENTER:
            case Keys.NUMPAD_ENTER:
                ui.confirm();
                break;
            case Keys.NUM_1: case Keys.NUM_2: case Keys.NUM_3: case Keys.NUM_4:
                ui.chooseIndex(keycode - Keys.NUM_1);
                break;
            default:
                break;
        }
        return true;   // dialog "menelan" semua tombol
    }
}
