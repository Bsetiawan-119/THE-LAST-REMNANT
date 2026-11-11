// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : UiStyles.java
//  Bagian  : UI & game flow (palet warna, font, panel - satu gaya untuk semua layar)
//  PJ      : Budiman Setiawan
//  Minggu  : W2
//  LibGDX  : Scene2D (LabelStyle, NinePatchDrawable)
// =============================================================================
package com.kelompok7.lastremnant.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.kelompok7.lastremnant.assets.GameAssets;

/**
 * Gaya visual UI dibuat lewat kode (tanpa file skin JSON), supaya semua layar
 * konsisten dan aset final Carlos cukup menimpa panel.png / font.png.
 */
public final class UiStyles {

    // Palet UI: krem kertas, tinta malam, aksen sian "memori", kuning lampu jalan
    public static final Color CREAM = new Color(0xECDEBEFF);
    public static final Color DIM = new Color(0x9A92A8FF);
    public static final Color INK = new Color(0x161422FF);
    public static final Color ACCENT = new Color(0x5CE6DCFF);
    public static final Color WARM = new Color(0xFAD26EFF);
    public static final Color ECHO = new Color(0xC9B8FFFF);

    public final BitmapFont font;
    /** Putih polos: dipakai label yang warnanya diatur lewat markup "[#RRGGBB]". */
    public final Label.LabelStyle plain;
    public final Label.LabelStyle body;
    public final Label.LabelStyle dim;
    public final Label.LabelStyle accent;
    public final Label.LabelStyle warm;
    public final Label.LabelStyle ink;
    public final Label.LabelStyle echo;

    public final NinePatchDrawable panel;
    public final NinePatchDrawable tag;
    public final TextureRegionDrawable cursor;
    public final TextureRegionDrawable pixel;
    public final TextureRegionDrawable slotEmpty;
    public final TextureRegionDrawable slotFull;
    public final TextureRegionDrawable indicator;
    public final TextureRegionDrawable vignette;
    public final TextureRegionDrawable remnantIcon;

    public UiStyles(GameAssets assets) {
        font = assets.font();
        plain = new Label.LabelStyle(font, Color.WHITE);
        body = new Label.LabelStyle(font, CREAM);
        dim = new Label.LabelStyle(font, DIM);
        accent = new Label.LabelStyle(font, ACCENT);
        warm = new Label.LabelStyle(font, WARM);
        ink = new Label.LabelStyle(font, INK);
        echo = new Label.LabelStyle(font, ECHO);

        panel = new NinePatchDrawable(new NinePatch(assets.texture("panel"), 4, 4, 4, 4));
        tag = new NinePatchDrawable(new NinePatch(assets.texture("tag"), 3, 3, 3, 3));
        cursor = new TextureRegionDrawable(assets.region("cursor"));
        pixel = new TextureRegionDrawable(assets.region("pixel"));
        slotEmpty = new TextureRegionDrawable(assets.region("slot_empty"));
        slotFull = new TextureRegionDrawable(assets.region("slot_full"));
        indicator = new TextureRegionDrawable(assets.region("indicator"));
        vignette = new TextureRegionDrawable(assets.region("vignette"));
        remnantIcon = new TextureRegionDrawable(assets.region("remnant_icon"));
    }

    /**
     * Font memakai markup warna ("[#RRGGBB]"). Teks dari naskah harus di-escape agar
     * karakter '[' tidak dianggap tag, mis. "[E] Bicara".
     */
    public static String escape(String text) {
        return text == null ? "" : text.replace("[", "[[");
    }

    /** Kode markup warna, mis. hex(WARM) -> "[#FAD26EFF]". */
    public static String hex(Color c) {
        return "[#" + c.toString() + "]";
    }

    public Label label(String text, Label.LabelStyle style) {
        return new Label(escape(text), style);
    }

    /** Persegi warna polos (tinta, tirai fade, garis pemisah). */
    public Drawable solid(Color color) {
        return pixel.tint(color);
    }

    /** Tirai hitam layar penuh untuk transisi fade. Mulai dalam keadaan buram. */
    public Image fadeCurtain() {
        Image curtain = new Image(solid(Color.BLACK));
        curtain.setFillParent(true);
        curtain.setTouchable(Touchable.disabled);
        return curtain;
    }
}
