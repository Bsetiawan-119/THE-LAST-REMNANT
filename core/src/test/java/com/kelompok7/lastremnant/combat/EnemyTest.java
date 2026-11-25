package com.kelompok7.lastremnant.combat;

import static org.junit.Assert.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kelompok7.lastremnant.entity.Direction;
import com.kelompok7.lastremnant.entity.Player;
import com.kelompok7.lastremnant.world.CollisionSystem;
import org.junit.Test;

public class EnemyTest {
    private final Array<Rectangle> walls = new Array<>();
    private final CollisionSystem collision = new CollisionSystem(walls, null);
    private final Player player = new Player(300, 100, collision);
    private final Enemy enemy = new Enemy(100, 100, player, collision, null);

    @Test public void patroliMengejarLaluKehilanganTarget() {
        enemy.update(0.1f);
        assertEquals(Enemy.Behavior.PATROL, enemy.getBehavior());
        assertTrue(enemy.getX() > 100);
        player.setPosition(150, 100);
        float start = enemy.getX();
        enemy.update(0.1f);
        assertEquals(Enemy.Behavior.CHASE, enemy.getBehavior());
        assertTrue(enemy.getX() > start);
        player.setPosition(300, 100);
        enemy.update(0.1f);
        assertEquals(Enemy.Behavior.PATROL, enemy.getBehavior());
    }

    @Test public void dindingMenghalangiKejarDanSerang() {
        walls.add(new Rectangle(110, 0, 16, 200));
        player.setPosition(140, 100);
        for (int i = 0; i < 150; i++) enemy.update(0.02f);
        assertTrue(enemy.getHitbox().x + enemy.getHitbox().width <= 110);
        assertEquals(player.getHealth().getMaximum(), player.getHealth().getCurrent());
    }

    @Test public void seranganKontakMemilikiCooldownDanBerhentiSaatMusuhKalah() {
        player.setPosition(100, 100);
        enemy.update(0.02f);
        assertEquals(Enemy.Behavior.ATTACK, enemy.getBehavior());
        int hp = player.getHealth().getMaximum() - 1;
        enemy.update(0.2f);
        assertEquals(hp, player.getHealth().getCurrent());
        enemy.update(1.1f);
        assertEquals(hp - 1, player.getHealth().getCurrent());
        enemy.takeDamage(50, null);
        enemy.update(2f);
        assertEquals(hp - 1, player.getHealth().getCurrent());
    }

    @Test public void pisauDanPeluruWeaponSystemBisaMengalahkanBoneka() {
        player.setPosition(88, 100);
        player.setFacing(Direction.RIGHT);
        WeaponSystem weapons = new WeaponSystem(collision, null);
        weapons.addTarget(enemy);
        player.requestAttack();
        player.update(0.02f);
        weapons.update(0.02f, player);
        assertTrue(enemy.isAlive());
        player.setPosition(55, 100);
        player.getInventory().pickup(new Pistol());
        player.requestAttack();
        player.update(0.02f);
        for (int i = 0; i < 15; i++) weapons.update(0.02f, player);
        assertFalse(enemy.isAlive());
    }

    @Test public void healthMemberiEventHanyaSaatNilaiBerubah() {
        Health hp = new Health(4);
        java.util.List<Integer> changes = new java.util.ArrayList<>();
        Health.Listener listener = health -> changes.add(health.getCurrent());
        hp.addListener(listener);
        hp.addListener(listener);
        assertFalse(hp.damage(0));
        assertTrue(hp.damage(1));
        hp.removeListener(listener);
        assertTrue(hp.damage(100));
        assertFalse(hp.damage(1));
        assertEquals(0, hp.getCurrent());
        assertEquals(java.util.List.of(3), changes);
    }
}
