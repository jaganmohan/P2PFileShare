import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerProcess {
	
	//static HandShakeMsg m = new HandShakeMsg(1001);
	
    public static void main(String[] args) throws IOException {

    	
    	//System.out.println(m.toString());
    	
    	getConfiguration();
        startServer();
        startSender();
    }
    
    public static void getConfiguration()
	{
		String st;
		try {
			
			String FileName = System.getProperty("user.dir")+"/PeerInfo.cfg";
			
			BufferedReader in = new BufferedReader(new FileReader(FileName));
			while((st = in.readLine()) != null) {
				
				 String[] tokens = st.split("\\s+");
				 for(int i=0;i<tokens.length;i++)
				 {	 
					 System.out.println(tokens[i]);					
				 }
			}
			
			in.close();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

    public static void startSender() {
        (new Thread() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket("localhost", 60010);
                    BufferedWriter out = new BufferedWriter(
                            new OutputStreamWriter(s.getOutputStream()));

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                    while(true)
                    {
                    	System.out.print("Hello, please input a sentence: ");
                        String message = bufferedReader.readLine();
                    	                   	
                        out.write(message);
                        out.newLine();
                        out.flush();

                        Thread.sleep(200);
                    }

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void startServer() {
        (new Thread() {
            @Override
            public void run() {
                ServerSocket ss;
                try {
                    ss = new ServerSocket(60010);

                    Socket s = ss.accept();

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(s.getInputStream()));
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}