// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Attack.java
//  Bagian  : Combat (data satu serangan)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  Kontrak : dibaca oleh WeaponSystem (Iqbal) dan Enemy/Health (Insan) lewat Damageable
// =============================================================================
package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.math.Rectangle;
import com.kelompok7.lastremnant.entity.Direction;

/**
 * Hasil satu kali menyerang. Senjata tidak tahu siapa yang kena; ia hanya
 * menjelaskan serangannya. WeaponSystem yang mencocokkan dengan target.
 * <ul>
 *   <li>{@link Kind#MELEE}      : area langsung di depan pemain, kena sekali di frame itu.</li>
 *   <li>{@link Kind#PROJECTILE} : peluru yang bergerak dari {@code area} ke arah {@code direction}.</li>
 * </ul>
 */
public final class Attack {

    public enum Kind { MELEE, PROJECTILE }

    private final Kind kind;
    private final String weaponId;
    private final int damage;
    private final Direction direction;
    private final Rectangle area;
    private final float speed;
    private final float range;

    private Attack(Kind kind, String weaponId, int damage, Direction direction,
                   Rectangle area, float speed, float range) {
        this.kind = kind;
        this.weaponId = weaponId;
        this.damage = damage;
        this.direction = direction;
        this.area = area;
        this.speed = speed;
        this.range = range;
    }

    public static Attack melee(String weaponId, int damage, Direction direction, Rectangle area) {
        return new Attack(Kind.MELEE, weaponId, damage, direction, new Rectangle(area), 0f, 0f);
    }

    public static Attack projectile(String weaponId, int damage, Direction direction,
                                    Rectangle start, float speed, float range) {
        return new Attack(Kind.PROJECTILE, weaponId, damage, direction, new Rectangle(start), speed, range);
    }

    public Kind getKind() {
        return kind;
    }

    public String getWeaponId() {
        return weaponId;
    }

    public int getDamage() {
        return damage;
    }

    public Direction getDirection() {
        return direction;
    }

    /** Salinan area supaya data serangan tidak bisa diubah dari luar. */
    public Rectangle getArea(Rectangle out) {
        return out.set(area);
    }

    public float getSpeed() {
        return speed;
    }

    public float getRange() {
        return range;
    }

    @Override
    public String toString() {
        return "Attack(" + kind + ", " + weaponId + ", dmg=" + damage + ", " + direction + ")";
    }
}
