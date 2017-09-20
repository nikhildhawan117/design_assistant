import rbsa.eoss.Architecture;
import rbsa.eoss.ArchitectureEvaluator;
import rbsa.eoss.ArchitectureGenerator;
import rbsa.eoss.Result;
import rbsa.eoss.ResultManager;
import rbsa.eoss.local.Params;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GeneticCollaborativeAgent {
	public ArchitectureGenerator AG;
	public ArchitectureEvaluator AE;
	private ArrayList<Double> genFitness;
	private PriorityQueue<Individual> currentPopulation;
	private int popSize;
	private int generation;
	private int xMin = 85;
	private int xMax = 1285+100; //4000x (0,0.281) 100px margin of error (these bounds are estimated on the pre-data)
	private int yMin = 25;
	private int yMax = 825+50; //(1/12)x (0,9981), with a 50px margin of error (some of the points were above the plot)
	
	public GeneticCollaborativeAgent(ArchitectureGenerator AG, ArchitectureEvaluator AE, int size) {
		this.AE = AE;
		this.AG = AG;
		this.genFitness = new ArrayList<Double>();
		this.popSize = size;
		this.generation = 0;
		currentPopulation = new PriorityQueue<Individual>(size, new Comparator<Individual>() {
			public int compare(Individual i1, Individual i2) {
				if(i1.getFitness()>i2.getFitness())
					return 1;
				else if(i1.getFitness()<i2.getFitness())
					return -1;
				return 0;
			}
		});
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
			String serial = "";
			for (int j = 0; j < 35; j++)
				serial+=0;
			for (int j = 0; j < 25; j++) {
				int rand_bit = (int)(Math.random()*2);
				serial += rand_bit;
			}
			Individual randomIndividual = new Individual(serial);
			currentPopulation.add(randomIndividual);
		}
		setCurrentAverageFitness();
		generation++;
	}
	
	private void setCurrentAverageFitness() {
		Individual[] individuals = new Individual[currentPopulation.size()];
		currentPopulation.toArray(individuals);
		double totalFitness = 0;
		for (int i = 0; i < individuals.length; i++) {
			totalFitness += individuals[i].fitness;
			
		}	
		genFitness.add(totalFitness/popSize);
	}
	
	public void getNextGen() {
		while (currentPopulation.size() > popSize/2) {
			currentPopulation.remove();
		}
		Individual[] is = new Individual[currentPopulation.size()];
		currentPopulation.toArray(is);
		ArrayList<Individual> individuals = new ArrayList<Individual>(Arrays.asList(is));
		
		while (!individuals.isEmpty()) {
			Individual i1 = individuals.remove((int)(Math.random()*individuals.size()));
			Individual i2 = individuals.remove((int)(Math.random()*individuals.size()));
			Individual child = mate(i1,i2);
			currentPopulation.add(child);
		}
		setCurrentAverageFitness();
		generation++;
	}
	
	private Individual mate(Individual i1, Individual i2) {
		int cross_point = (int)(Math.random()*60);
		int mutate = (int)(Math.random()*100);
		
		String child_serial = i1.getConfiguration().getBinaryString().substring(0, cross_point);
		child_serial += i2.getConfiguration().getBinaryString().substring(cross_point);
		if(mutate < 10) {
			int mutation = (int)(Math.random()*60);
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
	}
	
	public GraphPoint[] getCurrentGraphPoints() {
		Individual[] individuals = new Individual[currentPopulation.size()];
		currentPopulation.toArray(individuals);
		GraphPoint[] gps = new GraphPoint[currentPopulation.size()];
		
		for (int i = 0; i< individuals.length; i++) {
			System.out.println(individuals[i].getConfiguration().getBinaryString());
			gps[i] = new GraphPoint(individuals[i].getConfiguration(), individuals[i].getScience()*4000, individuals[i].getCost()/12.0, xMin, xMax, yMin, yMax);
			gps[i].fromAgent = true;
		}
		System.out.println();
		
		return gps;
		
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
	        	System.out.println("Evaluating");
	            Result result = AE.evaluateArchitecture(architecture,"Slow");
	            // Save the score and the cost
	            cost = result.getCost();
	            science = result.getScience();
	            System.out.println("Cost: " + cost + " Science: " + science);
	        }
	        catch(ArrayIndexOutOfBoundsException e) {
	        	System.out.println("FAILED");
	        	cost = 0;
	        	science = 0;
	        }
			if (cost != 0)
				fitness = science/cost;
			fitness = 0;
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
