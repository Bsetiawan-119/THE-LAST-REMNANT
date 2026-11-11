# Weekly Report W2 · Kelompok 7 · The Last Remnant

**Periode:** 11–17 November 2026  
**Milestone:** Prototype playable (17 Nov)  
**Status:** ✅ Tercapai

## Ringkasan
Prototype sudah bisa dimainkan dari menu utama sampai akhir segmen cerita pertama. Modul keempat anggota sudah digabung di `GameScreen`, dan setiap modul punya unit test (62 test, semua lulus).

## Progres per anggota

| Anggota | Modul | Selesai minggu ini | Test |
|---|---|---|---|
| Iqbal | Core gameplay & world | `Entity`, `Player` (gerak 8 arah + animasi), `CollisionSystem` (per sumbu, sub-step, entity solid), `InputHandler` + Command, `World` (loader Tiled + area uji) | `CollisionSystemTest` (8), `InputHandlerTest` (11) |
| Carlos | Dialogue & story | `Dialogue`, `DialogueNode`, `DialogueChoice`, `DialogueSession`, `DialogueRepository` (JSON), `StoryManager` (flag + Observer), `NPC`, naskah prototype, aset sementara | `DialogueSessionTest` (9), `DialogueRepositoryTest` (6), `StoryManagerTest` (6) |
| Insan | Puzzle, memory, quest | `MemoryDevice`, `Journal` (Observer), `MemoryEcho`, `MemoryFragment`, `MemoryObject`, data `memories.json` | `MemoryDeviceTest` (6), `JournalTest` (5) |
| Setiawan | UI & game flow | `MainMenu`, `GameScreen`, `GameStateManager` + 3 state, `Hud`, `DialogueUI` & `JournalUI` (versi awal) | `GameStateManagerTest` (7) |
| Tim | Integrasi | `InteractionContext`, `Interactable`, uji konsistensi data naskah | `StoryDataIntegrityTest` (4) |

## Penerapan OOP & pattern minggu ini
- **Command:** tombol → `MoveCommand` / `InteractCommand` → `Player`. Kontrol bisa diganti (sudah diuji).
- **State:** `ExplorationState` ⇄ `DialogueState` ⇄ `JournalState` lewat tumpukan state.
- **Observer:** `StoryManager` dan `Journal` memberi tahu `Hud`.
- **Polymorphism:** loop update/render `Entity` dan `Interactable.interact()`.

## Kendala & solusi
| Kendala | Solusi |
|---|---|
| Aset pixel art final belum siap | Script pembuat aset sementara (`tools/`) dengan format file yang sama seperti aset final |
| Salah ketik id di JSON baru ketahuan saat bermain | `StoryDataIntegrityTest` memeriksa semua data naskah sebelum game dijalankan |
| Kata "melompat" ke baris bawah saat efek mesin ketik | Sisa teks digambar transparan, sehingga pemenggalan baris sudah tetap sejak huruf pertama |

## Rencana W3 (18–24 Nov)
- Peta kota lengkap (Iqbal)
- `Puzzle` + `SequencePuzzle` + `QuestManager` (Insan)
- Konten NPC tambahan + naskah dua ending (Carlos)
- `PuzzleState`, DialogueUI/JournalUI final yang tersambung ke QuestManager (Setiawan)
