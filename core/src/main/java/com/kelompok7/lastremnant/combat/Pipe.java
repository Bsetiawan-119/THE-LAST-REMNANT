package com.kelompok7.lastremnant.combat;

public final class Pipe extends Weapon {
    public Pipe() { super("Pipa besi", 0.70f); }
    @Override protected Strike fire() { return new Strike(31, 66, 38, 0.4f, false); }
}
