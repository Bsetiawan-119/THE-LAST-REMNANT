// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : PickupResult.java
//  Bagian  : Combat (hasil mengambil senjata)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
// =============================================================================
package com.kelompok7.lastremnant.combat;

/** Hasil {@link Inventory#pickup(Weapon)}. */
public final class PickupResult {

    public enum Type {
        /** Masuk slot kosong. */
        STORED,
        /** Senjata sejenis sudah ada: pelurunya digabung, benda di lantai hilang. */
        MERGED,
        /** Slot penuh: ditukar dengan senjata di tangan, yang lama jatuh ke lantai. */
        SWAPPED,
        /** Ditolak (benda yang sama sudah dipegang / pisau kedua). */
        REJECTED
    }

    private final Type type;
    private final Weapon dropped;

    private PickupResult(Type type, Weapon dropped) {
        this.type = type;
        this.dropped = dropped;
    }

    static PickupResult stored() {
        return new PickupResult(Type.STORED, null);
    }

    static PickupResult merged() {
        return new PickupResult(Type.MERGED, null);
    }

    static PickupResult swapped(Weapon dropped) {
        return new PickupResult(Type.SWAPPED, dropped);
    }

    static PickupResult rejected() {
        return new PickupResult(Type.REJECTED, null);
    }

    public Type getType() {
        return type;
    }

    /** Senjata lama yang harus diletakkan di lantai (hanya untuk SWAPPED). */
    public Weapon getDropped() {
        return dropped;
    }

    /** Benda di lantai sudah masuk inventori dan harus dihapus dari dunia. */
    public boolean isTaken() {
        return type != Type.REJECTED;
    }
}
