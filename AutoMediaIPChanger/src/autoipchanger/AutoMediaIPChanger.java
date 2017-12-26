package autoipchanger;

import ipchangershutdown.ShutDownListener;
import ipchangershutdown.ShutDownService;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class AutoMediaIPChanger implements ShutDownListener
{
	public static final String APPLICATION_NAME = "AutoMediaIPChanger";
	public static String VERSION = "1.0.3";
	static Logger logger = Logger.getLogger(autoipchanger.AutoMediaIPChanger.class);

	
	public static AutoMediaIPChanger autoMediaIPChanger=null;
	public static IPChanger ipChanger = null;
	public static IPLoader iploader = null;
	
	public static void main(String[] args)	
	{			
		PropertyConfigurator.configure("log4jIPC.properties");
		
		autoMediaIPChanger = new AutoMediaIPChanger();
		
		System.out.println("Starting AutoMediaIPChanger version :"+VERSION);	
		logger.debug("Starting AutoMediaIPChanger version :"+VERSION+ " at "+new java.util.Date());
	
		ipChanger = IPChanger.createIPChanger();
		iploader = IPLoader.createIPLoader();
		IPPool.getInstance();
		
		ShutDownService.getInstance().setShutDownListener(autoMediaIPChanger);
		ipChanger.start();
		iploader.start();
		
		System.out.println("AutoMediaIPChanger started successfully");
		
	} 
	

	public void shutDown()
	{
		ipChanger.running = false;
		try{
			Thread.sleep(1000L);
		}catch(Exception e){}
		logger.debug("Voice Listening IP Changer shutting down successfully at "+new java.util.Date());
		System.out.println("Voice Listening IP Changer shutting down successfully.");
        System.exit(0);
	}

}
