// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Lwjgl3Launcher.java
//  Bagian  : Integrasi & setup repo (launcher desktop)
//  PJ      : Tim (setup W1)
//  Minggu  : W1
//  LibGDX  : LWJGL3 backend
// =============================================================================
package com.kelompok7.lastremnant.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.kelompok7.lastremnant.LastRemnantGame;
import com.kelompok7.lastremnant.util.Constants;

/** Jalankan: ./gradlew lwjgl3:run */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        new Lwjgl3Application(new LastRemnantGame(), createConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration createConfiguration() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(Constants.TITLE);
        config.useVsync(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        config.setWindowedMode(1280, 720);
        config.setWindowSizeLimits(640, 360, -1, -1);
        config.setWindowIcon("sprites/remnant_icon.png");
        return config;
    }
}
