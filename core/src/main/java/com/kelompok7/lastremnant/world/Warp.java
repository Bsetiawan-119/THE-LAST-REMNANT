// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Warp.java
//  Bagian  : Core gameplay & world (pintu: luar <-> dalam rumah No. 17)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3
//  OOP     : INHERITANCE (extends Entity), POLYMORPHISM (Interactable)
// =============================================================================
package com.kelompok7.lastremnant.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.entity.Direction;
import com.kelompok7.lastremnant.entity.Entity;
import com.kelompok7.lastremnant.entity.Interactable;
import com.kelompok7.lastremnant.entity.InteractionContext;
import java.util.function.Function;

/**
 * Area tak terlihat di depan pintu. Ditekan E -> pemain pindah ke objek lain
 * di peta yang sama (properti Tiled {@code target} = nama objek tujuan).
 * Interior diletakkan di bagian peta yang terpisah, jadi tidak perlu ganti peta.
 */
public class Warp extends Entity implements Interactable {

    private final String label;
    private final float targetX;
    private final float targetY;
    private final Direction targetFacing;
    private final Rectangle bounds = new Rectangle();

    public Warp(Rectangle area, String label, float targetX, float targetY, Direction targetFacing) {
        super(area.x + area.width / 2f, area.y, area.width, area.height);
        this.bounds.set(area);
        this.label = label == null ? "Masuk" : label;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetFacing = targetFacing;
    }

    /**
     * Buat dari objek Tiled {@code kind=warp}.
     *
     * @param lookup pencari objek tujuan berdasarkan nama, di game: {@code world::findSpawnByName}
     * @throws IllegalArgumentException bila objek tujuan tidak ada (salah ketik di Tiled)
     */
    public static Warp fromSpawn(World.Spawn s, Function<String, World.Spawn> lookup) {
        World.Spawn target = lookup.apply(s.prop("target"));
        if (target == null) {
            throw new IllegalArgumentException("Warp '" + s.name + "': target '" + s.prop("target") + "' tidak ada di peta");
        }
        Direction facing = Direction.parse(target.prop("facing"), Direction.parse(s.prop("facing"), Direction.DOWN));
        return new Warp(s.bounds(new Rectangle()), s.prop("label"), target.x, target.y, facing);
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void render(SpriteBatch batch) {
        // tidak terlihat: pintunya sudah digambar sebagai tile
    }

    @Override
    public Rectangle getInteractBounds() {
        return bounds;
    }

    @Override
    public String getInteractLabel(InteractionContext ctx) {
        return label;
    }

    @Override
    public Vector2 getIndicatorPosition(Vector2 out) {
        return out.set(bounds.x + bounds.width / 2f, bounds.y + bounds.height + 6f);
    }

    @Override
    public void interact(InteractionContext ctx) {
        ctx.teleportPlayer(targetX, targetY, targetFacing);
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public Direction getTargetFacing() {
        return targetFacing;
    }
}
