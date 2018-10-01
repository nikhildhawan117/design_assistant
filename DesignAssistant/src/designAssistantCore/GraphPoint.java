package designAssistantCore;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import TUIO.*;

public class GraphPoint extends JComponent{
	
	//Configuration associated with this graph point
	private Configuration config;
	public static boolean t1 = true;
	public static boolean t2;
	public static boolean t3;
	public static boolean t4;
	//the x and y dimensions associated with the graph point's configuration
	double x_dim;
	double y_dim;
	
	private int xMin;
	private int xMax;
	private int yMin;
	private int yMax;
	private int xPlot;
	private int yPlot;
	private int diameter;
	public int index;
	
	public boolean isSelected;
	public boolean isCurrPoint;
	public boolean isPrevPoint;
	public boolean isInFilter;
	public boolean isPreData;
	public boolean fromAgent;
	
	public GraphPoint(Configuration config, double x_dim, double y_dim, int xMin, int xMax, int yMin, int yMax) {
		diameter = 6;
		this.config = config;
		this.x_dim = x_dim;
		this.y_dim = y_dim;
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.xPlot = (int)x_dim+xMin-diameter/2;
		this.yPlot = yMax-(int)y_dim-diameter/2;
		isCurrPoint = true;
		isSelected = false;
		isPrevPoint = false;
		isInFilter = false;
		isPreData = false;
		fromAgent = false;
		
	}
	
	public GraphPoint(Configuration config, double x_dim, double y_dim, int xMin, int xMax, int yMin, int yMax, int index) {
		diameter = 6;
		this.config = config;
		this.x_dim = x_dim;
		this.y_dim = y_dim;
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.xPlot = (int)x_dim+xMin-diameter/2;
		this.yPlot = yMax-(int)y_dim-diameter/2;
		isCurrPoint = true;
		isSelected = false;
		isPrevPoint = false;
		isInFilter = false;
		isPreData = false;
		fromAgent = false;
		this.index = index;
		
	}
	
	public GraphPoint(Configuration config, double x_dim, double y_dim, int xMin, int xMax, int yMin, int yMax, boolean isPreData) {
		diameter = 4;
		this.config = config;
		this.x_dim = x_dim;
		this.y_dim = y_dim;
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.xPlot = (int)x_dim+xMin-diameter/2;
		this.yPlot = yMax-(int)y_dim-diameter/2;
		isCurrPoint = false;
		isSelected = false;
		isPrevPoint = false;
		isInFilter = false;
		fromAgent = false;
		this.isPreData = isPreData;
	}
	
	public void paint(Graphics g) {
		if(isPreData || fromAgent) return; //dont plot nonuser points
		int size = diameter;
		//g.setColor(Color.getHSBColor(0.75f,(float)(index)/GraphComponent.numUserPts, 1.0f));
		//user-generated points
		g.setColor(Color.getHSBColor(0.75f,1.0f, 1.0f));
		g.setColor(Color.getHSBColor(0.21f, 0.18f, 0.54f));
		//initial points(none by default)
		if(isPreData){ 
			g.setColor(Color.lightGray);
			g.setColor(Color.getHSBColor(0.75f,1.0f, 1.0f));
		}
		//computer-generated points (agent)		
		if(fromAgent){
			g.setColor(Color.lightGray);
			g.setColor(Color.getHSBColor(0.79f,0.30f, 0.54f));
		}
		//if(isInFilter) 
			//g.setColor(Color.blue.brighter());
		//current point on the table
		if(isCurrPoint) 
			g.setColor(Color.getHSBColor(0.25f, 1.0f, 0.78f));
			size = diameter;
		//previous human-explored point
		if(isPrevPoint) {
			g.setColor(Color.getHSBColor(0.26f,0.82f, 0.45f));
			
		}
		
			
		
		//Draw the graph point
		
		g.fillOval(xPlot-2, yPlot-2, size+4,size+4);
		
		if(isSelected) {
			g.setColor(Color.blue);
			g.drawOval(xPlot-8, yPlot-8, size+16, size+16);
		}	
	}
	
	public Configuration getConfig() {
		return config;
	}
	
	public int[] getBoundaries() {
		int[] bounds = {xPlot-2,yPlot-2,diameter+4};  
		return bounds;
	}
	


}
