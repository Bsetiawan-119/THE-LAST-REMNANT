// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : GameState.java
//  Bagian  : UI & game flow (kontrak satu kondisi permainan)
//  PJ      : Budiman Setiawan
//  Minggu  : W2
//  Pattern : STATE  ->  Menu -> Eksplorasi -> Dialog -> Puzzle -> Ending  (proposal hal. 8)
// =============================================================================
package com.kelompok7.lastremnant.state;

/**
 * Satu kondisi permainan. GameScreen tidak punya rantai if-else
 * ("kalau lagi dialog... kalau lagi jurnal..."); ia hanya memanggil state aktif.
 */
public interface GameState {

    /** Dipanggil saat state mulai aktif (di-push atau di-set). */
    void enter();

    /** Dipanggil saat state dibuang dari tumpukan. */
    void exit();

    /** State lain di-push di atas state ini (mis. jurnal dibuka saat eksplorasi). */
    default void pause() {
    }

    /** State di atasnya sudah di-pop, state ini aktif lagi. */
    default void resume() {
    }

    void update(float delta);

    /** @return true bila tombol dipakai state ini (tidak diteruskan ke InputHandler). */
    default boolean keyDown(int keycode) {
        return false;
    }

    /** Nama untuk debug / log. */
    default String getName() {
        return getClass().getSimpleName();
    }
}
