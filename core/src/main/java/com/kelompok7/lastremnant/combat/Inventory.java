package com.kelompok7.lastremnant.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Dua slot yang dapat diganti. Setelah didapat, pisau selalu dibawa. */
public final class Inventory {
    private final Weapon[] slots = new Weapon[2];
    private final List<Runnable> listeners = new ArrayList<>();
    private Knife knife;
    private int selected = -1;

    public boolean pickUp(Weapon weapon) {
        Objects.requireNonNull(weapon);
        if (weapon instanceof Knife foundKnife) {
            if (knife != null) return false;
            knife = foundKnife;
        } else {
            if (slots[0] == weapon || slots[1] == weapon) return false;
            int free = slots[0] == null ? 0 : slots[1] == null ? 1 : -1;
            if (free == -1) return false;
            slots[free] = weapon;
            selected = free;
        }
        notifyChanged();
        return true;
    }

    public Weapon active() { return selected == -1 ? knife : slots[selected]; }
    public Knife knife() { return knife; }

    public void cycle() {
        int candidate = selected;
        for (int i = 0; i < 3; i++) {
            candidate = candidate == 1 ? -1 : candidate + 1;
            if (candidate == -1 ? knife != null : slots[candidate] != null) {
                if (selected != candidate) {
                    selected = candidate;
                    notifyChanged();
                }
                return;
            }
        }
    }

    public Weapon drop() {
        if (selected < 0) return null;
        Weapon dropped = slots[selected];
        slots[selected] = null;
        selected = knife != null ? -1 : slots[0] != null ? 0 : slots[1] != null ? 1 : -1;
        notifyChanged();
        return dropped;
    }

    public void update(float delta) {
        if (knife != null) knife.update(delta);
        for (Weapon weapon : slots) if (weapon != null) weapon.update(delta);
    }

    public boolean addPistolAmmo(int count) {
        for (Weapon w : slots) {
            if (w instanceof Pistol pistol) {
                pistol.ammo().addReserve(count);
                return true;
            }
        }
        return false;
    }

    public void addListener(Runnable listener) { if (!listeners.contains(listener)) listeners.add(listener); }
    public void removeListener(Runnable listener) { listeners.remove(listener); }
    private void notifyChanged() { List.copyOf(listeners).forEach(Runnable::run); }
}
