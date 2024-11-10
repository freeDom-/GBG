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
                return createFeatureVector0(stateObs.getPlayer(), stateObs.getBoard());
            default:
                System.out.println("Unknown feature mode, defaulting to feature mode 0");
                return createFeatureVector0(stateObs.getPlayer(), stateObs.getBoard());
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
        return new int[] {0};
    }

    @Override
    public int getInputSize(int featMode)
    {
        switch (featMode)
        {
            case 0:
                return TaflConfig.TILE_COUNT + 1;
            default:
                throw new RuntimeException("Unknown featmode: " + featMode);
        }
    }

    /**
     * Converts the raw board data to a vector and adds one feature indicating which player's turn it is.
     *
     * @param player The player who has the next move
     * @param board  Current board array
     * @return A vector containing all the features described above
     */
    public double[] createFeatureVector0(int player, TaflTile[][] board)
    {
        double[] inputVector = new double[TaflConfig.TILE_COUNT + 1];

        for (int y = 0; y < TaflConfig.BOARD_SIZE; y++)
        {
            for (int x = 0; x < TaflConfig.BOARD_SIZE; x++)
            {
                double v = board[y][x].getToken();
                inputVector[y * TaflConfig.BOARD_SIZE + x] = v;
            }
        }

        inputVector[TaflConfig.TILE_COUNT] = player;

        return inputVector;
    }
}
