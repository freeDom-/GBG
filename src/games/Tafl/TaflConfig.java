package games.Tafl;

public class TaflConfig
{

    final static double REWARD_NEGATIVE = -1.0;
    final static double REWARD_POSITIVE = 1.0;

    /**
     * Length of one side of the game board in tiles
     */
    public static int BOARD_SIZE = 7;

    /**
     * Number of tiles on the game board
     */
    static int TILE_COUNT = BOARD_SIZE * BOARD_SIZE;

    static int ACTIONS_PER_TOKEN = 14;

    static int UI_TILE_SIZE = 64;
    static int UI_OFFSET = 0; //UI_TILE_SIZE / 4;

    /**
     * Number of episodes to play when evaluating an agent
     *
     * @see EvaluatorTafl
     */
    final static int EVAL_NUMEPISODES = 3; // 5; //10;

    static boolean RULE_HARD_KING_CAPTURE = false;
    static boolean RULE_WHITE_LOSES_ON_SURROUND = false;
    static boolean RULE_SHIELDWALL_CAPTURE = false;
    static boolean RULE_WHITE_LOSES_ON_REPETITIONS = false;
}
