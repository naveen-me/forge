/*
 * Created on Mar 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.valueobjects;

/**
 * @author Neelima
 *
 */
public class ContactHistoryVO {

	private String contactId;
	private String contactDate;
	private String contactTypeId;
	private String contactNotes;
	private String contactPurpose;
	private String customerId;
	private String repId;


	/**
	 * @return Returns the contactPurpose.
	 */
	public String getContactPurpose() {
		return contactPurpose;
	}
	/**
	 * @param contactPurpose The contactPurpose to set.
	 */
	public void setContactPurpose(String contactPurpose) {
		this.contactPurpose = contactPurpose;
	}
	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getContactDate() {
		return contactDate;
	}

	public void setContactDate(String contactDate) {
		this.contactDate = contactDate;
	}

	public String getContactTypeId() {
		return contactTypeId;
	}

	public void setContactTypeId(String contactTypeId) {
		this.contactTypeId = contactTypeId;
	}

	public String getContactNotes() {
		return contactNotes;
	}

	public void setContactNotes(String contactNotes) {
		this.contactNotes = contactNotes;
	}

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
	 * @return Returns the repId.
	 */
	public String getRepId() {
		return repId;
	}
	/**
	 * @param repId The repId to set.
	 */
	public void setRepId(String repId) {
		this.repId = repId;
	}
}
