package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.entity.CharacterSprites;
import com.kelompok7.lastremnant.entity.Direction;
import com.kelompok7.lastremnant.entity.Entity;
import com.kelompok7.lastremnant.entity.Player;
import com.kelompok7.lastremnant.world.CollisionSystem;

/** Boneka Stage 1. Senjata pemain mengenalinya lewat Damageable. */
public final class Enemy extends Entity implements Damageable {
    private static final float DETECT_RADIUS = 75f;
    private static final float LOSE_RADIUS = 110f;
    private static final float ATTACK_INTERVAL = 1.2f;
    private static final float PATROL_RADIUS = 24f;

    /** Strategy menentukan tindakan per frame. Masing-masing Boneka menyimpan progres AI sendiri. */
    public enum Behavior {
        PATROL {
            @Override void act(Enemy enemy, float delta) {
                float endX = enemy.homeX + enemy.patrolSign * PATROL_RADIUS;
                boolean blocked = enemy.walkTo(endX, enemy.homeY, 18f, delta);
                if (blocked || Math.abs(enemy.getX() - endX) < 0.1f) enemy.patrolSign *= -1;
            }
        },
        CHASE {
            @Override void act(Enemy enemy, float delta) {
                enemy.walkTo(enemy.player.getX(), enemy.player.getY(), 46f, delta);
            }
        },
        ATTACK {
            @Override void act(Enemy enemy, float delta) {
                enemy.facing = Direction.fromVector(enemy.player.getX() - enemy.getX(),
                        enemy.player.getY() - enemy.getY(), enemy.facing);
                if (enemy.cooldown <= 0f) {
                    enemy.player.getHealth().damage(1);
                    enemy.cooldown = ATTACK_INTERVAL;
                }
            }
        };

        abstract void act(Enemy enemy, float delta);
    }

    private final Player player;
    private final CollisionSystem collision;
    private final CharacterSprites sprites;
    private final Health health = new Health(3);
    private final Rectangle hurtbox = new Rectangle();
    private final float homeX;
    private final float homeY;
    private int patrolSign = 1;
    private float cooldown;
    private float animationTime;
    private boolean moving;
    private Direction facing = Direction.DOWN;
    private Behavior behavior = Behavior.PATROL;

    public Enemy(float x, float y, Player player, CollisionSystem collision, CharacterSprites sprites) {
        super(x, y, 10f, 8f);
        homeX = x;
        homeY = y;
        this.player = player;
        this.collision = collision;
        this.sprites = sprites;
    }

    @Override public void update(float delta) {
        if (!isAlive() || !player.getHealth().isAlive() || delta <= 0f) return;
        cooldown = Math.max(0f, cooldown - delta);
        moving = false;
        float radius = behavior == Behavior.PATROL ? DETECT_RADIUS : LOSE_RADIUS;
        if (hitbox.overlaps(player.getHitbox())) {
            behavior = Behavior.ATTACK;
        } else if (position.dst2(player.getPosition()) <= radius * radius) {
            behavior = Behavior.CHASE;
        } else {
            behavior = Behavior.PATROL;
        }
        behavior.act(this, delta);
        animationTime = moving ? animationTime + delta : 0f;
    }

    private boolean walkTo(float x, float y, float speed, float delta) {
        float dx = x - position.x;
        float dy = y - position.y;
        float distance = Vector2.len(dx, dy);
        if (distance == 0f) return false;
        float scale = Math.min(1f, speed * delta / distance);
        float oldX = hitbox.x;
        float oldY = hitbox.y;
        boolean blocked = collision.move(hitbox, dx * scale, dy * scale, this);
        syncPositionFromHitbox();
        moving = hitbox.x != oldX || hitbox.y != oldY;
        facing = Direction.fromVector(dx, dy, facing);
        return blocked;
    }

    @Override public void render(SpriteBatch batch) {
        if (!isAlive() || sprites == null) return;
        TextureRegion frame = sprites.frame(facing, moving, animationTime);
        batch.draw(frame, Math.round(getX() - frame.getRegionWidth() / 2f), Math.round(getY()));
    }

    @Override public Rectangle getHurtbox() {
        return hurtbox.set(getX() - 6f, getY(), 12f, 20f);
    }

    @Override public boolean isAlive() { return health.isAlive(); }
    @Override public void takeDamage(int amount, Attack source) { health.damage(amount); }
    public Behavior getBehavior() { return behavior; }
}
