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

    @Override
    public String getGameName()
    {
        return "Tafl";
    }

    public static void setBoardSize(int value)
    {
        TaflConfig.BOARD_SIZE = value;
        TaflConfig.TILE_COUNT = value * value;
        TaflConfig.ACTIONS_PER_TOKEN = value * 2;

        switch (value)
        {
            case 5:
                setRuleHardKingCapture(false);
                setRuleWhiteLosesOnSurround(false);
                setRuleShieldwallCapture(false);
                setRuleWhiteLosesOnRepetitions(false);
                setRuleNoSpecialTiles(true);
                setEasyKingEscape(true);
                setStartPlayer(TaflUtils.PLAYER_WHITE);
                break;
            case 7:
                setRuleHardKingCapture(false);
                setRuleWhiteLosesOnSurround(false);
                setRuleShieldwallCapture(false);
                setRuleWhiteLosesOnRepetitions(false);
                setRuleNoSpecialTiles(false);
                setEasyKingEscape(false);
                setStartPlayer(TaflUtils.PLAYER_BLACK);
                break;
            case 9:
                setRuleHardKingCapture(false);
                setRuleWhiteLosesOnSurround(false);
                setRuleShieldwallCapture(false);
                setRuleWhiteLosesOnRepetitions(false);
                setRuleNoSpecialTiles(false);
                setEasyKingEscape(false);
                setStartPlayer(TaflUtils.PLAYER_BLACK);
                break;
            case 11:
                setRuleHardKingCapture(true);
                setRuleWhiteLosesOnSurround(true);
                setRuleShieldwallCapture(true);
                setRuleWhiteLosesOnRepetitions(true);
                setRuleNoSpecialTiles(false);
                setEasyKingEscape(false);
                setStartPlayer(TaflUtils.PLAYER_BLACK);
                break;
            default:
                throw new RuntimeException("Invalid board size " + value);
        }
    }

    public static void setRuleHardKingCapture(boolean value)
    {
        TaflConfig.RULE_HARD_KING_CAPTURE = value;
    }

    public static void setRuleWhiteLosesOnSurround(boolean value)
    {
        TaflConfig.RULE_WHITE_LOSES_ON_SURROUND = value;
    }

    public static void setRuleShieldwallCapture(boolean value)
    {
        TaflConfig.RULE_SHIELDWALL_CAPTURE = value;
    }

    public static void setRuleWhiteLosesOnRepetitions(boolean value)
    {
        TaflConfig.RULE_WHITE_LOSES_ON_REPETITIONS = value;
    }

    public static void setRuleNoSpecialTiles(boolean value)
    {
        TaflConfig.RULE_NO_SPECIAL_TILES = value;
    }

    public static void setEasyKingEscape(boolean value)
    {
        TaflConfig.RULE_EASY_KING_ESCAPE = value;
    }

    public static void setStartPlayer(int value)
    {
        TaflConfig.START_PLAYER = value;
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
