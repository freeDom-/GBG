package games.Tafl;

import games.BoardVector;
import games.StateObsWithBoardVector;
import games.StateObservation;
import games.XNTupleFuncs;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;

public class XNTupleFuncsTafl implements XNTupleFuncs, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public XNTupleFuncsTafl() {

    }

    @Override
    public boolean instantiateAfterLoading() {
        return false;
    }

    @Override
    public int getNumCells() {
        return 0;
    }

    @Override
    public int getNumPositionValues() {
        return 0;
    }

    @Override
    public int[] getPositionValuesVector() {
        return new int[0];
    }

    @Override
    public int getNumPlayers() {
        return 0;
    }

    @Override
    public int getNumSymmetries() {
        return 0;
    }

    @Override
    public BoardVector getBoardVector(StateObservation so) {
        return null;
    }

    @Override
    public BoardVector makeBoardVectorEachCellDifferent() {
        return null;
    }

    @Override
    public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
        return new BoardVector[0];
    }

    @Override
    public BoardVector[] symmetryVectors(StateObsWithBoardVector curSOWB, int n) {
        return new BoardVector[0];
    }

    @Override
    public int[] symmetryActions(int actionKey) {
        return new int[0];
    }

    @Override
    public boolean useActionMap() {
        return false;
    }

    @Override
    public int[][] fixedNTuples(int mode) {
        return new int[0][];
    }

    @Override
    public String fixedTooltipString() {
        return "";
    }

    @Override
    public int[] fixedNTupleModesAvailable() {
        return new int[0];
    }

    @Override
    public HashSet adjacencySet(int iCell) {
        return null;
    }
}
