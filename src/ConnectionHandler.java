package src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
/**
 * Contains connection related information between host peer and neighboring peer including object streams
 * to read and write from the connection.
 * 
 * @author Jagan
 *
 */
public class ConnectionHandler extends Thread{
	
	private Socket sock;
		
	private Peer host;
	
	private Peer neighbor;
	
	private ObjectOutputStream sout;
	
	private ObjectInputStream sin;
	
	private int piecesDownloaded;
	
	private PeerHandler pHandler;
	
	public ConnectionHandler(){}
	
	public ConnectionHandler(Peer h, Peer n, ObjectInputStream i, ObjectOutputStream o, Socket s, PeerHandler p){
		host = h;
		neighbor = n;
		sin = i;
		sout = o;
		sock = s;
		pHandler = p;
		piecesDownloaded =0;
		
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
							case UNCHOKE:{
								int pieceIdx = FileManager.requestPiece(neighbor.getBitfield(), host.getBitfield());
								Payload requestPayload = new RequestPayload(pieceIdx);
								Message msgRequest = new Message(MessageType.REQUEST, requestPayload);
								sout.writeObject(msgRequest);
								sout.flush();}
								break;
							case CHOKE:
								//TODO stop sending file pieces
								break;
							case HAVE:{
								HavePayload have = (HavePayload)(recv.mPayload);
								neighbor.updateBitfield(have.getIndex());
								System.out.println("Peer "+neighbor.getPeerId()+" contains interesting file pieces");
								
								//Check whether the piece is interesting and send interested message
								
								if(FileManager.isInteresting(have.getIndex()))
								{
									Message interested = new Message(MessageType.INTERESTED,null);
									sendMessage(interested);
								}
								break;}
							case REQUEST:{
								int requestedIndex = ((RequestPayload)recv.mPayload).getIndex();
								byte [] pieceContent = FileManager.get(requestedIndex).getContent();
								int pieceIndex = FileManager.get(requestedIndex).getIndex();
								Message pieceToSend = new Message(MessageType.PIECE, new PiecePayload(pieceContent, pieceIndex));
								sendMessage(pieceToSend);
								break;}
							case INTERESTED:
								pHandler.add(neighbor);
								break;
							case NOT_INTERESTED:
								pHandler.remove(neighbor);
								break;
							case BITFIELD:{
						    	BitfieldPayload in_payload = (BitfieldPayload)(recv.mPayload);
						    	System.out.println("Received Bitfield Message from : "+neighbor.getPeerId());
						    	//setting bitfield for the neighboring peer
						        neighbor.setBitfield(in_payload.getBitfield());
						    	if(!FileManager.compareBitfields(in_payload.getBitfield(),host.getBitfield() )){
						    		System.out.println("Peer "+neighbor.getPeerId()+" does not contain any interesting file pieces");
						    		Message notInterested = new Message(MessageType.NOT_INTERESTED,null);
									sendMessage(notInterested);
						    	}
								System.out.println("Peer "+neighbor.getPeerId()+" contains interesting file pieces");
								Message interested = new Message(MessageType.INTERESTED,null);
								sendMessage(interested);
						    	// No need to add peers that you are interested in.
							
								break;}
							case PIECE:{

								try
								{
									FileManager.store((PiecePayload)recv.mPayload);
								}catch (Exception e) {
									// TODO: handle exception
									e.printStackTrace();
								}
								host.updateBitfield(((PiecePayload)recv.mPayload).getIndex());
								
								pHandler.sendHaveAll(((PiecePayload)recv.mPayload).getIndex());
								piecesDownloaded++;
								
								break;}
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
		neighbor.setDownloadSpeed(piecesDownloaded);
	}
	
	public void resetPiecesDownloaded()
	{
		piecesDownloaded = 0;
	}
	
}
