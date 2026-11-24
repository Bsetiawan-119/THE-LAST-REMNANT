// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Projectile.java
//  Bagian  : Combat (peluru yang terbang)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  OOP     : INHERITANCE (extends Entity), POLYMORPHISM (update/render seperti entity lain)
// =============================================================================
package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kelompok7.lastremnant.entity.Entity;
import com.kelompok7.lastremnant.world.CollisionSystem;

/**
 * Peluru lurus ke salah satu dari 4 arah. Hilang saat menabrak tembok,
 * mengenai target, atau melewati jarak maksimum.
 * Hitbox = seluruh peluru (bukan hanya "kaki" seperti karakter).
 */
public class Projectile extends Entity {

    private final Attack attack;
    private final CollisionSystem collision;
    private final TextureRegion sprite;
    private final float vx;
    private final float vy;
    private float traveled;
    private boolean alive = true;

    public Projectile(Attack attack, CollisionSystem collision, TextureRegion sprite) {
        super(0f, 0f, 1f, 1f);
        if (attack.getKind() != Attack.Kind.PROJECTILE) throw new IllegalArgumentException("bukan serangan proyektil");
        this.attack = attack;
        this.collision = collision;
        this.sprite = sprite;
        attack.getArea(hitbox);
        position.set(hitbox.x + hitbox.width / 2f, hitbox.y);
        vx = attack.getDirection().dx * attack.getSpeed();
        vy = attack.getDirection().dy * attack.getSpeed();
    }

    @Override
    public void update(float delta) {
        advance(delta, null);
    }

    /**
     * Gerakkan peluru dalam langkah kecil (tidak menembus tembok tipis saat lag).
     * Di tiap langkah target dicek DULU, baru tembok, supaya musuh yang solid
     * tetap terkena peluru (bukan dianggap tembok).
     *
     * @return target yang terkena, atau null
     */
    public Damageable advance(float delta, Array<Damageable> targets) {
        if (!alive) return null;
        float dx = vx * delta;
        float dy = vy * delta;
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / 4f));
        Damageable hit = null;
        for (int i = 0; i < steps && alive; i++) {
            hitbox.x += dx / steps;
            hitbox.y += dy / steps;
            traveled += (Math.abs(dx) + Math.abs(dy)) / steps;
            hit = firstHit(targets);
            if (hit != null || (collision != null && collision.isBlocked(hitbox, null))
                    || traveled >= attack.getRange()) {
                alive = false;
            }
        }
        position.set(hitbox.x + hitbox.width / 2f, hitbox.y);
        return hit;
    }

    private Damageable firstHit(Array<Damageable> targets) {
        if (targets == null) return null;
        for (int i = 0; i < targets.size; i++) {
            Damageable t = targets.get(i);
            if (t.isAlive() && t.getHurtbox().overlaps(hitbox)) return t;
        }
        return null;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!alive || sprite == null) return;
        batch.draw(sprite, Math.round(hitbox.x + hitbox.width / 2f - sprite.getRegionWidth() / 2f),
                Math.round(hitbox.y + hitbox.height / 2f - sprite.getRegionHeight() / 2f));
    }

    /** Dipanggil WeaponSystem setelah peluru mengenai target. */
    public void kill() {
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }

    public Attack getAttack() {
        return attack;
    }

    public Rectangle getBounds() {
        return hitbox;
    }

    public float getTraveled() {
        return traveled;
    }
}
