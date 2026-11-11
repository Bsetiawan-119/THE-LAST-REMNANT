// =============================================================================
//  THE LAST REMNANT · Kelompok 7  ·  Unit test InputHandler + Command + Player
//  PJ : Iqbal Aulia Rachman  ·  Minggu : W2
// =============================================================================
package com.kelompok7.lastremnant.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.Input.Keys;
import com.kelompok7.lastremnant.entity.Direction;
import com.kelompok7.lastremnant.entity.Player;
import com.kelompok7.lastremnant.util.Constants;
import org.junit.Before;
import org.junit.Test;

public class InputHandlerTest {

    private static final float DT = 0.1f;
    private InputHandler input;
    private Player player;

    @Before
    public void setUp() {
        input = InputHandler.withDefaultBindings();
        player = new Player(100, 100, null);       // tanpa collision & tanpa grafis
    }

    private void frame() {
        input.handleInput(player);
        player.update(DT);
    }

    @Test
    public void tahanDBergerakKeKanan() {
        input.keyDown(Keys.D);
        frame();
        assertEquals(100 + Constants.PLAYER_SPEED * DT, player.getX(), 0.001f);
        assertEquals(Direction.RIGHT, player.getFacing());
        assertTrue(player.isMoving());
    }

    @Test
    public void lepasTombolBerhenti() {
        input.keyDown(Keys.W);
        frame();
        input.keyUp(Keys.W);
        float y = player.getY();
        frame();
        assertEquals(y, player.getY(), 0.0001f);
        assertFalse(player.isMoving());
    }

    @Test
    public void diagonalTidakLebihCepat() {
        input.keyDown(Keys.D);
        input.keyDown(Keys.W);
        frame();
        float dx = player.getX() - 100;
        float dy = player.getY() - 100;
        assertEquals(Constants.PLAYER_SPEED * DT, (float) Math.sqrt(dx * dx + dy * dy), 0.001f);
    }

    @Test
    public void tombolBerlawananSalingMeniadakan() {
        input.keyDown(Keys.A);
        input.keyDown(Keys.D);
        frame();
        assertEquals(100f, player.getX(), 0.0001f);
    }

    @Test
    public void interaksiHanyaSekaliPerTekan() {
        input.keyDown(Keys.E);
        input.handleInput(player);
        assertTrue(player.consumeInteractRequest());
        input.handleInput(player);                 // E masih ditahan, tapi bukan tekan baru
        assertFalse(player.consumeInteractRequest());
    }

    @Test
    public void saatDinonaktifkanTidakAdaAksi() {
        input.setEnabled(false);
        input.keyDown(Keys.E);
        input.keyDown(Keys.D);
        frame();
        assertFalse(player.consumeInteractRequest());
        assertEquals(100f, player.getX(), 0.0001f);
    }

    @Test
    public void tombolDitahanTetapDiingatSetelahDiaktifkanLagi() {
        input.setEnabled(false);
        input.keyDown(Keys.D);
        input.setEnabled(true);
        frame();
        assertTrue(player.getX() > 100f);
    }

    @Test
    public void kontrolBisaDiganti() {
        input.bindHeld(Keys.L, MoveCommand.RIGHT);
        input.unbind(Keys.D);
        assertFalse(input.keyDown(Keys.D));        // tidak lagi dipakai
        input.keyDown(Keys.L);
        frame();
        assertTrue(player.getX() > 100f);
        assertSame(MoveCommand.RIGHT, input.getBinding(Keys.L));
    }

    @Test
    public void tombolTakTerpakaiDiteruskan() {
        assertFalse(input.keyDown(Keys.J));        // J dipakai GameState, bukan InputHandler
    }

    @Test
    public void probeInteraksiMengikutiArahHadap() {
        player.setFacing(Direction.UP);
        assertTrue(player.getInteractionProbe().y >= player.getY());
        player.setFacing(Direction.DOWN);
        assertTrue(player.getInteractionProbe().y < player.getY());
        player.setFacing(Direction.LEFT);
        assertTrue(player.getInteractionProbe().x < player.getHitbox().x);
    }

    @Test
    public void arahDariVektor() {
        assertEquals(Direction.UP, Direction.fromVector(0.2f, 1f, Direction.DOWN));
        assertEquals(Direction.LEFT, Direction.fromVector(-1f, 0.3f, Direction.DOWN));
        assertEquals(Direction.RIGHT, Direction.fromVector(0f, 0f, Direction.RIGHT));
        // diagonal sempurna mempertahankan arah lama jika masih searah
        assertEquals(Direction.RIGHT, Direction.fromVector(1f, 1f, Direction.RIGHT));
        assertEquals(Direction.LEFT, Direction.parse("left", Direction.DOWN));
        assertEquals(Direction.DOWN, Direction.parse("ngawur", Direction.DOWN));
    }
}
