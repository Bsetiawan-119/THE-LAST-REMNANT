// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Unit test Weapon / Knife / Pistol
//  PJ : Iqbal Aulia Rachman  ·  Minggu : W3
//  Kriteria: cooldown konsisten, peluru tervalidasi, isi ulang benar
// =============================================================================
package com.kelompok7.lastremnant.combat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.entity.Direction;
import com.kelompok7.lastremnant.util.Constants;
import org.junit.Test;

public class WeaponTest {

    private final Vector2 origin = new Vector2(100, 100);

    @Test
    public void pisauMenyerangLaluCooldown() {
        Knife k = new Knife();
        Attack a = k.tryAttack(origin, Direction.RIGHT);
        assertNotNull(a);
        assertEquals(Attack.Kind.MELEE, a.getKind());
        assertEquals(Constants.KNIFE_DAMAGE, a.getDamage());
        assertNull("masih cooldown", k.tryAttack(origin, Direction.RIGHT));
        k.update(Constants.KNIFE_COOLDOWN + 0.01f);
        assertNotNull(k.tryAttack(origin, Direction.RIGHT));
    }

    @Test
    public void cooldownSamaUntukBanyakFrameKecil() {
        Knife k = new Knife();
        k.tryAttack(origin, Direction.UP);
        int frames = 0;
        while (!k.isReady()) {
            k.update(1f / 60f);
            frames++;
        }
        // 0.35 detik pada 60 FPS = 21 frame (toleransi pembulatan 1 frame)
        assertEquals(Math.round(Constants.KNIFE_COOLDOWN * 60f), frames, 1);
    }

    @Test
    public void areaPisauDiDepanArahHadap() {
        Knife k = new Knife();
        Rectangle r = new Rectangle();
        k.hitArea(origin, Direction.RIGHT, r);
        assertTrue(r.x > origin.x);
        k.hitArea(origin, Direction.LEFT, r);
        assertTrue(r.x + r.width < origin.x);
        k.hitArea(origin, Direction.UP, r);
        assertTrue(r.y > origin.y);
        k.hitArea(origin, Direction.DOWN, r);
        assertTrue(r.y + r.height <= origin.y);
    }

    @Test
    public void pisauTidakBisaDibuang() {
        assertFalse(new Knife().isDroppable());
        assertTrue(new Pistol().isDroppable());
    }

    @Test
    public void pistolEnamPeluruLaluKosong() {
        Pistol p = new Pistol();
        assertEquals(6, p.getLoaded());
        for (int i = 0; i < 6; i++) {
            Attack a = p.tryAttack(origin, Direction.LEFT);
            assertNotNull("tembakan ke-" + (i + 1), a);
            assertEquals(Attack.Kind.PROJECTILE, a.getKind());
            assertEquals(Direction.LEFT, a.getDirection());
            p.update(Constants.PISTOL_COOLDOWN + 0.01f);
        }
        assertTrue(p.isEmpty());
        assertNull(p.tryAttack(origin, Direction.LEFT));
        assertFalse("tanpa cadangan tidak bisa isi ulang", p.startReload());
    }

    @Test
    public void isiUlangMemindahkanCadangan() {
        Pistol p = new Pistol(1, 10);
        assertTrue(p.startReload());
        assertTrue(p.isReloading());
        assertNull("tidak bisa menembak saat isi ulang", p.tryAttack(origin, Direction.UP));
        p.update(Constants.PISTOL_RELOAD_TIME + 0.01f);
        assertFalse(p.isReloading());
        assertEquals(6, p.getLoaded());
        assertEquals(5, p.getReserve());
    }

    @Test
    public void isiUlangSaatPenuhDitolak() {
        assertFalse(new Pistol(6, 12).startReload());
    }

    @Test
    public void absorbMenggabungPeluruTanpaDuplikasi() {
        Pistol mine = new Pistol(2, 0);
        Pistol floor = new Pistol(6, 3);
        assertTrue(mine.absorb(floor));
        assertEquals(2, mine.getLoaded());
        assertEquals(9, mine.getReserve());
        assertEquals("peluru tidak tercipta dua kali", 0, floor.getLoaded() + floor.getReserve());
        assertFalse(mine.absorb(mine));
        assertFalse(new Knife().absorb(new Knife()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isiMagasinTidakValidDitolak() {
        new Pistol(7, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cadanganNegatifDitolak() {
        new Pistol().addReserve(-1);
    }

    @Test
    public void factoryMembatasiIsiMagasin() {
        Pistol p = (Pistol) WeaponFactory.create("pistol", 99, 4);
        assertEquals(6, p.getLoaded());
        assertEquals(4, p.getReserve());
        assertEquals(6, ((Pistol) WeaponFactory.create("pistol", -1, 0)).getLoaded());
        assertTrue(WeaponFactory.create("knife", 0, 0) instanceof Knife);
        assertFalse(WeaponFactory.isKnown("bazoka"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void factoryIdTidakDikenal() {
        WeaponFactory.create("bazoka", 0, 0);
    }
}
