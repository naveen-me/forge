package com.svb.common.mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.MimeMultipart;

/**
 * @author Madhav Sadhu
 */
public class EmailMessage implements Serializable {
    protected String to;
    protected String from;
    protected String subject;
    protected String body;
    protected ArrayList names   = new ArrayList();
    protected ArrayList   groupTo = new ArrayList();
	private MimeMultipart multiPart;
	private String replyTo;
	private String messageType;
	private String mailId;
	private String fileName;
	private String attachPdf;

	/**
	 * @return Returns the messageType.
	 */
	public String getMessageType() {
		return messageType;
	}
	/**
	 * @param messageType The messageType to set.
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	/**
	 * @return Returns the replyTo.
	 */
	public String getReplyTo() {
		return replyTo;
	}
	/**
	 * @param replyTo The replyTo to set.
	 */
	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}
    public EmailMessage(String to, String from, String subject, String body) {
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.body = body;
    }

    public EmailMessage(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public EmailMessage() {
    }

    public String getTo() {
        return to;
    }

    /**
     * Setter
     * @param to - comma separated address list.
     */
    public void setTo(String to) {
        this.to = to;
        this.groupTo.add(to);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = getNotNullString(subject);
    }

    private String getNotNullString(String value) {
        return value == null ? "" : value;

    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = getNotNullString(body);
    }
	/**
	 * @return Returns the groupTo.
	 */
	public List getGroupTo() {
		return groupTo;
	}
	/**
	 * @param groupTo The groupTo to set.
	 */
	public void setGroupTo(ArrayList groupTo) {
		this.groupTo = groupTo;
	}

	/**
	 * @param multipart
	 */
	public void setMultiPart(MimeMultipart multipart) {
		// TODO Auto-generated method stub
		this.multiPart = multipart;
	}
	/**
	 * @return Returns the multiPart.
	 */
	public MimeMultipart getMultiPart() {
		return multiPart;
	}
	/**
	 * @return Returns the names.
	 */
	public ArrayList getNames() {
		return names;
	}
	/**
	 * @param names The names to set.
	 */
	public void setNames(ArrayList names) {
		this.names = names;
	}
	/**
	 * @param mailId
	 */
	public void setMailId(String mailId) {
		// TODO Auto-generated method stub
		this.mailId = mailId;
	}
	/**
	 * @return Returns the mailId.
	 */
	public String getMailId() {
		return mailId;
	}
	/**
	 * @return Returns the attachPdf.
	 */
	public String getAttachPdf() {
		return attachPdf;
	}
	/**
	 * @param attachPdf The attachPdf to set.
	 */
	public void setAttachPdf(String attachPdf) {
		this.attachPdf = attachPdf;
	}
	/**
	 * @return Returns the fileName.
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName The fileName to set.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
