// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Unit test CollisionSystem
//  PJ : Iqbal Aulia Rachman  ·  Minggu : W2
//  Jalankan: ./gradlew core:test
// =============================================================================
package com.kelompok7.lastremnant.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kelompok7.lastremnant.entity.Entity;
import org.junit.Before;
import org.junit.Test;

public class CollisionSystemTest {

    private static final float EPS = 0.0001f;
    private Array<Rectangle> solids;
    private CollisionSystem collision;

    /** Entity dummy untuk menguji tabrakan antar-entity. */
    private static class Block extends Entity {
        private final boolean solid;

        Block(float x, float y, boolean solid) {
            super(x, y, 10f, 6f);
            this.solid = solid;
        }

        @Override public void update(float delta) { }
        @Override public void render(SpriteBatch batch) { }
        @Override public boolean isSolid() { return solid; }
    }

    @Before
    public void setUp() {
        solids = new Array<>();
        solids.add(new Rectangle(50, 0, 10, 100));     // tembok vertikal di x 50..60
        collision = new CollisionSystem(solids, new Rectangle(0, 0, 200, 200));
    }

    @Test
    public void gerakBebasTanpaHalangan() {
        Rectangle box = new Rectangle(10, 120, 10, 6);
        boolean blocked = collision.move(box, 5, 3, null);
        assertFalse(blocked);
        assertEquals(15f, box.x, EPS);
        assertEquals(123f, box.y, EPS);
    }

    @Test
    public void berhentiTepatDiDepanTembok() {
        Rectangle box = new Rectangle(35, 10, 10, 6);
        assertTrue(collision.move(box, 20, 0, null));
        assertEquals(40f, box.x, EPS);             // sisi kanan box = 50 = sisi kiri tembok
    }

    @Test
    public void meluncurSepanjangTemboSaatDiagonal() {
        Rectangle box = new Rectangle(40, 10, 10, 6);
        collision.move(box, 5, 5, null);
        assertEquals(40f, box.x, EPS);             // X tertahan
        assertEquals(15f, box.y, EPS);             // Y tetap jalan
    }

    @Test
    public void tidakMenembusTembokTipisSaatSangatCepat() {
        solids.clear();
        solids.add(new Rectangle(30, 0, 2, 100));  // tembok setebal 2 px
        Rectangle box = new Rectangle(10, 10, 10, 6);
        collision.move(box, 60, 0, null);          // lompatan besar (lag)
        assertEquals(20f, box.x, EPS);
    }

    @Test
    public void tertahanBatasPeta() {
        Rectangle box = new Rectangle(2, 2, 10, 6);
        collision.move(box, -10, -10, null);
        assertEquals(0f, box.x, EPS);
        assertEquals(0f, box.y, EPS);
        box.set(185, 190, 10, 6);
        collision.move(box, 20, 20, null);
        assertEquals(190f, box.x, EPS);
        assertEquals(194f, box.y, EPS);
    }

    @Test
    public void entitySolidMenghalangiYangTidakSolidTidak() {
        Block npc = new Block(100, 150, true);     // hitbox x 95..105, y 150..156
        Block ghost = new Block(100, 170, false);
        collision.addEntity(npc);
        collision.addEntity(ghost);

        Rectangle box = new Rectangle(80, 150, 10, 6);
        collision.move(box, 20, 0, null);
        assertEquals(85f, box.x, EPS);             // berhenti di depan NPC

        Rectangle box2 = new Rectangle(80, 170, 10, 6);
        collision.move(box2, 20, 0, null);
        assertEquals(100f, box2.x, EPS);           // menembus entity tidak solid
    }

    @Test
    public void entityTidakMenabrakDirinyaSendiri() {
        Block self = new Block(100, 150, true);
        collision.addEntity(self);
        Rectangle box = self.getHitbox();
        assertFalse(collision.move(box, 3, 0, self));
        assertFalse(collision.isBlocked(box, self));
    }

    @Test
    public void isBlockedMendeteksiTumpangTindih() {
        assertTrue(collision.isBlocked(new Rectangle(45, 10, 10, 6), null));
        assertFalse(collision.isBlocked(new Rectangle(10, 10, 10, 6), null));
        assertTrue(collision.isBlocked(new Rectangle(-5, 10, 10, 6), null));   // keluar peta
    }
}
