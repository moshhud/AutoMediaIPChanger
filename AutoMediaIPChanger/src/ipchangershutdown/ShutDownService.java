package ipchangershutdown;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;

import autoipchanger.IPChanger;


public class ShutDownService extends Thread 
{	
	static Logger logger = Logger.getLogger(ShutDownService.class);
    boolean running;
    public ShutDownListener shutDownProcess;
    private static ShutDownService shutDownService = null;
    
    private ShutDownService()
    {
        File file = new File(IPChanger.shutdownFile);
        file.delete();
        start();
    }
    
    public void setShutDownListener(ShutDownListener sdl)
    {
    	shutDownProcess = sdl;
    }
    
    private static synchronized void createShutDownThread()
    {
        if(shutDownService == null)
        {
            shutDownService = new ShutDownService();
        }
    }
    
    public static ShutDownService getInstance()
    {
        if(shutDownService == null)
        {
            createShutDownThread();
        }
        return shutDownService;
    }
    
    public void run()
    {
    	try 
    	{
            running = true;
    		
            while (running)
    		{
			 	File file = new File(IPChanger.shutdownFile);
		        if(file.exists())
		        {
		        	logger.debug("Going To ShutDown At:" + new Date());
		            if(!file.delete())
		            {
		                file.deleteOnExit();
		            }
		            stopService();
		            
		            ShutDownListener sdl = (ShutDownListener)shutDownProcess;
		            sdl.shutDown();		
		        }
		        Thread.sleep(1000L);
    		}
    	}
    	catch(Exception e){
    		logger.error(e);
    	}
    }
    
    public void stopService()
    {
        running = false;
    }
}
