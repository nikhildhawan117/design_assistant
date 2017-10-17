package designAssistantCore;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class Cluster {
		Set<TuioBlock> blockSet;
		double distanceThreshold;
		
		public Cluster(TuioBlock firstObject, double clusterWidthThreshold){
			this.distanceThreshold = clusterWidthThreshold;
			this.blockSet = new HashSet<TuioBlock>();
			this.blockSet.add(firstObject);
		}
		
		public long toBinaryOneHot() {
			long oneHotRepr = 0;
			for(TuioBlock e : blockSet){
				oneHotRepr |= 1<<(Configuration.numInstruments-(e.getSymbolID()%12+1)); 
			}
			return oneHotRepr;
		}
		public void mergeCluster(Cluster newCluster){
			this.blockSet.addAll(newCluster.blockSet);
		}
		public boolean addtoCluster(TuioBlock newObject){
			//tries to add a new block to a cluster; if it is out of range, don't add and return false
			for(TuioBlock m : blockSet){
				if(calculateDistance(m,newObject)<=distanceThreshold){
					this.blockSet.add(newObject);
					return true;
				}
			}
			
			return false;
		}
		
		private double calculateDistance(TuioBlock a, TuioBlock b){
			return Math.sqrt(Math.pow(a.x_pos-b.x_pos,2)+Math.pow(a.y_pos-b.y_pos, 2));
		}
		
		public Iterable<Point2D> getClusterHull(int width, int height){
			
			ArrayList<Point2D> points = new ArrayList<Point2D>();
			for (TuioBlock marker : blockSet) {
				PathIterator pi = marker.getPathItr(width,height);
				//ArrayList<TuioPoint> path = marker.getPath();
				double[] coords = new double[6];
				for (PathIterator i = pi; !i.isDone();i.next()) {
					    if (i.currentSegment(coords) != PathIterator.SEG_CLOSE) {
								      points.add(new Point2D(coords[0], coords[1]));
					    }
				}
				
	        }
	        GrahamScan graham = new GrahamScan(points.toArray(new Point2D[0]));
	        return graham.hull();
		}
	}