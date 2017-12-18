package games;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import agentIO.LoadSaveTD;
import tools.Progress;
import controllers.AgentBase;
import controllers.ExpectimaxWrapper;
import controllers.PlayAgent;
import controllers.MC.MCAgent;
import controllers.MCTS.MCTSAgentT;
import games.Hex.GameBoardHex;
import games.Hex.HexTile;
import games.Hex.StateObserverHex;
import games.MTrain;
import games.Arena.Task;
import games.TicTacToe.LaunchArenaTTT;
import games.TicTacToe.LaunchTrainTTT;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import tools.MessageBox;
import tools.StatusBar;
import tools.Types;

/**
 * This class contains the GUI and the task dispatcher for the game. The GUI for 
 * buttons and choice boxes is in {@link XArenaButtons}.
 * <p>
 * Run this class for example from {@code main} in 
 * {@link LaunchArenaTTT} or {@link LaunchTrainTTT} for the TicTacToe game.
 * 
 * @author Wolfgang Konen, TH K�ln, Nov'16
 */
abstract public class Arena extends JPanel implements Runnable {
	public enum Task {PARAM, TRAIN, MULTTRN, PLAY, INSPECTV
		 //, INSPECTNTUP, BAT_TC, BATCH
		 , COMPETE, SWAPCMP, MULTCMP, IDLE  };
	public XArenaFuncs m_xfun;
	public JFrame m_TicFrame = null;  
	public XArenaMenu m_menu = null;
	public XArenaTabs m_tabs = null;
	public XArenaButtons m_xab;	// the game buttons and text fields
	private Thread playThread = null;	
	private Progress progress = null; // progress for some functions
	public LoadSaveTD tdAgentIO;	// saving/loading of agents
	protected JLabel m_title;
	protected GameBoard gb;
	protected StatusBar statusBar = new StatusBar();
	public Task taskState = Task.IDLE;
	public Task taskBefore = Task.IDLE;

	public int minSleepDuration = 0;
	public int maxSleepDuration = 2000;
	public int currentSleepDuration = 0;
	public LogManager logManager;
	private int logSessionid;
	
	public Arena() {
		initGame();
	}
	public Arena(JFrame frame) {
		m_TicFrame = frame; 
		initGame();
	}
	
	protected void initGame() {
		gb = makeGameBoard();
		gb.setActionReq(false);
		
		m_xfun 		= new XArenaFuncs(this);
		m_xab    	= new XArenaButtons(m_xfun,this);		// needs a constructed 'gb'		
		tdAgentIO = new LoadSaveTD(this, m_xab, m_TicFrame);

		JPanel titlePanel = new JPanel();
		titlePanel.setBackground(Types.GUI_BGCOLOR);
		JLabel Blank=new JLabel(" ");		// a little bit of space
		m_title=new JLabel("Arena",SwingConstants.CENTER);
		m_title.setForeground(Color.black);	
		Font font=new Font("Arial",1,20);			
		m_title.setFont(font);	
		titlePanel.add(Blank);
		titlePanel.add(m_title);
		
		JPanel infoPanel = new JPanel(new BorderLayout(0,0));
		infoPanel.setBackground(Types.GUI_BGCOLOR);
		statusBar.setBackground(Types.GUI_BGCOLOR);
		setLayout(new BorderLayout(10,0));
		setBackground(Types.GUI_BGCOLOR);				//Color.white
		JPanel jPanel = new JPanel();
		jPanel.setBackground(Types.GUI_BGCOLOR);
		//new JLabel(" ")
		infoPanel.add(jPanel,BorderLayout.NORTH); // a little gap
		//infoPanel.add(boardPanel,BorderLayout.CENTER);
		infoPanel.add(statusBar,BorderLayout.SOUTH);	

		m_menu = new XArenaMenu(this, m_TicFrame);
		m_tabs = new XArenaTabs(this);
		add(titlePanel,BorderLayout.NORTH);				
		add(m_xab,BorderLayout.CENTER);	
		add(infoPanel,BorderLayout.SOUTH);

		logManager = new LogManager();
		logManager.setSubDir(gb.getSubDir());
	}
	
	public void init()
	{	
		// this causes Arena.run() to be executed as a separate thread
		playThread = new Thread(this);
		playThread.start();		
	}	

	public void run() {
		String agentN;
		int n;
		gb.showGameBoard(this,true);
		
		try {
			m_xfun.m_PlayAgents = m_xfun.fetchAgents(m_xab);
			// ensure that m_PlayAgents has the agents selected, even if the first 
			// thing is to issue a save agent command.
			// ensure that the most recent parameters for this agent are fetched from 
			// the params tab.
		} catch(RuntimeException e) 
		{
			MessageBox.show(m_xab, 
					e.getMessage(), 
					"Warning", JOptionPane.WARNING_MESSAGE);
			taskState = Task.IDLE;		
			setStatusMessage("Done.");
			//return;			
		}

		while(true)
		{
			switch(taskState) {
			case COMPETE:
				enableButtons(false);
				setStatusMessage("Running Single Compete ...");
				
				m_xfun.singleCompete(m_xab, gb);
				
				enableButtons(true);
				setStatusMessage("Compete finished.");
				UpdateBoard();
				taskState = Task.IDLE; 
				break;				
			case SWAPCMP:
				enableButtons(false);
				setStatusMessage("Running Swap Compete ...");
				
				m_xfun.swapCompete(m_xab, gb);
				
				enableButtons(true);
				setStatusMessage("Swap Compete finished.");
				UpdateBoard();
				taskState = Task.IDLE; 
				break;				
			case MULTCMP:
				boolean silent=false;
				enableButtons(false);
				setStatusMessage("Running Multi Compete ...");
				
				try {
					m_xfun.multiCompete(silent, m_xab, gb);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				enableButtons(true);
				setStatusMessage("Multi Compete finished.");
				UpdateBoard();
				taskState = Task.IDLE; 
				break;				
			case PLAY: 
				//enableButtons(false);		// see Play.addActionListener in XArenaButtons
				gb.showGameBoard(this,false);
				gb.clearBoard(false,true);
				PlayGame();
				enableButtons(true);
				break;
			case INSPECTV:
				gb.showGameBoard(this,false);
				gb.clearBoard(false,true);
				gb.setActionReq(true);
				InspectGame();
				break;
			case IDLE:
			default:
				try
				{
					Thread.sleep(100);
				}	
				catch (Exception e){}
			}

			performArenaDerivedTasks();
			// a derived class, e.g. ArenaTrain, may define additional tasks 
			// in this method

		}
	}
	
	protected void UpdateBoard() {
		gb.updateBoard(null, false, false);
	}

	/**
	 * Inspect the value function in m_PlayAgent[0] of {@link #m_xfun}. This agent will 
	 * be initially the one set in {@link XArenaFuncs}'s constructor (usually {@link controllers.MinimaxAgent})
	 * or the agent last trained via button "Train X"
	 */
	protected void InspectGame() {
		String AgentX = m_xab.getSelectedAgent(0);
		StateObservation so;
		Types.ACTIONS_VT actBest;
		PlayAgent paX;
		PlayAgent[] paVector,qaVector;
		
		boolean DBG_HEX=false;
		
		int numPlayers = gb.getStateObs().getNumPlayers();
		int wrappedNPly=m_xab.oPar.getWrapperNPly();
		try 
		{
			paVector = m_xfun.fetchAgents(m_xab);
			AgentBase.validTrainedAgents(paVector,numPlayers);
			qaVector = m_xfun.wrapAgents(paVector,wrappedNPly,gb.getStateObs());
			paX = qaVector[0];
		} catch(RuntimeException e) 
		{
			MessageBox.show(m_xab, 
					e.getMessage(), 
					"Warning", JOptionPane.WARNING_MESSAGE);
			taskState = Task.IDLE;		
			setStatusMessage("Done.");
			return;			
		}

		String pa_string = paX.getClass().getName();
		System.out.println("[InspectGame] "+pa_string);
		
		gb.clearBoard(true, true);
		gb.updateBoard(null,false,true);				// enable Board buttons
		while(taskState == Task.INSPECTV)
		{			
			if(gb.isActionReq()){
				gb.setActionReq(false);
				so = gb.getStateObs();
				
				if (DBG_HEX && (so instanceof StateObserverHex)) {
					StateObserverHex soh = (StateObserverHex) so; 
					XNTupleFuncs xnf = this.makeXNTupleFuncs();
					int[] bvec = xnf.getBoardVector(soh);		// look at bvec in debugger to see the board representation
					//int Index = this.getHexIndex(soh.getBoard());
					//System.out.println("Index: "+Index);
					System.out.println("["+soh.stringDescr()+"]");
					int dummy=1;
				}
				
				if (so.isLegalState() && !so.isGameOver()) {
					boolean silent=false;
					actBest = paX.getNextAction2(so, false, true);
					if (actBest!=null)  // a HumanAgent will return actBest=null
						so.storeBestActionInfo(actBest, actBest.getVTable());
					
					gb.updateBoard(so,false,true);
				} else {
					gb.updateBoard(so,false,true);
					// not a valid play position >> show the board settings, i.e. 
					// the game-over position, but clear the values: 
					gb.clearBoard(false,true);
					
					break; // out of while
				}
			}
			else
			{
				try
				{
					Thread.sleep(100);
					//
					// wait until an action in GameBoard gb occurs (see
					// ActionListener in GameBoardTTT::InitBoard() or MyMouseListener
					// in GameBoardHex), which will set 
					// gb.isActionReq() to true again. 
				}	
				catch (Exception e){}
			}

		} // while(taskState == Task.INSPECTV) [will be left only by the break above or when taskState changes]
		
		// We arrive here under three conditions:
		//		1) InspectV pressed again --> taskState changed to Task.IDLE
		//  	2) game over --> break in while above
		// 		3) Play button pressed --> taskState changed to Task.PLAY
		// In case 1)+2) we want to be in IDLE mode, but in case 3) we want to stay in PLAY mode.
		// That is the reason for the if-clause in the next line:
		
		if (taskState!=Task.PLAY) {
			// game over or InspectV pressed again --> leave the INSPECTV task
			taskState = Task.IDLE;		
			setStatusMessage("Done.");			
		}
	}

	// only needed in case DBG_HEX for debugging the TDAgent in case 2x2 Hex
    private int getHexIndex(HexTile[][] board){
    	if (board[0].length!=2)
    		throw new RuntimeException("getHexIndex only available for 2x2 board");
    	
        int index=0; // index into LUT = inputVector 
        for (int i = 0, k=1; i<2; i++ ){
            for (int j = 0; j<2; j++){
                int posValue = board[i][j].getPlayer()+1;
                index += k*posValue;
                k *= 3;
            }
        }

        return index;
    }

	/**
	 * Play a game (using the agents selected in the combo boxes).  
	 * One or multiple of them may be "Human".
	 * For 2-player games it is a game "X vs. O".
 	 */
	public void PlayGame()
	{
		int Player,player;
		StateObservation so;
		Types.ACTIONS_VT actBest=null;
		MCTSAgentT p2 = new MCTSAgentT("MCTS",null,m_xab.mctsParams,m_xab.oPar); // only DEBG
		PlayAgent pa;
		PlayAgent[] paVector,qaVector;
		int nEmpty = 0,cumEmpty=0;
		int moveNum=0;
		double gameScore=0.0;
		PStats pstats;
		ArrayList<PStats> psList = new ArrayList<PStats>();

		// fetch the agents in a way general for 1-, 2- and N-player games
		int numPlayers = gb.getStateObs().getNumPlayers();
		int wrappedNPly=m_xab.oPar.getWrapperNPly();
		boolean showValue = (taskState==Task.PLAY) ? m_xab.getShowValueOnGameBoard() : true;
		boolean showStoredV=true; 
		
		try 
		{
			paVector = m_xfun.fetchAgents(m_xab);
			AgentBase.validTrainedAgents(paVector,numPlayers);
			qaVector = m_xfun.wrapAgents(paVector,wrappedNPly,gb.getStateObs());
		} catch(RuntimeException e) 
		{
			MessageBox.show(m_xab, 
					e.getMessage(), 
					"Warning", JOptionPane.WARNING_MESSAGE);
			taskState = Task.IDLE;		
			setStatusMessage("Done.");
			return;			
		}


		String agentX ="";
		String agentO ="";
		String[] agentVec = new String[numPlayers];
		String sMsg ="";

		gb.toFront();

		switch (numPlayers) {
			case (1):
				agentX = m_xab.getSelectedAgent(0);
				sMsg = "Playing a game ... [ "+agentX+" ]"; 
				if (wrappedNPly>0)
					sMsg = "Playing a game ... [ "+agentX+", nPly="+wrappedNPly+" ]"; 
				break;
			case (2):
				agentX = m_xab.getSelectedAgent(0);
				agentO = m_xab.getSelectedAgent(1);
				sMsg = "Playing a game ... ["+agentX+" (X) vs. "+agentO+" (O)]";
				break;
			default:
				sMsg = "Playing a game ... [";
				for (int n=0; n<numPlayers; n++) {
					agentVec[n] = m_xab.getSelectedAgent(0);
					sMsg = sMsg+agentVec[n]+"("+n+")";
					if (n<numPlayers-1) sMsg = sMsg + ", ";
				}
				sMsg = sMsg+"]";
				break;
		}

		setStatusMessage(sMsg);
		System.out.println(sMsg);
		
		// if taskBefore==INSPECTV, start from the board left by InspectV,
		// if taskBefore!=INSPECTV, start from empty board:
		if (taskBefore!=Task.INSPECTV) {
			gb.clearBoard(true,true);
		}
		taskBefore=Task.IDLE;
		
		gb.setActionReq(true);
		so = gb.getStateObs();
		
		assert qaVector.length == so.getNumPlayers() : 
			  "Number of agents does not match so.getNumPlayers()!";

		logSessionid = logManager.newLoggingSession(so);

		try 
		{
		  while(taskState == Task.PLAY)	// game play interruptible by hitting 'Play' again 
		  {			
			if(gb.isActionReq()){
				moveNum++;
				so = gb.getStateObs();
				//int[] test= so.getBoardVector();
				player = so.getPlayer();
				pa = qaVector[player];
					if (pa instanceof controllers.HumanPlayer) {
						gb.setActionReq(false);
						gb.updateBoard(so,false,showValue);	
						// leave the previously shown values if it is HumanPlayer
					}
					else {
                        boolean DEBG=false; //false;true;
                        int N_EMPTY = 4;
                        if (DEBG) {
                        	actBest = getNextAction_DEBG(so,pa,p2,N_EMPTY);
                        } else {	
                            actBest = pa.getNextAction2(so, false, true);
                        }
                        
                        so.storeBestActionInfo(actBest, actBest.getVTable());
//                        if (so.getNumPlayers()==1) {
//                        	// show state and stored vtable *before* advance
//                            gb.updateBoard(so,false,showValue);
//                            showStoredV=false; // this is for 2nd updateBoard below
//                        }
                        so.advance(actBest);
						logManager.addLogEntry(actBest, so, logSessionid);
                        try {
                            Thread.sleep(currentSleepDuration);
                            // waiting time between agent-agent actions
                        } catch (Exception e) {
                            System.out.println("Thread 1");
                        }
                        gb.updateBoard(so,false,showValue);

                        // gather information for later printout to agents/gameName/csv/playStats.csv.
                        // This is mostly for diagnostics in game 2048, but not completely useless for other
                        // games as well.
                        if (so instanceof StateObserver2048) {
                        	StateObserver2048 so2048 = (StateObserver2048) so;
                        	nEmpty = so2048.getNumEmptyTiles();  
                        	cumEmpty += nEmpty;
                        	gameScore = so2048.getGameScore()*so2048.MAXSCORE;
                        } else {
                        	gameScore = so.getGameScore();
                        }
                        pstats = new PStats(1,moveNum,gameScore,(double)nEmpty,(double)cumEmpty);
            			psList.add(pstats);

				}
				
			} // if(gb.isActionReq())
			else
			{
				try
				{
					Thread.sleep(100);
					//
					// wait until an action in GameBoard gb occurs (see
					// ActionListener in InitBoard(), usually calling a function HGameMove),  
					// which will set gb.isActionReq() to true again. 
				}	
				catch (Exception e){System.out.println("Thread 3");}
			}
			so = gb.getStateObs();
			if (so.isGameOver()) {
				switch (so.getNumPlayers()) {
				case 1: 
					double gScore = so.getGameScore();
					if (so instanceof StateObserver2048) gScore *= StateObserver2048.MAXSCORE;
					MessageBox.show(m_TicFrame, "Game finished with score " +gScore, 
							"Game Over", JOptionPane.INFORMATION_MESSAGE );
					break;  // out of switch
				case 2: 
					int win=so.getGameWinner().toInt();
					Player=Types.PLAYER_PM[so.getPlayer()];
					switch(Player*win) {
					case (+1): 
						MessageBox.show(m_TicFrame, "X ("+agentX+") wins", 
								"Game Over", JOptionPane.INFORMATION_MESSAGE );
						break;  // out of inner switch
					case (-1):
						MessageBox.show(m_TicFrame, "O ("+agentO+") wins",
								"Game Over", JOptionPane.INFORMATION_MESSAGE );
						break;  // out of inner switch
					case ( 0):
						MessageBox.show(m_TicFrame, "Tie", 
								"Game Over", JOptionPane.INFORMATION_MESSAGE );
						break;  // out of inner switch
					} // switch(Player*win)
					break;   // out of switch
				default: 
					// TODO: implement s.th. for n-player games (n>2)
					break;  // out of switch
				}
				
				break;			// this is the final break out of while loop 	
			} // if isGameOver
			
		  }	// while(taskState == Task.PLAY) [will be left only by the last break above or when taskState changes]
		} catch(RuntimeException e) 
		{
			// a possible RuntimeException is raised when an agent for nondeterministic games
			// (ExpectimaxNAgent, MCTSExpectimax) is called with a (deterministic) 
			// StateObservation object 
			MessageBox.show(m_xab, 
					e.getMessage(), 
					"Error", JOptionPane.WARNING_MESSAGE);
			taskState = Task.IDLE;		
			setStatusMessage("Done.");
			return;			
		}

		// game over - leave the Task.PLAY task:
		PStats.printPlayStats(psList, paVector[0],this);
		logManager.endLoggingSession(logSessionid);
		taskState = Task.IDLE;		
		setStatusMessage("Done.");
	}

	/**
	 * For debugging during {@link #PlayGame()}: This function is only called if switch
	 * DEBG in source code of {@link #PlayGame()} is set to true and if the number of empty 
	 * tiles is below a threshold. - It calls MCTSAgentT p2.getNextAction2() repeatedly and prints the vtable
	 * results on console. It calls PlayAgent pa (usually MCAgent) repeatedly as well. 
	 * In addition, it prints the best i and the number of rollouts (iterations) in which the 
	 * game terminates.
	 * 
	 * Currently this function is only for StateObserver2048 so. Otherwise it returns the
	 * 'normal' pa.getNextAction2().
	 * 
	 * @param so
	 * @param pa
	 * @param p2
	 * @param N_EMPTY return the 'normal' pa.getNextAction2(), if number of empty cells is 
	 * 		greater or equal to N_EMPTY
	 * @return the chosen action from the last call of pa.getNextAction2()
	 */
	Types.ACTIONS_VT  getNextAction_DEBG(StateObservation so, PlayAgent pa, MCTSAgentT p2, 
			int N_EMPTY) {
		Types.ACTIONS_VT actBest=null;
        double MAXSCORE = 3932156;
        double[] vtable;
        int nEmpty;
        if (so instanceof StateObserver2048) {
        	nEmpty = ((StateObserver2048) so).getNumEmptyTiles();        	
        } else {
        	return pa.getNextAction2(so, false, true);
        }
        if (nEmpty>=N_EMPTY) 
        	return pa.getNextAction2(so, false, true);

    	for (int k=0; k<3; k++) {
            actBest = p2.getNextAction2(so, false, true);
            vtable = actBest.getVTable();
            System.out.print("p2 ["+p2.getName()+"]: ");
            double vbest = -Double.MAX_VALUE;
            int ibest = -1;
            for (int i=0;i<so.getNumAvailableActions();i++) {
            	System.out.print(String.format("%.3f",vtable[i]*MAXSCORE)+" ");
            	if (vtable[i]>vbest) {
            		vbest=vtable[i];
            		ibest=i;
            	}
            }
        	int nRolloutFinished = p2.getNRolloutFinished();
        	int nIterations = p2.getNIterations();
            System.out.println(";  Best = "+ibest+", Finished="+nRolloutFinished+"/"+nIterations);                        		
    	}
    	for (int k=0; k<2; k++) {
            actBest = pa.getNextAction2(so, false, true);
            vtable = actBest.getVTable();
            System.out.print("pa ["+pa.getName()+"]: "); 
            double vbest = -Double.MAX_VALUE;
            int ibest = -1;
            for (int i=0;i<so.getNumAvailableActions();i++) {
            	System.out.print(String.format("%.3f",vtable[i]*MAXSCORE)+" ");
            	if (vtable[i]>vbest) {
            		vbest=vtable[i];
            		ibest=i;
            	}
            }
            if (pa instanceof MCAgent) {
            	int nRolloutFinished = ((MCAgent) pa).getNRolloutFinished();
            	int nIterations = ((MCAgent) pa).getNIterations();
                System.out.println(";  Best = "+ibest+", Finished="+nRolloutFinished+"/"+nIterations);                        		
            } else {
                System.out.println(";  Best = "+ibest);              	
            }
    	}
    	return actBest;
    }


	public GameBoard getGameBoard() {
		return gb;
	}
	public LogManager getLogManager() {
		return logManager;
	}
	public int getLogSessionID() {
		return logSessionid;
	}
	
	public void setStatusMessage(String msg) { statusBar.setMessage(msg); }
	public StatusBar getStatusBar() { return statusBar; }

	
//	@Override
	public void setProgress(tools.Progress p) {
		this.progress = p;
	}

	public void enableButtons(boolean state) { m_xab.enableButtons(state); }

	abstract public String getGameName();
	
	/**
	 * Factory pattern method: make a new GameBoard 
	 * @return	the game board
	 */
	abstract public GameBoard makeGameBoard(); 

	/**
	 * Factory pattern method: make a new Evaluator
	 * @param pa	the agent to evaluate
	 * @param gb	the game board
	 * @param stopEval	the number of successful evaluations needed to reach the 
	 * 					evaluator goal (may be used during training to stop it 
	 * 					prematurely)
	 * @param mode		which evaluator mode. If -1, take the default evalMode
	 * 					{@link Evaluator#getDefaultEvalMode()}
	 * @param verbose	how verbose or silent the evaluator is
	 * @return			the evaluator
	 */
	abstract public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose);

	/**
	 * Factory pattern method: make a new {@link Feature} tailored to a specific game. <br>
	 * (We delegate this task to derived classes ArenaXYZ, since they usually 
	 * require a game-tailored FeatureXYZ.) <p>
	 * 
	 * If the derived class does not override {@link #makeFeatureClass(int)}, 
	 * the default behavior is to throw a {@link RuntimeException}.
	 * 
	 * @param featmode feature mode
	 * @return an object implementing {@link Feature}
	 */
	public Feature makeFeatureClass(int featmode) {
		throw new RuntimeException("No Feature class available for game "+this.getGameName()+" (needed for TDS)");
	}

	/**
	 * Factory pattern method: make a new {@link XNTupleFuncs} tailored to a specific game. <br>
	 * (We delegate this task to derived classes ArenaXYZ, since they usually 
	 * require a game-tailored XNTupleFuncsXYZ.) <p>
	 * 
	 * If the derived class does not override {@link #makeXNTupleFuncs()}, 
	 * the default behavior is to throw a {@link RuntimeException}.
	 * 
	 * @return an object implementing {@link XNTupleFuncs}
	 */
	public XNTupleFuncs makeXNTupleFuncs() {
		throw new RuntimeException("No XNTupleFuncs class available for game "+this.getGameName()+" (needed for TD-Ntuple)");
	}

	/**
	 * This method is called from {@link #run()} and it has to be overridden by 
	 * classes derived from {@link Arena} (e.g. {@link ArenaTrain}). <p>
	 * 
	 * It allows to add additional tasks to the task switch.
	 * May be an empty method if no tasks have to be added. <p>
	 * 
	 * This method will use member {@code taskState} from {@link Arena}. 
	 * It performs several actions appropriate for the derived class 
	 * and - importantly - changes taskState back to IDLE (when appropriate).
	 * 
	 * @see ArenaTrain
	 */
	abstract public void performArenaDerivedTasks();

}
