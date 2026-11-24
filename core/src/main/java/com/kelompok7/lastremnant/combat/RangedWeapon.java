// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : RangedWeapon.java
//  Bagian  : Combat (senjata berpeluru)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  OOP     : INHERITANCE (extends Weapon), ENCAPSULATION (peluru hanya berubah
//            lewat tembak / isi ulang / absorb, selalu tervalidasi)
// =============================================================================
package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.entity.Direction;

/**
 * Senjata dengan magasin dan peluru cadangan.
 * <ul>
 *   <li>Tembak memakai 1 peluru di magasin.</li>
 *   <li>Isi ulang memindahkan peluru cadangan ke magasin setelah {@code reloadTime} detik.</li>
 *   <li>Selama isi ulang senjata tidak bisa menembak.</li>
 * </ul>
 */
public abstract class RangedWeapon extends Weapon {

    private static final float BULLET_SIZE = 3f;

    private final int magazineSize;
    private final float reloadTime;
    private final float projectileSpeed;
    private final float projectileRange;
    private int loaded;
    private int reserve;
    private float reloadLeft;
    private final Rectangle tmp = new Rectangle();

    protected RangedWeapon(String id, String displayName, String spriteKey, int damage, float cooldown,
                           int magazineSize, float reloadTime, float projectileSpeed, float projectileRange,
                           int loaded, int reserve) {
        super(id, displayName, spriteKey, damage, cooldown);
        if (magazineSize <= 0) throw new IllegalArgumentException("magasin harus > 0");
        if (loaded < 0 || loaded > magazineSize) throw new IllegalArgumentException("isi magasin tidak valid: " + loaded);
        if (reserve < 0) throw new IllegalArgumentException("cadangan tidak boleh negatif");
        this.magazineSize = magazineSize;
        this.reloadTime = reloadTime;
        this.projectileSpeed = projectileSpeed;
        this.projectileRange = projectileRange;
        this.loaded = loaded;
        this.reserve = reserve;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (reloadLeft > 0f) {
            reloadLeft -= delta;
            if (reloadLeft <= 0f) finishReload();
        }
    }

    // ------------------------------------------------------------------ Weapon

    @Override
    protected boolean canFire() {
        return loaded > 0 && !isReloading();
    }

    @Override
    protected boolean consumeForAttack() {
        if (loaded <= 0) return false;
        loaded--;
        return true;
    }

    @Override
    protected Attack createAttack(Vector2 origin, Direction facing) {
        // peluru muncul dari tangan (sedikit di atas kaki, sedikit di depan badan)
        float hx = origin.x + facing.dx * 7f - BULLET_SIZE / 2f;
        float hy = origin.y + 7f + facing.dy * 5f - BULLET_SIZE / 2f;
        tmp.set(hx, hy, BULLET_SIZE, BULLET_SIZE);
        return Attack.projectile(getId(), getDamage(), facing, tmp, projectileSpeed, projectileRange);
    }

    /** Pistol kedua yang dipungut: semua pelurunya masuk cadangan. */
    @Override
    public boolean absorb(Weapon other) {
        if (other == this || !(other instanceof RangedWeapon) || !other.getId().equals(getId())) return false;
        RangedWeapon r = (RangedWeapon) other;
        addReserve(r.loaded + r.reserve);
        r.loaded = 0;
        r.reserve = 0;
        return true;
    }

    @Override
    public String getAmmoText() {
        if (isReloading()) return "isi ulang...";
        return loaded + "/" + magazineSize + " | " + reserve;
    }

    // ------------------------------------------------------------------ amunisi

    /**
     * Mulai isi ulang.
     *
     * @return false bila magasin sudah penuh, cadangan kosong, atau sedang isi ulang
     */
    public boolean startReload() {
        if (isReloading() || loaded >= magazineSize || reserve <= 0) return false;
        if (reloadTime <= 0f) {
            finishReload();
        } else {
            reloadLeft = reloadTime;
        }
        return true;
    }

    private void finishReload() {
        reloadLeft = 0f;
        int moved = Math.min(magazineSize - loaded, reserve);
        loaded += moved;
        reserve -= moved;
    }

    public void cancelReload() {
        reloadLeft = 0f;
    }

    public void addReserve(int amount) {
        if (amount < 0) throw new IllegalArgumentException("jumlah peluru tidak boleh negatif");
        reserve += amount;
    }

    public boolean isReloading() {
        return reloadLeft > 0f;
    }

    public boolean isEmpty() {
        return loaded == 0;
    }

    public int getLoaded() {
        return loaded;
    }

    public int getReserve() {
        return reserve;
    }

    public int getMagazineSize() {
        return magazineSize;
    }

    public float getReloadTime() {
        return reloadTime;
    }
}
