// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Conditions.java
//  Bagian  : Dialogue & story (syarat cabang cerita)
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W2
// =============================================================================
package com.kelompok7.lastremnant.dialogue;

/**
 * Evaluasi syarat sederhana yang ditulis di file JSON naskah.
 * <pre>
 *   "has_remnant"                  -> flag harus ada
 *   "!met_sari"                    -> flag TIDAK boleh ada
 *   "scanned_kite,scanned_photo"   -> semua syarat (AND)
 *   null / ""                      -> selalu benar
 * </pre>
 */
public final class Conditions {

    private Conditions() {
    }

    public static boolean test(String expression, FlagQuery flags) {
        if (expression == null || expression.isBlank()) return true;
        for (String raw : expression.split(",")) {
            String term = raw.trim();
            if (term.isEmpty()) continue;
            boolean negate = term.startsWith("!");
            String flag = negate ? term.substring(1).trim() : term;
            if (flags.hasFlag(flag) == negate) return false;
        }
        return true;
    }
}
