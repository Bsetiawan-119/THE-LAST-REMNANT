// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Interactable.java
//  Bagian  : Integrasi (interface antarmodul, disepakati di W1)
//  PJ      : Tim  -  diimplementasi oleh NPC (Carlos) dan MemoryObject (Insan)
//  Minggu  : W1 (kontrak), W2 (dipakai)
//  OOP     : ABSTRACTION + POLYMORPHISM (ExplorationState tidak peduli jenis bendanya)
// =============================================================================
package com.kelompok7.lastremnant.entity;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/** Sesuatu yang bisa diajak bicara / diperiksa dengan tombol interaksi. */
public interface Interactable {

    /** Area yang harus disentuh probe Player agar interaksi muncul. */
    Rectangle getInteractBounds();

    /** Teks petunjuk di HUD, mis. "Bicara dengan Bu Sari". */
    String getInteractLabel(InteractionContext ctx);

    /** Titik di atas kepala/benda untuk ikon panah penanda. */
    Vector2 getIndicatorPosition(Vector2 out);

    void interact(InteractionContext ctx);

    default boolean canInteract() {
        return true;
    }
}
