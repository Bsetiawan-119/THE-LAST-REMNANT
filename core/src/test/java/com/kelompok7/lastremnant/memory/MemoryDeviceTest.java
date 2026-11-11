// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Unit test MemoryDevice
//  PJ : Azzam Insan Kamil  ·  Minggu : W2
// =============================================================================
package com.kelompok7.lastremnant.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class MemoryDeviceTest {

    private Journal journal;
    private MemoryDevice device;
    private MemoryEcho radio;

    @Before
    public void setUp() {
        journal = new Journal();
        device = new MemoryDevice(journal);
        radio = new MemoryEcho("radio", "Radio tua", "obj_radio", "inspect_radio", "echo_radio",
                "Lagu di radio", "Isi catatan");
    }

    @Test(expected = IllegalStateException.class)
    public void tidakBisaMemindaiSebelumAktif() {
        device.scan(radio);
    }

    @Test
    public void memindaiMenambahEntriJurnal() {
        device.activate();
        JournalEntry e = device.scan(radio);
        assertNotNull(e);
        assertEquals(JournalEntry.Category.ECHO, e.getCategory());
        assertEquals("Lagu di radio", e.getTitle());
        assertEquals(1, journal.size());
        assertTrue(device.hasScanned("radio"));
        assertEquals(1, device.getScannedCount());
    }

    @Test
    public void gemaYangSamaTidakDicatatDuaKali() {
        device.activate();
        device.scan(radio);
        assertNull(device.scan(radio));
        assertEquals(1, journal.size());
    }

    @Test
    public void tigaFragmenMembuatAlatLengkap() {
        device.activate();
        assertFalse(device.isComplete());
        assertTrue(device.addFragment(new MemoryFragment("f1", "Satu", "...")));
        assertTrue(device.addFragment(new MemoryFragment("f2", "Dua", "...")));
        assertFalse(device.addFragment(new MemoryFragment("f2", "Dua lagi", "...")));   // dobel
        assertTrue(device.addFragment(new MemoryFragment("f3", "Tiga", "...")));
        assertTrue(device.isComplete());
        assertFalse(device.addFragment(new MemoryFragment("f4", "Empat", "...")));      // penuh
        assertEquals(3, device.getFragmentCount());
        assertEquals(3, journal.size());
    }

    @Test
    public void fragmenDitolakSaatAlatMati() {
        assertFalse(device.addFragment(new MemoryFragment("f1", "Satu", "...")));
        assertEquals(0, device.getFragmentCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void kapasitasHarusPositif() {
        new MemoryDevice(journal, 0);
    }
}
