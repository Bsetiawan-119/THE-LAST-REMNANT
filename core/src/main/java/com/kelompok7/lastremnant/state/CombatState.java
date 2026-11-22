package com.kelompok7.lastremnant.state;

import com.badlogic.gdx.Input.Keys;
import com.kelompok7.lastremnant.entity.Direction;
import com.kelompok7.lastremnant.entity.Interactable;
import com.kelompok7.lastremnant.screens.GameScreen;

/** Mode Stage 1, memakai Command dan overlay yang sama seperti prolog. */
public final class CombatState extends ExplorationState {
    private final GameScreen screen;

    public CombatState(GameScreen screen) {
        super(screen);
        this.screen = screen;
    }

    @Override public void enter() {
        super.enter();
        var input = screen.getInputHandler();
        input.unbind(Keys.SPACE);
        input.bindPress(Keys.F, player -> {
            Direction dir = player.getFacing();
            screen.stageOne().attack(dir.dx, dir.dy);
        });
        input.bindPress(Keys.R, player -> screen.stageOne().reload());
        input.bindPress(Keys.Q, player -> screen.stageOne().switchWeapon());
        input.bindPress(Keys.G, player -> screen.stageOne().drop());
    }

    @Override public void exit() {
        super.exit();
        var input = screen.getInputHandler();
        for (int key : new int[]{Keys.F, Keys.R, Keys.Q, Keys.G}) input.unbind(key);
    }

    @Override public void update(float delta) {
        if (screen.stageOne().beforeWorld(delta)) super.update(delta);
        else screen.setFocus(null);
        if (screen.getStateManager().current() == this) screen.stageOne().afterWorld();
    }

    @Override protected void interact(Interactable target) {
        if (target != null) super.interact(target);
        else screen.stageOne().backstab();
    }

    @Override public boolean keyDown(int keycode) {
        if (screen.stageOne().hiding() && keycode != Keys.ESCAPE) return true;
        return super.keyDown(keycode);
    }
}
