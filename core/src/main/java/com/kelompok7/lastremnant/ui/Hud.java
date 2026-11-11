// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : Hud.java
//  Bagian  : UI & game flow (tampilan di atas layar saat eksplorasi)
//  PJ      : Budiman Setiawan
//  Minggu  : W2
//  Pattern : OBSERVER (pengamat)  ->  mendengarkan StoryManager (tujuan) & Journal (entri baru)
// =============================================================================
package com.kelompok7.lastremnant.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Queue;
import com.kelompok7.lastremnant.memory.Journal;
import com.kelompok7.lastremnant.memory.JournalEntry;
import com.kelompok7.lastremnant.memory.JournalListener;
import com.kelompok7.lastremnant.memory.MemoryDevice;
import com.kelompok7.lastremnant.story.StoryListener;

/**
 * Isi HUD:
 * <pre>
 *  +-------------------+                         +------------------+
 *  | TUJUAN            |      [ toast ]          | [ikon] THE REMNANT|
 *  | Antar paket ke... |                         |  <> <> <>  0/3   |
 *  +-------------------+                         +------------------+
 *
 *                    [E] Bicara dengan Bu Sari          [J] Jurnal (1 baru)
 * </pre>
 */
public class Hud implements StoryListener, JournalListener {

    private static final float TOAST_TIME = 2.6f;

    private final UiStyles st;
    private final MemoryDevice device;
    private final Journal journal;

    private final Table root = new Table();
    private final Table objectivePanel = new Table();
    private final Label objectiveLabel;
    private final Table devicePanel = new Table();
    private final Image[] slots;
    private final Label fragmentLabel;
    private final Container<Label> promptBox;
    private final Label promptLabel;
    private final Label journalHint;
    private final Container<Label> toastBox;
    private final Label toastLabel;
    private final Table locationBanner = new Table();
    private final Label locationTitle;
    private final Label locationSub;

    private final Queue<String> toastQueue = new Queue<>();
    private float toastTimer;
    private String currentPrompt;
    private int lastUnread = -1;
    private int lastFragments = -1;

    public Hud(Stage stage, UiStyles st, MemoryDevice device, Journal journal) {
        this.st = st;
        this.device = device;
        this.journal = journal;

        // --- tujuan (kiri atas) ---
        objectiveLabel = new Label("", st.body);
        objectiveLabel.setWrap(true);
        objectivePanel.setBackground(st.panel);
        objectivePanel.pad(8, 10, 9, 10);
        objectivePanel.add(new Label("TUJUAN", st.warm)).left().row();
        objectivePanel.add(objectiveLabel).width(196).left().padTop(2);
        objectivePanel.setVisible(false);

        // --- status The Remnant (kanan atas) ---
        devicePanel.setBackground(st.panel);
        devicePanel.pad(7, 10, 8, 10);
        Table top = new Table();
        top.add(new Image(st.remnantIcon)).size(16).padRight(6);
        top.add(new Label("THE REMNANT", st.accent));
        devicePanel.add(top).left().row();
        Table slotRow = new Table();
        slots = new Image[device.getCapacity()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new Image(st.slotEmpty);
            slotRow.add(slots[i]).size(12).padRight(3);
        }
        fragmentLabel = new Label("", st.dim);
        slotRow.add(fragmentLabel).padLeft(4);
        devicePanel.add(slotRow).left().padTop(4);
        devicePanel.setVisible(false);

        // --- petunjuk interaksi (bawah tengah) ---
        promptLabel = new Label("", st.plain);
        promptBox = new Container<>(promptLabel);
        promptBox.setBackground(st.panel);
        promptBox.pad(5, 10, 6, 10);
        promptBox.setVisible(false);

        journalHint = new Label("", st.plain);

        root.setFillParent(true);
        root.setTouchable(Touchable.disabled);
        root.pad(10);
        root.add(objectivePanel).top().left().expandX();
        root.add(devicePanel).top().right().row();
        root.add().colspan(2).expand().row();
        root.add(promptBox).colspan(2).bottom().padBottom(26).row();
        root.add(journalHint).colspan(2).right().bottom();
        stage.addActor(root);

        // --- toast & banner lokasi (melayang, tidak ikut tabel) ---
        toastLabel = new Label("", st.body);
        toastBox = new Container<>(toastLabel);
        toastBox.setBackground(st.panel);
        toastBox.pad(5, 12, 6, 12);
        toastBox.setTouchable(Touchable.disabled);
        toastBox.getColor().a = 0f;
        stage.addActor(toastBox);

        locationTitle = new Label("", st.warm);
        locationTitle.setFontScale(2f);
        locationSub = new Label("", st.body);
        locationBanner.add(locationTitle).row();
        locationBanner.add(locationSub).padTop(2);
        locationBanner.setTouchable(Touchable.disabled);
        locationBanner.getColor().a = 0f;
        stage.addActor(locationBanner);

        refreshCounters();
    }

    /** Dipanggil tiap frame oleh GameScreen. */
    public void update(float delta) {
        refreshCounters();

        // antrean toast: satu per satu, supaya tidak menumpuk
        if (toastTimer > 0f) {
            toastTimer -= delta;
        } else if (toastQueue.notEmpty()) {
            String msg = toastQueue.removeFirst();
            toastLabel.setText(UiStyles.escape(msg));
            toastBox.pack();
            Stage stage = toastBox.getStage();
            toastBox.setPosition(Math.round((stage.getWidth() - toastBox.getWidth()) / 2f),
                    Math.round(stage.getHeight() - toastBox.getHeight() - 14f));
            toastBox.clearActions();
            toastBox.addAction(Actions.sequence(
                    Actions.alpha(0f), Actions.fadeIn(0.2f),
                    Actions.delay(TOAST_TIME - 0.6f), Actions.fadeOut(0.4f)));
            toastTimer = TOAST_TIME;
        }
    }

    private void refreshCounters() {
        devicePanel.setVisible(device.isActive());
        int frags = device.getFragmentCount();
        if (frags != lastFragments) {
            lastFragments = frags;
            for (int i = 0; i < slots.length; i++) {
                slots[i].setDrawable(i < frags ? st.slotFull : st.slotEmpty);
            }
            fragmentLabel.setText("Fragmen " + frags + "/" + device.getCapacity());
        }
        int unread = journal.getUnreadCount();
        if (unread != lastUnread) {
            lastUnread = unread;
            String dim = UiStyles.hex(UiStyles.DIM);
            journalHint.setText(unread > 0
                    ? dim + "[[J] Jurnal[]  " + UiStyles.hex(UiStyles.ACCENT) + "(" + unread + " baru)[]"
                    : dim + "[[J] Jurnal    [[Esc] Menu[]");
        }
    }

    // ------------------------------------------------------------------ API

    /** null = sembunyikan petunjuk. */
    public void setPrompt(String text) {
        if (text == null) {
            if (currentPrompt != null) promptBox.setVisible(false);
            currentPrompt = null;
            return;
        }
        if (text.equals(currentPrompt)) return;
        currentPrompt = text;
        promptLabel.setText(UiStyles.hex(UiStyles.WARM) + "[[E][]  "
                + UiStyles.hex(UiStyles.CREAM) + UiStyles.escape(text) + "[]");
        promptBox.setVisible(true);
        promptBox.clearActions();
        promptBox.addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.12f)));
    }

    public void showToast(String message) {
        toastQueue.addLast(message);
    }

    public void showLocation(String title, String subtitle) {
        locationTitle.setText(UiStyles.escape(title));
        locationSub.setText(UiStyles.escape(subtitle));
        locationBanner.pack();
        Stage stage = locationBanner.getStage();
        locationBanner.setPosition(Math.round((stage.getWidth() - locationBanner.getWidth()) / 2f),
                Math.round(stage.getHeight() * 0.62f));
        locationBanner.clearActions();
        locationBanner.addAction(Actions.sequence(
                Actions.delay(0.4f), Actions.fadeIn(0.8f), Actions.delay(2.2f), Actions.fadeOut(1f)));
    }

    /** Sembunyikan elemen eksplorasi saat jurnal terbuka (toast tetap muncul). */
    public void setExplorationVisible(boolean visible) {
        root.setVisible(visible);
    }

    // ------------------------------------------------------------------ Observer

    @Override
    public void onObjectiveChanged(String objective) {
        boolean has = objective != null && !objective.isEmpty();
        objectivePanel.setVisible(has);
        if (!has) return;
        objectiveLabel.setText(UiStyles.escape(objective));
        objectivePanel.clearActions();
        objectivePanel.addAction(Actions.sequence(
                Actions.alpha(0.2f), Actions.fadeIn(0.5f)));
    }

    @Override
    public void onEntryAdded(JournalEntry entry) {
        showToast("Jurnal  -  " + entry.getCategory().label + ": " + entry.getTitle());
    }
}
