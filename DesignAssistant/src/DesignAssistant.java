import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import TUIO.*;

import rbsa.eoss.Architecture;
import rbsa.eoss.ArchitectureEvaluator;
import rbsa.eoss.ArchitectureGenerator;
import rbsa.eoss.Result;
import rbsa.eoss.ResultManager;
import rbsa.eoss.local.Params;

public class DesignAssistant {
	
	private final int xMin = 85;
	private final int xMax = 1285+100; //4000x (0,0.281) 100px margin of error (these bounds are estimated on the pre-data)
	private final int yMin = 25;
	private final int yMax = 825+50; //(1/12)x (0,9981), with a 50px margin of error (some of the points were above the plot)
	private final double xScale = 4000;
	private final double yScale = 1/12.0;
	private final int table_width = 640;
	private final int table_height = 480;
	private final int graph_width = yMax+75;
	private final int graph_height = xMax+75;
	private final int numOrbits = 5;
	private final String preDataFile = "/Users/Nikhil/Desktop/git_repo/DesignAssistant/EOSS_data.csv";
	private ArchitectureGenerator AG;
	private ArchitectureEvaluator AE;
	
	private Hashtable<Long,TuioBlock> blockList;
	private TableComponent tableDisplayComponent;
	private GraphComponent graphDisplayComponent;
	private TuioBlockListener blockListener;
	private JFrame tableFrame;
	private JFrame graphFrame;
	private GraphicsDevice device;
	private Configuration currentConfig;
	private Configuration prevConfig;
	//the current Configuration used by the agent to calculate local points
	private Configuration currentReferenceConfig;
	
	public DesignAssistant() {
		//initialize static variables
		Configuration.orbit_space_width = (int)(table_width*0.7);
		Configuration.orbit_space_height = table_height;
		Configuration.numOrbits = numOrbits;
		Configuration.numInstruments = 12;
		Configuration.clusterWidthThreshold = 0.15; 
		
		blockList = new Hashtable<Long, TuioBlock>();
		currentConfig = new Configuration();
		prevConfig = new Configuration();
		currentReferenceConfig = new Configuration();
		tableDisplayComponent = new TableComponent(blockList);
		tableDisplayComponent.setSize(table_width,table_height);
		graphDisplayComponent = new GraphComponent(blockList, xMin, xMax, yMin, yMax, xScale, yScale);
		getInitialData(preDataFile);
		
		//Set up initial filter
		Filter.applyFilter(graphDisplayComponent.getAllGraphPoints(), currentConfig);
		blockListener = new TuioBlockListener(blockList, currentConfig, prevConfig);
		
		device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		
		initRBSAEOSS();
		setupTableWindow();
		setupGraphWindow();
		showTableWindow();
		showGraphWindow();
	}
	
	public TuioListener getTuioListener() {
		return blockListener;
	}
	
	public void setupTableWindow() {
		tableFrame = new JFrame();
		
	
	
		tableFrame.setTitle("Block Space");
		tableFrame.setResizable(false);
		Insets insets = tableFrame.getInsets();		
		tableFrame.setSize(table_width,table_height+insets.top);
		tableFrame.setCursor(Cursor.getDefaultCursor());
		
		tableFrame.addWindowListener(
				new WindowAdapter() { 
					public void windowClosing(WindowEvent evt) {
						System.exit(0);
					} 
				});
		tableFrame.add(tableDisplayComponent);
			
	}

	public void showTableWindow() {
		tableFrame.setVisible(true);
		tableFrame.repaint();
	}
	
	public void setupGraphWindow() {
		//TODO
		graphFrame = new JFrame();
		graphFrame.setTitle("Cost vs Science Benefit Plot");
		//graphFrame.setBounds(5, 5, graph_width+75, graph_height);
		graphFrame.setBounds(5, 5, 1400, yMax+100);
		graphFrame.setResizable(false);
		graphFrame.getContentPane().setBackground(Color.WHITE);
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
			
			//System.out.println("Current Config: " + currentConfig.toString());
			//System.out.println("Prev Config: " + prevConfig.toString());
			
			DesignAssistant thisAssistant = this;
			
			//ensures that calculation and filtering is only done when a new
			//configuration is created
			if(!currentConfig.equals(prevConfig) ) {
				Filter.applyFilter(graphDisplayComponent.getAllGraphPoints(), currentConfig);	
				Configuration nextPointConfig = new Configuration(blockList);
				if(graphDisplayComponent.getMode()=="Exploration") {
					new Thread(new Runnable() {
						public void run() {
							double[] point = thisAssistant.evaluateArchitecture(currentConfig);
							GraphPoint nextPoint = new GraphPoint(nextPointConfig, point[0]*4000, point[1]/12.0, xMin, xMax, yMin, yMax);
							graphDisplayComponent.addGraphPoint(nextPoint);
						
						}
					}).start();
				}	
			}
			
			//ensures local configs are only calculated on most recently explored point
			//such that the most recently explored point was not already 
			//explored by the agent and such that the agent is not 
			//currently exploring a point
			if(!currentConfig.equals(currentReferenceConfig) && !CollaborativeAgent.agentLock) {
				currentReferenceConfig = currentConfig;
				//ensures the agent is not exploring a point defined by the empty configuration
				//this can be removed if desired
				if(currentReferenceConfig.equals(new Configuration())) {
					CollaborativeAgent.agentLock = true;
					new Thread(new Runnable() {
						public void run() {
							String[] agentConfigs = CollaborativeAgent.getLocalConfig(currentConfig.getBinaryOneHot());
							for(int i = 0; i < agentConfigs.length; i++) {
								Configuration agentConfiguration = new Configuration(agentConfigs[i]);
								double[] point = thisAssistant.evaluateArchitecture(agentConfiguration);
								System.out.println("HEREHERE");
								GraphPoint agentPoint = new GraphPoint(agentConfiguration, point[0]*4000, point[1]/12.0, xMin, xMax, yMin, yMax);
								agentPoint.fromAgent = true;
								graphDisplayComponent.addGeneratedGraphPoint(agentPoint);
							}
							CollaborativeAgent.agentLock = false;
						}
					}).start();
				}
			}
			
			
			tableDisplayComponent.setConfigs(currentConfig, prevConfig);
			graphDisplayComponent.setConfigs(currentConfig, prevConfig);
			tableDisplayComponent.repaint();
			graphDisplayComponent.repaint();
			
			synchronized(blockListener) {
				blockListener.setUpdateFlag(false);
			}

		}
		
	}
	
	public void initRBSAEOSS() { 
        // Set a path to the project folder
        String path = "/Users/Nikhil/Desktop/git_repo/RBSAEOSS-Eval";
        //path = "/Users/designassistant/Documents/workspace/design_assistant_HRC2/RBSAEOSS-Eval";
        //path = "/Users/mvl24/Documents/workspace/design_assistant_HRC2/RBSAEOSS-Eval2";
        AE = ArchitectureEvaluator.getInstance();
        AG = ArchitectureGenerator.getInstance();
        Params params = null;
        String search_clps = "";
        params = new Params(path, "FUZZY-ATTRIBUTES", "test","normal",search_clps);//FUZZY or CRISP
        AE.init(1);
	}
	
	public double[] evaluateArchitecture(Configuration config) {
		
		ArrayList<String> inputArch = new ArrayList<String>(Arrays.asList(config.getConfig()));
       
		try{
		
        	Architecture architecture = AG.defineNewArch(inputArch);
            // Evaluate the architecture
            Result result = AE.evaluateArchitecture(architecture,"Slow");
            
            // Save the score and the cost
            double cost = result.getCost();
            double science = result.getScience();

    		return new double[] {science, cost};
        }
        catch(ArrayIndexOutOfBoundsException e) {
        	return new double[] {0, 0};
        }

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
	                graphDisplayComponent.addGraphPoint(point);
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
					client = new TuioClient( Integer.parseInt(argv[0])); 
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

}
