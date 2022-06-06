package games.RubiksCube;

import controllers.PlayAgent;
import games.*;
import games.RubiksCube.CubeConfig.BoardVecType;
import games.RubiksCube.CubeConfig.CubeSize;
import games.RubiksCube.CubeConfig.TwistType;

/**
 * {@link Arena} for Rubik's Cube. It borrows all functionality
 * from the general class {@link Arena} . It only overrides
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()}, 
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int)}, and 
 * <li> {@link Arena#makeFeatureClass(int)}, 
 * </ul> such that 
 * these factory methods return objects of class {@link GameBoardCube}, 
 * {@link EvaluatorCube}, and {@link FeatureCube}, respectively.
 * 
 * @see GameBoardCube
 * @see EvaluatorCube
 * 
 * @author Wolfgang Konen, TH Koeln, 2018-2021
 */
public class ArenaCube extends Arena   {

	public ArenaCube(String title, boolean withUI) {
		super(title,withUI);
		CubeStateFactory.generateInverseTs();
		CubeState.generateForwardTs();
	}

	public ArenaCube(String title, boolean withUI, boolean withTrainRights) {
		super(title,withUI,withTrainRights);
		CubeStateFactory.generateInverseTs();
		CubeState.generateForwardTs();
	}

	/**
	 * @return a name of the game, suitable as subdirectory name in the 
	 *         {@code agents} directory
	 *         
	 * @see GameBoardCube#getSubDir() 
	 */
	@Override
	public String getGameName() {
		return "RubiksCube";
	}
	
	/**
	 * Factory pattern method
	 */
	@Override
	public GameBoard makeGameBoard() {
		CubeStateFactory.generateInverseTs();	// since makeGameBoard is called via super --> Arena
		CubeState.generateForwardTs();			// prior to finishing ArenaTrainCube(String,boolean)
		gb = new GameBoardCube(this);

		// optional debug info: print out invU and invL, given invF:
		boolean SHOW_INV = false;
		if (SHOW_INV) {
			CubeStateFactory csfactory = new CubeStateFactory();
			csfactory.makeCubeState().show_invF_invL_invU();
			// once to print out the arrays needed for invL and invU (see CubeState2x2, CubeState3x3)
		}

		return gb;
	}

	/**
	 * Factory pattern method: make a new Evaluator
	 * @param pa		the agent to evaluate
	 * @param gb		the game board
	 * @param stopEval	maximum episode length {@code epiLength} during evaluation
	 * @param mode		which evaluator mode: -1,0,1. Throws a runtime exception
	 * 					if {@code mode} is not in the set {@link Evaluator#getAvailableModes()}.
	 * @param verbose	how verbose or silent the evaluator is
	 * @return			the new evaluator
	 */
	@Override
	public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
		return new EvaluatorCube(pa,gb,stopEval,mode,verbose);
	}

	@Override
	public Feature makeFeatureClass(int featmode) {
		return new FeatureCube(featmode);
	}

	@Override
	public XNTupleFuncs makeXNTupleFuncs() {
		return new XNTupleFuncsCube();
	}

    /**
     * set the cube type (POCKET or RUBIKS) for Rubik's Cube
     */
    public static void setCubeType(String sCube) {
		switch (sCube) {
			case "2x2x2" -> CubeConfig.cubeSize = CubeSize.POCKET;
			case "3x3x3" -> CubeConfig.cubeSize = CubeSize.RUBIKS;
			default -> throw new RuntimeException("Cube type " + sCube + " is not known.");
		}
    }

    /**
     * set the board vector type for Rubik's Cube
     */
    public static void setBoardVecType(String bvType) {
		switch (bvType) {
			case "CSTATE" -> CubeConfig.boardVecType = BoardVecType.CUBESTATE;
			case "CPLUS" -> CubeConfig.boardVecType = BoardVecType.CUBEPLUSACTION;
			case "STICKER" -> CubeConfig.boardVecType = BoardVecType.STICKER;
			case "STICKER2" -> CubeConfig.boardVecType = BoardVecType.STICKER2;
			default -> throw new RuntimeException("Board vector type " + bvType + " is not known.");
		}
    }

    /**
     * set the twist type (HTM = half-turn metric or QTM = quarter-turn metric) for Rubik's Cube
     */
    public static void setTwistType(String tCube) {
		switch (tCube) {
			case "HTM" -> CubeConfig.twistType = TwistType.HTM;
			case "QTM" -> CubeConfig.twistType = TwistType.QTM;
			default -> throw new RuntimeException("Twist type " + tCube + " is not known.");
		}
    }

    // *** ArenaCube is now started via GBGLaunch or GBGBatch
//	/**
//	 * Start GBG for Rubik's Cube
//	 *
//	 * @param args
//	 */
//	public static void main(String[] args)
//	{
//		ArenaCube t_Frame = new ArenaCube("General Board Game Playing",true);
//
//		if (args.length==0) {
//			t_Frame.init();
//		} else {
//			throw new RuntimeException("[ArenaCube.main] args="+args+" not allowed. Use batch facility.");
//		}
//	}
	
}
