// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Weapon.java
//  Bagian  : Combat (kontrak senjata)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  OOP     : ABSTRACTION  -> Inventory, Command, dan HUD hanya mengenal Weapon
//            INHERITANCE  -> MeleeWeapon / RangedWeapon -> Knife / Pistol
//            POLYMORPHISM -> serangan tiap senjata lewat createAttack()
//            (Template Method: alur tryAttack() sama, isinya beda per senjata)
//  Test    : core/src/test/.../combat/WeaponTest.java
// =============================================================================
package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.entity.Direction;

/**
 * Semua senjata yang bisa dipegang Zentra.
 * <p>
 * Alur serangan dikunci di {@link #tryAttack(Vector2, Direction)}:
 * cek cooldown -> cek syarat khusus senjata (mis. peluru) -> buat serangan.
 * Subclass hanya menentukan bentuk serangannya, sehingga aturan cooldown
 * pasti konsisten untuk semua senjata (kriteria penerimaan combat).
 */
public abstract class Weapon {

    private final String id;
    private final String displayName;
    private final String spriteKey;
    private final int damage;
    private final float cooldown;
    private float cooldownLeft;

    protected Weapon(String id, String displayName, String spriteKey, int damage, float cooldown) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id senjata wajib ada");
        if (damage <= 0) throw new IllegalArgumentException("damage harus > 0");
        if (cooldown < 0) throw new IllegalArgumentException("cooldown tidak boleh negatif");
        this.id = id;
        this.displayName = displayName;
        this.spriteKey = spriteKey;
        this.damage = damage;
        this.cooldown = cooldown;
    }

    /** Hitung mundur cooldown. Subclass yang butuh timer lain wajib memanggil super.update(). */
    public void update(float delta) {
        if (cooldownLeft > 0f) cooldownLeft = Math.max(0f, cooldownLeft - delta);
    }

    /**
     * Coba menyerang dari titik {@code origin} (kaki pemilik) ke arah {@code facing}.
     *
     * @return serangan baru, atau null bila senjata belum siap (cooldown, peluru habis, isi ulang)
     */
    public final Attack tryAttack(Vector2 origin, Direction facing) {
        if (!isReady()) return null;
        if (!consumeForAttack()) return null;
        cooldownLeft = cooldown;
        return createAttack(origin, facing);
    }

    /** Siap dipakai: cooldown habis dan syarat khusus senjata terpenuhi. */
    public boolean isReady() {
        return cooldownLeft <= 0f && canFire();
    }

    /** Syarat tambahan per senjata. Default: selalu bisa (senjata jarak dekat). */
    protected boolean canFire() {
        return true;
    }

    /**
     * Pakai sumber daya untuk satu serangan (mis. satu peluru).
     *
     * @return false bila serangan batal
     */
    protected boolean consumeForAttack() {
        return true;
    }

    /** Bentuk serangan khas senjata ini. */
    protected abstract Attack createAttack(Vector2 origin, Direction facing);

    /** Pisau tidak bisa dibuang; senjata lain bisa. */
    public boolean isDroppable() {
        return true;
    }

    /**
     * Gabungkan senjata sejenis yang dipungut (mis. peluru pistol kedua masuk cadangan).
     * Dipakai Inventory supaya tidak ada dua senjata sama di dua slot.
     *
     * @return true bila senjata lain berhasil diserap
     */
    public boolean absorb(Weapon other) {
        return false;
    }

    /** Teks amunisi untuk HUD, mis. "4/6 | 12". Null = tidak memakai amunisi. */
    public String getAmmoText() {
        return null;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSpriteKey() {
        return spriteKey;
    }

    public int getDamage() {
        return damage;
    }

    public float getCooldown() {
        return cooldown;
    }

    public float getCooldownLeft() {
        return cooldownLeft;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + id + ")";
    }
}
