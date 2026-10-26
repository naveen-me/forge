/*
 * Created on Mar 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.customerregistration.javabeans;

/**
 * @author David  murphy
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */


import javax.mail.*;
import javax.mail.internet.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.svb.common.ConstantDeclarations;

import java.util.*;



public class SendEmail {

	public static void postMail(String recipient, String subject, String message,String from) throws MessagingException
	{
	    boolean debug = false;
	    Session session;
//	    boolean valid = false;
	     //Set the host smtp address
	    // Properties props = new Properties();
	   //  props.put("mail.smtp.host",ConstantDeclarations.MAIL_HOST);
	    Context initCtx;
		try {
			initCtx = new InitialContext();
			session = (Session) initCtx.lookup("java:/Mail");

		// create a message
	    Message msg = new MimeMessage(session);

	    // set the from and to address
	    InternetAddress addressFrom = new InternetAddress(from);
	    msg.setFrom(addressFrom);
	    InternetAddress addressTo = new InternetAddress(recipient);
	    msg.setRecipient(Message.RecipientType.TO, addressTo);

	    // Optional : You can also set your custom headers in the Email if you Want
	    msg.addHeader("MyHeaderName", "myHeaderValue");

	    // Setting the Subject and Content Type
	    msg.setSubject(subject);
	    msg.setContent(message, "text/plain");
	 try{
	 	Transport.send(msg);

	 }catch(Exception e)
	 	{
	 	e.printStackTrace() ;
	 	//logger.log(Level.DEBUG,"Invalid Host name in the E-mail address. Failed to get in touch with the representative.");
	 	}

//	    String success = "Success";
//	    return valid;
	}catch(Exception e){
		e.printStackTrace();
	}

	}

	public static void postMail(String custName, String recipient, String subject, String message,String from) throws MessagingException
	{
	    boolean debug = false;
	    Session session;
//	    boolean valid = false;
	     //Set the host smtp address
	    // Properties props = new Properties();
	   //  props.put("mail.smtp.host",ConstantDeclarations.MAIL_HOST);
	    Context initCtx;
		try {
			initCtx = new InitialContext();
			session = (Session) initCtx.lookup("java:/Mail");

		// create a message
	    Message msg = new MimeMessage(session);

	    // set the from and to address
	    //from = ConstantDeclarations.MAIL_MANAGER;//AvonBeauty@avonpersonalbeauty.com
	    //InternetAddress addressFrom = new InternetAddress(from,custName);
	    from = ConstantDeclarations.CUSTOMER_SERVICE;
	    InternetAddress addressFrom = new InternetAddress(from);
	    msg.setFrom(addressFrom);
	    InternetAddress addressTo = new InternetAddress(recipient);
	    msg.setRecipient(Message.RecipientType.TO, addressTo);

	    // Optional : You can also set your custom headers in the Email if you Want
	    msg.addHeader("MyHeaderName", "myHeaderValue");

	    // Setting the Subject and Content Type
	    msg.setSubject(subject);
	    msg.setContent(message, "text/plain");
	 try{
	 	Transport.send(msg);

	 }catch(Exception e)
	 	{
	 	e.printStackTrace() ;
	 	//logger.log(Level.DEBUG,"Invalid Host name in the E-mail address. Failed to get in touch with the representative.");
	 	}

//	    String success = "Success";
//	    return valid;
	}catch(Exception e){
		e.printStackTrace();
	}

	}

}
