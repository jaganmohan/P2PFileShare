package src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Hashtable;

public class FileManager 
{
	/** File pieces owned by the peer */
	private static boolean[] filePiecesOwned;
	
	/** Table to keep track of requested file pieces at any instant so that redundant requests are not made */
	private static Hashtable<Integer, Integer> requestedPieces = new Hashtable<Integer, Integer>();
	
	/** Number of file pieces the file can be broken into */
	private static final int noOfFilePieces = (int)Math.ceil(ConfigParser.getFileSize()/ConfigParser.getPieceSize());

	/** File pieces available by the peer */
	private static int noOfPiecesAvailable = 0;
	
	private String directory = null;
	private String fileName = null;
	private static int fileSize = 0;
	private static File file = null;
		
	public byte[] getBitField() throws Exception {
		return createBitfield(filePiecesOwned);
	}

	public FileManager(int peerid , boolean has) 
	{
		directory = "../peer_" + peerid + "/";
		fileName = ConfigParser.getFileName();
		
		filePiecesOwned = new boolean[noOfFilePieces];
		
		if(has)
		{
			Arrays.fill(filePiecesOwned, true);
			noOfPiecesAvailable = noOfFilePieces;
		}
		
		File folder = new File(directory);

		if (!folder.exists()) 
		{
			folder.mkdirs();
		}

		file = new File(directory + fileName);
		
		if (!file.exists()) 
		{
			FileOutputStream fos = null;
			try 
			{
				fos = new FileOutputStream(file);
				fos.write(new byte[fileSize]);
				fos.close();
			}catch (IOException e) 
			{
				e.printStackTrace();
			}
		}	
	}
	
	public static synchronized PiecePayload get(int index) 
	{
		try 
		{
			FileInputStream fis = new FileInputStream(file);
			int loc = ConfigParser.getPieceSize() * index;
			fis.skip(loc);
			int contentSize = ConfigParser.getPieceSize();
			if (fileSize - loc < ConfigParser.getPieceSize())
				contentSize = fileSize - loc;
			byte[] content = new byte[contentSize];
			fis.read(content);
			fis.close();
			return new PiecePayload(content, index);
		}catch (FileNotFoundException e) 
		{
			return null;
		}catch (IOException e) 
		{
			return null;
		}
	}

	public static synchronized void store(PiecePayload piece) throws Exception
	{
		int loc = ConfigParser.getPieceSize() * piece.getIndex();
		RandomAccessFile fos = null;
		try {
			fos = new RandomAccessFile(file, "rw");
			fos.seek(loc);
			fos.write(piece.getContent());
			fos.close();
			
			noOfPiecesAvailable++;
			filePiecesOwned[piece.getIndex()] = true;
			
		}catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates bitfield first time during initialization for the peer
	 * 
	 * @param pieces boolean representation of pieces available
	 * @return byte format of the pieces available
	 * @throws Exception
	 */
	public byte[] createBitfield(boolean[] pieces) throws Exception{
		
		byte[] bitfield = new byte[(int)Math.ceil(noOfFilePieces/8)];
		if(noOfPiecesAvailable == 0)
			return null;
		
		int counter = 0;
		for(int i=0;i<noOfFilePieces;i=i+8){
			bitfield[counter++] = FileUtilities.boolToByte(Arrays.copyOfRange(filePiecesOwned, i, i+8));
		}
		
		return bitfield;
	}
	
	/**
	 * Compares bitfields of two peers to decide interesting or not interesting
	 * 
	 * @param neighborBitfield Neighboring peer's bitfield
	 * @param bitfield Host peer's bitfield
	 * @return
	 */
	public static boolean compareBitfields(byte[] neighborBitfield, byte[] bitfield){
		boolean flag = false;
		byte[] interesting = new byte[noOfFilePieces];
		if(neighborBitfield == null) return flag;
		for(int i=0;i<bitfield.length;i++){
			interesting[i] = (byte) ((bitfield[i]^neighborBitfield[i])&neighborBitfield[i]);
			if(interesting[i] != 0)
				flag = true;
		}
		return flag;
	}
	
	/**
	 * Randomly requests a file piece to a neighbor which it does not have and has not been requested to other peers
	 * 
	 * @param neighborBitfield Neighboring peer's bitfield
	 * @param bitfield Host peer's bitfield
	 * @return
	 */
	public static int requestPiece(byte[] neighborBitfield, byte[] bitfield)
	{
		byte[] interesting = new byte[(int)Math.ceil(noOfFilePieces/8)];
		
		boolean[] interestingPieces = new boolean[noOfFilePieces];
		
		for(int i=0,j=0;i<bitfield.length;i++,j=j+8){
			interesting[i] = (byte) ((bitfield[i]^neighborBitfield[i])&neighborBitfield[i]);
			System.arraycopy(FileUtilities.byteToBoolean(interesting[i]), 0, interestingPieces, j, 8);
		}
		for(int i=0; i<noOfFilePieces; i++){
			if(interestingPieces[i] == true && !requestedPieces.containsKey(i))
				return i;
		}
		// TODO make it fail safe
		return 0;
	}
	
	
	public static boolean isInteresting(int index)
	{
		if(filePiecesOwned[index])
		{
			return true;
		}	
		return false;		
	}
	
	public static boolean hasCompleteFile(){
		if(noOfPiecesAvailable < noOfFilePieces)
			return false;
		return true;
	}
}