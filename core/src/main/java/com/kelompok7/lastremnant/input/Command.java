// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Command.java
//  Bagian  : Core gameplay & world (input)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W1 (interface antarmodul), W2 (implementasi)
//  Pattern : COMMAND  ->  Tombol -> Command -> Player   (proposal hal. 8)
// =============================================================================
package com.kelompok7.lastremnant.input;

import com.kelompok7.lastremnant.entity.Player;

/**
 * Satu aksi yang bisa dilakukan pemain. Tombol keyboard dipetakan ke objek Command,
 * sehingga mengganti kontrol cukup mengganti pemetaan di InputHandler.
 */
@FunctionalInterface
public interface Command {
    void execute(Player player);
}
