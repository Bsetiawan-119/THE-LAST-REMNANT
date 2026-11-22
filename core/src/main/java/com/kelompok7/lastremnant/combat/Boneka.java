package com.kelompok7.lastremnant.combat;

import com.kelompok7.lastremnant.entity.CharacterSprites;
import com.kelompok7.lastremnant.entity.Player;
import com.kelompok7.lastremnant.world.CollisionSystem;
import java.util.function.BooleanSupplier;

public final class Boneka extends Enemy {
    private boolean firstShotMisses;

    public Boneka(float x, float y, boolean firstShotMisses, CollisionSystem collision, Player target,
                  Health targetHealth, BooleanSupplier hidden, CharacterSprites sprites) {
        super(x, y, 60, 44, 68, false, collision, target, targetHealth, hidden, sprites);
        this.firstShotMisses = firstShotMisses;
    }

    @Override protected void strike(Health targetHealth) {
        if (firstShotMisses) firstShotMisses = false;
        else targetHealth.damage(9);
    }
}
