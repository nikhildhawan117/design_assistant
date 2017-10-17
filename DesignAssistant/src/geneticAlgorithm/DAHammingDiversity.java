package geneticAlgorithm;

import java.util.List;

public class DAHammingDiversity implements DADiversity{

	@Override
	public double evaluate(DASolution solution, List<DASolution> population) {
		// TODO Auto-generated method stub
		long solutionLong = solution.getVariableValue(0).getBinaryOneHot();
		double accumulatedDistance=0;
		for(int i = 0; i<population.size(); i++){
			accumulatedDistance += hammingDistance(solutionLong, population.get(i).getVariableValue(0).getBinaryOneHot());
		}
		return accumulatedDistance/population.size();
	}
	private int hammingDistance(long a, long b){
		Long unique = a^b;
		//note, this assumes that unique is positive, which will be true
		//as long as the top bit remains unfilled (which should be since we have 60 bits)
		return Long.bitCount(unique);
	}

}
