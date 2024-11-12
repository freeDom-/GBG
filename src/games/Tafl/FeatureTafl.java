package games.Tafl;

import games.Feature;
import games.StateObservation;

import java.io.Serial;
import java.io.Serializable;

public class FeatureTafl
    implements Feature, Serializable
{

    @Serial
    private static final long serialVersionUID = 1L;

    private int featMode;

    public FeatureTafl(int featMode)
    {
        this.featMode = featMode;
    }

    @Override
    public double[] prepareFeatVector(StateObservation so)
    {
        StateObserverTafl stateObs = (StateObserverTafl) so;

        switch (featMode)
        {
            case 0:
                return createFeatureVector0(stateObs.getPlayer(), stateObs.getBoard(), stateObs.getLastBoards());
            case 1:
                return createFeatureVector1(stateObs.getPlayer(), stateObs.getBoard(), stateObs.getLastBoards(), stateObs.getKing());
            default:
                System.out.println("Unknown feature mode, defaulting to feature mode 0");
                return createFeatureVector0(stateObs.getPlayer(), stateObs.getBoard(), stateObs.getLastBoards());
        }
    }

    @Override
    public String stringRepr(double[] featVec)
    {
        StringBuilder sb = new StringBuilder();
        for (double aFeatVec : featVec)
        {
            sb.append(aFeatVec);
            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        return sb.toString();
    }

    @Override
    public int getFeatmode()
    {
        return featMode;
    }

    @Override
    public int[] getAvailFeatmode()
    {
        return new int[] {0, 1};
    }

    @Override
    public int getInputSize(int featMode)
    {
        switch (featMode)
        {
            case 0:
                return (TaflConfig.TILE_COUNT + 1) * 6;
            default:
                throw new RuntimeException("Unknown featmode: " + featMode);
        }
    }

    /**
     * For the current board and the last 5 boards:
     * Converts the raw board data to a vector for the current board and adds one feature indicating which player's turn it is.
     *
     * @param player The player who has the next move
     * @param board  Current board array
     * @return A vector containing all the features described above
     */
    public double[] createFeatureVector0(int player, TaflTile[][] board, TaflTile[][][] lastBoards)
    {
        int index = 0;
        double[] inputVector = new double[(TaflConfig.TILE_COUNT + 1) * 6];
        TaflTile[][][] allBoards = new TaflTile[6][TaflConfig.BOARD_SIZE][TaflConfig.BOARD_SIZE];
        allBoards[0] = board;
        System.arraycopy(lastBoards, 0, allBoards, 1, 5);

        for (int i = 0; i < allBoards.length; i++)
        {
            for (int y = 0; y < TaflConfig.BOARD_SIZE; y++)
            {
                for (int x = 0; x < TaflConfig.BOARD_SIZE; x++)
                {
                    double v = board[y][x].getToken();
                    //index = (y * TaflConfig.BOARD_SIZE + x) + i * (TaflConfig.TILE_COUNT + 1);
                    inputVector[index] = v;
                    index++;
                }
            }
            //index = TaflConfig.TILE_COUNT + i * (TaflConfig.TILE_COUNT + 1);
            inputVector[index] = player;
            index++;
        }

        return inputVector;
    }

    /**
     * For the current board and the last 5 boards:
     * Converts the raw board data to a vector for the current board and adds one feature indicating which player's turn it is.
     * Also contains the row and column where the king is positioned
     *
     * @param player The player who has the next move
     * @param board  Current board array
     * @param king   The king token
     * @return A vector containing all the features described above
     */
    public double[] createFeatureVector1(int player, TaflTile[][] board, TaflTile[][][] lastBoards, TaflTile king)
    {
        int index = 0;
        double[] inputVector = new double[(TaflConfig.TILE_COUNT + 1) * 6 + 2 * TaflConfig.BOARD_SIZE];
        TaflTile[][][] allBoards = new TaflTile[6][TaflConfig.BOARD_SIZE][TaflConfig.BOARD_SIZE];
        allBoards[0] = board;
        System.arraycopy(lastBoards, 0, allBoards, 1, 5);

        for (int i = 0; i < allBoards.length; i++)
        {
            for (int y = 0; y < TaflConfig.BOARD_SIZE; y++)
            {
                for (int x = 0; x < TaflConfig.BOARD_SIZE; x++)
                {
                    double v = board[y][x].getToken();
                    //index = (y * TaflConfig.BOARD_SIZE + x) + i * (TaflConfig.TILE_COUNT + 1);
                    inputVector[index] = v;
                    index++;
                }
            }
            //index = TaflConfig.TILE_COUNT + i * (TaflConfig.TILE_COUNT + 1);
            inputVector[index] = player;
            index++;
        }

        // Add kings column and row
        //index = (TaflConfig.TILE_COUNT + 1) * 6;
        for (int x = 0; x < TaflConfig.BOARD_SIZE; x++)
        {
            inputVector[index] = board[x][king.getCoords().y].getToken();
            index++;
        }
        for (int y = 0; y < TaflConfig.BOARD_SIZE; y++)
        {
            inputVector[index] = board[king.getCoords().x][y].getToken();
            index++;
        }

        return inputVector;
    }
}
