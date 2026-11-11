// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Player.java
//  Bagian  : Core gameplay & world (karakter utama: Iwan)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W1 (struktur), W2 (movement + collision + animasi + probe interaksi)
//  OOP     : INHERITANCE (extends Entity), ENCAPSULATION (gerak hanya lewat move()),
//            target COMMAND pattern (MoveCommand / InteractCommand memanggil method di sini)
// =============================================================================
package com.kelompok7.lastremnant.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.util.Constants;
import com.kelompok7.lastremnant.world.CollisionSystem;

/**
 * Iwan, kurir di Depok Selatan.
 * <p>
 * Player TIDAK membaca keyboard sendiri. Input diubah menjadi Command oleh
 * InputHandler, lalu Command memanggil {@link #move(float, float)} /
 * {@link #requestInteract()}. Jadi kontrol bisa diganti tanpa mengubah kelas ini.
 */
public class Player extends Entity {

    private final CollisionSystem collision;
    private CharacterSprites sprites;           // boleh null (unit test tanpa grafis)

    private final Vector2 moveIntent = new Vector2();
    private final Rectangle probe = new Rectangle();
    private Direction facing = Direction.DOWN;
    private float speed = Constants.PLAYER_SPEED;
    private boolean moving;
    private boolean interactRequested;
    private float stateTime;

    public Player(float x, float y, CollisionSystem collision) {
        super(x, y, Constants.HITBOX_W, Constants.HITBOX_H);
        this.collision = collision;
    }

    public void setSprites(CharacterSprites sprites) {
        this.sprites = sprites;
    }

    // ------------------------------------------------------------------ Command API

    /** Tambah niat gerak frame ini. Beberapa tombol dijumlahkan (mis. W + D = diagonal). */
    public void move(float dirX, float dirY) {
        moveIntent.add(dirX, dirY);
    }

    /** Minta interaksi; diproses oleh ExplorationState pada frame yang sama. */
    public void requestInteract() {
        interactRequested = true;
    }

    /** Ambil & reset permintaan interaksi (dipakai sekali per tekan tombol). */
    public boolean consumeInteractRequest() {
        boolean r = interactRequested;
        interactRequested = false;
        return r;
    }

    /** Hentikan gerak (mis. saat dialog dibuka). */
    public void stop() {
        moveIntent.setZero();
        moving = false;
        interactRequested = false;
    }

    // ------------------------------------------------------------------ Entity

    @Override
    public void update(float delta) {
        if (moveIntent.isZero()) {
            moving = false;
            stateTime = 0f;
            return;
        }
        moveIntent.nor();   // kecepatan diagonal = kecepatan lurus
        facing = Direction.fromVector(moveIntent.x, moveIntent.y, facing);

        float startX = hitbox.x;
        float startY = hitbox.y;
        if (collision != null) {
            collision.move(hitbox, moveIntent.x * speed * delta, moveIntent.y * speed * delta, this);
        } else {
            hitbox.x += moveIntent.x * speed * delta;
            hitbox.y += moveIntent.y * speed * delta;
        }
        syncPositionFromHitbox();

        // animasi jalan hanya jika benar-benar berpindah (mentok tembok = diam di tempat)
        moving = Math.abs(hitbox.x - startX) > 0.001f || Math.abs(hitbox.y - startY) > 0.001f;
        stateTime = moving ? stateTime + delta : 0f;
        moveIntent.setZero();
    }

    @Override
    public void render(SpriteBatch batch) {
        if (sprites == null) return;
        float x = Math.round(position.x);
        float y = Math.round(position.y);
        TextureRegion shadow = sprites.getShadow();
        if (shadow != null) {
            batch.draw(shadow, x - shadow.getRegionWidth() / 2f, y - 1);
        }
        TextureRegion frame = sprites.frame(facing, moving, stateTime);
        batch.draw(frame, x - frame.getRegionWidth() / 2f, y - 1);
    }

    /**
     * Kotak kecil tepat di depan Iwan sesuai arah hadap. Interactable yang
     * bersinggungan dengan kotak ini bisa diajak bicara / diperiksa.
     */
    public Rectangle getInteractionProbe() {
        float reach = Constants.INTERACT_REACH;
        switch (facing) {
            case UP:
                probe.set(position.x - 6f, hitbox.y + hitbox.height, 12f, reach + 4f);
                break;
            case DOWN:
                probe.set(position.x - 6f, hitbox.y - reach, 12f, reach);
                break;
            case LEFT:
                probe.set(hitbox.x - reach, hitbox.y - 2f, reach, 14f);
                break;
            default:
                probe.set(hitbox.x + hitbox.width, hitbox.y - 2f, reach, 14f);
                break;
        }
        return probe;
    }

    public Direction getFacing() {
        return facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
    }

    public boolean isMoving() {
        return moving;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        if (speed < 0) throw new IllegalArgumentException("speed tidak boleh negatif");
        this.speed = speed;
    }
}
