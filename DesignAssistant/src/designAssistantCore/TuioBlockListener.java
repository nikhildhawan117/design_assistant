package designAssistantCore;
import java.util.*;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.*;
import java.awt.*;
import TUIO.*;

public class TuioBlockListener implements TuioListener{
	
	//this class writes to blockList
	private Hashtable<Long,TuioBlock> blockList;
	private ArrayList<Hashtable<Long,TuioBlock>> blockListBuffer;
	//this class writes to currentConfig and prevConfig
	private Configuration currentConfig;
	private Configuration prevConfig;
	private boolean update_flag = false;
	private int counter = 0;
	private int sample_period = 1;
	private int sample_threshold = 1;
	
	public TuioBlockListener(Hashtable<Long,TuioBlock> blockList, Configuration currentConfig, Configuration prevConfig) {

		this.blockList = blockList;
		this.blockListBuffer = new ArrayList<Hashtable<Long,TuioBlock>>(sample_period);
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
		StringBuilder blockString = new StringBuilder("{\"op\":\"publish\",\"topic\":\"/blocks\","
				+ "\"msg\":{\"data\":\"[");
		String prefix="";
		for (final Map.Entry<Long, TuioBlock> entry : blockList.entrySet()){
			TuioBlock block = entry.getValue();
			blockString.append(prefix);
			blockString.append("{\\\"x\\\":"+block.getX()+",\\\"y\\\":"+block.getY()+",\\\"id\\\":"+block.getSymbolID()+"}");
			prefix=",";
		}
		blockString.append("]\"}}");
		if(DesignAssistant.rosPublisher !=null){
			DesignAssistant.rosPublisher.sendMessage(blockString.toString());
		}
		//prevConfig = currentConfig;
		//currentConfig = new Configuration(blockList);

	}

	@Override
	public void updateTuioObject(TuioObject tobj) {
		TuioBlock tb = blockList.get(tobj.getSessionID());
		tb.update(tobj);
		StringBuilder blockString = new StringBuilder("{\"op\":\"publish\",\"topic\":\"/blocks\","
				+ "\"msg\":{\"data\":\"[");
		String prefix="";
		for (final Map.Entry<Long, TuioBlock> entry : blockList.entrySet()){
			TuioBlock block = entry.getValue();
			blockString.append(prefix);
			blockString.append("{\\\"x\\\":"+block.getX()+",\\\"y\\\":"+block.getY()+",\\\"id\\\":"+block.getSymbolID()+"}");
			prefix=",";
		}
		blockString.append("]\"}}");
		if(DesignAssistant.rosPublisher !=null){
			DesignAssistant.rosPublisher.sendMessage(blockString.toString());
		}
		//prevConfig = currentConfig;
		//currentConfig = new Configuration(blockList);
	}

	@Override
	public void removeTuioObject(TuioObject tobj) {
		blockList.remove(tobj.getSessionID());
		StringBuilder blockString = new StringBuilder("{\"op\":\"publish\",\"topic\":\"/blocks\","
				+ "\"msg\":{\"data\":\"[");
		String prefix="";
		for (final Map.Entry<Long, TuioBlock> entry : blockList.entrySet()){
			TuioBlock block = entry.getValue();
			blockString.append(prefix);
			blockString.append("{\\\"x\\\":"+block.getX()+",\\\"y\\\":"+block.getY()+",\\\"id\\\":"+block.getSymbolID()+"}");
			prefix=",";
		}
		blockString.append("]\"}}");
		if(DesignAssistant.rosPublisher !=null){
			DesignAssistant.rosPublisher.sendMessage(blockString.toString());
		}
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
		if(sample_period==1){
			prevConfig = currentConfig;
			currentConfig = new Configuration(blockList);
			synchronized(this){
				update_flag = true;
				this.notifyAll();
			}
			return;
		}
		if(counter++ < sample_period){
			blockListBuffer.add(blockList);
			return;
		}
		counter = 0;
		prevConfig = currentConfig;
		currentConfig = new Configuration(blockListBuffer,sample_threshold);
		
		
		synchronized(this) {
		update_flag = true;
		this.notifyAll();
		}

	}

}
