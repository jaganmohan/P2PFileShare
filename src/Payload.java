package src;

public abstract class Payload {
	
	private int msgLength = 0;
	
	public void setMsgLength(int length){
		msgLength = length;
	}
	
	public int getMsgLength(){
		return msgLength;
	}
}

class HavePayload extends Payload{
	
	// 4-byte piece index field we will index from -2,147,483,648 to 2,147,483,647
	private int index;
}

class BitfieldPayload extends Payload{
	
	//Representing file pieces present in the corresponding bits
	// A four byte field
	private byte[] bitfield;
	
	public BitfieldPayload(byte[] bitfield){
		this.bitfield = bitfield;
	}
	
	public byte[] getBitfield(){
		return bitfield;
	}
}

class RequestPayload extends Payload{
	// 4-byte piece index field we will index from -2,147,483,648 to 2,147,483,647
	private int index;
}

class PiecePayload extends Payload{
	// 4-byte piece index field we will index from -2,147,483,648 to 2,147,483,647
	private int index;
	
	//max byte buffer of length 4,294,967,295 bytes minus 5 bytes to accommodate mType and index fields
	private byte buffer[];
}