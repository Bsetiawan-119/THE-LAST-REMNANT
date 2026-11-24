# W3 · Iqbal Aulia Rachman: Weapon + Inventory, peta Kota Kepompong + ruang rahasia

Branch: `feat/iqbal-world` · Status: siap review · `./gradlew core:test`: semua test lulus (126 test, 64 di antaranya baru)

## Yang selesai

**Senjata (`combat/`)**
- `Weapon` (abstract) → `MeleeWeapon` → `Knife`, dan `RangedWeapon` → `Pistol` (magasin 6, isi ulang 1,2 detik).
  Alur serang dikunci di `Weapon.tryAttack()` (Template Method), jadi cooldown sama untuk semua senjata.
- `Inventory`: pisau tetap di slot 0 dan tidak bisa dibuang, plus 2 slot pungutan. Benda yang sama tidak bisa masuk dua kali,
  pistol kedua digabung jadi peluru cadangan, dan kalau slot penuh senjata ditukar (yang lama jatuh ke lantai).
  Ganti senjata membatalkan isi ulang.
- `WeaponSystem`: satu titik integrasi untuk ambil/buang, sabetan, peluru, dan target `Damageable`.
- `WeaponPickup` (senjata di lantai, bisa diambil dengan E/F), `Projectile`, `TargetDummy` (boneka latihan sementara).
- Command baru: `AttackCommand` (K), `ReloadCommand` (R), `SwitchWeaponCommand` (Q, 1–3), `PickupCommand` (F), `DropCommand` (G).
- Observer: `InventoryListener` (untuk HUD), `WeaponSystemListener` (suara, toast).

**Peta & world (`world/`, `assets/maps/`)**
- `kota_kepompong.tmx` (80×44): Ladang Kepompong (Z-02), jalan Depok yang hancur, pos ronda + pistol,
  Gang Kenanga, rumah No. 17 + interior, ruang rahasia (TV, catatan, coretan EXIT), puing di ujung gang.
- `Warp` (pintu luar ↔ dalam, fade + kamera langsung ikut), `Barrier` (terbuka oleh flag), `InspectPoint` (TV/catatan → dialog JSON).
- `tools/generate_stage1_assets.py`: tileset hancur (pakai ulang palet Carlos), sprite senjata, dan SFX combat.
- `Stage1MapIntegrityTest`: memastikan alur Stage 1 bisa dituntaskan tanpa jalan buntu, ruang rahasia tidak bisa dimasuki
  sebelum radio dipindai, dan jalan keluar tidak terbuka sebelum TV ditonton.

**Coba:** `./gradlew lwjgl3:run "-Pmap=maps/kota_kepompong.tmx"` (Windows PowerShell: `.\gradlew.bat lwjgl3:run "-Pmap=maps/kota_kepompong.tmx"`, tanda kutip wajib)

## Kontrak untuk anggota lain

**Insan (Enemy, Health, boss)**
- Enemy/Sang Exitium cukup `implements combat.Damageable` (`getHurtbox()`, `isAlive()`, `takeDamage(int, Attack)`),
  lalu daftarkan dengan `screen.getWeaponSystem().addTarget(enemy)`.
- Titik spawn Boneka: objek `kind=enemy`, `enemyId=boneka` (6 titik di peta). Boneka latihan (`kind=dummy`) boleh dihapus kalau Enemy sudah siap.
- Hurtbox sebaiknya setinggi badan (contoh di `TargetDummy`: 12×20 dari kaki ke atas).

**Setiawan (CombatState, HUD, alur)**
- HUD darah/peluru/senjata: `implements InventoryListener`, daftarkan ke `screen.getInventory().addListener(hud)`.
  Teks peluru siap pakai: `weapon.getAmmoText()`, contoh `"5/6 | 0"`.
- `weapons.update(delta, player)` sekarang dipanggil di `GameScreen.updateWorld()`. Kalau CombatState dibuat, panggil dari sana.
- Masuk Stage 1 setelah BANGUN: `new GameScreen(game, Constants.STAGE1_MAP)`. Untuk sementara Stage 1 dibuka langsung dan
  mengaktifkan The Remnant.
- Penanda di peta: `kind=checkpoint` (2), `kind=hiding` (2), `kind=exit` (`nextMap=maps/perpustakaan_arsip.tmx`).
- Bagian yang saya ubah di `GameScreen` ditandai `[W3 Iqbal]`; tolong direview.

**Carlos (naskah & aset)**
- Dialog yang dipanggil peta (belum ada di `dialogues.json`; untuk sementara game hanya mencatat error lalu lanjut):
  `s1_pos_ronda`, `s1_foto_dinding`, `s1_tv_rekaman`, `s1_catatan_kalpataru`, `s1_papan_exit`.
- Flag yang dipakai: `scanned_radio` (otomatis dari MemoryObject) membuka rak, `tv_watched` (di-set setelah dialog TV selesai) membuka puing.
- Radio di No. 17 memakai `memoryId=radio`. Kalau Amulet Kenang punya echo sendiri, ganti properti ini di Tiled.
- Sprite sementara (ganti dengan menimpa file berukuran sama): `weapon_knife`, `weapon_pistol`, `bullet` (4×4), `dummy`,
  `dummy_broken` (16×24), `secret_shelf`, `secret_shelf_open` (32×32), `rubble_gate` (48×32), `kepompong_tiles.png`.
- `GameAssets`: saya menambahkan kunci sprite & SFX baru ke array `SPRITES`/`SFX`.

## Berikutnya (W4, 25 Nov–1 Des)
Peta Perpustakaan Arsip (6 ruang + hub) dan arena boss, memakai `Warp`/`Barrier`/`InspectPoint` yang sama,
plus balancing senjata bersama Insan.
