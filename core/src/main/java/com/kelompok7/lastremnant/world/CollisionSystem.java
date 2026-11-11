// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : CollisionSystem.java
//  Bagian  : Core gameplay & world (deteksi & resolusi tabrakan)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W1 (AABB dasar), W2 (geser per sumbu, sub-step, entity solid)
//  Test    : core/src/test/.../world/CollisionSystemTest.java
// =============================================================================
package com.kelompok7.lastremnant.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kelompok7.lastremnant.entity.Entity;

/**
 * Tabrakan AABB (kotak sejajar sumbu) antara hitbox yang bergerak dengan:
 * <ol>
 *   <li>kotak statis dari layer "collision" di peta Tiled,</li>
 *   <li>entity lain yang {@link Entity#isSolid() solid} (NPC),</li>
 *   <li>batas peta.</li>
 * </ol>
 * Gerakan diselesaikan per sumbu (X dulu, lalu Y) sehingga pemain bisa
 * "meluncur" menyusuri tembok saat bergerak diagonal.
 */
public class CollisionSystem {

    /** Langkah maksimum per sub-step, mencegah hitbox menembus tembok tipis saat lag. */
    private static final float MAX_STEP = 4f;

    private final Array<Rectangle> solids;
    private final Rectangle bounds;
    private final Array<Entity> entities = new Array<>();
    private final Rectangle tmp = new Rectangle();

    /**
     * @param solids kotak statis (tidak disalin; World tetap pemiliknya)
     * @param bounds batas dunia; null = tanpa batas
     */
    public CollisionSystem(Array<Rectangle> solids, Rectangle bounds) {
        this.solids = solids;
        this.bounds = bounds;
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public void removeEntity(Entity e) {
        entities.removeValue(e, true);
    }

    /**
     * Geser {@code box} sejauh (dx, dy) sambil menghormati tabrakan.
     * Kotak dimodifikasi langsung.
     *
     * @param self entity pemilik box (agar tidak menabrak dirinya sendiri), boleh null
     * @return true jika gerakan terhalang di salah satu sumbu
     */
    public boolean move(Rectangle box, float dx, float dy, Entity self) {
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / MAX_STEP));
        float sx = dx / steps;
        float sy = dy / steps;
        boolean blocked = false;
        for (int i = 0; i < steps; i++) {
            if (sx != 0f) {
                box.x += sx;
                blocked |= resolveX(box, sx, self);
            }
            if (sy != 0f) {
                box.y += sy;
                blocked |= resolveY(box, sy, self);
            }
        }
        return blocked;
    }

    /** Apakah kotak ini menabrak sesuatu (berguna untuk validasi titik spawn). */
    public boolean isBlocked(Rectangle box, Entity self) {
        if (bounds != null && !contains(bounds, box)) return true;
        for (Rectangle s : solids) {
            if (s.overlaps(box)) return true;
        }
        for (Entity e : entities) {
            if (e != self && e.isSolid() && e.getHitbox().overlaps(box)) return true;
        }
        return false;
    }

    private boolean resolveX(Rectangle box, float dx, Entity self) {
        boolean hit = false;
        for (int i = 0, n = solids.size + entities.size; i < n; i++) {
            Rectangle s = obstacle(i, self);
            if (s == null || !s.overlaps(box)) continue;
            box.x = dx > 0 ? s.x - box.width : s.x + s.width;
            hit = true;
        }
        if (bounds != null) {
            if (box.x < bounds.x) { box.x = bounds.x; hit = true; }
            if (box.x + box.width > bounds.x + bounds.width) { box.x = bounds.x + bounds.width - box.width; hit = true; }
        }
        return hit;
    }

    private boolean resolveY(Rectangle box, float dy, Entity self) {
        boolean hit = false;
        for (int i = 0, n = solids.size + entities.size; i < n; i++) {
            Rectangle s = obstacle(i, self);
            if (s == null || !s.overlaps(box)) continue;
            box.y = dy > 0 ? s.y - box.height : s.y + s.height;
            hit = true;
        }
        if (bounds != null) {
            if (box.y < bounds.y) { box.y = bounds.y; hit = true; }
            if (box.y + box.height > bounds.y + bounds.height) { box.y = bounds.y + bounds.height - box.height; hit = true; }
        }
        return hit;
    }

    /** Index gabungan: [0, solids.size) = statis, sisanya = entity solid. */
    private Rectangle obstacle(int index, Entity self) {
        if (index < solids.size) return solids.get(index);
        Entity e = entities.get(index - solids.size);
        if (e == self || !e.isSolid()) return null;
        return tmp.set(e.getHitbox());
    }

    private static boolean contains(Rectangle outer, Rectangle inner) {
        return inner.x >= outer.x && inner.y >= outer.y
                && inner.x + inner.width <= outer.x + outer.width
                && inner.y + inner.height <= outer.y + outer.height;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
