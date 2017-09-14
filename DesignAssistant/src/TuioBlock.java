import java.util.*;
import java.util.function.Function;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import TUIO.*;

public class TuioBlock extends TuioObject{

	private Shape outline;
	
	//These are default value. Set values in Design Assistant.
	public static int block_size = 105;
	public static int table_size = 760;
	public static final double ax_x = -1.706548908;
	public static final double ax_y = - .07869167392;
	public static final double cx = 2091.810068;


	public static final double ay_x =  -.08770692435;
	public static final double ay_y = 1.662719449;
	public static final double cy =  - 161.0721432;
	public double x_pos;
	public double y_pos;
	
	public TuioBlock(TuioObject tobj) {
		super(tobj);
		outline = new RoundRectangle2D.Float(-block_size/2,-block_size/2,(float)TuioBlock.block_size,(float)TuioBlock.block_size,(float)TuioBlock.block_size/8,(float)TuioBlock.block_size/8);
		//outline = new Rectangle2D.Float(-block_size/2,-block_size/2,block_size,block_size);
		AffineTransform transform = new AffineTransform();
		transform.translate(xpos,ypos);
		
		transform.rotate(angle,xpos,ypos);
		outline = transform.createTransformedShape(outline);
		x_pos = outline.getBounds2D().getX();
		y_pos = outline.getBounds2D().getY();
	}
	
	public String toTuioLetter() {
		int mod_id = symbol_id % 26; 
		Function<Integer, String> num_to_str = i -> {
			String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; 
			return alphabet.substring(i, i+1);
		};
		return num_to_str.apply(mod_id);
	}
	
	
	PathIterator getPathItr(int width,int height){
		float Xpos = xpos*width;
		float Ypos = ypos*height;
		float scale = height/(float)table_size;

		AffineTransform trans = new AffineTransform();
		trans.translate(-xpos,-ypos);
		trans.translate(Xpos,Ypos);
		trans.scale(1,1);
		Shape s = trans.createTransformedShape(outline);
		double x_trans = ax_x*s.getBounds2D().getX() + ax_y*s.getBounds2D().getY() + cx;
		double y_trans = ay_x*s.getBounds2D().getX() + ay_y*s.getBounds2D().getY() + cy;
		trans = new AffineTransform();
		trans.translate(x_trans-s.getBounds2D().getX(), y_trans-s.getBounds2D().getY());
		s = trans.createTransformedShape(s);
		return s.getPathIterator(null, 1);
		
		
		
		/*
		float Xpos = xpos*width;
		float Ypos = height-ypos*height;
		float scale = height/(float)table_size;
		AffineTransform trans = new AffineTransform();
		trans.translate(-xpos,-ypos);
		trans.translate(Xpos,Ypos);
		trans.scale(scale,scale);
		Shape s = trans.createTransformedShape(outline);
		return s.getPathIterator(null, 1);*/
	}


	
	public void setCoords(double x, double y) {
		x_pos = x;
		y_pos = y;
	}
	public void paint(Graphics2D g, int width, int height) {
		
		float Xpos = xpos*width;
		float Ypos = ypos*height;
		float scale = height/(float)table_size;

		AffineTransform trans = new AffineTransform();
		trans.translate(-xpos,-ypos);
		trans.translate(Xpos,Ypos);
		trans.scale(1,1);
		Shape s = trans.createTransformedShape(outline);
		double x_trans = ax_x*s.getBounds2D().getX() + ax_y*s.getBounds2D().getY() + cx;
		double y_trans = ay_x*s.getBounds2D().getX() + ay_y*s.getBounds2D().getY() + cy;
		trans = new AffineTransform();
		trans.translate(x_trans-s.getBounds2D().getX()-s.getBounds2D().getWidth()/2, 
				y_trans-s.getBounds2D().getY()-s.getBounds2D().getHeight()/2);
		s = trans.createTransformedShape(s);
		//s.getBounds().x = (int)x_trans;
		//s.getBounds().x = (int)y_trans;
		g.setPaint(Color.white);
		setCoords(x_trans, y_trans);
		g.draw(s);
		
		//g.drawString(toTuioLetter(),(int)x_trans-10,(int)y_trans);
		
	}
	

	public void update(TuioObject tobj) {
		
		float dx = tobj.getX() - xpos;
		float dy = tobj.getY() - ypos;
		float da = tobj.getAngle() - angle;

		if ((dx!=0) || (dy!=0)) {
			AffineTransform trans = AffineTransform.getTranslateInstance(dx,dy);
			outline = trans.createTransformedShape(outline);
		}
		
		if (da!=0) {
			AffineTransform trans = AffineTransform.getRotateInstance(da,tobj.getX(),tobj.getY());
			outline = trans.createTransformedShape(outline);
		}

		super.update(tobj);
	}
	
	public static void setSize(int size) {
		TuioBlock.block_size = size;
	}
	
	public static void setTableSize(int table_size) {
		TuioBlock.table_size = table_size;
	}
	

	
	
	
	

}
