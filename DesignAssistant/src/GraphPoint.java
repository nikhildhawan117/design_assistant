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
		g.setColor(Color.magenta);
		
		if(isPreData) 
			g.setColor(Color.gray);
				
		if(isInFilter) 
			g.setColor(Color.blue.brighter());
		
		if(fromAgent)
			g.setColor(Color.green.darker());
		
		if(isCurrPoint) 
			g.setColor(Color.red);
		
		if(isPrevPoint) {
			g.setColor(Color.pink);
		}
		
		//Draw the graph point
		int size = diameter;
		g.fillOval(xPlot-2, yPlot-2, size+4,size+4);
		
		if(isSelected) {
			g.setColor(Color.blue);
			g.drawOval(xPlot-2, yPlot-2, size+4, size+4);
		}	
	}
	
	public Configuration getConfig() {
		return config;
	}
	
	public int[] getBoundaries() {
		int[] bounds = {xPlot,yPlot,diameter};  
		return bounds;
	}
	


}
