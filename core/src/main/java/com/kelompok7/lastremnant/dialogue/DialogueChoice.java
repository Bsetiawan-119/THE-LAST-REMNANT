// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : DialogueChoice.java
//  Bagian  : Dialogue & story (pilihan pemain)
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W2
// =============================================================================
package com.kelompok7.lastremnant.dialogue;

import com.badlogic.gdx.utils.Array;

/** Satu opsi jawaban. {@code nextId == null} berarti dialog selesai setelah dipilih. */
public final class DialogueChoice {

    private final String text;
    private final String nextId;
    private final String condition;
    private final Array<DialogueAction> actions;

    public DialogueChoice(String text, String nextId, String condition, Array<DialogueAction> actions) {
        this.text = text;
        this.nextId = nextId;
        this.condition = condition;
        this.actions = actions != null ? actions : new Array<>();
    }

    public String getText() {
        return text;
    }

    public String getNextId() {
        return nextId;
    }

    public String getCondition() {
        return condition;
    }

    public Array<DialogueAction> getActions() {
        return actions;
    }
}
