package geneticAlgorithm;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ2;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT2;
import org.uma.jmetal.runner.AbstractAlgorithmRunner;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import designAssistantCore.DesignAssistant;

import java.io.FileNotFoundException;
import java.util.List;

public class DARunner extends AbstractAlgorithmRunner {
  /**
   * @param args Command line arguments.
   * @throws JMetalException
   * Invoking command:
    java org.uma.jmetal.runner.multiobjective.NSGAIIRunner problemName [referenceFront]
   */
  public DARunner(DesignAssistant da) throws JMetalException {
    Problem<DASolution> problem;
    Algorithm<List<DASolution>> algorithm;
    CrossoverOperator<DASolution> crossover;
    MutationOperator<DASolution> mutation;
    SelectionOperator<List<DASolution>, DASolution> selection;
    SolutionListEvaluator<DASolution> evaluator;
    DADiversity diversityMetric;
    
    problem = new DAProblem(2,1,da);//ProblemUtils.<DoubleSolution> loadProblem(problemName);

    double crossoverProbability = 0.9 ;
    crossover = new DACrossover(crossoverProbability) ;

    double mutationProbability = 1.0 / problem.getNumberOfVariables() ;
    double mutationDistributionIndex = 20.0 ;
    mutation = new DAMutation(mutationProbability) ;

    selection = new BinaryTournamentSelection<DASolution>(
        new RankingAndCrowdingDistanceComparator<DASolution>());
    diversityMetric = new DAHammingDiversity();
    evaluator = new DACachedSequentialSolutionListEvaluator(diversityMetric);
    //to use no diversity metric, replace above with this:
    //evaluator = new DACachedSequentialSolutionListEvaluator();
    
    algorithm = new NSGAIIBuilder<DASolution>(problem, crossover, mutation)
        .setSelectionOperator(selection)
        .setSolutionListEvaluator(evaluator)
        .setVariant(NSGAIIBuilder.NSGAIIVariant.SteadyStateNSGAII)
        .setMaxEvaluations(25000)
        .setPopulationSize(50)
        .build() ;

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    List<DASolution> population = algorithm.getResult() ;
    long computingTime = algorithmRunner.getComputingTime() ;

  }
}
