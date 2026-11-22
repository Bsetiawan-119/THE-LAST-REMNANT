package com.kelompok7.lastremnant.combat;

public final class Pistol extends Weapon {
    private final Ammo ammo;
    public Pistol(int rounds) { super("Pistol", 0.32f); ammo = new Ammo(6, rounds); }
    @Override public Ammo ammo() { return ammo; }
    @Override protected Strike fire() { return ammo.spend() ? new Strike(152, 15, 35, 0, true) : null; }
}
