package games.Tafl;

import games.Hex.HexConfig;
import games.ObserverBase;
import games.StateObservation;
import tools.Types;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StateObserverTafl
    extends ObserverBase
    implements StateObservation
{

    @Serial
    private static final long serialVersionUID = 1L;

    private int currentPlayer;
    private final TaflTile[][] board;
    private TaflTile lastMovedToken;

    private ArrayList<Types.ACTIONS> availableActions;

    public StateObserverTafl()
    {
        board = defaultGameBoard();
        currentPlayer = TaflUtils.PLAYER_BLACK;
        setAvailableActions();
    }

    public StateObserverTafl(StateObserverTafl other)
    {
        super(other);
        board = Arrays.copyOf(other.board, other.board.length);
        currentPlayer = other.currentPlayer;
        lastMovedToken = other.lastMovedToken;
        if (other.availableActions != null)
        {
            availableActions = (ArrayList<Types.ACTIONS>) List.copyOf(other.availableActions);
        }
    }

    public TaflTile[][] getBoard()
    {
        return board;
    }

    /**
     * Set all tile values to the default (Double.NaN)
     */
    protected void clearTileValues()
    {
        for (int i = 0; i < HexConfig.BOARD_SIZE; i++)
        {
            for (int j = 0; j < HexConfig.BOARD_SIZE; j++)
            {
                board[i][j].setValue(Double.NaN);
            }
        }
    }

    /**
     * @return The last updated tile
     */
    TaflTile getlastMovedToken()
    {
        return lastMovedToken;
    }

    /**
     * @return An empty board array
     */
    private TaflTile[][] defaultGameBoard()
    {
        TaflTile[][] newBoard = new TaflTile[TaflConfig.BOARD_SIZE][TaflConfig.BOARD_SIZE];

        int[][] board = switch (TaflConfig.BOARD_SIZE)
        {
            case 7 -> TaflUtils.startBoard7;
            case 9 -> TaflUtils.startBoard9;
            case 11 -> TaflUtils.startBoard11;
            default -> throw new RuntimeException("Board size " + TaflConfig.BOARD_SIZE + " is not supported.");
        };

        for (int i = 0; i < TaflConfig.BOARD_SIZE; i++)
        {
            for (int j = 0; j < TaflConfig.BOARD_SIZE; j++)
            {
                newBoard[i][j] = new TaflTile(i, j, board[i][j]);
                newBoard[i][j].setRect(TaflUtils.createRect(i, j, TaflConfig.UI_TILE_SIZE));
            }
        }

        return newBoard;
    }

    @Override
    public StateObserverTafl copy()
    {
        return new StateObserverTafl(this);
    }

    @Override
    public ArrayList<Types.ACTIONS> getAllAvailableActions()
    {
        ArrayList<Types.ACTIONS> allActions = new ArrayList<>();
        for (int i = 0; i < TaflConfig.BOARD_SIZE; i++)
        {
            for (int j = 0; j < TaflConfig.BOARD_SIZE; j++)
            {
                int tokenAction = i * TaflConfig.BOARD_SIZE + j;
                for (int k = 0; k < TaflConfig.ACTIONS_PER_TOKEN; k++)
                {
                    int actionInt = tokenAction + k;
                    allActions.add(Types.ACTIONS.fromInt(actionInt));
                }
            }
        }
        return allActions;
    }

    @Override
    public ArrayList<Types.ACTIONS> getAvailableActions()
    {
        return availableActions;
    }

    @Override
    public int getNumAvailableActions()
    {
        return availableActions.size();
    }

    @Override
    public void setAvailableActions()
    {
        availableActions = new ArrayList<>();
        for (int i = 0; i < TaflConfig.BOARD_SIZE; i++)
        {
            for (int j = 0; j < TaflConfig.BOARD_SIZE; j++)
            {
                if (board[i][j].getPlayer() != TaflUtils.PLAYER_NONE)
                {
                    // TODO: Generate possible moves for token

                }
            }
        }
    }

    @Override
    public Types.ACTIONS getAction(int i)
    {
        return availableActions.get(i);
    }

    @Override
    public void advance(Types.ACTIONS action, Random cmpRand)
    {

    }

    @Override
    public int getPlayer()
    {
        return currentPlayer;
    }

    @Override
    public int getNumPlayers()
    {
        return 2;
    }

    /**
     * @return the game score, i.e. the sum of rewards for the current state.
     * For Tafl only game-over states have a non-zero game score.
     * It is the reward from the perspective of {@code player}.
     */
    @Override
    public double getGameScore(int player)
    {
        int sign = (player == currentPlayer) ? 1 : (-1);
        int winner = determineWinner();
        if (winner == TaflUtils.PLAYER_NONE)
        {
            return 0;
        }

        return sign * (winner == currentPlayer ? TaflConfig.REWARD_POSITIVE : TaflConfig.REWARD_NEGATIVE);
    }

    @Override
    public double getMinGameScore()
    {
        return TaflConfig.REWARD_NEGATIVE;
    }

    @Override
    public double getMaxGameScore()
    {
        return TaflConfig.REWARD_POSITIVE;
    }

    @Override
    public boolean isGameOver()
    {
        return determineWinner() != TaflUtils.PLAYER_NONE;
    }

    /**
     * Uses information from the last moved token to determine if the king is captured or escaped.
     *
     * @return ID of the player who won the game. ID of HexConfig.PLAYER_NONE if game is not over.
     */
    private int determineWinner()
    {
        Types.WINNER winner = TaflUtils.getWinner(board, lastMovedToken);
        if (winner == Types.WINNER.PLAYER_WINS || getNumAvailableActions() == 0)
        {
            //Reverse winners, since current player changes after the winning tile was placed
            return (currentPlayer == TaflUtils.PLAYER_BLACK ? TaflUtils.PLAYER_WHITE : TaflUtils.PLAYER_BLACK);
        }
        return TaflUtils.PLAYER_NONE;
    }

    @Override
    public boolean isDeterministicGame()
    {
        return true;
    }

    @Override
    public boolean isFinalRewardGame()
    {
        return true;
    }

    @Override
    public boolean isLegalState()
    {
        return true;
    }

    @Override
    public String stringDescr()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TaflConfig.BOARD_SIZE; i++)
        {
            for (int j = 0; j < TaflConfig.BOARD_SIZE; j++)
            {
                switch (board[i][j].getToken())
                {
                    case TaflUtils.BLACK_TOKEN:
                        sb.append('B');
                        break;
                    case TaflUtils.WHITE_TOKEN:
                        sb.append('W');
                        break;
                    case TaflUtils.KING:
                        sb.append('K');
                        break;
                    default:
                        sb.append('-');
                }
            }
            sb.append("/");
        }
        return sb.toString();
    }
}
