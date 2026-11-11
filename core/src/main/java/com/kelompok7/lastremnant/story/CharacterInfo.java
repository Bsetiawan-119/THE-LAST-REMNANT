// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : CharacterInfo.java
//  Bagian  : Dialogue & story (data tokoh)
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W2
// =============================================================================
package com.kelompok7.lastremnant.story;

import com.badlogic.gdx.utils.Array;
import com.kelompok7.lastremnant.dialogue.Conditions;
import com.kelompok7.lastremnant.dialogue.FlagQuery;

/**
 * Data satu tokoh / pembicara: nama tampil, sprite, warna suara, dan aturan
 * dialog mana yang dipakai sesuai progres cerita.
 */
public final class CharacterInfo {

    /** "Jika syarat terpenuhi, pakai dialog ini". Dicek berurutan dari atas. */
    public static final class DialogueRule {
        public final String condition;
        public final String dialogueId;

        public DialogueRule(String condition, String dialogueId) {
            this.condition = condition;
            this.dialogueId = dialogueId;
        }
    }

    /** Gaya teks di DialogueUI: biasa, kilas balik memori, suara mesin, narasi. */
    public enum Style { NORMAL, ECHO, SYSTEM, NARRATION }

    private final String id;
    private final String name;
    private final String spriteKey;
    private final String portraitKey;
    private final float voicePitch;
    private final Style style;
    private final Array<DialogueRule> rules;

    public CharacterInfo(String id, String name, String spriteKey, String portraitKey,
                         float voicePitch, Style style, Array<DialogueRule> rules) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.spriteKey = spriteKey;
        this.portraitKey = portraitKey;
        this.voicePitch = voicePitch;
        this.style = style == null ? Style.NORMAL : style;
        this.rules = rules != null ? rules : new Array<>();
    }

    /** Dialog pertama yang syaratnya terpenuhi, atau null bila tidak ada. */
    public String pickDialogue(FlagQuery flags) {
        for (DialogueRule r : rules) {
            if (Conditions.test(r.condition, flags)) return r.dialogueId;
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpriteKey() {
        return spriteKey;
    }

    /** Kunci potret di DialogueUI. Default: sama dengan sprite karakter. */
    public String getPortraitKey() {
        return portraitKey != null ? portraitKey : spriteKey;
    }

    public float getVoicePitch() {
        return voicePitch;
    }

    public Style getStyle() {
        return style;
    }

    public Array<DialogueRule> getRules() {
        return rules;
    }
}
