package geneticAlgorithm;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.binarySet.BinarySet;
import org.uma.jmetal.util.pseudorandom.BoundedRandomGenerator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import designAssistantCore.Configuration;

public class DACrossover implements CrossoverOperator<DASolution> {
	private double crossoverProbability ;
	private RandomGenerator<Double> crossoverRandomGenerator ;
	private BoundedRandomGenerator<Integer> pointRandomGenerator ;
	
	public DACrossover(double crossoverProbability){
		this(crossoverProbability, () -> JMetalRandom.getInstance().nextDouble(), (a, b) -> JMetalRandom.getInstance().nextInt(a, b));
	}

	public DACrossover(double crossoverProbability, RandomGenerator<Double> crossoverRandomGenerator, BoundedRandomGenerator<Integer> pointRandomGenerator) {
		if (crossoverProbability < 0) {
			throw new JMetalException("Crossover probability is negative: " + crossoverProbability) ;
		}
		this.crossoverProbability = crossoverProbability;
		this.crossoverRandomGenerator = crossoverRandomGenerator ;
		this.pointRandomGenerator = pointRandomGenerator ;
	}

	@Override
	public List<DASolution> execute(List<DASolution> parents) {
		if (parents == null) {
		      throw new JMetalException("Null parameter") ;
		    } else if (parents.size() != 2) {
		      throw new JMetalException("There must be two parents instead of " + parents.size()) ;
		    }

		    return doCrossover(crossoverProbability, parents.get(0), parents.get(1)) ;
	}

	private List<DASolution> doCrossover(double probabilityThreshold, DASolution parent1, DASolution parent2) {
		// TODO Auto-generated method stub
		List<DASolution> offspring = new ArrayList<>(2);
	    offspring.add((DASolution) parent1.copy()) ;
	    offspring.add((DASolution) parent2.copy()) ;

	    if (crossoverRandomGenerator.getRandomValue() < probabilityThreshold) {
	      // 1. Get the total number of bits
	      int totalNumberOfBits = parent1.getVariableValue(0).getBinaryString().length();
	      // 2. Calculate the point to make the crossover
	      int crossoverPoint = pointRandomGenerator.getRandomValue(0, totalNumberOfBits - 1);

	      // 5. Apply the crossover to the variable;
	      StringBuilder offspring1String, offspring2String;
	      offspring1String = new StringBuilder(parent1.getVariableValue(0).getBinaryString());
	      offspring2String = new StringBuilder(parent2.getVariableValue(0).getBinaryString());
	      
	      String offspring1Tail = offspring1String.substring(crossoverPoint);
	      String offspring2Tail = offspring2String.substring(crossoverPoint);
	      
	      offspring1String.replace(crossoverPoint,offspring1String.length(),offspring2Tail);
	      offspring2String.replace(crossoverPoint,offspring2String.length(),offspring1Tail);

	      offspring.get(0).setVariableValue(0, new Configuration(offspring1String.toString()));
	      offspring.get(1).setVariableValue(0, new Configuration(offspring2String.toString()));


	    }
	    return offspring ;

	}

	@Override
	public int getNumberOfRequiredParents() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public int getNumberOfGeneratedChildren() {
		// TODO Auto-generated method stub
		return 2;
	}

}
