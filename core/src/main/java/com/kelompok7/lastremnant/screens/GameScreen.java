// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : GameScreen.java
//  Bagian  : UI & game flow (layar permainan utama + titik integrasi semua modul)
//  PJ      : Budiman Setiawan
//  Minggu  : W2  (integrasi bertahap: Iqbal + Carlos + Insan digabung di sini)
//            W3  [Iqbal] WeaponSystem, senjata di lantai, Warp/Barrier/InspectPoint,
//                        peta Stage 1 (GameScreen(game, mapPath)). Ditandai "[W3 Iqbal]".
//  OOP     : POLYMORPHISM -> update()/render() semua Entity tanpa cek jenis
//  Pattern : STATE (memakai GameStateManager), OBSERVER (mendaftarkan Hud & JournalUI)
//  LibGDX  : Screen, OrthographicCamera, FitViewport, Stage, InputMultiplexer
// =============================================================================
package com.kelompok7.lastremnant.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
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
import com.kelompok7.lastremnant.combat.Attack;
import com.kelompok7.lastremnant.combat.Damageable;
import com.kelompok7.lastremnant.combat.Inventory;
import com.kelompok7.lastremnant.combat.InventoryListener;
import com.kelompok7.lastremnant.combat.PickupResult;
import com.kelompok7.lastremnant.combat.RangedWeapon;
import com.kelompok7.lastremnant.combat.TargetDummy;
import com.kelompok7.lastremnant.combat.Weapon;
import com.kelompok7.lastremnant.combat.WeaponPickup;
import com.kelompok7.lastremnant.combat.WeaponSystem;
import com.kelompok7.lastremnant.combat.WeaponSystemListener;
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
import com.kelompok7.lastremnant.state.DialogueState;
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
import com.kelompok7.lastremnant.world.Barrier;
import com.kelompok7.lastremnant.world.CollisionSystem;
import com.kelompok7.lastremnant.world.InspectPoint;
import com.kelompok7.lastremnant.world.Warp;
import com.kelompok7.lastremnant.world.World;

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
public class GameScreen extends ScreenAdapter implements InteractionContext, DialogueActionHandler,
        WeaponSystemListener, InventoryListener {

    /** Ganti peta awal tanpa ubah kode: ./gradlew lwjgl3:run "-Pmap=maps/kota_kepompong.tmx"  [W3 Iqbal] */
    public static final String MAP_PROPERTY = "lastremnant.map";

    private final LastRemnantGame game;
    private final GameAssets assets;
    private final SpriteBatch batch;

    // --- kamera & viewport ---
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
    private final Vector2 cameraPos = new Vector2();
    private final Stage uiStage;

    // --- modul tim ---
    private final World world;
    private final CollisionSystem collision;
    private final Player player;
    private final InputHandler inputHandler = InputHandler.withDefaultBindings();
    private final GameStateManager stateManager = new GameStateManager();
    private final StoryManager story = new StoryManager();
    private final CharacterDatabase characters;
    private final DialogueRepository dialogues;
    private final MemoryDatabase memories;
    private final Journal journal = new Journal();
    private final MemoryDevice device = new MemoryDevice(journal);
    private final String mapPath;                                   // [W3 Iqbal]
    private final WeaponSystem weapons;                             // [W3 Iqbal]
    private boolean combatHintShown;

    // --- UI ---
    private final UiStyles styles;
    private final Hud hud;
    private final DialogueUI dialogueUI;
    private final JournalUI journalUI;
    private final Image curtain;

    // --- isi dunia ---
    private final Array<Entity> entities = new Array<>();
    private final Array<Interactable> interactables = new Array<>();
    private final Array<Interactable> candidates = new Array<>();   // [W3 Iqbal]
    private final TextureRegion indicator;
    private final Vector2 tmpVec = new Vector2();
    private Interactable focus;
    private float time;
    private boolean leaving;

    public GameScreen(LastRemnantGame game) {
        this(game, System.getProperty(MAP_PROPERTY, Constants.TEST_MAP));
    }

    /** [W3 Iqbal] Dipakai alur BANGUN (Setiawan) untuk masuk ke Stage 1: Constants.STAGE1_MAP. */
    public GameScreen(LastRemnantGame game, String mapPath) {
        this.game = game;
        this.mapPath = mapPath;
        this.assets = game.getAssets();
        this.batch = game.getBatch();

        // data naskah & memori (JSON di assets/data)
        characters = CharacterDatabase.fromJson(assets.readData("characters.json"));
        dialogues = DialogueRepository.fromJson(assets.readData("dialogues.json"));
        memories = MemoryDatabase.fromJson(assets.readData("memories.json"));

        // dunia & entity
        world = new World(mapPath, batch);
        collision = new CollisionSystem(world.getSolids(), world.getBounds());
        World.Spawn ps = world.findSpawn("player");
        player = new Player(ps != null ? ps.x : 48f, ps != null ? ps.y : 48f, collision);
        player.setSprites(assets.characterSprites("zentra"));
        player.setFacing(ps != null ? Direction.parse(ps.prop("facing"), Direction.RIGHT) : Direction.RIGHT);
        entities.add(player);
        weapons = new WeaponSystem(collision, assets::region);      // [W3 Iqbal]
        weapons.spawnFromWorld(world);
        spawnEntities();

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
        uiStage.addActor(curtain);
        indicator = assets.region("indicator");

        // Observer: HUD mendengarkan cerita & jurnal
        story.addListener(hud);
        journal.addListener(hud);
        // [W3 Iqbal] sementara GameScreen yang mendengarkan combat (suara + toast).
        // Setiawan: HUD darah/peluru/senjata cukup implements InventoryListener juga.
        weapons.addListener(this);
        player.getInventory().addListener(this);
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
                // ---------------------------------------------------- [W3 Iqbal]
                case "dummy": {
                    TargetDummy d = new TargetDummy(s.x, s.y, parseInt(s.prop("hp"), 3),
                            assets.region("dummy"), assets.region("dummy_broken"));
                    entities.add(d);
                    collision.addEntity(d);
                    weapons.addTarget(d);
                    break;
                }
                case "warp": {
                    Warp w = Warp.fromSpawn(s, world::findSpawnByName);
                    entities.add(w);
                    interactables.add(w);
                    break;
                }
                case "barrier": {
                    String closed = s.prop("sprite");
                    String opened = s.prop("openSprite");
                    Barrier b = new Barrier(s.bounds(new Rectangle()), story, s.prop("openFlag"),
                            closed != null ? assets.region(closed) : null,
                            opened != null ? assets.region(opened) : null);
                    b.setOnOpened(() -> {
                        assets.play("confirm", 0.6f, 0.7f);
                        hud.showToast("Sesuatu bergeser...");
                    });
                    entities.add(b);
                    collision.addEntity(b);
                    break;
                }
                case "inspect": {
                    InspectPoint ip = InspectPoint.fromSpawn(s, story);
                    entities.add(ip);
                    interactables.add(ip);
                    break;
                }
                default:
                    break;   // "player"/"weapon" sudah ditangani; enemy/hiding/checkpoint/exit = penanda modul lain
            }
        }
    }

    // ================================================================== Screen

    @Override
    public void show() {
        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(uiStage);                       // klik mouse pada pilihan dialog
        mux.addProcessor(new InputAdapter() {            // tombol khusus state (jurnal, dialog)
            @Override
            public boolean keyDown(int keycode) {
                return !leaving && stateManager.keyDown(keycode);
            }
        });
        mux.addProcessor(inputHandler);                  // tombol gerak & interaksi (Command)
        Gdx.input.setInputProcessor(mux);

        snapCamera();
        stateManager.set(new ExplorationState(this));
        curtain.addAction(Actions.sequence(Actions.alpha(1f), Actions.fadeOut(1.1f)));
        if (Constants.TEST_MAP.equals(mapPath)) {
            hud.showLocation("DEPOK SELATAN", world.getDisplayName() + "  -  16.40");
            startDialogue("intro", null);
        } else {
            // [W3 Iqbal] Stage 1 dibuka langsung (uji peta). Alur BANGUN yang lengkap milik Setiawan/Carlos.
            device.activate();
            hud.showLocation("STAGE 1", world.getDisplayName());
            story.setObjective("Ambil pistol di pos ronda, lalu cari rumah No. 17.");
        }
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, Constants.MAX_DELTA);
        time += delta;

        stateManager.update(delta);
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
        weapons.renderFloor(batch);        // [W3 Iqbal] senjata di lantai selalu di bawah kaki
        entities.sort((a, b) -> Float.compare(b.getDepth(), a.getDepth()));
        for (Entity e : entities) {
            e.render(batch);               // polimorfisme: Player, NPC, MemoryObject, Barrier, TargetDummy
        }
        weapons.renderEffects(batch);      // [W3 Iqbal] peluru & sabetan
        batch.end();
        world.renderAbove(camera);

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
        weapons.removeListener(this);                       // [W3 Iqbal]
        player.getInventory().removeListener(this);
        world.dispose();
        uiStage.dispose();
    }

    // ================================================================== dipakai oleh state

    /** Update semua entity (polimorfik) + kamera. */
    public void updateWorld(float delta) {
        for (Entity e : entities) {
            e.update(delta);
        }
        weapons.update(delta, player);   // [W3 Iqbal] ambil/buang senjata, serangan, peluru
        updateCamera(delta);
    }

    /** Interactable terdekat yang bersinggungan dengan probe di depan zentra. */
    public Interactable findInteractTarget() {
        Rectangle probe = player.getInteractionProbe();
        Interactable best = null;
        float bestDist = Float.MAX_VALUE;
        candidates.clear();
        candidates.addAll(interactables);
        for (WeaponPickup wp : weapons.getFloorItems()) candidates.add(wp);   // [W3 Iqbal] senjata di lantai
        for (Interactable it : candidates) {
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
        hud.setPrompt(target != null ? target.getInteractLabel(this) : null);
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
        startDialogue(dialogues.get(dialogueId), onFinished);
    }

    @Override
    public void startDialogue(Dialogue dialogue, Runnable onFinished) {
        DialogueSession session = new DialogueSession(dialogue, story, this);
        Runnable next = () -> {
            if (onFinished != null) onFinished.run();
            if ("sari_photo".equals(dialogue.getId()) && story.hasFlag("bangun_done")) {
                enterStage1();
            }
        };
        stateManager.push(new DialogueState(this, session, next));
    }

    public void enterStage1() {
        if (leaving) return;
        leaving = true;
        inputHandler.setEnabled(false);
        assets.play("scan", 0.8f, 0.75f);
        curtain.clearActions();
        curtain.addAction(Actions.sequence(
                Actions.fadeIn(0.8f),
                Actions.run(() -> game.changeScreen(new GameScreen(game, Constants.STAGE1_MAP)))
        ));
    }

    @Override
    public void onEchoScanned(MemoryEcho echo, JournalEntry entry) {
        assets.play("scan", 0.8f, 1f);
        // [W2 sementara] progres pindai ditulis langsung di sini.
        // W3: dipindah ke QuestManager (Insan) yang mengumumkan perubahan lewat Observer.
        int done = device.getScannedCount();
        int total = memories.echoCount();
        if (done < total) {
            story.setObjective("Pindai benda bermakna dengan The Remnant (" + done + "/" + total + ").");
        } else {
            story.setObjective("Tunjukkan foto lama dari bangku taman kepada Bu Sari.");
        }
    }

    /** [W3 Iqbal] Pintu rumah: layar gelap sebentar, pemain pindah, kamera langsung ikut. */
    @Override
    public void teleportPlayer(float x, float y, Direction facing) {
        inputHandler.setEnabled(false);
        player.stop();
        assets.play("select", 0.5f, 0.7f);
        curtain.clearActions();
        curtain.addAction(Actions.sequence(Actions.fadeIn(0.18f), Actions.run(() -> {
            player.setPosition(x, y);
            if (facing != null) player.setFacing(facing);
            snapCamera();
            if (stateManager.isCurrent(ExplorationState.class)) inputHandler.setEnabled(true);
        }), Actions.fadeOut(0.25f)));
    }

    // ================================================================== combat [W3 Iqbal]

    @Override
    public void onAttack(Attack attack) {
        if (attack.getKind() == Attack.Kind.MELEE) {
            assets.play("slash", 0.6f, MathUtils.random(0.9f, 1.1f));
        } else {
            assets.play("shot", 0.7f, MathUtils.random(0.95f, 1.05f));
        }
    }

    @Override
    public void onHit(Damageable target, Attack attack) {
        assets.play("blip", 0.6f, target.isAlive() ? 0.7f : 0.45f);
    }

    @Override
    public void onPickedUp(Weapon weapon, PickupResult result) {
        assets.play("pickup", 0.8f, 1f);
        if (result.getType() == PickupResult.Type.MERGED) {
            hud.showToast("Peluru " + weapon.getDisplayName().toLowerCase() + " ditambahkan.");
        } else if (!combatHintShown) {
            combatHintShown = true;
            hud.showToast("K serang  -  R isi ulang  -  Q / 1-3 ganti  -  G buang");
        }
    }

    @Override
    public void onDropped(Weapon weapon) {
        assets.play("select", 0.5f, 0.6f);
    }

    @Override
    public void onActionRejected(String reason) {
        hud.showToast(reason);
    }

    @Override
    public void onActiveWeaponChanged(Weapon weapon, int slot) {
        String ammo = weapon.getAmmoText();
        hud.showToast("[" + (slot + 1) + "] " + weapon.getDisplayName() + (ammo != null ? "  " + ammo : ""));
    }

    @Override
    public void onAmmoChanged(Weapon weapon) {
        if (weapon instanceof RangedWeapon) {
            RangedWeapon r = (RangedWeapon) weapon;
            if (r.isReloading()) {
                assets.play("reload", 0.7f, 1f);
            } else if (r.isEmpty() && r.getReserve() == 0) {
                assets.play("empty", 0.7f, 1f);
                hud.showToast(weapon.getDisplayName() + " kosong.");
            }
        }
    }

    private static int parseInt(String s, int fallback) {
        try {
            return s == null ? fallback : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
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

    /** [W3 Iqbal] Untuk CombatState / HUD (Setiawan) dan Enemy (Insan: weapons.addTarget). */
    public WeaponSystem getWeaponSystem() {
        return weapons;
    }

    public Inventory getInventory() {
        return player.getInventory();
    }
}
