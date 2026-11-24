// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : WeaponPickup.java
//  Bagian  : Combat (senjata yang tergeletak di lantai)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  OOP     : INHERITANCE (extends Entity), COMPOSITION (membungkus satu Weapon),
//            POLYMORPHISM (Interactable: bisa diambil dengan E seperti benda lain)
// =============================================================================
package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.entity.Entity;
import com.kelompok7.lastremnant.entity.Interactable;
import com.kelompok7.lastremnant.entity.InteractionContext;

/**
 * Senjata di lantai. Objek senjatanya tetap sama saat diambil lalu dibuang lagi,
 * jadi sisa peluru ikut tersimpan (tidak ada senjata/peluru yang "lahir" dua kali).
 */
public class WeaponPickup extends Entity implements Interactable {

    private final Weapon weapon;
    private final Rectangle interactBounds = new Rectangle();
    private WeaponSystem owner;
    private final TextureRegion sprite;
    private final TextureRegion shadow;
    private float time;

    public WeaponPickup(Weapon weapon, float x, float y, TextureRegion sprite, TextureRegion shadow) {
        super(x, y, 12f, 8f);
        if (weapon == null) throw new IllegalArgumentException("senjata null");
        this.weapon = weapon;
        this.sprite = sprite;
        this.shadow = shadow;
        time = MathUtils.random(2f);
    }

    @Override
    public void update(float delta) {
        time += delta;
    }

    @Override
    public void render(SpriteBatch batch) {
        float x = Math.round(position.x);
        float y = Math.round(position.y);
        if (shadow != null) batch.draw(shadow, x - shadow.getRegionWidth() / 2f, y - 1);
        if (sprite == null) return;
        float bob = Math.round(MathUtils.sin(time * 3f) * 1.2f);
        batch.draw(sprite, x - sprite.getRegionWidth() / 2f, y + 1 + bob);
    }

    /** Jarak (kuadrat) dari titik kaki pemain ke benda ini. */
    public float distance2(float px, float py) {
        float dx = position.x - px;
        float dy = position.y + 3f - py;
        return dx * dx + dy * dy;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    void setOwner(WeaponSystem owner) {
        this.owner = owner;
    }

    // ------------------------------------------------------------------ Interactable

    @Override
    public Rectangle getInteractBounds() {
        return interactBounds.set(position.x - 8f, position.y - 4f, 16f, 14f);
    }

    /** "Ambil pistol" / "Ambil peluru pistol" / "Tukar pipa dengan pistol". */
    @Override
    public String getInteractLabel(InteractionContext ctx) {
        return describeFor(ctx.getPlayer().getInventory());
    }

    public String describeFor(Inventory inv) {
        String name = weapon.getDisplayName().toLowerCase();
        if (inv.has(weapon.getId())) return "Ambil peluru " + name;
        if (inv.isFull()) {
            Weapon swap = inv.getActiveIndex() == Inventory.KNIFE_SLOT ? inv.getSlot(1) : inv.getActive();
            return "Tukar " + swap.getDisplayName().toLowerCase() + " dengan " + name;
        }
        return "Ambil " + name;
    }

    @Override
    public Vector2 getIndicatorPosition(Vector2 out) {
        return out.set(position.x, position.y + 14f);
    }

    @Override
    public void interact(InteractionContext ctx) {
        if (owner != null) owner.pickup(ctx.getPlayer(), this);
    }
}
