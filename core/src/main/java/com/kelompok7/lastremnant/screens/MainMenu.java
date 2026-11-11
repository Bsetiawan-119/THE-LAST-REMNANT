// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : MainMenu.java
//  Bagian  : UI & game flow (menu utama)
//  PJ      : Budiman Setiawan
//  Minggu  : W2
//  LibGDX  : Screen, Scene2D, SpriteBatch (latar parallax)
// =============================================================================
package com.kelompok7.lastremnant.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kelompok7.lastremnant.LastRemnantGame;
import com.kelompok7.lastremnant.assets.GameAssets;
import com.kelompok7.lastremnant.ui.UiStyles;
import com.kelompok7.lastremnant.util.Constants;

/**
 * Menu utama: kota senja dengan parallax, butiran "memori" melayang naik,
 * pilihan Mulai / Kontrol / Keluar. Bisa dengan keyboard maupun mouse.
 */
public class MainMenu extends ScreenAdapter {

    private static final String[] ITEMS = {"MULAI", "KONTROL", "KELUAR"};
    private static final int MOTES = 48;

    private final LastRemnantGame game;
    private final GameAssets assets;
    private final SpriteBatch batch;
    private final UiStyles st;

    private final OrthographicCamera bgCamera = new OrthographicCamera();
    private final FitViewport bgViewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, bgCamera);
    private final Stage stage;
    private final TextureRegion sky;
    private final Texture far;
    private final Texture near;
    private final TextureRegion pixel;

    private final Array<Label> itemLabels = new Array<>();
    private final Array<Image> itemCursors = new Array<>();
    private final Table controlsPanel = new Table();
    private final Image curtain;

    private final float[] moteX = new float[MOTES];
    private final float[] moteY = new float[MOTES];
    private final float[] moteSpeed = new float[MOTES];
    private final float[] motePhase = new float[MOTES];

    private int selected;
    private float time;
    private boolean leaving;

    public MainMenu(LastRemnantGame game) {
        this.game = game;
        this.assets = game.getAssets();
        this.batch = game.getBatch();
        this.st = new UiStyles(assets);
        this.stage = new Stage(new FitViewport(Constants.UI_WIDTH, Constants.UI_HEIGHT), batch);

        sky = assets.region("menu_sky");
        far = assets.texture("menu_far");
        near = assets.texture("menu_near");
        far.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        near.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        pixel = assets.region("pixel");
        for (int i = 0; i < MOTES; i++) resetMote(i, true);

        buildUi();
        curtain = st.fadeCurtain();
        stage.addActor(curtain);
    }

    private void buildUi() {
        Table root = new Table();
        root.setFillParent(true);
        root.pad(24, 40, 18, 40);

        // judul dengan bayangan tebal ala game pixel
        Label shadow = new Label("THE LAST REMNANT", new Label.LabelStyle(st.font, UiStyles.INK));
        shadow.setFontScale(3f);
        Label title = new Label("THE LAST REMNANT", st.warm);
        title.setFontScale(3f);
        Container<Label> shadowBox = new Container<>(shadow).padLeft(3).padTop(3);
        Stack titleStack = new Stack(shadowBox, new Container<>(title).padRight(3).padBottom(3));

        Table titleBlock = new Table();
        titleBlock.add(titleStack).left().row();
        titleBlock.add(new Label("Sebelum semuanya terlupakan.", st.body)).left().padTop(2).padLeft(2);

        Table menu = new Table();
        for (int i = 0; i < ITEMS.length; i++) {
            final int index = i;
            Image cursor = new Image(st.cursor);
            Label label = new Label(ITEMS[i], st.body);
            label.setFontScale(1.5f);
            label.setTouchable(Touchable.enabled);
            label.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer == -1 && index != selected && !controlsPanel.isVisible()) select(index, true);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (controlsPanel.isVisible()) return;
                    select(index, false);
                    activate();
                }
            });
            menu.add(cursor).size(12).padRight(8);
            menu.add(label).left().padBottom(6).row();
            itemCursors.add(cursor);
            itemLabels.add(label);
        }

        Table footer = new Table();
        footer.add(new Label(Constants.VERSION, st.dim)).left().expandX();
        footer.add(new Label(Constants.CREDIT, st.dim)).right();

        root.top().left();
        root.add(titleBlock).left().padTop(18).row();
        root.add(menu).left().padTop(34).padLeft(6).expandY().top().row();
        root.add(footer).growX().bottom();
        stage.addActor(root);

        buildControlsPanel();
        select(0, false);
    }

    private void buildControlsPanel() {
        String[][] rows = {
                {"W A S D / Panah", "Berjalan"},
                {"E / Spasi / Enter", "Bicara, periksa, lanjut dialog"},
                {"1 - 4", "Pilih jawaban dialog"},
                {"J / Tab", "Buka jurnal"},
                {"Esc", "Kembali ke menu"},
                {"F11", "Layar penuh"},
        };
        Table box = new Table();
        box.setBackground(st.panel);
        box.pad(14, 18, 12, 18);
        box.add(new Label("KONTROL", st.warm)).colspan(2).left().padBottom(10).row();
        for (String[] r : rows) {
            box.add(new Label(UiStyles.escape(r[0]), st.accent)).left().padRight(24).padBottom(4);
            box.add(new Label(UiStyles.escape(r[1]), st.body)).left().padBottom(4).row();
        }
        box.add(new Label(UiStyles.escape("[Esc / Enter] Tutup"), st.dim)).colspan(2).left().padTop(10);

        controlsPanel.setFillParent(true);
        controlsPanel.setBackground(st.solid(new Color(0.03f, 0.02f, 0.06f, 0.6f)));
        controlsPanel.setTouchable(Touchable.enabled);
        controlsPanel.add(box);
        controlsPanel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleControls(false);
            }
        });
        controlsPanel.setVisible(false);
        stage.addActor(controlsPanel);
    }

    // ------------------------------------------------------------------ Screen

    @Override
    public void show() {
        InputMultiplexer mux = new InputMultiplexer(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                return handleKey(keycode);
            }
        }, stage);
        Gdx.input.setInputProcessor(mux);
        curtain.addAction(Actions.sequence(Actions.alpha(1f), Actions.fadeOut(0.9f)));
    }

    @Override
    public void render(float delta) {
        time += delta;
        ScreenUtils.clear(0.06f, 0.05f, 0.09f, 1f);

        bgViewport.apply();
        bgCamera.position.set(Constants.WORLD_WIDTH / 2f, Constants.WORLD_HEIGHT / 2f, 0f);
        bgCamera.update();
        batch.setProjectionMatrix(bgCamera.combined);
        batch.setColor(1f, 1f, 1f, 1f);   // reset warna sisa Scene2D
        batch.begin();
        batch.draw(sky, 0, 0);
        drawScrolling(far, time * 3f);
        drawMotes(delta);
        drawScrolling(near, time * 9f);
        batch.end();

        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    /** Gambar lapisan yang bergulir horizontal tanpa batas (tekstur Repeat). */
    private void drawScrolling(Texture tex, float offset) {
        int src = MathUtils.floor(offset) % tex.getWidth();
        batch.draw(tex, 0, 0, (int) Constants.WORLD_WIDTH, (int) Constants.WORLD_HEIGHT,
                src, 0, (int) Constants.WORLD_WIDTH, (int) Constants.WORLD_HEIGHT, false, false);
    }

    private void drawMotes(float delta) {
        for (int i = 0; i < MOTES; i++) {
            moteY[i] += moteSpeed[i] * delta;
            if (moteY[i] > Constants.WORLD_HEIGHT + 4) resetMote(i, false);
            float sway = MathUtils.sin(time * 0.8f + motePhase[i]) * 6f;
            float life = moteY[i] / Constants.WORLD_HEIGHT;
            float alpha = MathUtils.clamp(0.9f - life * 0.8f, 0f, 1f) * (0.6f + 0.4f * MathUtils.sin(time * 3f + motePhase[i]));
            batch.setColor(0.55f, 1f, 0.95f, alpha);
            batch.draw(pixel, Math.round(moteX[i] + sway), Math.round(moteY[i]), 1f, 1f);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void resetMote(int i, boolean anywhere) {
        moteX[i] = MathUtils.random(0f, Constants.WORLD_WIDTH);
        moteY[i] = anywhere ? MathUtils.random(0f, Constants.WORLD_HEIGHT) : MathUtils.random(-30f, 0f);
        moteSpeed[i] = MathUtils.random(5f, 16f);
        motePhase[i] = MathUtils.random(MathUtils.PI2);
    }

    @Override
    public void resize(int width, int height) {
        bgViewport.update(width, height, false);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    // ------------------------------------------------------------------ menu

    private boolean handleKey(int keycode) {
        if (leaving) return true;
        if (controlsPanel.isVisible()) {
            if (keycode == Keys.ESCAPE || keycode == Keys.ENTER || keycode == Keys.E || keycode == Keys.SPACE) {
                toggleControls(false);
            }
            return true;
        }
        switch (keycode) {
            case Keys.W:
            case Keys.UP:
                select((selected - 1 + ITEMS.length) % ITEMS.length, true);
                return true;
            case Keys.S:
            case Keys.DOWN:
                select((selected + 1) % ITEMS.length, true);
                return true;
            case Keys.ENTER:
            case Keys.NUMPAD_ENTER:
            case Keys.SPACE:
            case Keys.E:
                activate();
                return true;
            case Keys.ESCAPE:
                select(ITEMS.length - 1, true);
                return true;
            default:
                return false;
        }
    }

    private void select(int index, boolean sound) {
        selected = index;
        for (int i = 0; i < itemLabels.size; i++) {
            boolean on = i == index;
            itemLabels.get(i).setStyle(on ? st.warm : st.body);
            itemCursors.get(i).setVisible(on);
        }
        if (sound) assets.play("select", 0.5f, 1f);
    }

    private void activate() {
        switch (selected) {
            case 0:
                leaving = true;
                assets.play("confirm", 0.8f, 1f);
                curtain.clearActions();
                curtain.addAction(Actions.sequence(Actions.fadeIn(0.7f),
                        Actions.run(() -> game.changeScreen(new GameScreen(game)))));
                break;
            case 1:
                toggleControls(true);
                break;
            default:
                leaving = true;
                curtain.clearActions();
                curtain.addAction(Actions.sequence(Actions.fadeIn(0.35f), Actions.run(() -> Gdx.app.exit())));
                break;
        }
    }

    private void toggleControls(boolean visible) {
        assets.play("select", 0.5f, visible ? 1.1f : 0.9f);
        controlsPanel.setVisible(visible);
        if (visible) {
            controlsPanel.clearActions();
            controlsPanel.addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.15f)));
        }
    }
}
