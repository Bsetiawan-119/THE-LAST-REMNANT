// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : ReloadCommand.java
//  Bagian  : Core gameplay & world (input combat)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  Pattern : COMMAND (concrete command)
// =============================================================================
package com.kelompok7.lastremnant.input;

import com.kelompok7.lastremnant.entity.Player;

/** Isi ulang senjata berpeluru. Tidak berpengaruh saat memegang pisau. */
public final class ReloadCommand implements Command {

    public static final ReloadCommand INSTANCE = new ReloadCommand();

    private ReloadCommand() {
    }

    @Override
    public void execute(Player player) {
        player.requestReload();
    }

    @Override
    public String toString() {
        return "ReloadCommand";
    }
}
