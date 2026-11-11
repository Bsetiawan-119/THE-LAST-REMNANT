// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : ExplorationState.java
//  Bagian  : UI & game flow (state: Iwan berjalan bebas di kota)
//  PJ      : Budiman Setiawan
//  Minggu  : W2
//  Pattern : STATE (concrete state)
// =============================================================================
package com.kelompok7.lastremnant.state;

import com.badlogic.gdx.Input.Keys;
import com.kelompok7.lastremnant.entity.Interactable;
import com.kelompok7.lastremnant.screens.GameScreen;

/**
 * Loop inti eksplorasi: input -> gerak -> cari benda/NPC di depan Iwan -> interaksi.
 * Interaksi bersifat polimorfik: state ini tidak tahu apakah targetnya NPC atau benda.
 */
public class ExplorationState implements GameState {

    private final GameScreen screen;

    public ExplorationState(GameScreen screen) {
        this.screen = screen;
    }

    @Override
    public void enter() {
        screen.getInputHandler().setEnabled(true);
    }

    @Override
    public void resume() {
        screen.getInputHandler().setEnabled(true);
    }

    @Override
    public void pause() {
        freeze();
    }

    @Override
    public void exit() {
        freeze();
    }

    private void freeze() {
        screen.getInputHandler().setEnabled(false);
        screen.getPlayer().stop();
        screen.setFocus(null);
    }

    @Override
    public void update(float delta) {
        screen.getInputHandler().handleInput(screen.getPlayer());
        screen.updateWorld(delta);

        Interactable target = screen.findInteractTarget();
        screen.setFocus(target);

        if (screen.getPlayer().consumeInteractRequest() && target != null) {
            target.interact(screen);   // bisa mendorong DialogueState ke atas tumpukan
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Keys.J:
            case Keys.TAB:
                screen.openJournal();
                return true;
            case Keys.ESCAPE:
                // sementara: langsung ke menu. W4 diganti PauseState (menu jeda)
                screen.returnToMenu();
                return true;
            default:
                return false;
        }
    }
}
