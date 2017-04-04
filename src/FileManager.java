package src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Hashtable;

import util.FileUtilities;

public class FileManager 
{
	private static boolean[] filePiecesOwned;
		
	private static Hashtable<Integer, Integer> requestedPieces = new Hashtable<Integer, Integer>();
	
	private static final int noOfFilePieces = (int)Math.ceil(ConfigParser.getFileSize()/ConfigParser.getPieceSize());

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


	public static synchronized void store(PiecePayload piece) 
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
	
	public static int requestPiece(byte[] neighborBitfield, byte[] bitfield){
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
	
	public static boolean hasCompleteFile(){
		
		if(noOfPiecesAvailable < noOfFilePieces)
			return false;
		return true;
	}
}