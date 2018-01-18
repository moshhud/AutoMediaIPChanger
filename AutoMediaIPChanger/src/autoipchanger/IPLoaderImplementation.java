package autoipchanger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Random;



public class IPLoaderImplementation extends IPLoader{	
	
	final int PACKET_TYPE_IPCHANGER_MAILSERVER_REQUEST=0x0003;
	final int PACKET_TYPE_IPCHANGER_MAILSERVER_RESPONSE=0x0004;
	final int MAILSERVER_IP=0x0066;
	final int MAILSERVER_PORT=0x0067;
	final int AUTH_MAIL_ADDRESS=0x0068;
	final int AUTH_MAIL_PASS=0x0069;
	final int VOICE_LISTEN_IP_LIST=0x0002;
	final int MEDIA_PROXY_PUBLIC_IP_LIST=0x0003;
	final int OPERATOR_CODE=0x0004;
		
	String voiceListenIPList=null;
	String mediaProxyPublicIP=null; 
	
	String mailServerIP = null;
	String mailServerPort=null;
	String mailID = null;
	String mailPass = null;	
	
	DatagramPacket sendPacket=null;
	DatagramPacket receivePacket=null;
	DatagramSocket clientSocket=null;
	String localIP = "43.240.101.55";
    int localPort = 2200;
	
	@Override
	public void run()
	{
		
		running = true;		
		try{
			localIP = IPChanger.serverIP;
			localPort = IPChanger.serverPort;
			
			while (running){
				IPChanger.enableProvisioning = true;
				if(IPChanger.enableProvisioning){
					
					logger.debug("Voice Listen IP: "+IPChanger.voiceListenIPList);
				    logger.debug("Public IP: " +IPChanger.mediaProxyPublicIPList);
					
					loadVoiceListenIPFromDB();					
				    
					try {
						
						if(IPChanger.enableREVEMailServer&&IPChanger.enableMailSending){
							logger.debug("Loading REVE email server config...");
							if(mailServerIP!=null){
								IPChanger.smtpServer = mailServerIP;
								mailServerIP=null;
							}
							if(mailServerPort!=null){
								IPChanger.smtpPort = mailServerPort;
								mailServerPort=null;
							}
							if(mailID!=null){
								IPChanger.fromEmail = mailID;	
								mailID=null;
							}
							if(mailPass!=null){
								IPChanger.fromEmailPassword = mailPass;	
								mailPass=null;
							}
								
						}
						
						
						
						if((mediaProxyPublicIP==null)&&(voiceListenIPList!=null)&&(voiceListenIPList.length()>7)){
				    		logger.debug("Media Proxy Public IP List is empty.");
				    		IPChanger.mediaProxyPublicIPList = voiceListenIPList;
				    		IPChanger.voiceListenIPList = voiceListenIPList;	
				    		voiceListenIPList=null;
				    	}
						if((mediaProxyPublicIP!=null)&&(mediaProxyPublicIP.length()>7)) {
							logger.debug("loading public IP...");
							IPChanger.mediaProxyPublicIPList = mediaProxyPublicIP;
							mediaProxyPublicIP=null;
						}
						if((voiceListenIPList!=null) && (voiceListenIPList.length()>7)) {
							logger.debug("loading Voice Listen IP...");
							IPChanger.voiceListenIPList = voiceListenIPList;	
							voiceListenIPList=null;
						}						
					}
					catch(Exception e) {
						logger.fatal("loading IP: "+e.toString());
					}
				}
				else{
					logger.debug("Provisioning service is disabled.");
				}
				try
				{
					Thread.sleep(30000);
				}
				catch(Exception ex){}
			}
		 }
		 catch(Exception e){
	   	  	logger.fatal("Error 1: "+e);
	   	 }		
	}
	
	
	@Override
	public void loadVoiceListenIPFromDB()
	{		
		try
		{   
	 	    clientSocket = new DatagramSocket(null);//new DatagramSocket(localPort,InetAddress.getByName(localIP))
	 	    clientSocket.bind(new InetSocketAddress(localIP, localPort));
	 	    clientSocket.setSoTimeout(15000); 			
			send(PACKET_TYPE_IPCHANGER_MAILSERVER_REQUEST);
			
		}
		catch(Exception e)
		{
			logger.fatal("IP Loader: "+e.toString());	
			
		}
		finally
		{				
			if(clientSocket!=null)
				try
				{
					clientSocket.close();
					clientSocket=null;
				}catch(Exception ex){}					
			
		}
	}//end of method
	
	public synchronized void  send(int REQUEST_TYPE){
	 	try{	 		
	 	     
             sendPacket = createPacket(REQUEST_TYPE);
             clientSocket.send(sendPacket);
             logger.debug("Sending Data ...:"+REQUEST_TYPE);	
             
             byte[] receiveData = new byte[2048];
 			 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
       		 clientSocket.receive(receivePacket);
       		      		
       		 byte[] data = new byte[2048];
             System.arraycopy(receivePacket.getData(), 0, data, 0, receivePacket.getLength());                 
             int dcb = decodeBytes(data,receivePacket.getLength());            
             getParameterValue(data,dcb);
	 	}
	 	catch(Exception e){
	 		logger.fatal("Error 2: "+e);
	 	}
	 }
	
	public int decodeBytes(byte [] data,int len){
		  int index=0;
		  if(len>=5){
		   byte temp = data[1];
		         data[1] = data[4];
		         data[4] = temp;
		         int randLen = data[0];
		         randLen=(randLen<<8)|data[1];
		         int dataLen=len-randLen-2;
		         if (dataLen>0) {
		             for (int i = 0; i < dataLen; i++) {
		              data[i + randLen + 2] = (byte) (data[i + randLen + 2] ^ data[i % randLen + 2]);
		             }
		             for(int i=0;i<dataLen;i++){
		              data[i]=data[i + randLen + 2];
		             }
		             index=dataLen;
		         }
		  }
		  return index;
	 }
	
	public int encodeBytes(byte [] data,int len){
		  Random rand=new Random();
		  int randLen=rand.nextInt(20)+3;
		  for(int i=len-1;i>=0;i--){
		   data[i+randLen+2]=data[i];
		  }
		  data[0]=(byte)(randLen>>8 & 0xff);
		  data[1]=(byte)(randLen & 0xff);
		  for(int i=0;i<randLen;i++){
		   data[2+i]=(byte) rand.nextInt(256);
		  }
		  for(int i=0;i<len;i++){
		   data[i+randLen+2]=(byte) (data[(i%randLen)+2]^data[i+2+randLen]);
		  }
		  byte temp=data[1];
		  data[1]=data[4];
		  data[4]=temp;
		  return randLen+len+2;
	 }//
	
	private  DatagramPacket createPacket(int REQUEST_TYPE) throws UnknownHostException{
	        byte [] sendData=new byte[2048];
	        int index=0;
	        sendData[index++]=(byte)((REQUEST_TYPE>>8) & 0xff);
	        sendData[index++]=(byte)((REQUEST_TYPE) & 0xff);
	        index+=2;
	        sendData[2]=(byte)(((index-4)>>8) & 0xff);
	        sendData[3]=(byte)(((index-4)) & 0xff);
	        index=encodeBytes(sendData, index);
	        DatagramPacket sendPacket=new DatagramPacket(sendData,index,InetAddress.getByName(IPChanger.configurationLoaderIp),IPChanger.configurationLoaderPort);
	        return sendPacket;
	}//
	

	
public void getParameterValue(byte[] data,int len){
	 	
	 	int dataLen;
        dataLen=data[2];
        dataLen=(dataLen<<8)|data[3];
        
        int index=0;
        
        int pktType=data[index++];
        pktType=(pktType<<8)|data[index++];            
        int pktLen=data[index++];
        pktLen=(pktLen<<8)|data[index++];            
        logger.debug("Packet : "+pktType+", Length: "+pktLen);
        
        while(index<len){
        	
        	int attrType=data[index++];
            attrType=(attrType<<8)|data[index++];
            int attrLen=data[index++];
            attrLen=(attrLen<<8)|data[index++];
            
            if(attrLen<=0)continue;
            byte [] value=new byte[attrLen];
            
            for(int j=0;j<attrLen;j++){
                value[j]=data[index++];               
            }             
            //logger.debug("Value: "+ new String(value));
            switch(attrType){
            	case VOICE_LISTEN_IP_LIST:
            		 voiceListenIPList=new String(value);
            	     logger.debug("Got Voice Listen IP: "+voiceListenIPList);
            	     break;
            	case MEDIA_PROXY_PUBLIC_IP_LIST:
           		     mediaProxyPublicIP=new String(value);
           		     logger.debug("Got Public IP: "+mediaProxyPublicIP);
           	         break;
            	case OPERATOR_CODE:      	     	    
            		 logger.debug("OP Code: "+new String(value));
            	     break; 
            	case MAILSERVER_IP:     
            		mailServerIP = new String(value);
            		 //logger.debug("Got Mail server IP: "+mailServerIP);            		 
            	     break; 
            	case MAILSERVER_PORT:
            		 mailServerPort = new String(value);
            		 //logger.debug("Got Mail server Port: "+mailServerPort);
            	     break;
            	case AUTH_MAIL_ADDRESS:
            		 mailID = new String(value);
            		 //logger.debug("Got Mail ID: "+mailID);
            	     break;
            	case AUTH_MAIL_PASS: 
            		 mailPass = new String(value);
            		 //logger.debug("Got Mail Pass: "+mailPass);
            	     break;
            }
            
            
        }
	 }
	
	
	

}//end of class
