import java.util.Random;

public class CollaborativeAgent {

	static boolean agentLock = false;
	
	public static String[] getLocalConfig(long configOneHot) {
		String str = String.format("%60s", Long.toBinaryString(configOneHot)).replace(" ", "0");
		StringBuilder config_sb = new StringBuilder(str);
		String.format(str);
		
		String[] localConfigs = new String[10];
		for(int i = 0; i < 10; i++) {
			int bit_index = (int)(Math.random()*60);
			//System.out.println(bit_index);
			if(config_sb.charAt(bit_index)=='0')
				config_sb.setCharAt(bit_index, '1');
			else
				config_sb.setCharAt(bit_index, '0');
			
			localConfigs[i] = config_sb.toString();
			//System.out.println("Random Config: " + localConfigs[i]);
		}
		
		return localConfigs;
	}
}
