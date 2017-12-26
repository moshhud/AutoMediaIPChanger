package autoipchanger;

import java.net.DatagramSocket;


public class UDPClientThread extends Thread{
	
	public static UDPClientThread ob;
	DatagramSocket clientSocket=null;
	boolean running=true;

	
	
	public static UDPClientThread getInstance(){
		if(ob==null){			
			createInstance();
		}		
		return ob;
   }//
	
    public static synchronized UDPClientThread createInstance(){
		if(ob==null){
			ob = new UDPClientThread();			
		}
		return ob;
    }//
    
    public void load(DatagramSocket clientSocket){
            this.clientSocket = clientSocket;
            start();
    }
    
    public void stopThread(){
            running=false; 
      		
    }
	
	
	long prevTime = System.currentTimeMillis();
	long timedOut=30000;
	
	public void run(){ 
		
		
		while(running){
			
			if(System.currentTimeMillis()-prevTime>=timedOut){				
      			prevTime = System.currentTimeMillis();
                System.out.println("Timed out, Closing Socket");
                if(clientSocket!=null)
      		       clientSocket.close();
      		    running=false; 	
			}
			
			try{
                 Thread.sleep(1000);
                 
            }
            catch(Exception e){
              	
            }
		}
	   
	
	}
}