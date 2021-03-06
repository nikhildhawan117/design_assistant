import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import TUIO.*;

public class TableComponent extends JComponent{
	
	//this class reads from blockList
	private Hashtable<Long,TuioBlock> blockList;
	public static int width, height;
	public static int numOrbits = 5;
	private float scale = 1.0f;
	private Color[] orbitColors = {Color.blue, Color.red};
	Configuration currentConfig;
	Configuration prevConfig;
	//invariant currentSelectedPoint must always point to the same object as in GraphComponent
	GraphPoint currentSelectedPoint = null;
	
	//sets currentConfig and prevConfig to configs based on blockList
	public TableComponent(Hashtable<Long,TuioBlock> blockList) {
		this.blockList = blockList;
		this.currentConfig = new Configuration(blockList);
		this.prevConfig = new Configuration(blockList);
	}
	
	
	
	public void setSize(int w, int h) {
		super.setSize(w, h);
		width = w;
		height = h;
		scale  = height/(float)TuioBlock.table_size;
	}
	
	public void setConfigs(Configuration currentConfig, Configuration prevConfig) {
		this.currentConfig = currentConfig;
		this.prevConfig = prevConfig;
	}
	
	public void init(Graphics g) {
		
		//get the largest possible height that is divisible by the numOrbits
		int largestHeight = this.height-(this.height%this.numOrbits);
		//we will divide this into our orbits
		int orbitHeight = largestHeight/this.numOrbits;
		int orbitWidth = (int)(width*0.7);
		Font currentFont = g.getFont();
		Font newFont = currentFont.deriveFont(currentFont.getSize() * 4F);
		g.setFont(newFont);
		
		//paint the orbits and orbit lists
		int j = 0;
		for(int i=0; i<=this.height-orbitHeight; i+= orbitHeight){
			int orbitIndex = i/orbitHeight;
			int colorIndex = orbitIndex%2;
			g.setColor(this.orbitColors[colorIndex]);
			g.fillRect(0, i, orbitWidth, orbitHeight);
			g.setColor(Color.white);
			g.drawString(currentConfig.toFancyString()[j], 10, i+orbitHeight/2+80);
			j++;
			
		}
		
		if(currentSelectedPoint != null)
			currentSelectedPoint.getConfig().paintConfig(g, orbitHeight, numOrbits);

		//Draw filtering zones
		g.setColor(Color.black);
		g.fillRect(orbitWidth, 0, width-orbitWidth, height);
		g.setColor(Color.white);
		g.drawString("Global Filters", (int)(orbitWidth+(width-orbitWidth)*0.13), height/2);
		
		/*
		for(int i = 0; i < 8; i++) {
			for(int k = 0; k < 5; k++) {
				Shape p = new Ellipse2D.Float(60+orbitWidth/6*i, orbitHeight/2+orbitHeight*k, 5, 5);
				int px = 60+orbitWidth/6*i;
				int py = orbitHeight/2+orbitHeight*k;
				//System.out.println(px + " " + py);
				((Graphics2D)g).draw(p);
			}
		}*/
		
	}
	

	
	public void paint(Graphics g) {
		update(g);
	}
	

	

	
	public void update(Graphics g) {
		//System.out.println(currentConfig.getConfig()[0]);
		init(g);
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		String[] strs = new String[9];
		
		//draw the object
		Enumeration<TuioBlock> objects = blockList.elements();
		while (objects.hasMoreElements()) {
			TuioBlock tblock = objects.nextElement();
			if (tblock!=null) tblock.paint(g2, width,height);
		}
		
		for (Cluster cluster : currentConfig.getCluster()){

			Stack<Point2D> points = (Stack<Point2D>) cluster.getClusterHull(width,height);
			GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, points.size());
			Point2D start = points.pop();
			polyline.moveTo(start.x(), start.y());
			while(!points.isEmpty()){
				Point2D point = points.pop();
				polyline.lineTo(point.x(), point.y());
			}
			g2.setColor(Color.green);
			g2.draw(polyline);
			
		}
		
		
		
		
		
	}
	
	
	
	
}
