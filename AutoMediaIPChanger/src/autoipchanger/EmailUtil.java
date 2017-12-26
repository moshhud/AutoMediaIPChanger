package autoipchanger;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.apache.log4j.Logger;

import java.util.*;

public class EmailUtil 
{

	static Logger logger = Logger.getLogger(autoipchanger.EmailUtil.class);
	public static void sendEmail(String toEmail, String subject, String body){
	        if(toEmail.isEmpty() || IPChanger.fromEmail.isEmpty() || IPChanger.fromEmailPassword.isEmpty() || IPChanger.smtpServer.isEmpty() || IPChanger.smtpPort.isEmpty())
	        {
	        	logger.error("Mail can not be sent. Insufficient mail info in properties file. Configure IPChangerProperties.cfg file appropriately");
	        	return;
	        }
	        
        	Properties props =  new Properties();
        	props.put("mail.smtp.host" , IPChanger.smtpServer);
        	props.put("mail.smtp.port", IPChanger.smtpPort); //TLS Port
        	props.put("mail.smtp.auth", "true"); //enable authentication
        	
        	if(IPChanger.smtpPort.equals("587"))
        		props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
        	else
        		props.put("mail.smtp.starttls.enable", "false");
        	
            
        	Authenticator auth = new Authenticator() {
        	       //override the getPasswordAuthentication method
        	       protected PasswordAuthentication getPasswordAuthentication() {
        	           return new PasswordAuthentication(IPChanger.fromEmail, IPChanger.fromEmailPassword);
        	       }
        	   };
        	Session session = Session.getInstance(props, auth);   
        	
	        try{
	        	MimeMessage msg = new MimeMessage(session);	        	
	        	msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
	        	msg.addHeader("format", "flowed");
	          	msg.addHeader("Content-Transfer-Encoding", "8bit");
	          	
	          	String mailSubject;
	          	if(IPChanger.serverID != null && !IPChanger.serverID.isEmpty())
	          		mailSubject = IPChanger.serverID+":: "+subject;
	          	else
	          		mailSubject = subject;
	          	msg.setFrom(new InternetAddress(IPChanger.fromEmail));	 
	          	msg.setSubject(mailSubject, "UTF-8");
	          	msg.setText(body,"US-ASCII","html");	 
	          	msg.setSentDate(new Date());
	          	msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
	          	Transport.send(msg);  
	 
	          	logger.debug("EMail Sent Successfully!!!");
	        }
	        catch (Exception e) {
	        	logger.error(e);
	        }
	  }
}
