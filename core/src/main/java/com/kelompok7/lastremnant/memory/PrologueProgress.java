package com.kelompok7.lastremnant.memory;

import java.util.Set;

/** Prolog membutuhkan tiga gema spesifik, terlepas dari jumlah catatan lain. */
public final class PrologueProgress {
    private static final Set<String> REQUIRED = Set.of("radio", "photo", "kite");

    private PrologueProgress() {
    }

    public static String objective(MemoryDevice device) {
        long scanned = REQUIRED.stream().filter(device::hasScanned).count();
        if (scanned == REQUIRED.size()) {
            return "Tunjukkan foto lama dari bangku taman kepada Bu Sari.";
        }
        return "Pindai benda bermakna dengan The Remnant (" + scanned + "/" + REQUIRED.size() + ").";
    }
}
