// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Unit test Inventory
//  PJ : Iqbal Aulia Rachman  ·  Minggu : W3
//  Kriteria: dua slot + pisau tetap, ambil/buang tanpa duplikasi
// =============================================================================
package com.kelompok7.lastremnant.combat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.entity.Direction;
import com.kelompok7.lastremnant.util.Constants;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class InventoryTest {

    /** Senjata palsu kedua supaya bisa menguji slot penuh. */
    private static class Pipe extends MeleeWeapon {
        Pipe() {
            super("pipe", "Pipa", "weapon_pipe", 2, 0.5f, 16f, 12f);
        }
    }

    private Inventory inv;
    private final List<String> events = new ArrayList<>();

    @Before
    public void setUp() {
        inv = new Inventory();
        inv.addListener(new InventoryListener() {
            @Override
            public void onActiveWeaponChanged(Weapon weapon, int slot) {
                events.add("active:" + weapon.getId() + "@" + slot);
            }

            @Override
            public void onSlotsChanged(Inventory inventory) {
                events.add("slots");
            }

            @Override
            public void onAmmoChanged(Weapon weapon) {
                events.add("ammo:" + weapon.getId());
            }
        });
    }

    @Test
    public void awalnyaHanyaPisau() {
        assertTrue(inv.getActive() instanceof Knife);
        assertEquals(Inventory.KNIFE_SLOT, inv.getActiveIndex());
        assertEquals(1, inv.getWeaponCount());
        assertEquals(Constants.WEAPON_SLOTS + 1, inv.getSlotCount());
    }

    @Test
    public void ambilPistolMasukSlotDanLangsungDipegang() {
        Pistol p = new Pistol();
        PickupResult r = inv.pickup(p);
        assertEquals(PickupResult.Type.STORED, r.getType());
        assertSame(p, inv.getActive());
        assertEquals(1, inv.getActiveIndex());
        assertTrue(events.contains("slots"));
        assertTrue(events.contains("active:pistol@1"));
    }

    @Test
    public void bendaYangSamaTidakBisaDiambilDuaKali() {
        Pistol p = new Pistol();
        inv.pickup(p);
        assertEquals(PickupResult.Type.REJECTED, inv.pickup(p).getType());
        assertEquals(2, inv.getWeaponCount());
    }

    @Test
    public void pistolKeduaDigabungBukanSlotBaru() {
        inv.pickup(new Pistol(3, 0));
        PickupResult r = inv.pickup(new Pistol(6, 0));
        assertEquals(PickupResult.Type.MERGED, r.getType());
        assertTrue(r.isTaken());
        assertEquals(2, inv.getWeaponCount());
        assertEquals(6, ((Pistol) inv.getSlot(1)).getReserve());
        assertTrue(events.contains("ammo:pistol"));
    }

    @Test
    public void pisauKeduaDitolak() {
        assertEquals(PickupResult.Type.REJECTED, inv.pickup(new Knife()).getType());
    }

    @Test
    public void slotPenuhMenukarSenjataDiTangan() {
        Pistol p = new Pistol();
        Pipe pipe = new Pipe();
        inv.pickup(p);
        inv.pickup(pipe);
        assertTrue(inv.isFull());
        assertSame(pipe, inv.getActive());

        Pipe pipe2 = new Pipe() {
            @Override
            public String getDisplayName() {
                return "Pipa berkarat";
            }
        };
        // id sama -> digabung (MeleeWeapon tidak bisa absorb) -> ditolak
        assertEquals(PickupResult.Type.REJECTED, inv.pickup(pipe2).getType());

        // senjata baru jenis lain: tukar dengan yang sedang dipegang (pipa)
        Weapon bat = new MeleeWeapon("bat", "Pemukul", "weapon_bat", 2, 0.4f, 16f, 12f) { };
        PickupResult r = inv.pickup(bat);
        assertEquals(PickupResult.Type.SWAPPED, r.getType());
        assertSame(pipe, r.getDropped());
        assertSame(bat, inv.getActive());
        assertSame(p, inv.getSlot(1));
        assertEquals(3, inv.getWeaponCount());
    }

    @Test
    public void slotPenuhSaatPegangPisauMenukarSlotSatu() {
        Pistol p = new Pistol();
        inv.pickup(p);
        inv.pickup(new Pipe());
        inv.select(0);
        Weapon bat = new MeleeWeapon("bat", "Pemukul", "weapon_bat", 2, 0.4f, 16f, 12f) { };
        PickupResult r = inv.pickup(bat);
        assertSame(p, r.getDropped());
        assertSame(bat, inv.getSlot(1));
    }

    @Test
    public void buangSenjataKembaliKePisau() {
        Pistol p = new Pistol();
        inv.pickup(p);
        assertSame(p, inv.dropActive());
        assertEquals(Inventory.KNIFE_SLOT, inv.getActiveIndex());
        assertNull(inv.getSlot(1));
        assertFalse(inv.has("pistol"));
    }

    @Test
    public void pisauTidakBisaDibuang() {
        assertNull(inv.dropActive());
        assertTrue(inv.getActive() instanceof Knife);
    }

    @Test
    public void ambilBuangAmbilTidakMenggandakan() {
        Pistol p = new Pistol(4, 2);
        inv.pickup(p);
        Weapon dropped = inv.dropActive();
        assertEquals(PickupResult.Type.STORED, inv.pickup(dropped).getType());
        assertEquals(2, inv.getWeaponCount());
        assertEquals(4, ((Pistol) inv.getActive()).getLoaded());
        assertEquals(2, ((Pistol) inv.getActive()).getReserve());
    }

    @Test
    public void slotKosongTidakBisaDipilih() {
        assertFalse(inv.select(1));
        assertFalse(inv.select(99));
        assertFalse(inv.select(-1));
        assertFalse("tidak berganti", inv.select(0));
    }

    @Test
    public void selectNextBerputar() {
        inv.pickup(new Pistol());
        inv.select(0);
        assertTrue(inv.selectNext());
        assertEquals(1, inv.getActiveIndex());
        assertTrue(inv.selectNext());
        assertEquals(0, inv.getActiveIndex());
    }

    @Test
    public void gantiSenjataMembatalkanIsiUlang() {
        Pistol p = new Pistol(2, 6);
        inv.pickup(p);
        assertTrue(inv.reload());
        assertTrue(p.isReloading());
        inv.select(0);
        assertFalse(p.isReloading());
        assertEquals(2, p.getLoaded());
    }

    @Test
    public void tembakSaatKosongOtomatisIsiUlang() {
        Pistol p = new Pistol(0, 6);
        inv.pickup(p);
        events.clear();
        assertNull(inv.attack(new Vector2(), Direction.RIGHT));
        assertTrue(p.isReloading());
        inv.update(Constants.PISTOL_RELOAD_TIME + 0.05f);
        assertEquals(6, p.getLoaded());
        // ammo diumumkan saat mulai dan saat selesai isi ulang
        assertEquals(2, events.stream().filter("ammo:pistol"::equals).count());
    }

    @Test
    public void tembakMengumumkanPeluru() {
        inv.pickup(new Pistol());
        events.clear();
        assertNotNull(inv.attack(new Vector2(), Direction.RIGHT));
        assertEquals("[ammo:pistol]", events.toString());
        assertEquals("5/6 | 0", inv.getActive().getAmmoText());
    }

    @Test
    public void listenerBisaDilepas() {
        Inventory i2 = new Inventory();
        InventoryListener l = new InventoryListener() {
            @Override
            public void onSlotsChanged(Inventory inventory) {
                throw new AssertionError("tidak boleh dipanggil");
            }
        };
        i2.addListener(l);
        i2.removeListener(l);
        i2.pickup(new Pistol());
    }
}
