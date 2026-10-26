package com.svb.common.mail;



import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author
 */
public class EmailService {
   /* private static Logger logger = Logger.getLogger(EmailService.class);

    static {
        if (!logger.getAllAppenders().hasMoreElements()) {
            ConsoleAppender console = new ConsoleAppender(new SimpleLayout());
            logger.addAppender(console);
        }
    }
 */

    private static EmailService instance;

    private EmailService() {
    }

    public static EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    private String getCommaSeparated(Collection emails) {
        StringBuffer buffer = new StringBuffer();
        Iterator it = emails.iterator();
        while (it.hasNext()) {
            String email = (String) it.next();
            buffer.append(email);
            if (it.hasNext()) {
                buffer.append(",");
            }
        }
        return buffer.toString();
    }

    public void sendMessage(Collection userEmails,
                            EmailMessage message)
            throws Exception {
        if (userEmails == null) {
            return;
        }

        if (!userEmails.isEmpty()) {
            message.setTo(getCommaSeparated(userEmails));
            sendMessage(message);
        }
    }

    public void sendMessage(EmailMessage message) throws Exception {

        if (message.getTo() == null) {
            throw new Exception("Message must contain TO addreses");
        }

/*
        if (message.getFrom() == null) {
            message.setFrom("dummy@mail.ru");
        }
*/

        MessageSenderQueue.getInstance().addMessageToSend(message);

    }

    public static Session getSession() throws MessagingException {
        try {
            return (Session) PortableRemoteObject.narrow(new InitialContext().lookup("java:Mail"), Session.class);
        } catch (NamingException e) {
            throw new MessagingException("Couldn't find java:Mail ", e);
        }
    }


}
