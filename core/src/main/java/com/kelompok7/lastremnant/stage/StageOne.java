package com.kelompok7.lastremnant.stage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok7.lastremnant.combat.Ammo;
import com.kelompok7.lastremnant.combat.Boneka;
import com.kelompok7.lastremnant.combat.Enemy;
import com.kelompok7.lastremnant.combat.Health;
import com.kelompok7.lastremnant.combat.Inventory;
import com.kelompok7.lastremnant.combat.Knife;
import com.kelompok7.lastremnant.combat.Pemburu;
import com.kelompok7.lastremnant.combat.Pistol;
import com.kelompok7.lastremnant.combat.Weapon;
import com.kelompok7.lastremnant.combat.WeaponPickup;
import com.kelompok7.lastremnant.entity.Direction;
import com.kelompok7.lastremnant.entity.Player;
import com.kelompok7.lastremnant.memory.Amulet;
import com.kelompok7.lastremnant.screens.GameScreen;
import com.kelompok7.lastremnant.world.StageArea;
import com.kelompok7.lastremnant.world.World;

/** Progres dan entitas Kota Kepompong. GameScreen tetap mengelola state dan peta aktif. */
public final class StageOne {
    private final GameScreen screen;
    private final Health health = new Health(100);
    private final Inventory inventory = new Inventory();
    private final Array<Enemy> enemies = new Array<>();
    private final Vector2 aim = new Vector2();
    private final Vector2 traceStart = new Vector2();
    private final Vector2 traceEnd = new Vector2();
    private final TextureRegion pixel;
    private World.Spawn alarm;
    private HidingSpot closet;
    private Enemy visitor;
    private Enemy firstGuard;
    private float traceTime;
    private boolean alarmed;
    private boolean spentAid;

    public StageOne(GameScreen screen) {
        this.screen = screen;
        pixel = screen.getAssets().region("pixel");
    }

    public void load(World world, StageArea area) {
        alarm = null;
        alarmed = false;
        closet = null;
        visitor = null;
        firstGuard = null;
        traceTime = 0;
        enemies.clear();
        for (World.Spawn spawn : world.getSpawns()) {
            float x = spawn.x, y = spawn.y;
            switch (spawn.kind) {
                case "player" -> { }
                case "alarm" -> alarm = spawn;
                case "enemy" -> spawnGuard(spawn);
                case "weapon" -> {
                    Weapon weapon = Weapon.fromId(spawn.prop("weapon"), Integer.parseInt(spawn.prop("rounds")));
                    // Restart hanya dari kota. Pisau yang sudah diambil tetap dibawa.
                    if (!(weapon instanceof Knife) || inventory.knife() == null) addPickup(x, y, weapon);
                }
                case "pod" -> prop(spawn, "glow", spawn.prop("label"),
                        () -> screen.startDialogue(spawn.prop("dialogue"), null));
                case "aid" -> prop(spawn, "slot_full", "Pakai P3K", this::useAid);
                case "house" -> prop(spawn, "obj_parcel", "Masuk rumah No. 17", () -> {
                    if (firstGuard != null && !firstGuard.defeated()) {
                        toast("Hadapi Boneka Penjaga yang pertama dulu.");
                        return;
                    }
                    if (!alarmed) {
                        startChase();
                        toast("Alarm berbunyi. Masuk rumah untuk menghindari Pemburu!");
                        return;
                    }
                    screen.queueArea(StageArea.HOUSE_17);
                });
                case "closet" -> {
                    closet = new HidingSpot(x, y, screen.getAssets().region("panel"),
                            () -> objective("Tahan SPASI sampai Boneka pergi."));
                    screen.addStageEntity(closet);
                }
                case "radio" -> prop(spawn, "obj_radio", "Pindai radio Pak Darto", () -> {
                    if (closet == null || !closet.cleared()) {
                        toast("Tunggu sampai Boneka pergi. Bersembunyi di lemari.");
                        return;
                    }
                    screen.startDialogue("radio_hatch", () -> screen.queueArea(StageArea.HIDEOUT));
                });
                case "note" -> prop(spawn, "obj_photo", spawn.prop("label"), () -> {
                    screen.addNote(spawn.prop("note"));
                    screen.openJournal();
                });
                case "tv" -> prop(spawn, "slot_full", "Nyalakan TV", () -> screen.startDialogue("exit_tv", () -> {
                    screen.getStory().setFlag("watched_exit_tv");
                    if (!screen.getMemoryDevice().hasAmulet(Amulet.KENANG)) objective("Periksa radio Pak Darto di ruang ini.");
                }));
                case "kenang" -> prop(spawn, "obj_radio", "Buka radio Pak Darto", this::collectKenang);
                case "library" -> prop(spawn, "obj_parcel", "Pergi ke Perpustakaan Arsip", this::finish);
                case "ammo" -> prop(spawn, "slot_full", "Ambil peluru pistol", () -> {
                    if (screen.getStory().hasFlag("hideout_ammo")) return;
                    if (!inventory.addPistolAmmo(6)) { toast("Bawa pistol untuk mengambil peluru ini."); return; }
                    screen.getStory().setFlag("hideout_ammo");
                    toast("Peluru cadangan +6");
                });
                default -> throw new IllegalArgumentException("Objek Stage 1 tidak dikenal: " + spawn.kind);
            }
        }
        switch (area) {
            case COCOON_CITY -> objective(inventory.knife() == null
                    ? "Ambil pisau lalu hadapi Boneka Penjaga." : "Hadapi Boneka dan cari rumah No. 17.");
            case HOUSE_17 -> objective("Sembunyi di lemari sebelum Boneka menemukanmu.");
            case HIDEOUT -> objective("Periksa catatan di ruang rahasia dan nyalakan TV.");
            default -> { }
        }
    }

    private void spawnGuard(World.Spawn spawn) {
        boolean isVisitor = "visitor".equals(spawn.prop("role"));
        boolean first = "first".equals(spawn.prop("id"));
        Enemy enemy = new Boneka(spawn.x, spawn.y, first, screen.collision(), screen.getPlayer(),
                health, this::hiding, screen.getAssets().characterSprites("boneka"));
        if (isVisitor) visitor = enemy;
        if (first) firstGuard = enemy;
        enemies.add(enemy);
        screen.addStageEntity(enemy);
    }

    public boolean beforeWorld(float delta) {
        inventory.update(delta);
        traceTime = Math.max(0, traceTime - delta);
        if (!hiding()) return true;
        HidingSpot.Outcome outcome = closet.step(delta, Gdx.input.isKeyPressed(Keys.SPACE));
        if (outcome == HidingSpot.Outcome.SAFE) {
            if (visitor != null) visitor.retire();
            screen.getStory().setFlag("hid_in_closet");
            objective("Pindai radio Pak Darto.");
        } else if (outcome == HidingSpot.Outcome.CAUGHT) {
            health.damage(12);
            toast("Ketahuan! Masuk lemari dan tahan SPASI sampai Boneka pergi.");
            objective("Coba bersembunyi lagi di lemari.");
        }
        screen.getPlayer().stop();
        return false;
    }

    public void afterWorld() {
        if (!health.alive()) {
            screen.offerRetry();
            return;
        }
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            if (!enemy.defeated()) continue;
            enemies.removeIndex(i);
            screen.removeStageEntity(enemy);
            if (enemy instanceof Boneka && enemy != visitor) {
                addPickup(enemy.getX(), enemy.getY(), new Pistol(6));
                screen.addNote("guard_id");
                if (!alarmed) objective("Ambil pistol, lalu cari rumah No. 17.");
            }
        }
        if (alarm != null && !alarmed && screen.getPlayer().getX() >= alarm.x - 22) startChase();
    }

    private void startChase() {
        alarmed = true;
        objective("LARI ke rumah No. 17 di ujung timur!");
        screen.getAssets().play("scan", 0.9f, 0.65f);
        for (int i = 0; i < 8; i++) {
            float x = alarm.x - 18 + (i % 3) * 7;
            float y = alarm.y - 14 + (i / 3) * 9;
            Enemy hunter = new Pemburu(x, y, screen.collision(), screen.getPlayer(), health,
                    this::hiding, screen.getAssets().characterSprites("boneka"));
            enemies.add(hunter);
            screen.addStageEntity(hunter);
        }
    }

    private Enemy backstabTarget() {
        if (!(inventory.active() instanceof Knife) || hiding()) return null;
        Player player = screen.getPlayer();
        for (Enemy enemy : enemies) {
            if (enemy.active() && enemy.isBackExposed(player.getX(), player.getY())
                    && player.getInteractionProbe().overlaps(enemy.getHitbox())
                    && Vector2.dst2(player.getX(), player.getY(), enemy.getX(), enemy.getY()) <= 24 * 24
                    && screen.collision().hasClearLine(player.getX(), player.getY() + 5, enemy.getX(), enemy.getY() + 5)) {
                return enemy;
            }
        }
        return null;
    }

    public boolean canBackstab() { return backstabTarget() != null; }

    public void backstab() {
        Enemy target = backstabTarget();
        if (target != null && inventory.active().attack() != null) {
            target.hit(100, 0);
            screen.getAssets().play("select", 0.8f, 0.8f);
        }
    }

    public void attack(float dx, float dy) {
        if (hiding() || !health.alive()) return;
        Weapon weapon = inventory.active();
        if (weapon == null) { toast("Ambil pisau dulu."); return; }
        Weapon.Strike strike = weapon.attack();
        if (strike == null) {
            Ammo ammo = weapon.ammo();
            if (ammo != null && ammo.loaded() == 0) toast("Peluru habis. Tekan R atau ganti senjata dengan Q.");
            return;
        }
        Player player = screen.getPlayer();
        Direction facing = Direction.fromVector(dx, dy, player.getFacing());
        player.setFacing(facing);
        aim.set(dx, dy);
        if (aim.isZero()) aim.set(facing.dx, facing.dy);
        aim.nor();
        Enemy nearest = null;
        float nearestDistance = Float.MAX_VALUE;
        float minDot = MathUtils.cosDeg(strike.halfAngle());
        for (Enemy enemy : enemies) {
            if (!enemy.active()) continue;
            if (strike.shot()) enemy.hearShot(player.getX(), player.getY());
            float ex = enemy.getX() - player.getX(), ey = enemy.getY() - player.getY();
            float distance = Vector2.len(ex, ey);
            if (distance > strike.range() || distance >= nearestDistance) continue;
            if (distance > 0.01f && (ex * aim.x + ey * aim.y) / distance < minDot) continue;
            if (!screen.collision().hasClearLine(player.getX(), player.getY() + 5, enemy.getX(), enemy.getY() + 5)) continue;
            nearest = enemy;
            nearestDistance = distance;
        }
        if (nearest != null) {
            int damage = weapon instanceof Knife && nearest.isBackExposed(player.getX(), player.getY())
                    ? 100 : strike.damage();
            nearest.hit(damage, strike.stunSeconds());
        }
        float range = nearest == null ? strike.range() : nearestDistance;
        traceStart.set(player.getX(), player.getY() + 6);
        traceEnd.set(traceStart);
        for (float dist = Math.min(4, range); dist <= range && dist > 0; dist += 4) {
            float x = traceStart.x + aim.x * dist, y = traceStart.y + aim.y * dist;
            if (!screen.collision().hasClearLine(traceStart.x, traceStart.y, x, y)) break;
            traceEnd.set(x, y);
        }
        traceTime = 0.11f;
        screen.getAssets().play(strike.shot() ? "confirm" : "select", 0.75f, strike.shot() ? 0.58f : 1.1f);
    }

    public void render(SpriteBatch batch) {
        if (traceTime <= 0) return;
        float dx = traceEnd.x - traceStart.x, dy = traceEnd.y - traceStart.y;
        batch.setColor(1, 0.7f, 0.27f, traceTime / 0.11f);
        batch.draw(pixel, traceStart.x, traceStart.y, 0, 0, Vector2.len(dx, dy), 1.4f, 1, 1,
                MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees);
        batch.setColor(1, 1, 1, 1);
    }

    public void reload() {
        Weapon weapon = inventory.active();
        if (!hiding() && weapon != null && weapon.ammo() != null && !weapon.ammo().reload()) {
            toast("Magasin penuh atau peluru cadangan kosong.");
        }
    }

    public void switchWeapon() { if (!hiding()) inventory.cycle(); }

    public void drop() {
        if (hiding()) return;
        Weapon removed = inventory.drop();
        if (removed == null) return;
        Player player = screen.getPlayer();
        addPickup(player.getX(), player.getY(), removed);
    }

    private void addPickup(float x, float y, Weapon weapon) {
        screen.addStageEntity(new WeaponPickup(x, y, inventory, weapon,
                screen.getAssets().region("slot_full"), () -> toast("Dua slot penuh. Tekan G untuk buang senjata.")));
    }

    private void useAid() {
        if (spentAid || health.value() == health.max()) return;
        spentAid = true;
        health.heal(45);
        toast("HP pulih.");
    }

    private void collectKenang() {
        if (!screen.getStory().hasFlag("watched_exit_tv")) { toast("Cari petunjuk di TV dulu."); return; }
        if (!screen.getMemoryDevice().addAmulet(Amulet.KENANG)) return;
        screen.startDialogue("kenang_found", () -> objective("Pergi ke Perpustakaan Arsip Kota."));
    }

    private void finish() {
        if (!screen.getMemoryDevice().hasAmulet(Amulet.KENANG)) { toast("Temukan Amulet Kenang terlebih dahulu."); return; }
        if (!screen.getStory().setFlag("stage1_done")) return;
        screen.startDialogue("library_destination", () -> {
            objective("Stage 1 selesai. Tujuan berikutnya: Perpustakaan Arsip.");
            toast("Amulet Kenang tersimpan. Demo W3 selesai.");
        });
    }

    private void prop(World.Spawn spawn, String sprite, String label, Runnable action) {
        screen.addStageEntity(new StageProp(spawn.x, spawn.y,
                screen.getAssets().region(sprite), label, () -> true, action));
    }

    private void toast(String text) { screen.getHud().showToast(text); }
    private void objective(String text) { screen.getStory().setObjective(text); }
    public boolean hiding() { return closet != null && closet.hiding(); }
    public Health health() { return health; }
    public Inventory inventory() { return inventory; }
}
