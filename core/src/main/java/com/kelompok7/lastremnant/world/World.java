// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : World.java
//  Bagian  : Core gameplay & world (peta Tiled, layer, titik spawn)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W2 = loader peta + area uji (maps/test_area.tmx)
//            W3 = peta kota lengkap (cukup ganti file .tmx, kelas ini tetap)
//  LibGDX  : TiledMap, TmxMapLoader, OrthogonalTiledMapRenderer
// =============================================================================
package com.kelompok7.lastremnant.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;

/**
 * Membungkus satu peta Tiled.
 * <p>
 * Kontrak layer (wajib diikuti saat membuat peta baru di Tiled):
 * <ul>
 *   <li>Tile layer <b>ground</b>, <b>detail</b> : digambar DI BAWAH karakter.</li>
 *   <li>Tile layer <b>overhead</b>              : digambar DI ATAS karakter (tajuk pohon, lampu).</li>
 *   <li>Object layer <b>collision</b>           : kotak-kotak yang tidak bisa dilewati.</li>
 *   <li>Object layer <b>entities</b>            : titik spawn; properti {@code kind} = player | npc | memory.</li>
 * </ul>
 */
public class World implements Disposable {

    /** Data titik spawn dari layer "entities". Posisi = tengah-bawah objek. */
    public static final class Spawn {
        public final String kind;
        public final String name;
        public final float x;
        public final float y;
        public final MapProperties properties;

        Spawn(String kind, String name, float x, float y, MapProperties properties) {
            this.kind = kind;
            this.name = name;
            this.x = x;
            this.y = y;
            this.properties = properties;
        }

        public String prop(String key) {
            return properties.get(key, String.class);
        }
    }

    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final Array<Rectangle> solids = new Array<>();
    private final Array<Spawn> spawns = new Array<>();
    private final Rectangle bounds;
    private final int[] belowLayers;
    private final int[] aboveLayers;
    private final String displayName;

    public World(String mapPath, Batch batch) {
        map = new TmxMapLoader().load(mapPath);
        renderer = new OrthogonalTiledMapRenderer(map, 1f, batch);

        MapProperties p = map.getProperties();
        int tileW = p.get("tilewidth", Integer.class);
        int tileH = p.get("tileheight", Integer.class);
        int w = p.get("width", Integer.class);
        int h = p.get("height", Integer.class);
        bounds = new Rectangle(0, 0, w * tileW, h * tileH);
        displayName = p.get("displayName", "Tanpa Nama", String.class);

        belowLayers = layerIndices("ground", "detail");
        aboveLayers = layerIndices("overhead");
        readCollision();
        readSpawns();
    }

    private int[] layerIndices(String... names) {
        IntArray result = new IntArray();
        for (String n : names) {
            int i = map.getLayers().getIndex(n);
            if (i >= 0) result.add(i);
        }
        return result.toArray();
    }

    private void readCollision() {
        MapLayer layer = map.getLayers().get("collision");
        if (layer == null) return;
        for (MapObject o : layer.getObjects()) {
            if (o instanceof RectangleMapObject) {
                solids.add(new Rectangle(((RectangleMapObject) o).getRectangle()));
            }
        }
    }

    private void readSpawns() {
        MapLayer layer = map.getLayers().get("entities");
        if (layer == null) return;
        for (MapObject o : layer.getObjects()) {
            if (!(o instanceof RectangleMapObject)) continue;
            Rectangle r = ((RectangleMapObject) o).getRectangle();
            String kind = o.getProperties().get("kind", "", String.class);
            spawns.add(new Spawn(kind, o.getName(), r.x + r.width / 2f, r.y, o.getProperties()));
        }
    }

    // ------------------------------------------------------------------ render

    public void renderBelow(OrthographicCamera camera) {
        renderer.setView(camera);
        if (belowLayers.length > 0) renderer.render(belowLayers);
    }

    public void renderAbove(OrthographicCamera camera) {
        renderer.setView(camera);
        if (aboveLayers.length > 0) renderer.render(aboveLayers);
    }

    // ------------------------------------------------------------------ getter

    public Array<Rectangle> getSolids() {
        return solids;
    }

    public Array<Spawn> getSpawns() {
        return spawns;
    }

    public Spawn findSpawn(String kind) {
        for (Spawn s : spawns) {
            if (s.kind.equals(kind)) return s;
        }
        return null;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void dispose() {
        renderer.dispose();
        map.dispose();
    }
}
