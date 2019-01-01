package games.ZweiTausendAchtundVierzig.Heuristic;

import controllers.PlayAgent;
import games.Evaluator;
import games.ZweiTausendAchtundVierzig.ConfigEvaluator;
import tools.cmaes.CMAEvolutionStrategy;

/**
 * Created by Johannes on 29.06.2017.
 */
public class Evaluator2048_EA extends Evaluator {
    private HeuristicSettings2048 fitfun = new HeuristicSettings2048();
    private CMAEvolutionStrategy cma = new CMAEvolutionStrategy();
    private double bestFitness = 0;

    public Evaluator2048_EA(PlayAgent e_PlayAgent, int stopEval, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
    }

    @Override
    protected boolean eval_Agent(PlayAgent pa) {
    	m_PlayAgent = pa;
        cma.setDimension(15);
        cma.setInitialX(0.5);
        cma.setInitialStandardDeviation(0.2);

        cma.options.stopMaxFunEvals = 240;

        double[] fitness = cma.init();

        double[][] sam = cma.samplePopulation();

   /*     System.out.println("sample population: ");
        for(int i = 0; i < sam.length; i++) {
            for(int j = 0; j < sam[i].length; j++) {
                System.out.print(sam[i][j] + "  |  ");
            }
            System.out.print("\n\n");
        }*/

        while(cma.stopConditions.getNumber() == 0) {

            // core iteration step
            double[][] pop = cma.samplePopulation(); // get a new population of solutions
            for (int i = 0; i < pop.length; ++i) {    // for each candidate solution i
                System.out.println("\nStarting Evaluation: " + (cma.getCountEval() + i));
                while (!fitfun.isFeasible(pop[i]))   //    test whether solution is feasible,
                    pop[i] = cma.resampleSingle(i);  //       re-sample solution until it is feasible
                fitness[i] = fitfun.valueOf(pop[i]); //    compute fitness value, where fitfun
            }                                         //    is the function to be minimized
            cma.updateDistribution(fitness);         // pass fitness array to update search distribution

            // output to console and files
            cma.writeToDefaultFiles();
            int outmod = 150;
            if (cma.getCountIter() % (15 * outmod) == 1)
                cma.printlnAnnotation(); // might write file as well
            if (cma.getCountIter() % outmod == 1)
                cma.println();


            bestFitness = (1-cma.getBestFunctionValue())*100000;
            cma.println("\nbest fitness " + bestFitness
                    + " at evaluation " + cma.getBestEvaluationNumber());

            System.out.println("best settings are: ");
            double[] best = cma.getBestX();
            for (int i = 0; i < best.length; i++) {
                System.out.println(i + ": " + best[i]);
            }
        }

        bestFitness = (1-cma.getBestFunctionValue())*100000;

        cma.writeToDefaultFiles(1);
        cma.println();
        cma.println("\nTerminated due to");
        for (String s : cma.stopConditions.getMessages())
            cma.println("  " + s);
        cma.println("best fitness " + bestFitness
                + " at evaluation " + cma.getBestEvaluationNumber());

        System.out.println("best settings are: ");
        double[] best = cma.getBestX();
        for (int i = 0; i < best.length; i++) {
            System.out.println(i + ": " + best[i]);
        }

        return true;
    }

    @Override
    public double getLastResult() {
        return bestFitness;
    }

    @Override
    public String getMsg() {
        return "";
    }

    @Override
    public boolean isAvailableMode(int mode) {
        switch (mode) {
        	case -1: 
        	case  0:
            case  1:
            case  2:
                return true;
            default:
                return false;
        }
    }

    @Override
    public int[] getAvailableModes() {
        return new int[]{-1, 0, 1, 2};
    }

    @Override
    public int getQuickEvalMode() {
        return ConfigEvaluator.DEFAULTEVALUATOR;
    }

    @Override
    public int getTrainEvalMode() {
        return 0;
    }

    @Override
    public int getMultiTrainEvalMode() {
        return 0;
    }

    @Override
    public String getPrintString() {
        return"success rate";
    }

	@Override
	public String getTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>-1: none<br>"
				+ "0: avg score from 50 games<br>"
				+ "1: Evaluator2048_BoardPositions<br>"
				+ "2: Evaluator2048_EA<br>"
				+ "</html>";
	}

    @Override
    public String getPlotTitle() {
        return "success";
    }
}
