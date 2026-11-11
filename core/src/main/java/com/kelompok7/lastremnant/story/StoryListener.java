// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : StoryListener.java
//  Bagian  : Dialogue & story
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W2
//  Pattern : OBSERVER (interface pengamat). HUD milik Setiawan mendengarkan ini.
// =============================================================================
package com.kelompok7.lastremnant.story;

/** Pengamat perubahan cerita. Semua method default supaya pengamat cukup override yang perlu. */
public interface StoryListener {

    default void onFlagChanged(String flag, boolean value) {
    }

    default void onObjectiveChanged(String objective) {
    }
}
