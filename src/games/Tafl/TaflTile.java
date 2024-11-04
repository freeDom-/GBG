package games.Tafl;

import java.awt.*;
import java.io.Serializable;

public class TaflTile implements Serializable {

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .gamelog containing this object will become
     * unreadable or you have to provide a special version transformation)
     */
    private static final long serialVersionUID = 1L;

    private Point coords;
    private int player;
    private int token;
    private Rectangle rect;
    private double value;

    /**
     * Constructor for tiles that are not supposed to be drawn to the screen.
     *
     * @param i first index in board array
     * @param j second index in board array
     */
    public TaflTile(int i, int j, int token) {
        coords = new Point(i, j);
        this.token = token;
        this.player = switch (token) {
            case TaflUtils.BLACK_TOKEN -> TaflUtils.PLAYER_BLACK;
            case TaflUtils.WHITE_TOKEN, TaflUtils.KING -> TaflUtils.PLAYER_WHITE;
            default -> TaflUtils.PLAYER_NONE;
        };
        value = Double.NaN;
    }

    /**
     * Constructor for tiles that are part of the visible game board.
     *
     * @param i     first index in board array
     * @param j     second index in board array
     * @param token The token that is present on the tile
     * @param rect  Rectangle containing the vertices needed for drawing the tile to the screen
     * @param value Tile value
     */
    public TaflTile(int i, int j, int token, Rectangle rect, double value) {
        coords = new Point(i, j);
        this.token = token;
        this.player = switch (token) {
            case TaflUtils.BLACK_TOKEN -> TaflUtils.PLAYER_BLACK;
            case TaflUtils.WHITE_TOKEN, TaflUtils.KING -> TaflUtils.PLAYER_WHITE;
            default -> TaflUtils.PLAYER_NONE;
        };
        this.rect = rect;
        this.value = value;
    }

    /**
     * @return A point, with x and y being the place in the board array
     */
    public Point getCoords() {
        return coords;
    }

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
        this.player = switch (token) {
            case TaflUtils.BLACK_TOKEN -> TaflUtils.PLAYER_BLACK;
            case TaflUtils.WHITE_TOKEN, TaflUtils.KING -> TaflUtils.PLAYER_WHITE;
            default -> TaflUtils.PLAYER_NONE;
        };
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public Rectangle getRect() {
        return rect;
    }

    public void setRect(Rectangle rect) {
        this.rect = rect;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof TaflTile &&
                ((TaflTile) object).getCoords().x == this.coords.x &&
                ((TaflTile) object).getCoords().y == this.coords.y;
    }

    public double getValue() {
        return value;
    }

    /**
     * Sets the tile value given by the agent that had the latest turn. Used when drawing the tiles.
     *
     * @param value Tile value
     */
    public void setValue(double value) {
        this.value = value;
    }

    public TaflTile copy() {
        return new TaflTile(coords.x, coords.y, token, rect, value);
    }

    @Override
    public String toString() {
        return "TaflTile [" + coords.x + ", " + coords.y + "]";
    }
}
