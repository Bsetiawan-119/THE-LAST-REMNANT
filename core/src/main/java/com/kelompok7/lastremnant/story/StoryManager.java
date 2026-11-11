// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : StoryManager.java
//  Bagian  : Dialogue & story (status cerita: flag & tujuan aktif)
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W2 = flag + objective + observer
//            W3 = mendengarkan QuestManager (Insan) + logika dua ending
//  OOP     : ENCAPSULATION (flag hanya berubah lewat method publik)
//  Pattern : OBSERVER (subject)
//  Test    : core/src/test/.../story/StoryManagerTest.java
// =============================================================================
package com.kelompok7.lastremnant.story;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.kelompok7.lastremnant.dialogue.FlagQuery;

/**
 * Menyimpan "ingatan" cerita: kejadian apa saja yang sudah terjadi (flag) dan
 * apa tujuan pemain sekarang. Modul lain hanya membaca lewat {@link FlagQuery}
 * dan diberi tahu lewat {@link StoryListener}.
 */
public class StoryManager implements FlagQuery {

    private final ObjectSet<String> flags = new ObjectSet<>();
    private final Array<StoryListener> listeners = new Array<>();
    private String objective = "";

    // ------------------------------------------------------------------ flag

    @Override
    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    /** @return true bila flag baru (belum pernah di-set). */
    public boolean setFlag(String flag) {
        if (flag == null || flag.isBlank()) return false;
        boolean added = flags.add(flag);
        if (added) {
            for (StoryListener l : listeners) l.onFlagChanged(flag, true);
        }
        return added;
    }

    public boolean clearFlag(String flag) {
        boolean removed = flags.remove(flag);
        if (removed) {
            for (StoryListener l : listeners) l.onFlagChanged(flag, false);
        }
        return removed;
    }

    public int getFlagCount() {
        return flags.size;
    }

    // ------------------------------------------------------------------ objective

    public void setObjective(String objective) {
        String value = objective == null ? "" : objective;
        if (value.equals(this.objective)) return;
        this.objective = value;
        for (StoryListener l : listeners) l.onObjectiveChanged(value);
    }

    public String getObjective() {
        return objective;
    }

    // ------------------------------------------------------------------ observer

    public void addListener(StoryListener listener) {
        if (!listeners.contains(listener, true)) listeners.add(listener);
    }

    public void removeListener(StoryListener listener) {
        listeners.removeValue(listener, true);
    }
}
