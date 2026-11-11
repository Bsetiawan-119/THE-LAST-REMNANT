// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Constants.java
//  Bagian  : Integrasi & setup repo (dipakai semua modul)
//  PJ      : Tim (disepakati bersama di W1)
//  Minggu  : W1, ditambah sedikit di W2
// =============================================================================
package com.kelompok7.lastremnant.util;

/** Konstanta global. Ubah angka di sini, bukan di dalam kode tiap modul. */
public final class Constants {

    private Constants() {
    }

    // --- identitas game ---
    public static final String TITLE = "The Last Remnant";
    public static final String VERSION = "v0.2.0 - Prototype W2";
    public static final String CREDIT = "Kelompok 7 - Final Project OOP";

    // --- resolusi virtual ---
    /** Ukuran dunia game (pixel art). 480x270 = 1/4 dari 1920x1080. */
    public static final float WORLD_WIDTH = 480f;
    public static final float WORLD_HEIGHT = 270f;
    /** Ukuran kanvas UI. Teks lebih tajam karena skala UI lebih kecil dari dunia. */
    public static final float UI_WIDTH = 640f;
    public static final float UI_HEIGHT = 360f;

    // --- dunia ---
    public static final int TILE_SIZE = 16;
    public static final String TEST_MAP = "maps/test_area.tmx";

    // --- karakter ---
    public static final int CHAR_FRAME_W = 16;
    public static final int CHAR_FRAME_H = 24;
    public static final float WALK_FRAME_DURATION = 0.14f;
    public static final float PLAYER_SPEED = 74f;         // pixel per detik
    public static final float HITBOX_W = 10f;
    public static final float HITBOX_H = 6f;
    public static final float INTERACT_REACH = 12f;

    // --- kamera ---
    public static final float CAMERA_LERP = 7f;

    // --- memori ---
    /** Jumlah fragmen memori yang harus dikumpulkan sebelum pilihan akhir (proposal hal. 3). */
    public static final int FRAGMENTS_NEEDED = 3;

    // --- dialog ---
    public static final float TYPE_SPEED = 48f;            // karakter per detik

    /** Delta waktu maksimum per frame, mencegah "teleport" saat game lag. */
    public static final float MAX_DELTA = 1f / 30f;
}
