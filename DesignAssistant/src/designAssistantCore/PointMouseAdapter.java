package designAssistantCore;
import java.awt.Component;
import java.awt.event.*;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import rbsa.eoss.Orbit;

public class PointMouseAdapter extends MouseAdapter {
	GraphComponent graphDisplayComponent;
	TableComponent tableDisplayComponent;
	Logger logger;
	public PointMouseAdapter(GraphComponent graphDisplayComponent, TableComponent tableDisplayComponent){
		this.graphDisplayComponent = graphDisplayComponent;
		this.tableDisplayComponent = tableDisplayComponent;
		
		logger = Logger.getLogger("Design Assistant User Study File Log");
	}
	

	
	public void mouseClicked(MouseEvent e) {
	
		
		if(e.getButton()==MouseEvent.BUTTON1){
			//System.out.println(e.getX() + " " + e.getY());
			int x = e.getX();
			int y = e.getY();
			String key = String.format("%d%d", x,y);
			
			GraphPoint gp = graphDisplayComponent.pixelMap.get(key);
			
			if(gp != null){
				String configString = gp.getConfig().getBinaryString();
				double science = gp.x_dim/4000;
				double cost = gp.y_dim*12;
				logger.info(DesignAssistant.CLICK_EVENT + DesignAssistant.logDelimiter + configString + DesignAssistant.logDelimiter + science + DesignAssistant.logDelimiter + cost);
				if(graphDisplayComponent.currentSelectedPoint != null)
					graphDisplayComponent.currentSelectedPoint.isSelected = false;
				
				gp.isSelected = true;
				graphDisplayComponent.currentSelectedPoint = gp;
				tableDisplayComponent.currentSelectedPoint = gp;
			}
			
			else {
				logger.info(DesignAssistant.CLICK_EVENT + DesignAssistant.logDelimiter + Configuration.EMPTY + DesignAssistant.logDelimiter + 0 + DesignAssistant.logDelimiter + 0);
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
