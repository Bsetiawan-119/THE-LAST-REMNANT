// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : WeaponFactory.java
//  Bagian  : Combat (membuat senjata dari id di peta Tiled)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
// =============================================================================
package com.kelompok7.lastremnant.combat;

/**
 * Satu tempat untuk mengubah {@code weaponId} di peta menjadi objek senjata.
 * Senjata baru (mis. senapan di Stage 2) cukup ditambahkan di sini.
 */
public final class WeaponFactory {

    private WeaponFactory() {
    }

    /**
     * @param ammo isi magasin awal; negatif = penuh (dibatasi ukuran magasin)
     * @throws IllegalArgumentException bila id tidak dikenal
     */
    public static Weapon create(String weaponId, int ammo, int reserve) {
        if (weaponId == null) throw new IllegalArgumentException("weaponId null");
        switch (weaponId) {
            case Knife.ID:
                return new Knife();
            case Pistol.ID: {
                Pistol p = new Pistol();
                int loaded = ammo < 0 ? p.getMagazineSize() : Math.min(ammo, p.getMagazineSize());
                return new Pistol(loaded, Math.max(0, reserve));
            }
            default:
                throw new IllegalArgumentException("Senjata tidak dikenal: " + weaponId);
        }
    }

    public static boolean isKnown(String weaponId) {
        return Knife.ID.equals(weaponId) || Pistol.ID.equals(weaponId);
    }
}
