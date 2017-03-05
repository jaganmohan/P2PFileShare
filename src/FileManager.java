package src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import util.FileUtilities;

public class FileManager 
{
	private static boolean[] filePiecesOwned;
	
	private static final int noOfFilePieces = ConfigParser.getFileSize()/8;

	private static byte[] bitfield = new byte[noOfFilePieces];
		
	private static int noOfPiecesAvailable = 0;
	
	private String directory = null;
	private String fileName = null;
	private int fileSize = 0;
	private File file = null;
		
	public byte[] getBitField() throws Exception {
		return createBitfield(filePiecesOwned);
	}

	public FileManager(int peerid) 
	{
		directory = "../peer_" + peerid + "/";
		fileName = ConfigParser.getFileName();
		filePiecesOwned = new boolean[fileSize];
		
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
	
	public FilePiece get(int index) 
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
			return new FilePiece(content, index);
		}catch (FileNotFoundException e) 
		{
			return null;
		}catch (IOException e) 
		{
			return null;
		}
	}


	public void store(FilePiece piece) 
	{
		int loc = ConfigParser.getPieceSize() * piece.getIndex();
		RandomAccessFile fos = null;
		try {
			fos = new RandomAccessFile(file, "rw");
			fos.seek(loc);
			fos.write(piece.getContent());
			fos.close();
			
//			bitField.set(piece.getIndex());
//			if (bitField.hasAll()) {
//			}
		}catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public byte[] createBitfield(boolean[] pieces) throws Exception{
		
		if(noOfPiecesAvailable == 0)
			return null;
		
		for(int i=0;i<noOfFilePieces;i=i+8){
			bitfield[noOfPiecesAvailable++] = FileUtilities.boolToByte(Arrays.copyOfRange(filePiecesOwned, i, i+8));
		}
		
		return bitfield;
	}
	
	public boolean compareBitfields(byte[] neighborBitfield, byte[] interesting){
		boolean flag = false;
		if(neighborBitfield == null) return flag;
		for(int i=0;i<bitfield.length;i++){
			interesting[i] = (byte) ((bitfield[i]^neighborBitfield[i])&neighborBitfield[i]);
			if(interesting[i] != 0)
				flag = true;
		}
		return flag;
	}
}