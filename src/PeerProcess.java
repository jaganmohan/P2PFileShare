package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class PeerProcess extends Peer{
	
	//Stores the peer process objects in a map
	private static HashMap<Integer,Peer> peers = new HashMap<Integer,Peer>();
	
	//Singleton object for Peer process
	private static PeerProcess peerProcess;
	
	//Server socket for this peer
	private static ServerSocket sSocket;
	//listening socket
	private static Socket lSocket;
	
	public PeerProcess(String pid, String hName, String portno, String present){
		super(pid,hName,portno,present);
	}
	
	public PeerProcess(int pid, String hName, int portno, short present){
		super(pid,hName,portno,present);
	}
	
    public static void startSender(String host, int port) {
        (new Thread() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket(host, port);
                    BufferedWriter out = new BufferedWriter(
                            new OutputStreamWriter(s.getOutputStream()));

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                    while(true)
                    {
                    	System.out.print("Hello, please input a sentence: ");
                        String message = bufferedReader.readLine();
                    	                   	
                        out.write(message);
                        out.newLine();
                        out.flush();

                        Thread.sleep(200);
                    }

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void startServer() {
    	Socket s = lSocket;
        (new Thread() {
            @Override
            public void run() {
                try {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(s.getInputStream()));
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    public static void main(String[] args) throws IOException {
    	
    	getConfiguration();
        //startServer();
        //startSender();
    }
    
    public static void getConfiguration()
	{
		String st;
		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			
			String FileName = System.getProperty("user.dir")+"/PeerInfo.cfg";
			
			BufferedReader in = new BufferedReader(new FileReader(FileName));
			
			while((st = in.readLine()) != null) {	
				String[] tokens = st.split("\\s+");
				Peer peer = new Peer(tokens[0],tokens[1],tokens[2],tokens[3]);
				peers.put(Integer.parseInt(tokens[0]),peer);
				if(tokens[1].trim().equals(hostname)){
					peerProcess = new PeerProcess(tokens[0],tokens[1],tokens[2],tokens[3]);
				}
				try {
					sSocket = new ServerSocket(Integer.parseInt(tokens[2]));
					//lSocket = sSocket.accept();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("Error opening socket");
					e.printStackTrace();
				}
			}
			
			in.close();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
}