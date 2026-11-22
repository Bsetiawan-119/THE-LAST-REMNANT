package com.kelompok7.lastremnant.combat;

public final class Knife extends Weapon {
    public Knife() { super("Pisau", 0.30f); }
    @Override protected Strike fire() { return new Strike(24, 62, 24, 0, false); }
}
