// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : InspectPoint.java
//  Bagian  : Core gameplay & world (benda yang bisa diperiksa: TV, catatan, pos ronda)
//  PJ      : Iqbal Aulia Rachman  (isi dialog: Carlos, di dialogues.json)
//  Minggu  : W3
//  OOP     : INHERITANCE (extends Entity), POLYMORPHISM (Interactable)
// =============================================================================
package com.kelompok7.lastremnant.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kelompok7.lastremnant.dialogue.FlagQuery;
import com.kelompok7.lastremnant.entity.Entity;
import com.kelompok7.lastremnant.entity.Interactable;
import com.kelompok7.lastremnant.entity.InteractionContext;

/**
 * Titik periksa tak terlihat di atas tile (TV, meja catatan, foto dinding).
 * Properti Tiled ({@code kind=inspect}):
 * <ul>
 *   <li>{@code dialogueId}   : id dialog di dialogues.json (wajib)</li>
 *   <li>{@code label}        : teks petunjuk HUD</li>
 *   <li>{@code setFlag}      : flag yang aktif setelah dialog selesai (opsional, mis. tv_watched)</li>
 *   <li>{@code requiresFlag} : hanya bisa diperiksa bila flag ini aktif (opsional)</li>
 * </ul>
 * Konten baru cukup ditambah lewat Tiled + JSON, tanpa menyentuh Java.
 */
public class InspectPoint extends Entity implements Interactable {

    private final Rectangle bounds = new Rectangle();
    private final String dialogueId;
    private final String label;
    private final String setFlag;
    private final String requiresFlag;
    private final FlagQuery flags;

    public InspectPoint(Rectangle area, String dialogueId, String label,
                        String setFlag, String requiresFlag, FlagQuery flags) {
        super(area.x + area.width / 2f, area.y, area.width, area.height);
        if (dialogueId == null || dialogueId.isEmpty()) throw new IllegalArgumentException("dialogueId wajib diisi");
        bounds.set(area);
        this.dialogueId = dialogueId;
        this.label = label == null ? "Periksa" : label;
        this.setFlag = emptyToNull(setFlag);
        this.requiresFlag = emptyToNull(requiresFlag);
        this.flags = flags;
    }

    public static InspectPoint fromSpawn(World.Spawn s, FlagQuery flags) {
        return new InspectPoint(s.bounds(new Rectangle()), s.prop("dialogueId"), s.prop("label"),
                s.prop("setFlag"), s.prop("requiresFlag"), flags);
    }

    private static String emptyToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void render(SpriteBatch batch) {
        // tidak terlihat: bendanya sudah ada di tile
    }

    @Override
    public boolean canInteract() {
        return requiresFlag == null || (flags != null && flags.hasFlag(requiresFlag));
    }

    @Override
    public Rectangle getInteractBounds() {
        return bounds;
    }

    @Override
    public String getInteractLabel(InteractionContext ctx) {
        return label;
    }

    @Override
    public Vector2 getIndicatorPosition(Vector2 out) {
        return out.set(bounds.x + bounds.width / 2f, bounds.y + bounds.height + 4f);
    }

    @Override
    public void interact(InteractionContext ctx) {
        ctx.startDialogue(dialogueId, setFlag == null ? null : () -> ctx.getStory().setFlag(setFlag));
    }

    public String getDialogueId() {
        return dialogueId;
    }

    public String getSetFlag() {
        return setFlag;
    }
}
