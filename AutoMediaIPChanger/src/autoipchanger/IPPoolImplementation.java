package autoipchanger;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;



public class IPPoolImplementation extends IPPool 
{
	
	
	public IPPoolImplementation()
	{
		super();
		
	}
	public void loadVoiceListenIPListAndSet()
	{
		boolean loadStatus = loadVoiceListenIPList();
		loadmediaProxyPublicIPList();
		if(IPChanger.enableMailSending&&IPChanger.enableStartUPemail)
		{
			sendStartupIPStatusNotificationMail();
			IPChanger.enableStartUPemail = false;
		}
		if(loadStatus)
		{
			//upToUsedIPIndexFromList = -1;
			loadCurUsedVoiceListenIP();
			logger.debug("Currently used voice Listen IP: "+ presentUsedIP);
			
			// mailing..
			if(IPChanger.enableMailSending)
			{
				sendStartupIPSettingNotificationMail(presentUsedIP);
			}
		}
		else
		{
			// No valid active IP exist in cfg file 
		}
		
	}
	public boolean loadVoiceListenIPList()
	{
		StringTokenizer st = new StringTokenizer(IPChanger.voiceListenIPList, ",");
		//System.out.println("No. of tokens "+st.countTokens());
		voiceListenIPcnt = 0;
		int tokencnt = st.countTokens();
		for(int i = 0; i < tokencnt; i++)
		{
			String curIP = st.nextToken().trim();
			if (curIP.length()<=6)
				continue;
			if(checkIfValidIP(curIP))
			{
				voiceListenIPList[voiceListenIPcnt++] = curIP;

			}
			else
			{
				testFailedIPList[testFailedIPcnt] = curIP;
				testFailedIPListInfo[testFailedIPcnt++] = "Invalid IP";
			}
		}
		
		if(voiceListenIPcnt > 0)
			return true;
		else
			return false;
	}
	
	public boolean loadmediaProxyPublicIPList()
	{
		StringTokenizer st = new StringTokenizer(IPChanger.mediaProxyPublicIPList, ",");
		//System.out.println("No. of tokens "+st.countTokens());
		mediaPublicIPcnt = 0;
		int tokencnt = st.countTokens();
		for(int i = 0; i < tokencnt; i++)
		{
			String curIP = st.nextToken().trim();
			if (curIP.length()<=6)
				continue;
			if(checkIfValidIP(curIP))
			{
				mediaProxyPublicIPList[mediaPublicIPcnt++] = curIP;
				//logger.debug("Public IP: "+curIP);
			}
			else
			{
				logger.debug("Invalid IP: "+curIP);
			}
		}
		
		if(mediaPublicIPcnt > 0)
			return true;
		else
			return false;
	}
	
	
	public  boolean voiceListenIPAvailabilityTest(String ip)
	{
		try{
			InetAddress addr = InetAddress.getByName(ip);
			NetworkInterface interf = NetworkInterface.getByInetAddress(addr);
			if(interf.isUp() && !interf.isLoopback())
			{
				return true;
			}
		}
		catch(UnknownHostException uhe){
			logger.error(uhe);
		}
		catch(SecurityException se){
			logger.error(se);
		}
		catch (SocketException se) {
			logger.error(se);
		}
		catch (NullPointerException npe) {
			logger.error("InetAddress can't be created with the ip: " + ip);
		}
		catch(Throwable th)
		{
			logger.error("InetAddress can't be created with the ip: " + ip);
		}
		
		return false;
	}
	
	public boolean changeVoiceListenIP(double packetLoss)
	{
		loadCurUsedVoiceListenIP();
		int badIPcnt = 0;
		while (true)
		{
			int nextIndex = (upToUsedIPIndexFromList+1) % voiceListenIPcnt;
			logger.debug(nextIndex+":"+upToUsedIPIndexFromList+":"+voiceListenIPcnt);
			if((!presentUsedIP.equals(voiceListenIPList[nextIndex])) 
					&& voiceListenIPAvailabilityTest(voiceListenIPList[nextIndex]))
			{
				if( changeVoiceListenIP(voiceListenIPList[nextIndex]))
				{
					String prevIP = presentUsedIP;
					upToUsedIPIndexFromList = nextIndex;
					presentUsedIP = voiceListenIPList[upToUsedIPIndexFromList];
					logger.debug("Changing public IP to : "+mediaProxyPublicIPList[nextIndex]);
					changeMediaProxyPublicIP(mediaProxyPublicIPList[nextIndex]);
					
					logger.debug("Voice Listen IP of "+IPChanger.mediaProxyConfigFile+" changed from "+ prevIP+" to "+presentUsedIP);
					// Mailing..
					if(IPChanger.enableMailSending)
					{
						sendIPChangingNotificationMail(prevIP, presentUsedIP, packetLoss);
					}
					return true;
				}
				else{
					logger.error("failed to change IP!");
					return false;
				}
			}
			else
			{
				badIPcnt++;
				if (badIPcnt == voiceListenIPcnt)
				{				
					logger.debug("No active IP avaiable other than currently used IP. So Voice Listen IP cann't be changed.");
					return false;
				}
				
				logger.debug("Trying for next IP ...");
				upToUsedIPIndexFromList = nextIndex;
			}
		}

	}
	
	public void loadCurUsedVoiceListenIP()
	{
		
		FileInputStream fileInputStream = null;
		try {										
			
			String strConfigFileName = IPChanger.mediaProxyConfigFile;
			Properties properties = new Properties();
			File configFile = new File(strConfigFileName);
			if (configFile.exists())
			{
				fileInputStream = new FileInputStream(strConfigFileName);
				properties.load(fileInputStream);
				if(properties.get("voiceListenIP")!=null){
					presentUsedIP = (String)properties.get("voiceListenIP");
					upToUsedIPIndexFromList = Arrays.asList(voiceListenIPList).lastIndexOf(presentUsedIP);
		        }	
			}
		}
		catch(NullPointerException npe)
		{
			logger.error(npe);
		}
		catch(FileNotFoundException fnfe)
		{
			logger.error(fnfe);
		}
		catch(SecurityException se){
			logger.error(se);
		}
		catch(IOException ioe)
		{
			logger.error(ioe);
		}
		finally{
			try{
				if(fileInputStream != null)
					fileInputStream.close();
			}
			catch(IOException ioe)
			{
				logger.error(ioe);
			}
		}
			
	}
	
	public boolean changeMediaProxyPublicIP(String newIP)
	{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {			
			Properties prop =new Properties();
   			prop.load(new FileInputStream(IPChanger.signalingConfigFile));
   			String value = prop.getProperty("mediaProxyPublicIP");
			
			if(value!=null)
			{
				fis = new FileInputStream(new File(IPChanger.signalingConfigFile));
				int fileLength =(int) fis.getChannel().size();
				fis.read(fileData, 0, fileLength);
				String data = new String(fileData, 0, fileLength);				
				saveOldFile("server.cfg",data);	
				
				data = data.replace("mediaProxyPublicIP="+value,"mediaProxyPublicIP="+newIP);	
				fos = new FileOutputStream(new File(IPChanger.signalingConfigFile));
				fos.write(data.getBytes());
				fos.flush();				
	   			
			}
			else
			{
				return false;
			}
			
		}
		catch(NullPointerException npe)
		{
			logger.error(npe);
			return false;
		}
		catch(FileNotFoundException fnfe)
		{
			logger.error(fnfe);
			return false;
		}
		catch(SecurityException se){
			logger.error(se);
			return false;
		}
		catch(IOException ioe)
		{
			logger.error(ioe);
			return false;
		}
		finally{
			try{
				if(fis != null)
					fis.close();
				if(fos != null)
					fos.close();
			}
			catch(IOException ioe)
			{
				logger.error(ioe);
			}
		}
		
		return true;
	}
	
	public boolean changeVoiceListenIP(String newIP)
	{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {			
			
			
			Properties prop =new Properties();
   			prop.load(new FileInputStream(IPChanger.mediaProxyConfigFile));
   			String value = prop.getProperty("voiceListenIP");
			
			if(value!=null)
			{
				fis = new FileInputStream(new File(IPChanger.mediaProxyConfigFile));
				int fileLength =(int) fis.getChannel().size();
				fis.read(fileData, 0, fileLength);
				String data = new String(fileData, 0, fileLength);				
				saveOldFile("rtpProperties",data);	
				
				data = data.replace("voiceListenIP="+value,"voiceListenIP="+newIP);	
				fos = new FileOutputStream(new File(IPChanger.mediaProxyConfigFile));				
				fos.write(data.getBytes());				
				fos.flush();			
	   			
			}
			else
			{
				return false;
			}
			
		}
		catch(NullPointerException npe)
		{
			logger.error(npe);
			return false;
		}
		catch(FileNotFoundException fnfe)
		{
			logger.error(fnfe);
			return false;
		}
		catch(SecurityException se){
			logger.error(se);
			return false;
		}
		catch(IOException ioe)
		{
			logger.error(ioe);
			return false;
		}
		finally{
			try{
				if(fis != null)
					fis.close();
				if(fos != null)
					fos.close();
			}
			catch(IOException ioe)
			{
				logger.error(ioe);
			}
		}
		
		return true;
	}

	public boolean checkIfValidIP(String testIP)
	{
		StringTokenizer st = new StringTokenizer(testIP, ".");
		if(st.countTokens() == 4)
		{			
			try{
				int a = Integer.parseInt(st.nextToken());
				if(a<0||a>255)return false;
				a = Integer.parseInt(st.nextToken());
				if(a<0||a>255)return false;
				a = Integer.parseInt(st.nextToken());
				if(a<0||a>255)return false;
				a = Integer.parseInt(st.nextToken());
				if(a<0||a>255)return false;
			}
			catch(NumberFormatException e)
			{
				return false;
			}
			return true;
		}
		return false;
	}
	
	public boolean saveOldFile(String fname,String oldFileData)
	{
		FileOutputStream fos = null;
		try{
			String dateStr = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());
			fos = new FileOutputStream(new File("IPChangerBackup/Properties/"+fname+dateStr+".cfg"));//define backup location 
			fos.write(oldFileData.getBytes());
			
			return true;
		}
		catch(FileNotFoundException fnfe){
			logger.error(fnfe);
		}
		catch(SecurityException se){
			logger.error(se);
		}
		catch (IOException ioe) {
			logger.error(ioe);
		}
		finally{
			try{
				if(fos != null)
					fos.close();
			}
			catch (IOException ioe) {
				logger.error(ioe);
			}
		}
		
		return false;
	}
	
	public  void sendIPChangingNotificationMail(String prevIP, String newIP, double packetLoss)
	{
		String subject = "Voice Listening IP is changed to a new one";				

		String body = "";
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>");
		sb.append("Alert");
		sb.append("</title>");
		sb.append("</head>");
		sb.append("<body>");
		
		sb.append("<p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
		sb.append("Dear Concern,<br>");
		sb.append("Voice Listening IP  of "+IPChanger.mediaProxyConfigFile+" file has been changed.");
		sb.append("</p>");
		
		
		sb.append("<table border=\"1\" width=400  cellspacing=0 cellpadding=0 align=left style=\"border-collapse:collapse;border:none\">");
		
		sb.append("<tr style=\"background:#568BE1;\">");		
		sb.append("<td>");
		sb.append("<p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:white;\'>");
		sb.append("New IP");
		sb.append("</p>");	
		sb.append("</td>");
		sb.append("<td>");
		sb.append("<p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:white;\'>");
		sb.append("Previous IP");
		sb.append("</p>");	
		sb.append("</td>");		
		sb.append("</tr>");
		
		sb.append("<tr>");		
		sb.append("<td>");
		sb.append("<p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
		sb.append(newIP);
		sb.append("</p>");	
		sb.append("</td>");
		sb.append("<td>");
		sb.append("<p style=\'font-size:10.0pt;font-family:\"Century Gothic\";color:black;\'>");
		sb.append(prevIP);
		sb.append("</p>");	
		sb.append("</td>");		
		sb.append("</tr>");
		sb.append("</table>");		
		
		if (packetLoss > 0.0)
		{			
			sb.append("<br><br><br><p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
			sb.append("Data loss for a source ISP with media IP "+prevIP+" was "+packetLoss+"% for last "+IPChanger.testingTimeInereval+" Minutes<br>");			
			sb.append("</p>");
			
		}
		sb.append("<br><br><br><p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
	
		if ((voiceListenIPcnt - upToUsedIPIndexFromList-1) == 2 )
		{			
			
			sb.append("ALERT: Only 2" +" free IPs are remaining in list. Add more IPs or take necessary steps.<br>");			
			sb.append("Remaining IPs are: <br><br>" + voiceListenIPList[voiceListenIPcnt-2] + "<br>"+  voiceListenIPList[voiceListenIPcnt-1]+"<br>");
						
		}
		else if ((voiceListenIPcnt - upToUsedIPIndexFromList-1) == 1 )
		{
			
			sb.append("ALERT: Only 1" +" free IP is remaining in list. Add more IPs or take necessary steps.<br>");
			sb.append("Remaining IP is: " + voiceListenIPList[voiceListenIPcnt-1]);
		
		}
		else if ((voiceListenIPcnt - upToUsedIPIndexFromList-1) == 0 )
		{
			sb.append("ALERT: No free IP is remaining in list! Add more IPs or take necessary steps.");
		}
		sb.append("</p>");
		
		
		sb.append("<br><br><br><p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
		sb.append("Note that this is auto generated Mail, Please don't reply.<br>");	
		sb.append("</p>");
		
		
		sb.append("<br><br><br><p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
		sb.append("Regards,<br>");
		sb.append("iTel Support Team");
		sb.append("</p>");
		
		sb.append("</body>");
		sb.append("</html>");
		
		body=sb.toString();		
		EmailUtil.sendEmail(IPChanger.toEmail, subject, body);
		
		
	}
	
	public  void sendStartupIPSettingNotificationMail(String newIP)
	{
		String subject = "Startup Time Voice Listening IP";
		
		
		String body = "";
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>");
		sb.append("Alert");
		sb.append("</title>");
		sb.append("</head>");
		sb.append("<body>");
		
		sb.append("<p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
		sb.append("Dear Concern,<br>");
		sb.append("Voice Listening IP  of "+IPChanger.mediaProxyConfigFile+" file currently is in used : "+newIP);
		sb.append("</p>");
				
		
		sb.append("<br><br><br><p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
	
		if ((voiceListenIPcnt - upToUsedIPIndexFromList-1) == 2 )
		{			
			
			sb.append("ALERT: Only 2" +" free IPs are remaining in list. Add more IPs or take necessary steps.<br>");			
			sb.append("Remaining IPs are: <br><br>" + voiceListenIPList[voiceListenIPcnt-2] + "<br>"+  voiceListenIPList[voiceListenIPcnt-1]+"<br>");
						
		}
		else if ((voiceListenIPcnt - upToUsedIPIndexFromList-1) == 1 )
		{
			
			sb.append("ALERT: Only 1" +" free IP is remaining in list. Add more IPs or take necessary steps.<br>");
			sb.append("Remaining IP is: " + voiceListenIPList[voiceListenIPcnt-1]);
		
		}
		else if ((voiceListenIPcnt - upToUsedIPIndexFromList-1) == 0 )
		{
			sb.append("ALERT: No free IP is remaining in list! Add more IPs or take necessary steps.");
		}
		sb.append("</p>");
		
		
		sb.append("<br><br><br><p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
		sb.append("Note that this is auto generated Mail, Please don't reply.<br>");	
		sb.append("</p>");
		
		
		sb.append("<br><br><br><p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
		sb.append("Regards,<br>");
		sb.append("iTel Support Team");
		sb.append("</p>");
		
		sb.append("</body>");
		sb.append("</html>");
		
		body=sb.toString();		
		EmailUtil.sendEmail(IPChanger.toEmail, subject, body);
		
		
	}
	
	public void sendStartupIPStatusNotificationMail()
	{
		String subject = "Startup Voice Listening IP Status";
		
		String body = "";
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>");
		sb.append("Alert");
		sb.append("</title>");
		sb.append("</head>");
		sb.append("<body>");
		
		sb.append("<p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
		sb.append("Dear Concern,<br>");
		sb.append("Below the Voice Listening IP  Status: ");
		sb.append("</p>");
				
		
		sb.append("<br><br><br><p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
	
		
		if(voiceListenIPcnt == 0)
		{
			sb.append("No IP presents in "+IPChanger.mediaProxyConfigFile+" file is valid voice listening IP<br>");
		}
		else if(voiceListenIPcnt == 1)
		{
			sb.append("Only One IP presents in "+IPChanger.mediaProxyConfigFile+" file is valid voice listening IP.\r\nThe IP is :  " + voiceListenIPList[0]);
		}
		else if(voiceListenIPcnt > 1)
		{
			sb.append(voiceListenIPcnt + " IPs presents in "+IPChanger.mediaProxyConfigFile+" file are valid voice listening IP\r\nFollowing are these IPs:<br>");
			for (int i = 0; i < voiceListenIPcnt; i++)
			{
				sb.append(voiceListenIPList[i]+"<br>");
			}
		}
		
		if( testFailedIPcnt > 0)
		{
			sb.append("<br>Invalid or Unreachable IP ststus:<br>");
			for (int i = 0; i < testFailedIPcnt; i++)
			{
				sb.append(testFailedIPList[i]+ " : "+ testFailedIPListInfo[i] + "<br>");
			}
		}
		
		sb.append("</p>");
		
		
		sb.append("<br><br><br><p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
		sb.append("Note that this is auto generated Mail, Please don't reply.<br>");	
		sb.append("</p>");
		
		
		sb.append("<br><br><br><p style=\'font-size:11.0pt;font-family:\"Century Gothic\";color:black;\'>");
		sb.append("Regards,<br>");
		sb.append("iTel Support Team");
		sb.append("</p>");
		
		sb.append("</body>");
		sb.append("</html>");
		
		body=sb.toString();		
		EmailUtil.sendEmail(IPChanger.toEmail, subject, body);
		
		
	}

}
