package autoipchanger;

import org.apache.log4j.Classloader;
import org.apache.log4j.Logger;

public abstract class IPLoader extends Thread{
	public static boolean running = false;
	
	public static Logger logger = Logger.getLogger(autoipchanger.IPLoader.class);
	private static Class iploaderClass;
	
	public IPLoader(){
		
	}
	
	public synchronized static IPLoader createIPLoader()
	  {
	      while(true)
	      {
	        try
	        {
	          if(iploaderClass==null)
	          {
	        	logger.debug("Starting IP loader...");
	            Classloader loader = new Classloader();
	            iploaderClass = loader.loadClass("autoipchanger.IPLoaderImplementation");
	          }
	          return  (IPLoader) iploaderClass.newInstance();
	        }
	        catch (Exception ex) {}
	      }
	  }
	
	public abstract void loadVoiceListenIPFromDB();
	public abstract int decodeBytes(byte [] data,int len);
	public abstract int encodeBytes(byte [] data,int len);
	
	
	

}
