/*
 * Created on Apr 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.mail;

import java.util.ArrayList;

/**
 * @author mahdav
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SendMail {

	/*protected void sendMail(){
		EmailMessage msg = new EmailMessage();
		ArrayList list = new ArrayList();
		list.add("madhav@stylevalet.com");
		//list.add("rajg22@yahoo.com");
		msg.setGroupTo(list);
		//msg.setTo("madhav@stylevalet.com,ragj22@yahoo.com");
		msg.setBody("Hi There");
		msg.setFrom("madhav@saama.com");
		msg.setSubject("Mail test");
		MessageSenderQueue queue = MessageSenderQueue.getInstance();
		queue.addMessageToSend(msg);
	}
	*/
	protected void sendMail(EmailMessage message){
		MessageSenderQueue queue = MessageSenderQueue.getInstance();
		queue.addMessageToSend(message);
	}

}
