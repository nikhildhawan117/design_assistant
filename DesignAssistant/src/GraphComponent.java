import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import TUIO.*;

//this class reads from blockList


public class GraphComponent extends JComponent{
	
	private Hashtable<Long,TuioBlock> blockList; 
	private int xMin;
	private int xMax;
	private int yMin;
	private int yMax;
	private double xScale;
	private double yScale;
	private int numTicks = 10;
	private int xTickWidth;
	private int yTickWidth;
	private String mode;
	public Configuration currentConfig;
	public Configuration prevConfig;
	private LinkedList<GraphPoint> allGraphPoints;
	public HashMap<String,GraphPoint> pixelMap = new HashMap<String,GraphPoint>();
	//invariant currentSelectedPoint must always point to the same object as in TableComponent
	public GraphPoint currentSelectedPoint = null;
	//sets currentConfig and prevConfig to orbits defined by blockList
	public GraphComponent(Hashtable<Long,TuioBlock> blockList, int xMin, int xMax, int yMin, int yMax, double xScale, double yScale) {
		this.blockList = blockList;
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMax = yMax;
		this.yMin = yMin;
		this.xScale = xScale;
		this.yScale = yScale;
		this.xTickWidth = (int)((this.xMax-this.xMin)/this.numTicks); 
		this.yTickWidth = (int)((this.yMax-this.yMin)/this.numTicks); 
		prevConfig = new Configuration(blockList);
		currentConfig = new Configuration(blockList);
		setAllGraphPoints(new LinkedList<GraphPoint>());
		explore();
	}
	


	public void setConfigs(Configuration currentConfig, Configuration prevConfig) {
		this.currentConfig = currentConfig;
		this.prevConfig = prevConfig;
	}
	
	/*
	 * Adds a graphpoint to the head of the list and
	 * and sets the previous head
	 * to prevPoint and prevPoint to a regular point.
	 * Assumes gp is constructed such that isCurrpoint is true
	 * also adds gp as a component to GraphPoints list of components
	 * also updates the pixelMap
	 * */
	public void addGraphPoint(GraphPoint gp) {
		
		add(gp);
		if(!gp.isPreData) {
			if(getAllGraphPoints().size() > 0) {
				getAllGraphPoints().get(0).isCurrPoint = false;
				getAllGraphPoints().get(0).isPrevPoint = true;
			}
		
			if(getAllGraphPoints().size() > 1) 
				getAllGraphPoints().get(1).isPrevPoint = false;
		}
		getAllGraphPoints().addFirst(gp);
		
        int [] bounds = gp.getBoundaries();
        for(int i=bounds[0]; i<=(bounds[0]+bounds[2]); i++){
        	for(int j=bounds[1]; j<=(bounds[1]+bounds[2]); j++){
        		pixelMap.put(String.format("%d%d",i,j), gp);
        	}
        }
	}
	
	/**
	 * Adds a graph point to the tail of allGraphPoints
	 * automatically sets isCurrPoint and isPrevPoint to false
	 */
	public void addGeneratedGraphPoint(GraphPoint gp) {
		gp.isCurrPoint = false;
		gp.isPrevPoint = false;
		add(gp);
		getAllGraphPoints().add(gp);
		
        int [] bounds = gp.getBoundaries();
        for(int i=bounds[0]; i<=(bounds[0]+bounds[2]); i++){
        	for(int j=bounds[1]; j<=(bounds[1]+bounds[2]); j++){
        		pixelMap.put(String.format("%d%d",i,j), gp);
        	}
        }
	}
	
	public void init(Graphics g) {


		//Draw Graph Components 
		g.setColor(Color.black);
		g.drawLine(xMin, yMin, xMin, yMax); //y axis
		g.drawLine(xMin, yMax, xMax, yMax); //x axis
		g.drawString("Science", xMax/2, yMax+40);
		g.drawString("Cost", xMin/2, yMin/2);
		g.drawString("Mode: "+ mode, xMax/2, yMin+40);
		//tick marks
		for(int xTick = xMin; xTick < xMax; xTick+=xTickWidth){
			g.drawLine(xTick, yMax+5, xTick, yMax-5);
			g.drawString(String.format("%.3f",(1/xScale)*(xTick-xMin)), xTick-20, yMax+20);
		}
		for(int yTick = yMin; yTick < yMax; yTick+=yTickWidth){
			g.drawLine(xMin+5, yTick, xMin-5, yTick);
			g.drawString(String.format("%d",(int)(1/yScale)*(yMax-yTick)),xMin-45,yTick+5);
		}
		
	}
	
	public void paint(Graphics g) {
		update(g);
	
	}
	

	
	public void update(Graphics g) {
		init(g);
		
		//these should only be GraphPoints
		Component[] allComponents = getComponents();
		for(int i = 0; i < allComponents.length; i++)
			allComponents[i].paint(g);

	}
	
	public void clear() {
		this.removeAll();
	}

	public LinkedList<GraphPoint> getAllGraphPoints() {
		return allGraphPoints;
	}
	
	private void setAllGraphPoints(LinkedList<GraphPoint> linkedList) {
		allGraphPoints = linkedList;
	}
	
	public void filter() {
		mode = "Filtering";
	}
	
	public void explore() {
		mode = "Exploration";
	}
	
	public String getMode() {
		return mode;
	}
	
	public void toggleMode() {
		if(mode.equals("Filtering"))
			explore();
		else 
			filter();
	}


}
