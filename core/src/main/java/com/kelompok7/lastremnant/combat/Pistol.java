// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Pistol.java
//  Bagian  : Combat (senjata api Stage 1)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  OOP     : INHERITANCE (Weapon -> RangedWeapon -> Pistol)
// =============================================================================
package com.kelompok7.lastremnant.combat;

import com.kelompok7.lastremnant.util.Constants;

/** Pistol Stage 1: magasin 6 peluru (proposal: "ambil pistol berisi 6 peluru"). */
public class Pistol extends RangedWeapon {

    public static final String ID = "pistol";

    /** Pistol dengan magasin penuh dan tanpa cadangan. */
    public Pistol() {
        this(Constants.PISTOL_MAGAZINE, 0);
    }

    public Pistol(int loaded, int reserve) {
        super(ID, "Pistol", "weapon_pistol", Constants.PISTOL_DAMAGE, Constants.PISTOL_COOLDOWN,
                Constants.PISTOL_MAGAZINE, Constants.PISTOL_RELOAD_TIME,
                Constants.BULLET_SPEED, Constants.BULLET_RANGE, loaded, reserve);
    }
}
