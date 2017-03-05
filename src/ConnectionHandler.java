package src;

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
	
	public void run(){
		
		//TODO after p seconds of time interval
		neighbor.setDownloadSpeed(avgDownloadRate);
	}
	
}
