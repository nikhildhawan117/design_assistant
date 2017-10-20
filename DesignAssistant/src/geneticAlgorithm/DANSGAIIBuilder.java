package geneticAlgorithm;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIMeasures;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.SteadyStateNSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder.NSGAIIVariant;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

public class DANSGAIIBuilder<S extends Solution<?>> extends NSGAIIBuilder<S> {

	public DANSGAIIBuilder(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
			MutationOperator<S> mutationOperator) {
		super(problem, crossoverOperator, mutationOperator);
		
	}
	
	  public NSGAII<S> build() {
		    NSGAII<S> algorithm = null;
		    algorithm = new DAssNSGAII<S>(getProblem(), getMaxIterations(), getPopulationSize(), getCrossoverOperator(),
		            getMutationOperator(), getSelectionOperator(), getSolutionListEvaluator());
		    return algorithm ;
		  }	

}
