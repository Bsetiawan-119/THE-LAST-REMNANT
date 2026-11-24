// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : PickupCommand.java
//  Bagian  : Core gameplay & world (input combat)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  Pattern : COMMAND (concrete command)
// =============================================================================
package com.kelompok7.lastremnant.input;

import com.kelompok7.lastremnant.entity.Player;

/** Ambil senjata di lantai dekat kaki Zentra. */
public final class PickupCommand implements Command {

    public static final PickupCommand INSTANCE = new PickupCommand();

    private PickupCommand() {
    }

    @Override
    public void execute(Player player) {
        player.requestPickup();
    }

    @Override
    public String toString() {
        return "PickupCommand";
    }
}
