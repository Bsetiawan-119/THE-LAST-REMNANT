package com.kelompok7.lastremnant.stage;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.entity.Entity;
import com.kelompok7.lastremnant.entity.Interactable;
import com.kelompok7.lastremnant.entity.InteractionContext;

/** Adegan lemari: beri waktu untuk menekan spasi, lalu cek tahan 2 detik tanpa putus. */
final class HidingSpot extends Entity implements Interactable {
    private final TextureRegion image;
    private final Runnable onBegin;
    private final Rectangle reach;
    private boolean hiding;
    private boolean cleared;
    private boolean startedHolding;
    private float time;
    private float held;

    HidingSpot(float x, float y, TextureRegion image, Runnable onBegin) {
        super(x, y, 14, 18);
        this.image = image;
        this.onBegin = onBegin;
        reach = new Rectangle(x - 9, y - 4, 18, 20);
    }

    enum Outcome { WAITING, CAUGHT, SAFE }

    Outcome step(float delta, boolean spaceHeld) {
        if (!hiding) return Outcome.WAITING;
        time += delta;
        if (spaceHeld) {
            startedHolding = true;
            held += delta;
            if (held >= 2f) {
                hiding = false;
                cleared = true;
                return Outcome.SAFE;
            }
        } else if (startedHolding || time >= 1.5f) {
            hiding = false;
            return Outcome.CAUGHT;
        }
        return Outcome.WAITING;
    }

    @Override public void interact(InteractionContext context) {
        if (!canInteract()) return;
        hiding = true;
        startedHolding = false;
        time = 0;
        held = 0;
        onBegin.run();
    }

    boolean hiding() { return hiding; }
    boolean cleared() { return cleared; }
    @Override public boolean canInteract() { return !hiding && !cleared; }
    @Override public void update(float delta) { }
    @Override public Rectangle getInteractBounds() { return reach; }
    @Override public String getInteractLabel(InteractionContext context) { return "Bersembunyi di lemari"; }
    @Override public Vector2 getIndicatorPosition(Vector2 out) { return out.set(getX(), getY() + 18); }
    @Override public void render(SpriteBatch batch) { batch.draw(image, getX() - 7, getY(), 14, 18); }
}
