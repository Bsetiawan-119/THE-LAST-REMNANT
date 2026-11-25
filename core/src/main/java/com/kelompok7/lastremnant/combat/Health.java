package com.kelompok7.lastremnant.combat;

import com.badlogic.gdx.utils.Array;

/** HP untuk pemain maupun musuh. Nilai nol berarti kalah. */
public final class Health {
    public interface Listener {
        void onHealthChanged(Health health);
    }

    private final Array<Listener> listeners = new Array<>();
    private final int maximum;
    private int current;

    public Health(int maximum) {
        if (maximum <= 0) throw new IllegalArgumentException("HP maksimum harus positif");
        this.maximum = maximum;
        this.current = maximum;
    }

    public boolean damage(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Damage tidak boleh negatif");
        if (amount == 0 || current == 0) return false;
        current = Math.max(0, current - amount);
        for (int i = 0; i < listeners.size; i++) listeners.get(i).onHealthChanged(this);
        return true;
    }

    public void addListener(Listener listener) {
        if (!listeners.contains(listener, true)) listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.removeValue(listener, true);
    }

    public int getCurrent() { return current; }
    public int getMaximum() { return maximum; }
    public boolean isAlive() { return current > 0; }
}
