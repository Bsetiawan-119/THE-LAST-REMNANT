// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Unit test WeaponSystem (+ Player + Command)
//  PJ : Iqbal Aulia Rachman  ·  Minggu : W3
// =============================================================================
package com.kelompok7.lastremnant.combat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kelompok7.lastremnant.entity.Direction;
import com.kelompok7.lastremnant.entity.Player;
import com.kelompok7.lastremnant.input.InputHandler;
import com.kelompok7.lastremnant.util.Constants;
import com.kelompok7.lastremnant.world.CollisionSystem;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class WeaponSystemTest {

    private static final float DT = 1f / 60f;

    private CollisionSystem collision;
    private Player player;
    private WeaponSystem weapons;
    private InputHandler input;
    private final List<String> log = new ArrayList<>();

    @Before
    public void setUp() {
        Array<Rectangle> solids = new Array<>();
        solids.add(new Rectangle(300, 0, 16, 400));          // tembok di x = 300
        collision = new CollisionSystem(solids, new Rectangle(0, 0, 400, 400));
        player = new Player(100, 100, collision);
        player.setFacing(Direction.RIGHT);
        weapons = new WeaponSystem(collision, null);
        input = InputHandler.withDefaultBindings();
        weapons.addListener(new WeaponSystemListener() {
            @Override
            public void onHit(Damageable target, Attack attack) {
                log.add("hit:" + attack.getWeaponId());
            }

            @Override
            public void onPickedUp(Weapon weapon, PickupResult result) {
                log.add("pickup:" + weapon.getId() + ":" + result.getType());
            }

            @Override
            public void onDropped(Weapon weapon) {
                log.add("drop:" + weapon.getId());
            }

            @Override
            public void onActionRejected(String reason) {
                log.add("reject");
            }
        });
    }

    private void frame() {
        input.handleInput(player);
        player.update(DT);
        weapons.update(DT, player);
    }

    private void press(int key) {
        input.keyDown(key);
        frame();
        input.keyUp(key);
    }

    private void frames(int n) {
        for (int i = 0; i < n; i++) frame();
    }

    @Test
    public void pisauMengenaiTargetDiDepan() {
        TargetDummy d = new TargetDummy(112, 100, 3, null, null);
        weapons.addTarget(d);
        press(Keys.K);
        assertEquals(2, d.getHp());
        assertEquals("[hit:knife]", log.toString());
        assertEquals(1, weapons.getActiveSlashCount());
    }

    @Test
    public void pisauTidakMengenaiTargetDiBelakang() {
        TargetDummy d = new TargetDummy(85, 100, 3, null, null);
        weapons.addTarget(d);
        press(Keys.K);
        assertEquals(3, d.getHp());
    }

    @Test
    public void tahanTombolSerangTidakMengulangSerangan() {
        TargetDummy d = new TargetDummy(112, 100, 10, null, null);
        weapons.addTarget(d);
        input.keyDown(Keys.K);
        frames(60);                     // satu detik ditahan
        assertEquals("tekan sekali = satu serangan", 9, d.getHp());
    }

    @Test
    public void ambilPistolDariLantaiLaluTembak() {
        weapons.spawnPickup(new Pistol(), 104, 100);
        assertNotNull(weapons.getPickupPrompt(player));
        press(Keys.F);
        assertEquals(0, weapons.getFloorItems().size);
        assertTrue(player.getInventory().getActive() instanceof Pistol);
        assertEquals("[pickup:pistol:STORED]", log.toString());

        TargetDummy d = new TargetDummy(200, 100, 5, null, null);
        weapons.addTarget(d);
        press(Keys.K);
        assertEquals(1, weapons.getProjectiles().size);
        frames(60);
        assertEquals(5 - Constants.PISTOL_DAMAGE, d.getHp());
        assertEquals("peluru hilang setelah kena", 0, weapons.getProjectiles().size);
        assertEquals(5, ((Pistol) player.getInventory().getActive()).getLoaded());
    }

    @Test
    public void peluruMengenaiMusuhYangSolid() {
        // musuh solid terdaftar di CollisionSystem: peluru harus kena, bukan dianggap tembok
        player.getInventory().pickup(new Pistol());
        TargetDummy d = new TargetDummy(160, 100, 5, null, null);
        collision.addEntity(d);
        weapons.addTarget(d);
        press(Keys.K);
        frames(30);
        assertEquals(5 - Constants.PISTOL_DAMAGE, d.getHp());
    }

    @Test
    public void peluruBerhentiDiTembok() {
        player.getInventory().pickup(new Pistol());
        TargetDummy behindWall = new TargetDummy(350, 100, 5, null, null);
        weapons.addTarget(behindWall);
        press(Keys.K);
        frames(120);
        assertEquals(5, behindWall.getHp());
        assertEquals(0, weapons.getProjectiles().size);
    }

    @Test
    public void peluruHilangSetelahJarakMaksimum() {
        collision = new CollisionSystem(new Array<>(), null);   // tanpa tembok & tanpa batas
        player = new Player(0, 0, collision);
        player.setFacing(Direction.UP);
        weapons = new WeaponSystem(collision, null);
        player.getInventory().pickup(new Pistol());
        press(Keys.K);
        Projectile p = weapons.getProjectiles().first();
        frames(300);
        assertFalse(p.isAlive());
        assertTrue(p.getTraveled() >= Constants.BULLET_RANGE);
    }

    @Test
    public void buangSenjataMunculDiLantaiDanBisaDiambilLagi() {
        Pistol p = new Pistol(3, 0);
        player.getInventory().pickup(p);
        press(Keys.G);
        assertEquals(1, weapons.getFloorItems().size);
        assertSame(p, weapons.getFloorItems().first().getWeapon());
        assertTrue(player.getInventory().getActive() instanceof Knife);

        press(Keys.F);
        assertEquals(0, weapons.getFloorItems().size);
        assertSame(p, player.getInventory().getActive());
        assertEquals("peluru sisa ikut tersimpan", 3, p.getLoaded());
        assertEquals("[drop:pistol, pickup:pistol:STORED]", log.toString());
    }

    @Test
    public void buangPisauDitolak() {
        press(Keys.G);
        assertEquals(0, weapons.getFloorItems().size);
        assertEquals("[reject]", log.toString());
    }

    @Test
    public void ambilTanpaBendaDitolak() {
        weapons.spawnPickup(new Pistol(), 200, 200);   // jauh
        press(Keys.F);
        assertEquals(1, weapons.getFloorItems().size);
        assertEquals("[reject]", log.toString());
        assertNull(weapons.getPickupPrompt(player));
    }

    @Test
    public void pistolKeduaDiLantaiJadiPeluru() {
        player.getInventory().pickup(new Pistol(6, 0));
        weapons.spawnPickup(new Pistol(6, 0), 104, 100);
        assertTrue(weapons.getPickupPrompt(player).contains("peluru"));
        press(Keys.F);
        assertEquals(0, weapons.getFloorItems().size);
        assertEquals(2, player.getInventory().getWeaponCount());
        assertEquals(6, ((Pistol) player.getInventory().getActive()).getReserve());
    }

    @Test
    public void tukarSenjataSaatPenuhMeninggalkanYangLamaDiLantai() {
        Weapon pipe = new MeleeWeapon("pipe", "Pipa", "weapon_pipe", 2, 0.5f, 16f, 12f) { };
        Weapon bat = new MeleeWeapon("bat", "Pemukul", "weapon_bat", 2, 0.4f, 16f, 12f) { };
        player.getInventory().pickup(new Pistol());
        player.getInventory().pickup(pipe);
        weapons.spawnPickup(bat, 104, 100);
        assertEquals("Tukar pipa dengan pemukul", weapons.getPickupPrompt(player));
        press(Keys.F);
        assertEquals(1, weapons.getFloorItems().size);
        assertSame(pipe, weapons.getFloorItems().first().getWeapon());
        assertSame(bat, player.getInventory().getActive());
    }

    @Test
    public void ambilDenganInteractableMemilihBendaYangDitunjuk() {
        WeaponPickup near = weapons.spawnPickup(new Pistol(2, 0), 104, 100);
        WeaponPickup other = weapons.spawnPickup(new Pistol(6, 0), 110, 104);
        assertTrue(weapons.pickup(player, other));
        assertEquals(6, ((Pistol) player.getInventory().getActive()).getLoaded());
        assertSame(near, weapons.getFloorItems().first());
        assertFalse("benda yang sudah diambil tidak bisa diambil lagi", weapons.pickup(player, other));
    }

    @Test
    public void tombolAngkaMemilihSlot() {
        player.getInventory().pickup(new Pistol());
        press(Keys.NUM_1);
        assertEquals(0, player.getInventory().getActiveIndex());
        press(Keys.NUM_2);
        assertEquals(1, player.getInventory().getActiveIndex());
        press(Keys.Q);
        assertEquals(0, player.getInventory().getActiveIndex());
    }

    @Test
    public void tombolRIsiUlang() {
        Pistol p = new Pistol(1, 6);
        player.getInventory().pickup(p);
        press(Keys.R);
        assertTrue(p.isReloading());
        frames(Math.round(Constants.PISTOL_RELOAD_TIME * 60) + 2);
        assertEquals(6, p.getLoaded());
    }

    @Test
    public void stopMembatalkanPermintaanCombat() {
        player.requestAttack();
        player.requestPickup();
        player.requestDrop();
        player.stop();
        player.update(DT);
        assertFalse(player.consumePickupRequest());
        assertFalse(player.consumeDropRequest());
        assertEquals(0, player.drainAttacks(new Array<>()).size);
    }

    @Test
    public void targetMatiTidakKenaLagi() {
        TargetDummy d = new TargetDummy(112, 100, 1, null, null);
        weapons.addTarget(d);
        press(Keys.K);
        assertFalse(d.isAlive());
        assertFalse("bangkai boneka bisa dilewati", d.isSolid());
        frames(30);
        press(Keys.K);
        assertEquals(1, d.getHitCount());
    }
}
