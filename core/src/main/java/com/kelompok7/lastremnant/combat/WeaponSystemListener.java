// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : WeaponSystemListener.java
//  Bagian  : Combat (pengamat kejadian combat: suara, toast, getaran layar)
//  PJ      : Iqbal Aulia Rachman  -  didengarkan GameScreen / CombatState (Setiawan)
//  Minggu  : W3
//  Pattern : OBSERVER
// =============================================================================
package com.kelompok7.lastremnant.combat;

public interface WeaponSystemListener {

    /** Pemain menyerang (sabet atau tembak). */
    default void onAttack(Attack attack) {
    }

    /** Serangan mengenai target. */
    default void onHit(Damageable target, Attack attack) {
    }

    /** Senjata berhasil diambil dari lantai. */
    default void onPickedUp(Weapon weapon, PickupResult result) {
    }

    /** Senjata dibuang ke lantai. */
    default void onDropped(Weapon weapon) {
    }

    /** Tombol buang ditekan saat memegang pisau / tombol ambil tanpa benda di dekat kaki. */
    default void onActionRejected(String reason) {
    }
}
