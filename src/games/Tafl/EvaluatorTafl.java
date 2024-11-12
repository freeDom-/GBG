package games.Tafl;

import controllers.MCTS.MCTSAgentT;
import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.*;
import params.ParMCTS;
import params.ParMaxN;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EvaluatorTafl
    extends Evaluator
{

    private final String logDir = "logs/Tafl/train";
    private final double trainingThreshold = 0.8;

    private MaxNAgent maxNAgent;
    private MCTSAgentT mctsAgent;
    private RandomAgent randomAgent;
    private PlayAgent playAgent;
    private double bestResult = Double.NEGATIVE_INFINITY;
    private String agentDir;

    /**
     * logResults toggles logging of training progress to a csv file located in {@link #logDir}
     */
    private boolean logResults = true;
    private boolean fileCreated = false;
    private PrintWriter logFile;
    private StringBuilder logSB;

    public EvaluatorTafl(PlayAgent e_PlayAgent, GameBoard gb, int mode, int verbose)
    {
        super(e_PlayAgent, gb, mode, verbose);
        if (verbose == 1)
        {
            System.out.println("Using evaluation mode " + mode);
        }
        initEvaluator(e_PlayAgent, gb);
        if (mode == 2 && maxNAgent.getDepth() < TaflConfig.TILE_COUNT)
        {
            System.out.println("Using Max-N with limited tree depth: " +
                               maxNAgent.getDepth() + " used, " + TaflConfig.TILE_COUNT + " needed");
        }
    }

    private void initEvaluator(PlayAgent playAgent, GameBoard gameBoard)
    {
        this.m_gb = gameBoard;
        this.playAgent = playAgent;

        ParMCTS params = new ParMCTS();
        int numIterExp = (Math.min(TaflConfig.BOARD_SIZE, 5) - 1);
        params.setNumIter((int) Math.pow(10, numIterExp));
        int treeDepth = TaflConfig.BOARD_SIZE * TaflConfig.BOARD_SIZE;
        params.setTreeDepth(treeDepth);
        mctsAgent = new MCTSAgentT("MCTS", new StateObserverTafl(), params);

        randomAgent = new RandomAgent("Random");

        ParMaxN parM = new ParMaxN();
        parM.setMaxNDepth(10);
        parM.setMaxNUseHashmap(true);
        maxNAgent = new MaxNAgent("Max-N", parM, new ParOther());
    }

    @Override
    protected EvalResult evalAgent(PlayAgent pa)
    {
        this.playAgent = pa;
        ArenaTafl arena = (ArenaTafl) m_gb.getArena();

        //Disable evaluation by using mode -1
        if (m_mode == -1)
        {
            m_msg = "no evaluation done ";
            lastResult = Double.NaN;
            return new EvalResult(lastResult, true, m_msg, m_mode, Double.NaN);
        }

        //Disable logging for the final evaluation after training
        if (!fileCreated && playAgent.getGameNum() == playAgent.getMaxGameNum())
        {
            logResults = false;
        }

        if (logResults && !fileCreated)
        {
            tools.Utils.checkAndCreateFolder(logDir);
            logSB = new StringBuilder();
            logSB.append("Evaluating agent ").append(playAgent.getName()).append(" for ").append(playAgent.getMaxGameNum()).append(" ").append(getPrintString()).append("\n");
            logSB.append("Agent params: ").append(TaflUtils.getParamsString(playAgent)).append("\n");
            logSB.append("training_matches").append(",");
            logSB.append("result").append(",");
            logSB.append("num_learn_actions").append(",");
            logSB.append("num_train_moves").append(",");
            logSB.append("train_time_ms").append(",");
            logSB.append("eval_time_ms").append("\n");
            try
            {
                logFile = new PrintWriter(logDir + "/" + getCurrentTimeStamp() + " - " + playAgent.getName() + ".csv");
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            fileCreated = true;
        }

        double result;
        int numEpisodes = TaflConfig.EVAL_NUMEPISODES;
        result = switch (m_mode)
        {
            case 0 -> competeAgainstMCTS(playAgent, numEpisodes, 0);
            case 1 -> competeAgainstRandom(playAgent, 0);
            case 2 -> competeAgainstMaxN(playAgent, numEpisodes, 0);
            default -> throw new RuntimeException("Invalid m_mode = " + m_mode);
        };

        String formattedResult = String.format("%.2f", result);

        if (logResults)
        {
            logSB.append(playAgent.getGameNum()).append(",");
            logSB.append(formattedResult).append(",");
            logSB.append(playAgent.getNumLrnActions()).append(",");
            logSB.append(playAgent.getNumTrnMoves()).append(",");
            logSB.append(playAgent.getDurationTrainingMs()).append(",");
            logSB.append(playAgent.getDurationEvaluationMs()).append("\n");
            logFile.write(logSB.toString());
            logSB.delete(0, logSB.length());
            logFile.flush();

            //If the last game has been played, close the file handle.
            //Does not work if the maximum number of training games is not divisible by the number of games per eval.
            if (playAgent.getMaxGameNum() == playAgent.getGameNum())
            {
                logFile.close();
            }
        }

        // Check if Arena Task State == TRAIN and save agent if certain score is reached
        if (arena.taskState == Arena.Task.TRAIN && (result >= bestResult || playAgent.getGameNum() == playAgent.getMaxGameNum()))
        {
            // Create folder for agent on the first evaluation
            if (playAgent.getGameNum() == playAgent.getParOther().getNumEval())
            {
                String gameDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + arena.getGameName() + "/";
                String subDir = arena.getGameBoard().getSubDir() + "/";
                String params = TaflUtils.getParamsStringAbbr(playAgent);
                String dateDir = getCurrentTimeStamp() + " " + playAgent.getName() + (params.isEmpty() ? "" : " " + params) + "/";
                agentDir = gameDir + subDir + dateDir;
                tools.Utils.checkAndCreateFolder(agentDir);
            }

            String fileName = playAgent.getName() + " " + playAgent.getGameNum() + " " + formattedResult + ".agt.zip";
            String savePath = agentDir + fileName;
            playAgent.setAgentState(PlayAgent.AgentState.TRAINED);
            arena.saveAgent(playAgent, savePath);
            playAgent.setAgentState(PlayAgent.AgentState.INIT);
            bestResult = result;
        }

        return new EvalResult(result, lastResult > trainingThreshold, m_msg, m_mode, trainingThreshold);
    }

    /**
     * Very weak but fast evaluator to see if there is a training progress at all.
     * Getting a high win rate against this evaluator does not guarantee good performance of the evaluated agent.
     *
     * @param playAgent Agent to be evaluated
     * @return Percentage of games won on a scale of [0, 1] as double
     */
    private double competeAgainstRandom(PlayAgent playAgent, int verbose)
    {
        ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, randomAgent), 0, new StateObserverTafl(), 100, verbose, null, null, null, false);
        lastResult = sc.scTup[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
        if (this.verbose > 0)
            System.out.println(m_msg);
        return lastResult;
    }

    /**
     * Evaluates an agent's performance with perfect play, as long as tree and rollout depth are not limited.
     * Scales poorly with board size, requires more than 8 GB RAM for board sizes higher than 4x4.
     * And the execution time is unbearable for board sizes of 5x5 and higher.
     *
     * @param playAgent Agent to be evaluated
     * @return Percentage of games won on a scale of [0, 1] as double
     */
    private double competeAgainstMaxN(PlayAgent playAgent, int numEpisodes, int verbose)
    {
        ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, maxNAgent), 0, new StateObserverTafl(), numEpisodes, verbose, null, null, null, false);
        lastResult = sc.scTup[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult + "  (#=" + numEpisodes + ")";
        if (this.verbose > 0)
            System.out.println(m_msg);
        return lastResult;
    }

    /**
     * Evaluates an agent's performance using enough iterations to play (near-) perfectly on boards
     * up to and including 5x5. No guarantees for 6x6 board or higher. Tends to require a lot of
     * memory for 7x7 and up.
     *
     * @param playAgent   agent to be evaluated
     * @param numEpisodes number of episodes played during evaluation
     * @return a value in range [-1,1], depending on the rate of evaluation games won by the agent
     */
    private double competeAgainstMCTS(PlayAgent playAgent, int numEpisodes, int verbose)
    {
        ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(playAgent, mctsAgent), 0, new StateObserverTafl(), numEpisodes, verbose, null, null, null, false);
        lastResult = sc.scTup[0];
        m_msg = playAgent.getName() + ": " + this.getPrintString() + lastResult;
        if (this.verbose > 0)
            System.out.println(m_msg);
        return lastResult;
    }

    @Override
    public int[] getAvailableModes()
    {
        return new int[] {-1, 0, 1, 2};
    }

    @Override
    public int getQuickEvalMode()
    {
        return 0;
    }

    @Override
    public int getTrainEvalMode()
    {
        return -1;
    }

    @Override
    public String getPrintString()
    {
        return switch (m_mode)
        {
            case -1 -> "no evaluation done ";
            case 0 -> "success against MCTS (best is 1.0): ";
            case 1 -> "success against Random (best is 1.0): ";
            case 2 -> "success against Max-N (best is 1.0): ";
            default -> null;
        };
    }

    @Override
    public String getTooltipString()
    {
        return "<html>-1: none<br>"
               + "0: against MCTS, best is 1.0<br>"
               + "1: against Random, best is 1.0<br>"
               + "2: against Max-N, best is 1.0<br>"
               + "</html>";
    }

    @Override
    public String getPlotTitle()
    {
        return switch (m_mode)
        {
            case 0 -> "success against MCTS";
            case 1 -> "success against Random";
            case 2 -> "success against Max-N";
            default -> null;
        };
    }

    /**
     * generates String containing the current timestamp
     *
     * @return the timestamp
     */
    private static String getCurrentTimeStamp()
    {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date now = new Date();
        return sdfDate.format(now);
    }
}
