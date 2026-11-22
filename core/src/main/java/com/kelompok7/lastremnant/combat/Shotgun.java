package com.kelompok7.lastremnant.combat;

public final class Shotgun extends Weapon {
    private final Ammo ammo;
    public Shotgun(int rounds) { super("Shotgun", 0.85f); ammo = new Ammo(2, rounds); }
    @Override public Ammo ammo() { return ammo; }
    @Override protected Strike fire() { return ammo.spend() ? new Strike(90, 34, 50, 0.35f, true) : null; }
}
