package games.Tafl;

import controllers.PlayAgent;
import games.*;

import java.io.IOException;

public class ArenaTafl
    extends Arena
{

    public ArenaTafl(String title, boolean withUI)
    {
        super(title, withUI);
    }

    public ArenaTafl(String title, boolean withUI, boolean withTrainRights)
    {
        super(title, withUI, withTrainRights);
    }

    public static int getBoardSize()
    {
        return TaflConfig.BOARD_SIZE;
    }

    public static void setBoardSize(int value)
    {
        TaflConfig.BOARD_SIZE = value;
        TaflConfig.TILE_COUNT = value * value;
        TaflConfig.ACTIONS_PER_TOKEN = value * 2;

        switch (value)
        {
            case 7:
                setRuleHardKingCapture(false);
                setRuleWhiteLosesOnSurround(false);
                setRuleShieldwallCapture(false);
                setRuleWhiteLosesOnRepetitions(false);
                break;
            case 9:
                setRuleHardKingCapture(false);
                setRuleWhiteLosesOnSurround(false);
                setRuleShieldwallCapture(false);
                setRuleWhiteLosesOnRepetitions(false);
                break;
            case 11:
                setRuleHardKingCapture(true);
                setRuleWhiteLosesOnSurround(true);
                setRuleShieldwallCapture(true);
                setRuleWhiteLosesOnRepetitions(true);
                break;
            default:
                throw new RuntimeException("Invalid board size " + value);
        }
    }

    public static boolean getRuleHardKingCapture()
    {
        return TaflConfig.RULE_HARD_KING_CAPTURE;
    }

    public static void setRuleHardKingCapture(boolean value)
    {
        TaflConfig.RULE_HARD_KING_CAPTURE = value;
    }

    public static boolean getRuleWhiteLosesOnSurround()
    {
        return TaflConfig.RULE_WHITE_LOSES_ON_SURROUND;
    }

    public static void setRuleWhiteLosesOnSurround(boolean value)
    {
        TaflConfig.RULE_WHITE_LOSES_ON_SURROUND = value;
    }

    public static boolean getRuleShieldwallCapture()
    {
        return TaflConfig.RULE_SHIELDWALL_CAPTURE;
    }

    public static void setRuleShieldwallCapture(boolean value)
    {
        TaflConfig.RULE_SHIELDWALL_CAPTURE = value;
    }

    public static boolean getRuleWhiteLosesOnRepetitions()
    {
        return TaflConfig.RULE_WHITE_LOSES_ON_REPETITIONS;
    }

    public static void setRuleWhiteLosesOnRepetitions(boolean value)
    {
        TaflConfig.RULE_WHITE_LOSES_ON_REPETITIONS = value;
    }

    @Override
    public String getGameName()
    {
        return "Tafl";
    }

    @Override
    public GameBoard makeGameBoard()
    {
        return new GameBoardTafl(this);
    }

    @Override
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int mode, int verbose)
    {
        return new EvaluatorTafl(pa, gb, mode, verbose);
    }

    @Override
    public Feature makeFeatureClass(int featMode)
    {
        return new FeatureTafl(featMode);
    }

    @Override
    public XNTupleFuncs makeXNTupleFuncs()
    {
        return new XNTupleFuncsTafl();
    }

    /**
     * Start GBG for Tafl (non-trainable version)
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args)
        throws IOException
    {
        ArenaTafl t_Frame = new ArenaTafl("General Board Game Playing", true);

        if (args.length == 0)
        {
            t_Frame.init();
        }
        else
        {
            throw new RuntimeException("[ArenaTafl.main] args=" + args + " not allowed. Use GBGBatch.");
        }
    }
}
