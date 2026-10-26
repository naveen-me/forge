/*
 * Created on Mar 7, 2002
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.valueobjects;

/**
 * @author madhav
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EmailVO {

	 protected String id;
	 protected String subject;
	 protected String to;
	 protected String from;
	 protected String messsage;
	 protected String theme;


	/**
	 * @return Returns the from.
	 */
	public String getFrom() {
		return from;
	}
	/**
	 * @param from The from to set.
	 */
	public void setFrom(String from) {
		this.from = from;
	}
	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return Returns the messsage.
	 */
	public String getMesssage() {
		return messsage;
	}
	/**
	 * @param messsage The messsage to set.
	 */
	public void setMesssage(String messsage) {
		this.messsage = messsage;
	}
	/**
	 * @return Returns the subject.
	 */
	public String getSubject() {
		return subject;
	}
	/**
	 * @param subject The subject to set.
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	/**
	 * @return Returns the theme.
	 */
	public String getTheme() {
		return theme;
	}
	/**
	 * @param theme The theme to set.
	 */
	public void setTheme(String theme) {
		this.theme = theme;
	}
	/**
	 * @return Returns the to.
	 */
	public String getTo() {
		return to;
	}
	/**
	 * @param to The to to set.
	 */
	public void setTo(String to) {
		this.to = to;
	}


}
