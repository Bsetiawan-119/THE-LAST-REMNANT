package com.kelompok7.lastremnant.combat;

import java.util.ArrayList;
import java.util.List;

public final class Ammo {
    private final int capacity;
    private int loaded;
    private int reserve;
    private final List<Runnable> listeners = new ArrayList<>();

    public Ammo(int capacity, int total) {
        if (capacity < 1 || total < 0) throw new IllegalArgumentException("Kapasitas atau peluru tidak valid");
        this.capacity = capacity;
        loaded = Math.min(capacity, total);
        reserve = total - loaded;
    }

    public boolean spend() {
        if (loaded == 0) return false;
        loaded--;
        notifyChanged();
        return true;
    }

    public boolean reload() {
        int count = Math.min(capacity - loaded, reserve);
        if (count == 0) return false;
        loaded += count;
        reserve -= count;
        notifyChanged();
        return true;
    }

    public void addReserve(int count) {
        if (count <= 0) return;
        reserve = (int) Math.min(Integer.MAX_VALUE, (long) reserve + count);
        notifyChanged();
    }

    public int loaded() { return loaded; }
    public int reserve() { return reserve; }
    public void addListener(Runnable listener) { if (!listeners.contains(listener)) listeners.add(listener); }
    public void removeListener(Runnable listener) { listeners.remove(listener); }
    private void notifyChanged() { List.copyOf(listeners).forEach(Runnable::run); }
}
