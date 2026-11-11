# Panduan W2 (11–17 Nov) untuk Setiawan

Target milestone: **17 Nov, prototype playable**.  
Semua kode W1 dan W2 sudah ada di repo ini. Yang perlu kamu lakukan sekarang adalah **memasang, menjalankan, membagikan ke tim, dan mendemokan**.

---

## 1. Persiapan laptop (sekali saja)

| Alat | Catatan |
|---|---|
| **JDK 21** (Temurin / OpenJDK) | Cek dengan `java -version` |
| **IntelliJ IDEA Community** | Buka proyek lewat File → Open |
| **Git** | Cek dengan `git --version` |
| Tiled *(opsional)* | Untuk membuka `assets/maps/test_area.tmx` |

## 2. Masukkan kode ke repo GitHub (repo masih kosong)

```bash
git clone https://github.com/Bsetiawan-119/THE-LAST-REMNANT.git
cd THE-LAST-REMNANT
# salin SEMUA isi zip ke folder ini (termasuk .gitignore, .gitattributes, gradlew, folder gradle/)
git add .
git commit -m "chore: setup proyek LibGDX + prototype W2"
git push origin main
```

Setelah itu buat branch untuk tiap anggota:

```bash
git branch feat/iqbal-world      && git push origin feat/iqbal-world
git branch feat/carlos-dialogue  && git push origin feat/carlos-dialogue
git branch feat/insan-memory     && git push origin feat/insan-memory
git branch feat/setiawan-ui      && git push origin feat/setiawan-ui
```

Di GitHub, buka **Settings → Branches → Add rule** untuk `main`, lalu aktifkan *Require a pull request before merging*.  
Undang anggota lain di **Settings → Collaborators**.

## 3. Jalankan & tes

```bash
./gradlew core:test       # harus: BUILD SUCCESSFUL, 62 test lulus
./gradlew lwjgl3:run      # game terbuka di jendela 1280x720
```

Run pertama akan mengunduh LibGDX (±1–2 menit, butuh internet).

**Kalau gagal:**
- `Unsupported class file major version`: JDK terlalu lama, pasang JDK 17 atau 21.
- macOS: jika jendela tidak muncul, pastikan menjalankan lewat `./gradlew lwjgl3:run` (flag `-XstartOnFirstThread` sudah diatur di build).
- File aset tidak ditemukan: jalankan lewat task Gradle `run`, bukan klik kanan `Lwjgl3Launcher`. Task `run` memakai folder `assets/` sebagai working directory.

## 4. Bagian kamu (Setiawan, UI & game flow)

| File | Minggu | Isi |
|---|---|---|
| `LastRemnantGame.java` | W1–W2 | Kelas `Game`, ganti layar dengan aman, F11 layar penuh |
| `screens/MainMenu.java` | **W2** | Menu dengan latar parallax, partikel memori, panel kontrol, fade |
| `screens/GameScreen.java` | **W2** | Layar utama + **titik integrasi** modul semua anggota |
| `state/GameState.java` | W2 | Interface State pattern |
| `state/GameStateManager.java` | W2 | Tumpukan state (push/pop/set) |
| `state/ExplorationState.java` | W2 | Jalan, cari target, interaksi |
| `state/DialogueState.java` | W2 | Dialog sedang berjalan |
| `state/JournalState.java` | W2 | Jurnal terbuka, dunia berhenti |
| `ui/UiStyles.java` | W2 | Palet warna, font, panel |
| `ui/Hud.java` | W2 | Tujuan, status The Remnant, petunjuk [E], toast (**Observer**) |
| `ui/DialogueUI.java` | W2 *(versi awal)* | Efek mesin ketik, potret, pilihan; dipoles di W3 |
| `ui/JournalUI.java` | W2 *(versi awal)* | Daftar + isi catatan, slot fragmen; Observer QuestManager di W3 |
| `state/GameStateManagerTest.java` | W2 | 7 unit test |

> Kenapa DialogueUI dan JournalUI sudah dibuat di W2, padahal di Gantt ada di W3?  
> Karena milestone 17 Nov adalah *prototype playable*, dan dialog tidak bisa dimainkan tanpa tampilan. Di W3 tinggal dipoles dan disambungkan ke QuestManager.

## 5. Yang harus kamu kuasai saat ditanya dosen/asisten

1. **State pattern**: buka `GameStateManager`. Jelaskan `push(DialogueState)` → eksplorasi di-`pause()`, lalu `pop()` → eksplorasi di-`resume()`. Tunjukkan bahwa `GameScreen.render()` hanya memanggil `stateManager.update()`, tanpa if-else.
2. **Observer**: `StoryManager.setObjective()` → `Hud.onObjectiveChanged()`. `Journal.addEntry()` → `Hud.onEntryAdded()` (muncul toast).
3. **Polymorphism**: di `GameScreen.render()` ada `for (Entity e : entities) e.render(batch)`. Player, NPC, dan MemoryObject digambar dengan caranya masing-masing.
4. **Integrasi**: `GameScreen` mengimplementasikan `InteractionContext`, sehingga NPC (Carlos) dan MemoryObject (Insan) tidak bergantung langsung ke kelas UI.

## 6. Skenario demo 17 Nov (±4 menit)

1. Menu utama: tunjukkan navigasi keyboard + mouse dan panel **Kontrol**.
2. **Mulai**: fade in, banner lokasi, intro Iwan. Tujuan muncul di kiri atas.
3. Jalan ke atas, ke warung (bertenda merah-putih). Tunjukkan Iwan tidak bisa menembus tembok atau Bu Sari.
4. Bicara dengan **Bu Sari**: pilih "Lupa? Sejak kapan, Bu?" lalu kembali ke daftar pertanyaan (opsi yang sudah dipilih hilang).
5. Tekan **J**: jurnal berisi 2 catatan. Tutup jurnal.
6. Lewat jembatan kecil ke Gang Kenanga, lalu ke rumah krem No. 17. Bicara dengan **Pak Darto**: The Remnant aktif dan panel kanan atas muncul.
7. Benda-benda mulai **berpendar**. Pindai radio, foto di bangku taman, dan layangan. Tunjukkan kilas balik (teks ungu) dan tujuan "(n/3)".
8. Kembali ke Bu Sari: percakapan foto, lalu toast **"Akhir prototype W2"**.
9. Jalankan `./gradlew core:test`: 62 test lulus.

## 7. Serah terima ke W3

| Anggota | Pekerjaan W3 |
|---|---|
| Iqbal | Peta kota lengkap di Tiled (pakai kontrak layer di `World.java`), transisi antar-area |
| Carlos | Konten NPC tambahan + naskah dua ending di JSON, StoryManager mendengarkan QuestManager |
| Insan | `Puzzle` (abstract) + `SequencePuzzle`, `QuestManager` (Observer), pindahkan logika "(n/3)" dari `GameScreen.onEchoScanned()` ke QuestManager |
| Setiawan | `PuzzleState`, DialogueUI/JournalUI final, JournalUI mendengarkan QuestManager |
| Carlos (aset) | Ganti aset sementara (timpa file PNG/WAV dengan nama dan ukuran yang sama), musik latar |
