package TournamentSystem;

import TournamentSystem.Scoring.Elo.EloCalculator;
import TournamentSystem.Scoring.Glicko2.Glicko2RatingCalculator;
import TournamentSystem.Scoring.Glicko2.Glicko2RatingPeriodResults;
import TournamentSystem.jheatchart.HeatChart;
import TournamentSystem.tools.TSGameDataTransfer;
import TournamentSystem.tools.TSHeatmapDataTransfer;
import TournamentSystem.tools.TSScatterPlot;
import controllers.PlayAgent;
import controllers.RandomAgent;
import games.Arena;
import games.GameBoard;
import games.StateObservation;
import games.XArenaMenu;
import tools.Utils;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is the core of the GBG Tournament System.
 * This class manages the procedure, agents and data handling and is called from {@link TSSettingsGUI2}.
 *
 * @author Felix Barsnick, Cologne University of Applied Sciences, 2018
 */
public class TSAgentManager {
    private final String TAG = "[TSAgentManager] ";
    public TSResultStorage results;
    public JTextField gameNumJTF, numOfMovesJTF;
    public JCheckBox nRandomJCB, autoSaveAfterTSJCB;
    public JRadioButton singleRR, doubleRR;
    private final Glicko2RatingCalculator glicko2RatingSystem;
    private final Glicko2RatingPeriodResults glicko2Results;
    private int episodesPlayed;
    private boolean autoSaveAfterTS;
    private final int numPlayers;
    private StateObservation[] randomStartStates;
    private boolean playDoubleRoundRobin = true;
    private int userGameNumLimitDRR;

    public static final float factorWin = 1.0f;
    public static final float factorTie = 0.5f;
    public static final float factorLoss = 0.0f;

    public TSAgentManager(int gameNumberOfPlayers) {
        results = new TSResultStorage();
        results.mAgents = new ArrayList<>();

        glicko2RatingSystem = new Glicko2RatingCalculator(0.06, 0.5);
        glicko2Results = new Glicko2RatingPeriodResults();

        episodesPlayed = 0;
        autoSaveAfterTS = false;
        numPlayers = gameNumberOfPlayers;
    }

    /**
     * save the start date to the result storage for result visualization
     */
    public void setResultsStartDate() {
        results.startDate = "Tournament Start Date: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * set is the tournament should be double or single round robin
     * @param mode code to set tournament mode<br>
     *             0 = single round robin<br>
     *             1 = double round robin<br>
     *             2 = double round robin with custom number of matches
     * @param numGames number of matches to play in mode 2
     */
    public void setTournamentMode(int mode, int numGames) {
        switch (mode) {
            case 0 -> playDoubleRoundRobin = false;
            default -> playDoubleRoundRobin = true;
        }
        userGameNumLimitDRR = numGames;
    }

    /**
     * en/disable automatically saving the tournament results to disk after TS is done
     * @param b true:enable, false:disable (standard:disabled)
     */
    public void setAutoSaveAfterTS(boolean b) {
        autoSaveAfterTS = b;
    }

    /**
     * did the user enable auto save of results after TS is done
     * @return boolean if auto save enabled after TS
     */
    public boolean getAutoSaveAfterTS() {
        return autoSaveAfterTS;
    }

    /**
     * set number of episodes (rounds) to be played per match. 
     * A match is a pair of agents in the gamePlan 
     * @param num number of episodes per match
     */
    public void setNumberOfEpisodes(int num) {
        results.numberOfEpisodes = num;
    }

    /**
     * set number of random start moves for each episode
     * @param num number of random start moves
     */
    public void setNumberOfRandomStartMoves(int num) {
        results.numberOfRandomStartMoves = num;
    }

    /**
     * add a new agent to the tournament
     * @param name agent name
     * @param agent agent type
     * @param checkbox JCheckBox used in {@link TSSettingsGUI2}
     * @param hddAgent {@code true} if the agent was loaded from disk
     * @param playAgent instance of the hdd agent, {@code null} if its a standard agent
     */
    public void addAgent(String name, String agent, JCheckBox checkbox, boolean hddAgent, PlayAgent playAgent) {
        if (!results.lockedToCompete)
            results.mAgents.add(new TSAgent(name, agent, checkbox, hddAgent, playAgent, glicko2RatingSystem));
        else
            System.out.println(TAG+"ERROR :: manager is locked to compete, can not add new agent");
    }

    /**
     * number of agents already added to tournament
     * @return number of agents as int
     */
    public int getNumAgents() {
        return results.mAgents.size();
    }

    /**
     * @return number of players in the game (currently only 1 or 2)
     */
    public int getNumPlayers() {
        return numPlayers;
    }

    /**
     * enables all checkboxes and textfields in {@link TSSettingsGUI2}, to be used before and after tournament
     * @param state true enables gui elements of TS settings GUI
     */
    public void setSettingsGUIElementsEnabled(boolean state) {
        for (TSAgent tsa : results.mAgents)
            tsa.guiCheckBox.setEnabled(state);
        gameNumJTF.setEnabled(state);
        numOfMovesJTF.setEnabled(state);
        nRandomJCB.setEnabled(state);
        autoSaveAfterTSJCB.setEnabled(state);

        if (numPlayers>1) {
            singleRR.setEnabled(state);
            doubleRR.setEnabled(state);
        }
    }

    /**
     * get the number of agents selected via checkbox in {@link TSSettingsGUI2}
     * @return number of selected agents
     */
    public int getNumAgentsSelected() {
        int num = 0;
        for (TSAgent agent : results.mAgents)
        {
            if(agent.guiCheckBox.isSelected()) num++;
        }
        return num;
    }

    /**
     * get the number of agents loaded from disk selected via checkbox in {@link TSSettingsGUI2}
     * @return number of selected hdd agents
     */
    public int getNumDiskAgents() {
        int num = 0;
        for (TSAgent t : results.mAgents)
            if (t.isHddAgent())
                num++;
        return num;
    }

    /**
     * get the ID names of the selected agents in {@link TSSettingsGUI2}
     * @return string array with ID names "Agent 0", "Agent 1", ... of selected agents
     */
    public String[] getNamesAgentsSelected() {
        String[] selectedAgents = new String[getNumAgentsSelected()]; // just selected agents
//        int i = 0;
        int ID = 0;
        for (TSAgent agent : results.mAgents) {
            if (agent.guiCheckBox.isSelected()) {
                selectedAgents[ID] = "Agent #"+ID++;
            }
        }
        return selectedAgents;
    }

    /**
     * get the filenames of the selected agents in {@link TSSettingsGUI2}
     * @return string array with filenames of selected agents
     */
    public String[] getFileNamesAgentsSelected() {
        String[] selectedAgents = new String[getNumAgentsSelected()]; // just selected agents
        int i = 0;
        for (TSAgent agent : results.mAgents) {
            if (agent.guiCheckBox.isSelected()) {
                selectedAgents[i++] = agent.getName();
            }
        }
        return selectedAgents;
    }

    /**
     * set if all HDD agents in {@link TSSettingsGUI2} should be selected
     * @param selected selection state of hdd agent checkboxes
     */
    public void setAllHDDAgentsSelected(boolean selected) {
        for (TSAgent agent : results.mAgents) {
            if (agent.isHddAgent()) {
                agent.guiCheckBox.setSelected(selected);
            }
        }
    }

    /**
     * delete all HDD agents with a selected checkbox from tournament
     */
    public void deleteAllHDDAgentsSelected() {
        int numAgents = getNumDiskAgents();
        Iterator<TSAgent> i = results.mAgents.iterator();
        while (i.hasNext()) {
            TSAgent a = i.next();
            if (a.isHddAgent()) {
                if (a.guiCheckBox.isSelected()) {
                    a.guiCheckBox.setVisible(false);
                    System.out.println(TAG+"Deleted Agent "+a.getName());
                    i.remove();
                }
            }
        }
        System.out.println(TAG+"Number of Disk Agents was reduced from "+numAgents+" to "+getNumDiskAgents());
    }

    /**
     * get the gamePlan with all agent pairs playing against each other with their names in a string array
     * @return string array with the names of agents playing against each other
     */
    public String[][] getGamePlan(boolean doubleRoundRobin) {
        int[][] internalGamePlan = generateGamePlanInternal(doubleRoundRobin);
        String[][] gamePlan = new String[internalGamePlan.length][2]; // games to be played

        for (int i=0; i<internalGamePlan.length; i++) {
            gamePlan[i][0] = results.mAgents.get(internalGamePlan[i][0]).getName();
            if (numPlayers>1)
                gamePlan[i][1] = results.mAgents.get(internalGamePlan[i][1]).getName();
            else
                gamePlan[i][1] = "null";
        }
        return gamePlan;
    }

    /**
     * get the IDs of the agents selected. the ID represents the agents position in arraylist {@code TSResultStorage.mAgents}
     * @return arraylist positions of selected agents in {@code TSResultStorage.mAgents}
     */
    public int[] getIDAgentsSelected() {
        int[] selectedAgents = new int[getNumAgentsSelected()]; // just selected agents
        int k = 0;
        for (int i=0; i<results.mAgents.size(); i++) {
            if (results.mAgents.get(i).guiCheckBox.isSelected()) {
                selectedAgents[k++] = i;
            }
        }
        return selectedAgents;
    }

    /**
     * generates the gamePlan and playing pairs of agents identified by the ID (position in mAgent Arraylist)
     * @param doubleRoundRobin is the tournament mode double round robin?
     * @return gamePlan with agent IDs
     */
    private int[][] generateGamePlanInternal(boolean doubleRoundRobin) {
        int[] selectedAgents = getIDAgentsSelected();
        int[][] gamePlan = null;
        if (numPlayers == 2) {
            int tmpGame = 0;
            if (doubleRoundRobin) {
                gamePlan = new int[getNumAgentsSelected() * (getNumAgentsSelected() - 1)][2]; // games to be played
                for (int i = 0; i < getNumAgentsSelected(); i++) {
                    for (int j = 0; j < getNumAgentsSelected(); j++) {
                        if (i != j) { // avoid agent to play against itself
                            gamePlan[tmpGame][0] = selectedAgents[i];
                            gamePlan[tmpGame++][1] = selectedAgents[j];
                        }
                    }
                }
                if (userGameNumLimitDRR > 0) {
                    // ... remove games from gamePlan as set by user
                    final int numGamesToPlay = userGameNumLimitDRR;
                    final int numGamesToRemove = gamePlan.length - userGameNumLimitDRR;
                    final int numGamesMinimum = (getNumAgentsSelected() / 2) + 1;

                    // matches which have to be played to make sure that every agent plays once
                    // this saves the position of the matches in the original double round robin gamePlan
                    // if the number of players is uneven, the last agent plays against the first
                    int[] safeMatches = new int[numGamesMinimum];

                    for (int i=0; i<selectedAgents.length; i+=2) {
                        if (i+1 == selectedAgents.length) {
                            safeMatches[i/2] = getPosGamePlan(selectedAgents[i],selectedAgents[0], gamePlan); // position of game in gamePlan
                        } else {
                            safeMatches[i/2] = getPosGamePlan(selectedAgents[i],selectedAgents[i+1], gamePlan); // position of game in gamePlan
                        }
                    }

                    // this holds the gamePlan positions of matches to be deleted, randomly chosen
                    int[] matchesToDelete = getNRandomInts(0, gamePlan.length-1, numGamesToRemove, safeMatches);

                    int[][] newGamePlan = new int[numGamesToPlay][2];
                    int pos = 0;

                    for (int i=0; i<gamePlan.length; i++) {
                        boolean skip = false;

                        for (int j:matchesToDelete) {
                            if (j==i) {
                                skip = true; // check if game is to be deleted
                            }
                        }

                        if (!skip) { // if not then copy match to new gamePlan
                            newGamePlan[pos][0] = gamePlan[i][0];
                            newGamePlan[pos][1] = gamePlan[i][1];
                            pos++;
                        }
                    }

                    return newGamePlan;
                }
            } else { // single round robin
                gamePlan = new int[(getNumAgentsSelected() * (getNumAgentsSelected() - 1))/2][2]; // games to be played
                for (int i = 0; i < getNumAgentsSelected(); i++) {
                    for (int j = 0; j < getNumAgentsSelected(); j++) {
                        if (i != j) { // avoid agent to play against itself
                            if (j > i) { // avoid  double play for every pair
                                gamePlan[tmpGame][0] = selectedAgents[i];
                                gamePlan[tmpGame++][1] = selectedAgents[j];
                            }
                        }
                    }
                }
            }
        }
        if (numPlayers == 1) {
            gamePlan = new int[getNumAgentsSelected()][1]; // games to be played
            for (int i = 0; i < getNumAgentsSelected(); i++) {
                gamePlan[i][0] = selectedAgents[i];
            }
        }
        if (numPlayers>2)
            System.out.println(TAG+"ERROR :: GamePlan generation not supported for games >2 Players!!");
        return gamePlan;
    }

    /**
     * generate an array of unique random Integers in a specified range excluding specified Integers
     * @param low lower limit which is included
     * @param high upper limit which is still included
     * @param count how many random numbers are requested
     * @param safe which Integers must not be returned
     * @return array of random Integers fitting the given parameters
     */
    private int[] getNRandomInts(int low, int high, int count, int[] safe) {
        int[] randoms = new int[count];
        int pos = 0;

        while (pos<count) {
            boolean failed = false;
            int rnd = getRandomInt(low, high);

            for (int i:safe) {
                if (i == rnd) {
                    failed = true;
                }
            }

            for (int i=0; i<pos; i++) {
                if (randoms[i] == rnd) {
                    failed = true;
                }
            }

            if (!failed) {
                randoms[pos++] = rnd;
            }
        }

        return randoms;
    }

    /**
     * generate a random Integer between a lower and upper limit
     * @param low lower limit which is included
     * @param high upper limit which is still included
     * @return random Integers fitting the given parameters
     */
    private int getRandomInt(int low, int high) {
        return ThreadLocalRandom.current().nextInt(low, high + 1);
    }

    /**
     * print the gamePlan with agent names to the console
     */
    public void printGamePlan() {
        String[][] gamePlan = getGamePlan(playDoubleRoundRobin);
        System.out.println(TAG+"+ GamePlan Info: +");
        System.out.println(TAG+"Matches to play: "+gamePlan.length);
        System.out.println(TAG+"each Match is run "+results.numberOfEpisodes+" time(s)");
        for (String[] round : gamePlan)
            System.out.println(TAG+"["+round[0]+"] vs ["+round[1]+"]");
        System.out.println(TAG+"+ End Info +");
    }

    /**
     * get agent by name
     * @param name name of agent
     * @return PlayAgent instance
     */
    public TSAgent getAgent(String name) {
        for (TSAgent agnt : results.mAgents)
            if (agnt.getName().equals(name))
                return agnt;
        return null;
    }

    /**
     * return a specific agent by its tournament ID
     * @param id tournament ID
     * @return specified agent
     */
    public TSAgent getAgentByID(int id) {
        return results.mAgents.get(id);
    }

    /**
     * check is the tournament is locked to avoid changes of input data
     * @return boolean if tournament is locked
     */
    public boolean isLockedToCompete() {
        return results.lockedToCompete;
    }

    /**
     * lock the tournament system to prevent data from changes.
     * also the gamePlan is calculated and measurements are prepared.
     */
    public void lockToCompete(GameBoard gb) {
        if (results.numberOfEpisodes == -1) {
            System.out.println(TAG+"ERROR :: number of games was not set! using 1");
            results.numberOfEpisodes = 1;
        }
        results.gamePlan = generateGamePlanInternal(playDoubleRoundRobin);
        results.gameResult = new int[results.gamePlan.length][3]; // is initialized with all zeros by JDK (primitive datatyp)
        results.timeStorage = new TSTimeStorage[results.gamePlan.length][numPlayers];
        for (TSTimeStorage[] t : results.timeStorage) { // initialize all positions
            for (int p=0; p<numPlayers; p++)
                t[p] = new TSTimeStorage();
        }
        results.nextGame = 0;
        episodesPlayed = 0;
        results.resetAgentScores();
        results.lockedToCompete = true;

        randomStartStates = new StateObservation[results.numberOfEpisodes];
        for (int game=0; game<results.numberOfEpisodes; game++) {
            randomStartStates[game] = gb.getDefaultStartState(null);

            if (results.numberOfRandomStartMoves>0) {
                System.out.println(TAG+"Calculation Random Start Moves...");
                long rnd = getRandomInt(111111,9999999)*1234567890;
                //System.out.println("DEVDEV:: "+rnd);
                RandomAgent raX = new RandomAgent("Random Agent X", rnd);

                for (int i = 0; i < results.numberOfRandomStartMoves; i++) {
                    randomStartStates[game].advance(raX.getNextAction2(randomStartStates[game].partialState(), false, false, true), null);
                }

                //try { Thread.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); } // replaced by externally set random seed in new random agent constructor
            }
        }

        /*
        if (results.numberOfRandomStartMoves>0) {
            for (int i=0; i<randomStartStates.length; i++) {
                for (int j=0; j<randomStartStates.length; j++) {
                    if (i != j) {
                        if (randomStartStates[i].stringDescr().equals(randomStartStates[j].stringDescr())) {
                            System.out.println("states gleich! i: "+i);
                        }
                    }
                }
            }
        }
        */

        System.out.println(TAG+"Start States:");
        for (StateObservation s : randomStartStates)
            System.out.println(s);

    }

    /**
     * get the agent and measurement data for the next playing agents according to the gamePlan.
     * <p>
     * you also need to get {@link TSAgentManager#getNextCompetitionTimeStorage()} to save the time measurements
     * @return TSAgent instances of the next playing agents
     */
    public TSAgent[] getNextCompetitionTeam() {
        TSAgent[] out = null;
        if (numPlayers == 2) {
            out = new TSAgent[2];
            out[0] = results.mAgents.get(results.gamePlan[results.nextGame][0]);
            out[1] = results.mAgents.get(results.gamePlan[results.nextGame][1]);
        }
        if (numPlayers == 1) {
            out = new TSAgent[1];
            out[0] = results.mAgents.get(results.gamePlan[results.nextGame][0]);
        }
        results.tournamentDone = false;
        return out;
    }

    /**
     * get the time measurement data for the next playing agents according to the gamePlan
     * @return time storage instance for the next game
     */
    public TSTimeStorage[] getNextCompetitionTimeStorage() {
        return results.timeStorage[results.nextGame];
    }

    /**
     * after two agents played their match, use this method to save the game result.
     * this also prepares the time storage for the next game and updates the gamePlan<p>
     * 0 : agent 1 wins<p>
     * 1 : tie<p>
     * 2 : agent 2 wins
     * @param type game result code
     */
    public void enterGameResultWinner(int type) {
        if (!results.lockedToCompete) {
            System.out.println(TAG+"ERROR :: manager ist not locked, cannot enter result. run lockToCompete() first");
            return;
        }
        if (type<0 || type>2) {
            System.out.println(TAG + "ERROR :: enterGameResultWinner(int type) wrong value for type [0;2] = " + type);
            return;
        }
        else {
            results.gameResult[results.nextGame][type] = results.gameResult[results.nextGame][type] + 1;

            TSAgent[] teamPlayed = getNextCompetitionTeam(); // save individual win or loss to the tsagent objects in magents list
            if (type == 0){
                teamPlayed[0].addWonGame();
                teamPlayed[1].addLostGame();
                EloCalculator.setNewElos(teamPlayed[0].mEloPlayerFIDE, +1, teamPlayed[1].mEloPlayerFIDE);
                EloCalculator.setNewElos(teamPlayed[0].mEloPlayerUSCF, +1, teamPlayed[1].mEloPlayerUSCF);
                glicko2Results.addResult(teamPlayed[0].mGlicko2Rating, teamPlayed[1].mGlicko2Rating);
            }
            if (type == 1){
                teamPlayed[0].addTieGame();
                teamPlayed[1].addTieGame();
                EloCalculator.setNewElos(teamPlayed[0].mEloPlayerFIDE, 0, teamPlayed[1].mEloPlayerFIDE);
                EloCalculator.setNewElos(teamPlayed[0].mEloPlayerUSCF, 0, teamPlayed[1].mEloPlayerUSCF);
                glicko2Results.addDraw(teamPlayed[0].mGlicko2Rating, teamPlayed[1].mGlicko2Rating);
            }
            if (type == 2){
                teamPlayed[0].addLostGame();
                teamPlayed[1].addWonGame();
                EloCalculator.setNewElos(teamPlayed[0].mEloPlayerFIDE, -1, teamPlayed[1].mEloPlayerFIDE);
                EloCalculator.setNewElos(teamPlayed[0].mEloPlayerUSCF, -1, teamPlayed[1].mEloPlayerUSCF);
                glicko2Results.addResult(teamPlayed[1].mGlicko2Rating, teamPlayed[0].mGlicko2Rating);
            }

            results.timeStorage[results.nextGame][0].roundFinished();
            results.timeStorage[results.nextGame][1].roundFinished();
        }

        if (results.gameResult[results.nextGame][0]+results.gameResult[results.nextGame][1]+results.gameResult[results.nextGame][2] == results.numberOfEpisodes)
            results.nextGame++;

        episodesPlayed++;

        results.tournamentDone = false;
    }

    /**
     * returns if a next game is available according ot the gamePlan
     * @return boolean if a next game is available
     */
    public boolean hasNextGame() {
        if (results.nextGame == results.gamePlan.length) {
            results.tournamentDone = true;
            return false;
        }
        else {
            if (numPlayers == 1){
                if (getNextCompetitionTeam()[0].getSinglePlayScores().length == results.numberOfEpisodes) {
                    results.nextGame++;
                    if (results.nextGame == results.gamePlan.length) {
                        results.tournamentDone = true;
                        return false;
                    }
                }
            }

            return true;
        }
    }

    /**
     * print a summary of gameresults and basic statistics to the console
     */
    public void printGameResults() {
        if (results.gamePlan.length != results.gameResult.length) {
            System.out.println(TAG+"printGameResults() failed - gamePlan.length != gameResult.length");
            return;
        }
        System.out.println(TAG+"Info on individual games:");
        for (int i=0; i<results.gamePlan.length; i++) {
            System.out.print(TAG);
            System.out.print("Team: ");
            //System.out.print("["+gamePlan[i][0]+"] vs ["+gamePlan[i][1]+"] || ");
            System.out.print("["+results.mAgents.get(results.gamePlan[i][0]).getName()+"] vs ["+results.mAgents.get(results.gamePlan[i][1]).getName()+"] || ");
            System.out.print("Res.: Win1: "+results.gameResult[i][0]+" Tie: "+results.gameResult[i][1]+" Win2: "+results.gameResult[i][2]+" || ");
            System.out.print("Agt.1 average Time MS: "+results.timeStorage[i][0].getAverageTimeForGameMS()+" ");
            System.out.print("Agt.2 average Time MS: "+results.timeStorage[i][1].getAverageTimeForGameMS()+" ");
            System.out.print("");
            System.out.println();
        }
        System.out.println(TAG+"Info on individual Agents:");
        int[] selectedAgents = getIDAgentsSelected();
        for (int id : selectedAgents) {
            TSAgent a = results.mAgents.get(id);
            System.out.print(TAG);
            System.out.print("AgentName: "+a.getName()+" ");
            System.out.print("GamesWon: "+a.getCountWonGames()+" GamesTie: "+a.getCountTieGames()+" GamesLost: "+a.getCountLostGames()+" | ");
            System.out.print("AgentScore: "+a.getAgentScore()+" | ");
            System.out.print("FIDE-ELO: "+a.mEloPlayerFIDE.getEloRating()+" USCF-ELO: "+a.mEloPlayerUSCF.getEloRating()+" ");
            NumberFormat formatter1 = new DecimalFormat("#0.0");
            System.out.print("Glicko2: "+formatter1.format(a.mGlicko2Rating.getRating())+" ");
            NumberFormat formatter2 = new DecimalFormat("#0.000");
            System.out.print("radev.: "+formatter2.format(a.mGlicko2Rating.getRatingDeviation())+" ");
            System.out.print("vol.: "+formatter2.format(a.mGlicko2Rating.getVolatility())+" ");
            System.out.println();
        }
    }

    /**
     * unlock the tournament system to enable data changes
     */
    public void unlockAfterComp() {
        results.lockedToCompete = false;
        /*
        gamePlan = null;
        gameResult = null;
        timeStorage = null;
        nextGame = 0;
        */
    }

    /**
     * returns if a tournament is running
     * @return boolean if tournament is running
     */
    public boolean isTournamentDone() {
        return results.tournamentDone;
    }

    /*  ++++++++++++++++++
     *  +++ STATISTICS +++
     *  ++++++++++++++++++
     */

    /**
     * call this method after the tournament is finished to process the measurement data 
     * and to generate the elements in the results window {@link TSResultWindow}.
     */
    public void makeStats() {
        if (!results.tournamentDone) {
            System.out.println(TAG+"ERROR :: Stats Window cannot be opened, tournament data not available");
            return;
        }

        //NumberFormat numberFormat0 = new DecimalFormat("#0.0");
        NumberFormat numberFormat00 = new DecimalFormat("#0.00");
        NumberFormat numberFormat000 = new DecimalFormat("#0.000");
        //NumberFormat numberFormat0000 = new DecimalFormat("#0.0000");
        NumberFormat numberFormat00000 = new DecimalFormat("#0.00000");

        String startDate = results.startDate+" | Matches: "+results.gamePlan.length
                +" | Episodes per Match: "+results.numberOfEpisodes
                +" | Random Start Moves: "+results.numberOfRandomStartMoves/*
                +" | Duration: "+numberFormat000.format(results.getTSTotalPlayTimeS())+"[s] = "
                +numberFormat00.format(results.getTSTotalPlayTimeS()/60.0)+"[min]";*/
                +" | Duration: "+numberFormat000.format(results.durationTSMS/1000.0)+" sec = "
                +numberFormat00.format(results.durationTSMS/60000.0)+" min";
        boolean singlePlayerGame = false;
        if (numPlayers==1)
            singlePlayerGame = true;

        TSResultWindow tsResultWindow = new TSResultWindow(startDate, singlePlayerGame);
        TSHeatmapDataTransfer mTSHeatmapDataTransfer = new TSHeatmapDataTransfer();
        double[][] rowDataHM = null;

        if (numPlayers>1) {
            // http://www.codejava.net/java-se/swing/a-simple-jtable-example-for-display
            /*
             * Table | WTL and Score
             */
            // headers for the table
            String[] agents = getFileNamesAgentsSelected();    // /WK/ file names as column heads
            String[] columnNames1 = new String[agents.length + 1]; //{ "Y vs X"//, "Agent#1", "Agent#2", "Agent#3" };
            columnNames1[0] = ""; //"Y vs X";
            System.arraycopy(agents, 0, columnNames1, 1, agents.length);

            final String empty = "null";
            final String noGame = "NoGame";
            Object[][] rowData1 = new Object[getNumAgentsSelected()][getNumAgentsSelected() + 1];
            Object[][] rowData3 = new Object[getNumAgentsSelected()][getNumAgentsSelected() + 1];
            rowDataHM = new double[getNumAgentsSelected()][getNumAgentsSelected()];
            for (int i = 0; i < getNumAgentsSelected(); i++) {
                rowData1[i][0] = getFileNamesAgentsSelected()[i];   // /WK/ file names as row heads
                rowData3[i][0] = getFileNamesAgentsSelected()[i];   // /WK/ file names as row heads
                for (int j = 0; j < getNumAgentsSelected(); j++) {
                    if (i == j) { // main axis of agents playing against itself
                        rowData1[i][j + 1] = empty;
                        rowData3[i][j + 1] = empty;
                        rowDataHM[i][j] = HeatChart.COLOR_DIAGONALE; // diagonale
                    } else {
                        int gameNum = getPosGamePlan(getIDAgentsSelected()[i],getIDAgentsSelected()[j], results.gamePlan); // position of game in gamePlan
                        if (gameNum == -1) { // game not available
                            rowData1[i][j + 1] = noGame;
                            rowData3[i][j + 1] = noGame;
                            rowDataHM[i][j] = HeatChart.COLOR_GAMENOTPLAYED; // not played
                        } else { // game was played
                            //int gameNum = getPosFullGamePlan(getIDAgentsSelected()[i], getIDAgentsSelected()[j]);
                            rowData1[i][j + 1] = "W:" + results.gameResult[gameNum][0] + " | T:" + results.gameResult[gameNum][1] + " | L:" + results.gameResult[gameNum][2];
                            double score = 0;
                            score += results.gameResult[gameNum][0] * factorWin;
                            score += results.gameResult[gameNum][1] * factorTie;
                            score += results.gameResult[gameNum][2] * factorLoss;
                            rowData3[i][j + 1] = "" + score;
                            rowDataHM[i][j] = score;
                        }
                    }
                }
            }

            //create table with data
            //JTable tableMatrixWTL = new JTable(rowData1, columnNames1);
            DefaultTableModel defTableMatrixWTL = new DefaultTableModel(rowData1, columnNames1);
            tsResultWindow.setTableMatrixWTL(defTableMatrixWTL);
            //JTable tableMatrixSCR = new JTable(rowData3, columnNames1);
            DefaultTableModel defTableMatrixSCR = new DefaultTableModel(rowData3, columnNames1);
            tsResultWindow.setTableMatrixSCR(defTableMatrixSCR);

            /*
             * Score Heatmap
             */
            // create Score HeatMap
            HeatChart map = new HeatChart(rowDataHM, 0, HeatChart.max(rowDataHM), true);
            Object[] agentNames = getNamesAgentsSelected();
            map.setXValues(agentNames);
            map.setYValues(agentNames);
            map.setCellSize(new Dimension(25, 25));
            //map.setTitleFont();
            Image hm = map.getChartImage();
            //tsResultWindow.setHeatMap(new ImageIcon(hm));
            mTSHeatmapDataTransfer.scoreHeatmap = new ImageIcon(hm);

            /*
             * Score Heatmap Analysis
             */
            // copy score heatmap data into seperate array
            double[][] agentScoreHMData = new double[rowDataHM.length][rowDataHM[0].length];
            for (int i = 0; i < rowDataHM.length; i++) {
                System.arraycopy(rowDataHM[i], 0, agentScoreHMData[i], 0, rowDataHM[i].length);
            }

            /*
            System.out.println("\nDEV ## Score Heatmap Analysis");
            System.out.println("agentScoreHMData data:");
            for (double[] d:agentScoreHMData)
                System.out.println(Arrays.toString(d));
                */

            double minimum = HeatChart.min(agentScoreHMData, true); // lowest value in dataset (score)
            double maximum = HeatChart.max(agentScoreHMData); // highest value in dataset (score)
            //System.out.println("data min "+minimum+" max: "+maximum);
            //System.out.println("data minNorm. "+normalize(minimum, minimum, maximum)+" max: "+normalize(maximum, minimum, maximum));

            // convert score values to normalized values between 0 and 1
            for (int i=0; i<agentScoreHMData.length; i++) {
                for (int j=0; j<agentScoreHMData[0].length; j++) {
                    if (agentScoreHMData[i][j]>=0) { // leave out -1 values, which mark the main diagonale
                        agentScoreHMData[i][j] = normalize(agentScoreHMData[i][j], minimum, maximum);
                    }
                }
            }

            double[][] dataHMAnalysis1 = new double[agentScoreHMData.length][agentScoreHMData[0].length];
            double[][] dataHMAnalysis2 = new double[agentScoreHMData.length][agentScoreHMData[0].length];
            double[][] dataHMAnalysis3 = new double[agentScoreHMData.length][agentScoreHMData[0].length];
            for (int i=0; i<agentScoreHMData.length; i++) {
                for (int j=0; j<agentScoreHMData[0].length; j++) {
                    if (i==j) {
                        dataHMAnalysis1[i][j] = HeatChart.COLOR_DIAGONALE;
                        dataHMAnalysis2[i][j] = HeatChart.COLOR_DIAGONALE;
                        dataHMAnalysis3[i][j] = HeatChart.COLOR_DIAGONALE;
                    } else {
                        boolean both = true;

                        // advanced analysis 1 - is Wab = Wba
                        if (agentScoreHMData[i][j] == agentScoreHMData[j][i]) {
                            dataHMAnalysis1[i][j] = HeatChart.COLOR_ANALYSISPOS;
                            //dataHMAnalysis1[i][j] = Math.abs(agentScoreHMData[i][j] - agentScoreHMData[j][i]);
                        } else {
                            //dataHMAnalysis1[i][j] = HeatChart.COLOR_ANALYSISNEG;
                            dataHMAnalysis1[i][j] = Math.abs(agentScoreHMData[i][j] - agentScoreHMData[j][i]);
                            both = false;
                        }

                        // advanced analysis 2 - is Wab = 1-Wba
                        if (agentScoreHMData[i][j] == 1-agentScoreHMData[j][i]) {
                            dataHMAnalysis2[i][j] = HeatChart.COLOR_ANALYSISPOS;
                            //dataHMAnalysis2[i][j] = Math.abs(agentScoreHMData[i][j] - (1-agentScoreHMData[j][i]));
                        } else {
                            //dataHMAnalysis2[i][j] = HeatChart.COLOR_ANALYSISNEG;
                            dataHMAnalysis2[i][j] = Math.abs(agentScoreHMData[i][j] - (1-agentScoreHMData[j][i]));
                            both = false;
                        }

                        // advanced analysis 3 - are both previous test true
                        if (both) {
                            dataHMAnalysis3[i][j] = HeatChart.COLOR_ANALYSISPOS;
                        } else {
                            dataHMAnalysis3[i][j] = HeatChart.COLOR_ANALYSISNEG;
                        }
                    }
                }
            }

            HeatChart mapA1 = new HeatChart(dataHMAnalysis1, 0, 1, true);
            mapA1.setXValues(getNamesAgentsSelected());
            mapA1.setYValues(getNamesAgentsSelected());
            mapA1.setCellSize(new Dimension(25, 25));
            Image hmA1 = mapA1.getChartImage();
            mTSHeatmapDataTransfer.scoreHeatmapA1 = new ImageIcon(hmA1);

            HeatChart mapA2 = new HeatChart(dataHMAnalysis2, 0, 1, true);
            mapA2.setXValues(getNamesAgentsSelected());
            mapA2.setYValues(getNamesAgentsSelected());
            mapA2.setCellSize(new Dimension(25, 25));
            Image hmA2 = mapA2.getChartImage();
            mTSHeatmapDataTransfer.scoreHeatmapA2 = new ImageIcon(hmA2);

            HeatChart mapA3 = new HeatChart(dataHMAnalysis3, 0, 1, true);
            mapA3.setXValues(getNamesAgentsSelected());
            mapA3.setYValues(getNamesAgentsSelected());
            mapA3.setCellSize(new Dimension(25, 25));
            Image hmA3 = mapA3.getChartImage();
            mTSHeatmapDataTransfer.scoreHeatmapA3 = new ImageIcon(hmA3);

            //System.out.println("\nDEV ## Score Heatmap Analysis DONE");
            //tsResultWindow.setHeatMap(mTSHeatmapDataTransfer); // moved below after creation of sorted heatmap
        }

        /*
         * Table | Agent Score
         */
        String[] columnNames4 = {
                "Rank",
                "Agent",
                "Filename",
                "Games Won",
                "Games Tie",
                "Games Lost",
                "WTL Score",
                "FIDE Elo",
                "USCF Elo",
                "Glicko2",
                "WonGameRatio"
        };
        if (singlePlayerGame) {
            columnNames4 = new String[]{
                    "Rank",
                    "Agent",
                    "Filename",
                    "highest Score",
                    "lowest Score",
                    "average Score",
                    "median Score"
            };
        }
        Object[][] rowData4 = new Object[getNumAgentsSelected()][columnNames4.length];
        TSHMDataStorage[] rankAgents = new TSHMDataStorage[getNumAgentsSelected()]; 
        // TSHMDataStorage is needed to pack agent and heatmap data into one to share sorting
        int[] selectedAgents = getIDAgentsSelected();
        for (int i=0; i<selectedAgents.length; i++) {
            rankAgents[i] = new TSHMDataStorage(); // must init to avoid NullPointerEX
            rankAgents[i].agent = results.mAgents.get(selectedAgents[i]);
            rankAgents[i].name = getNamesAgentsSelected()[i];
            if (numPlayers>1)
                rankAgents[i].hmScoreValues = rowDataHM[i];
            else
                rankAgents[i].hmScoreValues = null; // fuer zweite HM, wird nur bei spielen Player>1 gerendert
            //System.out.println("DEVDEV "+rankAgents[i].agent.getName()+" -- "+Arrays.toString(rowDataHM[i]));
        }

        // sort rankAgent array by agent WTL score
        /*
        Arrays.sort(rankAgents, new Comparator<TSAgent>() {
            @Override
            public int compare(final TSAgent entry1, final TSAgent entry2) {
                if (entry1.getAgentScore()>entry2.getAgentScore())
                    return -1;
                if (entry1.getAgentScore()<entry2.getAgentScore())
                    return +1;
                return 0;
            }
        });
        */
        if (!singlePlayerGame) { // multiplayer game
            Arrays.sort(rankAgents, (entry1, entry2) -> { // same as above
                return Float.compare(entry2.agent.getAgentScore(), entry1.agent.getAgentScore());
            });
        } else { // singleplayer game
            Arrays.sort(rankAgents, (entry1, entry2) -> { // same as above
                return Double.compare(entry2.agent.getMedianSinglePlayerScore(), entry1.agent.getMedianSinglePlayerScore());
            });
        }

        glicko2RatingSystem.updateRatings(glicko2Results); // update glicko2 ratings once after TS

        // put data into table
        for (int i=0; i<rowData4.length; i++) {
            // "Rank"
            rowData4[i][0] = ""+(i+1);
            // "Agent"
            rowData4[i][1] = rankAgents[i].name;
            // "Filename"
            rowData4[i][2] = rankAgents[i].agent.getName();
            if (!singlePlayerGame) {
                // "Games Won"
                rowData4[i][3] = rankAgents[i].agent.getCountWonGames();
                // "Games Tie"
                rowData4[i][4] = rankAgents[i].agent.getCountTieGames();
                // "Games Lost"
                rowData4[i][5] = rankAgents[i].agent.getCountLostGames();
                // "WTL Score"
                rowData4[i][6] = rankAgents[i].agent.getAgentScore();
                // "FIDE Elo"
                rowData4[i][7] = rankAgents[i].agent.mEloPlayerFIDE.getEloRating();
                // "USCF Elo"
                rowData4[i][8] = rankAgents[i].agent.mEloPlayerUSCF.getEloRating();
                // "Glicko2"
                //rowData4[i][8] = rankAgents[i].mGlicko2Rating.getRating();
                rowData4[i][9] = numberFormat00.format(rankAgents[i].agent.mGlicko2Rating.getRating());
                // "WonGameRatio"
                //float w = rankAgents[i].agent.getCountWonGames();
                float s = rankAgents[i].agent.getAgentScore(); // score agent
                float a = rankAgents[i].agent.getCountAllGames()*factorWin; // max possible score with every episode won
                float f = s / a;
                rowData4[i][10] = numberFormat00.format(f * 100) + "%";
            }
            else {
                // "highest Score"
                rowData4[i][3] = rankAgents[i].agent.getMaxSinglePlayerScore();
                // "lowest Score",
                rowData4[i][4] = rankAgents[i].agent.getMinSinglePlayerScore();
                // "average Score"
                rowData4[i][5] = rankAgents[i].agent.getAverageSinglePlayScore();
                // "median Score"
                rowData4[i][6] = rankAgents[i].agent.getMedianSinglePlayerScore();
            }
        }

        //create table with data
        //JTable tableAgentScore = new JTable(rowData4, columnNames4);
        DefaultTableModel defTableAgentScore = new DefaultTableModel(rowData4, columnNames4);
        tsResultWindow.setTableAgentScore(defTableAgentScore);

        if (numPlayers>1) {
            /*
             * HeatMap2 | Agent Scores sorted by AgentScores
             */
            // create Score HeatMap
            double[][] dataHM2 = new double[rowDataHM.length][rowDataHM[0].length];
            Object[] agentNamesX = getNamesAgentsSelected();
            Object[] agentNamesY = new Object[rankAgents.length];
            // agents sorted vertically
            for (int i = 0; i < rankAgents.length; i++) {
                //agentNamesY[i] = rankAgents[i].agent.getName();
                agentNamesY[i] = rankAgents[i].name;
                dataHM2[i] = rankAgents[i].hmScoreValues;
            }
            // sort agents horizontally
            // step through old array and extract data
            TSHM2DataStorage[] dataHM2UnSorted = new TSHM2DataStorage[dataHM2.length];
            for (int x=0; x<dataHM2[0].length; x++) {
                double[] column = new double[dataHM2.length];
                for (int y=0; y<dataHM2.length; y++) {
                   column[y] = dataHM2[y][x];
                }
                //System.out.println(Arrays.toString(column));
                dataHM2UnSorted[x] = new TSHM2DataStorage((String) agentNamesX[x], column);
                //System.out.println(Arrays.toString(column)+" sum: "+dataHM2UnSorted[x].columnSum+" agentX: "+dataHM2UnSorted[x].agentName);
            }
            //sort by names
            TSHM2DataStorage[] dataHM2Sorted = new TSHM2DataStorage[dataHM2UnSorted.length];
            for (int i=0; i<agentNamesY.length; i++) {
                for (TSHM2DataStorage tshm2DataStorage : dataHM2UnSorted) {
                    if (agentNamesY[i].equals(tshm2DataStorage.agentName)) {
                        dataHM2Sorted[i] = tshm2DataStorage;
                        break;
                    }
                }
            }
            for (int x=0; x<dataHM2[0].length; x++) {
                for (int y=0; y<dataHM2.length; y++) {
                    dataHM2[y][x] = dataHM2Sorted[x].verticalScoreValues[y];
                }
                agentNamesX[x] = dataHM2Sorted[x].agentName;
            }

            HeatChart map2 = new HeatChart(dataHM2, 0, HeatChart.max(dataHM2), true);
            map2.setXValues(agentNamesX);
            map2.setYValues(agentNamesY);
            map2.setCellSize(new Dimension(25, 25));
            Image hm2 = map2.getChartImage();
            //tsResultWindow.setHeatMapSorted(new ImageIcon(hm2));
            mTSHeatmapDataTransfer.scoreHeatmapSorted = new ImageIcon(hm2);
            tsResultWindow.setHeatMap(mTSHeatmapDataTransfer);
        }

        /*
         * Scatterplot | AgentScore vs Time
         */
        // Create dataset
        XYSeriesCollection dataset = TSScatterPlot.createDataset(this, this.results);
        // Create chart
        boolean hasLogarithmicX = tsResultWindow.isLogarithmicXAxisSelected();
        JFreeChart scatterPlot = TSScatterPlot.createScatterPlot(dataset, hasLogarithmicX, numPlayers);
        tsResultWindow.setScatterPlotASvT(scatterPlot);

        /*
         * Table | Times (detailed and simplified)
         */
        // headers for detailed time table
        String[] columnNamesTimeDetail = {
                "Match",
                "Filename",
                "Agent Type",
                "Fastest Move",
                "Slowest Move",
                "Average Move",
                "Median Move",
                "Average Episode",
                "Median Episode",
                "Total Time",
                "Average Move Count",
                "Median Move Count"
        };

        // detailed time table
        final int numAgentsPerMatch = numPlayers;
        Object[][] rowDataTimeDetail = new Object[results.gameResult.length*numAgentsPerMatch][columnNamesTimeDetail.length];
        TSSimpleTimeTableHelper[] simpleTimes = new TSSimpleTimeTableHelper[getNumAgentsSelected()]; // collect times for simplified view
        for (int i=0; i<simpleTimes.length; i++) {
            simpleTimes[i] = new TSSimpleTimeTableHelper(getIDAgentsSelected()[i]); // init with IDs of selected agents
        }
        int pos = 0;
        double z;
        for (int i=0; i<results.gameResult.length; i++) {
            for (int j=0; j<numAgentsPerMatch; j++) {
                TSSimpleTimeTableHelper timeHelper = null;
                for (TSSimpleTimeTableHelper tss : simpleTimes) { // find matching timehelper for agent in gamePlan
                    if (tss.agentID == results.gamePlan[i][j]) {
                        timeHelper = tss;
                    }
                }

                // "Match":
                rowDataTimeDetail[pos][0] = ""+(i+1);
                // "Agent Name"
                rowDataTimeDetail[pos][1] = results.mAgents.get(results.gamePlan[i][j]).getName();
                // "Agent Type"
                rowDataTimeDetail[pos][2] = results.mAgents.get(results.gamePlan[i][j]).getAgentType();
                // "Fastest Move":
                z = results.timeStorage[i][j].getMinTimeForGameMS();
                rowDataTimeDetail[pos][3] = numberFormat00000.format(z);
                if (z>0) // just store value if >0 := proper measurement is available
                    Objects.requireNonNull(timeHelper).minTimeForGameMS.add(z);
                // "Slowest Move":
                z = results.timeStorage[i][j].getMaxTimeForGameMS();
                rowDataTimeDetail[pos][4] = numberFormat00000.format(z);
                if (z>0) Objects.requireNonNull(timeHelper).maxTimeForGameMS.add(z);
                // "Average Move":
                z = results.timeStorage[i][j].getAverageTimeForGameMS();
                rowDataTimeDetail[pos][5] = numberFormat00000.format(z);
                if (z>0) Objects.requireNonNull(timeHelper).averageTimeForGameMS.add(z);
                // "Median Move":
                z = results.timeStorage[i][j].getMedianTimeForGameMS();
                rowDataTimeDetail[pos][6] = numberFormat00000.format(z);
                if (z>0) Objects.requireNonNull(timeHelper).medianTimeForGameMS.add(z);
                // "Average Episode":
                z = results.timeStorage[i][j].getAverageRoundTimeMS();
                rowDataTimeDetail[pos][7] = numberFormat00000.format(z);
                if (z>0) Objects.requireNonNull(timeHelper).averageRoundTimeMS.add(z);
                // "Median Episode":
                z = results.timeStorage[i][j].getMedianRoundTimeMS();
                rowDataTimeDetail[pos][8] = numberFormat00000.format(z);
                if (z>0) Objects.requireNonNull(timeHelper).medianRoundTimeMS.add(z);
                // "Total Time":
                z = results.timeStorage[i][j].getTotalTimeMS();
                rowDataTimeDetail[pos][9] = numberFormat00000.format(z);
                Objects.requireNonNull(timeHelper).totalPlayTimeMS += z;
                // "Average Move Count":
                rowDataTimeDetail[pos][10] = results.timeStorage[i][j].getAverageCountOfMovesPerEpisode();
                // "Median Move Count":
                rowDataTimeDetail[pos][11] = results.timeStorage[i][j].getMedianCountOfMovesPerEpisode();
                pos++;
            }
        } // for (i)

        // headers for simplified time table
        String[] columnNamesTimeSimple = {
                "Agent",
                "Filename",
                "Agent Type",
                "Fastest Move",
                "Slowest Move",
                "Average Move",
                "Median Move",
                "Average Episode",
                "Median Episode",
                "Total Time"
        };

        // simplified time table
        Object[][] rowDataTimeSimple = new Object[getNumAgentsSelected()][columnNamesTimeSimple.length];
        int[] selectedAgents3 = getIDAgentsSelected();
        for (int i=0; i<selectedAgents3.length; i++) {
            TSAgent tmp = results.mAgents.get(selectedAgents3[i]);
            String name = getNamesAgentsSelected()[i];
            TSSimpleTimeTableHelper timeHelper = simpleTimes[i];

            // "Agent"
            rowDataTimeSimple[i][0] = name;
            // "Filename"
            rowDataTimeSimple[i][1] = tmp.getName();
            // "Agent Type"
            rowDataTimeSimple[i][2] = tmp.getAgentType();
            // "Fastest Move":
            rowDataTimeSimple[i][3] = numberFormat00000.format(timeHelper.getMinTimeForGameMS());
            // "Slowest Move":
            rowDataTimeSimple[i][4] = numberFormat00000.format(timeHelper.getMaxTimeForGameMS());
            // "Average Move":
            rowDataTimeSimple[i][5] = numberFormat00000.format(Utils.calculateAverage(timeHelper.averageTimeForGameMS));
            // "Median Move":
            rowDataTimeSimple[i][6] = numberFormat00000.format(Utils.calculateMedian(timeHelper.medianTimeForGameMS));
            // "Average Episode":
            rowDataTimeSimple[i][7] = numberFormat00000.format(Utils.calculateAverage(timeHelper.averageRoundTimeMS));
            // "Median Episode":
            rowDataTimeSimple[i][8] = numberFormat00000.format(Utils.calculateMedian(timeHelper.medianRoundTimeMS));
            // "Total Time":
            rowDataTimeSimple[i][9] = numberFormat000.format(timeHelper.totalPlayTimeMS);
            
            // just debug code to check the new calculateMedian method
//          double s1 = calculateMedian(timeHelper.medianTimeForGameMS);
//          double s2 = calculateMedianOLD(timeHelper.medianTimeForGameMS);
//          assert (s1 == s2) : "Discrepancy in median calculation";
        }

        //create table with data
        //JTable tableTimeDetail = new JTable(rowDataTimeDetail, columnNamesTimeDetail);
        DefaultTableModel defTableTimeDetail = new DefaultTableModel(rowDataTimeDetail, columnNamesTimeDetail);
        DefaultTableModel defTableTimeSimple = new DefaultTableModel(rowDataTimeSimple, columnNamesTimeSimple);
        tsResultWindow.setTableTimeDetail(defTableTimeDetail, defTableTimeSimple);

    }

    /**
     * load a saved {@link TSResultStorage} from disk, load it and open the result window {@link TSResultWindow}.
     * Gets called in {@link XArenaMenu#generateTournamentMenu()} via the top menu.
     * @param tsr instance of {@link TSResultStorage} loaded from disk. Is NULL if the FileChooser is closed
     *            without a file chosen
     */
    public void loadAndShowTSFromDisk(TSResultStorage tsr) {
        if (tsr == null)
            return;
        // load ts results from disk
        results = tsr;
        // visualize
        makeStats();
    }

    /**
     * returns the current tournament progress
     * @return [ episodes (rounds) played , total number of rounds ]
     */
    public int[] getTSProgress() {
        return new int[]{episodesPlayed, results.gamePlan.length*results.numberOfEpisodes };
    }

    /**
     * get position of selected agent's game in the gamePlan. returns -1 if not found!
     * @param agentAID ID of first agent
     * @param agentBID ID of second agent
     * @param gamePlan the tournament gamePlan
     * @return positiom of of game in gamePlan or -1 if not found
     */
    private int getPosGamePlan(int agentAID, int agentBID, int[][] gamePlan) {
        for (int i=0; i<gamePlan.length; i++) {
            if (gamePlan[i][0]==agentAID && gamePlan[i][1]==agentBID) {
                return i;
            }
        }
        return -1;
    }

    /*
     * same as {@link TSAgentManager#getPosGamePlan(int, int, int[][])} but uses full double round robin gamePlan
     * @param agentAID ID of first agent
     * @param agentBID ID of second agent
     * @return positiom of of game in gamePlan or -1 if not found
     */
    /*
    private int getPosFullGamePlan(int agentAID, int agentBID) {
        int doubleRRGamePlan[][] = generateGamePlanInternal(true);
        return getPosGamePlan(agentAID, agentBID, doubleRRGamePlan);
    }
    */

    /**
     * run a single player tournament, started in {@link TSSettingsGUI2}, and show stats afterwards
     * @param mArena arena to play in
     */
    public void runSinglePlayerTournament(Arena mArena) {
        lockToCompete(mArena.getGameBoard());
        setSettingsGUIElementsEnabled(false);
        mArena.singlePlayerTSRunning = true;

        /* Progressbar not visible, thread problem?
        JFrame progressBarJF = new JFrame();
        progressBarJF.setSize(300, 100);
        progressBarJF.setTitle("TS Progress...");
        JPanel progressBarJP = new JPanel();
        // JProgressBar-Objekt wird erzeugt
        JProgressBar tsProgressBar = new JProgressBar(0, getTSProgress()[1]);
        // Wert für den Ladebalken wird gesetzt
        tsProgressBar.setValue(0);
        // Der aktuelle Wert wird als
        // Text in Prozent angezeigt
        tsProgressBar.setStringPainted(true);
        // JProgressBar wird Panel hinzugefügt
        progressBarJP.add(tsProgressBar);
        progressBarJF.add(progressBarJP);
        progressBarJF.setVisible(true);
        */

        String res = "";

        while (hasNextGame()) {
            TSAgent[] nextTeam = getNextCompetitionTeam(); // get next Agents
            TSTimeStorage[] nextTimes = getNextCompetitionTimeStorage(); // get timestorage for next game
            StateObservation startSo = getNextStartState();
            TSGameDataTransfer spDT = new TSGameDataTransfer(nextTeam, nextTimes, results.numberOfRandomStartMoves, startSo);
            mArena.m_xab.enableTournamentRemoteData(nextTeam);
            mArena.taskState = Arena.Task.PLAY;
            mArena.PlayGame(spDT);
            nextTimes[0].roundFinished();
            /*
            // progressbar
            gamesPlayed++;
            int[] progress = getTSProgress();
            tsProgressBar.setValue(progress[0]);
            System.out.println(TAG+"TS Progress "+ Arrays.toString(progress));
            */
            mArena.taskState = Arena.Task.IDLE;
            mArena.m_xab.disableTournamentRemoteData();
            res += TAG+"agent:"+nextTeam[0].getName()+" scores: "+Arrays.toString(nextTeam[0].getSinglePlayScores())+"\n";
        }

        makeStats();
        if (getAutoSaveAfterTS()) {
            try {
                mArena.tdAgentIO.saveTSResult(results, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //progressBarJF.dispatchEvent(new WindowEvent(progressBarJF, WindowEvent.WINDOW_CLOSING)); // close progressbar window
        mArena.singlePlayerTSRunning = false;
        unlockAfterComp();
        setSettingsGUIElementsEnabled(true);
        mArena.enableButtons(true);
        //System.out.println(TAG+"agent:"+nextTeam[0].getName()+" scores: "+Arrays.toString(nextTeam[0].getSinglePlayScores()));
        System.out.println(res);
    } // runSinglePlayerTournament

    /**
     * normalize a double value in a range to the range 0 to 1
     * @param data value to be normalized
     * @param dataLow lower limit of data
     * @param dataHigh upper limit of data
     * @return normalized value
     */
    public double normalize(double data, double dataLow, double dataHigh) {
        return normalize(data, dataLow, dataHigh, 0, 1);
    }

    /**
     * normalize a double value in a range and specifie the range it should be normalized to
     * @param data value to be normalized
     * @param dataLow lower limit of data
     * @param dataHigh upper limit of data
     * @param normalizedLow lower limit of normalization
     * @param normalizedHigh upper limit of normalization
     * @return normalized value
     */
    public double normalize(double data, double dataLow, double dataHigh, double normalizedLow, double normalizedHigh) {
        return ((data - dataLow) / (dataHigh - dataLow)) * (normalizedHigh - normalizedLow) + normalizedLow;
    }

    public StateObservation getNextStartState() {
        int gameNumNow;
        if (numPlayers==1) {
            gameNumNow = getNextCompetitionTeam()[0].getSinglePlayScores().length;
        } else {
            gameNumNow = results.gameResult[results.nextGame][0]
                    + results.gameResult[results.nextGame][1]
                    + results.gameResult[results.nextGame][2];
        }
        return randomStartStates[gameNumNow];
    }

    /**
     * helping class to create the sorted heatmap. this saves an agent and his score data
     * in the heatmap to keep them together while sorting by score.
     */
    public static class TSHMDataStorage{
        public String name;
        public TSAgent agent;
        public double[] hmScoreValues;
    }

    /**
     * helping class to create the sorted heatmap. This saves the agent name and the column scores
     * after the rows are sorted.
     */
    public static class TSHM2DataStorage{
        public String agentName;
        public double[] verticalScoreValues;

        /**
         * Constructor to init this class before sorting the heatmap data horizontally
         * @param name name of the rows agent
         * @param verticalScores vertical scores of the agents column
         */
        public TSHM2DataStorage(String name, double[] verticalScores) {
            agentName = name;
            verticalScoreValues = verticalScores;
        }

        public String toString() {
            return "ColumnScores: "+Arrays.toString(verticalScoreValues)+" AgentX: "+agentName;
        }
    }

    /**
     * helping class to create the simplified time measurement table
     */
    public static class TSSimpleTimeTableHelper{
        public int agentID;
        public ArrayList<Double> minTimeForGameMS = new ArrayList<>();
        public ArrayList<Double> maxTimeForGameMS = new ArrayList<>();
        public ArrayList<Double> averageTimeForGameMS = new ArrayList<>();
        public ArrayList<Double> medianTimeForGameMS = new ArrayList<>();
        public ArrayList<Double> averageRoundTimeMS = new ArrayList<>();
        public ArrayList<Double> medianRoundTimeMS = new ArrayList<>();
        public double totalPlayTimeMS = 0;

        public TSSimpleTimeTableHelper(int agentID) {
            this.agentID = agentID;
        }

        public double getMinTimeForGameMS() {
            return Collections.min(minTimeForGameMS);
        }

        public double getMaxTimeForGameMS() {
            return Collections.max(maxTimeForGameMS);
        }

    }
}
