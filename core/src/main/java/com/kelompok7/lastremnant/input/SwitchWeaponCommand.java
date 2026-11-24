// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : SwitchWeaponCommand.java
//  Bagian  : Core gameplay & world (input combat)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  Pattern : COMMAND (concrete command)
// =============================================================================
package com.kelompok7.lastremnant.input;

import com.kelompok7.lastremnant.entity.Player;

/**
 * Ganti senjata: ke slot tertentu (tombol 1-3) atau ke slot terisi berikutnya (Q).
 * Satu kelas dengan dua mode, dibuat lewat factory method supaya maksudnya jelas.
 */
public final class SwitchWeaponCommand implements Command {

    private static final int NEXT = -1;

    public static final SwitchWeaponCommand NEXT_WEAPON = new SwitchWeaponCommand(NEXT);

    private final int slot;

    private SwitchWeaponCommand(int slot) {
        this.slot = slot;
    }

    /** @param slot 0 = pisau, 1..n = slot senjata pungutan */
    public static SwitchWeaponCommand toSlot(int slot) {
        if (slot < 0) throw new IllegalArgumentException("slot tidak boleh negatif");
        return new SwitchWeaponCommand(slot);
    }

    @Override
    public void execute(Player player) {
        if (slot == NEXT) {
            player.requestNextWeapon();
        } else {
            player.requestSelectSlot(slot);
        }
    }

    @Override
    public String toString() {
        return slot == NEXT ? "SwitchWeaponCommand(next)" : "SwitchWeaponCommand(" + slot + ")";
    }
}
