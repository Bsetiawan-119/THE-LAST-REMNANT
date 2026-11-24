// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : MeleeWeapon.java
//  Bagian  : Combat (senjata jarak dekat)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  OOP     : INHERITANCE (extends Weapon), POLYMORPHISM (createAttack)
// =============================================================================
package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.entity.Direction;

/** Senjata yang mengenai area kecil tepat di depan pemakainya. */
public abstract class MeleeWeapon extends Weapon {

    private final float reach;
    private final float width;
    private final Rectangle tmp = new Rectangle();

    protected MeleeWeapon(String id, String displayName, String spriteKey,
                          int damage, float cooldown, float reach, float width) {
        super(id, displayName, spriteKey, damage, cooldown);
        if (reach <= 0 || width <= 0) throw new IllegalArgumentException("jangkauan harus > 0");
        this.reach = reach;
        this.width = width;
    }

    @Override
    protected Attack createAttack(Vector2 origin, Direction facing) {
        return Attack.melee(getId(), getDamage(), facing, hitArea(origin, facing, tmp));
    }

    /**
     * Kotak serangan di depan kaki pemain. Titik y dinaikkan sedikit supaya
     * sabetan ke kiri/kanan setinggi badan, bukan setinggi kaki.
     */
    public Rectangle hitArea(Vector2 origin, Direction facing, Rectangle out) {
        float bodyY = origin.y + 4f;
        switch (facing) {
            case UP:
                return out.set(origin.x - width / 2f, origin.y + 6f, width, reach);
            case DOWN:
                return out.set(origin.x - width / 2f, origin.y - reach, width, reach);
            case LEFT:
                return out.set(origin.x - 5f - reach, bodyY - width / 2f, reach, width);
            default:
                return out.set(origin.x + 5f, bodyY - width / 2f, reach, width);
        }
    }

    public float getReach() {
        return reach;
    }
}
