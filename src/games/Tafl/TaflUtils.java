package games.Tafl;

import tools.Types;

import java.awt.*;
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
            if (!TaflConfig.RULE_NO_SPECIAL_TILES && (isTileThrone(king) || isTileNextToThrone(king)) || TaflConfig.RULE_HARD_KING_CAPTURE)
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
                    return isTileHostile(behindKing, PLAYER_WHITE);
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
     * Draws a single tile
     *
     * @param tile            Tile to be drawn
     * @param g2              Graphics context
     * @param backgroundColor Color to draw the tile in
     * @param highlight       If a red border surrounding the tile should be drawn
     */
    public static void drawTile(TaflTile tile, Graphics2D g2, Color backgroundColor, boolean highlight)
    {
        Rectangle rect = tile.getRect();

        if (tile.getPlayer() != PLAYER_NONE)
        {
            g2.setColor(backgroundColor);
            g2.fillRect(rect.x, rect.y, rect.width, rect.height);
            Color tokenColor = tile.getPlayer() == PLAYER_BLACK ? GameBoardTaflGui.COLOR_PLAYER_BLACK : GameBoardTaflGui.COLOR_PLAYER_WHITE;
            g2.setColor(tokenColor);
            g2.fillOval(rect.x, rect.y, rect.width, rect.height);
            if (tile.getToken() == KING)
            {
                g2.setColor(Color.GRAY);
                Font font = new Font("Times New Roman", Font.BOLD, TaflConfig.UI_TILE_SIZE / 2);
                drawCenteredString(g2, "K", rect, font);
            }
        }
        else
        {
            g2.setColor(backgroundColor);
            g2.fillRect(rect.x, rect.y, rect.width, rect.height);
        }

        if (highlight)
        {
            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.RED);
        }
        else
        {
            g2.setColor(GameBoardTaflGui.COLOR_GRID);
        }

        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Draw a String centered in the middle of a Rectangle.
     *
     * @param g    The Graphics instance.
     * @param text The String to draw.
     * @param rect The Rectangle to center the text in.
     */
    public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font)
    {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

    /**
     * Draws the tile value on top of the tile.
     * Tile value is transformed from [-1, +1] to [-1000, +1000].
     * Text color is calculated using luminosity function from: https://en.wikipedia.org/wiki/Relative_luminance
     * Text color function has been adjusted for better readability on deep red tiles.
     * Text is always exactly centered in the tile.
     *
     * @param tile Tile for which value is to be drawn
     * @param g2   Graphics context
     */
    public static void drawTileValueText(TaflTile tile, Graphics2D g2)
    {
        double tileValue = tile.getValue();
        if (Double.isNaN(tileValue))
        {
            return;
        }

        Rectangle rect = tile.getRect();
        Color tokenColor = tile.getPlayer() == PLAYER_BLACK ? Color.BLACK : Color.WHITE;
        int luminance = (int) (0.8 * tokenColor.getRed() + 0.7152 * tokenColor.getGreen() + 0.0722 * tokenColor.getBlue());
        int luminance_inverse = Math.max(255 - luminance, 0);
        Color textColor = new Color(luminance_inverse, luminance_inverse, luminance_inverse, 255);
        g2.setColor(textColor);

        String tileText = Long.toString(Math.round(tileValue * 100));
        Font font = new Font("Times New Roman", Font.BOLD, TaflConfig.UI_TILE_SIZE / 2);
        drawCenteredString(g2, tileText, rect, font);
    }
}
