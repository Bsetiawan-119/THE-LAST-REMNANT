// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : InputHandler.java
//  Bagian  : Core gameplay & world (input pemain)
//  PJ      : Iqbal Aulia Rachman
//  Minggu  : W1 (kerangka InputProcessor), W2 (pemetaan Command + rebinding)
//  Pattern : COMMAND (invoker)   |   LibGDX: InputProcessor
//  Test    : core/src/test/.../input/InputHandlerTest.java
// =============================================================================
package com.kelompok7.lastremnant.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.kelompok7.lastremnant.entity.Player;

/**
 * Mengubah tombol menjadi objek {@link Command}.
 * <ul>
 *   <li><b>held</b>  : dieksekusi tiap frame selama tombol ditahan (gerak).</li>
 *   <li><b>press</b> : dieksekusi sekali saat tombol ditekan (interaksi).</li>
 * </ul>
 * Status tombol dicatat dari event keyDown/keyUp, jadi kelas ini bisa diuji
 * tanpa jendela game (lihat InputHandlerTest).
 */
public class InputHandler extends InputAdapter {

    private final IntMap<Command> heldBindings = new IntMap<>();
    private final IntMap<Command> pressBindings = new IntMap<>();
    private final IntSet heldKeys = new IntSet();
    private final Array<Command> pressedQueue = new Array<>();
    private boolean enabled = true;

    /** Kontrol bawaan: WASD / panah untuk jalan, E / Space / Enter untuk interaksi. */
    public static InputHandler withDefaultBindings() {
        InputHandler h = new InputHandler();
        h.bindHeld(Keys.W, MoveCommand.UP);
        h.bindHeld(Keys.UP, MoveCommand.UP);
        h.bindHeld(Keys.S, MoveCommand.DOWN);
        h.bindHeld(Keys.DOWN, MoveCommand.DOWN);
        h.bindHeld(Keys.A, MoveCommand.LEFT);
        h.bindHeld(Keys.LEFT, MoveCommand.LEFT);
        h.bindHeld(Keys.D, MoveCommand.RIGHT);
        h.bindHeld(Keys.RIGHT, MoveCommand.RIGHT);
        h.bindPress(Keys.E, InteractCommand.INSTANCE);
        h.bindPress(Keys.SPACE, InteractCommand.INSTANCE);
        h.bindPress(Keys.ENTER, InteractCommand.INSTANCE);
        return h;
    }

    // ------------------------------------------------------------------ binding

    public void bindHeld(int keycode, Command command) {
        pressBindings.remove(keycode);
        heldBindings.put(keycode, command);
    }

    public void bindPress(int keycode, Command command) {
        heldBindings.remove(keycode);
        pressBindings.put(keycode, command);
    }

    public void unbind(int keycode) {
        heldBindings.remove(keycode);
        pressBindings.remove(keycode);
    }

    public Command getBinding(int keycode) {
        Command c = heldBindings.get(keycode);
        return c != null ? c : pressBindings.get(keycode);
    }

    // ------------------------------------------------------------------ InputProcessor

    @Override
    public boolean keyDown(int keycode) {
        boolean bound = heldBindings.containsKey(keycode) || pressBindings.containsKey(keycode);
        if (!bound) return false;
        heldKeys.add(keycode);
        Command press = pressBindings.get(keycode);
        if (press != null && enabled) {
            pressedQueue.add(press);
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        return heldKeys.remove(keycode);
    }

    // ------------------------------------------------------------------ per frame

    /** Jalankan semua Command yang aktif ke pemain. Dipanggil oleh ExplorationState. */
    public void handleInput(Player player) {
        if (!enabled) {
            pressedQueue.clear();
            return;
        }
        IntSet.IntSetIterator it = heldKeys.iterator();
        while (it.hasNext) {
            Command c = heldBindings.get(it.next());
            if (c != null) c.execute(player);
        }
        for (Command c : pressedQueue) {
            c.execute(player);
        }
        pressedQueue.clear();
    }

    /**
     * Matikan saat dialog / jurnal terbuka. Tombol yang masih ditahan tetap dicatat
     * supaya saat kembali ke eksplorasi Iwan langsung jalan lagi tanpa harus tekan ulang.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) pressedQueue.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Lupakan semua tombol (mis. saat jendela kehilangan fokus / pindah layar). */
    public void reset() {
        heldKeys.clear();
        pressedQueue.clear();
    }

    public boolean isHeld(int keycode) {
        return heldKeys.contains(keycode);
    }
}
