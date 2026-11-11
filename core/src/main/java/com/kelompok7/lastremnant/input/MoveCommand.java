// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : MoveCommand.java
//  Bagian  : Core gameplay & world (input)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W2
//  Pattern : COMMAND (concrete command)
// =============================================================================
package com.kelompok7.lastremnant.input;

import com.kelompok7.lastremnant.entity.Player;

/** Gerak satu arah. Dieksekusi setiap frame selama tombolnya ditahan. */
public final class MoveCommand implements Command {

    public static final MoveCommand UP = new MoveCommand(0, 1);
    public static final MoveCommand DOWN = new MoveCommand(0, -1);
    public static final MoveCommand LEFT = new MoveCommand(-1, 0);
    public static final MoveCommand RIGHT = new MoveCommand(1, 0);

    private final float dx;
    private final float dy;

    public MoveCommand(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void execute(Player player) {
        player.move(dx, dy);
    }

    @Override
    public String toString() {
        return "MoveCommand(" + dx + ", " + dy + ")";
    }
}
