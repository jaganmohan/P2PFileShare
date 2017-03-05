package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * This is the main process which will run on each host, this class takes the responsibility
 * of performing all the peer responsibilities for that particular Peer which represents that host
 * 
 * For now this class takes responsibility for all peer processes for testing
 * 
 * @author Jagan
 *
 */
public class PeerProcess extends Peer implements Runnable{
	
	//Stores the peer process objects in a map
	private static HashMap<Integer,Peer> peers = new HashMap<Integer,Peer>();
	
	private final PeerHandler pHandler;
	
	//Singleton object for Peer process
	//private static PeerProcess peerProcess;
	
	//Server socket for this peer
	private ServerSocket sSocket;
	
	private FileManager fileData;
	
	/**
	 * Number of connections active for the peer - only k+1 concurrent connections can be active at any time
	 * k = number of preferred neighbors
	 */
	
	public PeerProcess(String pid, String hName, String portno, String present){
		super(pid,hName,portno,present);
		pHandler = new PeerHandler(sSocket);
	}
	
	public PeerProcess(int pid, String hName, int portno, short present){
		super(pid,hName,portno,present);
		pHandler = new PeerHandler(sSocket);
	}
	
	public void startSender(Peer pNeighbor, ConnectionHandler conn) {
    	Peer pHost = this;
        (new Thread() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket(pNeighbor.getHostname(), pNeighbor.getPortNo());
                    ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());

                    	System.out.println("Handshake Message sent from peer "+pHost.getPeerId()+" to peer "+pNeighbor.getPeerId());                    	                   	
                        out.writeObject(new HandShakeMsg(pHost.getPeerId()));
                        out.flush();
                        BitfieldPayload payload = new BitfieldPayload(fileData.getBitField());
                        out.writeObject(new Message(MessageType.BITFIELD.getVal(), payload));

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e){
                	e.printStackTrace();
                }
            }
        }).start();
    }

	public void startServer(){

		(new Thread() {
			@Override
			public void run() {
				while(!sSocket.isClosed()){
					try {
						/*(connActive+1 > ConfigParser.getNumberOfPreferredNeighbors()){
							System.out.println("Number of concurrent connections cannot exceed "+
									ConfigParser.getNumberOfPreferredNeighbors());
							ifreturn;
						}*/
						ConnectionHandler conn = establishConnection();
						conn.start();
						//if(conn != null)
						//	++connActive;
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();

	}
    
	public static void main(String[] args) throws IOException 
	{			
			System.out.println(ConfigParser.getFileName());
	    	getConfiguration();
	    	
	}
	
    public static void getConfiguration()
	{
		String st;
		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			
			String FileName = "PeerInfo.cfg";
			
			BufferedReader in = new BufferedReader(new FileReader(FileName));
			
			while((st = in.readLine()) != null) {	
				String[] tokens = st.split("\\s+");
				Peer peer = new Peer(tokens[0],tokens[1],tokens[2],tokens[3]);
				peers.put(Integer.parseInt(tokens[0]),peer);
				if(tokens[1].trim().equals(hostname)){
					new Thread(new PeerProcess(tokens[0],tokens[1],tokens[2],tokens[3])).start();
				}
			}
			
			in.close();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
    
    public ConnectionHandler establishConnection() throws Exception{

    	Socket lSocket = sSocket.accept();
    	ObjectInputStream in = new ObjectInputStream(lSocket.getInputStream());
    	ObjectOutputStream out = new ObjectOutputStream(lSocket.getOutputStream());

    	HandShakeMsg incoming = (HandShakeMsg)in.readObject();
    	if(peers.get(incoming.getPeerId()) == null || !incoming.getHeader().equals(HandShakeMsg.Header)){
    		System.out.println("Error performing Handshake : PeerId or Header unknown");
    	}
    	System.out.println("Received Handshake Message : "+
    			incoming.getPeerId()+" Header - "+incoming.getHeader());
    	
    	//Exchanging Handshake message
    	HandShakeMsg outgoing = new HandShakeMsg(getPeerId());
    	out.writeObject(outgoing);
        out.flush();
    	System.out.println("Handshake Message sent from peer "+getPeerId()+" to peer "+incoming.getPeerId());                    	                   	
        
    	//Exchanging Bitfield message
    	Message in_bitfieldMsg = (Message)in.readObject();
    	BitfieldPayload in_payload = (BitfieldPayload)(in_bitfieldMsg.mPayload);
    	System.out.println("Received Bitfield Message from : "+incoming.getPeerId());
    	BitfieldPayload out_payload = new BitfieldPayload(fileData.getBitField());
        out.writeObject(new Message(MessageType.BITFIELD.getVal(), out_payload));
        out.flush();
    	
        //setting bitfield for the neighbor peer
        peers.get(incoming.getPeerId()).setBitfield(in_payload.getBitfield());
    	byte[] interesting = null;
    	if(!fileData.compareBitfields(in_payload.getBitfield(), interesting)){
    		System.out.println("Peer "+incoming.getPeerId()+" does not contain any interesting file pieces");
    	}
		System.out.println("Peer "+incoming.getPeerId()+" contains interesting file pieces, establishing connection");
		pHandler.add(peers.get(incoming.getPeerId()));
		
		//Creating connection assuming both are interested
		ConnectionHandler conn = new ConnectionHandler(peers.get(this.getPeerId()), peers.get(incoming.getPeerId()),
				in, out, lSocket);
		return conn;
    }
    
	@Override
	public void run() {
		// TODO Auto-generated method stub
		fileData = new FileManager(getPeerId());
		try {
			setBitfield(fileData.getBitField());
    		sSocket = new ServerSocket(this.getPortNo());
    	} catch (Exception e) {
    		// TODO Auto-generated catch block
    		System.out.println("Error opening socket");
    		e.printStackTrace();
    	}
		startServer();
		pHandler.start();
	}
}