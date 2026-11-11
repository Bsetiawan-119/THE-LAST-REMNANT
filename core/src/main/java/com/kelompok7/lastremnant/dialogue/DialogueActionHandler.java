// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : DialogueActionHandler.java
//  Bagian  : Dialogue & story (interface antarmodul)
//  PJ      : Carlos Ricardo Dwi Saputra (implementasinya di GameScreen milik Setiawan)
//  Minggu  : W2
// =============================================================================
package com.kelompok7.lastremnant.dialogue;

/** Penerima efek dialog. Diimplementasikan oleh GameScreen (titik integrasi). */
@FunctionalInterface
public interface DialogueActionHandler {
    void onDialogueAction(DialogueAction action);
}
