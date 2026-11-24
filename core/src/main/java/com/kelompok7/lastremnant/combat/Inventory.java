// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Inventory.java
//  Bagian  : Combat (dua slot senjata + pisau tetap)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  OOP     : ENCAPSULATION -> isi slot hanya berubah lewat pickup()/drop()/select(),
//            jadi aturan "pisau tetap" dan "tanpa duplikasi" tidak bisa dilanggar
//  Pattern : OBSERVER (subject) -> InventoryListener (HUD senjata/peluru)
//  Test    : core/src/test/.../combat/InventoryTest.java
// =============================================================================
package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok7.lastremnant.entity.Direction;
import com.kelompok7.lastremnant.util.Constants;

/**
 * Inventori senjata Zentra.
 * <pre>
 *   slot 0      : pisau (tetap, tidak bisa dibuang/ditukar)
 *   slot 1..n   : senjata pungutan (default n = 2)
 * </pre>
 * Aturan (kriteria penerimaan "dua slot + pisau tetap, ambil/buang tanpa duplikasi"):
 * <ul>
 *   <li>Benda yang sama tidak bisa ada di dua slot.</li>
 *   <li>Senjata sejenis yang dipungut digabung pelurunya, tidak memakan slot baru.</li>
 *   <li>Slot penuh: senjata di tangan ditukar dan dikembalikan ke pemanggil untuk diletakkan di lantai.</li>
 *   <li>Ganti senjata membatalkan isi ulang.</li>
 * </ul>
 */
public class Inventory {

    public static final int KNIFE_SLOT = 0;

    private final Weapon[] slots;
    private final Array<InventoryListener> listeners = new Array<>();
    private int activeIndex = KNIFE_SLOT;
    private boolean wasReloading;

    public Inventory() {
        this(new Knife(), Constants.WEAPON_SLOTS);
    }

    public Inventory(Weapon fixedWeapon, int extraSlots) {
        if (fixedWeapon == null) throw new IllegalArgumentException("senjata tetap wajib ada");
        if (extraSlots < 1) throw new IllegalArgumentException("minimal 1 slot tambahan");
        slots = new Weapon[extraSlots + 1];
        slots[KNIFE_SLOT] = fixedWeapon;
    }

    // ------------------------------------------------------------------ per frame

    /** Hitung cooldown & isi ulang semua senjata. */
    public void update(float delta) {
        for (Weapon w : slots) {
            if (w != null) w.update(delta);
        }
        boolean reloading = isActiveReloading();
        if (wasReloading && !reloading) notifyAmmo(getActive());   // isi ulang selesai
        wasReloading = reloading;
    }

    /**
     * Serang dengan senjata di tangan. Pistol kosong otomatis mulai isi ulang
     * (bila ada cadangan) supaya pemain tidak terjebak menekan tombol serang.
     *
     * @return serangan baru atau null bila senjata belum siap
     */
    public Attack attack(Vector2 origin, Direction facing) {
        Weapon w = getActive();
        Attack a = w.tryAttack(origin, facing);
        if (w instanceof RangedWeapon) {
            RangedWeapon r = (RangedWeapon) w;
            if (a != null) {
                notifyAmmo(w);
            } else if (r.isEmpty() && r.startReload()) {
                wasReloading = true;
                notifyAmmo(w);
            }
        }
        return a;
    }

    /** @return true bila isi ulang dimulai. */
    public boolean reload() {
        Weapon w = getActive();
        if (!(w instanceof RangedWeapon)) return false;
        boolean started = ((RangedWeapon) w).startReload();
        if (started) {
            wasReloading = true;
            notifyAmmo(w);
        }
        return started;
    }

    // ------------------------------------------------------------------ slot

    /**
     * Pilih slot. Slot kosong tidak bisa dipilih.
     *
     * @return true bila senjata di tangan berganti
     */
    public boolean select(int index) {
        if (index < 0 || index >= slots.length || slots[index] == null || index == activeIndex) return false;
        cancelReload();
        activeIndex = index;
        notifyActive();
        return true;
    }

    /** Pindah ke slot terisi berikutnya (berputar kembali ke pisau). */
    public boolean selectNext() {
        for (int step = 1; step < slots.length; step++) {
            int i = (activeIndex + step) % slots.length;
            if (slots[i] != null) return select(i);
        }
        return false;
    }

    /**
     * Ambil senjata dari lantai.
     *
     * @return hasil; untuk {@link PickupResult.Type#SWAPPED} senjata lama ada di {@code getDropped()}
     */
    public PickupResult pickup(Weapon weapon) {
        if (weapon == null) throw new IllegalArgumentException("senjata null");
        if (indexOf(weapon) >= 0) return PickupResult.rejected();           // benda yang sama

        int same = indexOfId(weapon.getId());
        if (same >= 0) {
            if (!slots[same].absorb(weapon)) return PickupResult.rejected(); // mis. pisau kedua
            notifyAmmo(slots[same]);
            return PickupResult.merged();
        }

        for (int i = 1; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = weapon;
                notifySlots();
                select(i);
                return PickupResult.stored();
            }
        }

        // penuh: tukar dengan senjata di tangan; kalau sedang pegang pisau, tukar slot 1
        int target = activeIndex == KNIFE_SLOT ? 1 : activeIndex;
        cancelReload();
        Weapon old = slots[target];
        slots[target] = weapon;
        activeIndex = target;
        notifySlots();
        notifyActive();
        return PickupResult.swapped(old);
    }

    /**
     * Buang senjata di tangan. Pisau tidak bisa dibuang.
     *
     * @return senjata yang dibuang (untuk diletakkan di lantai), atau null
     */
    public Weapon dropActive() {
        Weapon w = getActive();
        if (!w.isDroppable()) return null;
        cancelReload();
        slots[activeIndex] = null;
        activeIndex = KNIFE_SLOT;
        notifySlots();
        notifyActive();
        return w;
    }

    private void cancelReload() {
        Weapon w = getActive();
        if (w instanceof RangedWeapon && ((RangedWeapon) w).isReloading()) {
            ((RangedWeapon) w).cancelReload();
            notifyAmmo(w);
        }
        wasReloading = false;
    }

    // ------------------------------------------------------------------ getter

    public Weapon getActive() {
        return slots[activeIndex];
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public Weapon getSlot(int index) {
        return slots[index];
    }

    public int getSlotCount() {
        return slots.length;
    }

    /** Jumlah senjata dipegang, termasuk pisau. */
    public int getWeaponCount() {
        int n = 0;
        for (Weapon w : slots) {
            if (w != null) n++;
        }
        return n;
    }

    public boolean has(String weaponId) {
        return indexOfId(weaponId) >= 0;
    }

    public boolean isFull() {
        for (int i = 1; i < slots.length; i++) {
            if (slots[i] == null) return false;
        }
        return true;
    }

    private boolean isActiveReloading() {
        Weapon w = getActive();
        return w instanceof RangedWeapon && ((RangedWeapon) w).isReloading();
    }

    private int indexOf(Weapon weapon) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == weapon) return i;
        }
        return -1;
    }

    private int indexOfId(String id) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null && slots[i].getId().equals(id)) return i;
        }
        return -1;
    }

    // ------------------------------------------------------------------ observer

    public void addListener(InventoryListener l) {
        if (l != null && !listeners.contains(l, true)) listeners.add(l);
    }

    public void removeListener(InventoryListener l) {
        listeners.removeValue(l, true);
    }

    private void notifyActive() {
        for (int i = 0; i < listeners.size; i++) listeners.get(i).onActiveWeaponChanged(getActive(), activeIndex);
    }

    private void notifySlots() {
        for (int i = 0; i < listeners.size; i++) listeners.get(i).onSlotsChanged(this);
    }

    private void notifyAmmo(Weapon w) {
        for (int i = 0; i < listeners.size; i++) listeners.get(i).onAmmoChanged(w);
    }
}
