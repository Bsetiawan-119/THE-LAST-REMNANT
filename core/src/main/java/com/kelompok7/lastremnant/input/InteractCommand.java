// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : InteractCommand.java
//  Bagian  : Core gameplay & world (input)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W2
//  Pattern : COMMAND (concrete command)
// =============================================================================
package com.kelompok7.lastremnant.input;

import com.kelompok7.lastremnant.entity.Player;

/** Bicara / periksa / pindai benda di depan Iwan. Dieksekusi sekali per tekan. */
public final class InteractCommand implements Command {

    public static final InteractCommand INSTANCE = new InteractCommand();

    private InteractCommand() {
    }

    @Override
    public void execute(Player player) {
        player.requestInteract();
    }

    @Override
    public String toString() {
        return "InteractCommand";
    }
}
