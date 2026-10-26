package com.svb.common.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import com.svb.common.ConstantDeclarations;

/**
 * @author Denis Belorunov
 */
public class MessageSenderQueue implements Runnable {
    private static final int EMAIL_SEND_GAP = 60000;

    private List queue = new ArrayList();
    private static MessageSenderQueue instance;
    private Thread runThread;

    private MessageSenderQueue() {
        runThread = new Thread(this);
        runThread.start();
    }

    public static MessageSenderQueue getInstance() {
        //logger.log(Level.DEBUG,"In MessageSenderQueue");
    	if (instance == null) {
    		//logger.log(Level.DEBUG,"$$$ In MessageSenderQueue new instance");
            System.out.println("$$$ In MessageSenderQueue new instance");
    		instance = new MessageSenderQueue();
        }else{
        	//logger.log(Level.DEBUG,"#### In MessageSenderQueue old instance");
        }

        return instance;
    }

    public void addMessageToSend(EmailMessage message) {
        synchronized (queue) {
            queue.add(message);
            queue.notify();
        }
    }

    public void run() {
        List messages = new ArrayList();
        do {
            waitForMessage();
            synchronized (queue) {
                if (queue.isEmpty()) {
                    continue;
                }
                messages.addAll(queue);
                queue.clear();
            }
            sendMessage(messages);
            messages.clear();
        } while (true);
    }

    private void waitForMessage() {
        try {
            synchronized (queue) {
                while (queue.isEmpty()) {
                    queue.wait();
                }
            }
        } catch (InterruptedException e) {
        	e.printStackTrace();
        }
    }

    public void sendMessage(List messages) {

		try {
			Store store = null;
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
	    		String  mailId         = message.getMailId();

				MimeMessage msg = new MimeMessage(session1);
				if(!mailfrom.equals(""))
					msg.setFrom(new InternetAddress(mailfrom));
				else {
					msg.setFrom(new InternetAddress(ConstantDeclarations.MAIL_MANAGER));
					if(replyTo.equals(""))
						replyTo = ConstantDeclarations.MAIL_MANAGER;
				}

				//msg.setFrom(new InternetAddress(ConstantDeclarations.MAIL_MANAGER,(String)names.get(0)));

				InternetAddress[] idr = new InternetAddress[1];
				idr[0] = new InternetAddress(replyTo);
				msg.setReplyTo(idr);
				InternetAddress[] addressTo = new InternetAddress[mailto.size()];

				for(int i = 0 ;i < mailto.size();i++){
					addressTo[i] = new InternetAddress((String)mailto.get(i));
				}

				msg.setRecipients(Message.RecipientType.TO, addressTo);
		   		msg.setSubject(subject);
		   		if(message.getMultiPart()!=null)
		   			msg.setContent(message.getMultiPart());
		   		else
		   			msg.setContent(body,message.getMessageType());
		   		try {
		   			Transport.send(msg);
				} catch (MessagingException mex) {
					System.out.println("^^^ could not send message for mailid ^^^"+mailId);
					mex.printStackTrace();
					flag = false;
				}
				if(flag) {
					System.out.println("^^^ sent message, so updating status for mailid ^^^"+mailId);
					mailDal.updateMail(mailId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
