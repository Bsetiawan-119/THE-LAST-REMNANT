// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Direction.java
//  Bagian  : Core gameplay & world (arah hadap entity)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W1
// =============================================================================
package com.kelompok7.lastremnant.entity;

/**
 * Empat arah hadap karakter. Urutan enum = urutan baris di spritesheet
 * (baris 0 = bawah, 1 = kiri, 2 = kanan, 3 = atas).
 */
public enum Direction {
    DOWN(0, -1),
    LEFT(-1, 0),
    RIGHT(1, 0),
    UP(0, 1);

    public final int dx;
    public final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /** Baris pada spritesheet karakter. */
    public int sheetRow() {
        return ordinal();
    }

    /**
     * Arah dominan dari sebuah vektor gerak. Bila vektor nol, kembalikan fallback
     * supaya karakter tetap menghadap arah terakhir.
     */
    public static Direction fromVector(float x, float y, Direction fallback) {
        if (x == 0f && y == 0f) {
            return fallback;
        }
        if (Math.abs(x) > Math.abs(y)) {
            return x > 0 ? RIGHT : LEFT;
        }
        if (Math.abs(y) > Math.abs(x)) {
            return y > 0 ? UP : DOWN;
        }
        // diagonal sempurna: pertahankan arah lama jika masih searah, agar animasi tidak berkedip
        if (fallback.dx != 0 && Math.signum(x) == fallback.dx) return fallback;
        if (fallback.dy != 0 && Math.signum(y) == fallback.dy) return fallback;
        return y > 0 ? UP : DOWN;
    }

    /** Baca dari properti peta Tiled ("down", "left", ...). */
    public static Direction parse(String value, Direction fallback) {
        if (value == null) return fallback;
        try {
            return Direction.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
