// =============================================================================
//  THE LAST REMNANT · Kelompok 7
//  File    : GameStateManager.java
//  Bagian  : UI & game flow (pengatur pergantian state)
//  PJ      : Budiman Setiawan
//  Minggu  : W2  =  Exploration, Dialogue, Journal
//            W3  =  PuzzleState      W4 = PauseState, EndingState
//  Pattern : STATE (context). Tumpukan (stack) agar overlay bisa kembali ke state sebelumnya.
//  Test    : core/src/test/.../state/GameStateManagerTest.java
// =============================================================================
package com.kelompok7.lastremnant.state;

import com.badlogic.gdx.utils.Array;

/**
 * Menyimpan tumpukan state. Yang paling atas = yang aktif.
 * <pre>
 *   set(Exploration)            [Exploration]
 *   push(Dialogue)              [Exploration, Dialogue]   (eksplorasi di-pause)
 *   pop()                       [Exploration]             (eksplorasi di-resume)
 * </pre>
 */
public class GameStateManager {

    private final Array<GameState> stack = new Array<>();

    /** Buang semua state lalu jadikan {@code state} satu-satunya. */
    public void set(GameState state) {
        while (stack.size > 0) {
            stack.pop().exit();
        }
        stack.add(state);
        state.enter();
    }

    public void push(GameState state) {
        if (state == null) throw new IllegalArgumentException("state null");
        GameState top = current();
        if (top != null) top.pause();
        stack.add(state);
        state.enter();
    }

    public GameState pop() {
        if (stack.size == 0) throw new IllegalStateException("Tidak ada state untuk di-pop");
        GameState removed = stack.pop();
        removed.exit();
        GameState top = current();
        if (top != null) top.resume();
        return removed;
    }

    public GameState current() {
        return stack.size == 0 ? null : stack.peek();
    }

    public boolean isCurrent(Class<? extends GameState> type) {
        GameState c = current();
        return c != null && type.isInstance(c);
    }

    public void update(float delta) {
        GameState c = current();
        if (c != null) c.update(delta);
    }

    public boolean keyDown(int keycode) {
        GameState c = current();
        return c != null && c.keyDown(keycode);
    }

    public int depth() {
        return stack.size;
    }
}
