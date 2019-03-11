package controllers;

import java.io.Serializable;

import games.StateObservation;
import params.ParMaxN;
import params.ParOther;
import tools.Types;
import tools.Types.ScoreTuple;

/**
 * Wrapper based on {@link MaxNAgent} for n-ply look-ahead in deterministic games
 * 
 * @author Wolfgang Konen, TH K�ln, Dec'17
 * 
 * @see MaxNAgent
 */
public class MaxNWrapper extends MaxNAgent implements Serializable {
	private PlayAgent wrapped_pa;
	
	public MaxNWrapper(PlayAgent pa, int nply) {
		super("MaxNWrapper", nply);
		this.wrapped_pa = pa;
	}
	
	// XArenaFuncs::wrapAgent is now based on this agent to get MaxN tree depth
	// and useHashMap-flag from ParMaxN mPar
	public MaxNWrapper(PlayAgent pa, ParMaxN mPar, ParOther oPar) {
		super("MaxNWrapper", mPar, oPar);
		this.wrapped_pa = pa;
	}
	
	/**
	 * When the recursion tree has reached its maximal depth m_depth, then return
	 * an estimate of the game score (tuple for all players).  
	 * <p>
	 * Here we use the wrapped {@link PlayAgent} to return a score tuple.
	 * 
	 * @param sob	the state observation
	 * @return		the estimated score tuple
	 */
	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation sob) {
		//--- only debug ---
//		double x = wrapped_pa.getScoreTuple(sob).scTup[0];
//		System.out.println(sob.stringDescr()+":  "+x);
		
		return wrapped_pa.getScoreTuple(sob);
	}
	
	public PlayAgent getWrappedPlayAgent() {
		return wrapped_pa;
	}

	@Override
	public String stringDescr() {
		String cs = wrapped_pa.getClass().getSimpleName();
		cs = cs + "[nPly="+m_depth+"]";
		return cs;
	}

	@Override
	public String getName() {
		String cs = wrapped_pa.getName();
		cs = cs + "[nPly="+m_depth+"]";
		return cs;
	}

}
