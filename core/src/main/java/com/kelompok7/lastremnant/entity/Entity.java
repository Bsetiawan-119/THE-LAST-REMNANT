// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Entity.java
//  Bagian  : Core gameplay & world (kelas induk semua objek di dunia)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W1 (kontrak), W2 (hitbox & depth sorting)
//  OOP     : ABSTRACTION + INHERITANCE  ->  Player, NPC, MemoryObject mewarisi kelas ini
//            POLYMORPHISM -> GameScreen memanggil update()/render() tanpa cek jenis
// =============================================================================
package com.kelompok7.lastremnant.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Semua benda yang hidup di dunia game.
 * <p>
 * Konvensi posisi: {@code position} adalah titik <b>tengah-bawah</b> (kaki) entity.
 * Dengan begitu urutan gambar cukup diurutkan berdasarkan y (semakin kecil y = semakin depan).
 */
public abstract class Entity {

    protected final Vector2 position = new Vector2();
    protected final Rectangle hitbox = new Rectangle();
    protected final float hitboxW;
    protected final float hitboxH;

    protected Entity(float x, float y, float hitboxW, float hitboxH) {
        this.hitboxW = hitboxW;
        this.hitboxH = hitboxH;
        // tidak memanggil setPosition() (method yang bisa di-override) dari konstruktor
        position.set(x, y);
        hitbox.set(x - hitboxW / 2f, y, hitboxW, hitboxH);
    }

    /** Logika per frame. Tiap subclass punya perilaku sendiri. */
    public abstract void update(float delta);

    /** Gambar entity. Dipanggil di antara batch.begin() dan batch.end(). */
    public abstract void render(SpriteBatch batch);

    /** Apakah entity lain bisa menembus entity ini. Default: bisa ditembus. */
    public boolean isSolid() {
        return false;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
        syncHitbox();
    }

    /** Hitbox kecil di kaki, supaya kepala karakter bisa "menutupi" tembok di belakangnya. */
    protected void syncHitbox() {
        hitbox.set(position.x - hitboxW / 2f, position.y, hitboxW, hitboxH);
    }

    /** Dipanggil setelah CollisionSystem menggeser hitbox. */
    protected void syncPositionFromHitbox() {
        position.set(hitbox.x + hitboxW / 2f, hitbox.y);
    }

    /** Nilai urutan gambar: y lebih besar digambar lebih dulu (lebih "belakang"). */
    public float getDepth() {
        return position.y;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
}
