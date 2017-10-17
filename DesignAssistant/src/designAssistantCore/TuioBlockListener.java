package designAssistantCore;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import TUIO.*;

public class TuioBlockListener implements TuioListener{
	
	//this class writes to blockList
	private Hashtable<Long,TuioBlock> blockList;

	//this class writes to currentConfig and prevConfig
	private Configuration currentConfig;
	private Configuration prevConfig;
	private boolean update_flag = false;
	
	public TuioBlockListener(Hashtable<Long,TuioBlock> blockList, Configuration currentConfig, Configuration prevConfig) {

		this.blockList = blockList;
		this.currentConfig = currentConfig;
		this.prevConfig = prevConfig;
	}
	
	public void setUpdateFlag(boolean bool) {
		update_flag = bool;
	}
	
	public boolean getUpdateFlag() {
		return update_flag;
	}
	
	public Configuration getCurrentConfig() {
		return currentConfig;
	}
	
	public Configuration getPrevConfig() {
		return prevConfig;
	}
	
	@Override
	public void addTuioObject(TuioObject tobj) {
		TuioBlock tb = new TuioBlock(tobj);
		blockList.put(tobj.getSessionID(), tb);	
		//prevConfig = currentConfig;
		//currentConfig = new Configuration(blockList);

	}

	@Override
	public void updateTuioObject(TuioObject tobj) {
		TuioBlock tb = blockList.get(tobj.getSessionID());
		tb.update(tobj);
		//prevConfig = currentConfig;
		//currentConfig = new Configuration(blockList);
	}

	@Override
	public void removeTuioObject(TuioObject tobj) {
		blockList.remove(tobj.getSessionID());
	}

	@Override
	public void addTuioCursor(TuioCursor tcur) {
		System.out.println("TuioCursor not supported.");
		
	}

	@Override
	public void updateTuioCursor(TuioCursor tcur) {
		System.out.println("TuioCursor not supported.");
		
	}

	@Override
	public void removeTuioCursor(TuioCursor tcur) {
		System.out.println("TuioCursor not supported.");
		
	}

	@Override
	public void addTuioBlob(TuioBlob tblb) {
		System.out.println("TuioBlob not supported.");
		
	}

	@Override
	public void updateTuioBlob(TuioBlob tblb) {
		System.out.println("TuioBlob not supported.");
		
	}

	@Override
	public void removeTuioBlob(TuioBlob tblb) {
		System.out.println("TuioBlob not supported.");
		
	}

	@Override
	public void refresh(TuioTime ftime) {
		prevConfig = currentConfig;
		currentConfig = new Configuration(blockList);
		synchronized(this) {
		update_flag = true;
		this.notifyAll();
		}

	}

}
