package com.kelompok7.lastremnant.combat;

/** Perilaku yang dijalankan Enemy pada frame aktif. */
public enum EnemyState {
    PATROL { @Override void update(Enemy enemy, float delta) { enemy.patrol(delta); } },
    CHASE { @Override void update(Enemy enemy, float delta) { enemy.chase(delta); } },
    ATTACK { @Override void update(Enemy enemy, float delta) { enemy.attack(delta); } },
    STUNNED { @Override void update(Enemy enemy, float delta) { enemy.recover(delta); } };

    abstract void update(Enemy enemy, float delta);
}
