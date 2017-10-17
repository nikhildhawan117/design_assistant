package designAssistantCore;
import java.util.LinkedList;

public class Filter {
	
	public static void applyFilter(LinkedList<GraphPoint> allGraphPoints, Configuration currentConfig) {
		
		for(GraphPoint gp : allGraphPoints) {
			long checkOneHot = gp.getConfig().getBinaryOneHot();
			checkOneHot &= currentConfig.getBinaryOneHot();
			
			if(checkOneHot == currentConfig.getBinaryOneHot())
				gp.isInFilter = true;
			
			else
				gp.isInFilter = false;
		}	
	}
	
	public static void applyGlobalFilter(LinkedList<GraphPoint> allGraphPoints, Configuration currentConfig) {
		
	}

}
