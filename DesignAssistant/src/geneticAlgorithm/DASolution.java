package geneticAlgorithm;

import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.impl.AbstractGenericSolution;
import org.uma.jmetal.util.binarySet.BinarySet;

import designAssistantCore.Configuration;

public class DASolution extends AbstractGenericSolution<Configuration,DAProblem> {

	protected DASolution(DAProblem problem) {
		super(problem);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getVariableValueString(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Solution<Configuration> copy() {
		// TODO Auto-generated method stub
		return null;
	}

	private void initializeConfiguration() {
	    
	      setVariableValue(0, new Configuration());
	    
	 }

}
