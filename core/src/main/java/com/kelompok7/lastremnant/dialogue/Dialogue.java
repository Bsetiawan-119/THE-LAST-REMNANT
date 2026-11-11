// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Dialogue.java
//  Bagian  : Dialogue & story (satu percakapan utuh / pohon dialog)
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W2
// =============================================================================
package com.kelompok7.lastremnant.dialogue;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/** Kumpulan {@link DialogueNode} yang saling terhubung, dimulai dari {@code startId}. */
public final class Dialogue {

    private final String id;
    private final String startId;
    private final ObjectMap<String, DialogueNode> nodes = new ObjectMap<>();

    public Dialogue(String id, String startId, Array<DialogueNode> nodeList) {
        this.id = id;
        this.startId = startId;
        for (DialogueNode n : nodeList) {
            if (nodes.containsKey(n.getId())) {
                throw new IllegalArgumentException("Dialog '" + id + "': id node dobel '" + n.getId() + "'");
            }
            nodes.put(n.getId(), n);
        }
    }

    /**
     * Dialog linear sederhana tanpa file JSON (dipakai untuk teks pendek seperti
     * "Jejak memori ini sudah tersimpan").
     */
    public static Dialogue linear(String id, String speakerId, String... lines) {
        Array<DialogueNode> list = new Array<>();
        for (int i = 0; i < lines.length; i++) {
            String next = i + 1 < lines.length ? "l" + (i + 1) : null;
            list.add(new DialogueNode("l" + i, speakerId, lines[i], next, null, null));
        }
        return new Dialogue(id, "l0", list);
    }

    /**
     * Pastikan semua rujukan next valid. Dipanggil saat naskah dimuat agar typo
     * di JSON ketahuan saat game dibuka, bukan di tengah permainan.
     */
    public void validate() {
        if (!nodes.containsKey(startId)) {
            throw new IllegalStateException("Dialog '" + id + "': start '" + startId + "' tidak ada");
        }
        for (DialogueNode n : nodes.values()) {
            check(n.getNextId(), n.getId());
            for (DialogueChoice c : n.getChoices()) {
                check(c.getNextId(), n.getId());
            }
        }
    }

    private void check(String target, String from) {
        if (target != null && !nodes.containsKey(target)) {
            throw new IllegalStateException("Dialog '" + id + "': node '" + from
                    + "' menunjuk ke '" + target + "' yang tidak ada");
        }
    }

    public String getId() {
        return id;
    }

    public String getStartId() {
        return startId;
    }

    public DialogueNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public int size() {
        return nodes.size;
    }
}
