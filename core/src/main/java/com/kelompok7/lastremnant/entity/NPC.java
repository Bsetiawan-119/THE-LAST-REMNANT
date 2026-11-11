// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : NPC.java
//  Bagian  : Dialogue & story (warga Depok Selatan yang bisa diajak bicara)
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W2 = perilaku dasar + pemilihan dialog sesuai flag
//            W3 = konten NPC tambahan (cukup tambah data di characters.json & peta)
//  OOP     : INHERITANCE (extends Entity), POLYMORPHISM (Interactable)
// =============================================================================
package com.kelompok7.lastremnant.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.story.CharacterInfo;
import com.kelompok7.lastremnant.util.Constants;

/**
 * NPC berdiri di tempat, sesekali menoleh, dan menghadap Iwan saat diajak bicara.
 * Dialog yang dipakai dipilih dari aturan di {@link CharacterInfo} sesuai flag cerita,
 * jadi isi percakapan berubah seiring progres tanpa if-else di kode.
 */
public class NPC extends Entity implements Interactable {

    private static final Direction[] IDLE_LOOKS = {Direction.DOWN, Direction.LEFT, Direction.RIGHT, Direction.DOWN};

    private final CharacterInfo info;
    private final CharacterSprites sprites;
    private final Direction homeFacing;
    private final Rectangle interactBounds = new Rectangle();
    private Direction facing;
    private boolean talking;
    private float lookTimer;

    public NPC(CharacterInfo info, CharacterSprites sprites, float x, float y, Direction facing) {
        super(x, y, Constants.HITBOX_W, Constants.HITBOX_H);
        this.info = info;
        this.sprites = sprites;
        this.facing = facing;
        this.homeFacing = facing;
        this.lookTimer = MathUtils.random(2f, 5f);
    }

    @Override
    public void update(float delta) {
        if (talking) return;
        lookTimer -= delta;
        if (lookTimer <= 0f) {
            // menoleh sebentar lalu kembali ke arah semula: terasa hidup tanpa AI rumit
            facing = facing == homeFacing ? IDLE_LOOKS[MathUtils.random(IDLE_LOOKS.length - 1)] : homeFacing;
            lookTimer = facing == homeFacing ? MathUtils.random(3f, 6f) : MathUtils.random(0.8f, 1.6f);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (sprites == null) return;
        float x = Math.round(position.x);
        float y = Math.round(position.y);
        TextureRegion shadow = sprites.getShadow();
        if (shadow != null) batch.draw(shadow, x - shadow.getRegionWidth() / 2f, y - 1);
        TextureRegion frame = sprites.frame(facing, false, 0f);
        batch.draw(frame, x - frame.getRegionWidth() / 2f, y - 1);
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    // ------------------------------------------------------------------ Interactable

    @Override
    public Rectangle getInteractBounds() {
        return interactBounds.set(position.x - 8f, position.y - 4f, 16f, Constants.CHAR_FRAME_H + 2f);
    }

    @Override
    public String getInteractLabel(InteractionContext ctx) {
        return "Bicara dengan " + info.getName();
    }

    @Override
    public Vector2 getIndicatorPosition(Vector2 out) {
        return out.set(position.x, position.y + Constants.CHAR_FRAME_H + 2f);
    }

    @Override
    public void interact(InteractionContext ctx) {
        String dialogueId = info.pickDialogue(ctx.getStory());
        if (dialogueId == null) return;
        faceTowards(ctx.getPlayer().getPosition());
        ctx.getPlayer().setFacing(opposite(facing));
        talking = true;
        ctx.startDialogue(dialogueId, () -> {
            talking = false;
            lookTimer = MathUtils.random(2f, 4f);
        });
    }

    // ------------------------------------------------------------------ helper

    public void faceTowards(Vector2 target) {
        facing = Direction.fromVector(target.x - position.x, target.y - position.y, facing);
    }

    private static Direction opposite(Direction d) {
        switch (d) {
            case UP: return Direction.DOWN;
            case DOWN: return Direction.UP;
            case LEFT: return Direction.RIGHT;
            default: return Direction.LEFT;
        }
    }

    public CharacterInfo getInfo() {
        return info;
    }

    public boolean isTalking() {
        return talking;
    }

    public Direction getFacing() {
        return facing;
    }
}
