/*
 * Created on Mar 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.valueobjects;

/**
 * @author mahdav
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ReplenishmentVO {
	private String id; //replenishment id
	private String cusId;
	private String cusFirstName;
	private String cusLastName;
	private String cusEmail;
	private String cusEmailStatus;
	private String cusPhone;
	private String contactAttribValue;
	private String productId;
	private String productMainId;
	private String productName;
	private String productPrice;
	private String purchaseDate;
	private String replenishementDate;
	private String dateDiff;
	private String dsname;
	private String phoneChecked;
	private String emailChecked;
	private String visitChecked;
	private String productResource;

	/**
	 * @return Returns the contactAttribValue.
	 */
	public String getContactAttribValue() {
		return contactAttribValue;
	}
	/**
	 * @param contactAttribValue The contactAttribValue to set.
	 */
	public void setContactAttribValue(String contactAttribValue) {
		this.contactAttribValue = contactAttribValue;
	}
	/**
	 * @return Returns the cusFirstName.
	 */
	public String getCusFirstName() {
		return cusFirstName;
	}
	/**
	 * @param cusFirstName The cusFirstName to set.
	 */
	public void setCusFirstName(String cusFirstName) {
		this.cusFirstName = cusFirstName;
	}
	/**
	 * @return Returns the cusId.
	 */
	public String getCusId() {
		return cusId;
	}
	/**
	 * @param cusId The cusId to set.
	 */
	public void setCusId(String cusId) {
		this.cusId = cusId;
	}
	/**
	 * @return Returns the cusLastName.
	 */
	public String getCusLastName() {
		return cusLastName;
	}
	/**
	 * @param cusLastName The cusLastName to set.
	 */
	public void setCusLastName(String cusLastName) {
		this.cusLastName = cusLastName;
	}
	/**
	 * @return Returns the productId.
	 */
	public String getProductId() {
		return productId;
	}
	/**
	 * @param productId The productId to set.
	 */
	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductMainId() {
		return productMainId;
	}

	public void setProductMainId(String productMainId) {
		this.productMainId = productMainId;
	}

	/**
	 * @return Returns the productName.
	 */
	public String getProductName() {
		return productName;
	}
	/**
	 * @param productName The productName to set.
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}
	/**
	 * @return Returns the purchaseDate.
	 */
	public String getPurchaseDate() {
		return purchaseDate;
	}
	/**
	 * @param purchaseDate The purchaseDate to set.
	 */
	public void setPurchaseDate(String purchaseDate) {
		this.purchaseDate = purchaseDate;
	}
	/**
	 * @return Returns the replenishementDate.
	 */
	public String getReplenishementDate() {
		return replenishementDate;
	}
	/**
	 * @param replenishementDate The replenishementDate to set.
	 */
	public void setReplenishementDate(String replenishementDate) {
		this.replenishementDate = replenishementDate;
	}
	/**
	 * @param dsname
	 */
	public void setName(String dsname) {
		// TODO Auto-generated method stub
		this.dsname = dsname;
	}
	public String getName() {
		// TODO Auto-generated method stub
		return dsname;
	}



	/**
	 * @return Returns the dsname.
	 */
	public String getDsname() {
		return dsname;
	}
	/**
	 * @param dsname The dsname to set.
	 */
	public void setDsname(String dsname) {
		this.dsname = dsname;
	}
	/**
	 * @return Returns the emailChecked.
	 */
	public String getEmailChecked() {
		return emailChecked;
	}
	/**
	 * @param emailChecked The emailChecked to set.
	 */
	public void setEmailChecked(String emailChecked) {
		this.emailChecked = emailChecked;
	}
	/**
	 * @return Returns the phoneChecked.
	 */
	public String getPhoneChecked() {
		return phoneChecked;
	}
	/**
	 * @param phoneChecked The phoneChecked to set.
	 */
	public void setPhoneChecked(String phoneChecked) {
		this.phoneChecked = phoneChecked;
	}
	/**
	 * @return Returns the pvisitChecked.
	 */
	public String getVisitChecked() {
		return visitChecked;
	}
	/**
	 * @param pvisitChecked The pvisitChecked to set.
	 */
	public void setVisitChecked(String pvisitChecked) {
		this.visitChecked = pvisitChecked;
	}
	/**
	 * @return Returns the productPrice.
	 */
	public String getProductPrice() {
		return productPrice;
	}
	/**
	 * @param productPrice The productPrice to set.
	 */
	public void setProductPrice(String productPrice) {
		this.productPrice = productPrice;
	}
	/**
	 * @return Returns the dateDiff.
	 */
	public String getDateDiff() {
		return dateDiff;
	}
	/**
	 * @param dateDiff The dateDiff to set.
	 */
	public void setDateDiff(String dateDiff) {
		this.dateDiff = dateDiff;
	}
	/**
	 * @return Returns the cusEmail.
	 */
	public String getCusEmail() {
		return cusEmail;
	}
	/**
	 * @param cusEmail The cusEmail to set.
	 */
	public void setCusEmail(String cusEmail) {
		this.cusEmail = cusEmail;
	}
	/**
	 * @return Returns the cusPhone.
	 */
	public String getCusPhone() {
		return cusPhone;
	}
	/**
	 * @param cusPhone The cusPhone to set.
	 */
	public void setCusPhone(String cusPhone) {
		this.cusPhone = cusPhone;
	}
	/**
	 * @param string
	 */
	public void setReplenishmentId(String id) {
		// TODO Auto-generated method stub
		this.id = id ;
	}
	public String getReplenishmentId(){
		return id;
	}
	/**
	 * @return Returns the cusEmailStatus.
	 */
	public String getCusEmailStatus() {
		return cusEmailStatus;
	}
	/**
	 * @param cusEmailStatus The cusEmailStatus to set.
	 */
	public void setCusEmailStatus(String cusEmailStatus) {
		this.cusEmailStatus = cusEmailStatus;
	}

	public String getProductResource() {
		return productResource;
	}

	public void setProductResource(String productResource) {
		this.productResource = productResource;
	}
}
