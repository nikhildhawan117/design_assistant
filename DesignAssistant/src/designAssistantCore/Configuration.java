package designAssistantCore;
import java.awt.Graphics;
import java.util.*;
import java.util.function.Function;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.*;

import TUIO.*;

public class Configuration {
	
	private String[] orbits;
	private String physicalButtons;
	private LinkedList<Cluster> clusters;
	public static int numOrbits;
	public static int orbit_space_width;
	public static int orbit_space_height;
	public static int numInstruments;
	public final static String EMPTY = "000000000000000000000000000000000000000000000000000000000000";
	public static double clusterWidthThreshold;

	public Configuration() {
		orbits = new String[numOrbits];
		physicalButtons = "";
		clusters = new LinkedList<Cluster>();
		makeEmpty();
	}
	
	public Configuration(Hashtable<Long,TuioBlock> blockList) {
		orbits = new String[numOrbits];
		physicalButtons = "";
		clusters = new LinkedList<Cluster>();
		populateOrbits(blockList);
	}
	
	public Configuration(String serial_input) {
		orbits = new String[numOrbits];
		physicalButtons = "";
		clusters = new LinkedList<Cluster>();
		populateOrbits(serial_input);
	}
	
	private void makeEmpty() {
		for(int i = 0; i < orbits.length; i++) 
			orbits[i] = "";
	}
	

	private void populateOrbits(Hashtable<Long,TuioBlock> blockList) {
		makeEmpty();
		
		int largestHeight = orbit_space_height-(orbit_space_height%orbits.length);
		//we will divide this into our orbits
		int orbitHeight = largestHeight/orbits.length;
		
		Enumeration<TuioBlock> tuioBlocks = blockList.elements();
		//Place each TuioObject into its appropriate orbit
		while (tuioBlocks.hasMoreElements()) {
			TuioBlock tblock = tuioBlocks.nextElement();
			double x = tblock.x_pos;
			double y = tblock.y_pos;
			
			
			//If the tuio block is a valid instrument 
			if(tblock.toTuioLetter().compareTo(Character.toString((char)('A' + numInstruments))) < 0) {

				//iterates through orbits until either tblock has been appropriately
				//placed in orbits or orbits has been full iterated through
				for(int i = 0; i < orbits.length; i++)  {
					if(y<orbitHeight*(i+1) && y>orbitHeight*i && x < orbit_space_width) {
						orbits[i] += tblock.toTuioLetter();
						break;
					}
				}


				//handles blocks in the global filter zone
				if(x > orbit_space_width) {
					//if no clusters yet, create the first cluster containing this block and add to the cluster list
					if(clusters.size() == 0)
						clusters.add(new Cluster(tblock, clusterWidthThreshold));
					//otherwise, check the current clusters to see if it matches any
					else{
						boolean clustered = false;
						//this is in case a single block fits in more than one clusters, in which case we merge them
						Stack<Cluster> matched = new Stack<Cluster>(); //track which clusters match, to merge
						//try adding the block to each of the current clusters
						for(Cluster c : clusters){
							//if it fits in a current cluster, 
							if(c.addtoCluster(tblock)){
								clustered = true;
								matched.push(c);
								//System.out.println("Here");
							}
						}
						//if not clustered in any current, create a new cluster
						if(!clustered){
							clusters.add(new Cluster(tblock, clusterWidthThreshold));
						}
						//otherwise, check to see if we need to merge any
						else{
							Cluster primary = matched.pop(); 
							while(!matched.isEmpty()){
								Cluster toMerge = matched.pop();
								primary.mergeCluster(toMerge);

								clusters.remove(toMerge); 
							}
						}
					}
				}//end handling global filter zone
			}//end valid checking valid instruments
			
			else {
				physicalButtons += tblock.toTuioLetter();
			}
				
		}
		
		
	}
	
	private void populateOrbits(String serial) {
		makeEmpty();
		String[] serialArray = new String[numOrbits];
		for(int i = 0; i < numOrbits; i++) {
			serialArray[i] = serial.substring(i*numInstruments, numInstruments*(i+1));
		}
		for(int i = 0; i <serialArray.length; i++) {
			for(int j = 0; j < serialArray[i].length(); j++) {
				int bit = serialArray[i].charAt(j) - '0';
				if(bit==1)
					orbits[i] += ((char)(j +'A'));
			}
		}
	}
	
	public String[] getConfig() {
		return orbits;
	}
	
	public LinkedList<Cluster> getCluster() {
		return clusters;
	}
	
	public String getGlobalInstruments() {
		return physicalButtons;
	}
	
	//make sure to sort each orbit. DOES NOT WORK RIGHT NOW
	public boolean equals(Configuration c) {
		boolean isEqual = true;
		if(c.getConfig().length != this.orbits.length)
			return false;
		else {
			for(int i = 0; i < this.orbits.length; i++)
				isEqual = isEqual && (this.orbits[i].equals(c.getConfig()[i]));
			return isEqual;
		}	
	}
	
	public String[] toFancyString() {
		String[] res = new String[orbits.length];
		for(int i = 0; i < orbits.length; i++) {
			int orbit_num = i+1;
			String name = "Orbit " + orbit_num + ": [";
			for(int j = 0; j < orbits[i].length(); j++) {
				name+= (orbits[i].substring(j, j+1)+" ");
			}
			name = name.trim() + "]";
			res[i] = name;
		}
		return res;
	}
	
	public String toString() {
		String[] resArray = toFancyString();
		String res = "";
		for(int i = 0; i < resArray.length; i++)
			res+=resArray[i] + " ";

		return res.trim();
		
	}
	
	public String getPhysicalButtons() {
		return physicalButtons;
	}
	
	public void paintConfig(Graphics g, int orbitHeight, int numOrbits) {
		//initial padding between top of orbit and first row
		int init_padding = 10;
		//padding between markers
		int between_padding = 15;
		int numRows = (orbitHeight - init_padding)/(TuioBlock.block_size+between_padding);
		int numCols = orbit_space_width/(TuioBlock.block_size+5);
		Shape s = new Rectangle2D.Float(0,0,TuioBlock.block_size,TuioBlock.block_size);
		Color originalColor = g.getColor();
		for(int i = 0; i < numOrbits; i++) {
			int x = between_padding;
			int y = i*orbitHeight+init_padding+between_padding;
			int k = 0;
			for(int j = 0; j < orbits[i].length(); j++) {
				g.setColor(Color.white);
				//x+=5;
				s = new RoundRectangle2D.Float((float)x,(float)y,(float)TuioBlock.block_size,(float)TuioBlock.block_size,(float)TuioBlock.block_size/8,(float)TuioBlock.block_size/8);
				
				Stroke oldStroke = ((Graphics2D)g).getStroke();
				((Graphics2D)g).setStroke(new BasicStroke(5F));
				((Graphics2D)g).draw(s);
				((Graphics2D)g).setStroke(oldStroke);
				//10 and 20 represent position of letter (instrument type) in square
				g.drawString(orbits[i].substring(j, j+1),x+10,y+50);
				//this is gross but we are indexing the image array by the letter
				//g.drawImage(TableComponent.symbols.get((int)orbits[i].substring(j, j+1).charAt(0)-(int)'A'),x,y, null);
				x+= (between_padding+TuioBlock.block_size);
				k++;
				if(k>numCols) {
					//blocks are separated by 5 pixels
					y+= (between_padding+TuioBlock.block_size);
					x = 0;
				}
				if(k > numCols*numRows) {
					System.out.println("ERROR"); //Create Error to throw
				}
			}
		}
		g.setColor(originalColor);
	}
	
	//Orbit 1 is on the left side 
	public long getBinaryOneHot() {
		Function<String, Long> orbitOneHot = orbit -> {
			long oneHot = 0;
			for(int i = 0; i < orbit.length(); i++) {
				int mod_id = Character.getNumericValue(orbit.charAt(i))-10;
				oneHot |= 1<<(numInstruments-(mod_id+1)); 
			}
			return oneHot;
		};
		long filterMatch = 0L;
		for(int i = 0; i < orbits.length; i++) {
			long oneHot = orbitOneHot.apply(orbits[i]);
			filterMatch |= oneHot<<((numOrbits-(i+1))*numInstruments); //simplify this! shouldn't it be <<<? causes syntax error...
		}
		
		return filterMatch;
	}
	
	public String getBinaryString() {
		Long configLong = this.getBinaryOneHot();
		String configString = String.format("%60s", Long.toBinaryString(configLong)).replace(" ", "0");
		return configString;
	}
	

}
