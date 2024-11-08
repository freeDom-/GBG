package games.Tafl;

import games.ObserverBase;
import games.StateObservation;
import tools.Types;

import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;
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

        currentPlayer = TaflConfig.START_PLAYER;
        setAvailableActions();
    }

    public StateObserverTafl(StateObserverTafl other)
    {
        super(other);
        board = new TaflTile[TaflConfig.BOARD_SIZE][TaflConfig.BOARD_SIZE];
        copyTable(other.board);
        currentPlayer = other.currentPlayer;
        lastMovedToken = other.lastMovedToken;
        if (other.availableActions != null)
        {
            //availableActions = (ArrayList<Types.ACTIONS>) other.availableActions.stream().map(Types.ACTIONS::new).collect(Collectors.toList());
            setAvailableActions();
        }
    }

    @Override
    public StateObserverTafl copy()
    {
        return new StateObserverTafl(this);
    }

    /**
     * Replaces the current game board array by a copy of the array that is passed as the parameter.
     *
     * @param table The game board array that is to be copied
     */
    private void copyTable(TaflTile[][] table)
    {
        for (int i = 0; i < TaflConfig.BOARD_SIZE; i++)
        {
            for (int j = 0; j < TaflConfig.BOARD_SIZE; j++)
            {
                board[i][j] = table[i][j].copy();
            }
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
        for (int i = 0; i < TaflConfig.BOARD_SIZE; i++)
        {
            for (int j = 0; j < TaflConfig.BOARD_SIZE; j++)
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
            case 5 -> TaflUtils.startBoard5;
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
    public ArrayList<Types.ACTIONS> getAllAvailableActions()
    {
        ArrayList<Types.ACTIONS> allActions = new ArrayList<>();
        for (int i = 0; i < TaflConfig.TILE_COUNT; i++)
        {
            for (int j = 0; j < TaflConfig.ACTIONS_PER_TOKEN; j++)
            {
                int actionInt = i * TaflConfig.ACTIONS_PER_TOKEN + j;
                allActions.add(Types.ACTIONS.fromInt(actionInt));
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
                if (board[i][j].getPlayer() == currentPlayer)
                {
                    ArrayList<Point> targets = TaflUtils.generateMovesForToken(board, board[i][j]);
                    for (Point target : targets)
                    {
                        int actionNum = TaflUtils.getActionNumberFromMove(board[i][j].getCoords(), target);
                        availableActions.add(Types.ACTIONS.fromInt(actionNum));
                    }
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
        super.advanceBase(action);        //		includes addToLastMoves(action)
        if (action == null)
        {
            System.out.println("TAFL ERROR: null given as action in advance()");
            return;
        }
        int actionInt = action.toInt();
        assert (0 <= actionInt && actionInt < TaflConfig.TILE_COUNT * TaflConfig.ACTIONS_PER_TOKEN) : "Invalid action: " + actionInt;
        Point[] move = TaflUtils.getMoveFromActionNumber(actionInt);
        int startX = move[0].x;
        int startY = move[0].y;
        int endX = move[1].x;
        int endY = move[1].y;

        TaflTile startTile = board[startX][startY];
        TaflTile endTile = board[endX][endY];
        if (startTile.getPlayer() != currentPlayer)
        {
            System.out.println("Token on tile " + startTile.getCoords() + " does not belong to the player " + currentPlayer + ".");
            return;
        }
        if (endTile.getPlayer() != TaflUtils.PLAYER_NONE)
        {
            System.out.println("Token on tile " + startTile.getCoords() + " is not empty.");
            return;
        }
        endTile.setPlayer(currentPlayer);
        endTile.setToken(startTile.getToken());
        startTile.setPlayer(TaflUtils.PLAYER_NONE);
        startTile.setToken(TaflUtils.EMPTY);

        // Get captures
        ArrayList<TaflTile> captures = TaflUtils.getCaptures(board, endTile);
        for (TaflTile captured : captures)
        {
            captured.setPlayer(TaflUtils.PLAYER_NONE);
            captured.setToken(TaflUtils.EMPTY);
        }

        lastMovedToken = endTile;
        // set up player for next advance()
        currentPlayer = (currentPlayer == TaflUtils.PLAYER_BLACK ? TaflUtils.PLAYER_WHITE : TaflUtils.PLAYER_BLACK);
        setAvailableActions();            // IMPORTANT: adjust the available actions
        super.incrementMoveCounter();
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
        return determineWinner() != TaflUtils.PLAYER_NONE || getNumAvailableActions() == 0;
    }

    /**
     * Uses information from the last moved token to determine if the king is captured or escaped.
     *
     * @return ID of the player who won the game. ID of TaflConfig.PLAYER_NONE if game is not over.
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
