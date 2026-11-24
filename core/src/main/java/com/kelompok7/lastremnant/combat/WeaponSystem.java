// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : WeaponSystem.java
//  Bagian  : Combat (penghubung Player, senjata di lantai, peluru, dan target)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  Kontrak : dipanggil CombatState / ExplorationState (Setiawan) tiap frame,
//            target = Damageable (Enemy & Sang Exitium milik Insan)
//  Pattern : OBSERVER (subject) -> WeaponSystemListener
//  Test    : core/src/test/.../combat/WeaponSystemTest.java
// =============================================================================
package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kelompok7.lastremnant.entity.Player;
import com.kelompok7.lastremnant.util.Constants;
import com.kelompok7.lastremnant.world.CollisionSystem;
import com.kelompok7.lastremnant.world.World;

/**
 * Satu titik integrasi combat:
 * <ol>
 *   <li>proses permintaan ambil/buang senjata dari Player,</li>
 *   <li>ubah {@link Attack} dari Player menjadi sabetan / peluru,</li>
 *   <li>cocokkan serangan dengan target {@link Damageable}.</li>
 * </ol>
 * Senjata di lantai digambar lewat {@link #renderFloor}, peluru & efek lewat
 * {@link #renderEffects}. Semua sprite boleh null (unit test tanpa grafis).
 */
public class WeaponSystem {

    /** Sumber gambar; di game diisi {@code assets::region}. */
    @FunctionalInterface
    public interface SpriteSource {
        TextureRegion region(String key);
    }

    private static final float SLASH_TIME = 0.12f;

    private final CollisionSystem collision;
    private final SpriteSource sprites;
    private final Array<WeaponPickup> floor = new Array<>();
    private final Array<Projectile> projectiles = new Array<>();
    private final Array<Slash> slashes = new Array<>();
    private final Array<Damageable> targets = new Array<>();
    private final Array<Attack> attackBuffer = new Array<>();
    private final Array<WeaponSystemListener> listeners = new Array<>();
    private final Rectangle tmp = new Rectangle();
    private final Rectangle tmp2 = new Rectangle();

    public WeaponSystem(CollisionSystem collision, SpriteSource sprites) {
        this.collision = collision;
        this.sprites = sprites;
    }

    // ------------------------------------------------------------------ setup

    /**
     * Buat senjata di lantai dari layer "entities" di peta.
     * Objek Tiled: {@code kind=weapon}, {@code weaponId=pistol}, opsional {@code ammo}, {@code reserve}.
     *
     * @return jumlah senjata yang dibuat
     */
    public int spawnFromWorld(World world) {
        int n = 0;
        for (World.Spawn s : world.getSpawns()) {
            if (!"weapon".equals(s.kind)) continue;
            String id = s.prop("weaponId");
            if (!WeaponFactory.isKnown(id)) continue;
            spawnPickup(WeaponFactory.create(id, parseInt(s.prop("ammo"), -1), parseInt(s.prop("reserve"), 0)), s.x, s.y);
            n++;
        }
        return n;
    }

    public WeaponPickup spawnPickup(Weapon weapon, float x, float y) {
        WeaponPickup p = new WeaponPickup(weapon, x, y, region(weapon.getSpriteKey()), region("shadow"));
        p.setOwner(this);
        floor.add(p);
        return p;
    }

    public void addTarget(Damageable target) {
        if (target != null && !targets.contains(target, true)) targets.add(target);
    }

    public void removeTarget(Damageable target) {
        targets.removeValue(target, true);
    }

    // ------------------------------------------------------------------ per frame

    /** Dipanggil SETELAH player.update() supaya serangan frame ini langsung diproses. */
    public void update(float delta, Player player) {
        if (player != null) {
            handleDrop(player);
            handlePickup(player);
            attackBuffer.clear();
            for (Attack a : player.drainAttacks(attackBuffer)) {
                resolve(a);
            }
        }
        updateProjectiles(delta);
        for (int i = slashes.size - 1; i >= 0; i--) {
            Slash s = slashes.get(i);
            s.life -= delta;
            if (s.life <= 0f) slashes.removeIndex(i);
        }
        for (WeaponPickup p : floor) p.update(delta);
    }

    private void handleDrop(Player player) {
        if (!player.consumeDropRequest()) return;
        Weapon w = player.getInventory().dropActive();
        if (w == null) {
            notifyRejected("Pisau tidak bisa dibuang.");
            return;
        }
        // letakkan sedikit di depan kaki; kalau di depan tembok, di bawah kaki saja
        float x = player.getX() + player.getFacing().dx * 10f;
        float y = player.getY() + player.getFacing().dy * 8f;
        tmp.set(x - 4f, y, 8f, 4f);
        if (collision != null && collision.isBlocked(tmp, null)) {
            x = player.getX();
            y = player.getY();
        }
        spawnPickup(w, x, y);
        for (int i = 0; i < listeners.size; i++) listeners.get(i).onDropped(w);
    }

    private void handlePickup(Player player) {
        if (!player.consumePickupRequest()) return;
        WeaponPickup p = findPickup(player.getX(), player.getY());
        if (p == null) {
            notifyRejected("Tidak ada senjata di dekat kaki.");
            return;
        }
        pickup(player, p);
    }

    /**
     * Ambil satu benda tertentu (tombol F memilih yang terdekat; tombol E lewat
     * Interactable memilih yang ada di depan pemain).
     *
     * @return true bila benda masuk inventori
     */
    public boolean pickup(Player player, WeaponPickup p) {
        if (!floor.contains(p, true)) return false;
        PickupResult r = player.getInventory().pickup(p.getWeapon());
        if (!r.isTaken()) {
            notifyRejected(p.getWeapon().getDisplayName() + " sudah dibawa.");
            return false;
        }
        floor.removeValue(p, true);
        if (r.getDropped() != null) {
            spawnPickup(r.getDropped(), p.getX(), p.getY());   // tukar tempat dengan yang diambil
        }
        for (int i = 0; i < listeners.size; i++) listeners.get(i).onPickedUp(p.getWeapon(), r);
        return true;
    }

    /** Senjata terdekat dalam jangkauan kaki, atau null. */
    public WeaponPickup findPickup(float x, float y) {
        WeaponPickup best = null;
        float bestD = Constants.PICKUP_REACH * Constants.PICKUP_REACH;
        for (WeaponPickup p : floor) {
            float d = p.distance2(x, y);
            if (d <= bestD) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }

    /** Teks petunjuk untuk senjata terdekat, mis. "Ambil pistol"; null bila tidak ada. */
    public String getPickupPrompt(Player player) {
        WeaponPickup p = findPickup(player.getX(), player.getY());
        return p == null ? null : p.describeFor(player.getInventory());
    }

    private void resolve(Attack a) {
        for (int i = 0; i < listeners.size; i++) listeners.get(i).onAttack(a);
        if (a.getKind() == Attack.Kind.MELEE) {
            a.getArea(tmp);
            for (int i = 0; i < targets.size; i++) {
                Damageable t = targets.get(i);
                if (t.isAlive() && t.getHurtbox().overlaps(tmp)) hit(t, a);
            }
            slashes.add(new Slash(a, tmp));
        } else {
            projectiles.add(new Projectile(a, collision, region("bullet")));
        }
    }

    private void updateProjectiles(float delta) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            Damageable t = p.advance(delta, targets);
            if (t != null) hit(t, p.getAttack());
            if (!p.isAlive()) projectiles.removeIndex(i);
        }
    }

    private void hit(Damageable t, Attack a) {
        t.takeDamage(a.getDamage(), a);
        for (int i = 0; i < listeners.size; i++) listeners.get(i).onHit(t, a);
    }

    // ------------------------------------------------------------------ render

    /** Senjata di lantai: digambar SEBELUM karakter (selalu di bawah kaki). */
    public void renderFloor(SpriteBatch batch) {
        for (WeaponPickup p : floor) p.render(batch);
    }

    /** Peluru & sabetan: digambar SETELAH karakter. */
    public void renderEffects(SpriteBatch batch) {
        for (Projectile p : projectiles) p.render(batch);
        TextureRegion px = region("pixel");
        if (px == null) return;
        for (Slash s : slashes) {
            float a = s.life / SLASH_TIME;
            batch.setColor(1f, 0.96f, 0.85f, 0.75f * a);
            // garis busur sederhana: tiga garis tipis yang memudar
            Rectangle r = s.area;
            boolean horizontal = s.attack.getDirection().dx != 0;
            for (int k = 0; k < 3; k++) {
                if (horizontal) {
                    batch.draw(px, r.x, r.y + 2 + k * (r.height - 4) / 2f, r.width, 1f);
                } else {
                    batch.draw(px, r.x + 2 + k * (r.width - 4) / 2f, r.y, 1f, r.height);
                }
            }
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    // ------------------------------------------------------------------ getter / observer

    public Array<WeaponPickup> getFloorItems() {
        return floor;
    }

    public Array<Projectile> getProjectiles() {
        return projectiles;
    }

    public int getActiveSlashCount() {
        return slashes.size;
    }

    public void addListener(WeaponSystemListener l) {
        if (l != null && !listeners.contains(l, true)) listeners.add(l);
    }

    public void removeListener(WeaponSystemListener l) {
        listeners.removeValue(l, true);
    }

    private void notifyRejected(String reason) {
        for (int i = 0; i < listeners.size; i++) listeners.get(i).onActionRejected(reason);
    }

    private TextureRegion region(String key) {
        return sprites == null ? null : sprites.region(key);
    }

    private static int parseInt(String s, int fallback) {
        if (s == null || s.trim().isEmpty()) return fallback;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** Efek sabetan singkat (hanya visual). */
    private static final class Slash {
        final Attack attack;
        final Rectangle area;
        float life = SLASH_TIME;

        Slash(Attack attack, Rectangle area) {
            this.attack = attack;
            this.area = new Rectangle(area);
        }
    }
}
