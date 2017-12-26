package autoipchanger;


import org.apache.log4j.Classloader;
import org.apache.log4j.Logger;


public abstract class IPPool 
{
	private static IPPool instance = null;
	public String voiceListenIPList[];
	public String mediaProxyPublicIPList[];
	public int voiceListenIPcnt;
	public int mediaPublicIPcnt;
	public int upToUsedIPIndexFromList=-1;
	public byte[] fileData;
	
	public String testFailedIPList[];
	public String testFailedIPListInfo[];
	public int testFailedIPcnt;
	public String presentUsedIP;
	public static Logger logger = Logger.getLogger(autoipchanger.IPPool.class);	
	private static Class ipPoolClass;
	
	protected IPPool()
	{
		voiceListenIPList = new String[100];
		mediaProxyPublicIPList = new String[100];
		testFailedIPList = new String[100];
		testFailedIPListInfo = new String[100];
		voiceListenIPcnt = 0;
		mediaPublicIPcnt = 0;
		testFailedIPcnt = 0;
		upToUsedIPIndexFromList = -1;
		presentUsedIP = null;
		
		fileData = new byte[2048];
	}
	
	public synchronized static IPPool getInstance()
	{
		if(instance == null)
		{
			instance = createIPPool();
		}
		
		return instance;
	}
	
	
	  public synchronized static IPPool createIPPool()
	  {
	      while(true)
	      {
	        try
	        {
	          if(ipPoolClass==null)
	          {
	            Classloader loader = new Classloader();
	            ipPoolClass = loader.loadClass("autoipchanger.IPPoolImplementation");
	          }
	          return  (IPPool) ipPoolClass.newInstance();
	        }
	        catch (Exception ex) {}
	      }
	  }
	
	public abstract void loadVoiceListenIPListAndSet();
	public abstract boolean loadVoiceListenIPList();	
	public abstract boolean voiceListenIPAvailabilityTest(String ip);
	public abstract boolean changeVoiceListenIP(double packetLoss);
	public abstract void loadCurUsedVoiceListenIP();
	public abstract boolean changeVoiceListenIP(String newIP);
	public abstract boolean checkIfValidIP(String testIP);
	public abstract boolean saveOldFile(String fname,String oldFileData);
	public abstract  void sendIPChangingNotificationMail(String prevIP, String newIP, double packetLoss);
	public abstract  void sendStartupIPSettingNotificationMail(String newIP);
	public abstract void sendStartupIPStatusNotificationMail();
	}
