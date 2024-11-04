package games.Tafl;

import controllers.PlayAgent;
import games.*;

public class ArenaTafl extends Arena {

    public ArenaTafl(String title, boolean withUI) {
        super(title, withUI);
    }

    public ArenaTafl(String title, boolean withUI, boolean withTrainRights) {
        super(title, withUI, withTrainRights);
    }

    public static int getBoardSize() {
        return TaflConfig.BOARD_SIZE;
    }

    public static void setBoardSize(int value) {
        TaflConfig.BOARD_SIZE = value;
        TaflConfig.TILE_COUNT = value * value;
        TaflConfig.ACTIONS_PER_TOKEN = value * 2;
    }

    public static boolean getRuleHardKingCapture() {
        return TaflConfig.RULE_HARD_KING_CAPTURE;
    }

    public static void setRuleHardKingCapture(boolean value) {
        TaflConfig.RULE_HARD_KING_CAPTURE = value;
    }

    public static boolean getRuleWhiteSurroundedVictory() {
        return TaflConfig.RULE_WHITE_SURROUNDED_VICTORY;
    }

    public static void setRuleWhiteSurroundedVictory(boolean value) {
        TaflConfig.RULE_WHITE_SURROUNDED_VICTORY = value;
    }

    public static boolean getRuleShieldwallCapture() {
        return TaflConfig.RULE_SHIELDWALL_CAPTURE;
    }

    public static void setRuleShieldwallCapture(boolean value) {
        TaflConfig.RULE_SHIELDWALL_CAPTURE = value;
    }

    public static boolean getRuleRepetitionsLoss() {
        return TaflConfig.RULE_REPETITIONS_LOSS;
    }

    public static void setRuleRepetitionsLoss(boolean value) {
        TaflConfig.RULE_REPETITIONS_LOSS = value;
    }

    @Override
    public String getGameName() {
        return "Tafl";
    }

    @Override
    public GameBoard makeGameBoard() {
        return new GameBoardTafl(this);
    }

    @Override
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int mode, int verbose) {
        return new EvaluatorTafl(pa, gb, mode, verbose);
    }

    @Override
    public Feature makeFeatureClass(int featMode) {
        return new FeatureTafl(featMode);
    }

    @Override
    public XNTupleFuncs makeXNTupleFuncs() {
        return new XNTupleFuncsTafl();
    }
}
