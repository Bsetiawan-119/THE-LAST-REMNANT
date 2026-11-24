// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Test integritas peta Stage 1 (kota_kepompong.tmx)
//  PJ : Iqbal Aulia Rachman  ·  Minggu : W3
//
//  Membaca file TMX langsung (tanpa OpenGL) lalu memastikan:
//   - layer wajib ada, semua objek di dalam peta, id senjata/memori/target warp valid
//   - titik spawn tidak berada di dalam tembok
//   - alur Stage 1 bisa dituntaskan (tidak ada jalan buntu):
//       kepompong -> pistol -> pintu No. 17 -> radio -> (rak terbuka) -> TV
//       -> (puing terbuka) -> jalan ke Perpustakaan Arsip
//   - ruang rahasia & jalan keluar TIDAK bisa dicapai sebelum flag-nya aktif
//  Kalau peta diedit di Tiled lalu ada yang rusak, test ini yang pertama gagal.
// =============================================================================
package com.kelompok7.lastremnant.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.kelompok7.lastremnant.combat.WeaponFactory;
import com.kelompok7.lastremnant.memory.MemoryDatabase;
import com.kelompok7.lastremnant.util.Constants;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;

public class Stage1MapIntegrityTest {

    /** Objek Tiled (koordinat Tiled: y ke bawah). */
    private static final class Obj {
        String name;
        String kind;
        Rectangle rect;
        ObjectMap<String, String> props = new ObjectMap<>();

        /** Titik kaki = tengah-bawah kotak (sama seperti World.Spawn). */
        int footTileX() {
            return (int) ((rect.x + rect.width / 2f) / Constants.TILE_SIZE);
        }

        int footTileY() {
            return (int) ((rect.y + rect.height - 1f) / Constants.TILE_SIZE);
        }
    }

    private static Path assetsDir;
    private static int width;
    private static int height;
    private static final Array<String> layers = new Array<>();
    private static final Array<Rectangle> solids = new Array<>();
    private static final List<Obj> objects = new ArrayList<>();
    private static XmlReader.Element root;

    @BeforeClass
    public static void load() throws IOException {
        for (String c : new String[]{"../assets", "assets", "."}) {
            if (Files.exists(Paths.get(c).resolve(Constants.STAGE1_MAP))) {
                assetsDir = Paths.get(c);
                break;
            }
        }
        if (assetsDir == null) throw new IllegalStateException("Peta Stage 1 tidak ditemukan: " + Constants.STAGE1_MAP);
        String xml = new String(Files.readAllBytes(assetsDir.resolve(Constants.STAGE1_MAP)), StandardCharsets.UTF_8);
        root = new XmlReader().parse(xml);
        width = root.getIntAttribute("width");
        height = root.getIntAttribute("height");
        for (XmlReader.Element l : root.getChildrenByName("layer")) layers.add(l.get("name"));
        for (XmlReader.Element g : root.getChildrenByName("objectgroup")) {
            layers.add(g.get("name"));
            for (XmlReader.Element o : g.getChildrenByName("object")) {
                Rectangle r = new Rectangle(o.getFloatAttribute("x"), o.getFloatAttribute("y"),
                        o.getFloatAttribute("width", 0f), o.getFloatAttribute("height", 0f));
                if ("collision".equals(g.get("name"))) {
                    solids.add(r);
                    continue;
                }
                Obj obj = new Obj();
                obj.name = o.get("name", "");
                obj.rect = r;
                XmlReader.Element props = o.getChildByName("properties");
                if (props != null) {
                    for (XmlReader.Element p : props.getChildrenByName("property")) {
                        obj.props.put(p.get("name"), p.get("value"));
                    }
                }
                obj.kind = obj.props.get("kind", "");
                objects.add(obj);
            }
        }
    }

    // ================================================================== struktur

    @Test
    public void layerWajibAda() {
        for (String l : new String[]{"ground", "detail", "overhead", "collision", "entities"}) {
            assertTrue("layer hilang: " + l, layers.contains(l, false));
        }
    }

    @Test
    public void tilesetAda() {
        String img = root.getChildByName("tileset").getChildByName("image").get("source");
        assertTrue("tileset tidak ada: " + img, Files.exists(assetsDir.resolve("maps").resolve(img)));
    }

    @Test
    public void isiLayerTileLengkapDanGidValid() {
        int tilecount = root.getChildByName("tileset").getIntAttribute("tilecount");
        for (XmlReader.Element l : root.getChildrenByName("layer")) {
            String[] cells = l.getChildByName("data").getText().trim().split("\\s*,\\s*");
            assertEquals("jumlah tile layer " + l.get("name"), width * height, cells.length);
            for (String c : cells) {
                int gid = Integer.parseInt(c.trim());
                assertTrue("gid di luar tileset: " + gid, gid >= 0 && gid <= tilecount);
            }
        }
    }

    @Test
    public void semuaSpriteDanSfxCombatAda() {
        for (String s : new String[]{"weapon_knife", "weapon_pistol", "bullet", "dummy", "dummy_broken"}) {
            assertSpriteExists(s);
        }
        for (String s : new String[]{"slash", "shot", "reload", "pickup", "empty"}) {
            assertTrue("sfx tidak ada: " + s, Files.exists(assetsDir.resolve("sfx/" + s + ".wav")));
        }
    }

    @Test
    public void tepatSatuPlayer() {
        int n = 0;
        for (Obj o : objects) if ("player".equals(o.kind)) n++;
        assertEquals(1, n);
    }

    @Test
    public void semuaObjekDiDalamPeta() {
        Rectangle map = new Rectangle(0, 0, width * Constants.TILE_SIZE, height * Constants.TILE_SIZE);
        for (Obj o : objects) {
            assertTrue("di luar peta: " + o.name, map.contains(o.rect));
        }
    }

    @Test
    public void propertiObjekValid() throws IOException {
        MemoryDatabase mem = MemoryDatabase.fromJson(new String(
                Files.readAllBytes(assetsDir.resolve("data/memories.json")), StandardCharsets.UTF_8));
        Set<String> names = new HashSet<>();
        for (Obj o : objects) {
            assertTrue("nama objek ganda: " + o.name, names.add(o.name));
        }
        for (Obj o : objects) {
            switch (o.kind) {
                case "weapon":
                    assertTrue(o.name + ": weaponId tidak dikenal", WeaponFactory.isKnown(o.props.get("weaponId")));
                    break;
                case "memory":
                    assertNotNull(o.name + ": memoryId tidak ada di memories.json", mem.getEcho(o.props.get("memoryId")));
                    break;
                case "warp":
                    assertNotNull(o.name + ": target warp tidak ada", find(o.props.get("target")));
                    break;
                case "barrier":
                    assertNotNull(o.name + ": openFlag kosong", o.props.get("openFlag"));
                    assertSpriteExists(o.props.get("sprite"));
                    if (o.props.containsKey("openSprite")) assertSpriteExists(o.props.get("openSprite"));
                    break;
                case "inspect":
                    assertNotNull(o.name + ": dialogueId kosong", o.props.get("dialogueId"));
                    break;
                case "player": case "marker": case "dummy": case "enemy": case "hiding":
                case "checkpoint": case "exit":
                    break;
                default:
                    fail(o.name + ": kind tidak dikenal '" + o.kind + "'");
            }
        }
    }

    private static void assertSpriteExists(String key) {
        assertNotNull("sprite kosong", key);
        assertTrue("sprite tidak ada: " + key, Files.exists(assetsDir.resolve("sprites/" + key + ".png")));
    }

    @Test
    public void titikBerdiriTidakDiDalamTembok() {
        boolean[][] blocked = grid(false, false);
        for (Obj o : objects) {
            switch (o.kind) {
                case "player": case "marker": case "weapon": case "dummy": case "memory":
                case "enemy": case "hiding": case "checkpoint":
                    assertFalse(o.name + " berada di dalam tembok (tile " + o.footTileX() + "," + o.footTileY() + ")",
                            blocked[o.footTileY()][o.footTileX()]);
                    break;
                default:
                    break;
            }
        }
    }

    // ================================================================== alur Stage 1

    @Test
    public void alurStage1BisaDituntaskan() {
        Obj start = first("player");

        // 1. rak & puing masih tertutup
        boolean[][] reach = flood(start, grid(false, false));
        assertReachable(reach, find("pistol_pos_ronda"), "pistol");
        assertReachable(reach, find("pintu_no17"), "pintu No. 17");
        assertReachable(reach, find("radio_darto"), "radio Pak Darto (lewat pintu)");
        assertReachable(reach, find("boneka_latihan_1"), "boneka latihan");
        assertFalse("ruang rahasia terbuka sebelum radio dipindai", touches(reach, find("tv_rekaman")));
        assertFalse("jalan keluar terbuka sebelum TV ditonton", touches(reach, find("ke_perpustakaan")));

        // 2. radio dipindai -> rak bergeser
        reach = flood(start, grid(true, false));
        assertReachable(reach, find("tv_rekaman"), "TV ruang rahasia");
        assertReachable(reach, find("catatan_kalpataru"), "catatan Kalpataru");
        assertFalse("jalan keluar terbuka sebelum TV ditonton", touches(reach, find("ke_perpustakaan")));

        // 3. TV ditonton -> puing terbuka
        reach = flood(start, grid(true, true));
        assertReachable(reach, find("ke_perpustakaan"), "jalan ke Perpustakaan Arsip");
    }

    @Test
    public void bisaKeluarLagiDariRumahNo17() {
        boolean[][] reach = flood(find("no17_dalam"), grid(false, false));
        assertReachable(reach, find("pintu_keluar_no17"), "pintu keluar");
        assertReachable(reach, find("pistol_pos_ronda"), "kembali ke kota");
    }

    // ================================================================== helper

    /** true = tile tidak bisa dilewati. Barrier dihitung tertutup kecuali flag-nya dianggap aktif. */
    private static boolean[][] grid(boolean radioScanned, boolean tvWatched) {
        int t = Constants.TILE_SIZE;
        boolean[][] b = new boolean[height][width];
        Rectangle cell = new Rectangle();
        Array<Rectangle> blockers = new Array<>(solids);
        for (Obj o : objects) {
            if (!"barrier".equals(o.kind)) continue;
            String flag = o.props.get("openFlag");
            boolean open = ("scanned_radio".equals(flag) && radioScanned) || ("tv_watched".equals(flag) && tvWatched);
            if (!open) blockers.add(o.rect);
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cell.set(x * t + 3, y * t + 3, t - 6, t - 6);   // tengah tile (toleransi tepi)
                for (Rectangle r : blockers) {
                    if (r.overlaps(cell)) {
                        b[y][x] = true;
                        break;
                    }
                }
            }
        }
        return b;
    }

    /** Flood fill 4 arah + loncatan lewat warp. */
    private static boolean[][] flood(Obj start, boolean[][] blocked) {
        boolean[][] seen = new boolean[height][width];
        ArrayDeque<int[]> q = new ArrayDeque<>();
        push(q, seen, blocked, start.footTileX(), start.footTileY());
        boolean changed = true;
        while (changed) {
            while (!q.isEmpty()) {
                int[] c = q.poll();
                push(q, seen, blocked, c[0] + 1, c[1]);
                push(q, seen, blocked, c[0] - 1, c[1]);
                push(q, seen, blocked, c[0], c[1] + 1);
                push(q, seen, blocked, c[0], c[1] - 1);
            }
            changed = false;
            for (Obj w : objects) {
                if (!"warp".equals(w.kind) || !touches(seen, w)) continue;
                Obj target = find(w.props.get("target"));
                int tx = target.footTileX();
                int ty = target.footTileY();
                if (!seen[ty][tx] && !blocked[ty][tx]) {
                    push(q, seen, blocked, tx, ty);
                    changed = true;
                }
            }
        }
        return seen;
    }

    private static void push(ArrayDeque<int[]> q, boolean[][] seen, boolean[][] blocked, int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height || seen[y][x] || blocked[y][x]) return;
        seen[y][x] = true;
        q.add(new int[]{x, y});
    }

    /** Objek bisa dicapai bila pemain bisa berdiri di tile objek atau tepat di sebelahnya. */
    private static boolean touches(boolean[][] reach, Obj o) {
        int t = Constants.TILE_SIZE;
        int x0 = (int) (o.rect.x / t) - 1;
        int y0 = (int) (o.rect.y / t) - 1;
        int x1 = (int) ((o.rect.x + Math.max(1f, o.rect.width) - 1f) / t) + 1;
        int y1 = (int) ((o.rect.y + Math.max(1f, o.rect.height) - 1f) / t) + 1;
        for (int y = Math.max(0, y0); y <= Math.min(height - 1, y1); y++) {
            for (int x = Math.max(0, x0); x <= Math.min(width - 1, x1); x++) {
                if (reach[y][x]) return true;
            }
        }
        return false;
    }

    private static void assertReachable(boolean[][] reach, Obj o, String what) {
        assertNotNull("objek tidak ada: " + what, o);
        assertTrue("tidak bisa dicapai: " + what, touches(reach, o));
    }

    private static Obj find(String name) {
        for (Obj o : objects) if (o.name.equals(name)) return o;
        return null;
    }

    private static Obj first(String kind) {
        for (Obj o : objects) if (o.kind.equals(kind)) return o;
        throw new AssertionError("tidak ada objek kind=" + kind);
    }
}
