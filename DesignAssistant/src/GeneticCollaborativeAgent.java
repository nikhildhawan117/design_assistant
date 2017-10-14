import rbsa.eoss.Architecture;
import rbsa.eoss.ArchitectureEvaluator;
import rbsa.eoss.ArchitectureGenerator;
import rbsa.eoss.Result;
import rbsa.eoss.ResultManager;
import rbsa.eoss.local.Params;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class GeneticCollaborativeAgent {
	public ArchitectureGenerator AG;
	public ArchitectureEvaluator AE;
	private ArrayList<Double> genFitness;
	private ArrayList<Individual> currentPopulation;
	private GraphComponent graphDisplayComponent;
	private HashSet<String> currentPopulationSet;
	private Logger logger;
	private int popSize;
	private int xMin = 85;
	private int xMax = 1285+100; //4000x (0,0.281) 100px margin of error (these bounds are estimated on the pre-data)
	private int yMin = 25;
	private int yMax = 825+50; //(1/12)x (0,9981), with a 50px margin of error (some of the points were above the plot)
	
	public GeneticCollaborativeAgent(ArchitectureGenerator AG, ArchitectureEvaluator AE, GraphComponent graphDisplayComponent, Logger logger, int size) {
		this.AE = AE;
		this.AG = AG;
		this.genFitness = new ArrayList<Double>();
		currentPopulationSet = new HashSet<String>();
		this.popSize = size;
		this.graphDisplayComponent = graphDisplayComponent;
		this.logger = logger;
		currentPopulation = new ArrayList<Individual>(size);
		getInitPop();
	}
	
	public Individual[] getCurrentPopulation() {
		Individual[] individuals = new Individual[currentPopulation.size()];
		currentPopulation.toArray(individuals);
		return individuals;
	}
	
	public static long powerN(long number, int power){
		long res = 1;
		long sq = number;
		while(power > 0){
			if(power % 2 == 1){
				res *= sq;
			}
			sq = sq * sq;
			power /= 2;
		}
		return res;
	}
	
	public void getInitPop() {		
		for (int i = 0; i < popSize; i++) {
			String config = Configuration.EMPTY;
			int max_instruments = (int)(Math.random()*15)+1;
			for (int j = 0; j < max_instruments; j++) {
				int index = (int)(Math.random()*60);
				if (index != 59)
					config = config.substring(0, index) + "1" + config.substring(index+1);
				else
					config = config.substring(0, index) + "1";
			}
			if (!currentPopulationSet.contains(config)) {
				currentPopulationSet.add(config);
				Individual randomIndividual = new Individual(config);
				GraphPoint gp = new GraphPoint(randomIndividual.getConfiguration(), randomIndividual.getScience()*4000, randomIndividual.getCost()/12.0, xMin, xMax, yMin, yMax);
				gp.isPreData = true;
				graphDisplayComponent.addGeneratedGraphPoint(gp);
				logger.info("AGENT_EVENT" + " " + randomIndividual.getConfiguration().getBinaryString() + " " + randomIndividual.getScience() + " " + randomIndividual.getCost());
				currentPopulation.add(randomIndividual);	
			}
			else
				i--;
		}
		currentPopulation.sort(new Comparator<Individual>() {
			public int compare(Individual i1, Individual i2) {
				if(i1.getFitness()>i2.getFitness())
					return 1;
				else if(i1.getFitness()<i2.getFitness())
					return -1;
				return 0;
			}
		});
		
		for (int i = 0; i< currentPopulation.size(); i++) {
			System.out.println(currentPopulation.get(i).fitness);
		}
	}
	
	private Individual[] selectParents() {
		LinkedList<Integer> indices = new LinkedList<Integer>();
		for (int i = 0; i < popSize/2; i++) {
			indices.add(currentPopulation.size()-i-1);
		}
		Collections.shuffle(indices);
		Individual p1 = currentPopulation.get(indices.poll());
		Individual p2 = currentPopulation.get(indices.poll());
		return new Individual[] {p1, p2};
	}
	
	private void removeIndividuals(int n) {
		for (int i = 0; i < n; i++) {
			if (currentPopulation.size() >= 0)
				currentPopulation.remove(0);
		}
	}
	
	public void updatePopulation() {
		Individual[] parents = selectParents();
		String child_string = crossover(parents[0], parents[1]);
		
		
		while (currentPopulationSet.contains(child_string)) {
			parents = selectParents();
			child_string = crossover(parents[0], parents[1]);
		}
		currentPopulationSet.add(child_string);
		removeIndividuals(1);
		Individual child = new Individual(child_string);
		currentPopulation.add(child);
		GraphPoint gp = new GraphPoint(child.getConfiguration(), child.getScience()*4000, child.getCost()/12.0, xMin, xMax, yMin, yMax);
		graphDisplayComponent.addGeneratedGraphPoint(gp);
		logger.info("AGENT_EVENT" + " " + child.getConfiguration().getBinaryString() + " " + child.getScience() + " " + child.getCost());
		currentPopulation.sort(new Comparator<Individual>() {
			public int compare(Individual i1, Individual i2) {
				if(i1.getFitness()>i2.getFitness())
					return 1;
				else if(i1.getFitness()<i2.getFitness())
					return -1;
				return 0;
			}
		});
		System.out.println("");
		for (Individual i : currentPopulation) {
			System.out.println(i.config.getBinaryString() + " Fitness: " + i.fitness);
		}
		System.out.println("");
	}
	
	/*
	private Individual mate(Individual i1, Individual i2) {
		int cross_point = (int)(Math.random()*59)+1;
		System.out.println("Cross Point: " + cross_point);
		System.out.println("Indiv1: " + i1.config.getBinaryString());
		System.out.println("Indiv2: " + i2.config.getBinaryString());
		int mutate = (int)(Math.random()*100);
		String child_serial = i1.getConfiguration().getBinaryString().substring(0, cross_point);
		child_serial += i2.getConfiguration().getBinaryString().substring(cross_point);
		if(mutate < 5) {
			int mutation = (int)(Math.random()*60);
			System.out.println("Mutation: " + mutation);
			String mutated_child = child_serial.substring(0, mutation);
			if(child_serial.charAt(mutation) == '0')
				mutated_child += 1;
			else
				mutated_child += 0;
			if(mutation != child_serial.length()-1);
				mutated_child += child_serial.substring(mutation+1);
			return new Individual(mutated_child);
		}
		return new Individual(child_serial);
	}	*/
	
	private String crossover(Individual i1, Individual i2) {
		int cross_point = (int)(Math.random()*59)+1;
		System.out.println("Cross Point: " + cross_point);
		System.out.println("Indiv1: " + i1.config.getBinaryString());
		System.out.println("Indiv2: " + i2.config.getBinaryString());
		int mutate = (int)(Math.random()*100);
		String child_serial = i1.getConfiguration().getBinaryString().substring(0, cross_point);
		child_serial += i2.getConfiguration().getBinaryString().substring(cross_point);
		if(mutate < 5) {
			int mutation = (int)(Math.random()*60);
			System.out.println("Mutation: " + mutation);
			String mutated_child = child_serial.substring(0, mutation);
			if(child_serial.charAt(mutation) == '0')
				mutated_child += 1;
			else
				mutated_child += 0;
			if(mutation != child_serial.length()-1);
				mutated_child += child_serial.substring(mutation+1);
			return mutated_child;
		}
		return child_serial;
	}
	
	public class Individual {
		private Configuration config;
		private double science;
		private double cost;
		private double fitness;
		
		public Individual(Configuration c, double science, double cost) {
			config = c;
			this.science = science;
			this.cost = cost;
			fitness = science/cost;
		}
		
		public Individual(String serial) {
			config = new Configuration(serial);
			ArrayList<String> inputArch = new ArrayList<String>(Arrays.asList(config.getConfig()));
			try{
	        	Architecture architecture = AG.defineNewArch(inputArch);
	            // Evaluate the architecture
	            Result result = AE.evaluateArchitecture(architecture,"Slow");
	            // Save the score and the cost
	            cost = result.getCost();
	            science = result.getScience();
	            System.out.println("Indiv3: " + serial);
	        }
	        catch(ArrayIndexOutOfBoundsException e) {
	        	System.out.println("FAILED");
	        	cost = 0;
	        	science = 0;
	        }
			if (cost != 0)
				//fitness = science/cost;
				fitness = science/.293+1-(cost/10000.0);
			else
				fitness = 0;
			
			System.out.println("Fitness: " + fitness);
		}
		
		public Configuration getConfiguration() {
			return config;
		}
		
		public double getFitness() {
			return fitness;
		}
		
		public double getScience() {
			return science;
		}
		
		public double getCost() {
			return cost;
		}
	}

}
