package com.kelompok7.lastremnant.combat;

public final class Flashlight extends Weapon {
    public Flashlight() { super("Senter", 0.45f); }
    @Override protected Strike fire() { return new Strike(95, 28, 0, 1.1f, false); }
}
