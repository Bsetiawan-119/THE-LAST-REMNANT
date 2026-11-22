package com.kelompok7.lastremnant.world;

import com.kelompok7.lastremnant.util.Constants;

/** Peta yang dapat dimuat oleh alur prolog dan Stage 1. */
public enum StageArea {
    PROLOGUE(Constants.TEST_MAP),
    COCOON_CITY("maps/cocoon_city.tmx"),
    HOUSE_17("maps/house_17.tmx"),
    HIDEOUT("maps/exit_hideout.tmx");

    private final String mapPath;

    StageArea(String mapPath) {
        this.mapPath = mapPath;
    }

    public String mapPath() {
        return mapPath;
    }
}
