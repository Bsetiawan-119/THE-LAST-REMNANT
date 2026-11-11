// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : DialogueSession.java
//  Bagian  : Dialogue & story (mesin penjalan dialog saat runtime)
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W2
//  Test    : core/src/test/.../dialogue/DialogueSessionTest.java
// =============================================================================
package com.kelompok7.lastremnant.dialogue;

import com.badlogic.gdx.utils.Array;

/**
 * Menjalankan satu {@link Dialogue}: menyimpan posisi node sekarang, menyaring
 * pilihan berdasarkan flag, dan mengirim aksi ke {@link DialogueActionHandler}.
 * <p>
 * Kelas ini murni logika (tanpa grafis). Tampilan diurus DialogueUI (Setiawan).
 */
public class DialogueSession {

    private final Dialogue dialogue;
    private final FlagQuery flags;
    private final DialogueActionHandler handler;
    private final Array<DialogueChoice> visibleChoices = new Array<>();

    private DialogueNode current;
    private boolean started;
    private boolean finished;

    public DialogueSession(Dialogue dialogue, FlagQuery flags, DialogueActionHandler handler) {
        this.dialogue = dialogue;
        this.flags = flags != null ? flags : f -> false;
        this.handler = handler != null ? handler : a -> { };
    }

    /** Masuk ke node awal. Aman dipanggil sekali saja. */
    public void start() {
        if (started) return;
        started = true;
        enter(dialogue.getStartId());
    }

    /** Lanjut ke baris berikutnya. Diabaikan bila pemain sedang harus memilih. */
    public boolean advance() {
        if (finished || current == null || isWaitingForChoice()) return false;
        enter(current.getNextId());
        return true;
    }

    /**
     * Ambil pilihan ke-{@code index} dari {@link #getVisibleChoices()}.
     * @return false bila index tidak valid
     */
    public boolean choose(int index) {
        if (!isWaitingForChoice() || index < 0 || index >= visibleChoices.size) return false;
        DialogueChoice choice = visibleChoices.get(index);
        fire(choice.getActions());
        enter(choice.getNextId());
        return true;
    }

    private void enter(String nodeId) {
        visibleChoices.clear();
        if (nodeId == null) {
            current = null;
            finished = true;
            return;
        }
        current = dialogue.getNode(nodeId);
        if (current == null) {
            throw new IllegalStateException("Node '" + nodeId + "' tidak ditemukan di dialog '" + dialogue.getId() + "'");
        }
        fire(current.getActions());
        for (DialogueChoice c : current.getChoices()) {
            if (Conditions.test(c.getCondition(), flags)) visibleChoices.add(c);
        }
    }

    private void fire(Array<DialogueAction> actions) {
        for (DialogueAction a : actions) handler.onDialogueAction(a);
    }

    // ------------------------------------------------------------------ getter

    /** True bila node sekarang punya pilihan yang lolos syarat. */
    public boolean isWaitingForChoice() {
        return current != null && visibleChoices.size > 0;
    }

    public Array<DialogueChoice> getVisibleChoices() {
        return visibleChoices;
    }

    public DialogueNode getCurrentNode() {
        return current;
    }

    public boolean isFinished() {
        return finished;
    }

    public Dialogue getDialogue() {
        return dialogue;
    }
}
