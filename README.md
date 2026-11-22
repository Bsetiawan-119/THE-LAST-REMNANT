# THE LAST REMNANT

Game petualangan aksi dan teka-teki 2D dengan gaya pixel art untuk desktop. Proyek akhir Pemrograman Berorientasi Objek, **Kelompok 7**, dikembangkan dari proposal **Sebelum PIKUN!**.

Zentra mengantar paket tanpa pengirim di Depok Selatan. Di dalamnya ada **The Remnant**, alat yang membaca gema memori dari benda. Radio Pak Darto, foto Bu Sari, dan layangan putus memperlihatkan jejak aplikasi **Brown Teddy**. Saat Bu Sari berteriak **BANGUN**, Zentra terjaga di Kota Kepompong dan mulai mencari tahu apa yang terjadi pada Depok.

## Target pencapaian W1–W5

| Minggu | Target Gantt | Status |
|---|---|---|
| W1 · 4–10 Nov | Siapkan repositori, antarmuka modul, naskah awal, gerak pemain, dan collision. | ✅ Fondasi tersedia |
| **W2 · 11–17 Nov** | **Sistem dialog, MemoryDevice dan jurnal, MainMenu dan GameScreen, integrasi, serta unit test. Milestone: 17 November 2026.** | ✅ **Prototype bisa dimainkan** |
| W3 · 18–24 Nov | Adegan BANGUN, peta Kota Kepompong, combat, inventori, rumah No. 17, Amulet Kenang, dan akses Perpustakaan Arsip. | 🟡 Demo tersedia. Uji desktop menyusul. |
| W4 · 25 Nov–1 Des | Perpustakaan Arsip dan teka-teki, Amulet Sesal, Sang Exitium dan Amulet Rindu, tiga ending, menu jeda, serta checkpoint fase Rindu. | ⏳ Direncanakan |
| W5 · 2–8 Des | Uji ulang alur dan cabang ending, sesuaikan tingkat kesulitan, perbaiki bug, dan siapkan rilis internal. | ⏳ Direncanakan |

**Isi prototype W2:** menu utama → intro dan eksplorasi dengan collision → dialog bercabang dengan Bu Sari → antar paket ke Pak Darto → The Remnant aktif → pindai radio, foto, dan layangan → lihat tiga gema memori dan catat di jurnal → percakapan penutup dengan Bu Sari.

## Fitur dan status

| Bagian | Isi | Status paket ini |
|---|---|---|
| **Prolog** | Paket Pak Darto, dialog bercabang, tiga gema memori, jurnal, dan adegan BANGUN | Tersedia |
| **Stage 1: Kota Kepompong** | Pisau dan pistol, patroli dan pengejaran, rumah No. 17, bersembunyi di lemari, ruang rahasia, rekaman TV, serta **Amulet Kenang** | Tersedia untuk demo W3 dengan peta dan sprite sementara |
| **Stage 2: Perpustakaan Arsip** | Enam ruangan dan hub, empat teka-teki urutan, kunci, arsip Proyek EXIT, serta **Amulet Sesal** di brankas | W4. Demo W3 berakhir di akses menuju perpustakaan |
| **Stage 3: Jantung Kalpataru** | Pertarungan tiga fase melawan Sang Exitium, **Amulet Rindu**, dan satu permintaan | W4 |

Setelah mendapat pisau, Zentra bisa membawa dua senjata tambahan. Senjata yang dibuang tertinggal di lantai dengan sisa pelurunya. The Remnant mencatat gema sebagai entri jurnal dan menyimpan **Kenang, Sesal, dan Rindu** sebagai tiga bagian kunci Kalpataru. Memindai tiga benda di prolog tidak mengisi slot amulet.

<details>
<summary>Spoiler naskah final dan tiga ending</summary>

Zentra menciptakan Sang Exitium dalam Proyek EXIT, yang merilis Brown Teddy sebagai aplikasi pemulihan ingatan. Pria bertopeng di TV adalah Zentra sendiri. Rekannya, Kruk, terperangkap di inti Sang Exitium dan menyuarakan panggilan terakhir melalui Bu Sari. Zentra yang dimainkan adalah Z-02, rekonstruksi yang dibuat Kruk dari sisa ingatan Zentra asli. Dari sinilah judul **The Last Remnant** berasal.

Kalpataru adalah mesin berusia 100 abad yang mengabulkan satu permintaan dari kesadaran manusia setelah tiga amulet disatukan.

| Ending | Hasil |
|---|---|
| **Bangun** | Semua orang bangun dengan ingatan utuh. Mereka tetap mengingat peran Zentra dalam bencana. Pilihan dialog `maaf` menambah adegan epilog. |
| **Yang Terlupakan** | Semua orang bangun. Zentra dan Kruk hilang dari ingatan dunia. |
| **Mimpi Abadi** | Ilusi menjadi permanen. Pilihan ini tersedia jika pemain mengaktifkan `ragu` saat berdialog dengan Sang Exitium. |

</details>

## Menjalankan game

Siapkan **JDK 17 atau lebih baru**. Proyek memakai **LibGDX 1.13.1**, backend desktop **LWJGL3**, dan Gradle Wrapper. Unduhan dependensi dibutuhkan saat build pertama.

```powershell
# Windows PowerShell
.\gradlew.bat lwjgl3:run
.\gradlew.bat core:test
.\gradlew.bat lwjgl3:jar
```

```bash
# Linux / macOS
bash gradlew lwjgl3:run
bash gradlew core:test
bash gradlew lwjgl3:jar
```

| Tombol | Aksi |
|---|---|
| WASD / panah | Bergerak |
| E / Enter | Bicara, memeriksa, memindai, mengambil barang atau melanjutkan dialog. E juga dapat menusuk Boneka dari belakang saat pisau aktif dan prompt muncul. |
| W/S lalu E, 1–4, atau klik | Memilih jawaban dialog |
| F / klik kiri | Serang mengikuti arah hadap / arah klik di Stage 1 |
| R / Q / G | Isi ulang / ganti senjata / buang senjata aktif |
| Tahan Spasi | Tahan napas saat bersembunyi di lemari |
| J / Tab | Buka atau tutup jurnal |
| Esc / F11 | Kembali ke menu saat eksplorasi / beralih layar penuh |

## Desain OOP

| Pattern | Penerapan |
|---|---|
| **Command** | `InputHandler` meneruskan aksi ke `Command`. Di `CombatState`, F, R, Q, dan G terhubung ke operasi combat dan inventori. |
| **Observer** | `StoryManager` dan `Journal` memberi tahu HUD saat objektif atau entri berubah. `Health`, `Ammo`, dan `Inventory` memberi tahu HUD saat kondisi combat berubah. Catatan amulet dibuat sekali saat koleksi bertambah. |
| **State** | `GameStateManager` mengatur eksplorasi, dialog, jurnal, transisi BANGUN, dan combat. `EnemyState` mengatur patroli, mengejar, menyerang, dan stun. |

`Weapon` dan `Enemy` menyediakan kontrak bersama untuk kelas turunannya. `Inventory`, `Health`, `Ammo`, dan `MemoryDevice` menyediakan operasi untuk mengubah isi slot, HP, peluru, dan koleksi. `Interactable` dipakai oleh NPC, benda memori, senjata di lantai, lemari, dan objek Stage 1.

## Struktur proyek

| Folder | Isi |
|---|---|
| `assets/data/` | Dialog, data tokoh, gema memori, dan catatan jurnal |
| `assets/maps/` | Peta prolog dan tiga peta Stage 1 dalam format Tiled TMX |
| `assets/sprites/` | Sprite sementara. Sheet karakter memakai frame 16 × 24 piksel, dengan empat arah. |
| `core/src/main/java/com/kelompok7/lastremnant/` | Logika game, combat, stage, input, memori, state, dan UI |
| `core/src/test/java/` | Pengujian logika, state, collision, dan integritas data cerita |
| `lwjgl3/` | Launcher desktop |

## Pembagian tugas W2–W3

| Anggota | Revisi W2 | Implementasi W3 |
|---|---|---|
| **Iqbal Aulia Rachman** | Definisi peta per area dan pemeriksaan garis pandang | Peta Kota Kepompong, rumah No. 17, dan ruang rahasia. Weapon, ammo, pickup, serta inventori dua slot dan pisau. |
| **Carlos Ricardo Dwi Saputra** | Nama Zentra dan petunjuk 16.40, huruf K, serta dialog Pak Darto | Dialog BANGUN dan TV, catatan Stage 1, serta sprite Zentra, Boneka, dan Kruk. |
| **Azzam Insan Kamil** | Objektif prolog berdasarkan tiga gema yang wajib dipindai | Boneka Penjaga dan Pemburu, perilaku musuh, Health, serta koleksi amulet di MemoryDevice. |
| **Budiman Setiawan** | Pemuatan area, sambungan setelah dialog, dan judul jurnal | CombatState, HUD combat, animasi BANGUN, serta integrasi transisi sampai pintu perpustakaan. |

Rujukan cerita dan pembagian pekerjaan: *The Last Remnant Naskah Cerita Lengkap* (hlm. 5–20) serta *Project Management Plan & Proposal* revisi.
