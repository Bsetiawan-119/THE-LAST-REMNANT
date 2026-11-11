// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : JournalUI.java
//  Bagian  : UI & game flow (layar jurnal: daftar catatan + isi + slot fragmen)
//  PJ      : Budiman Setiawan
//  Minggu  : W2 = versi awal (menampilkan Journal milik Insan)
//            W3 = disambungkan ke QuestManager lewat Observer (daftar quest aktif)
//  LibGDX  : Scene2D (Table, ScrollPane, Label)
// =============================================================================
package com.kelompok7.lastremnant.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kelompok7.lastremnant.assets.GameAssets;
import com.kelompok7.lastremnant.memory.Journal;
import com.kelompok7.lastremnant.memory.JournalEntry;
import com.kelompok7.lastremnant.memory.MemoryDevice;

/**
 * Jurnal Iwan.
 * <pre>
 * +--------------------------------------------------------------+
 * | JURNAL IWAN                               <> <> <>  Fragmen 0/3|
 * |--------------------------------------------------------------|
 * | > Paket tanpa pengirim  |  CATATAN                            |
 * |   The Remnant           |  Paket tanpa pengirim               |
 * |   Lagu di radio ... *   |  Alamat: Gg. Kenanga No. 17 ...      |
 * |--------------------------------------------------------------|
 * | [W/S] Pilih    [J/Esc] Tutup                                 |
 * +--------------------------------------------------------------+
 * </pre>
 */
public class JournalUI {

    private static final float LIST_W = 196f;
    private static final float BODY_W = 330f;

    private final UiStyles st;
    private final GameAssets assets;
    private final Journal journal;
    private final MemoryDevice device;

    private final Table root = new Table();
    private final Table list = new Table();
    private final ScrollPane listScroll;
    private final Label categoryLabel;
    private final Label titleLabel;
    private final Label bodyLabel;
    private final Label emptyLabel;
    private final Table slotRow = new Table();
    private final Array<Label> rows = new Array<>();
    private final Array<Image> cursors = new Array<>();
    private Array<JournalEntry> entries = new Array<>();
    private int selected;
    private boolean open;

    public JournalUI(Stage stage, UiStyles st, GameAssets assets, Journal journal, MemoryDevice device) {
        this.st = st;
        this.assets = assets;
        this.journal = journal;
        this.device = device;

        categoryLabel = new Label("", st.accent);
        titleLabel = new Label("", st.warm);
        titleLabel.setWrap(true);
        bodyLabel = new Label("", st.body);
        bodyLabel.setWrap(true);
        bodyLabel.setAlignment(Align.topLeft);
        emptyLabel = new Label("Belum ada catatan.", st.dim);

        list.top().left();
        listScroll = new ScrollPane(list);
        listScroll.setScrollingDisabled(true, false);
        listScroll.setFadeScrollBars(false);

        Table window = new Table();
        window.setBackground(st.panel);
        window.pad(14, 16, 12, 16);

        Table header = new Table();
        header.add(new Label("JURNAL IWAN", st.warm)).left().expandX();
        header.add(slotRow).right();
        window.add(header).growX().colspan(3).row();
        window.add(new Image(st.solid(new Color(1f, 1f, 1f, 0.15f)))).height(1).growX().colspan(3).padTop(8).padBottom(8).row();

        Table detail = new Table();
        detail.top().left();
        detail.add(categoryLabel).left().row();
        detail.add(titleLabel).width(BODY_W).left().padTop(2).row();
        detail.add(bodyLabel).width(BODY_W).left().padTop(8).expandY().top();

        window.add(listScroll).width(LIST_W).height(214).top().left();
        window.add(new Image(st.solid(new Color(1f, 1f, 1f, 0.12f)))).width(1).growY().padLeft(10).padRight(14);
        window.add(detail).width(BODY_W).height(214).top().left().row();

        window.add(new Image(st.solid(new Color(1f, 1f, 1f, 0.15f)))).height(1).growX().colspan(3).padTop(8).padBottom(6).row();
        window.add(new Label(UiStyles.escape("[W/S] Pilih     [J/Esc] Tutup"), st.dim)).left().colspan(3);

        root.setFillParent(true);
        root.setBackground(st.solid(new Color(0.03f, 0.02f, 0.06f, 0.72f)));
        root.setTouchable(Touchable.enabled);
        root.add(window).width(590);
        root.setVisible(false);
        stage.addActor(root);
    }

    public void show() {
        open = true;
        entries = journal.getEntries();
        rebuildList();
        rebuildSlots();
        // buka di catatan terbaru yang belum dibaca, atau entri terakhir
        int start = entries.size - 1;
        for (int i = 0; i < entries.size; i++) {
            if (!entries.get(i).isRead()) {
                start = i;
                break;
            }
        }
        select(Math.max(0, start));
        root.setVisible(true);
        root.clearActions();
        root.addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.18f)));
    }

    public void hide() {
        open = false;
        root.clearActions();
        root.setVisible(false);
    }

    public void moveSelection(int delta) {
        if (!open || entries.size == 0) return;
        int next = Math.max(0, Math.min(entries.size - 1, selected + delta));
        if (next != selected) {
            select(next);
            assets.play("select", 0.4f, 1.1f);
        }
    }

    public boolean isOpen() {
        return open;
    }

    // ------------------------------------------------------------------ internal

    private void rebuildList() {
        list.clearChildren();
        rows.clear();
        cursors.clear();
        if (entries.size == 0) {
            list.add(emptyLabel).left();
            return;
        }
        for (int i = 0; i < entries.size; i++) {
            final int index = i;
            JournalEntry e = entries.get(i);
            String marker = e.isRead() ? "" : "  " + UiStyles.hex(UiStyles.ACCENT) + "*[]";
            Label row = new Label(UiStyles.escape(e.getTitle()) + marker, st.body);
            row.setEllipsis(true);
            row.setTouchable(Touchable.enabled);
            row.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    select(index);
                }
            });
            Image cursor = new Image(st.cursor);
            list.add(cursor).size(8).padRight(5);
            list.add(row).width(LIST_W - 22).left().padBottom(5).row();
            rows.add(row);
            cursors.add(cursor);
        }
    }

    private void rebuildSlots() {
        slotRow.clearChildren();
        for (int i = 0; i < device.getCapacity(); i++) {
            slotRow.add(new Image(i < device.getFragmentCount() ? st.slotFull : st.slotEmpty)).size(12).padRight(3);
        }
        slotRow.add(new Label("Fragmen " + device.getFragmentCount() + "/" + device.getCapacity(), st.dim)).padLeft(5);
    }

    private void select(int index) {
        if (entries.size == 0) {
            categoryLabel.setText("");
            titleLabel.setText("");
            bodyLabel.setText(UiStyles.escape("Jurnal masih kosong. Jelajahi kota dan bicara dengan warga."));
            return;
        }
        selected = index;
        for (int i = 0; i < rows.size; i++) {
            boolean on = i == index;
            rows.get(i).setStyle(on ? st.warm : st.body);
            cursors.get(i).setVisible(on);
        }
        JournalEntry e = entries.get(index);
        categoryLabel.setText(e.getCategory().label);
        titleLabel.setText(UiStyles.escape(e.getTitle()));
        bodyLabel.setText(UiStyles.escape(e.getBody()));
        list.validate();
        Label row = rows.get(index);
        listScroll.layout();
        listScroll.scrollTo(row.getX(), row.getY(), row.getWidth(), row.getHeight());
    }
}
