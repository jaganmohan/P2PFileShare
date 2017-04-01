package src;

import java.net.ServerSocket;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

public class PeerHandler extends Thread{
		
	private ServerSocket sSocket;
	
	//Stores the peer process objects in a map
	private static HashMap<Integer,Peer> peers;

	private TreeSet<Peer> interested = new TreeSet<Peer>(new Comparator<Peer>() {
		Random r = new Random();
		@Override
		public int compare(Peer o1, Peer o2) {
			if(o1.getDownloadSpeed() == o2.getDownloadSpeed())
				return r.nextInt(2); //Randomly sequencing equal elements
			return (int)-(o1.getDownloadSpeed()-o2.getDownloadSpeed());
		}
	});
	
	private ArrayList<Peer> kPeers;
	
	private Peer optUnchokedPeer;
	
	public PeerHandler(){}
	
	public PeerHandler(ServerSocket s, HashMap<Integer,Peer> peers){
		sSocket = s;
		this.peers = peers;
	}
	
	public void add(Peer i){
		interested.add(i);
	}
	
	public void kPreferredPeers(){
		
		long timeout = ConfigParser.getUnchokingInterval()*1000;
		new Thread(){
			public void run(){
				try{
					synchronized(interested){
						// reselecting k preferred peers in time intervals of 'UnchokingInterval' from config
						do{
							kPeers = new ArrayList<Peer>();
							Iterator<Peer> it = interested.iterator();
							for(int i=0; i<ConfigParser.getNumberOfPreferredNeighbors()
									&& it.hasNext();i++){
								Peer p = it.next();
								kPeers.add(p);
								unchokePeer(p);
							}
							chokePeers();
							wait(timeout);
						}while(!sSocket.isClosed());
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}.start();
		
	}
	
	public void optUnchokePeer(){
		
		long timeout = ConfigParser.getOptimisticUnchokingInterval()*1000;
		new Thread(){
			public void run(){
				try{
					synchronized(interested){
						// reselecting optimistic peer in time intervals of 'OptimisticUnchokingInterval' from config
						do{
							Peer p;
							Random r = new Random();
							Peer[] prs = (Peer[]) interested.toArray();
							do{
								p = prs[r.nextInt(prs.length)];
							}while(!p.isUnchoked());
							optUnchokedPeer = p;
							unchokePeer(p);
							wait(timeout);
						}while(!sSocket.isClosed());
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}.start();
		
	}
	
	public void unchokePeer(Peer p){
		p.unChoke(true);
		//send unchoke message to peer p
		Message msgUnchoke = new Message(MessageType.UNCHOKE, null);
		p.getConn().sendMessage(msgUnchoke);
	}
	
	public void chokePeers(){
		//choke all other peers not in map kPeers
	}
	
	public void run(){
		kPreferredPeers();
		optUnchokePeer();
	}
}
