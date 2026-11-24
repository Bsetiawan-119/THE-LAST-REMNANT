// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Knife.java
//  Bagian  : Combat (senjata tetap Zentra)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  OOP     : INHERITANCE (Weapon -> MeleeWeapon -> Knife)
// =============================================================================
package com.kelompok7.lastremnant.combat;

import com.kelompok7.lastremnant.util.Constants;

/** Pisau: selalu ada di slot 0, tanpa amunisi, dan tidak bisa dibuang. */
public class Knife extends MeleeWeapon {

    public static final String ID = "knife";

    public Knife() {
        super(ID, "Pisau", "weapon_knife", Constants.KNIFE_DAMAGE, Constants.KNIFE_COOLDOWN,
                Constants.KNIFE_REACH, 14f);
    }

    @Override
    public boolean isDroppable() {
        return false;
    }
}
