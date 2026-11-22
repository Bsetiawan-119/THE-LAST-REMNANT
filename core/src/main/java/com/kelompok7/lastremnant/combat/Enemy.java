package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.entity.CharacterSprites;
import com.kelompok7.lastremnant.entity.Direction;
import com.kelompok7.lastremnant.entity.Entity;
import com.kelompok7.lastremnant.entity.Player;
import com.kelompok7.lastremnant.world.CollisionSystem;

import java.util.function.BooleanSupplier;

public abstract class Enemy extends Entity {
    private final CollisionSystem collision;
    private final Player target;
    private final Health targetHealth;
    private final BooleanSupplier hidden;
    private final CharacterSprites sprites;
    private final Health health;
    private final float startX;
    private final float startY;
    private final float speed;
    private final float attackRange;
    private final boolean pursuer;
    private EnemyState state = EnemyState.PATROL;
    private Direction facing = Direction.LEFT;
    private boolean patrolRight;
    private boolean moving;
    private float delay;
    private float stunned;
    private float anim;
    private float alert;
    private float shotFlash;
    private boolean retired;

    protected Enemy(float x, float y, int hp, float speed, float attackRange, boolean pursuer,
                    CollisionSystem collision, Player target, Health targetHealth,
                    BooleanSupplier hidden, CharacterSprites sprites) {
        super(x, y, 8, 8);
        startX = x;
        startY = y;
        this.speed = speed;
        this.attackRange = attackRange;
        this.pursuer = pursuer;
        this.collision = collision;
        this.target = target;
        this.targetHealth = targetHealth;
        this.hidden = hidden;
        this.sprites = sprites;
        health = new Health(hp);
    }

    @Override public final void update(float delta) {
        if (!active()) return;
        anim += delta;
        delay = Math.max(0, delay - delta);
        shotFlash = Math.max(0, shotFlash - delta);
        alert = Math.max(0, alert - delta);
        moving = false;
        state.update(this, delta);
    }

    void patrol(float delta) {
        if (canSee()) { state = EnemyState.CHASE; return; }
        float goal = startX + (patrolRight ? 20 : -20);
        if (!move(goal, startY, speed * 0.42f, delta)) patrolRight = !patrolRight;
    }

    void chase(float delta) {
        if ((!pursuer && alert == 0 && !canSee()) || hidden.getAsBoolean()) {
            state = EnemyState.PATROL;
            return;
        }
        if (inRange()) { state = EnemyState.ATTACK; delay = Math.max(delay, 0.38f); return; }
        float x = target.getX();
        float y = target.getY();
        if (move(x, y, speed, delta)) return;
        // Coba menyusuri penghalang jika langkah langsung buntu.
        float side = (getY() >= startY ? 1 : -1) * 24;
        if (!move(getX(), getY() + side, speed, delta)) move(getX(), getY() - side, speed, delta);
    }

    void attack(float delta) {
        if (!inRange()) { state = EnemyState.CHASE; return; }
        facing = Direction.fromVector(target.getX() - getX(), target.getY() - getY(), facing);
        if (delay > 0) return;
        strike(targetHealth);
        if (attackRange > 40) shotFlash = 0.09f;
        delay = 1.10f;
    }

    protected abstract void strike(Health targetHealth);

    void recover(float delta) {
        stunned = Math.max(0, stunned - delta);
        if (stunned == 0) state = EnemyState.CHASE;
    }

    public void hit(int damage, float stunSeconds) {
        if (!active()) return;
        health.damage(damage);
        alert = 2.5f;
        if (stunSeconds > 0 && active()) {
            stunned = Math.max(stunned, stunSeconds);
            state = EnemyState.STUNNED;
        } else if (state == EnemyState.PATROL) state = EnemyState.CHASE;
    }

    public void hearShot(float x, float y) {
        if (Vector2.dst2(x, y, getX(), getY()) > 140 * 140) return;
        alert = 2.5f;
        if (state == EnemyState.PATROL) state = EnemyState.CHASE;
    }

    public boolean isBackExposed(float x, float y) {
        return (x - getX()) * facing.dx + (y - getY()) * facing.dy < -2
                && state != EnemyState.ATTACK;
    }

    private boolean move(float x, float y, float rate, float delta) {
        float dx = x - getX();
        float dy = y - getY();
        float distance = Vector2.len(dx, dy);
        if (distance < 1) return false;
        facing = Direction.fromVector(dx, dy, facing);
        float step = Math.min(distance, rate * delta) / distance;
        float px = hitbox.x, py = hitbox.y;
        collision.move(hitbox, dx * step, dy * step, this);
        syncPositionFromHitbox();
        moving = Math.abs(hitbox.x - px) + Math.abs(hitbox.y - py) > 0.01f;
        return moving;
    }

    private boolean canSee() {
        return inRange(pursuer ? 320 : 86)
                && (pursuer || alert > 0 || (target.getX() - getX()) * facing.dx
                + (target.getY() - getY()) * facing.dy >= 0 || distanceSquared() < 18 * 18);
    }

    private boolean inRange() { return inRange(attackRange); }

    private boolean inRange(float range) {
        return targetHealth.alive() && !hidden.getAsBoolean() && distanceSquared() <= range * range
                && collision.hasClearLine(getX(), getY() + 5, target.getX(), target.getY() + 5);
    }

    private float distanceSquared() { return Vector2.dst2(getX(), getY(), target.getX(), target.getY()); }
    public boolean active() { return health.alive() && !retired; }
    public boolean defeated() { return !health.alive(); }
    public void retire() { retired = true; }
    public EnemyState state() { return state; }

    @Override public void render(SpriteBatch batch) {
        if (!active() || sprites == null) return;
        TextureRegion frame = sprites.frame(facing, moving, anim);
        if (state == EnemyState.STUNNED) batch.setColor(0.7f, 0.9f, 1, 0.7f);
        if (shotFlash > 0) batch.setColor(1, 0.62f, 0.35f, 1);
        batch.draw(frame, MathUtils.round(getX() - frame.getRegionWidth() / 2f), MathUtils.round(getY()));
        batch.setColor(1, 1, 1, 1);
    }
}
