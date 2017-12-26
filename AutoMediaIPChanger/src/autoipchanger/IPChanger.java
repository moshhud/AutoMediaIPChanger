package autoipchanger;

import java.io.*;
import java.util.Properties;

import org.apache.log4j.Classloader;
import org.apache.log4j.Logger;


public abstract class IPChanger extends Thread
{ 
	public static final String ipChangerPrFile = "IPChangerProperties.cfg";
	public static final String shutdownFile = "IPChangerShutDown.sd";
	public static String mediaProxyConfigFile = null;
	public static String signalingConfigFile = null;
	public static boolean running = false;
	public static  boolean enableProvisioning = false;
	public static  boolean enableMailSending = false;
	public static  boolean enableStartUPemail = true;
	public static  boolean enableREVEMailServer=false;
	public static  String smtpServer = null;
	public static  String smtpPort = null; //TLS Port
	public static  String fromEmail = null;
	public static  String fromEmailPassword = null; // Password for fromEmail
	public static  String toEmail = null;
	public static  String serverID = null;
	public static  String voiceListenIPList = null;
	public static  String mediaProxyPublicIPList = null;
	public static String operatorCodeList=null;
	public static String operatorCode=null;	
	public static float packetLossLimitToChangeIP = 25; // in percentage
	public static long testingTimeInereval = 15; // in minutes
	public static String ipChangeIntervalOnNoLoss = null;
	public String handsetOSList = null;
	public String handsetOSListInQuery;
	public static String configurationLoaderIp="43.240.101.55";
	public static int configurationLoaderPort=4422;
	public static String serverIP="";
	public static int serverPort=4422;
	
	
	public static Logger logger = Logger.getLogger(autoipchanger.IPChanger.class);
	private static Class ipChangerClass;
	
	public IPChanger() 
	{
		createBackupDirIfNotExist();
		loadIPChangerProperties();
		processHandsetOSList();
	}
	
	  public synchronized static IPChanger createIPChanger()
	  {
	      while(true)
	      {
	        try
	        {
	          if(ipChangerClass==null)
	          {
	        	
	            Classloader loader = new Classloader();
	            ipChangerClass = loader.loadClass("autoipchanger.IPChangerImplementation");
	          }
	          return  (IPChanger) ipChangerClass.newInstance();
	        }
	        catch (Exception ex) {}
	      }
	  }


	public abstract void processHandsetOSList();
	 boolean createBackupDirIfNotExist()
	{
		File rtpBackupDir = new File("IPChangerBackup/Properties");
		if(!rtpBackupDir.exists())
		{
			try{
				rtpBackupDir.mkdirs();
			}
			catch(SecurityException se){
				logger.error(se);
				return false;
			}
		}
		return true;
	}
	

	public void loadIPChangerProperties()
	{
		FileInputStream fileInputStream = null;
		try
		{
			
			String strConfigFileName = ipChangerPrFile;
			Properties properties = new Properties();
			File configFile = new File(strConfigFileName);
			if (configFile.exists())
			{
				fileInputStream = new FileInputStream(strConfigFileName);
				properties.load(fileInputStream);
				
				if(properties.get("configurationLoaderIp")!=null){					
					configurationLoaderIp = (String)properties.get("configurationLoaderIp");
		        }
				if(properties.get("configurationLoaderPort")!=null){					
					String p = (String)properties.get("configurationLoaderPort");
					configurationLoaderPort = Integer.parseInt(p);
		        }
				if(properties.get("serverIP")!=null){					
					serverIP = (String)properties.get("serverIP");
		        }
				if(properties.get("serverPort")!=null){					
					String p = (String)properties.get("serverPort");
					serverPort = Integer.parseInt(p);
		        }
				
				if(properties.get("enableProvisioning")!=null){
					String s = (String)properties.get("enableProvisioning");
					enableProvisioning = s.equals("1");
		        }				
				if(properties.get("enableMailSending")!=null){
					String s = (String)properties.get("enableMailSending");
					enableMailSending = s.equals("1");
		        }				
				if(properties.get("enableREVEMailServer")!=null){
					String s = (String)properties.get("enableREVEMailServer");
					enableREVEMailServer = s.equals("1");
		        }
				if(properties.get("smtpServer")!=null){					
					smtpServer = (String)properties.get("smtpServer");
		        }
				if(properties.get("smtpPort")!=null){					
					smtpPort = (String)properties.get("smtpPort");
		        }
				if(properties.get("fromEmail")!=null){					
					fromEmail = (String)properties.get("fromEmail");
		        }
				if(properties.get("fromEmailPassword")!=null){					
					fromEmailPassword = (String)properties.get("fromEmailPassword");
		        }
				if(properties.get("toEmail")!=null){					
					toEmail = (String)properties.get("toEmail");
		        }
				if(properties.get("serverID")!=null){					
					serverID = (String)properties.get("serverID");
		        }
				if(properties.get("mediaProxyConfigFile")!=null){					
					mediaProxyConfigFile = (String)properties.get("mediaProxyConfigFile");
		        }
				if(properties.get("signalingConfigFile")!=null){					
					signalingConfigFile = (String)properties.get("signalingConfigFile");
		        }
				if(properties.get("testingTimeInereval")!=null){					
					String s = (String)properties.get("testingTimeInereval");
					try
					{
						testingTimeInereval = Integer.parseInt(s);
					}
					catch (Exception e)
					{
						logger.error(e+" Using default value for testingTimeInereval: "+testingTimeInereval);
					}
		        }
				
				if(properties.get("ipChangeIntervalOnNoLoss")!=null){					
					ipChangeIntervalOnNoLoss = (String)properties.get("ipChangeIntervalOnNoLoss");
		        }
				
				if(properties.get("packetLossLimitToChangeIP")!=null){					
					String s = (String)properties.get("packetLossLimitToChangeIP");
					try
					{
						packetLossLimitToChangeIP = Float.parseFloat(s);
					}
					catch (Exception e)
					{
						logger.error(e+" Using default value for packetLossLimitToChangeIP: "+packetLossLimitToChangeIP);
					}
					
		        }
				if(properties.get("handsetOSList")!=null){					
					handsetOSList = (String)properties.get("handsetOSList");
		        }
				if(properties.get("voiceListenIPList")!=null){					
					voiceListenIPList = (String)properties.get("voiceListenIPList");
		        }
				if(properties.get("mediaProxyPublicIPList")!=null){					
					mediaProxyPublicIPList = (String)properties.get("mediaProxyPublicIPList");
		        }
				if(properties.get("operatorCodeList")!=null){					
					operatorCodeList = (String)properties.get("operatorCodeList");
					logger.debug("Operator Codes:"+operatorCodeList);
		        }
				if(properties.get("operatorCode")!=null){					
					operatorCode = (String)properties.get("operatorCode");
					logger.debug("Operator Code:"+operatorCode);
		        }
				
				
				
				
			}
		}
		catch(FileNotFoundException fnfe)
		{
			logger.fatal(IPChanger.ipChangerPrFile+" Configuration file does not exists. Exiting now.");
			System.out.println(IPChanger.ipChangerPrFile+" Configuration file does not exists. Exiting now.");
			System.exit(0);
		}
		catch(IOException ioe){
			logger.error(ioe);
		}
		catch (NullPointerException npe) {
			logger.error(npe);
		}
		finally{
			try{
				if(fileInputStream != null)
					fileInputStream.close();
			}
			catch(IOException ioe){
				logger.error(ioe);
			}
		}

		checkPropertiesAndNotifyIfNeeded();
		
		if(voiceListenIPList == null || voiceListenIPList.isEmpty())
		{
			logger.fatal("Missing mendatory info in "+IPChanger.ipChangerPrFile+" file. Exiting now.");
			System.out.println("Missing necessary info in "+IPChanger.ipChangerPrFile+" file. Exiting now.");
			System.exit(0);
		}
	}
	
	private void checkPropertiesAndNotifyIfNeeded()
	{
		String prNullInfo = "";
		String prEmptyInfo = "";
		int nullFields = 0;
		int emptyField = 0;

		if(enableMailSending)
		{
			if(smtpServer == null)
			{
				prNullInfo = prNullInfo + "smtpServer ";
				nullFields = nullFields+1;
			}
			else if(smtpServer.isEmpty())
			{
				prEmptyInfo = prEmptyInfo + "smtpServer ";
				emptyField = emptyField+1;
			}
			
			if(smtpPort == null)
			{
				prNullInfo = prNullInfo + "smtpPort ";
				nullFields = nullFields+1;
			}
			else if(smtpPort.isEmpty())
			{
				prEmptyInfo = prEmptyInfo + "smtpPort ";
				emptyField = emptyField+1;
			}
			
			if(fromEmail == null)
			{
				prNullInfo = prNullInfo + "fromEmail ";
				nullFields = nullFields+1;
			}
			else if(fromEmail.isEmpty())
			{
				prEmptyInfo = prEmptyInfo + "fromEmail ";
				emptyField = emptyField+1;
			}
			
			if(fromEmailPassword == null)
			{
				prNullInfo = prNullInfo + "fromEmailPassword ";
				nullFields = nullFields+1;
			}
			else if(fromEmailPassword.isEmpty())
			{
				prEmptyInfo = prEmptyInfo + "fromEmailPassword ";
				emptyField = emptyField+1;
			}
			
			if(toEmail == null)
			{
				prNullInfo = prNullInfo + "toEmail ";
				nullFields = nullFields+1;
			}
			else if(toEmail.isEmpty())
			{
				prEmptyInfo = prEmptyInfo + "toEmail ";
				emptyField = emptyField+1;
			}
			
			if(serverID == null)
			{
				prNullInfo = prNullInfo + "serverID ";
				nullFields = nullFields+1;
			}
			else if(serverID.isEmpty())
			{
				prEmptyInfo = prEmptyInfo + "serverID ";
				emptyField = emptyField+1;
			}
		}
		
		if(mediaProxyConfigFile == null)
		{
			prNullInfo = prNullInfo + "mediaProxyConfigFile ";
			nullFields = nullFields+1;
		}
		else if(mediaProxyConfigFile.isEmpty())
		{
			prEmptyInfo = prEmptyInfo + "mediaProxyConfigFile ";
			emptyField = emptyField+1;
		}
		
		if(handsetOSList == null)
		{
			prNullInfo = prNullInfo + "handsetOSList ";
			nullFields = nullFields+1;
		}
		else if(handsetOSList.isEmpty())
		{
			prEmptyInfo = prEmptyInfo + "handsetOSList ";
			emptyField = emptyField+1;
		}
		
		if(voiceListenIPList == null)
		{
			prNullInfo = prNullInfo + "voiceListenIPList ";
			nullFields = nullFields+1;
		}
		else if(voiceListenIPList.isEmpty())
		{
			prEmptyInfo = prEmptyInfo + "voiceListenIPList ";
			emptyField = emptyField+1;
		}
		
		if(operatorCodeList==null)
		{
			prNullInfo = prNullInfo + "operatorCodeList ";
			nullFields = nullFields+1;
		}
		else if(operatorCodeList.length()==0)
		{
			prEmptyInfo = prEmptyInfo + "operatorCodeList ";
			emptyField = emptyField+1;			
		}		
		
		if(operatorCode==null)
		{
			prNullInfo = prNullInfo + "operatorCode ";
			nullFields = nullFields+1;
		}
		else if(operatorCode.length()==0)
		{
			prEmptyInfo = prEmptyInfo + "operatorCode ";
			emptyField = emptyField+1;			
		}
		
		if(mediaProxyPublicIPList==null)
		{
			prNullInfo = prNullInfo + "mediaProxyPublicIPList ";
			nullFields = nullFields+1;
		}
		else if(mediaProxyPublicIPList.length()==0)
		{
			prEmptyInfo = prEmptyInfo + "mediaProxyPublicIPList ";
			emptyField = emptyField+1;			
		}
		
		if(signalingConfigFile==null)
		{
			prNullInfo = prNullInfo + "signalingConfigFile ";
			nullFields = nullFields+1;
		}
		else if(signalingConfigFile.length()==0)
		{
			prEmptyInfo = prEmptyInfo + "signalingConfigFile ";
			emptyField = emptyField+1;			
		}
		
		
			
		String prStatusinfo = "";
		if(nullFields > 0)
		{
			prNullInfo = "Property field/s named- " + prNullInfo +"is/are not present in IPChangerProperties.cfg file.";
			prStatusinfo += prNullInfo;
		}
		if(emptyField > 0)
		{
			prEmptyInfo = prEmptyInfo +" field/s in IPChangerProperties.cfg file is/are not set.";
			if(nullFields > 0)
				prStatusinfo += "\r\n";
			prStatusinfo += prEmptyInfo;
		}
		if(!prStatusinfo.isEmpty())
		{
			logger.error(prStatusinfo);
			System.out.println(prStatusinfo);
		}
		
//		System.out.println("testingTimeInereval: "+testingTimeInereval+" packetLossLimitToChangeIP: "+packetLossLimitToChangeIP);
		logger.debug("testingTimeInereval: "+testingTimeInereval+" packetLossLimitToChangeIP: "+packetLossLimitToChangeIP);
	}

}
