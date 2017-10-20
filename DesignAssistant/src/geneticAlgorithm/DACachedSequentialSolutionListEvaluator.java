package geneticAlgorithm;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.uma.jmetal.problem.ConstrainedProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import designAssistantCore.Configuration;

public class DACachedSequentialSolutionListEvaluator implements SolutionListEvaluator<DASolution> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1757668683872332893L;
	private HashMap<Configuration,double[]> cachedSolutions;
	private DADiversity diversityMetric;
	private Logger logger;
	
	public DACachedSequentialSolutionListEvaluator(){
		cachedSolutions = new HashMap<Configuration,double[]>();
		diversityMetric = null;
		this.logger = Logger.getLogger("Design Assistant User Study File Log");
	}
	public DACachedSequentialSolutionListEvaluator(DADiversity diversityMetric){
		cachedSolutions = new HashMap<Configuration,double[]>();
		this.diversityMetric = diversityMetric;
		this.logger = Logger.getLogger("Design Assistant User Study File Log");

	}
	@Override
	public List<DASolution> evaluate(List<DASolution> solutionList, Problem<DASolution> problem) {
		//loop through the solutions, if we've calculated science/cost before, dehash it, otherwise calculate
		boolean isFirstRun = cachedSolutions.isEmpty();
		solutionList.stream().forEach(s -> {
			double [] cachedSolution = cachedSolutions.get(s.getVariableValue(0));
			if(cachedSolution!=null){
				s.setObjective(0, cachedSolution[0]);
				s.setObjective(1, cachedSolution[1]);
			}
			else{
				problem.evaluate(s);
				
				cachedSolutions.put(s.getVariableValue(0),new double[] {s.getObjective(0),s.getObjective(1)});
				if(!isFirstRun){
					DAProblem daProblem = (DAProblem) problem;
					
					daProblem.plotObjectives(s.getVariableValue(0),(1-s.getObjective(0)),s.getObjective(1));
					logger.info("AGENT_EVENT" + "," + s.getVariableValue(0).getBinaryString() + "," + (1-s.getObjective(0)) + "," + s.getObjective(1));
				}
				
					System.out.println("Science: " + (1-s.getObjective(0)) + " Cost: " + s.getObjective(1));
				
			}
		});
		return solutionList;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		;
		
	}

}
