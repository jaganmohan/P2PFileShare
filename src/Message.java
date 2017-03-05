package src;

import java.io.Serializable;

public class Message implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2925592711545151885L;

	// Message Length in bytes excluding the length of message length field
	private int mLength;
	
	// Message Type
	private byte mType;
	
	// Message payload
	Payload mPayload;
	
	public Message(){}
	
	public Message(byte type, Payload payload){
		mType = type;
		mPayload = payload;
		mLength = payload.getMsgLength()+1;
	}
}

enum MessageType{
	
	CHOKE(0), UNCHOKE(1), INTERESTED(2), NOT_INTERESTED(3), HAVE(4),
	BITFIELD(5), REQUEST(6), PIECE(7);
	
	private byte val;
	
	MessageType(int val){
		this.val = (byte)val;
	}
	
	public byte getVal(){
		return val;
	}
}