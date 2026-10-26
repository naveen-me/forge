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
public class EmailCorrespondenceVO {

	protected String  customerFirstName;
	protected String  customerLastName;
	protected EmailVO customerEmail;
	protected String  productid;
	protected String  emailId;

	/**
	 * @return Returns the emailId.
	 */
	public String getEmailId() {
		return emailId;
	}
	/**
	 * @param emailId The emailId to set.
	 */
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	/**
	 * @return Returns the customerEmail.
	 */
	public EmailVO getCustomerEmail() {
		return customerEmail;
	}
	/**
	 * @param customerEmail The customerEmail to set.
	 */
	public void setCustomerEmail(EmailVO customerEmail) {
		this.customerEmail = customerEmail;
	}
	/**
	 * @return Returns the customerFirstName.
	 */
	public String getCustomerFirstName() {
		return customerFirstName;
	}
	/**
	 * @param customerFirstName The customerFirstName to set.
	 */
	public void setCustomerFirstName(String customerFirstName) {
		this.customerFirstName = customerFirstName;
	}
	/**
	 * @return Returns the customerLastName.
	 */
	public String getCustomerLastName() {
		return customerLastName;
	}
	/**
	 * @param customerLastName The customerLastName to set.
	 */
	public void setCustomerLastName(String customerLastName) {
		this.customerLastName = customerLastName;
	}
	/**
	 * @return Returns the productid.
	 */
	public String getProductid() {
		return productid;
	}
	/**
	 * @param productid The productid to set.
	 */
	public void setProductid(String productid) {
		this.productid = productid;
	}
}
