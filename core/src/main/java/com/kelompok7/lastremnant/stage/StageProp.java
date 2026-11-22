package com.kelompok7.lastremnant.stage;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.entity.Entity;
import com.kelompok7.lastremnant.entity.Interactable;
import com.kelompok7.lastremnant.entity.InteractionContext;

import java.util.function.BooleanSupplier;

final class StageProp extends Entity implements Interactable {
    private final TextureRegion image;
    private final String label;
    private final BooleanSupplier available;
    private final Runnable action;
    private final Rectangle reach;

    StageProp(float x, float y, TextureRegion image, String label, BooleanSupplier available, Runnable action) {
        super(x, y, 8, 8);
        this.image = image;
        this.label = label;
        this.available = available;
        this.action = action;
        reach = new Rectangle(x - 9, y - 4, 18, 18);
    }

    @Override public void update(float delta) { }
    @Override public boolean canInteract() { return available.getAsBoolean(); }
    @Override public Rectangle getInteractBounds() { return reach; }
    @Override public String getInteractLabel(InteractionContext context) { return label; }
    @Override public Vector2 getIndicatorPosition(Vector2 out) { return out.set(getX(), getY() + 15); }
    @Override public void interact(InteractionContext context) { if (canInteract()) action.run(); }

    @Override public void render(SpriteBatch batch) {
        if (image != null) batch.draw(image, getX() - 7, getY(), 14, 12);
    }
}
