package games.EWS;


import controllers.MCTS.MCTSAgentT;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import game.rules.play.Play;
import games.EWS.constants.ConfigEWS;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import games.XArenaFuncs;
import params.ParMaxN;
import tools.ScoreTuple;

import java.util.ArrayList;

public class EvaluatorEWS extends Evaluator {
    private static final long serialVersionUID = 12L;

    private RandomAgent randomAgent;
    private RandomAgent randomAgent2;
    private RandomAgent randomAgent3;


    protected static ArrayList<StateObserverEWS> diffStartList = null;
    protected static int NPLY_DS = 4;


    public EvaluatorEWS(PlayAgent e_PlayAgent, GameBoard gb, int stopEval) {
        super(e_PlayAgent, gb, 1, stopEval);
        initEvaluator();
    }

    public EvaluatorEWS(PlayAgent e_PlayAgent, GameBoard gb, int stopEval  ,int mode) {
        super(e_PlayAgent, gb, mode,stopEval);
        initEvaluator();
    }

    public EvaluatorEWS(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, gb, mode, stopEval, verbose);
        initEvaluator();
    }

    public void initEvaluator() {
        ParMaxN params = new ParMaxN();
        int maxNDepth = 4;
        params.setMaxNDepth(maxNDepth);
        randomAgent = new RandomAgent("Random");
        randomAgent2 = new RandomAgent("Random");
        randomAgent3 = new RandomAgent("Random");

    }

    private static ArrayList<StateObserverEWS> createDifferentStartingPositions(StateObserverEWS so, int k){
        // Creating multiple instances for the different starting positions
        for(int i = 0; i < k; k++){
            // call the random start state of observer
            StateObserverEWS copy = (StateObserverEWS) so.copy();
            diffStartList.add(copy);
        }
        return diffStartList;
    }

    @Override
    protected boolean evalAgent(PlayAgent playAgent) {
        m_PlayAgent  = playAgent;

        if(diffStartList == null){
            StateObserverEWS so = new StateObserverEWS(ConfigEWS.BOARD_SIZE, ConfigEWS.NUM_PLAYERS);
            diffStartList = new ArrayList<>();

        }

        switch (m_mode){
            case -1:
                m_msg = "No evaluation done";
                lastResult = 0.0;
                return false;
            case 0: switch(ConfigEWS.NUM_PLAYERS){
                case 2: return evalAgainstOpponent(m_PlayAgent, randomAgent,false, 100) > 0.0;
                case 3:return evalAgainstOpponent(m_PlayAgent, randomAgent,randomAgent2,false, 100) > 0.0;
                case 4:return evalAgainstOpponent(m_PlayAgent, randomAgent, randomAgent3,false, 100) > 0.0;
            };
            case 1: return evalAgainstOpponent(m_PlayAgent, randomAgent, false,100) > 0.0;
            default: return false;
        }
    }

    /**
     *
     * @param playAgent agent to be evaluated
     * @param opponent agent to be played against
     * @param diffStarts if true
     * @param numEpisodes number of episodes during evaluation
     * @return {@code ScoreTuple} Tuple which holds the average score for {@playAgent} and {@opponent}
     */
    private double evalAgainstOpponent(PlayAgent playAgent, PlayAgent opponent, boolean diffStarts, int numEpisodes){
        StateObservation so = m_gb.getDefaultStartState();
        // Weight stuff we maybe need this later
        int N = ConfigEWS.NUM_PLAYERS;
        ScoreTuple scMean = new ScoreTuple(N);
        if (diffStarts) 	// start from all start states in diffStartList
        {
            double sWeight = 1 / (double) (numEpisodes * diffStartList.size());
            int count=0;
            ScoreTuple sc;
            for (int c=0; c<numEpisodes; c++) {
                for (StateObservation sd : diffStartList) {
                    sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent), sd, 100, 0);
                    scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
                    count++;
                }
            }
            System.out.println("count = "+ count);
        }else {
            scMean= XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent,opponent), so, numEpisodes,0);
        }
        lastResult = scMean.scTup[0];
        m_msg = playAgent.getName() + ": " + getPrintString() + lastResult;
        return lastResult;
    }
    /**
     *
     * @param playAgent agent to be evaluated
     * @param opponent agent to be played against or with
     * @param opponent2 agent to be played against or with
     * @param diffStarts if true
     * @param numEpisodes number of episodes during evaluation
     * @return {@code ScoreTuple} Tuple which holds the average score for {@playAgent} and {@opponent}
     */
    private double evalAgainstOpponent(PlayAgent playAgent, PlayAgent opponent, PlayAgent opponent2, boolean diffStarts, int numEpisodes){
        StateObservation so = m_gb.getDefaultStartState();
        // Weight stuff we maybe need this later
        int N = ConfigEWS.NUM_PLAYERS;
        ScoreTuple scMean = new ScoreTuple(N);
        if (diffStarts) 	// start from all start states in diffStartList
        {
            double sWeight = 1 / (double) (numEpisodes * diffStartList.size());
            int count=0;
            ScoreTuple sc;
            for (int c=0; c<numEpisodes; c++) {
                for (StateObservation sd : diffStartList) {
                    sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent, opponent, opponent2), sd, 100, 0);
                    scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
                    count++;
                }
            }
            System.out.println("count = "+ count);
        }else {
            scMean= XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(playAgent,opponent, opponent2), so, numEpisodes,0);
        }
        lastResult = scMean.scTup[0];
        m_msg = playAgent.getName() + ": " + getPrintString() + lastResult;
        return lastResult;
    }

    @Override
    public int[] getAvailableModes() {
        return new int[]{-1, 0,1};
    }

    @Override
    public int getQuickEvalMode() {
        return 0;
    }

    @Override
    public int getTrainEvalMode() {
        return 0;
    }

    @Override
    public String getPrintString() {
        switch (m_mode) {
            case -1:return "no evaluation done ";
            case 0: return "success against Random (best is 1.0): ";
            case 1: return "success against Mcts (best is 1.0): ";
            default:
                return null;

        }

    }

    @Override
    public String getTooltipString() {
        return "<html>-1: none<br>"
                + " 0: vs. Random, best is 1.0"
                + "1: vs. Mcts, best is 1.0"
                + "</html>";

    }

    @Override
    public String getPlotTitle() {
        switch (m_mode) {
            case 0:
                return "success against Random";
            case 1: return "success against Mcts";
            default:
                return null;
        }


    }
}