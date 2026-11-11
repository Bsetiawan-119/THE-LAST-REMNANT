// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : FlagQuery.java
//  Bagian  : Dialogue & story (interface antarmodul)
//  PJ      : Carlos Ricardo Dwi Saputra
//  Minggu  : W1 (disepakati sebagai interface), W2
// =============================================================================
package com.kelompok7.lastremnant.dialogue;

/**
 * Sumber "flag" cerita. Sistem dialog hanya bergantung ke interface ini,
 * bukan ke StoryManager langsung, sehingga bisa diuji dengan data palsu.
 */
@FunctionalInterface
public interface FlagQuery {
    boolean hasFlag(String flag);
}
