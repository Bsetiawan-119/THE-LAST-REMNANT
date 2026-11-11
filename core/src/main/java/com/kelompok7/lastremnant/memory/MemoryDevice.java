// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : MemoryDevice.java
//  Bagian  : Puzzle, memory, quest ("The Remnant", alat pembaca memori)
//  PJ      : Azzam Insan Kamil
//  Minggu  : W2 = aktivasi, pindai gema, slot 3 fragmen
//            W3 = dipakai Puzzle & QuestManager untuk menyimpan fragmen
//  OOP     : ENCAPSULATION (status alat hanya berubah lewat method di bawah)
//  Test    : core/src/test/.../memory/MemoryDeviceTest.java
// =============================================================================
package com.kelompok7.lastremnant.memory;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.kelompok7.lastremnant.util.Constants;

/**
 * Perangkat yang ditemukan Iwan di dalam paket. Setelah aktif, bisa membaca
 * jejak memori dari benda (gema) dan menyimpan fragmen memori.
 * Semua hasil bacaan otomatis masuk ke {@link Journal}.
 */
public class MemoryDevice {

    private final Journal journal;
    private final ObjectSet<String> scanned = new ObjectSet<>();
    private final Array<MemoryFragment> fragments = new Array<>();
    private final int capacity;
    private boolean active;

    public MemoryDevice(Journal journal) {
        this(journal, Constants.FRAGMENTS_NEEDED);
    }

    public MemoryDevice(Journal journal, int fragmentCapacity) {
        if (journal == null) throw new IllegalArgumentException("journal wajib ada");
        if (fragmentCapacity <= 0) throw new IllegalArgumentException("kapasitas harus > 0");
        this.journal = journal;
        this.capacity = fragmentCapacity;
    }

    public void activate() {
        active = true;
    }

    public boolean isActive() {
        return active;
    }

    // ------------------------------------------------------------------ gema

    public boolean hasScanned(String echoId) {
        return scanned.contains(echoId);
    }

    /**
     * Baca gema memori dari sebuah benda dan catat di jurnal.
     *
     * @return entri jurnal baru, atau null bila gema ini sudah pernah dipindai
     * @throws IllegalStateException bila alat belum aktif
     */
    public JournalEntry scan(MemoryEcho echo) {
        if (!active) throw new IllegalStateException("The Remnant belum aktif");
        if (!scanned.add(echo.getId())) return null;
        JournalEntry entry = new JournalEntry("echo_" + echo.getId(), JournalEntry.Category.ECHO,
                echo.getJournalTitle(), echo.getJournalText());
        journal.addEntry(entry);
        return entry;
    }

    public int getScannedCount() {
        return scanned.size;
    }

    // ------------------------------------------------------------------ fragmen

    /**
     * Simpan fragmen memori (dipanggil oleh Puzzle / quest di W3).
     * @return false bila alat penuh, belum aktif, atau fragmen sudah ada
     */
    public boolean addFragment(MemoryFragment fragment) {
        if (!active || isComplete()) return false;
        for (MemoryFragment f : fragments) {
            if (f.getId().equals(fragment.getId())) return false;
        }
        fragments.add(fragment);
        journal.addEntry(new JournalEntry("fragment_" + fragment.getId(), JournalEntry.Category.FRAGMENT,
                fragment.getTitle(), fragment.getText()));
        return true;
    }

    public int getFragmentCount() {
        return fragments.size;
    }

    public int getCapacity() {
        return capacity;
    }

    /** Tiga fragmen terkumpul -> pilihan akhir boleh dibuka (W4). */
    public boolean isComplete() {
        return fragments.size >= capacity;
    }

    public Journal getJournal() {
        return journal;
    }
}
