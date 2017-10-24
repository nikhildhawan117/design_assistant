package geneticAlgorithm;

import java.util.HashMap;
import java.util.LinkedList;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.impl.AbstractGenericSolution;


import designAssistantCore.Configuration;

public class DASolution extends AbstractGenericSolution<Configuration,DAProblem> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8505580614299200682L;

	/** Constructor **/
	public DASolution(DAProblem problem) {
		super(problem);
		initializeConfiguration();
		initializeObjectiveValues();
		// TODO Auto-generated constructor stub
	}
	
	 /** Copy constructor */
	public DASolution(DASolution solution) {
		super(solution.problem);

		for (int i = 0; i < problem.getNumberOfVariables(); i++) {
			setVariableValue(i, new Configuration(solution.getVariableValue(i).getBinaryString()));
		}

		for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
			setObjective(i, solution.getObjective(i)) ;
		}

		attributes = new HashMap<Object, Object>(solution.attributes) ;
	}

	@Override
	public String getVariableValueString(int index) {
		// TODO Auto-generated method stub
		return getVariableValue(0).toString();
	}

	@Override
	public Solution<Configuration> copy() {
		// TODO Auto-generated method stub
		return new DASolution(this);
	}

	private void initializeConfiguration() {	
		setVariableValue(0, new Configuration(Configuration.EMPTY));
	}
	
	private String getRandomConfigurationString(){
		String config = Configuration.EMPTY;
		int max_instruments = (int)(Math.random()*15)+1;
		for (int j = 0; j < max_instruments; j++) {
			int index = (int)(Math.random()*60);
			if (index != 59)
				config = config.substring(0, index) + "1" + config.substring(index+1);
			else
				config = config.substring(0, index) + "1";
		}
		return config;
	}

}
