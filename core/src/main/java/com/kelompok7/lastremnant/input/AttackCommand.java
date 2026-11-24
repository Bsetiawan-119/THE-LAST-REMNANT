// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : AttackCommand.java
//  Bagian  : Core gameplay & world (input combat)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  Pattern : COMMAND (concrete command)
// =============================================================================
package com.kelompok7.lastremnant.input;

import com.kelompok7.lastremnant.entity.Player;

/** Serang dengan senjata di tangan (pisau = sabet, pistol = tembak). Sekali per tekan. */
public final class AttackCommand implements Command {

    public static final AttackCommand INSTANCE = new AttackCommand();

    private AttackCommand() {
    }

    @Override
    public void execute(Player player) {
        player.requestAttack();
    }

    @Override
    public String toString() {
        return "AttackCommand";
    }
}
