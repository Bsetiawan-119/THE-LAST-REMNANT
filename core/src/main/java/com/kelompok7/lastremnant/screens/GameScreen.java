// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : GameScreen.java
//  Bagian  : UI & game flow (layar permainan utama + titik integrasi semua modul)
//  PJ      : Budiman Setiawan
//  Minggu  : W2  (integrasi bertahap: Iqbal + Carlos + Insan digabung di sini)
//  OOP     : POLYMORPHISM -> update()/render() semua Entity tanpa cek jenis
//  Pattern : STATE (memakai GameStateManager), OBSERVER (mendaftarkan Hud & JournalUI)
//  LibGDX  : Screen, OrthographicCamera, FitViewport, Stage, InputMultiplexer
// =============================================================================
package com.kelompok7.lastremnant.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kelompok7.lastremnant.LastRemnantGame;
import com.kelompok7.lastremnant.assets.GameAssets;
import com.kelompok7.lastremnant.dialogue.Dialogue;
import com.kelompok7.lastremnant.dialogue.DialogueAction;
import com.kelompok7.lastremnant.dialogue.DialogueActionHandler;
import com.kelompok7.lastremnant.dialogue.DialogueRepository;
import com.kelompok7.lastremnant.dialogue.DialogueSession;
import com.kelompok7.lastremnant.entity.Direction;
import com.kelompok7.lastremnant.entity.Entity;
import com.kelompok7.lastremnant.entity.Interactable;
import com.kelompok7.lastremnant.entity.InteractionContext;
import com.kelompok7.lastremnant.entity.MemoryObject;
import com.kelompok7.lastremnant.entity.NPC;
import com.kelompok7.lastremnant.entity.Player;
import com.kelompok7.lastremnant.input.InputHandler;
import com.kelompok7.lastremnant.memory.Journal;
import com.kelompok7.lastremnant.memory.JournalEntry;
import com.kelompok7.lastremnant.memory.MemoryDatabase;
import com.kelompok7.lastremnant.memory.MemoryDevice;
import com.kelompok7.lastremnant.memory.MemoryEcho;
import com.kelompok7.lastremnant.memory.PrologueProgress;
import com.kelompok7.lastremnant.stage.StageOne;
import com.kelompok7.lastremnant.state.CombatState;
import com.kelompok7.lastremnant.state.DialogueState;
import com.kelompok7.lastremnant.state.WakeState;
import com.kelompok7.lastremnant.state.ExplorationState;
import com.kelompok7.lastremnant.state.GameStateManager;
import com.kelompok7.lastremnant.state.JournalState;
import com.kelompok7.lastremnant.story.CharacterDatabase;
import com.kelompok7.lastremnant.story.CharacterInfo;
import com.kelompok7.lastremnant.story.StoryManager;
import com.kelompok7.lastremnant.ui.DialogueUI;
import com.kelompok7.lastremnant.ui.Hud;
import com.kelompok7.lastremnant.ui.JournalUI;
import com.kelompok7.lastremnant.ui.UiStyles;
import com.kelompok7.lastremnant.util.Constants;
import com.kelompok7.lastremnant.world.CollisionSystem;
import com.kelompok7.lastremnant.world.World;
import com.kelompok7.lastremnant.world.StageArea;

/**
 * Menyatukan modul semua anggota:
 * <pre>
 *   Iqbal  : World, CollisionSystem, Player, InputHandler
 *   Carlos : DialogueRepository, StoryManager, CharacterDatabase, NPC
 *   Insan  : MemoryDevice, Journal, MemoryDatabase, MemoryObject
 *   Setiawan: GameStateManager + state, Hud, DialogueUI, JournalUI  (kelas ini)
 * </pre>
 * GameScreen sendiri tidak berisi logika "sedang apa pemain"; itu tugas state aktif.
 */
public class GameScreen extends ScreenAdapter implements InteractionContext, DialogueActionHandler {

    private final LastRemnantGame game;
    private final GameAssets assets;
    private final SpriteBatch batch;

    // --- kamera & viewport ---
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
    private final Vector2 cameraPos = new Vector2();
    private final Stage uiStage;

    // --- modul tim ---
    private World world;
    private StageArea area;
    private StageArea pendingArea;
    private StageOne stageOne;
    private boolean wakeCracks;
    private boolean awakeningShown;
    private final Vector2 pointer = new Vector2();
    private CollisionSystem collision;
    private Player player;
    private final InputHandler inputHandler = InputHandler.withDefaultBindings();
    private final GameStateManager stateManager = new GameStateManager();
    private final StoryManager story = new StoryManager();
    private final CharacterDatabase characters;
    private final DialogueRepository dialogues;
    private final MemoryDatabase memories;
    private final Journal journal = new Journal();
    private final MemoryDevice device = new MemoryDevice(journal);

    // --- UI ---
    private final UiStyles styles;
    private final Hud hud;
    private final DialogueUI dialogueUI;
    private final JournalUI journalUI;
    private final Image curtain;

    // --- isi dunia ---
    private final Array<Entity> entities = new Array<>();
    private final Array<Interactable> interactables = new Array<>();
    private final TextureRegion indicator;
    private final Vector2 tmpVec = new Vector2();
    private Interactable focus;
    private float time;
    private boolean leaving;

    public GameScreen(LastRemnantGame game) {
        this.game = game;
        this.assets = game.getAssets();
        this.batch = game.getBatch();

        // data naskah & memori (JSON di assets/data)
        characters = CharacterDatabase.fromJson(assets.readData("characters.json"));
        dialogues = DialogueRepository.fromJson(assets.readData("dialogues.json"));
        memories = MemoryDatabase.fromJson(assets.readData("memories.json"));

        loadArea(StageArea.PROLOGUE);

        // UI
        uiStage = new Stage(new FitViewport(Constants.UI_WIDTH, Constants.UI_HEIGHT), batch);
        styles = new UiStyles(assets);
        Image vignette = new Image(styles.vignette);
        vignette.setFillParent(true);
        vignette.setTouchable(Touchable.disabled);
        uiStage.addActor(vignette);
        hud = new Hud(uiStage, styles, device, journal);
        dialogueUI = new DialogueUI(uiStage, styles, assets, characters);
        journalUI = new JournalUI(uiStage, styles, assets, journal, device);
        curtain = styles.fadeCurtain();
        curtain.setDrawable(styles.solid(Color.WHITE));
        uiStage.addActor(curtain);
        indicator = assets.region("indicator");

        // Observer: HUD mendengarkan cerita & jurnal
        story.addListener(hud);
        journal.addListener(hud);
    }

    private void loadArea(StageArea destination) {
        World next = new World(destination.mapPath(), batch);
        if (world != null) world.dispose();
        area = destination;
        world = next;
        entities.clear();
        interactables.clear();
        focus = null;
        collision = new CollisionSystem(world.getSolids(), world.getBounds());
        World.Spawn spawn = world.findSpawn("player");
        player = new Player(spawn != null ? spawn.x : 48, spawn != null ? spawn.y : 48, collision);
        player.setSprites(assets.characterSprites(characters.get("zentra").getSpriteKey()));
        player.setFacing(Direction.RIGHT);
        entities.add(player);
        if (destination == StageArea.PROLOGUE) spawnEntities();
        else stageOne.load(world, destination);
        snapCamera();
    }

    /** Buat NPC & benda memori dari layer "entities" di peta. */
    private void spawnEntities() {
        for (World.Spawn s : world.getSpawns()) {
            switch (s.kind) {
                case "npc": {
                    CharacterInfo info = characters.get(s.prop("npcId"));
                    NPC npc = new NPC(info, assets.characterSprites(info.getSpriteKey()), s.x, s.y,
                            Direction.parse(s.prop("facing"), Direction.DOWN));
                    entities.add(npc);
                    interactables.add(npc);
                    collision.addEntity(npc);
                    break;
                }
                case "memory": {
                    MemoryEcho echo = memories.getEcho(s.prop("memoryId"));
                    MemoryObject obj = new MemoryObject(echo, device, assets.region(echo.getSpriteKey()),
                            assets.region("glow"), assets.region("pixel"), s.x, s.y);
                    entities.add(obj);
                    interactables.add(obj);
                    break;
                }
                default:
                    break;   // "player" sudah ditangani
            }
        }
    }

    // ================================================================== Screen

    @Override
    public void show() {
        curtain.setColor(0, 0, 0, 1);
        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(uiStage);                       // klik mouse pada pilihan dialog
        mux.addProcessor(new InputAdapter() {            // tombol khusus state (jurnal, dialog)
            @Override
            public boolean keyDown(int keycode) {
                return !leaving && stateManager.keyDown(keycode);
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointerIndex, int button) {
                if (leaving || button != Input.Buttons.LEFT || !stateManager.isCurrent(CombatState.class)) return false;
                viewport.unproject(pointer.set(screenX, screenY));
                stageOne.attack(pointer.x - player.getX(), pointer.y - player.getY());
                return true;
            }
        });
        mux.addProcessor(inputHandler);                  // tombol gerak & interaksi (Command)
        Gdx.input.setInputProcessor(mux);

        snapCamera();
        stateManager.set(new ExplorationState(this));
        curtain.addAction(Actions.sequence(Actions.alpha(1f), Actions.fadeOut(1.1f)));
        hud.showLocation("DEPOK SELATAN", world.getDisplayName() + "  -  16.40");
        startDialogue("intro", null);
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, Constants.MAX_DELTA);
        time += delta;

        stateManager.update(delta);
        applyPendingArea();
        hud.update(delta);

        ScreenUtils.clear(0.06f, 0.05f, 0.09f, 1f);
        viewport.apply();
        // kamera dibulatkan ke pixel agar tile tidak "bergetar"
        camera.position.set(Math.round(cameraPos.x), Math.round(cameraPos.y), 0f);
        camera.update();

        batch.setColor(1f, 1f, 1f, 1f);   // reset warna sisa Scene2D (tirai fade) agar dunia tidak transparan
        world.renderBelow(camera);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        entities.sort((a, b) -> Float.compare(b.getDepth(), a.getDepth()));
        for (Entity e : entities) {
            e.render(batch);               // polimorfisme: Player, NPC, MemoryObject
        }
        if (stageOne != null) stageOne.render(batch);
        batch.end();
        world.renderAbove(camera);
        if (wakeCracks) renderCracks();

        // penanda interaksi digambar paling atas supaya tidak tertutup pohon
        if (focus != null) {
            batch.begin();
            Vector2 p = focus.getIndicatorPosition(tmpVec);
            float bob = Math.round(MathUtils.sin(time * 5f) * 1.5f);
            batch.draw(indicator, Math.round(p.x - indicator.getRegionWidth() / 2f), Math.round(p.y + 3f + bob));
            batch.end();
        }

        uiStage.getViewport().apply();
        uiStage.act(delta);
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        inputHandler.reset();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        story.removeListener(hud);
        journal.removeListener(hud);
        hud.detachCombat();
        world.dispose();
        uiStage.dispose();
    }

    // ================================================================== dipakai oleh state

    /** Update semua entity (polimorfik) + kamera. */
    public void updateWorld(float delta) {
        if (leaving) return;
        for (Entity e : entities) {
            e.update(delta);
        }
        updateCamera(delta);
    }

    /** Interactable terdekat yang bersinggungan dengan probe di depan Iwan. */
    public Interactable findInteractTarget() {
        if (leaving) return null;
        Rectangle probe = player.getInteractionProbe();
        Interactable best = null;
        float bestDist = Float.MAX_VALUE;
        for (Interactable it : interactables) {
            if (!it.canInteract()) continue;
            Rectangle b = it.getInteractBounds();
            if (!b.overlaps(probe)) continue;
            float cx = b.x + b.width / 2f - player.getX();
            float cy = b.y + b.height / 2f - player.getY();
            float d = cx * cx + cy * cy;
            if (d < bestDist) {
                bestDist = d;
                best = it;
            }
        }
        return best;
    }

    public void setFocus(Interactable target) {
        focus = target;
        String prompt = target != null ? target.getInteractLabel(this) : null;
        if (prompt == null && !leaving && stageOne != null && stateManager.isCurrent(CombatState.class)
                && stageOne.canBackstab()) prompt = "Tusuk Boneka dari belakang";
        hud.setPrompt(prompt);
    }

    public void openJournal() {
        stateManager.push(new JournalState(this));
    }

    /** Kembali ke menu utama dengan fade. (W4: diganti menu jeda) */
    public void returnToMenu() {
        if (leaving) return;
        leaving = true;
        inputHandler.setEnabled(false);
        assets.play("select", 0.6f, 0.8f);
        curtain.clearActions();
        curtain.addAction(Actions.sequence(Actions.fadeIn(0.5f),
                Actions.run(() -> game.changeScreen(new MainMenu(game)))));
    }

    private void updateCamera(float delta) {
        float tx = player.getX();
        float ty = player.getY() + 12f;
        float k = Math.min(1f, delta * Constants.CAMERA_LERP);
        cameraPos.x += (tx - cameraPos.x) * k;
        cameraPos.y += (ty - cameraPos.y) * k;
        clampCamera();
    }

    private void snapCamera() {
        cameraPos.set(player.getX(), player.getY() + 12f);
        clampCamera();
    }

    private void clampCamera() {
        Rectangle b = world.getBounds();
        float hw = Constants.WORLD_WIDTH / 2f;
        float hh = Constants.WORLD_HEIGHT / 2f;
        cameraPos.x = b.width <= hw * 2 ? b.width / 2f : MathUtils.clamp(cameraPos.x, hw, b.width - hw);
        cameraPos.y = b.height <= hh * 2 ? b.height / 2f : MathUtils.clamp(cameraPos.y, hh, b.height - hh);
    }

    // ================================================================== InteractionContext

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public StoryManager getStory() {
        return story;
    }

    @Override
    public MemoryDevice getMemoryDevice() {
        return device;
    }

    @Override
    public void startDialogue(String dialogueId, Runnable onFinished) {
        if (!dialogues.has(dialogueId)) {
            Gdx.app.error("GameScreen", "Dialog tidak ditemukan: " + dialogueId);
            if (onFinished != null) onFinished.run();
            return;
        }
        if ("sari_photo".equals(dialogueId) && area == StageArea.PROLOGUE) {
            startDialogue(dialogues.get(dialogueId), () -> {
                if (onFinished != null) onFinished.run();
                beginWake();
            });
            return;
        }
        startDialogue(dialogues.get(dialogueId), onFinished);
    }

    @Override
    public void startDialogue(Dialogue dialogue, Runnable onFinished) {
        DialogueSession session = new DialogueSession(dialogue, story, this);
        stateManager.push(new DialogueState(this, session, onFinished));
    }

    @Override
    public void onEchoScanned(MemoryEcho echo, JournalEntry entry) {
        assets.play("scan", 0.8f, 1f);
        story.setObjective(PrologueProgress.objective(device));
    }

    // ================================================================== DialogueActionHandler

    @Override
    public void onDialogueAction(DialogueAction action) {
        String v = action.getValue();
        switch (action.getType()) {
            case "flag":
                story.setFlag(v);
                break;
            case "unflag":
                story.clearFlag(v);
                break;
            case "objective":
                story.setObjective(v);
                break;
            case "device":
                if ("on".equals(v)) device.activate();
                break;
            case "note":
                journal.addEntry(memories.createNote(v));
                break;
            case "sfx":
                assets.play(v);
                break;
            case "toast":
                hud.showToast(v);
                break;
            default:
                Gdx.app.error("GameScreen", "Aksi dialog tidak dikenal: " + action);
        }
    }

    private void beginWake() {
        if (wakeCracks) return;
        wakeCracks = true;
        story.setObjective("");
        startDialogue("wake_up", () -> stateManager.set(new WakeState(this)));
    }

    public void setWakeFlash(float elapsed) {
        curtain.clearActions();
        if (elapsed < 0.48f) curtain.setColor(1, 1, 1, Math.min(elapsed * 5f, 1f));
        else curtain.setColor(0, 0, 0, 1);
    }

    private void renderCracks() {
        float cx = camera.position.x, cy = camera.position.y;
        batch.begin();
        batch.setColor(0.58f, 0.93f, 1f, 0.75f);
        crack(cx, cy + 20, cx - 53, cy + 54);
        crack(cx - 53, cy + 54, cx - 121, cy + 48);
        crack(cx - 53, cy + 54, cx - 80, cy + 126);
        crack(cx, cy + 20, cx + 48, cy + 55);
        crack(cx + 48, cy + 55, cx + 142, cy + 95);
        crack(cx + 48, cy + 55, cx + 73, cy - 46);
        crack(cx, cy + 20, cx - 43, cy - 61);
        crack(cx - 43, cy - 61, cx - 91, cy - 126);
        batch.setColor(1, 1, 1, 1);
        batch.end();
    }

    private void crack(float x, float y, float endX, float endY) {
        float dx = endX - x, dy = endY - y;
        batch.draw(assets.region("pixel"), x, y, 0, 0, Vector2.len(dx, dy), 1, 1, 1,
                MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees);
    }

    public void queueArea(StageArea destination) {
        if (!leaving && pendingArea == null) pendingArea = destination;
    }

    private void applyPendingArea() {
        if (pendingArea == null || leaving) return;
        StageArea next = pendingArea;
        pendingArea = null;
        if (stageOne == null) createStageOne();
        inputHandler.reset();
        loadArea(next);
        wakeCracks = false;
        curtain.setColor(0, 0, 0, 1);
        curtain.clearActions();
        curtain.addAction(Actions.fadeOut(0.68f));
        stateManager.set(new CombatState(this));
        hud.showLocation(world.getDisplayName().toUpperCase(java.util.Locale.ROOT), "STAGE 1");
        if (next == StageArea.COCOON_CITY && !awakeningShown) {
            awakeningShown = true;
            startDialogue("cocoon_awakening", null);
        }
    }

    private void createStageOne() {
        stageOne = new StageOne(this);
        hud.attachCombat(stageOne.health(), stageOne.inventory());
    }

    public void offerRetry() {
        startDialogue(Dialogue.linear("retry_stage1", "narrator",
                "Pandangan Zentra gelap. Tekan E untuk memulai ulang Kota Kepompong."), () -> {
            createStageOne();
            story.clearFlag("hid_in_closet");
            queueArea(StageArea.COCOON_CITY);
        });
    }

    public void addStageEntity(Entity entity) {
        entities.add(entity);
        if (entity instanceof Interactable target) interactables.add(target);
    }

    public void removeStageEntity(Entity entity) {
        entities.removeValue(entity, true);
        if (entity instanceof Interactable target) interactables.removeValue(target, true);
    }

    public void addNote(String id) { journal.addEntry(memories.createNote(id)); }
    public CollisionSystem collision() { return collision; }
    public StageOne stageOne() { return stageOne; }

    // ================================================================== getter untuk state

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public GameStateManager getStateManager() {
        return stateManager;
    }

    public Hud getHud() {
        return hud;
    }

    public DialogueUI getDialogueUI() {
        return dialogueUI;
    }

    public JournalUI getJournalUI() {
        return journalUI;
    }

    public Journal getJournal() {
        return journal;
    }

    public GameAssets getAssets() {
        return assets;
    }
}
