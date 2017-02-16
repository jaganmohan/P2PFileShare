import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigParser 
{

		private static int numberOfPreferredNeighbors;
		private static int unchokingInterval;
		private static int optimisticUnchokingInterval;
		private static String fileName;
		private static int fileSize;
		private static int pieceSize;

		static 
		{
			try 
			{
				BufferedReader in = new BufferedReader(new FileReader("../Common.cfg"));
				String line;
				while ((line = in.readLine()) != null) 
				{
					String[] config = line.split(" ");
					String name = config[0];
					String value = config[1];
				
					switch (name)
					{
						case "NumberOfPreferredNeighbors":
							numberOfPreferredNeighbors = Integer.parseInt(value);
							break;
						case "UnchokingInterval":
							unchokingInterval = Integer.parseInt(value);
							break;
						case "OptimisticUnchokingInterval":
							optimisticUnchokingInterval = Integer.parseInt(value);
							break;
						case "FileName":
							fileName = value;
							break;
						case "FileSize":
							fileSize = Integer.parseInt(value);
							break;
						case "PieceSize":
							pieceSize = Integer.parseInt(value);
							break;
					}
				}
			} catch (FileNotFoundException e)
			{
				e.printStackTrace();
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		}

		public int getNumberOfPreferredNeighbors() 
		{
			return numberOfPreferredNeighbors;
		}

		public int getUnchokingInterval() 
		{
			return unchokingInterval;
		}

		public int getOptimisticUnchokingInterval() 
		{
			return optimisticUnchokingInterval;
		}

		public String getFileName() 
		{
			return fileName;
		}

		public int getFileSize() 
		{
			return fileSize;
		}

		public int getPieceSize() 
		{
			return pieceSize;
		}
}
