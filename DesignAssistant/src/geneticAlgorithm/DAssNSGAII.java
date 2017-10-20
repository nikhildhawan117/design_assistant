package geneticAlgorithm;
import java.util.List;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.*;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

public class DAssNSGAII<S extends Solution<?>> extends SteadyStateNSGAII<S>{

	public DAssNSGAII(Problem<S> problem, int maxEvaluations, int populationSize,
			CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
			SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
		super(problem, maxEvaluations, populationSize, crossoverOperator, mutationOperator, selectionOperator, evaluator);
		
	}
	
	/*
	 * Sets evaluations = max evaluations allowing termination
	 */
	public void terminate() {
		evaluations = maxEvaluations;
	}

}
