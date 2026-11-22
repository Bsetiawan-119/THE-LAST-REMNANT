package com.kelompok7.lastremnant.combat;

import java.util.ArrayList;
import java.util.List;

public final class Health {
    private final int max;
    private int value;
    private final List<Runnable> listeners = new ArrayList<>();

    public Health(int max) {
        if (max < 1) throw new IllegalArgumentException("HP maksimum harus positif");
        this.max = max;
        value = max;
    }

    public void damage(int amount) { if (amount > 0) set(Math.max(0, value - amount)); }
    public void heal(int amount) { if (amount > 0 && alive()) set(value + Math.min(max - value, amount)); }
    public void reset() { set(max); }
    public boolean alive() { return value > 0; }
    public int value() { return value; }
    public int max() { return max; }
    public void addListener(Runnable listener) { if (!listeners.contains(listener)) listeners.add(listener); }
    public void removeListener(Runnable listener) { listeners.remove(listener); }

    private void set(int next) {
        if (value == next) return;
        value = next;
        List.copyOf(listeners).forEach(Runnable::run);
    }
}
