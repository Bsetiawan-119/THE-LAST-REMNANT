// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Barrier.java
//  Bagian  : Core gameplay & world (penghalang yang terbuka oleh flag cerita)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W3  (rak ruang rahasia No. 17, puing di ujung gang)
//  OOP     : INHERITANCE (extends Entity), POLYMORPHISM (isSolid() dinamis,
//            dibaca CollisionSystem tanpa tahu jenis entity-nya)
//  Test    : core/src/test/.../world/WorldObjectsTest.java
// =============================================================================
package com.kelompok7.lastremnant.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.kelompok7.lastremnant.dialogue.FlagQuery;
import com.kelompok7.lastremnant.entity.Entity;

/**
 * Penghalang yang solid sampai flag tertentu aktif.
 * <ul>
 *   <li>Rak buku No. 17 : {@code openFlag = scanned_radio} (radio Pak Darto dipindai).</li>
 *   <li>Puing ujung gang : {@code openFlag = tv_watched} (rekaman TV sudah ditonton).</li>
 * </ul>
 * Bergantung ke {@link FlagQuery}, bukan StoryManager, jadi bisa diuji tanpa modul cerita.
 */
public class Barrier extends Entity {

    private final FlagQuery flags;
    private final String openFlag;
    private final TextureRegion closedSprite;
    private final TextureRegion openSprite;
    private boolean open;
    private Runnable onOpened;

    public Barrier(Rectangle area, FlagQuery flags, String openFlag,
                   TextureRegion closedSprite, TextureRegion openSprite) {
        super(area.x + area.width / 2f, area.y, area.width, area.height);
        if (flags == null) throw new IllegalArgumentException("flags wajib ada");
        if (openFlag == null || openFlag.isEmpty()) throw new IllegalArgumentException("openFlag wajib diisi");
        this.flags = flags;
        this.openFlag = openFlag;
        this.closedSprite = closedSprite;
        this.openSprite = openSprite;
        this.open = flags.hasFlag(openFlag);
    }

    /** Dipanggil sekali saat penghalang terbuka (suara, toast). */
    public void setOnOpened(Runnable onOpened) {
        this.onOpened = onOpened;
    }

    @Override
    public void update(float delta) {
        if (!open && flags.hasFlag(openFlag)) {
            open = true;
            if (onOpened != null) onOpened.run();
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion r = open ? openSprite : closedSprite;
        if (r != null) batch.draw(r, hitbox.x, hitbox.y);
    }

    /** Hanya menghalangi selama belum terbuka. Sekali terbuka tidak menutup lagi. */
    @Override
    public boolean isSolid() {
        return !open;
    }

    public boolean isOpen() {
        return open;
    }

    public String getOpenFlag() {
        return openFlag;
    }
}
