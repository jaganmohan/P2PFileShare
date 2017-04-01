package src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectionHandler extends Thread{
	
	private Socket sock;
		
	private Peer host;
	
	private Peer neighbor;
	
	private ObjectOutputStream sout;
	
	private ObjectInputStream sin;
	
	private long avgDownloadRate;
	
	private int piecesDownloaded;
	
	public ConnectionHandler(){}
	
	public ConnectionHandler(Peer h, Peer n, ObjectInputStream i, ObjectOutputStream o, Socket s){
		host = h;
		neighbor = n;
		sin = i;
		sout = o;
		sock = s;
	}
	
	public void setSocket(Socket s){
		sock = s;
	}
	
	public void calculateDownloadRate(){
		
	}
	
	public void sendMessage(Message msg){
		try {
			sout.writeObject(msg);
		} catch (IOException e) {
			System.out.println(host.getPeerId()+": Error sending message to "+neighbor.getPeerId());
			e.printStackTrace();
		}
	}
	
	public void receiveMessage(){
		// Waits on input stream of the connection to read incoming messages
		new Thread(){
			public void run(){

				Message recv = null;
				try {
					recv = (Message) sin.readObject();
					if(recv != null){
						switch (recv.getMsgType()){
							case UNCHOKE:
								int pieceIdx = FileManager.requestPiece(neighbor.getBitfield());
								Payload requestPayload = new RequestPayload(pieceIdx);
								Message msgRequest = new Message(MessageType.REQUEST, requestPayload);
								sout.writeObject(msgRequest);
								sout.flush();
								break;
							case CHOKE:
								break;
							case HAVE:
								break;
							case REQUEST:
								break;
							case INTERESTED:
								break;
							case NOT_INTERESTED:
								break;
							case BITFIELD:
								break;
							case PIECE:
								break;
						}
					}
				} catch (ClassNotFoundException | IOException e) {
					System.out.println(host.getPeerId()+": Error recieving message from "+neighbor.getPeerId());
					e.printStackTrace();
				}
							
			}
		}.start();
	}
	
	public void run(){
		
		//TODO after p seconds of time interval
		neighbor.setDownloadSpeed(avgDownloadRate);
	}
	
}
