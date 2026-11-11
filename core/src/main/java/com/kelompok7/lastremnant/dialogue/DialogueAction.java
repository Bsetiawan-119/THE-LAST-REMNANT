// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : DialogueAction.java
//  Bagian  : Dialogue & story (efek samping dari dialog)
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W2
// =============================================================================
package com.kelompok7.lastremnant.dialogue;

/**
 * Efek yang dipicu saat sebuah baris dialog muncul atau pilihan diambil.
 * Ditulis di JSON sebagai "tipe:nilai", contoh:
 * <pre>
 *   "flag:met_sari"            set flag cerita
 *   "unflag:met_sari"          hapus flag
 *   "objective:Antar paket"    ganti tujuan di HUD
 *   "device:on"                aktifkan The Remnant
 *   "note:delivery"            tambah catatan jurnal
 *   "sfx:scan"                 putar efek suara
 *   "toast:Teks singkat"       notifikasi singkat di layar
 * </pre>
 * Siapa yang menjalankan efeknya diputuskan oleh {@link DialogueActionHandler}.
 */
public final class DialogueAction {

    private final String type;
    private final String value;

    public DialogueAction(String type, String value) {
        if (type == null || type.isBlank()) throw new IllegalArgumentException("tipe aksi kosong");
        this.type = type.trim();
        this.value = value == null ? "" : value.trim();
    }

    public static DialogueAction parse(String raw) {
        int sep = raw.indexOf(':');
        if (sep < 0) return new DialogueAction(raw, "");
        return new DialogueAction(raw.substring(0, sep), raw.substring(sep + 1));
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return type + ":" + value;
    }
}
