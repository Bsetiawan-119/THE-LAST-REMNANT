// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : GameAssets.java
//  Bagian  : Aset pixel art & audio (pemuat aset terpusat)
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W2
//  LibGDX  : AssetManager, Texture, BitmapFont, Sound
// =============================================================================
package com.kelompok7.lastremnant.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.kelompok7.lastremnant.entity.CharacterSprites;
import com.kelompok7.lastremnant.util.Constants;

/**
 * Satu-satunya tempat yang memuat & membuang aset. Modul lain meminta aset
 * berdasarkan "kunci" (nama file tanpa ekstensi), jadi mengganti aset sementara
 * dengan aset final cukup menimpa file di folder assets/.
 */
public class GameAssets implements Disposable {

    private static final String[] SPRITES = {
            "iwan", "sari", "darto", "shadow", "glow", "indicator",
            "obj_kite", "obj_photo", "obj_radio", "obj_parcel", "remnant_icon"
    };
    private static final String[] UI = {
            "panel", "tag", "cursor", "slot_empty", "slot_full", "vignette", "pixel",
            "menu_sky", "menu_far", "menu_near"
    };
    private static final String[] SFX = {"blip", "select", "confirm", "scan", "journal"};
    private static final String FONT = "fonts/font.fnt";

    private final AssetManager manager = new AssetManager();
    private final ObjectMap<String, String> paths = new ObjectMap<>();
    private final ObjectMap<String, TextureRegion> regions = new ObjectMap<>();
    private final ObjectMap<String, CharacterSprites> characters = new ObjectMap<>();
    private BitmapFont font;
    private float sfxVolume = 0.7f;

    /** Muat semua aset secara sinkron (aset prototype kecil, < 1 detik). */
    public void loadAll() {
        for (String s : SPRITES) queueTexture(s, "sprites/" + s + ".png");
        for (String s : UI) queueTexture(s, "ui/" + s + ".png");
        for (String s : SFX) {
            String p = "sfx/" + s + ".wav";
            paths.put("sfx:" + s, p);
            manager.load(p, Sound.class);
        }
        manager.load(FONT, BitmapFont.class);
        manager.finishLoading();

        font = manager.get(FONT, BitmapFont.class);
        font.getData().markupEnabled = true;   // dipakai efek mesin ketik di DialogueUI
        font.setUseIntegerPositions(true);
    }

    private void queueTexture(String key, String path) {
        paths.put(key, path);
        manager.load(path, Texture.class);
    }

    // ------------------------------------------------------------------ getter

    public Texture texture(String key) {
        String p = paths.get(key);
        if (p == null) throw new IllegalArgumentException("Aset tidak terdaftar: " + key);
        return manager.get(p, Texture.class);
    }

    /** TextureRegion utuh dari sebuah tekstur (di-cache). */
    public TextureRegion region(String key) {
        TextureRegion r = regions.get(key);
        if (r == null) {
            r = new TextureRegion(texture(key));
            regions.put(key, r);
        }
        return r;
    }

    /** Animasi karakter 4 arah dari spritesheet 16x24 (di-cache per kunci). */
    public CharacterSprites characterSprites(String key) {
        CharacterSprites s = characters.get(key);
        if (s == null) {
            s = new CharacterSprites(texture(key), Constants.CHAR_FRAME_W, Constants.CHAR_FRAME_H,
                    Constants.WALK_FRAME_DURATION, region("shadow"));
            characters.put(key, s);
        }
        return s;
    }

    /** Potret untuk DialogueUI. Karakter = kepala dari sprite; selain itu = gambar utuh. */
    public TextureRegion portrait(String key) {
        if (key == null || !paths.containsKey(key)) return null;
        Texture t = texture(key);
        if (t.getHeight() == Constants.CHAR_FRAME_H * 4) return characterSprites(key).getPortrait();
        return region(key);
    }

    public BitmapFont font() {
        return font;
    }

    public String readData(String file) {
        return Gdx.files.internal("data/" + file).readString("UTF-8");
    }

    // ------------------------------------------------------------------ audio

    public void play(String sfx) {
        play(sfx, 1f, 1f);
    }

    public void play(String sfx, float volume, float pitch) {
        String p = paths.get("sfx:" + sfx);
        if (p == null) {
            Gdx.app.error("GameAssets", "SFX tidak ada: " + sfx);
            return;
        }
        manager.get(p, Sound.class).play(volume * sfxVolume, pitch, 0f);
    }

    public void setSfxVolume(float v) {
        sfxVolume = Math.max(0f, Math.min(1f, v));
    }

    @Override
    public void dispose() {
        manager.dispose();
    }
}
