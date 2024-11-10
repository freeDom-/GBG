package games.Tafl;

import games.ObserverBase;
import games.StateObservation;
import tools.Types;

import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class StateObserverTafl
    extends ObserverBase
    implements StateObservation
{

    @Serial
    private static final long serialVersionUID = 1L;

    private int currentPlayer;
    private final TaflTile[][] board;
    private final TaflTile[][][] lastBoards;
    private TaflTile lastMovedToken;

    private ArrayList<Types.ACTIONS> availableActions;

    public StateObserverTafl()
    {
        board = defaultGameBoard();
        lastBoards = new TaflTile[5][TaflConfig.BOARD_SIZE][TaflConfig.BOARD_SIZE];

        currentPlayer = TaflConfig.START_PLAYER;
        setAvailableActions();
    }

    public StateObserverTafl(StateObserverTafl other)
    {
        super(other);
        board = copyTable(other.board);
        currentPlayer = other.currentPlayer;
        lastMovedToken = other.lastMovedToken;
        lastBoards = other.lastBoards.clone();
        if (other.availableActions != null)
        {
            availableActions = (ArrayList<Types.ACTIONS>) other.availableActions.stream().map(Types.ACTIONS::new).collect(Collectors.toList());
            //setAvailableActions();
        }
    }

    @Override
    public StateObserverTafl copy()
    {
        return new StateObserverTafl(this);
    }

    /**
     * Create copy of the game board array that is passed as the parameter.
     *
     * @param table The game board array that is to be copied
     * @return a copy of the passed in board
     */
    private TaflTile[][] copyTable(TaflTile[][] table)
    {
        TaflTile board[][] = new TaflTile[TaflConfig.BOARD_SIZE][TaflConfig.BOARD_SIZE];
        for (int y = 0; y < TaflConfig.BOARD_SIZE; y++)
        {
            for (int x = 0; x < TaflConfig.BOARD_SIZE; x++)
            {
                board[x][y] = table[x][y].copy();
            }
        }

        return board;
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
        for (int y = 0; y < TaflConfig.BOARD_SIZE; y++)
        {
            for (int x = 0; x < TaflConfig.BOARD_SIZE; x++)
            {
                board[x][y].setValue(Double.NaN);
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

        for (int y = 0; y < TaflConfig.BOARD_SIZE; y++)
        {
            for (int x = 0; x < TaflConfig.BOARD_SIZE; x++)
            {
                newBoard[x][y] = new TaflTile(x, y, board[x][y]);
                newBoard[x][y].setRect(TaflUtils.createRect(x, y, TaflConfig.UI_TILE_SIZE));
            }
        }

        return newBoard;
    }

    @Override
    public ArrayList<Types.ACTIONS> getAllAvailableActions()
    {
        ArrayList<Types.ACTIONS> allActions = new ArrayList<>();
        for (int y = 0; y < TaflConfig.TILE_COUNT; y++)
        {
            for (int x = 0; x < TaflConfig.ACTIONS_PER_TOKEN; x++)
            {
                int actionInt = y * TaflConfig.ACTIONS_PER_TOKEN + x;
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
        for (int y = 0; y < TaflConfig.BOARD_SIZE; y++)
        {
            for (int x = 0; x < TaflConfig.BOARD_SIZE; x++)
            {
                if (board[x][y].getPlayer() == currentPlayer)
                {
                    ArrayList<Point> targets = TaflUtils.generateMovesForToken(board, board[x][y]);
                    for (Point target : targets)
                    {
                        int actionNum = TaflUtils.getActionNumberFromMove(board[x][y].getCoords(), target);
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

        lastBoards[4] = lastBoards[3].clone();
        lastBoards[3] = lastBoards[2].clone();
        lastBoards[2] = lastBoards[1].clone();
        lastBoards[1] = lastBoards[0].clone();
        lastBoards[0] = copyTable(board);

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
        int winner = TaflUtils.getWinner(board, lastBoards[4], availableActions, lastMovedToken);
        if (winner == TaflUtils.PLAYER_NONE)
        {
            return 0;
        }
        return winner == player ? TaflConfig.REWARD_POSITIVE : TaflConfig.REWARD_NEGATIVE;
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
        return TaflUtils.getWinner(board, lastBoards[4], availableActions, lastMovedToken) != TaflUtils.PLAYER_NONE;
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
        for (int y = 0; y < TaflConfig.BOARD_SIZE; y++)
        {
            for (int x = 0; x < TaflConfig.BOARD_SIZE; x++)
            {
                switch (board[x][y].getToken())
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
