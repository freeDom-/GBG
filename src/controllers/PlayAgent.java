package controllers;

import TournamentSystem.TSTimeStorage;
import games.StateObservation;
import params.ParOther;
import agentIO.LoadSaveGBG;
import games.Arena;
import games.Evaluator;
import games.GameBoard;
import params.ParRB;
import params.ParWrapper;
import tools.ScoreTuple;
import tools.Types;

/**
 * The abstract interface for game playing agents.
 * <p>
 * See {@link AgentBase} for a base implementation of this interface. Other implementing classes 
 * should usually inherit from {@link AgentBase}.
 *
 * @author Wolfgang Konen, TH Koeln
 */
public interface PlayAgent {
	enum AgentState {RAW, INIT, TRAINED}
	
	/**
	 * Get the best next action and return it 
	 * (NEW version: returns ACTIONS_VT and has a recursive part for multi-moves)
	 * 
	 * @param sob            current game state (is returned unchanged)
	 * @param random        allow random action selection with probability m_epsilon
	 * @param deterministic whether to act deterministic (if several actions have best value, return the 1st one)
	 * @param silent        whether to be silent
	 * @return actBest		the best action. If several actions have the same
	 * 						score, break ties by selecting one of them at random. 
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable to store the value for each available
	 * action (as returned by so.getAvailableActions()) and vBest to store the value for the best action actBest.
	 */
	Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean deterministic, boolean silent);
	
//	/**
//	 * Return the agent's estimate of the final score for that game state.
//	 * <p>
//	 * On the long run, {@link PlayAgent#getScore(StateObservation) getScore(sob)} should become deprecated (in favor of
//	 * {@link PlayAgent#getScoreTuple(StateObservation, ScoreTuple) getScoreTuple(sob, prevTuple)}).
//	 *
//	 * @param sob			the current game state;
//	 * @return				the agent's estimate of the final score for that state.
//	 * 						For 2-player games this is usually the probability
//	 * 						that the player to move wins from that state.
//	 * 						If game is over: the score for the player who *would*
//	 * 						move (if the game were not over).<p>
//	 * Each player wants to maximize *its* score.
//	 */
//	double getScore(StateObservation sob);

	/**
	 * Return the agent's estimate of {@code sob}'s score-to-come  (future reward) <b>for all players</b>.
	 * Is called by {@link #estimateGameValueTuple(StateObservation, ScoreTuple)}.
	 * @param sob			the state s_t for which the value is desired
	 * @param prevTuple		for N &ge; 3 player, we only know the game value for the player who <b>created</b>
	 * 						{@code sob}. To provide also values for other players, {@code prevTuple} allows
	 * 						passing in such other players' value from previous states, which may serve
	 * 						as surrogate for the unknown values in {@code sob}. {@code prevTuple} may be {@code null}.
	 *
	 * @return		an N-tuple with elements V(s_t|i), i=0,...,N-1, the agent's estimate of
	 * 				the **future** score for s_t from the perspective of player i.
	 */
	ScoreTuple getScoreTuple(StateObservation sob, ScoreTuple prevTuple);
	
	/**
	 * Return the agent's estimate of {@code sob}'s final game value (final reward) <b>for all players</b>. 
	 * Is called when maximum episode length (TD) or maximum tree depth for certain agents 
	 * ({@link MaxNAgent}, {@link ExpectimaxNAgent}) is reached.
	 * 
	 * @param sob			the current game state
	 * @param prevTuple		for N &ge; 3 player, we only know the game value for the player who <b>created</b>
	 * 						{@code sob}. To provide also values for other players, {@code prevTuple} allows
	 * 						passing in such other players' value from previous states, which may serve
	 * 						as surrogate for the unknown values in {@code sob}. {@code prevTuple} may be {@code null}. 
	 * @return				the agent's estimate of the final game value (score-so-far plus score-to-come)
	 * 						<b>for all players</b>. The return value is a tuple containing
	 * 						{@link StateObservation#getNumPlayers()} {@code double}'s.
	 */
	ScoreTuple estimateGameValueTuple(StateObservation sob, ScoreTuple prevTuple);
	
	/**
	 * Train the agent for one complete game episode. <p>
	 * Side effects: Increment m_GameNum by +1. Change the agent's internal  
	 * parameters (weights and so on).
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState(PlayAgent)} to get
	 * 					some exploration of different game paths)
	 * @return			true, if agent raised a stop condition (only CMAPlayer - deprecated)	 
	 */
	boolean trainAgent(StateObservation so /*, int epiLength, boolean learnFromRM*/);

	/**
	 * Train the agent for one complete game episode. <p>
	 * Side effects: Increment m_GameNum and {@code acting_pa}'s gameNum by +1.
	 * Change the agent's internal parameters (weights and so on).
	 * <p>
	 * This method is used by the wrappers: They call it with {@code this} being the wrapped agent (it has the internal
	 * parameters) and {@code acting_pa} being the wrapper.
	 *
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState(PlayAgent)} to get
	 * 					some exploration of different game paths)
	 * @param acting_pa the agent to be called when an action is requested ({@code getNextAction2})
	 * @return			true, if agent raised a stop condition (only CMAPlayer - deprecated)
	 */
	boolean trainAgent(StateObservation so, PlayAgent acting_pa);

	String printTrainStatus();
	
	/**
	 * @return true, if it is a trainable agent
	 */
	boolean isTrainable();
	
	/**
	 * @return true, if it was retrained (default: false) <br>
	 * [An agent is called 'retrained' if it was - after 1st training - trained again for a 
	 * second, third, ... time. This possibility for retraining is however for the future, 
	 * it is not yet implemented.]
	 */
	boolean isRetrained();
	
	void incrementDurationTrainingMs(long incr);
	void incrementDurationEvaluationMs(long incr);
	
	/**
	 * Time [ms] needed for training this agent
	 */
	long getDurationTrainingMs();
	/**
	 * 
	 * Time [ms] needed for evaluations during training
	 */
	long getDurationEvaluationMs();
	
	/**
	 * If agents need a special treatment after being loaded from disk (e. g. instantiation
	 * of transient members), override this method and put the relevant code in there.
	 * @return true on success
	 * 
	 * @see LoadSaveGBG#transformObjectToPlayAgent
	 */
	boolean instantiateAfterLoading();
	
	/**
	 * After loading an agent from disk fill the param tabs of {@link Arena} according to the
	 * settings of this agent
	 *
	 * @param n			use the param tabs of the {@code n}'th player
	 * @param m_arena	member {@code m_xab} has the param tabs
	 * 
	 * @see Arena#loadAgent
	 */
	void fillParamTabsAfterLoading(int n, Arena m_arena);
	
	/**
	 * @return a string with information about this agent
	 * @see #stringDescr2()
	 */
	String stringDescr();
	/**
	 * @return a string with additional information about this agent
	 * @see #stringDescr()
	 */
	String stringDescr2();
	
	byte getSize();		// estimated size of agent object

	/**
	 * @return maximum number of training games
	 */
	int getMaxGameNum();
	/**
	 * @return number of training games that this agent actually has performed
	 */
	int getGameNum();
	/**
	 * 
	 * @return number of learn actions (calls to update()) for trainable agents.
	 * (For non-trainable agents, {@link AgentBase} will return 0L.)
	 */
	long getNumLrnActions();
	/**
	 * 
	 * @return number of training moves for trainable agents. Difference to 
	 * {@link #getNumLrnActions()}: it is incremented on random actions as well.
	 * (For non-trainable agents, {@link AgentBase} will return 0L.)
	 */
	long getNumTrnMoves();
	
	/**
	 * @return number of moves in the last train episode for trainable agents.
	 * (For non-trainable agents, {@link AgentBase} will return 0.)
	 */
	int getMoveCounter();

	void setMaxGameNum(int num);
	void setGameNum(int num);
	
	ParOther getParOther();
	ParRB getParReplay();
	ParWrapper getParWrapper();

	/**
	 * @return During training: Call {@link Evaluator} after this number of training games
	 */
	int getNumEval();
	void setNumEval(int num);
	void setStopEval(int num);
	void setWrapperParamsO(ParOther otherPar);
	void setWrapperParamsOfromWr(ParWrapper wrPar);
	void setParOther(ParOther op);
	void setParReplay(ParRB prb);
	void setParWrapper(ParWrapper pwr);

	AgentState getAgentState();
	void setAgentState(AgentState aState);

	String getAgentFile();
	void setAgentFile(String agtFile);

	/**
	 * reset agent when starting a new episode
	 * (needed when re-using an agent, e.g. in competeNum episodes during a competition
	 * {@link games.XArenaFuncs#competeNPlayer(PlayAgtVector, int, StateObservation, int, int, TSTimeStorage[], java.util.ArrayList, java.util.Random, boolean)})
	 */
	void resetAgent();

	String getName();
	void setName(String name);

	boolean isWrapper();
	PlayAgent getWrappedPlayAgent();

	// --- never used ---
//	boolean isStochastic();
//	void setStochastic(boolean hasStochasticPolicy);

}
