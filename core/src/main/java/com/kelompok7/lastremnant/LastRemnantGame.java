// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : LastRemnantGame.java
//  Bagian  : UI & game flow (titik masuk game, pengatur pergantian layar)
//  PJ      : Budiman Setiawan  (kerangka dibuat bersama saat setup repo W1)
//  Minggu  : W1 (kerangka), W2 (transisi layar aman + layar penuh)
//  LibGDX  : Game
// =============================================================================
package com.kelompok7.lastremnant;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kelompok7.lastremnant.assets.GameAssets;
import com.kelompok7.lastremnant.screens.MainMenu;

/** Kelas utama. Memegang aset & SpriteBatch bersama, lalu membuka MainMenu. */
public class LastRemnantGame extends Game {

    private GameAssets assets;
    private SpriteBatch batch;

    @Override
    public void create() {
        assets = new GameAssets();
        assets.loadAll();
        batch = new SpriteBatch();
        setScreen(new MainMenu(this));
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Keys.F11)) toggleFullscreen();
        super.render();
    }

    /**
     * Ganti layar di awal frame berikutnya, lalu buang layar lama.
     * Aman dipanggil dari dalam Action Scene2D (tidak membuang Stage yang sedang berjalan).
     */
    public void changeScreen(Screen next) {
        Gdx.app.postRunnable(() -> {
            Screen old = getScreen();
            setScreen(next);
            if (old != null) old.dispose();
        });
    }

    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(1280, 720);
        } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
    }

    public GameAssets getAssets() {
        return assets;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    @Override
    public void dispose() {
        Screen s = getScreen();
        if (s != null) {
            s.hide();
            s.dispose();
        }
        batch.dispose();
        assets.dispose();
    }
}
