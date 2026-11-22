package com.kelompok7.lastremnant.state;

import com.kelompok7.lastremnant.screens.GameScreen;
import com.kelompok7.lastremnant.world.StageArea;

/** Kilatan putih lalu gelap sebelum mengganti peta prolog. */
public final class WakeState implements GameState {
    private static final float DURATION = 1.15f;
    private final GameScreen screen;
    private float time;

    public WakeState(GameScreen screen) { this.screen = screen; }

    @Override public void enter() {
        screen.getInputHandler().setEnabled(false);
        screen.getPlayer().stop();
        screen.setFocus(null);
    }

    @Override public void exit() { }

    @Override public void update(float delta) {
        time += delta;
        screen.setWakeFlash(time);
        if (time >= DURATION) screen.queueArea(StageArea.COCOON_CITY);
    }

    @Override public boolean keyDown(int keycode) { return true; }
}
