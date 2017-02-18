package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class P2Pfileshare {

	//Stores the peer process objects in a map
	private static HashMap<Integer,PeerProcess> peers = new HashMap<Integer,PeerProcess>();
	
	/*public static void main(String[] args) throws IOException {
    	
    	getConfiguration();
        //startServer();
        //startSender();
    }*/
    
    public static void getConfiguration()
	{
		String st;
		try {
			
			String FileName = System.getProperty("user.dir")+"/PeerInfo.cfg";
			
			BufferedReader in = new BufferedReader(new FileReader(FileName));
			
			while((st = in.readLine()) != null) {	
				 String[] tokens = st.split("\\s+");
				 PeerProcess peer = new PeerProcess(tokens[0],tokens[1],tokens[2],tokens[3]);
				 peers.put(Integer.parseInt(tokens[0]),peer);
			}
			
			in.close();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
}
