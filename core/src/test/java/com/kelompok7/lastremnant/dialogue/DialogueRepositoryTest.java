// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Unit test DialogueRepository (validasi naskah)
//  PJ : Carlos Ricardo Dwi Saputra  ·  Minggu : W2
// =============================================================================
package com.kelompok7.lastremnant.dialogue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DialogueRepositoryTest {

    @Test
    public void memuatDialogSederhana() {
        DialogueRepository repo = DialogueRepository.fromJson(
                "{ dialogues: [ { id: hi, nodes: [ { id: a, speaker: iwan, text: Halo } ] } ] }");
        assertTrue(repo.has("hi"));
        assertEquals("a", repo.get("hi").getStartId());   // start default = node pertama
        assertEquals("narrator", DialogueRepository.fromJson(
                "{ dialogues: [ { id: n, nodes: [ { id: a, text: x } ] } ] }").get("n").getNode("a").getSpeakerId());
    }

    @Test(expected = IllegalStateException.class)
    public void nextYangSalahKetahuanSaatDimuat() {
        DialogueRepository.fromJson(
                "{ dialogues: [ { id: hi, nodes: [ { id: a, text: Halo, next: typo } ] } ] }");
    }

    @Test(expected = IllegalStateException.class)
    public void pilihanKeNodeTidakAdaKetahuan() {
        DialogueRepository.fromJson("{ dialogues: [ { id: hi, nodes: ["
                + "{ id: a, text: Halo, choices: [ { text: ok, next: zzz } ] } ] } ] }");
    }

    @Test(expected = IllegalArgumentException.class)
    public void idDialogDobelDitolak() {
        DialogueRepository.fromJson("{ dialogues: ["
                + "{ id: hi, nodes: [ { id: a, text: x } ] },"
                + "{ id: hi, nodes: [ { id: a, text: y } ] } ] }");
    }

    @Test(expected = IllegalArgumentException.class)
    public void idNodeDobelDitolak() {
        DialogueRepository.fromJson("{ dialogues: [ { id: hi, nodes: [ { id: a, text: x }, { id: a, text: y } ] } ] }");
    }

    @Test(expected = IllegalArgumentException.class)
    public void dialogTidakAdaMelemparError() {
        DialogueRepository.fromJson("{ dialogues: [] }").get("tidak_ada");
    }
}
