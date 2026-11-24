// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : TargetDummy.java
//  Bagian  : Combat (sasaran latihan sementara)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  OOP     : INHERITANCE (extends Entity), POLYMORPHISM (Damageable)
//  Catatan : Pengganti sementara Boneka sampai Enemy + Health (Insan) siap.
//            Enemy cukup implements Damageable; WeaponSystem tidak perlu diubah.
// =============================================================================
package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.kelompok7.lastremnant.entity.Entity;

/** Boneka diam yang bisa dipukul/ditembak. Dipakai untuk uji senjata di peta. */
public class TargetDummy extends Entity implements Damageable {

    private static final float FLASH_TIME = 0.12f;

    private final TextureRegion sprite;
    private final TextureRegion brokenSprite;
    private final Rectangle hurtbox = new Rectangle();
    private final int maxHp;
    private int hp;
    private float flash;
    private int hitCount;

    public TargetDummy(float x, float y, int maxHp, TextureRegion sprite, TextureRegion brokenSprite) {
        super(x, y, 10f, 6f);
        if (maxHp <= 0) throw new IllegalArgumentException("hp harus > 0");
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.sprite = sprite;
        this.brokenSprite = brokenSprite;
    }

    @Override
    public void update(float delta) {
        if (flash > 0f) flash -= delta;
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion r = isAlive() ? sprite : brokenSprite;
        if (r == null) return;
        if (flash > 0f) batch.setColor(1f, 0.45f, 0.45f, 1f);
        batch.draw(r, Math.round(position.x - r.getRegionWidth() / 2f), Math.round(position.y - 1));
        batch.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public boolean isSolid() {
        return isAlive();
    }

    // ------------------------------------------------------------------ Damageable

    @Override
    public Rectangle getHurtbox() {
        return hurtbox.set(position.x - 6f, position.y, 12f, 20f);
    }

    @Override
    public boolean isAlive() {
        return hp > 0;
    }

    @Override
    public void takeDamage(int amount, Attack source) {
        if (!isAlive() || amount <= 0) return;
        hp = Math.max(0, hp - amount);
        flash = FLASH_TIME;
        hitCount++;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getHitCount() {
        return hitCount;
    }
}
