// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Unit test Barrier, Warp, InspectPoint
//  PJ : Iqbal Aulia Rachman  ·  Minggu : W3
// =============================================================================
package com.kelompok7.lastremnant.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kelompok7.lastremnant.dialogue.Dialogue;
import com.kelompok7.lastremnant.entity.Direction;
import com.kelompok7.lastremnant.entity.InteractionContext;
import com.kelompok7.lastremnant.entity.Player;
import com.kelompok7.lastremnant.memory.Journal;
import com.kelompok7.lastremnant.memory.JournalEntry;
import com.kelompok7.lastremnant.memory.MemoryDevice;
import com.kelompok7.lastremnant.memory.MemoryEcho;
import com.kelompok7.lastremnant.story.StoryManager;
import org.junit.Before;
import org.junit.Test;

public class WorldObjectsTest {

    private StoryManager story;
    private Player player;
    private CollisionSystem collision;
    private FakeContext ctx;

    /** InteractionContext palsu: dialog langsung "selesai". */
    private class FakeContext implements InteractionContext {
        String lastDialogue;

        @Override public Player getPlayer() { return player; }
        @Override public StoryManager getStory() { return story; }
        @Override public MemoryDevice getMemoryDevice() { return new MemoryDevice(new Journal()); }

        @Override
        public void startDialogue(String dialogueId, Runnable onFinished) {
            lastDialogue = dialogueId;
            if (onFinished != null) onFinished.run();
        }

        @Override public void startDialogue(Dialogue dialogue, Runnable onFinished) { }
        @Override public void onEchoScanned(MemoryEcho echo, JournalEntry entry) { }
    }

    @Before
    public void setUp() {
        story = new StoryManager();
        collision = new CollisionSystem(new Array<>(), new Rectangle(0, 0, 400, 400));
        player = new Player(100, 50, collision);
        ctx = new FakeContext();
    }

    @Test
    public void barrierMenghalangiSampaiFlagAktif() {
        Barrier b = new Barrier(new Rectangle(90, 70, 32, 32), story, "scanned_radio", null, null);
        collision.addEntity(b);
        assertTrue(b.isSolid());

        for (int i = 0; i < 30; i++) {          // jalan ke atas 30 frame
            player.move(0, 1);
            player.update(1f / 30f);
            b.update(1f / 30f);
        }
        assertTrue("tertahan rak", player.getY() < 70);

        int[] opened = {0};
        b.setOnOpened(() -> opened[0]++);
        story.setFlag("scanned_radio");
        b.update(0.016f);
        b.update(0.016f);
        assertFalse(b.isSolid());
        assertEquals("callback sekali saja", 1, opened[0]);

        for (int i = 0; i < 60; i++) {
            player.move(0, 1);
            player.update(1f / 30f);
        }
        assertTrue("bisa lewat", player.getY() > 102);
    }

    @Test
    public void barrierSudahTerbukaBilaFlagAktifSejakAwal() {
        story.setFlag("tv_watched");
        Barrier b = new Barrier(new Rectangle(0, 0, 16, 16), story, "tv_watched", null, null);
        assertTrue(b.isOpen());
    }

    @Test(expected = IllegalArgumentException.class)
    public void barrierTanpaFlagDitolak() {
        new Barrier(new Rectangle(0, 0, 16, 16), story, "", null, null);
    }

    @Test
    public void warpMemindahkanPemain() {
        Warp w = new Warp(new Rectangle(90, 60, 16, 12), "Masuk rumah No. 17", 300, 320, Direction.UP);
        assertEquals("Masuk rumah No. 17", w.getInteractLabel(ctx));
        w.interact(ctx);
        assertEquals(300, player.getX(), 0.001f);
        assertEquals(320, player.getY(), 0.001f);
        assertEquals(Direction.UP, player.getFacing());
        assertEquals(300, player.getHitbox().x + player.getHitbox().width / 2f, 0.001f);
    }

    @Test
    public void warpDariSpawnMembacaTargetDanArah() {
        World.Spawn door = spawn("warp", "pintu", 100, 50, 16, 12, "target", "dalam", "label", "Masuk");
        World.Spawn inside = spawn("marker", "dalam", 300, 320, 16, 16, "facing", "up");
        Warp w = Warp.fromSpawn(door, name -> "dalam".equals(name) ? inside : null);
        assertEquals(300, w.getTargetX(), 0.001f);
        assertEquals(Direction.UP, w.getTargetFacing());
    }

    /** Regresi: GameScreen membuat Warp di DALAM loop spawn; libGDX Array melarang iterator bersarang. */
    @Test
    public void cariSpawnAmanDiDalamLoopSpawn() {
        Array<World.Spawn> spawns = new Array<>();
        spawns.add(spawn("warp", "pintu", 100, 50, 16, 12, "target", "dalam"));
        spawns.add(spawn("marker", "dalam", 300, 320, 16, 16, "facing", "up"));
        int warps = 0;
        for (World.Spawn s : spawns) {
            if ("warp".equals(s.kind)) {
                Warp.fromSpawn(s, name -> World.findByName(spawns, name));
                warps++;
            }
        }
        assertEquals(1, warps);
    }

    @Test(expected = IllegalArgumentException.class)
    public void warpTargetSalahKetikLangsungKetahuan() {
        World.Spawn door = spawn("warp", "pintu", 100, 50, 16, 12, "target", "dalem");
        Warp.fromSpawn(door, name -> null);
    }

    @Test
    public void inspectMemulaiDialogLaluSetFlag() {
        InspectPoint tv = new InspectPoint(new Rectangle(0, 0, 16, 16), "s1_tv_rekaman", "Nyalakan TV",
                "tv_watched", null, story);
        tv.interact(ctx);
        assertEquals("s1_tv_rekaman", ctx.lastDialogue);
        assertTrue(story.hasFlag("tv_watched"));
    }

    @Test
    public void inspectBisaDisyaratkanFlag() {
        InspectPoint p = new InspectPoint(new Rectangle(0, 0, 16, 16), "x", null, null, "punya_kunci", story);
        assertFalse(p.canInteract());
        story.setFlag("punya_kunci");
        assertTrue(p.canInteract());
        assertEquals("Periksa", p.getInteractLabel(ctx));
        assertNull(p.getSetFlag());
    }

    // ------------------------------------------------------------------ helper

    private static World.Spawn spawn(String kind, String name, float x, float y, float w, float h, String... kv) {
        MapProperties p = new MapProperties();
        p.put("kind", kind);
        for (int i = 0; i + 1 < kv.length; i += 2) p.put(kv[i], kv[i + 1]);
        return new World.Spawn(kind, name, x, y, w, h, p);
    }
}
