// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : MemoryObject.java
//  Bagian  : Puzzle, memory, quest (benda yang menyimpan jejak memori)
//  PJ      : Azzam Insan Kamil
//  Minggu  : W2
//  OOP     : INHERITANCE (extends Entity), POLYMORPHISM (Interactable)
// =============================================================================
package com.kelompok7.lastremnant.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.dialogue.Dialogue;
import com.kelompok7.lastremnant.memory.JournalEntry;
import com.kelompok7.lastremnant.memory.MemoryDevice;
import com.kelompok7.lastremnant.memory.MemoryEcho;

/**
 * Benda biasa (radio, foto, layangan) yang menyimpan gema memori.
 * <ul>
 *   <li>Tanpa The Remnant  : Iwan hanya bisa memeriksa (deskripsi singkat).</li>
 *   <li>Dengan The Remnant : benda berpendar; dipindai -> kilas balik + entri jurnal.</li>
 * </ul>
 */
public class MemoryObject extends Entity implements Interactable {

    private static final int MAX_MOTES = 6;

    private final MemoryEcho echo;
    private final TextureRegion sprite;
    private final TextureRegion glow;
    private final TextureRegion pixel;
    private final MemoryDevice device;
    private final Rectangle interactBounds = new Rectangle();
    private final float[] moteX = new float[MAX_MOTES];
    private final float[] moteLife = new float[MAX_MOTES];
    private float time;

    public MemoryObject(MemoryEcho echo, MemoryDevice device, TextureRegion sprite,
                        TextureRegion glow, TextureRegion pixel, float x, float y) {
        super(x, y, 12f, 6f);
        this.echo = echo;
        this.device = device;
        this.sprite = sprite;
        this.glow = glow;
        this.pixel = pixel;
        for (int i = 0; i < MAX_MOTES; i++) moteLife[i] = MathUtils.random(1f);
    }

    /** Berpendar hanya bila alat aktif dan gema belum dibaca. */
    public boolean isResonating() {
        return device != null && device.isActive() && !device.hasScanned(echo.getId());
    }

    @Override
    public void update(float delta) {
        time += delta;
        if (!isResonating()) return;
        for (int i = 0; i < MAX_MOTES; i++) {
            moteLife[i] += delta * 0.6f;
            if (moteLife[i] >= 1f) {
                moteLife[i] = 0f;
                moteX[i] = MathUtils.random(-6f, 6f);
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        float x = Math.round(position.x - 8f);
        float y = Math.round(position.y);
        if (isResonating() && glow != null) {
            float pulse = 0.35f + 0.25f * MathUtils.sin(time * 3f);
            batch.setColor(0.45f, 1f, 0.95f, pulse);
            batch.draw(glow, x - 8f, y - 8f);
            // butiran cahaya naik pelan: visual "memori yang ingin diingat"
            if (pixel != null) {
                for (int i = 0; i < MAX_MOTES; i++) {
                    float a = 1f - moteLife[i];
                    batch.setColor(0.7f, 1f, 0.98f, a * 0.9f);
                    batch.draw(pixel, Math.round(position.x + moteX[i]), Math.round(y + 4f + moteLife[i] * 18f), 1f, 1f);
                }
            }
            batch.setColor(1f, 1f, 1f, 1f);
        }
        if (sprite != null) {
            float bob = isResonating() ? Math.round(MathUtils.sin(time * 2.2f)) : 0f;
            batch.draw(sprite, x, y + bob);
        }
    }

    // ------------------------------------------------------------------ Interactable

    @Override
    public Rectangle getInteractBounds() {
        return interactBounds.set(position.x - 9f, position.y - 3f, 18f, 19f);
    }

    @Override
    public String getInteractLabel(InteractionContext ctx) {
        MemoryDevice d = ctx.getMemoryDevice();
        if (!d.isActive()) return "Periksa " + echo.getObjectName().toLowerCase();
        if (d.hasScanned(echo.getId())) return "Lihat " + echo.getObjectName().toLowerCase();
        return "Pindai " + echo.getObjectName().toLowerCase() + " dengan The Remnant";
    }

    @Override
    public Vector2 getIndicatorPosition(Vector2 out) {
        return out.set(position.x, position.y + 19f);
    }

    @Override
    public void interact(InteractionContext ctx) {
        MemoryDevice d = ctx.getMemoryDevice();
        if (!d.isActive()) {
            ctx.startDialogue(echo.getInspectDialogueId(), null);
            return;
        }
        if (d.hasScanned(echo.getId())) {
            ctx.startDialogue(Dialogue.linear("rescan_" + echo.getId(), "remnant",
                    "JEJAK SUDAH TERSIMPAN. BUKA JURNAL UNTUK MEMBACA ULANG."), null);
            return;
        }
        JournalEntry entry = d.scan(echo);
        ctx.getStory().setFlag("scanned_" + echo.getId());
        ctx.onEchoScanned(echo, entry);
        ctx.startDialogue(echo.getEchoDialogueId(), null);
    }

    public MemoryEcho getEcho() {
        return echo;
    }
}
