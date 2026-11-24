// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Damageable.java
//  Bagian  : Combat (interface antarmodul)
//  PJ      : Iqbal Aulia Rachman (kontrak)  -  diimplementasi Enemy / Sang Exitium (Insan)
//  Minggu  : W3
//  OOP     : ABSTRACTION + POLYMORPHISM (WeaponSystem tidak peduli jenis musuhnya)
// =============================================================================
package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.math.Rectangle;

/**
 * Sesuatu yang bisa terkena serangan senjata Zentra (Boneka, Penghapus, Sang Exitium).
 * WeaponSystem hanya bergantung ke interface ini, jadi modul Enemy/Health milik
 * Insan bisa dikerjakan paralel.
 */
public interface Damageable {

    /** Area tubuh yang bisa kena pukul / peluru (biasanya lebih tinggi dari hitbox kaki). */
    Rectangle getHurtbox();

    boolean isAlive();

    /** Terima damage dari satu serangan. Kebal/armor diatur oleh implementasi. */
    void takeDamage(int amount, Attack source);
}
