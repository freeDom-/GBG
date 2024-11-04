package games.Tafl;

import tools.Types;

import java.awt.*;
import java.util.ArrayList;

public class TaflUtils {

    public static final int PLAYER_NONE = -1; // Beginning player
    public static final int PLAYER_BLACK = 0; // Beginning player
    public static final int PLAYER_WHITE = 1;

    public static final int EMPTY = 0;
    public static final int BLACK_TOKEN = -1;
    public static final int WHITE_TOKEN = 1;
    public static final int KING = 2;

    static final int[][] startBoard7 = new int[][]{
            {0, 0, 0, -1, 0, 0, 0},
            {0, 0, 0, -1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0},
            {-1, -1, 1, 2, 1, -1, -1},
            {0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, -1, 0, 0, 0},
            {0, 0, 0, -1, 0, 0, 0}
    };

    static final int[][] startBoard9 = new int[][]{
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

    static final int[][] startBoard11 = new int[][]{
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

    static boolean isTileCorner(TaflTile tile) {
        Point coords = tile.getCoords();
        return coords.x == 0 && coords.y == 0 || coords.x == 0 && coords.y == TaflConfig.BOARD_SIZE - 1 ||
                coords.x == TaflConfig.BOARD_SIZE - 1 && coords.y == 0 || coords.x == TaflConfig.BOARD_SIZE - 1 && coords.y == TaflConfig.BOARD_SIZE - 1;
    }

    static boolean isTileThrone(TaflTile tile) {
        Point coords = tile.getCoords();
        return coords.x == TaflConfig.BOARD_SIZE / 2 && coords.y == TaflConfig.BOARD_SIZE / 2;
    }

    static Types.WINNER getWinner(TaflTile[][] board, TaflTile lastMovedToken) {
        // White wins
        if (lastMovedToken.getToken() == KING && isTileCorner(lastMovedToken)) {
            return Types.WINNER.PLAYER_WINS;
        }
        // Black wins
        if (lastMovedToken.getPlayer() == PLAYER_BLACK) {
            if (isKingCaptured(TaflTile[][] board, lastMovedToken)) {
                return Types.WINNER.PLAYER_WINS;
            }
        }
        return null;
    }

    static boolean isKingCaptured(TaflTile[][] board, TaflTile lastMovedToken) {
        TaflTile[] neighbors = getNeighbors(board, lastMovedToken);
        for (TaflTile neighbor : neighbors) {
            if (neighbor.getToken() == KING) {
                if (TaflConfig.RULE_HARD_KING_CAPTURE) {
                    TaflTile[] kingsNeighbors = getNeighbors(board, neighbor);
                    for (TaflTile kingsNeighbor : kingsNeighbors) {
                        if (kingsNeighbor.getToken() != BLACK_TOKEN) {
                            return false;
                        }
                    }
                    return true;
                }
                else {
                    // Check token behind king
                    // TODO: implement simple king capture
                }
            }
        }
    }

    static TaflTile[] getNeighbors(TaflTile[][] board, TaflTile tile) {
        ArrayList<TaflTile> neighbors = new ArrayList<>();
        Point coords = tile.getCoords();
        int x = coords.x;
        int y = coords.y;
        TaflTile neighbor = board[x - 1][y];
        if (x > 0 && neighbor.getToken() != EMPTY) {
            neighbors.add(neighbor);
        }
        neighbor = board[x + 1][y];
        if (x < TaflConfig.BOARD_SIZE - 1 && neighbor.getToken() != EMPTY) {
            neighbors.add(neighbor);
        }
        neighbor = board[x][y - 1];
        if (y > 0 && neighbor.getToken() != EMPTY) {
            neighbors.add(neighbor);
        }
        neighbor = board[x][y + 1];
        if (y < TaflConfig.BOARD_SIZE - 1 && neighbor.getToken() != EMPTY) {
            neighbors.add(neighbor);
        }

        return neighbors.toArray(new TaflTile[2]);
    }

    static int getTargetActionNumber(Point start, Point end) {
        int offset = pointToPosition(start) * TaflConfig.ACTIONS_PER_TOKEN;
        if (start.x == end.x) {
            return offset + end.y;
        } else if (start.y == end.y) {
            return offset + end.x + TaflConfig.BOARD_SIZE;
        } else {
            throw new RuntimeException(String.format("Illegal move (%d,%d) -> (%d,%d)", start.x, start.y, end.x, end.y));
        }
    }

    static int pointToPosition(Point p) {
        return p.x * TaflConfig.BOARD_SIZE + p.y;
    }

    static Point positionToPoint(int mx, int my, int boardSize) {
        Point p = new Point(-1, -1);
        if (mx >= 0 && my >= 0) {
            for (int i = 0; i < boardSize; i++) {
                if ((i + 1) * TaflConfig.UI_TILE_SIZE >= mx) {
                    p.x = i;
                    break;
                }
            }
            for (int j = 0; j < boardSize; j++) {
                if ((j + 1) * TaflConfig.UI_TILE_SIZE >= my) {
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
    public static Rectangle createRect(int i, int j, int tileSize) {
        int x = i * tileSize;
        int y = j * tileSize;

        return new Rectangle(x, y, tileSize, tileSize);
    }

    /**
     * Draws a single tile
     *
     * @param tile      Tile to be drawn
     * @param g2        Graphics context
     * @param cellColor Color to draw the tile in
     * @param highlight If a red border surrounding the tile should be drawn
     */
    public static void drawTile(TaflTile tile, Graphics2D g2, Color cellColor, boolean highlight) {
        Rectangle rect = tile.getRect();

        g2.setColor(cellColor);
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);

        if (highlight) {
            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.RED);
        } else {
            g2.setColor(GameBoardTaflGui.COLOR_GRID);
        }

        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
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
    public static void drawTileValueText(TaflTile tile, Graphics2D g2, Color cellColor) {
        double tileValue = tile.getValue();
        if (Double.isNaN(tileValue)) {
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
