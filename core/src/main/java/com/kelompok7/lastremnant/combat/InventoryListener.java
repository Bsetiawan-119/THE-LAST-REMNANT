// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : InventoryListener.java
//  Bagian  : Combat (interface pengamat inventori)
//  PJ      : Iqbal Aulia Rachman  -  didengarkan HUD senjata/peluru (Setiawan)
//  Minggu  : W3
//  Pattern : OBSERVER. Semua method default supaya pengamat cukup override yang perlu.
// =============================================================================
package com.kelompok7.lastremnant.combat;

public interface InventoryListener {

    /** Senjata di tangan berganti (pilih slot, ambil, buang). */
    default void onActiveWeaponChanged(Weapon weapon, int slot) {
    }

    /** Isi slot berubah (ambil, tukar, buang). */
    default void onSlotsChanged(Inventory inventory) {
    }

    /** Peluru berubah (tembak, mulai/selesai isi ulang, peluru digabung). */
    default void onAmmoChanged(Weapon weapon) {
    }
}
