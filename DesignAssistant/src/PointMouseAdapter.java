import java.awt.Component;
import java.awt.event.*;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import rbsa.eoss.Orbit;

public class PointMouseAdapter extends MouseAdapter {
	GraphComponent graphDisplayComponent;
	TableComponent tableDisplayComponent;
	
	public PointMouseAdapter(GraphComponent graphDisplayComponent, TableComponent tableDisplayComponent){
		this.graphDisplayComponent = graphDisplayComponent;
		this.tableDisplayComponent = tableDisplayComponent;
	}
	
	public void mouseClicked(MouseEvent e) {
	
		
		if(e.getButton()==MouseEvent.BUTTON1){
			//System.out.println(e.getX() + " " + e.getY());
			int x = e.getX();
			int y = e.getY();
			String key = String.format("%d%d", x,y);
			
			GraphPoint gp = graphDisplayComponent.pixelMap.get(key);
			
			if(gp != null){
					
				if(graphDisplayComponent.currentSelectedPoint != null)
					graphDisplayComponent.currentSelectedPoint.isSelected = false;
				
				gp.isSelected = true;
				graphDisplayComponent.currentSelectedPoint = gp;
				tableDisplayComponent.currentSelectedPoint = gp;
			}
			
			else {
				if(graphDisplayComponent.currentSelectedPoint != null)
					graphDisplayComponent.currentSelectedPoint.isSelected = false;
				graphDisplayComponent.currentSelectedPoint = null;
				tableDisplayComponent.currentSelectedPoint = null;
			}
			graphDisplayComponent.repaint();
		}
		else if(e.getButton()==MouseEvent.BUTTON3){
			graphDisplayComponent.toggleMode();
			graphDisplayComponent.repaint();
		}
	}

}
