package games.Tafl;

import controllers.PlayAgent;
import controllers.TD.TDAgent;
import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import controllers.TD.ntuple4.QLearn4Agt;
import controllers.TD.ntuple4.Sarsa4Agt;
import controllers.TD.ntuple4.TDNTuple4Agt;
import params.ParNT;
import params.ParOther;
import params.ParRB;
import params.ParTD;
import tools.Types;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class TaflUtils
{

    enum Direction
    {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

    public static final int PLAYER_NONE = -1; // Beginning player
    public static final int PLAYER_BLACK = 0; // Beginning player
    public static final int PLAYER_WHITE = 1;

    public static final int NUM_POSITION_VALUES = 4;
    public static final int EMPTY = 0;
    public static final int BLACK_TOKEN = 1;
    public static final int WHITE_TOKEN = 2;
    public static final int KING = 3;

    static final int[][] startBoard5 = new int[][] {
        {1, 0, 1, 0, 1},
        {0, 0, 2, 0, 0},
        {1, 2, 3, 2, 1},
        {0, 0, 2, 0, 0},
        {1, 0, 1, 0, 1}
    };

    static final int[][] startBoard7 = new int[][] {
        {0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, 2, 0, 0, 0},
        {1, 1, 2, 3, 2, 1, 1},
        {0, 0, 0, 2, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0}
    };

    static final int[][] startBoard9 = new int[][] {
        {0, 0, 0, 1, 1, 1, 0, 0, 0},
        {0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 2, 0, 0, 0, 0},
        {1, 0, 0, 0, 2, 0, 0, 0, 1},
        {1, 1, 2, 2, 3, 2, 2, 1, 1},
        {1, 0, 0, 0, 2, 0, 0, 0, 1},
        {0, 0, 0, 0, 2, 0, 0, 0, 0},
        {0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 1, 1, 1, 0, 0, 0}
    };

    static final int[][] startBoard11 = new int[][] {
        {0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 2, 2, 2, 0, 0, 0, 1},
        {1, 1, 0, 2, 2, 3, 2, 2, 0, 1, 1},
        {1, 0, 0, 0, 2, 2, 2, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 1},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0}
    };

    /**
     * Checks if a tile is on one of the four corners of the game board
     *
     * @param tile The tile on the game board
     * @return true if a tile is on the corner, false otherwise
     */
    static boolean isTileCorner(TaflTile tile)
    {
        Point coords = tile.getCoords();
        return coords.x == 0 && coords.y == 0 || coords.x == 0 && coords.y == TaflConfig.BOARD_SIZE - 1 ||
               coords.x == TaflConfig.BOARD_SIZE - 1 && coords.y == 0 || coords.x == TaflConfig.BOARD_SIZE - 1 && coords.y == TaflConfig.BOARD_SIZE - 1;
    }

    /**
     * Checks if a tile is on the edge of the game board
     *
     * @param tile The tile on the game board
     * @return true if the tile is on the edge, false otherwise
     */
    static boolean isTileEdge(TaflTile tile)
    {
        Point coords = tile.getCoords();
        return coords.x == 0 || coords.x == TaflConfig.BOARD_SIZE - 1 ||
               coords.y == 0 || coords.y == TaflConfig.BOARD_SIZE - 1;
    }

    /**
     * Checks if a tile is the throne
     *
     * @param tile The tile on the game board
     * @return true if the tile is the throne, false otherwise
     */
    static boolean isTileThrone(TaflTile tile)
    {
        Point coords = tile.getCoords();
        return coords.x == TaflConfig.BOARD_SIZE / 2 && coords.y == TaflConfig.BOARD_SIZE / 2;
    }

    /**
     * Checks if a tile is next to the throne
     *
     * @param tile The tile on the game board
     * @return true if the tile is next to the throne, false otherwise
     */
    static boolean isTileNextToThrone(TaflTile tile)
    {
        Point coords = tile.getCoords();
        return (coords.x == TaflConfig.BOARD_SIZE / 2 - 1 && coords.y == TaflConfig.BOARD_SIZE / 2) ||
               (coords.x == TaflConfig.BOARD_SIZE / 2 + 1 && coords.y == TaflConfig.BOARD_SIZE / 2) ||
               (coords.x == TaflConfig.BOARD_SIZE / 2 && coords.y == TaflConfig.BOARD_SIZE / 2 - 1) ||
               (coords.x == TaflConfig.BOARD_SIZE / 2 && coords.y == TaflConfig.BOARD_SIZE / 2 + 1);
    }

    /**
     * Checks if the tile exists by checking if it is inside the bounds of the game board
     *
     * @param x index 1
     * @param y index 2
     * @return true if the tile exists, false otherwise
     */
    public static boolean isValidTile(int x, int y)
    {
        return (x >= 0 && x < TaflConfig.BOARD_SIZE) && (y >= 0 && y < TaflConfig.BOARD_SIZE);
    }

    /**
     * Checks if a tile is hostile for a player
     *
     * @param tile   The tile on the game board
     * @param player The player
     * @return true if the tile is hostile, false otherwise
     */
    static boolean isTileHostile(TaflTile tile, int player)
    {
        boolean checkEnemy = player == PLAYER_BLACK && tile.getPlayer() == PLAYER_WHITE || player == PLAYER_WHITE && tile.getPlayer() == PLAYER_BLACK;
        boolean checkCorner = !TaflConfig.RULE_NO_SPECIAL_TILES && isTileCorner(tile);
        boolean checkHostileThrone = !TaflConfig.RULE_NO_SPECIAL_TILES && isTileThrone(tile) && (player == PLAYER_BLACK || tile.getToken() == EMPTY);

        return checkEnemy || checkCorner || checkHostileThrone;
    }

    static int getOtherPlayer(int player)
    {
        return switch (player)
        {
            case PLAYER_NONE -> PLAYER_NONE;
            case PLAYER_BLACK -> PLAYER_WHITE;
            case PLAYER_WHITE -> PLAYER_BLACK;
            default -> throw new RuntimeException("Player number " + player + " outside allowed range 0..2");
        };
    }

    /**
     * Checks if the game has a winner
     *
     * @param board            The game board
     * @param repeatedBoard    The board which needs to be checked for a repetition
     * @param availableActions A list with all available actions
     * @param lastMovedToken   The last moved token
     * @param king             The king token
     * @return the number representation of the player who won the game, -1 if there is no winner
     */
    static int getWinner(TaflTile[][] board, TaflTile[][] repeatedBoard, ArrayList<Types.ACTIONS> availableActions, TaflTile lastMovedToken, TaflTile king)
    {
        if (lastMovedToken == null)
        {
            //System.out.println("lastMovedToken was null");
            return PLAYER_NONE;
        }
        // King escaped
        if (isKingEscaped(king))
        {
            return PLAYER_WHITE;
        }
        // King captured
        if (isKingCaptured(board, lastMovedToken, king))
        {
            return PLAYER_BLACK;
        }
        // No more valid moves left
        if (availableActions.isEmpty())
        {
            return getOtherPlayer(lastMovedToken.getPlayer());
        }
        // Repeated board position
        if (isBoardPositionRepeated(board, repeatedBoard))
        {
            return getOtherPlayer(lastMovedToken.getPlayer());
        }
        // TODO: Add other win conditions
        return PLAYER_NONE;
    }

    /**
     * Checks if a board position is repeated
     *
     * @param board         The current game board
     * @param repeatedBoard The previous game board to perform the repeated check against
     * @return true if the position was repeated, false otherwise
     */
    static boolean isBoardPositionRepeated(TaflTile[][] board, TaflTile[][] repeatedBoard)
    {
        if (repeatedBoard[0][0] == null)
        {
            return false;
        }
        for (int y = 0; y < TaflConfig.BOARD_SIZE; y++)
        {
            for (int x = 0; x < TaflConfig.BOARD_SIZE; x++)
            {
                if (board[x][y].getToken() != repeatedBoard[x][y].getToken())
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if the king is escaped
     *
     * @param king The king token
     * @return true if the king is escaped, false otherwise
     */
    static boolean isKingEscaped(TaflTile king)
    {
        return isTileCorner(king) || TaflConfig.RULE_EASY_KING_ESCAPE && isTileEdge(king);
    }

    /**
     * Checks if the king is captured
     *
     * @param board          The game board
     * @param lastMovedToken The last moved token
     * @param king           The king token
     * @return true if the king is captured, false otherwise
     */
    static boolean isKingCaptured(TaflTile[][] board, TaflTile lastMovedToken, TaflTile king)
    {
        if (lastMovedToken.getPlayer() == PLAYER_WHITE)
        {
            return false;
        }

        ArrayList<TaflTile> neighbors = getNeighbors(board, lastMovedToken);
        if (neighbors.contains(king))
        {
            // King captured by 4 tokens
            if (!TaflConfig.RULE_NO_SPECIAL_TILES && isTileThrone(king) || TaflConfig.RULE_HARD_KING_CAPTURE)
            {
                ArrayList<TaflTile> kingsNeighbors = getNeighbors(board, king);
                for (TaflTile kingsNeighbor : kingsNeighbors)
                {
                    if (!isTileHostile(kingsNeighbor, PLAYER_WHITE))
                    {
                        return false;
                    }
                }
                return true;
            }
            // King captured by 2 tokens
            else
            {
                // Get difference between lastMovedToken and king and multiply this value to get the token behind the king
                int behindKingX = king.getCoords().x + (king.getCoords().x - lastMovedToken.getCoords().x);
                int behindKingY = king.getCoords().y + (king.getCoords().y - lastMovedToken.getCoords().y);

                if (isValidTile(behindKingX, behindKingY))
                {
                    TaflTile behindKing = board[behindKingX][behindKingY];
                    // The throne is never hostile for the king
                    return (TaflConfig.RULE_NO_SPECIAL_TILES || !isTileThrone(behindKing)) && isTileHostile(behindKing, PLAYER_WHITE);
                }
                else
                {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Get all captures for a move
     *
     * @param board          The game board
     * @param lastMovedToken The last moved token
     * @return a list with the captured tokens
     */
    static ArrayList<TaflTile> getCaptures(TaflTile[][] board, TaflTile lastMovedToken)
    {
        ArrayList<TaflTile> captures = new ArrayList<>();
        ArrayList<TaflTile> neighbors = getNeighbors(board, lastMovedToken);
        int enemy = lastMovedToken.getPlayer() == PLAYER_BLACK ? PLAYER_WHITE : PLAYER_BLACK;
        int enemyToken = lastMovedToken.getPlayer() == PLAYER_BLACK ? WHITE_TOKEN : BLACK_TOKEN;
        for (TaflTile neighbor : neighbors)
        {
            if (neighbor.getToken() == enemyToken)
            {
                // Check if token is captured
                // Get difference between lastMovedToken and enemy token and multiply this value to get the token behind the enemy
                int behindEnemyX = neighbor.getCoords().x + (neighbor.getCoords().x - lastMovedToken.getCoords().x);
                int behindEnemyY = neighbor.getCoords().y + (neighbor.getCoords().y - lastMovedToken.getCoords().y);

                if (isValidTile(behindEnemyX, behindEnemyY))
                {
                    TaflTile behindEnemy = board[behindEnemyX][behindEnemyY];
                    if (isTileHostile(behindEnemy, enemy))
                    {
                        captures.add(neighbor);
                    }
                }
            }
        }
        return captures;
    }

    /**
     * Get all neighbors for a tile
     *
     * @param board The game board
     * @param tile  The tile on the game board
     * @return a list with the tiles neighbors
     */
    static ArrayList<TaflTile> getNeighbors(TaflTile[][] board, TaflTile tile)
    {
        ArrayList<TaflTile> neighbors = new ArrayList<>();
        Point coords = tile.getCoords();
        int x = coords.x;
        int y = coords.y;
        if (isValidTile(x - 1, y))
        {
            neighbors.add(board[x - 1][y]);
        }
        if (isValidTile(x + 1, y))
        {
            neighbors.add(board[x + 1][y]);
        }
        if (isValidTile(x, y - 1))
        {
            neighbors.add(board[x][y - 1]);
        }
        if (isValidTile(x, y + 1))
        {
            neighbors.add(board[x][y + 1]);
        }
        return neighbors;
    }

    /**
     * Converts a move from start position to end position to an action number
     * (0-14 representing the respective position in the row or column)
     *
     * @param start Start position of the move
     * @param end   Target position of the move
     * @return number representation for an action
     */
    static int getActionNumberFromMove(Point start, Point end)
    {
        int offset = pointToCell(start) * TaflConfig.ACTIONS_PER_TOKEN;
        if (start.x == end.x)
        {
            return offset + end.y;
        }
        else if (start.y == end.y)
        {
            return offset + end.x + TaflConfig.BOARD_SIZE;
        }
        else
        {
            throw new RuntimeException(String.format("Illegal move (%d,%d) -> (%d,%d)", start.x, start.y, end.x, end.y));
        }
    }

    /**
     * Get the start and end points of an action
     *
     * @param actionNumber Number representation for an action
     * @return an array containing the start and the end point
     */
    static Point[] getMoveFromActionNumber(int actionNumber)
    {
        Point start, end;

        int startNumber = Math.floorDiv(actionNumber, TaflConfig.ACTIONS_PER_TOKEN);
        start = cellToPoint(startNumber);
        int action = actionNumber % TaflConfig.ACTIONS_PER_TOKEN;
        if (action < TaflConfig.BOARD_SIZE)
        {
            end = new Point(start.x, action);
        }
        else
        {
            end = new Point(action - TaflConfig.BOARD_SIZE, start.y);
        }

        return new Point[] {start, end};
    }

    /**
     * Get all available move targets for a token
     *
     * @param board The game board
     * @param token The token on the game board
     * @return a list containing all available move targets
     */
    static ArrayList<Point> generateMovesForToken(TaflTile[][] board, TaflTile token)
    {
        ArrayList<Point> moveTargets = new ArrayList<>();

        moveTargets.addAll(generateMovesForDirection(board, token, Direction.LEFT));
        moveTargets.addAll(generateMovesForDirection(board, token, Direction.RIGHT));
        moveTargets.addAll(generateMovesForDirection(board, token, Direction.TOP));
        moveTargets.addAll(generateMovesForDirection(board, token, Direction.BOTTOM));

        return moveTargets;
    }

    /**
     * Generate all available move targets for a token for a direction
     *
     * @param board     The game board
     * @param token     The token on the game board
     * @param direction The direction to check
     * @return a list containing all available move targets for the direction
     */
    private static ArrayList<Point> generateMovesForDirection(TaflTile[][] board, TaflTile token, Direction direction)
    {
        ArrayList<Point> moveTargets = new ArrayList<>();
        int xStep = 0;
        int yStep = 0;
        switch (direction)
        {
            case LEFT:
                xStep = -1;
                break;
            case RIGHT:
                xStep = 1;
                break;
            case TOP:
                yStep = -1;
                break;
            case BOTTOM:
                yStep = 1;
                break;
        }

        int x = token.getCoords().x + xStep;
        int y = token.getCoords().y + yStep;
        while (TaflUtils.isValidTile(x, y) && board[x][y].getToken() == EMPTY)
        {
            TaflTile tile = board[x][y];
            if (TaflConfig.RULE_NO_SPECIAL_TILES || token.getToken() == KING || (!isTileCorner(tile) && !isTileThrone(tile)))
            {
                moveTargets.add(new Point(x, y));
            }
            x = x + xStep;
            y = y + yStep;
        }
        return moveTargets;
    }

    /**
     * Converts the two-dimensional board array to a one-dimensional vector.
     * Leftmost tile on board (coordinates: 0, 0) is the first element.
     * Bottom tile on board (coordinates: 0, n) is the n-th element.
     * Top tile on board (coordinates: n, 0) is the (n^2 - n + 1)-th element.
     * Rightmost tile on board (coordinates: n, n) is the (n^2)-th element.
     *
     * @param board The game board
     * @return the vector representation of the game board
     */
    static TaflTile[] boardToVector(TaflTile[][] board)
    {
        TaflTile[] boardVector = new TaflTile[board.length * board.length];
        for (int y = 0, i = 0; y < TaflConfig.BOARD_SIZE; y++)
        {
            for (int x = 0; x < TaflConfig.BOARD_SIZE; x++, i++)
            {
                boardVector[i] = board[x][y];
            }
        }
        return boardVector;
    }

    /**
     * Converts a point to a cell value of the board
     *
     * @param p The point containing coordinates of the game board
     * @return the number representation of the point
     */
    static int pointToCell(Point p)
    {
        return p.x * TaflConfig.BOARD_SIZE + p.y;
    }

    /**
     * Converts a cell value of the board to a point
     *
     * @param position The number representation of a point on the game board
     * @return the point representation of the cell
     */
    static Point cellToPoint(int position)
    {
        int x = Math.floorDiv(position, TaflConfig.BOARD_SIZE);
        int y = position % TaflConfig.BOARD_SIZE;
        return new Point(x, y);
    }

    /**
     * Converts a mouse position to a point of the game grid.
     * This function can be used to get the token at the mouse position.
     *
     * @param mx        Mouse position x
     * @param my        Mouse position y
     * @param boardSize The size of the game board defined by TaflConfig.BOARD_SIZE
     * @return the point representation of the mouse position
     */
    static Point mousePositionToPoint(int mx, int my, int boardSize)
    {
        Point p = new Point(-1, -1);
        if (mx >= 0 && my >= 0)
        {
            for (int x = 0; x < boardSize; x++)
            {
                if ((x + 1) * TaflConfig.UI_TILE_SIZE >= mx)
                {
                    p.x = x;
                    break;
                }
            }
            for (int y = 0; y < boardSize; y++)
            {
                if ((y + 1) * TaflConfig.UI_TILE_SIZE >= my)
                {
                    p.y = y;
                    break;
                }
            }
        }
        return p;
    }

    /**
     * Creates a polygon object for the specified tile
     *
     * @param x        first index
     * @param y        second index
     * @param tileSize size in px of each tile side to side
     * @return Rectangle for specified tile
     */
    public static Rectangle createRect(int x, int y, int tileSize)
    {
        int tileX = x * tileSize;
        int tileY = y * tileSize;

        return new Rectangle(tileX, tileY, tileSize, tileSize);
    }

    /**
     * Get string representations for an agents parameters
     *
     * @param playAgent    The play agent
     * @param shortVersion Returns a shorter version of the string
     * @return <pre>A string array containing string representation for the agents parameters in the following order:<br>
     * 0: tdParams<br>
     * 1: ntParams<br>
     * 2: rbParams<br>
     * 3: otherParams</pre>
     */
    public static String[] getParamStrings(PlayAgent playAgent, boolean shortVersion)
    {
        String[] params = new String[4];
        String paramString = "";
        Serializable[] agentParams = getAgentParams(playAgent);
        ParTD tdParams = (ParTD) agentParams[0];
        ParNT ntParams = (ParNT) agentParams[1];
        ParRB rbParams = (ParRB) agentParams[2];
        ParOther otherParams = (ParOther) agentParams[3];

        // TODO: remove general info from td args array entry
        paramString += shortVersion ? getNumberWithUnit(playAgent.getMaxGameNum()) + " " : "maxgames=" + playAgent.getMaxGameNum();

        // TD Params
        if (tdParams != null)
        {
            paramString += shortVersion ? "(" : "";
            paramString += (shortVersion ? "fm=" : ",featmode=") + tdParams.getFeatmode();
            paramString += (shortVersion ? ",a=" : ",alpha=") + tdParams.getAlpha();
            if (tdParams.getAlpha() != tdParams.getAlphaFinal())
            {
                paramString += ".." + tdParams.getAlphaFinal();
            }
            paramString += (shortVersion ? ",e=" : ",epsilon=") + tdParams.getEpsilon();
            if (tdParams.getEpsilon() != tdParams.getEpsilonFinal())
            {
                paramString += ".." + tdParams.getEpsilonFinal();
            }
            paramString += (shortVersion ? ",l=" : ",lambda=") + tdParams.getLambda();
            paramString += (shortVersion ? ",g=" : ",gamma=") + tdParams.getGamma();
            paramString += (shortVersion ? ",ep=" : ",epochs=") + tdParams.getEpochs();
            if (!tdParams.hasLinearNet())
            {
                paramString += shortVersion ? ",nn" : ",nnet";
            }
            if (tdParams.hasSigmoid())
            {
                paramString += shortVersion ? ",sig" : ",sigmoid";
            }
            if (tdParams.getNormalize())
            {
                paramString += shortVersion ? ",norm" : ",normalize";
            }
            if (tdParams.hasStopOnRoundOver())
            {
                paramString += shortVersion ? ",stop" : ",stoproundover";
            }
            paramString += shortVersion ? ")" : "";
        }
        params[0] = paramString;

        // NT Params
        paramString = "";
        if (ntParams != null) {
            paramString += shortVersion ? "(" : "";
            if (ntParams.getUSESYMMETRY()) {
                paramString += shortVersion ? "sym" : "usesymmetry";
            }
            if (ntParams.getRandomness()) {

            }
            if (ntParams.getTc()) {
                paramString += shortVersion ? ",tc" : ",temporalcoherence";
                // TODO: additional tc params
            }
            paramString += shortVersion ? ")" : "";
        }
        params[1] = paramString;

        // RB Params
        // TODO: add rb params
        paramString = "";
        params[2] = paramString;

        // Other params
        paramString = "";
        if (otherParams != null) {
            paramString += shortVersion ? "(" : "";
            paramString += (shortVersion ? "em=" : "evalmode=") + otherParams.getQuickEvalMode();
            paramString += (shortVersion ? ",tm=" : "trainevalmode=") + otherParams.getTrainEvalMode();
            paramString += (shortVersion ? ",ne=" : "numeval=") + otherParams.getNumEval();
            paramString += (shortVersion ? ",el=" : "episodelength=") + otherParams.getEpisodeLength();
            if (otherParams.getLearnFromRM()) {
                paramString += shortVersion ? "lrand" : "learnfromrandmoves";
            }
            if (!otherParams.getRewardIsGameScore()) {
                paramString += shortVersion ? "crew" : "usecustomreward";
            }
            paramString += shortVersion ? ")" : "";
        }
        params[3] = paramString;

        return params;
    }

    /**
     * Gets the parameters of an agent
     *
     * @param playAgent The play agent
     * @return <pre>An array containing the parameters  in the following order:<br>
     * 0: tdParams<br>
     * 1: ntParams<br>
     * 2: rbParams<br>
     * 3: otherParams</pre>
     */
    public static Serializable[] getAgentParams(PlayAgent playAgent)
    {
        ParTD tdParams = null;
        ParNT ntParams = null;
        ParRB rbParams = null;
        ParOther otherParams = null;

        switch (playAgent.getName())
        {
            case "TDS":
                TDAgent tdAgent = (TDAgent) playAgent;
                tdParams = tdAgent.getParTD();
                otherParams = tdAgent.getParOther();
                rbParams = tdAgent.getParReplay();
                break;
            case "TD-Ntuple-3":
                TDNTuple3Agt nt3Agent = (TDNTuple3Agt) playAgent;
                tdParams = nt3Agent.getParTD();
                ntParams = nt3Agent.getParNT();
                rbParams = nt3Agent.getParReplay();
                otherParams = nt3Agent.getParOther();
                break;
            case "TD-Ntuple-4":
                TDNTuple4Agt nt4Agent = (TDNTuple4Agt) playAgent;
                tdParams = nt4Agent.getParTD();
                ntParams = nt4Agent.getParNT();
                rbParams = nt4Agent.getParReplay();
                otherParams = nt4Agent.getParOther();
                break;
            case "Sarsa":
                SarsaAgt sarsaAgt = (SarsaAgt) playAgent;
                tdParams = sarsaAgt.getParTD();
                ntParams = sarsaAgt.getParNT();
                rbParams = sarsaAgt.getParReplay();
                otherParams = sarsaAgt.getParOther();
                break;
            case "Sarsa-4":
                Sarsa4Agt sarsa4Agt = (Sarsa4Agt) playAgent;
                tdParams = sarsa4Agt.getParTD();
                ntParams = sarsa4Agt.getParNT();
                rbParams = sarsa4Agt.getParReplay();
                otherParams = sarsa4Agt.getParOther();
                break;
            case "Qlearn-4":
                QLearn4Agt qLearn4Agt = (QLearn4Agt) playAgent;
                tdParams = qLearn4Agt.getParTD();
                ntParams = qLearn4Agt.getParNT();
                rbParams = qLearn4Agt.getParReplay();
                otherParams = qLearn4Agt.getParOther();
                break;
        }

        return new Serializable[] {tdParams, ntParams, rbParams, otherParams};
    }

    /**
     * Convert a number to a string containing the number and the unit (e.g. 10000 -> 10k)
     *
     * @param number The number
     * @return A string representation of the number
     */
    public static String getNumberWithUnit(int number)
    {
        String result = String.valueOf(number);

        if (number / 1000000 >= 1)
        {
            result = number / 1000000 + "m";
        }
        else if (number / 1000 >= 1)
        {
            result = number / 1000 + "k";
        }

        return result;
    }
}
