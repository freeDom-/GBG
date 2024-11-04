package games.Tafl;

import controllers.PlayAgent;
import games.Arena;
import games.GameBoard;
import games.GameBoardBase;
import games.StateObservation;
import tools.Types;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class GameBoardTafl extends GameBoardBase implements GameBoard {

    protected StateObserverTafl m_so;
    protected Random rand;
    final boolean verbose = false;

    private transient GameBoardTaflGui m_gameGui = null;

    public GameBoardTafl(Arena arena) {
        super(arena);

        m_so = new StateObserverTafl();
        rand = new Random(System.currentTimeMillis());

        if (getArena().hasGUI() && m_gameGui == null) {
            m_gameGui = new GameBoardTaflGui(this);
        }
    }

    @Override
    public void initialize() {
    }

    /**
     * update game-specific parameters from {@link Arena}'s param tabs
     */
    @Override
    public void updateParams() {
    }

    @Override
    public void clearBoard(boolean boardClear, boolean vClear, Random cmpRand) {
        if (boardClear) {
            m_so = new StateObserverTafl();
        } else if (vClear) {
            m_so.clearTileValues();
        }
        // considerable speed-up during training (!)
        if (m_gameGui != null && getArena().taskState != Arena.Task.TRAIN) {
            m_gameGui.clearBoard(boardClear, vClear);
        }
    }

    @Override
    public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard) {
        setStateObs(so);    // asserts that so is StateObserverTafl
        StateObserverTafl soTafl = (StateObserverTafl) so;

        if (m_gameGui != null) {
            m_gameGui.updateBoard(soTafl, withReset, showValueOnGameboard);
        }

        if (verbose) {
            // TODO: Add logging of features
        }
    }

    @Override
    public void showGameBoard(Arena arena, boolean alignToMain) {
        if (m_gameGui != null) {
            m_gameGui.showGameBoard(arena, alignToMain);
        }
    }

    @Override
    public void toFront() {
        if (m_gameGui != null) {
            m_gameGui.toFront();
        }
    }

    @Override
    public void destroy() {
        if (m_gameGui != null) {
            m_gameGui.destroy();
        }
    }

    @Override
    public String getSubDir() {
        DecimalFormat form = new DecimalFormat("00");
        return form.format(TaflConfig.BOARD_SIZE);
    }

    @Override
    public void enableInteraction(boolean enable) {
        if (m_gameGui != null) {
            m_gameGui.enableInteraction(enable);
        }
    }

    @Override
    public StateObservation getDefaultStartState(Random cmpRand) {
        clearBoard(true, true, null);
        return m_so;
    }

    @Override
    public void setStateObs(StateObservation so) {
        StateObserverTafl soTafl;

        if (so != null) {
            assert (so instanceof StateObserverTafl) : "StateObservation 'so' is not an instance of StateObserverTafl";
            soTafl = (StateObserverTafl) so;
            m_so = soTafl; //.copy();
        }
    }

    @Override
    public StateObservation getStateObs() {
        return m_so;
    }

    @Override
    public StateObservation chooseStartState(PlayAgent pa) {
        return chooseStartState();
    }

    /**
     * @return a start state which is with probability 0.5 the empty board
     * start state and with probability 0.5 one of the possible one-ply
     * successors
     */
    @Override
    public StateObservation chooseStartState() {
        clearBoard(true, true, null);
        if (rand.nextDouble() > 0.5) {
            // choose randomly one of the possible actions in default
            // start state and advance m_so by one ply
            ArrayList<Types.ACTIONS> acts = m_so.getAvailableActions();
            int i = rand.nextInt(acts.size());
            m_so.advance(acts.get(i), null);
        }
        return m_so;
    }
}
