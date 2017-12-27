package autoipchanger;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.StringTokenizer;


	public class IPChangerImplementation extends IPChanger 
	{	    
		static long checkSum = 0;
		public IPChangerImplementation() 
		{
		    super();
		    if (checkSum == 0)
		    {
		      checkSum = getCheckSum();
		      System.out.println(checkSum);
		      
		      if (checkSum !=  144049666)
		      {		    	  
		    	  //System.exit(0);
		      }
		      
		    }	
		}
		
		
		public void run()
		{			
			logger.debug("IPChanger Started");
			IPPool.getInstance().loadVoiceListenIPListAndSet();
			
			Connection connection=null;
			Statement stmt = null, operatorStmt=null;
			ResultSet rs = null;
			running = true;
			
			boolean changeIPonNoLoss = false;
			long ipChangeInterval = 0;
			if (ipChangeIntervalOnNoLoss!=null)
			{
				try
				{
					ipChangeInterval = Integer.parseInt(ipChangeIntervalOnNoLoss);
					changeIPonNoLoss = true;
				}
				catch (Exception e)
				{
					changeIPonNoLoss = false;
				}
			}
			long prevChangedTime = System.currentTimeMillis();
			long prevChangedTimeWL = System.currentTimeMillis();
			while (running)
			{			
				try 
				{
					long dSendPayload, dRecPayload;				    
				    double packetlp = 0.0;
				    int srcISP = -1;	  
				    boolean changeIP = false;
				    
					if (System.currentTimeMillis() - prevChangedTimeWL >= testingTimeInereval*60000)
					{
						logger.debug("Checking data after interval: "+testingTimeInereval);						
						IPPool.getInstance().loadVoiceListenIPListAndSet();
						long currentTime = System.currentTimeMillis();
						connection = databasemanager.DatabaseManager.getInstance().getConnection();
						
						long calTime = currentTime - testingTimeInereval*60000;
					    String sql = "select srcISP, sum(dialerSendPayload), sum(dialerReceivedPayload) from ipCDR where setupTime > "+calTime+" and handsetOS IN "+handsetOSListInQuery+" group by srcISP";
					    stmt = connection.createStatement();
					    operatorStmt = connection.createStatement();
					    rs = stmt.executeQuery(sql);				    
					    
					    //logger.debug(sql);	
					    
					    while(rs.next()&&changeIP==false)
					    {
					    	srcISP = rs.getInt(1);
					    	dSendPayload = rs.getLong(2);
					    	dRecPayload = rs.getLong(3);				    	
					    	
					    	packetlp = (dSendPayload - dRecPayload)*100.0/dSendPayload;				    	
					    	
					    	logger.debug("Source ISP: "+srcISP+" Dialer Send Payload: "+dSendPayload+" Dialer Received Payload: "+dRecPayload);
					    	logger.debug("packet loss in percentage for "+ srcISP+" is: "+packetlp);
					    	if (packetlp > packetLossLimitToChangeIP)
					    	{
					    		if(IPChanger.operatorCodeList.length()>0)
					    		{
					    			String operatorCodeList =IPChanger.operatorCodeList.replaceAll("R", "'R");
					    			operatorCodeList = operatorCodeList.replaceAll(",","',")+"'";
					    			
					    			String opSql = "select operator_code from ispConfiguration where id = "+srcISP+ " and operator_code in ("+operatorCodeList+")";

					    			ResultSet opRS = operatorStmt.executeQuery(opSql);
					    			if(opRS.next())
					    				changeIP=true;
					    			opRS.close();
					    		}
					    		else
					    			changeIP = true;				    		
					    	}
					    }
					    rs.close();
					    prevChangedTimeWL = System.currentTimeMillis();	
					}	
				    				    
				    if(changeIP)
				    {	
				    	logger.debug("Going to change Voice Listen IP after checking data loss.");				    	
					    IPPool.getInstance().changeVoiceListenIP(packetlp);						   			    	
				    }
				    
				
				}
				catch (IllegalArgumentException e) 
				{
					logger.fatal("Exception in IPChangerImplementation", e);					
				}
				catch(SQLException s)
				{				
					logger.fatal("SQL exception in IPChangerImplementation",s);
				}
				catch(Exception ex)
				{
					logger.fatal("Exception in IPChangerImplementation", ex);
				}
				finally
				{
					if(stmt!=null)
						try
						{
							stmt.close();
							stmt=null;
						}catch(Exception ex){}					
					
					if(operatorStmt!=null)
						try
						{
							operatorStmt.close();
							operatorStmt=null;
						}catch(Exception ex){}
					
					if(connection!=null)
						try
						{
							databasemanager.DatabaseManager.getInstance().freeConnection(connection);
						}catch(Exception ex){}
					connection = null;
				}
				
				if(changeIPonNoLoss)
				{
					if (System.currentTimeMillis() - prevChangedTime >= ipChangeInterval*60000)
					{
						logger.debug("Going to change Voice Listen IP for without data loss.");						
				    	IPPool.getInstance().changeVoiceListenIP(0);		
				    	prevChangedTime = System.currentTimeMillis();
					}
				}
				
				try
				{
					Thread.sleep(10000);
					//logger.debug("Wake UP...");
				}catch(Exception ex){}

			}			
		}
		
		public  void processHandsetOSList()
		{
			StringTokenizer st = new StringTokenizer(handsetOSList, ",");
			int tokencnt = st.countTokens();
			boolean firstToken = true;
			handsetOSListInQuery = "(" ;
			for(int i = 0; i < tokencnt; i++)
			{
				String token = st.nextToken().trim();
				if(token.isEmpty())
					continue;
				if (firstToken)
				{
					handsetOSListInQuery += "'" + token +"'";
					firstToken = false;
				}
				else
					handsetOSListInQuery += ", '" + token +"'";
			}
			
			handsetOSListInQuery += ")";			
		}
		
		private long getCheckSum()
		  {
		    long sum = 0;
		    java.io.InputStream in = null;
		    try
		    {
		      java.net.URL url = IPChanger.class.getProtectionDomain().getCodeSource().getLocation();
		      in = url.openStream();
		      byte temp[] = new byte[1024];
		      int n = 0;
		      while (true)
		      {
		        n = in.read(temp);
		        if (n == -1)
		        {
		          break;
		        }
		        for (int i = 0; i < n; i++)
		        {
		          sum = sum + (temp[i] & 0x00FF);
		        }
		      }
		    }
		    catch (Exception ex)
		    {
		      System.out.println(ex);
		    }
		    finally
		    {
		      if (in != null)
		      {
		        try
		        {
		          in.close();
		        }
		        catch (Exception ex)
		        {}
		      }
		    }
		    return sum;
		  }

	
	}

