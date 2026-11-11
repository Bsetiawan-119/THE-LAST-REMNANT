// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : InteractionContext.java
//  Bagian  : Integrasi (interface antarmodul)
//  PJ      : Tim  -  diimplementasi oleh GameScreen (Setiawan)
//  Minggu  : W2
// =============================================================================
package com.kelompok7.lastremnant.entity;

import com.kelompok7.lastremnant.dialogue.Dialogue;
import com.kelompok7.lastremnant.memory.JournalEntry;
import com.kelompok7.lastremnant.memory.MemoryDevice;
import com.kelompok7.lastremnant.memory.MemoryEcho;
import com.kelompok7.lastremnant.story.StoryManager;

/**
 * Apa saja yang boleh diakses sebuah Interactable saat berinteraksi.
 * NPC/MemoryObject tidak mengenal GameScreen secara langsung, hanya interface ini,
 * sehingga modul Carlos & Insan tidak bergantung pada modul UI Setiawan.
 */
public interface InteractionContext {

    Player getPlayer();

    StoryManager getStory();

    MemoryDevice getMemoryDevice();

    /** Mulai dialog dari naskah JSON berdasarkan id. */
    void startDialogue(String dialogueId, Runnable onFinished);

    /** Mulai dialog yang dibuat langsung di kode. */
    void startDialogue(Dialogue dialogue, Runnable onFinished);

    /** Diberi tahu setelah The Remnant berhasil membaca gema memori baru. */
    void onEchoScanned(MemoryEcho echo, JournalEntry entry);
}
