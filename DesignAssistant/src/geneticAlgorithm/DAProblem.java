package geneticAlgorithm;

import org.uma.jmetal.problem.BinaryProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.impl.AbstractGenericProblem;

import designAssistantCore.Configuration;
import designAssistantCore.DesignAssistant;
import rbsa.eoss.ArchitectureEvaluator;
import rbsa.eoss.ArchitectureGenerator;


public class DAProblem extends AbstractGenericProblem<DASolution> {
	private int numberOfVariables;
	private int numberOfObjectives;
	private DesignAssistant DA;
	
	public DAProblem(int numberOfObjectives,int numberOfVariables, DesignAssistant DA){
		this.numberOfObjectives = numberOfObjectives;
		this.numberOfVariables = numberOfVariables;
		this.DA = DA;
	}
	
	
	@Override
	public int getNumberOfVariables() {
		// TODO Auto-generated method stub
		return numberOfVariables;
	}

	@Override
	public int getNumberOfObjectives() {
		// TODO Auto-generated method stub
		return numberOfObjectives;
	}

	@Override
	public int getNumberOfConstraints() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "DAProblem";
	}

	@Override
	public void evaluate(DASolution solution) {
		// TODO Auto-generated method stub
		Configuration configuration = solution.getVariableValue(0);
		double [] result = DA.evaluateArchitecture(configuration);
		solution.setObjective(0, result[0]);
		solution.setObjective(1,result[1]);	
	}

	@Override
	public DASolution createSolution() {
		// TODO Auto-generated method stub
		return new DASolution(this);
	}

}
