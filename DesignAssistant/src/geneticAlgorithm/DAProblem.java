package geneticAlgorithm;

import org.uma.jmetal.problem.BinaryProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.impl.AbstractGenericProblem;

import designAssistantCore.Configuration;
import designAssistantCore.DesignAssistant;
import rbsa.eoss.ArchitectureEvaluator;
import rbsa.eoss.ArchitectureGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class DAProblem extends AbstractGenericProblem<DASolution> {
	private int numberOfVariables;
	private int numberOfObjectives;
	private DesignAssistant DA;
	private LinkedList<String[]> randomData;
	
	public DAProblem(int numberOfObjectives,int numberOfVariables, DesignAssistant DA){
		this.numberOfObjectives = numberOfObjectives;
		this.numberOfVariables = numberOfVariables;
		this.DA = DA;
		randomData = new LinkedList<String[]>();
		BufferedReader br;
		try {
			File cache_file = new File("./src/GA_initial_cache.txt");
			br = new BufferedReader(new FileReader(cache_file));
			
			String nextLine = br.readLine();
			while (nextLine != null) {
				String[] data = nextLine.split(",");
				randomData.add(data);
				nextLine = br.readLine();
			}
			br.close();
			
		} catch(IOException e) {
			e.printStackTrace();
		} 
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
		
		solution.setObjective(0, 1-result[0]);
		solution.setObjective(1, result[1]);	
	}
	
	public void evaluateInitialData(DASolution solution) {
		String[] datum = randomData.pop();
		solution.setVariableValue(0, new Configuration(datum[0]));
		solution.setObjective(0, 1-Double.parseDouble(datum[1]));
		solution.setObjective(1, Double.parseDouble(datum[2]));	
	}
	

	@Override
	public DASolution createSolution() {
		// TODO Auto-generated method stub
		return new DASolution(this);
	}
	
	public void plotObjectives(Configuration configuration, double science, double cost){
		this.DA.addGraphPoint(configuration,science,cost);
	}

}
