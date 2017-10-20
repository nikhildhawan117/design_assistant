package designAssistantCore;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.*;
import TUIO.*;
import geneticAlgorithm.DARunner;
import rbsa.eoss.Architecture;
import rbsa.eoss.ArchitectureEvaluator;
import rbsa.eoss.ArchitectureGenerator;
import rbsa.eoss.Result;
import rbsa.eoss.ResultManager;
import rbsa.eoss.local.Params;

public class DesignAssistant {
	
	private int xMin = 85;
	private int xMax = 1285+100; //4000x (0,0.281) 100px margin of error (these bounds are estimated on the pre-data)
	private int yMin = 25;
	private int yMax = 825+50; //(1/12)x (0,9981), with a 50px margin of error (some of the points were above the plot)
	private final double xScale = 4000;
	private final double yScale = 1/12.0;
	private int table_width = 1400;//default value for width and height, reset in constructor of design assistant
	private int table_height = 800;
	private final int table_width_offset = 410;
	private final int graph_width = yMax+75;
	private final int graph_height = xMax+75;
	private final int numOrbits = 5;

	private final String preDataFile = "./EOSS_data.csv";
	public static final String CLICK_EVENT = "CLICK_EVENT";
	public static final String EXPLORE_EVENT = "EXPLORE_EVENT";
	public static final String FILTER_EVENT = "FILTER_EVENT";
	public static final String AGENT_EVENT = "AGENT_EVENT";
	public static final String CLEAR_EVENT = "CLEAR_EVENT";
	public static final String TREATMENT_1 = "TREATMENT_1";
	public static final String TREATMENT_2 = "TREATMENT_2";
	public static final String TREATMENT_3 = "TREATMENT_3";
	public static final String TREATMENT_4 = "TREATMENT_4";
	public static final String TREATMENT_TRIAL = "TREATMENT_TRIAL";
	public static final String logDelimiter = ",";
	private ArchitectureGenerator AG;
	private ArchitectureEvaluator AE;
	private DARunner gaRunner;
	private ArchitectureGenerator agentAG;
	private ArchitectureEvaluator agentAE;
	
	private Hashtable<Long,TuioBlock> blockList;
	private TableComponent tableDisplayComponent;
	private GraphComponent graphDisplayComponent;
	private TuioBlockListener blockListener;
	private JFrame tableFrame;
	private JFrame graphFrame;
	private GraphicsDevice device;
	private Configuration currentConfig;
	private Configuration prevConfig;
	private Configuration prevCalcConfig;
	//the current Configuration used by the agent to calculate local points
	private Configuration currentReferenceConfig;
	private GraphPoint currentGP;
	private char[] currentCipher;
	private int orbitScrambleDist=0;
	public Logger logger;
	private FileHandler fileHandler;
	
	public boolean t1 = true;
	public boolean t2;
	public boolean t3;
	public boolean t4;
	public boolean t5;
	public boolean tt; //trial treatment
	public boolean clear_screen;
	
	
	private int counter;
	private final int counter_threshold = 3;
	
	private Thread curT;
	
	public DesignAssistant() {
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tQ,%4$s,%5$s%n");
		table_width = (int)gs[0].getDefaultConfiguration().getBounds().getWidth()-table_width_offset;
		table_height = (int)gs[0].getDefaultConfiguration().getBounds().getHeight();
		logger = Logger.getLogger("Design Assistant User Study File Log");
		//remove console handler
		logger.setUseParentHandlers(false);
		try {
			fileHandler = new FileHandler("./user_filelog_"+System.currentTimeMillis()+".log");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.addHandler(fileHandler);
		
		SimpleFormatter formatter = new SimpleFormatter();
		fileHandler.setFormatter(formatter);
		//initialize static variables
		Configuration.orbit_space_width = (int)(table_width*0.7);
		Configuration.orbit_space_height = table_height;
		Configuration.numOrbits = numOrbits;
		Configuration.numInstruments = 12;
		Configuration.clusterWidthThreshold = 500; 
		
		
		
		blockList = new Hashtable<Long, TuioBlock>();
		currentConfig = new Configuration();
		prevConfig = new Configuration();
		prevCalcConfig = new Configuration();
		currentReferenceConfig = new Configuration();
		makeCipher(0);
		initRBSAEOSS();
		setupTableWindow(gs[0]);
		if(gs.length > 1)
			setupGraphWindow(gs[1]);
		else
			setupGraphWindow(gs[0]);
		

		
		//getInitialData(preDataFile);
		//System.out.println("Generating Random Data...");
		//generateRandomData();
		//Set up initial filter
		Filter.applyFilter(graphDisplayComponent.getAllGraphPoints(), currentConfig);
		blockListener = new TuioBlockListener(blockList, currentConfig, prevConfig);
		
		showTableWindow();
		showGraphWindow();
		
	}
	
	public void makeCipher(int shift) {
		currentCipher = new char[12];
		for (int i = 0; i < 12; i++) {
			currentCipher[i] = (char)('A' + (i + shift)%12);
		}
	}
	
	
	public TuioListener getTuioListener() {
		return blockListener;
	}
	
	public void setupTableWindow(GraphicsDevice gd) {
		tableFrame = new JFrame(gd.getDefaultConfiguration());
		tableFrame.setUndecorated(true);
		Rectangle gcBounds = gd.getDefaultConfiguration().getBounds();
		
		
		tableFrame.setTitle("Block Space");
		tableFrame.setResizable(false);
		Insets insets = tableFrame.getInsets();		
		tableFrame.setSize(gcBounds.width, gcBounds.height);
	
		tableFrame.setCursor(Cursor.getDefaultCursor());
		gd.setFullScreenWindow(tableFrame);
		tableFrame.setLocation(gcBounds.x+table_width_offset, gcBounds.y);
		tableFrame.addWindowListener(
				new WindowAdapter() { 
					public void windowClosing(WindowEvent evt) {
						System.exit(0);
					} 
				});
		tableDisplayComponent = new TableComponent(blockList);
		
		tableDisplayComponent.setSize(table_width,table_height);
		tableFrame.add(tableDisplayComponent);
			
	}

	public void showTableWindow() {
		tableFrame.setVisible(true);
		tableFrame.repaint();
	}
	
	public void setupGraphWindow(GraphicsDevice gd) {
		//TODO
		graphFrame = new JFrame(gd.getDefaultConfiguration());
		Rectangle gcBounds = gd.getDefaultConfiguration().getBounds();
		//xMax = gcBounds.x;
		//yMax = gcBounds.y;
		graphFrame.setTitle("Cost vs Science Benefit Plot");
		//graphFrame.setBounds(5, 5, graph_width+75, graph_height);
		graphFrame.setLocation(50+gcBounds.x, 5+gcBounds.y);
		graphFrame.setSize(1600, yMax+100);
		graphFrame.setResizable(true);
		graphFrame.getContentPane().setBackground(Color.WHITE);
		
		JButton btn1 = new JButton("User Exploration");
		JButton btn2 = new JButton("Computer Exploration");
		JButton btn3 = new JButton("Collaborative Exploration");
		JButton btn4 = new JButton("Demo Treatment");
		JButton btn5 = new JButton("Clear Screen");
		
		btn1.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
				makeCipher(0);
				orbitScrambleDist = 0;
				t1 = true;
				GraphPoint.t1 = true;
				t2 = false;
				t3 = false;
				t4 = false;
				clear_screen = false;
				logger.info(TREATMENT_1 + logDelimiter + currentConfig.getBinaryString() + logDelimiter + "N/A" + logDelimiter + "N/A");
				//System.out.println("1");
			}
		});
		
		btn2.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
				makeCipher(0);
				orbitScrambleDist = 0;
				t1 = false;
				//GraphPoint.t1 = false;
				t2 = true;
				t3 = false;
				t4 = false;
				clear_screen = false;
				logger.info(TREATMENT_2 + logDelimiter + currentConfig.getBinaryString() + logDelimiter + "N/A" + logDelimiter + "N/A");
				//System.out.println("2");
			}
		});
		btn3.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
				makeCipher(6);
				orbitScrambleDist = 1;
				t1 = false;
				//GraphPoint.t1 = false;
				t2 = false;
				t3 = true;
				t4 = false;
				clear_screen = false;
				logger.info(TREATMENT_3 + logDelimiter + currentConfig.getBinaryString() + logDelimiter + "N/A" + logDelimiter + "N/A");
				//System.out.println("3");
			}
		});
		btn4.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
				makeCipher(9);
				orbitScrambleDist = 2;
				t1 = false;
				//GraphPoint.t1 = false;
				t2 = false;
				t3 = false;
				t4 = true;
				clear_screen = false;
				logger.info("DEMO_TREATMENT" + logDelimiter + currentConfig.getBinaryString() + logDelimiter + "N/A" + logDelimiter + "N/A");
				//System.out.println("4");
			}
		});
		
		btn5.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
				clear_screen = true;
				graphDisplayComponent.removeAllGraphPoints();
				if (gaRunner != null) {
					gaRunner.terminate();
					gaRunner = null;
				}
				logger.info("CLEAR_SCREEN" + logDelimiter + currentConfig.getBinaryString() + logDelimiter + "N/A" + logDelimiter + "N/A");
				//System.out.println("1");
			}
		});
	
		
		graphFrame.add(btn1);
		graphFrame.add(btn2);
		graphFrame.add(btn3);
		graphFrame.add(btn4);
		graphFrame.add(btn5);
		
		btn1.setBounds(xMax, yMin, 150, 30);
		btn2.setBounds(xMax, yMin+50, 150, 30);
		btn3.setBounds(xMax, yMin+100, 150, 30);
		btn4.setBounds(xMax, yMin+150, 150, 30);
		btn5.setBounds(xMax, yMin+200, 150, 30);
		
		graphFrame.addWindowListener(
				new WindowAdapter() { 
					public void windowClosing(WindowEvent evt) {
						System.exit(0);
					} 
				});
		graphDisplayComponent = new GraphComponent(blockList, xMin, xMax, yMin, yMax, xScale, yScale);
		graphFrame.add(graphDisplayComponent);
		graphDisplayComponent.addMouseListener(new PointMouseAdapter(graphDisplayComponent, tableDisplayComponent));
	}

	public void showGraphWindow() {
		//TODO
		graphFrame.setVisible(true);
		graphFrame.repaint();
	}
	
	//Potentially delete?
	public void destroyWindow(JFrame frame) {
		frame.setVisible(false);
		frame = null;
	}
	
	public void exec() {
		
		while(true) {
			synchronized(blockListener) {
				while(!blockListener.getUpdateFlag()) {
					try {
						blockListener.wait();
					} 
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			currentConfig = blockListener.getCurrentConfig();
			prevConfig = blockListener.getPrevConfig();
			
			//Will not terminate any intelligent Agent threads, need to fix this
			if(currentConfig.getPhysicalButtons().contains(new StringBuilder("O")) && !prevConfig.getPhysicalButtons().contains(new StringBuilder("O"))) {
				logger.info(CLEAR_EVENT + logDelimiter + currentConfig.getBinaryString() + logDelimiter + 0 + logDelimiter + 0);
			}
	
			DesignAssistant thisAssistant = this;
			
			//ensures that calculation and filtering is only done when a new
			//configuration is created
			if (!currentConfig.equals(prevConfig)) {
				Filter.applyFilter(graphDisplayComponent.getAllGraphPoints(), currentConfig);	
				Configuration nextPointConfig = new Configuration(blockList);
				if(!t2 && graphDisplayComponent.getMode()=="Exploration" || clear_screen) {
					
					curT = new Thread(new Runnable() {
						public void run() {
							try {
								//deep copy to be thread safe
								Configuration c = new Configuration(currentConfig.getBinaryString());
								double[] point = thisAssistant.evaluateArchitecture(c);
								
								//System.out.println("Science: " + point[0] + " Cost: " + point[1]);
								if(!currentConfig.equals(prevCalcConfig)) {
								thisAssistant.prevCalcConfig = c;
								GraphPoint nextPoint = new GraphPoint(nextPointConfig, point[0]*4000, point[1]/12.0, xMin, xMax, yMin, yMax, graphDisplayComponent.numUserPts);
								graphDisplayComponent.addGraphPoint(nextPoint);
								currentGP = nextPoint;
							
								String configString = nextPoint.getConfig().getBinaryString();
								double science = nextPoint.x_dim/4000;
								double cost = nextPoint.y_dim*12;
								logger.info(EXPLORE_EVENT + logDelimiter + configString + logDelimiter + science + logDelimiter + cost);
								}
							} catch(NullPointerException ce) {
								return;
							}
						}
					});
					curT.start();
							
				}
				
				else {
					logger.info(FILTER_EVENT + logDelimiter + currentConfig.getBinaryString() + logDelimiter + -1 + logDelimiter + -1);
				}
			}
			
			//ensures local configs are only calculated on most recently explored point
			//such that the most recently explored point was not already 
			//explored by the agent and such that the agent is not 
			//currently exploring a point
			//if(!currentConfig.equals(currentReferenceConfig) && !CollaborativeAgent.agentLock) {
			if(!CollaborativeAgent.agentLock) {
				//System.out.println("AGENT1");
				//currentReferenceConfig = currentConfig;
				//ensures the agent is not exploring a point defined by the empty configuration
				//this can be removed if desired
				if((t3) && !currentConfig.equals(new Configuration()) && !clear_screen) {
					//System.out.println("AGENT2");
					CollaborativeAgent.agentLock = true;
					new Thread(new Runnable() {
						public void run() {
							try{
								//System.out.println("THREADSTART");
								String[] agentConfigs = CollaborativeAgent.getLocalConfig(currentConfig.getBinaryOneHot());
								for(int i = 0; i < agentConfigs.length && !clear_screen; i++) {
									Configuration agentConfiguration = new Configuration(agentConfigs[i]);
									double[] point = thisAssistant.evaluateAgentArchitecture(agentConfiguration);
									GraphPoint agentPoint = new GraphPoint(agentConfiguration, point[0]*4000, point[1]/12.0, xMin, xMax, yMin, yMax);
									agentPoint.fromAgent = true;
									graphDisplayComponent.addGeneratedGraphPoint(agentPoint);
									String configString = agentPoint.getConfig().getBinaryString();
									double science = agentPoint.x_dim/4000;
									double cost = agentPoint.y_dim*12;
									logger.info(AGENT_EVENT + logDelimiter + configString + logDelimiter + science + logDelimiter + cost);
								}
								
								CollaborativeAgent.agentLock = false;
							} catch(Exception e) {
								CollaborativeAgent.agentLock = false;
							}
						}
					}, "point_calculation").start();
				}

				else if(t2 && !clear_screen){
					CollaborativeAgent.agentLock = true;
					new Thread(new Runnable() {
						public void run() {
							//run the GA--the constructor runs it
							DARunner gaRunner = new DARunner(thisAssistant);
							gaRunner.init();
							//need to give up the lock at some point
						}
					}, "point_calculation2").start();
					
				}
				
			}
			
			
			tableDisplayComponent.setConfigs(currentConfig, prevConfig);
			graphDisplayComponent.setConfigs(currentConfig, prevConfig);
			graphDisplayComponent.currentGP = currentGP;
			tableDisplayComponent.repaint();
			graphDisplayComponent.repaint();
			
			synchronized(blockListener) {
				blockListener.setUpdateFlag(false);
			}

		}
		
	}
	
	public void initRBSAEOSS() { 
        // Set a path to the project folder
        String path = "/Users/designassistant/Documents/workspace/design_assistant_HRC2/RBSAEOSS-Eval";
        //path = "/Users/designassistant/Documents/workspace/design_assistant_HRC2/RBSAEOSS-Eval";
        //path = "/Users/mvl24/Documents/workspace/design_assistant_HRC2/RBSAEOSS-Eval2";
        path = "/Users/Nikhil/Desktop/git_repo/RBSAEOSS-Eval-Copy";
        AE = ArchitectureEvaluator.getInstance();
      	AG = ArchitectureGenerator.getInstance();
        
        agentAE = ArchitectureEvaluator.getInstance();
        agentAG = ArchitectureGenerator.getInstance();
        Params params = null;
        String search_clps = "";
        params = new Params(path, "FUZZY-ATTRIBUTES", "test","normal",search_clps);//FUZZY or CRISP
        AE.init(4);
       // agentAE.init(1);
	}
	
	/*
	 * returns [science, cost] given a Configuration
	 */
	public double[] evaluateArchitecture(Configuration config) {
		
		ArrayList<String> inputArch = new ArrayList<String>(Arrays.asList(config.getConfig()));
       
		for(int i = 0; i < inputArch.size(); i++) {
			String replacementString = "";
			for (int j = 0; j < inputArch.get(i).length(); j++) {
				char replacementChar = currentCipher[inputArch.get(i).charAt(j)-'A'];
				replacementString += replacementChar;
			}
			inputArch.set(i, replacementString);
		}
		//scramble the orbits (by shifting for now)
		//note that by shifting instruments down one orbit, I'm shifting the frame of reference up one orbit
		//thus with dist=1, A in orbit 2 before is equivalent to A in orbit 1 after scramble
		Collections.rotate(inputArch, orbitScrambleDist);
		
		try{
			
			
        	Architecture architecture = AG.defineNewArch(inputArch);
            // Evaluate the architecture
            Result result = AE.evaluateArchitecture(architecture,"Slow");
            
            // Save the score and the cost
            double cost = result.getCost();
            double science = result.getScience();
            
            for(int i = 0; i < inputArch.size(); i++) {
				//System.out.println(inputArch.get(i));
			}
			//System.out.println("DONE");

    		return new double[] {science, cost};
        }
        catch(ArrayIndexOutOfBoundsException e) {
        	return new double[] {0, 0};
        }

	}
	
	public double[] evaluateAgentArchitecture(Configuration config) {
		
		ArrayList<String> inputArch = new ArrayList<String>(Arrays.asList(config.getConfig()));
       
		for(int i = 0; i < inputArch.size(); i++) {
			String replacementString = "";
			for (int j = 0; j < inputArch.get(i).length(); j++) {
				char replacementChar = currentCipher[inputArch.get(i).charAt(j)-'A'];
				replacementString += replacementChar;
			}
			inputArch.set(i, replacementString);
		}
		//scramble the orbits
		Collections.rotate(inputArch, orbitScrambleDist);
		
		try{
			
			
        	Architecture architecture = agentAG.defineNewArch(inputArch);
            // Evaluate the architecture
            Result result = agentAE.evaluateAgentArchitecture(architecture,"Slow");
            
            // Save the score and the cost
            double cost = result.getCost();
            double science = result.getScience();
            
            for(int i = 0; i < inputArch.size(); i++) {
				//System.out.println(inputArch.get(i));
			}
			//System.out.println("DONE");

    		return new double[] {science, cost};
        }
        catch(ArrayIndexOutOfBoundsException e) {
        	return new double[] {0, 0};
        }

	}
	
	private void generateRandomData() {
		makeCipher(3);
		Configuration startConfig = new Configuration();
		for(int i = 0; i < 100; i++) {
			String[] randomOneHots = CollaborativeAgent.getLocalConfig(startConfig.getBinaryOneHot());
			for(int j = 0; j < randomOneHots.length; j++) {
				double[] info = evaluateArchitecture(new Configuration(randomOneHots[j]));
				System.out.println(randomOneHots[j] + "," + info[0] + "," + info[1]);
			}
			startConfig =  new Configuration(randomOneHots[randomOneHots.length-1]);
		}
		makeCipher(0);
	}
	
	private void getInitialData(String filename){
		String row = "";
		BufferedReader br = null;
	    try {
	           br = new BufferedReader(new FileReader(filename));
	           row = br.readLine(); //strip column headers
	            while ((row = br.readLine()) != null) {
	                String[] rawInstance = row.split(",");
	                double[] dataPair = {Double.parseDouble(rawInstance[1])*4000,Double.parseDouble(rawInstance[2])/12};
	                Configuration config = new Configuration(rawInstance[0]);
	                GraphPoint point = new GraphPoint(config, dataPair[0], dataPair[1], xMin, xMax, yMin, yMax, true);
	                graphDisplayComponent.addGeneratedGraphPoint(point);
	            }

	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (br != null) {
	                try {
	                    br.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	}
	
public static void main(String argv[]) {
	
		DesignAssistant orbitDesignAssistant = new DesignAssistant();
		TuioClient client = null;
 
		switch (argv.length) {
			case 1:
				try { 
					client = new TuioClient(Integer.parseInt(argv[0])); 
				} catch (Exception e) {
					System.out.println("usage: java TuioDemo [port]");
					System.exit(0);
				}
				break;
			case 0:
				client = new TuioClient();
				break;
			default: 
				System.out.println("usage: java TuioDemo [port]");
				System.exit(0);
				break;
		}
		
		if (client!=null) {
			client.addTuioListener(orbitDesignAssistant.getTuioListener());
			client.connect();
		} 
		
		else {
			System.out.println("usage: java TuioDemo [port]");
			System.exit(0);
		}
		
		//Main Loop
		orbitDesignAssistant.exec();
		
		
	}

	public void addGraphPoint(Configuration configuration, double science, double cost){
		GraphPoint agentPoint = new GraphPoint(configuration, science*4000, cost/12.0, xMin, xMax, yMin, yMax);
	 	agentPoint.fromAgent = true;
	 	graphDisplayComponent.addGeneratedGraphPoint(agentPoint);
	}

}

/*
 * LOCAL SEARCH CODE
else if(t4) {
	//System.out.println("AGENT2");
	CollaborativeAgent.agentLock = true;
	new Thread(new Runnable() {
		public void run() {
			try{
				System.out.println("THREADSTART");
				String[] agentConfigs = CollaborativeAgent.getLocalConfig(currentReferenceConfig.getBinaryOneHot());
				double max_ratio = 0;
				for(int i = 0; i < agentConfigs.length; i++) {
					Configuration agentConfiguration = new Configuration(agentConfigs[i]);
					double[] point = thisAssistant.evaluateAgentArchitecture(agentConfiguration);
					GraphPoint agentPoint = new GraphPoint(agentConfiguration, point[0]*4000, point[1]/12.0, xMin, xMax, yMin, yMax);
					agentPoint.fromAgent = true;
					graphDisplayComponent.addGraphPoint(agentPoint);
					String configString = agentPoint.getConfig().getBinaryString();
					double science = agentPoint.x_dim/4000;
					double cost = agentPoint.y_dim*12;
					if(cost != 0 && science/cost > max_ratio) {
						max_ratio = science/cost;
						currentReferenceConfig = agentConfiguration;
					}
					logger.info(AGENT_EVENT + logDelimiter + configString + logDelimiter + science + logDelimiter + cost);
					double jump = Math.random();
					if(jump<0.1){
						int jumpIndex = (int)Math.random()*agentConfigs.length;
						currentReferenceConfig = new Configuration(agentConfigs[jumpIndex]);
					}
				}
				System.out.println("THREADEND");
				CollaborativeAgent.agentLock = false;
			} catch(Exception e) {
				CollaborativeAgent.agentLock = false;
			}
		}
	}, "point_calculation").start();
} 
*
*/

