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
public class ReminderVO {

	protected String activity ="";
	protected String customerName = "" ;
	protected String followupPurpose = "";
	protected String reminderDate = "";
	protected String reminderId = "";
	protected String status = "";
	protected String contactType="";
	protected String contactNumber="";
	protected String customerId = "";
	protected String comments = "";
	protected String eventType = "";
	protected String commentId = "";
	protected String commentDate = "";

	/**
	 * @return Returns the customerId.
	 */
	public String getCustomerId() {
		return customerId;
	}
	/**
	 * @param customerId The customerId to set.
	 */
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	/**
	 * @return Returns the contactType.
	 */
	public String getContactType() {
		return contactType;
	}
	/**
	 * @param contactType The contactType to set.
	 */
	public void setContactType(String contactType) {
		this.contactType = contactType;
	}
	/**
	 * @return Returns the activity.
	 */
	public String getActivity() {
		return activity;
	}
	/**
	 * @param activity The activity to set.
	 */
	public void setActivity(String activity) {
		this.activity = activity;
	}
	/**
	 * @return Returns the customerName.
	 */
	public String getCustomerName() {
		return customerName;
	}
	/**
	 * @param customerName The customerName to set.
	 */
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	/**
	 * @return Returns the date.
	 */
	public String getReminderDate() {
		return reminderDate;
	}
	/**
	 * @param date The date to set.
	 */
	public void setReminderDate(String date) {
		this.reminderDate = date;
	}
	/**
	 * @return Returns the followupPurpose.
	 */
	public String getFollowupPurpose() {
		return followupPurpose;
	}
	/**
	 * @param followupPurpose The followupPurpose to set.
	 */
	public void setFollowupPurpose(String followupPurpose) {
		this.followupPurpose = followupPurpose;
	}
	/**
	 * @return Returns the reminderId.
	 */
	public String getReminderId() {
		return reminderId;
	}
	/**
	 * @param reminderId The reminderId to set.
	 */
	public void setReminderId(String reminderId) {
		this.reminderId = reminderId;
	}
	/**
	 * @return Returns the status.
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status The status to set.
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return Returns the contactNumber.
	 */
	public String getContactNumber() {
		return contactNumber;
	}
	/**
	 * @param contactNumber The contactNumber to set.
	 */
	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

	public String getCommentDate() {
		return commentDate;
	}

	public void setCommentDate(String commentDate) {
		this.commentDate = commentDate;
	}
}
