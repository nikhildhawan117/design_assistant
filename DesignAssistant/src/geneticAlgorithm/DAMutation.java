package geneticAlgorithm;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.BoundedRandomGenerator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import designAssistantCore.Configuration;

public class DAMutation implements MutationOperator<DASolution>{
	private double mutationProbability ;
	private RandomGenerator<Double> randomGenerator ;
	private BoundedRandomGenerator<Integer> pointRandomGenerator ;
	/**  Constructor */
	public DAMutation(double probability) {
		this(probability, () -> JMetalRandom.getInstance().nextDouble(),(a, b) -> JMetalRandom.getInstance().nextInt(a, b));
	}

	/**  Constructor */
	public DAMutation(double probability, RandomGenerator<Double> randomGenerator, BoundedRandomGenerator<Integer> pointRandomGenerator) {
		if (probability < 0) {
			throw new JMetalException("Mutation probability is negative: " + mutationProbability) ;
		}

		this.mutationProbability = probability ;
		this.randomGenerator = randomGenerator ;
		this.pointRandomGenerator = pointRandomGenerator ;
	}
	
	/* Getters */
	public double getMutationProbability() {
		return mutationProbability;
	}

	/* Setters */
	public void setMutationProbability(double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}
	
	@Override
	public DASolution execute(DASolution solution) {
		if (null == solution) {
		      throw new JMetalException("Null parameter") ;
		    }

		    doMutation(mutationProbability, solution) ;
		    
		    return solution;
	}

	private void doMutation(double probabilityThreshold, DASolution solution) {
		if (randomGenerator.getRandomValue() <= probabilityThreshold) {
			// 1. Get the total number of bits
			int totalNumberOfBits = solution.getVariableValue(0).getBinaryString().length();
			// 2. Calculate the point to make the crossover
			int mutationBit = pointRandomGenerator.getRandomValue(0, totalNumberOfBits - 1);
			StringBuilder configString = new StringBuilder(solution.getVariableValue(0).getBinaryString());
			if(configString.charAt(mutationBit) == '1'){
				configString.setCharAt(mutationBit, '0');
			}
			else{
				configString.setCharAt(mutationBit, '1');
			}
			solution.setVariableValue(0, new Configuration(configString.toString())) ;
		}
		
	}	
}
