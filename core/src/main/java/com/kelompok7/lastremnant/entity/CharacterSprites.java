// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : CharacterSprites.java
//  Bagian  : Core gameplay & world (sprite & animasi karakter)
//  PJ      : Iqbal Aulia Rachman  (format spritesheet disepakati dengan Carlos)
//  Minggu  : W2
// =============================================================================
package com.kelompok7.lastremnant.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Memotong spritesheet karakter (4 kolom frame x 4 baris arah) menjadi animasi jalan.
 * Format sheet: frame 0 & 2 = berdiri, frame 1 & 3 = langkah kiri/kanan.
 * Dipakai bersama oleh Player dan NPC (komposisi, bukan pewarisan).
 */
public class CharacterSprites {

    private final Animation<TextureRegion>[] walk;
    private final TextureRegion[] idle;
    private final TextureRegion portrait;
    private final TextureRegion shadow;

    @SuppressWarnings({"unchecked", "rawtypes"})  // array generik harus dibuat dari raw type
    public CharacterSprites(Texture sheet, int frameW, int frameH, float frameDuration, TextureRegion shadow) {
        TextureRegion[][] grid = TextureRegion.split(sheet, frameW, frameH);
        Direction[] dirs = Direction.values();
        walk = new Animation[dirs.length];
        idle = new TextureRegion[dirs.length];
        for (Direction d : dirs) {
            Array<TextureRegion> frames = new Array<>(grid[d.sheetRow()]);
            walk[d.ordinal()] = new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
            idle[d.ordinal()] = grid[d.sheetRow()][0];
        }
        // potret = bagian kepala dari frame menghadap bawah (dipakai DialogueUI)
        portrait = new TextureRegion(sheet, 0, 0, frameW, Math.min(frameH, 14));
        this.shadow = shadow;
    }

    public TextureRegion frame(Direction dir, boolean moving, float stateTime) {
        return moving ? walk[dir.ordinal()].getKeyFrame(stateTime) : idle[dir.ordinal()];
    }

    public TextureRegion getPortrait() {
        return portrait;
    }

    public TextureRegion getShadow() {
        return shadow;
    }
}
