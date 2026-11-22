package com.kelompok7.lastremnant.combat;

/** Kontrak serangan. Dunia menentukan sasaran dan memeriksa tabrakan. */
public abstract class Weapon {
    public record Strike(float range, float halfAngle, int damage, float stunSeconds, boolean shot) { }

    private final String name;
    private final float interval;
    private float cooldown;

    protected Weapon(String name, float interval) {
        this.name = name;
        this.interval = interval;
    }

    public final Strike attack() {
        if (cooldown > 0) return null;
        Strike strike = fire();
        if (strike != null) cooldown = interval;
        return strike;
    }

    protected abstract Strike fire();

    public final void update(float delta) { cooldown = Math.max(0, cooldown - delta); }
    public final String name() { return name; }
    public Ammo ammo() { return null; }

    public static Weapon fromId(String id, int rounds) {
        return switch (id) {
            case "knife" -> new Knife();
            case "pistol" -> new Pistol(rounds);
            case "pipe" -> new Pipe();
            case "shotgun" -> new Shotgun(rounds);
            case "flashlight" -> new Flashlight();
            default -> throw new IllegalArgumentException("Senjata tidak dikenal: " + id);
        };
    }
}
