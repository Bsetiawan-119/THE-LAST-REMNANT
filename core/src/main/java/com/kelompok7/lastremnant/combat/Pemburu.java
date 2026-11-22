package com.kelompok7.lastremnant.combat;

import com.kelompok7.lastremnant.entity.CharacterSprites;
import com.kelompok7.lastremnant.entity.Player;
import com.kelompok7.lastremnant.world.CollisionSystem;
import java.util.function.BooleanSupplier;

public final class Pemburu extends Enemy {
    public Pemburu(float x, float y, CollisionSystem collision, Player target, Health targetHealth,
                   BooleanSupplier hidden, CharacterSprites sprites) {
        super(x, y, 42, 56, 13, true, collision, target, targetHealth, hidden, sprites);
    }

    @Override protected void strike(Health targetHealth) { targetHealth.damage(7); }
}
