// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Player.java
//  Bagian  : Core gameplay & world (karakter utama: zentra)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W1 (struktur), W2 (movement + collision + animasi + probe interaksi)
//            W3 (Inventory senjata + permintaan serang/isi ulang/ganti/ambil/buang)
//  OOP     : INHERITANCE (extends Entity), ENCAPSULATION (gerak hanya lewat move()),
//            COMPOSITION (Player PUNYA Inventory, bukan mewarisi senjata),
//            target COMMAND pattern (MoveCommand / InteractCommand / AttackCommand ...)
// =============================================================================
package com.kelompok7.lastremnant.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kelompok7.lastremnant.combat.Attack;
import com.kelompok7.lastremnant.combat.Inventory;
import com.kelompok7.lastremnant.combat.Health;
import com.kelompok7.lastremnant.util.Constants;
import com.kelompok7.lastremnant.world.CollisionSystem;

/**
 * zentra, kurir di Depok Selatan.
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

    // --- senjata (W3) ---
    private final Health health = new Health(6);
    private final Inventory inventory = new Inventory();
    private final Array<Attack> pendingAttacks = new Array<>();
    private boolean attackRequested;
    private boolean pickupRequested;
    private boolean dropRequested;

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

    /** Serang dengan senjata di tangan; diproses di update() frame ini. */
    public void requestAttack() {
        attackRequested = true;
    }

    /** Isi ulang senjata di tangan (langsung, tidak menunggu update). */
    public void requestReload() {
        inventory.reload();
    }

    /** Pilih slot senjata: 0 = pisau, 1..n = slot pungutan. */
    public void requestSelectSlot(int slot) {
        inventory.select(slot);
    }

    public void requestNextWeapon() {
        inventory.selectNext();
    }

    /** Ambil senjata di lantai; diproses WeaponSystem karena butuh daftar benda di lantai. */
    public void requestPickup() {
        pickupRequested = true;
    }

    /** Buang senjata di tangan; diproses WeaponSystem agar senjatanya muncul di lantai. */
    public void requestDrop() {
        dropRequested = true;
    }

    public boolean consumePickupRequest() {
        boolean r = pickupRequested;
        pickupRequested = false;
        return r;
    }

    public boolean consumeDropRequest() {
        boolean r = dropRequested;
        dropRequested = false;
        return r;
    }

    /**
     * Serangan yang dibuat sejak pemanggilan terakhir. Array dikosongkan oleh pemanggil
     * berikutnya, jadi salin isinya bila perlu disimpan.
     */
    public Array<Attack> drainAttacks(Array<Attack> out) {
        out.addAll(pendingAttacks);
        pendingAttacks.clear();
        return out;
    }

    /** Hentikan gerak (mis. saat dialog dibuka). */
    public void stop() {
        moveIntent.setZero();
        moving = false;
        interactRequested = false;
        attackRequested = false;
        pickupRequested = false;
        dropRequested = false;
    }

    // ------------------------------------------------------------------ Entity

    @Override
    public void update(float delta) {
        inventory.update(delta);
        if (attackRequested) {
            attackRequested = false;
            Attack a = inventory.attack(position, facing);
            if (a != null) pendingAttacks.add(a);
        }
        updateMovement(delta);
    }

    private void updateMovement(float delta) {
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
     * Kotak kecil tepat di depan zentra sesuai arah hadap. Interactable yang
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

    public Health getHealth() { return health; }

    public Inventory getInventory() {
        return inventory;
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
