// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : DialogueNode.java
//  Bagian  : Dialogue & story (satu baris dialog)
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W2
// =============================================================================
package com.kelompok7.lastremnant.dialogue;

import com.badlogic.gdx.utils.Array;

/**
 * Satu "kotak" di pohon dialog: siapa yang bicara, apa katanya, lalu ke mana.
 * Jika punya pilihan, pemain harus memilih; jika tidak, lanjut ke {@code nextId}.
 */
public final class DialogueNode {

    private final String id;
    private final String speakerId;
    private final String text;
    private final String nextId;
    private final Array<DialogueChoice> choices;
    private final Array<DialogueAction> actions;

    public DialogueNode(String id, String speakerId, String text, String nextId,
                        Array<DialogueChoice> choices, Array<DialogueAction> actions) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id node kosong");
        this.id = id;
        this.speakerId = speakerId == null ? "narrator" : speakerId;
        this.text = text == null ? "" : text;
        this.nextId = nextId;
        this.choices = choices != null ? choices : new Array<>();
        this.actions = actions != null ? actions : new Array<>();
    }

    public String getId() {
        return id;
    }

    public String getSpeakerId() {
        return speakerId;
    }

    public String getText() {
        return text;
    }

    public String getNextId() {
        return nextId;
    }

    public Array<DialogueChoice> getChoices() {
        return choices;
    }

    public Array<DialogueAction> getActions() {
        return actions;
    }

    public boolean hasChoices() {
        return choices.size > 0;
    }
}
