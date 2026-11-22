package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.entity.Entity;
import com.kelompok7.lastremnant.entity.Interactable;
import com.kelompok7.lastremnant.entity.InteractionContext;

/** Simpan objek senjata yang sama saat di lantai, termasuk sisa pelurunya. */
public final class WeaponPickup extends Entity implements Interactable {
    private final Inventory inventory;
    private final Weapon weapon;
    private final TextureRegion icon;
    private final Rectangle reach = new Rectangle();
    private final Runnable onFull;
    private boolean taken;

    public WeaponPickup(float x, float y, Inventory inventory, Weapon weapon,
                        TextureRegion icon, Runnable onFull) {
        super(x, y, 7, 4);
        this.inventory = inventory;
        this.weapon = weapon;
        this.icon = icon;
        this.onFull = onFull;
    }

    @Override public void update(float delta) { }
    @Override public boolean canInteract() { return !taken; }
    @Override public Rectangle getInteractBounds() { return reach.set(getX() - 6, getY() - 3, 12, 12); }
    @Override public String getInteractLabel(InteractionContext context) { return "Ambil " + weapon.name(); }
    @Override public Vector2 getIndicatorPosition(Vector2 out) { return out.set(getX(), getY() + 9); }

    @Override public void interact(InteractionContext context) {
        if (taken) return;
        taken = inventory.pickUp(weapon);
        if (!taken) onFull.run();
    }

    @Override public void render(SpriteBatch batch) {
        if (!taken) batch.draw(icon, getX() - 4, getY(), 8, 5);
    }
}
