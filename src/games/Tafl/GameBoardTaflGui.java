package games.Tafl;

import games.Arena;
import games.StateObservation;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameBoardTaflGui
{

    final static boolean GRAYSCALE = false; //Used to take screenshots of board states without using color
    final static Color COLOR_PLAYER_BLACK = Color.BLACK;
    final static Color COLOR_PLAYER_WHITE = Color.WHITE;
    final static Color COLOR_CELL = GRAYSCALE ? Color.LIGHT_GRAY : Color.ORANGE;
    final static Color COLOR_GRID = Color.GRAY;
    final static Color COLOR_CORNER = Color.PINK;
    final static Color COLOR_THRONE = Color.PINK;
    final static Color BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private final int WINDOW_HEIGHT;
    private final int WINDOW_WIDTH;
    private GameBoardTaflGui.TaflPanel gamePanel;
    private JFrame m_frame = new JFrame("Tafl - GBG");
    /**
     * a reference to the 'parent' {@link GameBoardTafl} object
     */
    private GameBoardTafl m_gb = null;

    private Point selectedToken;

    public GameBoardTaflGui(GameBoardTafl gb)
    {
        m_gb = gb;

        //Board size +2 to account for offset on top and bottom of the window
        WINDOW_HEIGHT = TaflConfig.UI_TILE_SIZE * TaflConfig.BOARD_SIZE + TaflConfig.UI_OFFSET * 2;
        WINDOW_WIDTH = TaflConfig.UI_TILE_SIZE * TaflConfig.BOARD_SIZE + TaflConfig.UI_OFFSET * 2;

        createAndShowGUI();
    }

    private void createAndShowGUI()
    {
        gamePanel = new GameBoardTaflGui.TaflPanel();
        //m_frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        m_frame.getContentPane().setBackground(Color.black);
        Container content = m_frame.getContentPane();
        content.add(gamePanel);
        m_frame.getContentPane().setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        m_frame.pack();
        m_frame.setResizable(false);
        m_frame.setLocationRelativeTo(null);
        m_frame.setVisible(true);
    }

    public void clearBoard(boolean boardClear, boolean vClear)
    {
        m_frame.repaint();
    }

    public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard)
    {
        gamePanel.setShowValues(showValueOnGameboard);

        if (so == null)
        {
            m_frame.repaint();
            return;
        }

        m_frame.repaint();
    }

    public void enableInteraction(boolean enable)
    {

    }

    public void showGameBoard(Arena arena, boolean alignToMain)
    {
        gamePanel.setVisible(true);
    }

    public void toFront()
    {
        gamePanel.toFront();
    }

    public void destroy()
    {
        m_frame.setVisible(false);
        m_frame.dispose();
    }

    /**
     * Class TaflPanel is used as the GUI for the game Tafl. It extends the class JPanel.
     * It is responsible for drawing the game state to the screen.
     * Includes a child class TaflMouseListener to process user input.
     */
    public class TaflPanel
        extends JPanel
    {

        boolean showValues = true;

        TaflPanel()
        {
            setBackground(GameBoardTaflGui.BACKGROUND_COLOR);

            TaflMouseListener ml = new TaflMouseListener();
            addMouseListener(ml);
        }

        public void toFront()
        {
            m_frame.setState(Frame.NORMAL);    // if window is iconified, display it normally
            super.setVisible(true);
        }

        public void setShowValues(boolean showValueOnGameboard)
        {
            showValues = showValueOnGameboard;
        }

        /**
         * Update the GUI based on current game state each time the window is repainted
         *
         * @param g The graphics context
         */
        public void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D) g;
            super.paintComponent(g2);

            //Don't draw the game board while training to save CPU cycles
            if (m_gb.getArena().taskState != Arena.Task.TRAIN
                && m_gb.getArena().taskState != Arena.Task.MULTTRN
            )
            {
                drawBoardToPanel(g2, showValues);
            }
        }

        /**
         * Draw the current game state to the TaflPanel. State information is read via
         * the StateObserverTafl instance that is managed by the parent class.
         * Requires static TaflUtils class.
         *
         * @param g2         The graphics context required for drawing to the screen.
         * @param showValues Whether tile values should be visible or not.
         */
        private void drawBoardToPanel(Graphics2D g2, boolean showValues)
        {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            TaflTile lastMovedToken = m_gb.m_so.getlastMovedToken();

            //draw tiles
            if (GRAYSCALE)
            {
                showValues = false;
            }
            for (int y = 0; y < TaflConfig.BOARD_SIZE; y++)
            {
                for (int x = 0; x < TaflConfig.BOARD_SIZE; x++)
                {
                    TaflTile tile = m_gb.m_so.getBoard()[x][y];
                    Color cellColor = getBackgroundColor(tile);
                    drawTile(tile, g2, cellColor, false);
                    if (showValues && !GRAYSCALE)
                    {
                        drawTileValueText(tile, g2);
                    }
                }
            }

            //draw last placed tile again so its highlighting overlaps all other tiles
            if (lastMovedToken != null && !GRAYSCALE)
            {
                Color cellColor = getBackgroundColor(lastMovedToken);
                drawTile(lastMovedToken, g2, cellColor, true);
                if (showValues)
                {
                    drawTileValueText(lastMovedToken, g2);
                }
            }
        }

        private Color getBackgroundColor(TaflTile tile)
        {
            if (!TaflConfig.RULE_NO_SPECIAL_TILES && TaflUtils.isTileCorner(tile))
            {
                return COLOR_CORNER;
            }
            if (!TaflConfig.RULE_NO_SPECIAL_TILES && TaflUtils.isTileThrone(tile))
            {
                return COLOR_THRONE;
            }
            return COLOR_CELL;
        }

        /**
         * Draws a single tile
         *
         * @param tile            Tile to be drawn
         * @param g2              Graphics context
         * @param backgroundColor Color to draw the tile in
         * @param highlight       If a red border surrounding the tile should be drawn
         */
        private void drawTile(TaflTile tile, Graphics2D g2, Color backgroundColor, boolean highlight)
        {
            Rectangle rect = tile.getRect();

            if (tile.getPlayer() != TaflUtils.PLAYER_NONE)
            {
                g2.setColor(backgroundColor);
                g2.fillRect(rect.x, rect.y, rect.width, rect.height);
                Color tokenColor = tile.getPlayer() == TaflUtils.PLAYER_BLACK ? GameBoardTaflGui.COLOR_PLAYER_BLACK : GameBoardTaflGui.COLOR_PLAYER_WHITE;
                if (tile.getCoords().equals(selectedToken))
                {
                    tokenColor = new Color(tokenColor.getRed(), tokenColor.getBlue(), tokenColor.getGreen(), 127);
                }
                g2.setColor(tokenColor);
                g2.fillOval(rect.x, rect.y, rect.width, rect.height);
                if (tile.getToken() == TaflUtils.KING)
                {
                    g2.setColor(Color.LIGHT_GRAY);
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
        private void drawCenteredString(Graphics g, String text, Rectangle rect, Font font)
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
        private void drawTileValueText(TaflTile tile, Graphics2D g2)
        {
            double tileValue = tile.getValue();
            if (Double.isNaN(tileValue))
            {
                return;
            }

            Rectangle rect = tile.getRect();
            Color tokenColor = tile.getPlayer() == TaflUtils.PLAYER_BLACK ? Color.BLACK : Color.WHITE;
            int luminance = (int) (0.8 * tokenColor.getRed() + 0.7152 * tokenColor.getGreen() + 0.0722 * tokenColor.getBlue());
            int luminance_inverse = Math.max(255 - luminance, 0);
            Color textColor = new Color(luminance_inverse, luminance_inverse, luminance_inverse, 255);
            g2.setColor(textColor);

            String tileText = Long.toString(Math.round(tileValue * 100));
            Font font = new Font("TimesRoman", Font.PLAIN, TaflConfig.UI_TILE_SIZE / 4);
            drawCenteredString(g2, tileText, rect, font);
        }

        /**
         * Converts the pixel that was clicked to the tile that is at that exact location,
         * taking into account the non-rectangular geometry of the tiles. Calculation done in TaflUtils.
         */
        class TaflMouseListener
            extends MouseAdapter
        {
            public void mouseReleased(MouseEvent e)
            {
                if (m_gb.isActionReq() ||
                    (m_gb.getArena().taskState != Arena.Task.PLAY && m_gb.getArena().taskState != Arena.Task.INSPECTV))
                {
                    return;
                }
                Point p = new Point(TaflUtils.mousePositionToPoint(e.getX(), e.getY(), TaflConfig.BOARD_SIZE));
                if (p.x < 0 || p.y < 0 || p.x >= TaflConfig.BOARD_SIZE || p.y >= TaflConfig.BOARD_SIZE)
                    return;

                if (selectedToken == null)
                {
                    TaflTile clicked = m_gb.m_so.getBoard()[p.x][p.y];
                    if (clicked.getPlayer() == m_gb.m_so.getPlayer() && !TaflUtils.generateMovesForToken(m_gb.m_so.getBoard(), clicked).isEmpty())
                    {
                        selectedToken = p;
                        m_gb.m_so.storeBestActionInfo(selectedToken);
                        m_frame.repaint();
                    }
                    return;
                }

                Types.ACTIONS act = Types.ACTIONS.fromInt(TaflUtils.getActionNumberFromMove(selectedToken, p));
                selectedToken = null;
                m_gb.m_so.advance(act, null);
                if (m_gb.getArena().taskState == Arena.Task.PLAY)
                {
                    // only do this when passing here during 'PLAY': add a log entry in case of Human move
                    // Do NOT do this during 'INSPECT', because then we have (currently) no valid log session ID
                    (m_gb.getArena().getLogManager()).addLogEntry(act, m_gb.m_so, m_gb.getArena().getLogSessionID());
                }
                updateBoard(null, false, false);
                m_gb.setActionReq(true);
            }
        } // class TaflMouseListener
    } // class TaflPanel
}
