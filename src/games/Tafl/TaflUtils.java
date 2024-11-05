package games.Tafl;

import tools.Types;

import java.awt.*;
import java.util.ArrayList;

public class TaflUtils
{

    public static final int PLAYER_NONE = -1; // Beginning player
    public static final int PLAYER_BLACK = 0; // Beginning player
    public static final int PLAYER_WHITE = 1;

    public static final int EMPTY = 0;
    public static final int BLACK_TOKEN = -1;
    public static final int WHITE_TOKEN = 1;
    public static final int KING = 2;

    static final int[][] startBoard7 = new int[][] {
        {0, 0, 0, -1, 0, 0, 0},
        {0, 0, 0, -1, 0, 0, 0},
        {0, 0, 0, 1, 0, 0, 0},
        {-1, -1, 1, 2, 1, -1, -1},
        {0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, -1, 0, 0, 0},
        {0, 0, 0, -1, 0, 0, 0}
    };

    static final int[][] startBoard9 = new int[][] {
        {0, 0, 0, -1, -1, -1, 0, 0, 0},
        {0, 0, 0, 0, -1, 0, 0, 0, 0},
        {0, 0, 0, 0, 1, 0, 0, 0, 0},
        {-1, 0, 0, 0, 1, 0, 0, 0, -1},
        {-1, -1, 1, 1, 2, 1, 1, -1, -1},
        {-1, 0, 0, 0, 1, 0, 0, 0, -1},
        {0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, -1, 0, 0, 0, 0},
        {0, 0, 0, -1, -1, -1, 0, 0, 0}
    };

    static final int[][] startBoard11 = new int[][] {
        {0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0},
        {0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {-1, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1},
        {-1, 0, 0, 0, 1, 1, 1, 0, 0, 0, -1},
        {-1, -1, 0, 1, 1, 2, 1, 1, 0, -1, -1},
        {-1, 0, 0, 0, 1, 1, 1, 0, 0, 0, -1},
        {-1, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0},
        {0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0}
    };

    static boolean isTileCorner(TaflTile tile)
    {
        Point coords = tile.getCoords();
        return coords.x == 0 && coords.y == 0 || coords.x == 0 && coords.y == TaflConfig.BOARD_SIZE - 1 ||
               coords.x == TaflConfig.BOARD_SIZE - 1 && coords.y == 0 || coords.x == TaflConfig.BOARD_SIZE - 1 && coords.y == TaflConfig.BOARD_SIZE - 1;
    }

    static boolean isTileThrone(TaflTile tile)
    {
        Point coords = tile.getCoords();
        return coords.x == TaflConfig.BOARD_SIZE / 2 && coords.y == TaflConfig.BOARD_SIZE / 2;
    }

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
     * @return True or False, depending on if the tile exists
     */
    public static boolean isValidTile(int x, int y)
    {
        return (x >= 0 && x < TaflConfig.BOARD_SIZE) && (y >= 0 && y < TaflConfig.BOARD_SIZE);
    }

    static boolean isTileHostile(TaflTile tile, int player)
    {
        boolean checkHostileThrone = isTileThrone(tile) && (player == PLAYER_BLACK || tile.getToken() == EMPTY);
        boolean checkEnemy = player == PLAYER_BLACK && tile.getPlayer() == PLAYER_WHITE || player == PLAYER_WHITE && tile.getPlayer() == PLAYER_BLACK;

        return isTileCorner(tile) || checkHostileThrone || checkEnemy;
    }

    static Types.WINNER getWinner(TaflTile[][] board, TaflTile lastMovedToken)
    {
        if (lastMovedToken == null)
        {
            //System.out.println("lastMovedToken was null");
            return null;
        }
        // King escaped
        if (lastMovedToken.getToken() == KING && isTileCorner(lastMovedToken))
        {
            return Types.WINNER.PLAYER_WINS;
        }
        // King captured
        if (lastMovedToken.getPlayer() == PLAYER_BLACK)
        {
            if (isKingCaptured(board, lastMovedToken))
            {
                return Types.WINNER.PLAYER_WINS;
            }
        }
        // TODO: Add other win conditions
        return null;
    }

    static boolean isKingCaptured(TaflTile[][] board, TaflTile lastMovedToken)
    {
        ArrayList<TaflTile> neighbors = getNeighbors(board, lastMovedToken);
        for (TaflTile neighbor : neighbors)
        {
            if (neighbor.getToken() == KING)
            {
                TaflTile king = neighbor;
                if (isTileThrone(king) || isTileNextToThrone(king) || TaflConfig.RULE_HARD_KING_CAPTURE)
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
        }
        return false;
    }

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
     * @param start position
     * @param end   position
     * @return number representation for an action
     */
    static int getActionNumberFromMove(Point start, Point end)
    {
        int offset = pointToPosition(start) * TaflConfig.ACTIONS_PER_TOKEN;
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

    static Point[] getMoveFromActionNumber(int actionNumber)
    {
        Point start, end;

        int startNumber = Math.floorDiv(actionNumber, TaflConfig.ACTIONS_PER_TOKEN);
        start = positionToPoint(startNumber);
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

    static ArrayList<Point> generateMovesForToken(TaflTile[][] board, TaflTile token)
    {
        ArrayList<Point> targets = new ArrayList<>();
        Point coords = token.getCoords();
        int x = coords.x - 1;
        int y = coords.y;
        while (TaflUtils.isValidTile(x, y) && board[x][y].getToken() == EMPTY)
        {
            TaflTile tile = board[x][y];
            if (token.getToken() == KING || (!isTileCorner(tile) && !isTileThrone(tile)))
            {
                targets.add(new Point(x, y));
            }
            x--;
        }
        x = coords.x + 1;
        y = coords.y;
        while (TaflUtils.isValidTile(x, y) && board[x][y].getToken() == EMPTY)
        {
            TaflTile tile = board[x][y];
            if (token.getToken() == KING || (!isTileCorner(tile) && !isTileThrone(tile)))
            {
                targets.add(new Point(x, y));
            }
            x++;
        }
        x = coords.x;
        y = coords.y - 1;
        while (TaflUtils.isValidTile(x, y) && board[x][y].getToken() == EMPTY)
        {
            TaflTile tile = board[x][y];
            if (token.getToken() == KING || (!isTileCorner(tile) && !isTileThrone(tile)))
            {
                targets.add(new Point(x, y));
            }
            y--;
        }
        x = coords.x;
        y = coords.y + 1;
        while (TaflUtils.isValidTile(x, y) && board[x][y].getToken() == EMPTY)
        {
            TaflTile tile = board[x][y];
            if (token.getToken() == KING || (!isTileCorner(tile) && !isTileThrone(tile)))
            {
                targets.add(new Point(x, y));
            }
            y++;
        }

        return targets;
    }

    /**
     * Converts the two-dimensional board array to a one-dimensional vector.
     * Leftmost tile on board (coordinates: 0, 0) is the first element.
     * Bottom tile on board (coordinates: 0, n) is the n-th element.
     * Top tile on board (coordinates: n, 0) is the (n^2 - n + 1)-th element.
     * Rightmost tile on board (coordinates: n, n) is the (n^2)-th element.
     *
     * @param board
     * @return
     */
    static TaflTile[] boardToVector(TaflTile[][] board)
    {
        TaflTile[] boardVector = new TaflTile[board.length * board.length];
        for (int i = 0, k = 0; i < TaflConfig.BOARD_SIZE; i++)
        {
            for (int j = 0; j < TaflConfig.BOARD_SIZE; j++, k++)
            {
                boardVector[k] = board[i][j];
            }
        }
        return boardVector;
    }

    static int pointToPosition(Point p)
    {
        return p.x * TaflConfig.BOARD_SIZE + p.y;
    }

    static Point positionToPoint(int position)
    {
        int x = Math.floorDiv(position, TaflConfig.BOARD_SIZE);
        int y = position % TaflConfig.BOARD_SIZE;
        return new Point(x, y);
    }

    static Point mousePositionToPoint(int mx, int my, int boardSize)
    {
        Point p = new Point(-1, -1);
        if (mx >= 0 && my >= 0)
        {
            for (int i = 0; i < boardSize; i++)
            {
                if ((i + 1) * TaflConfig.UI_TILE_SIZE >= mx)
                {
                    p.x = i;
                    break;
                }
            }
            for (int j = 0; j < boardSize; j++)
            {
                if ((j + 1) * TaflConfig.UI_TILE_SIZE >= my)
                {
                    p.y = j;
                    break;
                }
            }
        }
        return p;
    }

    /**
     * Creates a polygon object for the specified tile
     *
     * @param i        first index
     * @param j        second index
     * @param tileSize size in px of each tile side to side
     * @return Rectangle for specified tile
     */
    public static Rectangle createRect(int i, int j, int tileSize)
    {
        int x = i * tileSize;
        int y = j * tileSize;

        return new Rectangle(x, y, tileSize, tileSize);
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
                g2.setColor(Color.BLACK);
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
     * @param tile      Tile for which value is to be drawn
     * @param g2        Graphics context
     * @param cellColor Color of cell, needed for text color calculation
     */
    public static void drawTileValueText(TaflTile tile, Graphics2D g2, Color cellColor)
    {
        double tileValue = tile.getValue();
        if (Double.isNaN(tileValue))
        {
            return;
        }

        Rectangle rect = tile.getRect();
        int x = tile.getCoords().x;
        int y = tile.getCoords().y;

        int luminance = (int) (0.8 * cellColor.getRed() + 0.7152 * cellColor.getGreen() + 0.0722 * cellColor.getBlue());
        int luminance_inverse = Math.max(255 - luminance, 0);
        Color textColor = new Color(luminance_inverse, luminance_inverse, luminance_inverse, 255);

        g2.setColor(textColor);

        String tileText = Long.toString(Math.round(tileValue * 1000));

        int width = g2.getFontMetrics().stringWidth(tileText);
        int height = g2.getFontMetrics().getHeight();

        int textX = (int) (x + (rect.width * 1.5) - (width / 2f));
        int textY = (int) (y + (rect.height / 2f) + (height));

        g2.drawString(tileText, textX, textY);
    }
}
