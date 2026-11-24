// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : DropCommand.java
//  Bagian  : Core gameplay & world (input combat)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  Pattern : COMMAND (concrete command)
// =============================================================================
package com.kelompok7.lastremnant.input;

import com.kelompok7.lastremnant.entity.Player;

/** Buang senjata di tangan ke lantai (pisau tidak bisa dibuang). */
public final class DropCommand implements Command {

    public static final DropCommand INSTANCE = new DropCommand();

    private DropCommand() {
    }

    @Override
    public void execute(Player player) {
        player.requestDrop();
    }

    @Override
    public String toString() {
        return "DropCommand";
    }
}
