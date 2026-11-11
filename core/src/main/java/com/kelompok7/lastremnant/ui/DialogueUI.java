// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : DialogueUI.java
//  Bagian  : UI & game flow (kotak dialog: potret, nama, teks mesin ketik, pilihan)
//  PJ      : Budiman Setiawan
//  Minggu  : W2 = versi awal, wajib ada agar milestone "Prototype playable" 17 Nov bisa dimainkan
//            W3 = penyempurnaan tampilan + konten NPC lengkap dari Carlos
//  LibGDX  : Scene2D (Table, Label, Image, ClickListener)
// =============================================================================
package com.kelompok7.lastremnant.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kelompok7.lastremnant.assets.GameAssets;
import com.kelompok7.lastremnant.dialogue.DialogueChoice;
import com.kelompok7.lastremnant.dialogue.DialogueNode;
import com.kelompok7.lastremnant.dialogue.DialogueSession;
import com.kelompok7.lastremnant.story.CharacterDatabase;
import com.kelompok7.lastremnant.story.CharacterInfo;
import com.kelompok7.lastremnant.util.Constants;

/**
 * Menampilkan {@link DialogueSession} milik Carlos. Kelas ini TIDAK tahu isi cerita,
 * hanya cara menampilkannya:
 * <ul>
 *   <li>efek mesin ketik + bunyi "blip" dengan nada khas tiap tokoh,</li>
 *   <li>jeda kecil setelah tanda baca agar ritme bacaan terasa natural,</li>
 *   <li>warna teks berbeda untuk narasi, kilas balik memori, dan suara The Remnant,</li>
 *   <li>pilihan bisa dipilih dengan W/S, angka 1-4, atau klik mouse.</li>
 * </ul>
 */
public class DialogueUI {

    private static final float PANEL_W = 600f;
    private static final float PANEL_H = 100f;
    private static final float PORTRAIT_SCALE = 4f;
    private static final String HIDDEN = "[#00000000]";

    private final UiStyles st;
    private final GameAssets assets;
    private final CharacterDatabase characters;

    private final Table root = new Table();
    private final Table choiceBox = new Table();
    private final Table nameTag = new Table();
    private final Label nameLabel;
    private final Table panel = new Table();
    private final Image portrait = new Image();
    private final Cell<Image> portraitCell;
    private final Label text;
    private final Image arrow;
    private final Array<Label> choiceLabels = new Array<>();
    private final Array<Image> choiceCursors = new Array<>();

    private DialogueSession session;
    private Runnable onClosed;
    private String fullText = "";
    private float shown;
    private int revealed;
    private float punctuationPause;
    private int blipCounter;
    private float voicePitch = 1f;
    private int selected;
    private float time;
    private boolean open;

    public DialogueUI(Stage stage, UiStyles st, GameAssets assets, CharacterDatabase characters) {
        this.st = st;
        this.assets = assets;
        this.characters = characters;

        nameLabel = new Label("", st.ink);
        nameTag.setBackground(st.tag);
        nameTag.pad(2, 8, 3, 8);
        nameTag.add(nameLabel);

        text = new Label("", st.body);
        text.setWrap(true);
        text.setAlignment(Align.topLeft);

        arrow = new Image(st.indicator);

        panel.setBackground(st.panel);
        panel.pad(13, 14, 10, 14);
        panel.setTouchable(Touchable.enabled);
        portraitCell = panel.add(portrait).size(64, 56).top().padRight(14);
        panel.add(text).expand().fill().top().left().padTop(1);
        panel.add(arrow).size(8).bottom().right().padLeft(6);
        panel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (session != null && (!session.isWaitingForChoice() || isTyping())) confirm();
            }
        });

        choiceBox.setBackground(st.panel);
        choiceBox.pad(8, 10, 9, 14);
        choiceBox.setVisible(false);

        root.setFillParent(true);
        root.bottom().padBottom(10);
        root.add(choiceBox).right().padRight(30).padBottom(8).row();
        root.add(nameTag).left().padLeft(30).padBottom(-6).row();
        root.add(panel).width(PANEL_W).height(PANEL_H);
        root.setVisible(false);
        stage.addActor(root);
        nameTag.toFront();
    }

    // ------------------------------------------------------------------ alur

    /**
     * Buka dialog. {@code onClosed} dipanggil sekali saat sesi selesai
     * (DialogueState memakainya untuk kembali ke eksplorasi).
     */
    public void show(DialogueSession session, Runnable onClosed) {
        this.session = session;
        this.onClosed = onClosed;
        open = true;
        session.start();
        if (session.isFinished()) {
            close();
            return;
        }
        root.setVisible(true);
        root.clearActions();
        root.addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.12f)));
        showCurrentNode();
    }

    /** Tombol konfirmasi: percepat teks -> pilih opsi -> lanjut baris berikutnya. */
    public void confirm() {
        if (!open) return;
        if (isTyping()) {
            revealAll();
            return;
        }
        if (session.isWaitingForChoice()) {
            assets.play("confirm", 0.6f, 1f);
            session.choose(selected);
        } else {
            session.advance();
        }
        if (session.isFinished()) {
            close();
        } else {
            showCurrentNode();
        }
    }

    public void moveSelection(int delta) {
        if (!open || isTyping() || !session.isWaitingForChoice()) return;
        int n = choiceLabels.size;
        setSelected((selected + delta + n) % n);
        assets.play("select", 0.5f, 1f);
    }

    /** Pilih langsung dengan tombol angka (index mulai 0). */
    public void chooseIndex(int index) {
        if (!open || isTyping() || !session.isWaitingForChoice()) return;
        if (index < 0 || index >= choiceLabels.size) return;
        setSelected(index);
        confirm();
    }

    public void update(float delta) {
        if (!open) return;
        time += delta;
        if (isTyping()) typeStep(delta);
        boolean waitingInput = !isTyping() && !session.isWaitingForChoice();
        arrow.setVisible(waitingInput);
        arrow.getColor().a = MathUtils.sin(time * 6f) > -0.2f ? 1f : 0.25f;
    }

    /** Tutup paksa (mis. pemain kembali ke menu di tengah dialog), tanpa callback. */
    public void forceHide() {
        open = false;
        onClosed = null;
        root.setVisible(false);
    }

    public boolean isOpen() {
        return open;
    }

    // ------------------------------------------------------------------ internal

    private void close() {
        open = false;
        root.clearActions();
        root.setVisible(false);
        choiceBox.setVisible(false);
        Runnable callback = onClosed;
        onClosed = null;
        if (callback != null) callback.run();
    }

    private void showCurrentNode() {
        DialogueNode node = session.getCurrentNode();
        CharacterInfo who = characters.get(node.getSpeakerId());
        voicePitch = who.getVoicePitch();

        // nama
        boolean hasName = !who.getName().isEmpty();
        nameTag.setVisible(hasName);
        nameLabel.setText(UiStyles.escape(who.getName()));

        // potret (hilang untuk narasi & suara dari masa lalu)
        TextureRegion face = assets.portrait(who.getPortraitKey());
        if (face != null) {
            portrait.setDrawable(new TextureRegionDrawable(face));
            portraitCell.size(face.getRegionWidth() * PORTRAIT_SCALE, face.getRegionHeight() * PORTRAIT_SCALE)
                    .padRight(14);
            portrait.setVisible(true);
        } else {
            portrait.setVisible(false);
            portraitCell.size(0, 0).padRight(0);
        }
        panel.invalidate();

        // warna teks sesuai gaya penutur
        switch (who.getStyle()) {
            case ECHO: text.setStyle(st.echo); break;
            case SYSTEM: text.setStyle(st.accent); break;
            case NARRATION: text.setStyle(st.dim); break;
            default: text.setStyle(st.body); break;
        }

        fullText = node.getText();
        shown = 0f;
        revealed = 0;
        punctuationPause = 0f;
        blipCounter = 0;
        applyText();
        buildChoices();
        choiceBox.setVisible(false);
    }

    private void typeStep(float delta) {
        if (punctuationPause > 0f) {
            punctuationPause -= delta;
            return;
        }
        shown += delta * Constants.TYPE_SPEED;
        int target = Math.min((int) shown, fullText.length());
        while (revealed < target) {
            char c = fullText.charAt(revealed++);
            if (Character.isLetterOrDigit(c) && (blipCounter++ % 2 == 0)) {
                assets.play("blip", 0.32f, voicePitch * MathUtils.random(0.96f, 1.04f));
            }
            if (c == '.' || c == '?' || c == '!') {
                punctuationPause = 0.18f;
                shown = revealed;
                break;
            }
            if (c == ',' || c == ';') {
                punctuationPause = 0.08f;
                shown = revealed;
                break;
            }
        }
        applyText();
        if (!isTyping()) onTypingFinished();
    }

    private void revealAll() {
        revealed = fullText.length();
        applyText();
        onTypingFinished();
    }

    private void onTypingFinished() {
        if (session.isWaitingForChoice()) {
            choiceBox.setVisible(true);
            choiceBox.clearActions();
            choiceBox.addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.15f)));
        }
    }

    /**
     * Sisa teks tetap dimasukkan tapi transparan, supaya pemenggalan baris sudah
     * final sejak huruf pertama (kata tidak "melompat" ke baris bawah saat diketik).
     */
    private void applyText() {
        if (revealed >= fullText.length()) {
            text.setText(UiStyles.escape(fullText));
        } else {
            text.setText(UiStyles.escape(fullText.substring(0, revealed)) + HIDDEN
                    + UiStyles.escape(fullText.substring(revealed)) + "[]");
        }
    }

    private void buildChoices() {
        choiceBox.clearChildren();
        choiceLabels.clear();
        choiceCursors.clear();
        Array<DialogueChoice> choices = session.getVisibleChoices();
        for (int i = 0; i < choices.size; i++) {
            final int index = i;
            Image cursor = new Image(st.cursor);
            Label label = new Label(UiStyles.escape((i + 1) + ".  " + choices.get(i).getText()), st.body);
            label.setTouchable(Touchable.enabled);
            label.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer == -1 && index != selected) setSelected(index);
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    setSelected(index);
                    confirm();
                }
            });
            choiceBox.add(cursor).size(8).padRight(5);
            choiceBox.add(label).left().padTop(i == 0 ? 0 : 3).row();
            choiceCursors.add(cursor);
            choiceLabels.add(label);
        }
        setSelected(0);
    }

    private void setSelected(int index) {
        selected = index;
        for (int i = 0; i < choiceLabels.size; i++) {
            boolean on = i == index;
            choiceLabels.get(i).setStyle(on ? st.warm : st.body);
            choiceCursors.get(i).setVisible(on);
        }
    }

    private boolean isTyping() {
        return revealed < fullText.length();
    }
}
