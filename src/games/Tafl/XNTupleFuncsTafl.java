package games.Tafl;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;

public class XNTupleFuncsTafl
    extends XNTupleBase
    implements XNTupleFuncs, Serializable
{

    @Serial
    private static final long serialVersionUID = 1L;

    public XNTupleFuncsTafl()
    {

    }

    @Override
    public int getNumCells()
    {
        return TaflConfig.TILE_COUNT;
    }

    @Override
    public int getNumPositionValues()
    {
        return 4;
    }

    @Override
    public int getNumPlayers()
    {
        return 2;
    }

    @Override
    public int getNumSymmetries()
    {
        return 3;
    }

    @Override
    public BoardVector getBoardVector(StateObservation so)
    {
        StateObserverTafl stateObs = (StateObserverTafl) so;
        TaflTile[] boardVectorTiles = TaflUtils.boardToVector(stateObs.getBoard());
        int[] boardVectorInt = new int[boardVectorTiles.length];

        for (int i = 0; i < boardVectorInt.length; i++)
        {
            boardVectorInt[i] = boardVectorTiles[i].getToken();
        }

        return new BoardVector(boardVectorInt);
    }

    @Override
    public BoardVector[] symmetryVectors(BoardVector boardVector, int n)
    {
        BoardVector[] symmetries = new BoardVector[3];
        symmetries[0] = boardVector;
        symmetries[1] = mirrorBoard(boardVector, Axis.VERTICAL);
        symmetries[2] = mirrorBoard(boardVector, Axis.HORIZONTAL);
        //symmetries[3] = rotate(boardVector);
        //symmetries[4] = rotate(symmetries[3]);
        //symmetries[5] = rotate(symmetries[4]);
        return symmetries;
    }

    @Override
    public int[] symmetryActions(int actionKey)
    {
        Point[] move = TaflUtils.getMoveFromActionNumber(actionKey);

        // mirror vertically
        // start = 2, 5 -> 4, 5
        Point verticalMirroredStartPoint = new Point(TaflConfig.BOARD_SIZE - move[0].x - 1, move[0].y);
        Point verticalMirroredEndPoint = new Point(TaflConfig.BOARD_SIZE - move[1].x - 1, move[1].y);
        int verticalMirroredAction = TaflUtils.getActionNumberFromMove(verticalMirroredStartPoint, verticalMirroredEndPoint);

        // mirror horizontally
        // start = 2, 5 -> 2, 1
        Point horizontalMirroredStartPoint = new Point(move[0].x, TaflConfig.BOARD_SIZE - move[0].y - 1);
        Point horizontalMirroredEndPoint = new Point(move[1].x, TaflConfig.BOARD_SIZE - move[1].y - 1);
        int horizontalMirroredAction = TaflUtils.getActionNumberFromMove(horizontalMirroredStartPoint, horizontalMirroredEndPoint);

        return new int[] {actionKey, verticalMirroredAction, horizontalMirroredAction};
    }

    @Override
    public int[][] fixedNTuples(int mode)
    {
        int[][] tuples;

        switch (mode)
        {
            case 1:
                tuples = new int[TaflConfig.BOARD_SIZE * 2][TaflConfig.BOARD_SIZE];
                for (int i = 0; i < TaflConfig.BOARD_SIZE; i++)
                {
                    for (int j = 0; j < TaflConfig.BOARD_SIZE; j++)
                    {
                        int position = i * TaflConfig.BOARD_SIZE + j;
                        tuples[i][j] = position;

                        position = j * TaflConfig.BOARD_SIZE + i;
                        tuples[i + TaflConfig.BOARD_SIZE][j] = position;
                    }
                }
                break;
            default:
                throw new RuntimeException("[fixedNTuples] mode=" + mode + " not allowed");
        }

        return tuples;
    }

    @Override
    public String fixedTooltipString()
    {
        // use "<html> ... <br> ... </html>" to get multi-line tooltip text
        return "<html>"
               + "1: board columns + board rows<br>"
               + "</html>";
    }

    private static final int[] fixedModes = {1};

    @Override
    public int[] fixedNTupleModesAvailable()
    {
        return fixedModes;
    }

    @Override
    public HashSet adjacencySet(int iCell)
    {
        HashSet<Integer> adjacencySet = new HashSet<>();
        Point neighbor;
        Point coords = TaflUtils.cellToPoint(iCell);
        int x = coords.x;
        int y = coords.y;
        if (TaflUtils.isValidTile(x - 1, y))
        {
            neighbor = new Point(x - 1, y);
            adjacencySet.add(TaflUtils.pointToCell(neighbor));
        }
        if (TaflUtils.isValidTile(x + 1, y))
        {
            neighbor = new Point(x + 1, y);
            adjacencySet.add(TaflUtils.pointToCell(neighbor));
        }
        if (TaflUtils.isValidTile(x, y - 1))
        {
            neighbor = new Point(x, y - 1);
            adjacencySet.add(TaflUtils.pointToCell(neighbor));
        }
        if (TaflUtils.isValidTile(x, y + 1))
        {
            neighbor = new Point(x, y + 1);
            adjacencySet.add(TaflUtils.pointToCell(neighbor));
        }
        return adjacencySet;
    }

    private enum Axis
    {
        HORIZONTAL, VERTICAL
    }

    /**
     * Mirrors the board along the given axis.
     *
     * @param boardVector Game board vector
     * @param axis        Axis along which to mirror
     * @return Mirrored board
     */
    private BoardVector mirrorBoard(BoardVector boardVector, Axis axis)
    {
        int[] mirroredVector = boardVector.bvec.clone();

        if (axis == Axis.VERTICAL)
        {
            //Subdivide into chunks of BOARD_SIZE elements and reverse each
            //Example for BOARD_SIZE=3:
            //Before: 1,2,3, 4,5,6, 7,8,9
            //After:  3,2,1, 6,5,4, 9,8,7
            for (int i = 0; i < TaflConfig.BOARD_SIZE; i++)
            {
                int[] tmp = new int[TaflConfig.BOARD_SIZE];
                for (int j = 0; j < ((TaflConfig.BOARD_SIZE + 1) / 2); j++)
                {
                    tmp[j] = mirroredVector[i * TaflConfig.BOARD_SIZE + j];
                    mirroredVector[i * TaflConfig.BOARD_SIZE + j] = mirroredVector[i * TaflConfig.BOARD_SIZE + TaflConfig.BOARD_SIZE - j - 1];
                    mirroredVector[i * TaflConfig.BOARD_SIZE + TaflConfig.BOARD_SIZE - j - 1] = tmp[j];
                }
            }
        }
        else if (axis == Axis.HORIZONTAL)
        {
            //Swap the places of chunks of BOARD_SIZE elements from front and end until center is reached
            //Example for BOARD_SIZE=3:
            //Before: 1,2,3, 4,5,6, 7,8,9
            //After:  7,8,9, 4,5,6, 1,2,3
            for (int i = 0; i < ((TaflConfig.BOARD_SIZE + 1) / 2); i++)
            {
                int[] tmp = new int[TaflConfig.BOARD_SIZE];
                for (int j = 0; j < TaflConfig.BOARD_SIZE; j++)
                {
                    tmp[j] = mirroredVector[i * TaflConfig.BOARD_SIZE + j];
                    mirroredVector[i * TaflConfig.BOARD_SIZE + j] = mirroredVector[(TaflConfig.BOARD_SIZE - i - 1) * TaflConfig.BOARD_SIZE + j];
                    mirroredVector[(TaflConfig.BOARD_SIZE - i - 1) * TaflConfig.BOARD_SIZE + j] = tmp[j];
                }
            }
        }

        return new BoardVector(mirroredVector);
    }
}
