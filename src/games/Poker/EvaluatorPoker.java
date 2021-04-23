package games.Poker;

import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import games.Evaluator;
import games.GameBoard;
import games.StateObservation;
import games.XArenaFuncs;
import tools.ScoreTuple;

import java.util.ArrayList;

/**
 * Evaluator for Poker
 */
public class EvaluatorPoker extends Evaluator {

	private final RandomAgent randomAgent = new RandomAgent("Random");
	private final RandomAgent randomAgent2 = new RandomAgent("Random");
	private final RandomAgent randomAgent3 = new RandomAgent("Random");

	protected double[] m_thresh={0.1}; // threshold for each value of m_mode

	protected static ArrayList<StateObserverPoker> diffStartList = null;


	public EvaluatorPoker(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
		super(e_PlayAgent, gb, mode, stopEval, verbose);
		initEvaluator(gb);
	}
	
	private void initEvaluator(GameBoard gb) {
		if (gb!=null)			// /WK/ added to avoid NullPointerException
			gb.getArena();
	}

	@Override
	public boolean evalAgent(PlayAgent playAgent) {
		m_PlayAgent = playAgent;
		switch(m_mode) {
			case -1:
				m_msg = "no evaluation done ";
				lastResult = Double.NaN;
				return false;
			case 0:  return evalAgent1(m_PlayAgent,new PlayAgent[] {randomAgent},m_gb)>m_thresh[0];
			default: return false;
		}
	}

	public void evalAgentSpecificState(PlayAgent playAgent,StateObserverPoker so) {
		m_PlayAgent = playAgent;
		PlayAgent[] pavec = new PlayAgent[] {playAgent,randomAgent,randomAgent2,randomAgent3};
		ScoreTuple sc = XArenaFuncs.competeNPlayer(new PlayAgtVector(pavec), so, 100, 2, null);
		lastResult = sc.scTup[0];
		m_msg = playAgent.getName()+": "+getPrintString() + lastResult;
		System.out.println(m_msg);
	}

	public double evalAgent1(PlayAgent playAgent, PlayAgent opponent,PlayAgent opponent2,PlayAgent opponent3, GameBoard gb) {
		PlayAgent[] pavec = new PlayAgent[] {opponent,opponent2,opponent3};
		return evalAgent1(playAgent, pavec,  gb);
	}
	public double evalAgent1(PlayAgent playAgent, PlayAgent[] opponents, GameBoard gb) {
		StateObservation so = gb.getDefaultStartState();
		PlayAgent[] pavec = new PlayAgent[opponents.length+1];
		int i = 0;
		pavec[i++] = playAgent;
		for (PlayAgent opponent: opponents) {
			pavec[i++] = opponent;
		}
		ScoreTuple sc = XArenaFuncs.competeNPlayerAllRoles(new PlayAgtVector(pavec), so, 1000, 2, true);
		lastResult = sc.scTup[0];
		m_msg = playAgent.getName()+": "+getPrintString() + lastResult;
		if (this.verbose>0) System.out.println(m_msg);
		return lastResult;
	}


 	@Override
 	public int[] getAvailableModes() {
		int[] modes;
		modes = new int[]{0};
		return modes;
 	}
 	
 	@Override
 	public int getDefaultEvalMode() {
		return 0;
	}
	public int getQuickEvalMode()
	{
		return 0;
	}
	public int getTrainEvalMode()
	{
		return 0;
	}

	@Override
	public String getPrintString() {
		return switch (m_mode) {
			case -1 -> "no evaluation done ";
			case 0 -> "success rate (against randomAgent): ";
			default -> null;
		};
	}

	@Override
	public String getTooltipString() {
		return "<html>-1: none<br>"
				+ "</html>";
	}

	@Override
	public String getPlotTitle() {
		return switch (m_mode) {
			case 0 -> "success vs Random";
			case 1 -> "placeholder";
			default -> null;
		};
	}

	/*
	like competeNPlayerAllRoles but with random states
	 */
	public static ScoreTuple competeNPlayerAllRoles(PlayAgtVector paVector, StateObservation startSO, int competeNum,
													int verbose) {
		int N = startSO.getNumPlayers();
		double sWeight = 1 / (double) N;
		ScoreTuple sc, shiftedTuple, scMean = new ScoreTuple(N);
		PlayAgtVector qaVector;
		for (int k = 0; k < N; k++) {
			qaVector = paVector.shift(k);
			sc = XArenaFuncs.competeNPlayer(qaVector, startSO, competeNum, verbose, null,true);
			shiftedTuple = sc.shift(N - k);
			scMean.combine(shiftedTuple, ScoreTuple.CombineOP.AVG, 0, sWeight);
		}
		return scMean;
	}

}
