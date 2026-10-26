/*
 * Created on May 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.mail;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.Context;
import javax.naming.InitialContext;

import com.svb.common.ConstantDeclarations;

/**
 * @author mahdav
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SendMessage {

	 public void sendMessage(List messages) {

		try {
			System.out.println("Directly into sendMessage");
			Store store  = null;
			boolean flag = true;
			ManageMail mailDal = new ManageMail();
			//logger.log(Level.DEBUG,"In Message Sender Queue");
			//Properties props = new Properties();
			//props.put("mail.smtp.host", ConstantDeclarations.MAIL_HOST);
			//Session session1 = Session.getDefaultInstance(props, null);
			Context initCtx = new InitialContext();
			Session session1 = (Session) initCtx.lookup("java:/Mail");

			for (Iterator iterator = messages.iterator(); iterator.hasNext();) {
	                flag = true;
				    EmailMessage message = (EmailMessage) iterator.next();

	                String  mailfrom       = message.getFrom();
	             	String  body 		   = message.getBody();
	    			List    mailto  	   = message.getGroupTo();
	    			String  subject        = message.getSubject();
	    			List 	names          = message.getNames();
	    			String  replyTo        = message.getReplyTo();
	    		//	String  mailId         = message.getMailId();

				MimeMessage msg = new MimeMessage(session1);
		/*		if(mailfrom!=null && mailfrom.equals(ConstantDeclarations.CUSTOMER_SERVICE)){
					msg.setFrom(new InternetAddress(ConstantDeclarations.CUSTOMER_SERVICE));
				}
				else {
				//	msg.setFrom(new InternetAddress(ConstantDeclarations.MAIL_MANAGER,(String)names.get(0)));
					if(!mailfrom.equals(""))
						msg.setFrom(new InternetAddress(mailfrom));
					else {
						msg.setFrom(new InternetAddress(ConstantDeclarations.MAIL_MANAGER));
						if(replyTo.equals(""))
							replyTo = ConstantDeclarations.MAIL_MANAGER;
					}
				}
          */
				InternetAddress[] idr = new InternetAddress[1];
				idr[0] = new InternetAddress(replyTo);
				msg.setReplyTo(idr);
				
				if(!mailfrom.equals(""))
				   msg.setFrom(new InternetAddress(mailfrom));
				else
					   msg.setFrom(new InternetAddress("customerservice@sv-beauty.com"));
				
				InternetAddress[] addressTo = new InternetAddress[mailto.size()];

				for(int i = 0 ;i < mailto.size();i++){
					addressTo[i] = new InternetAddress((String)mailto.get(i));
				}

				msg.setRecipients(Message.RecipientType.TO, addressTo);
		   		msg.setSubject(subject);

		   		String attachpdf = message.getAttachPdf() ;
		   		if (attachpdf.equals("yes"))
		   		{
		   		  String filename = message.getFileName() ;
		   		  System.out.println("filename last "+filename) ;
		   		 // msg.setText(body) ;
		   		  MimeBodyPart messageBodyPart = new MimeBodyPart();
		   		  
		   		  messageBodyPart.setText(body);

		   		  Multipart mimeMultipart = new MimeMultipart("related");
		          mimeMultipart.addBodyPart(messageBodyPart);
		          
		          messageBodyPart = new MimeBodyPart();
		          FileDataSource file = new FileDataSource(filename);
		          DataHandler pdfDH = new DataHandler(file);
		          messageBodyPart.setDataHandler(pdfDH);
		          messageBodyPart.setFileName(file.getName()) ;
		          mimeMultipart.addBodyPart(messageBodyPart);
		          
		          messageBodyPart.setHeader("Content-Type", "application/pdf");
			      System.out.println("mime body part:"+mimeMultipart.getContentType());

		   		  msg.setContent(mimeMultipart);
		   		}
	            else
	            {
		   		  if(message.getMultiPart()!=null){
		   			msg.setContent(message.getMultiPart());
		   		  }else
		   			msg.setContent(body,message.getMessageType());
	            }
		   		
		   		try{
		   			System.out.println("^^^ sending" +
		   					" message for mailid ^^^");
		   			Transport.send(msg);

		   		}catch(MessagingException mex){
		   			System.out.println("^^^ could not send message for mailid ^^^");
		   			mex.printStackTrace();
					flag = false;
		   		}
		   		if(flag){
		   			System.out.println("^^^ sent message, so updating status for mailid ^^^");
		   		   //mailDal.updateMail(mailId);
		   		}

				}


		} catch (Exception e) {
				e.printStackTrace();

		}

	}
    public void VBEmail()
    {
        BodyPart messageBodyPart = new MimeBodyPart();
        
        
/*   		MimeBodyPart messageBodyPart = new MimeBodyPart();
	   	messageBodyPart.setText(body);
//	   	Multipart multipart = new MimeMultipart();
	   	Multipart multipart = new MimeMultipart("alternative");
	   	multipart.addBodyPart(messageBodyPart);
//	   	 Part two is attachment
	   	System.out.println("----->fileAttachment DISTINTO de NULL");
	   	messageBodyPart = new MimeBodyPart();
	   	FileDataSource fds = new FileDataSource(fileAttachment); 
	   	messageBodyPart.setDataHandler( new DataHandler(fds)); 
	   	messageBodyPart.setFileName(fileAttachment); //<-- El archivo atachado.

	   	multipart.addBodyPart(messageBodyPart);
//	   	EN DESARROLLO el envio de attachment!! 
//	   	 Put parts in message
	   	message.setContent(multipart);
*/
		
//vb		messageBodyPart.setContent(htmlText, "text/html");
        
        // end body message
		MimeMultipart mimeMultipart = new MimeMultipart("related");
        //mimeMultipart.setSubType("mixed");
//vb        mimeMultipart.addBodyPart(messageBodyPart);

//        File f2=new File(doc_path);
        
        
        // Attach with PDF to E-Mail message.
        //HttpSession session = request.getSession("true") ;
		//String filePath = session.getServlet().getServletContext().getRealPath("/") + "pdfFiles";
		
		
		
		//File pdfFile = new File(filePath, regimenId+".pdf");
        FileDataSource file = new FileDataSource("/pdfFiles/sv300010293.pdf.pdf");
        DataHandler pdfDH = new DataHandler(file);
        MimeBodyPart pdfPart = new MimeBodyPart();
//        pdfPart.setDataHandler(pdfDH);
//        mimeMultipart.addBodyPart(pdfPart);
       
//       pdfPart.setHeader("Content-Type","application/pdf; ");
//       pdfPart.setDisposition("inline; filename=\"doc.pdf\"");
       //pdfPart.setText(m_text);
    //vb   pdfPart.setDisposition("inline; filename="+f2.getName()+"");
      
       System.out.println("mime body part:"+mimeMultipart.getContentType());

     // Create an e-mail message object (MimeMessage)
  /*      Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.host", "465");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

		SecurityManager security = System.getSecurityManager();
		
		
		
		Authenticator auth=new SMTPAuthenticator();
		Session session = Session.getInstance(props, auth);
		session.setDebug(true);
		
		
	*/	
		
//        MimeMessage email = new MimeMessage(session);
      //  email.setDataHandler(new DataHandler(
        //		new ByteArrayDataSource(m_text, "text/html")));
        
  //      email.setContent(mimeMultipart);

    }

}
