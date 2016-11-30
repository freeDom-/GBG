package games.ZweiTausendVierundAchzig;

import games.Arena;
import games.GameBoard;
import games.StateObservation;
import tools.Types;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Johannes on 18.11.2016.
 */
public class GameBoardZTVA extends JFrame implements GameBoard {
    private JPanel boardPanel;
    private JPanel buttonPanel;
    private JPanel vButtonPanel;
    private double[] vTable;
    private JLabel leftInfo = new JLabel(" left ");
    private JLabel rightInfo = new JLabel(" right ");
    private Arena m_Arena;	// a reference to the Arena object, needed to infer the current taskState
    private StateObserverZTVA m_so;
    private boolean arenaActReq=false;

    /**
     * The clickable buttons in the GUI, used to controll the Gameboard (Left, Up, Right, Down). The buttons will be enabled only
     * when "Play" or "Inspect V" are clicked.
     */
    protected Button[] buttons;

    /**
     * The representation of the value function corresponding to the current board
     * {@link #buttons} position.
     */
    protected JLabel[] vBoard;

    /**
     * The representation of the gameBoard
     */
    protected JLabel[][] board;


    private void initGameBoard(Arena ztvaGame) {
        m_Arena         = ztvaGame;
        buttons         = new Button[4];
        vBoard          = new JLabel[4];
        board           = new JLabel[Config.ROWS][Config.COLUMNS];
        boardPanel      = initBoard();
        buttonPanel     = initButton();
        vButtonPanel    = initvBoard();
        vTable          = new double[4];
        m_so		    = new StateObserverZTVA();	// empty table


        JPanel titlePanel = new JPanel();
        JLabel Blank=new JLabel(" ");		// a little bit of space
        JLabel Title=new JLabel("2048",SwingConstants.CENTER);
        Title.setForeground(Color.black);
        Font font=new Font("Arial",1,20);
        Title.setFont(font);
        titlePanel.add(Blank);
        titlePanel.add(Title);

        JPanel boardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        boardPanel.add(this.boardPanel);
        boardPanel.add(new Label("    "));		// some space
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(2,1,20,20));
        rightPanel.add(buttonPanel);
        rightPanel.add(vButtonPanel);
        boardPanel.add(rightPanel);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        leftInfo.setFont(font);
        rightInfo.setFont(font);
        infoPanel.add(leftInfo);
        infoPanel.add(rightInfo);
        infoPanel.setSize(100,10);

        setLayout(new BorderLayout(10,0));
        add(titlePanel,BorderLayout.NORTH);
        add(boardPanel,BorderLayout.CENTER);
        add(infoPanel,BorderLayout.SOUTH);
        pack();
        setVisible(false);
    }

    private JPanel initBoard() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(Config.ROWS,Config.COLUMNS,20,20));
        for(int row = 0; row < Config.ROWS; row++) {
            for(int column = 0; column < Config.COLUMNS; column++) {
                board[row][column] = new JLabel();
                board[row][column].setOpaque(true);
                updateBoardLabel(row, column);
                board[row][column].setForeground(Color.black);
                board[row][column].setBorder(BorderFactory.createLineBorder(Color.black, 2));
                Font font=new Font("Consolas",1,22);
                board[row][column].setFont(font);
                panel.add(board[row][column]);
            }
        }
        return panel;
    }

    private JPanel initButton() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3,3,10,10));
        buttons[0] = new Button("left");
        buttons[1] = new Button("up");
        buttons[2] = new Button("right");
        buttons[3] = new Button("down");

        Font font = new Font("Arial",1,22);

        for(int i = 0; i < 4; i++) {
            buttons[i].setBackground(Color.white);
            buttons[i].setForeground(Color.black);
            buttons[i].setFont(font);
            buttons[i].setEnabled(false);
            buttons[i].addActionListener(
                    new ActionHandler(i)  // constructor copies (i) to members (x,y)
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            Arena.Task aTaskState = m_Arena.taskState;
                            if (aTaskState == Arena.Task.PLAY)
                            {
                           //     HGameMove(i);		// i.e. make human move (i), if buttons[i] is clicked
                            }
                            if (aTaskState == Arena.Task.INSPECTV)
                            {
                           //     InspectMove(i);	// i.e. update inspection, if buttons[i] is clicked
                            }
                        }
                    }
            );
        }

        panel.add(new Label());
        panel.add(buttons[1]);
        panel.add(new Label());
        panel.add(buttons[0]);
        panel.add(new Label());
        panel.add(buttons[2]);
        panel.add(new Label());
        panel.add(buttons[3]);
        panel.add(new Label());

        return panel;
    }

    private JPanel initvBoard() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3,3,10,10));
        for(int i = 0; i < 4; i++) {
            vBoard[i]= new JLabel();
            vBoard[i].setText(" ");
            vBoard[i].setOpaque(true);
            vBoard[i].setBackground(Color.orange);
            vBoard[i].setForeground(Color.black);
            Font font=new Font("Consolas",1,22);
            vBoard[i].setFont(font);
        }
        panel.add(new Label());
        panel.add(vBoard[1]);
        panel.add(new Label());
        panel.add(vBoard[0]);
        panel.add(new Label());
        panel.add(vBoard[2]);
        panel.add(new Label());
        panel.add(vBoard[3]);
        panel.add(new Label());
        return panel;
    }

    private void updateBoardLabel(int row, int column) {
        int value = 0;
        if(m_so.getPosition(row, column).getTile() != null) {
            value = m_so.getPosition(row, column).getTile().getValue();
        }

        switch (value) {
            case 0:
                board[row][column].setText("<html><br><font color='#eee4da'>......</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#eee4da"));
                break;
            case 2:
                board[row][column].setText("<html><br><font color='#eee4da'>...</font>" + value + "<font color='#eee4da'>...</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#eee4da"));
                break;
            case 4:
                board[row][column].setText("<html><br><font color='#ede0c8'>...</font>" + value + "<font color='#ede0c8'>...</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#ede0c8"));
                break;
            case 8:
                board[row][column].setText("<html><br><font color='#f2b179'>...</font>" + value + "<font color='#f2b179'>...</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#f2b179"));
                break;
            case 16:
                board[row][column].setText("<html><br><font color='#f59563'>..</font>" + value + "<font color='#f59563'>...</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#f59563"));
                break;
            case 32:
                board[row][column].setText("<html><br><font color='#f67c5f'>..</font>" + value + "<font color='#f67c5f'>...</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#f67c5f"));
                break;
            case 64:
                board[row][column].setText("<html><br><font color='#f65e3b'>..</font>" + value + "<font color='#f65e3b'>...</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#f65e3b"));
                break;
            case 128:
                board[row][column].setText("<html><br><font color='#edcf72'>..</font>" + value + "<font color='#edcf72'>..</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#edcf72"));
                break;
            case 256:
                board[row][column].setText("<html><br><font color='#edcc61'>..</font>" + value + "<font color='#edcc61'>..</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#edcc61"));
                break;
            case 512:
                board[row][column].setText("<html><br><font color='#edc850'>..</font>" + value + "<font color='#edc850'>..</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#edc850"));
                break;
            case 1024:
                board[row][column].setText("<html><br><font color='#edc53f'>.</font>" + value + "<font color='#edc53f'>..</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#edc53f"));
                break;
            case 2048:
                board[row][column].setText("<html><br><font color='#edc22e'>.</font>" + value + "<font color='#edc22e'>..</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#edc22e"));
                break;
            case 4096:
                board[row][column].setText("<html><br><font color='#3c3a32'>.</font>" + value + "<font color='#3c3a32'>..</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#3c3a32"));
                break;
            case 8192:
                board[row][column].setText("<html><br><font color='#3c3a32'>.</font>" + value + "<font color='#3c3a32'>..</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#3c3a32"));
                break;
            default:
                board[row][column].setText("<html><br><font color='#3c3a32'>.</font>" + value + "<font color='#3c3a32'>.</font><br><br></html>");
                board[row][column].setBackground(Color.decode("#3c3a32"));
                break;
        }
    }

    @Override
    public void clearBoard(boolean boardClear, boolean vClear) {
        if(boardClear) {
            m_so = new StateObserverZTVA();
            for(int row=0;row<Config.ROWS;row++){
                for(int column=0;column<Config.COLUMNS;column++){
                    updateBoardLabel(row, column);
                }
            }
        }
        if(vClear) {
            vTable = new double[4];
            for(int i = 0; i < 4; i++) {
                vTable[i] = Double.NaN;
                vBoard[i].setText(" ");
                vBoard[i].setBackground(Color.orange);
                vBoard[i].setForeground(Color.black);
            }
        }
    }

    @Override
    public void updateBoard(StateObservation so, boolean showStoredV, boolean enableOccupiedCells) {
        //ToDO: da Zellen keine Buttons sind bin ich mir nicht sicher wofür ich die Variable enable benutzen soll/kann

        if(so != null) {
            assert (so instanceof StateObserverZTVA): "StateObservation 'so' is not an instance of StateObserverZTVA";
            StateObserverZTVA soZTVA = (StateObserverZTVA) so;
            m_so = soZTVA.copy();

            if(so.isGameOver()) {
                int win = so.getGameWinner().toInt();
                switch(win) {
                    case(+1):
                        leftInfo.setText("You Won!");
                        break;
                    case(-1):
                        leftInfo.setText("You Lost!");
                        break;
                    default:
                        leftInfo.setText("Bug in Winstate @Line 316, please inform the Programmer! :-)");
                        break;
                }
            }

            if (showStoredV && soZTVA.storedValues!=null) {
                for(int i=0;i<4;i++) {
                    vTable[i] = Double.NaN;
                }

                for(int i = 0; i < soZTVA.storedValues.length; i++) {
                    Types.ACTIONS action = soZTVA.storedActions[i];
                    int iAction = action.toInt();
                    vTable[iAction] = soZTVA.storedValues[i];
                }

                rightInfo.setText("    Score for next Move");
            }
            else {
                rightInfo.setText("");
            }
        }
        guiUpdateBoard(enableOccupiedCells);
    }

    private void guiUpdateBoard(boolean enable) {
        //ToDO: da Zellen keine Buttons sind bin ich mir nicht sicher wofür ich die Variable enable benutzen soll/kann

        double score, maxscore=Double.NEGATIVE_INFINITY;
        int imax = 0;

        for(int row = 0; row < Config.ROWS; row++) {
            for (int column = 0; column < Config.COLUMNS; column++) {
                updateBoardLabel(row,column);
            }
        }

        for(int i = 0; i < 4; i++) {
            if(vTable == null) {
                // HumanPlayer and MCTSAgentT do not have a VTable (!)
                score = Double.NaN;
            }
            else {
                score = vTable[i];
            }
            if(Double.isNaN(score)) {
                vBoard[i].setText("   ");
                vBoard[i].setBackground(Color.green);
            } else {
                String txt = " "+(int)(score*100);
                if (score<0) txt = ""+(int)(score*100);
                vBoard[i].setText(txt);
                if (m_so.viableMoves.contains(i)) {
                    //ToDo: nicht sicher, sollte alle Viable Moves orange hinterlegen
                    vBoard[i].setBackground(Color.orange);
                    buttons[i].setEnabled(true);
                } else {
                    vBoard[i].setBackground(Color.green);
                    buttons[i].setEnabled(false);
                }
                if (score>maxscore) {
                    maxscore=score;
                    imax=i;
                }
            }
        }
        vBoard[imax].setBackground(Color.yellow);
        paint(this.getGraphics());
    }

    @Override
    public void showGameBoard(Arena ztvaGame) {
        this.setVisible(true);
        // place window with game board below the main window
        int x = ztvaGame.m_xab.getX() + ztvaGame.m_xab.getWidth() + 8;
        int y = ztvaGame.m_xab.getLocation().y;
        if (ztvaGame.m_TicFrame!=null) {
            x = ztvaGame.m_TicFrame.getX();
            y = ztvaGame.m_TicFrame.getY() + ztvaGame.m_TicFrame.getHeight() +1;
            this.setSize(750,550);
        }
        this.setLocation(x,y);
    }

    @Override
    public boolean isActionReq() {
        return arenaActReq;
    }

    @Override
    public void setActionReq(boolean actionReq) {
        arenaActReq=actionReq;
    }

    @Override
    public void enableInteraction(boolean enable) {

    }

    @Override
    public StateObservation getStateObs() {
        return m_so;
    }

    @Override
    public StateObservation getDefaultStartState() {
        clearBoard(true, true);
        return m_so;
    }

    @Override
    public StateObservation chooseStartState01() {
        return null;
    }

    public GameBoardZTVA(Arena ztvaGame) {
        initGameBoard(ztvaGame);
    }



    class ActionHandler implements ActionListener {
        int move;

        ActionHandler(int move)
        {
            this.move = move;
        }
        public void actionPerformed(ActionEvent e){}
    }
}
