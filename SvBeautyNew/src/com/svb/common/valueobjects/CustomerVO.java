/*
 * Created on Mar 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.valueobjects;

/**
 * @author David  murphy
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CustomerVO {
	private String custId;
	private String firstname;
	private String middlename;
	private String lastname;
	private String homephone;
	private String cellphone;
	private String status;
	private int score;
	public String scorestring;
	private String email;
	private String message;
	private String invitedDate;
	private String inviteType;
	private String purchaseDate;
    private String profileStatus ;
    private String prodrecstatus ;

	public String repid;
	private String emailSentDate;
	private String grandOpening = "";
	private String emailStatus = "";
	private String ethnicity = "" ;
	private String gender = "";
	private String mailType = "";
	private String newregimen = "" ;

	private String profileDate = "";
	private String colorprofile = "" ;

	public String getNewRegimen() {
		return newregimen;
	}
	public void setNewRegimen(String newregimen) {
		this.newregimen = newregimen;
	}
	/**
	 * @return Returns the emailStatus.
	 */
	public String getEmailStatus() {
		return emailStatus;
	}
	/**
	 * @param emailStatus The emailStatus to set.
	 */
	public void setEmailStatus(String emailStatus) {
		this.emailStatus = emailStatus;
	}
	public String getProfileStatus() {
		return profileStatus;
	}
	public void setProfileStatus(String profileStatus) {
		this.profileStatus = profileStatus;
	}

	public String getProdrecstatus() {
		return prodrecstatus;
	}
	public void setProdrecstatus(String prodrecstatus) {
		this.prodrecstatus = prodrecstatus;
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
	public String getMessage(){

		return message;
	}

	public void  setMessage(String message){

		this.message=message;

	}



	public void setRepId(String repid){
		this.repid=repid;
	}

	public String getRepId(){
		return repid;
	}



	/**
	 * @return Returns the email.
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email The email to set.
	 */
	public void setEmail(String email) {
		this.email = email;
	}



	public void setInvitedDate(String invitedDate){
		this.invitedDate = invitedDate;
	}

	public String getInvitedDate(){
		return invitedDate;
	}

	public String getInviteType() {
		return inviteType;
	}
	public void setInviteType(String inviteType) {
		this.inviteType = inviteType;
	}

	public void setCustId(String cusid) {
		//logger.log(Level.DEBUG,"cust id " + cusid);
		this.custId = cusid;
	}

	public void setFirstName(String firstname) {
		this.firstname = firstname;
	}

	public void setLastName(String lastname) {
		this.lastname = lastname;
	}

	public void setMiddleName(String middlename) {
		this.middlename = middlename;
	}

	public void setHomePhone(String homephone) {
		this.homephone = homephone;
	}

	public void setCellPhone(String cellphone) {
		this.cellphone = cellphone;
	}

	public void setStatus(String status) {
		if (status.equals("1"))
			status = "New";
		else if (status.equals("2"))
			status = "Most Active";
		else if (status.equals("3"))
			status = "Very Active";
		else if (status.equals("4"))
			status = "Active";
		else if (status.equals("5"))
			status = "Occasional";
		else if (status.equals("6"))
			status = "Not Defined";
		else if (status.equals("7"))
			status = "Not Active";
		else
			status = " ";
		this.status = status;
	}

	public void setScore(int score1) {
		this.score = score1;
		this.scorestring = "" + score1;
	}

	public int getScore() {
		return score;
	}

	public String getDisplayName() {
		return firstname + "  " + lastname;
	}

	public String getCustId() {
		return custId;
	}

	public String getFirstName() {
		return firstname;
	}

	public String getLastName() {
		return lastname;
	}

	public String getMiddleName() {
		return middlename;
	}

	public String getHomePhone() {
		return homephone;
	}

	public String getCellPhone() {
		return cellphone;
	}

	public String getStatus() {
		return status;
	}

	public void test() {
		//logger.log(Level.DEBUG,"Printing ok.......");
	}
	/**
	 * @param string
	 */
	public void setEmailSentDate(String mailDate) {
		// TODO Auto-generated method stub
		this.emailSentDate = mailDate;
	}
	public String getEmailSentDate() {
		// TODO Auto-generated method stub
		return this.emailSentDate;
	}
	/**
	 * @param string
	 */
	public void setMailType(String mailType) {
		// TODO Auto-generated method stub
		this.mailType = mailType;
	}
	public String getMailType() {
		// TODO Auto-generated method stub
		return mailType;
	}
	/**
	 * @param string
	 */
	public void setGrandOpening(String gop) {
		// TODO Auto-generated method stub
		this.grandOpening = gop;
	}

	public String getGrandOpening(){
		return grandOpening;
	}
	/**
	 * @param string
	 */
	public void setEthnicity(String ethnicity) {
		// TODO Auto-generated method stub
	   this.ethnicity  = ethnicity ;
	}
	public String getEthnicity(){
		return ethnicity;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getGender() {
		return gender;
	}

	public void setProfileDate(String profileDate) {
		this.profileDate = profileDate;
	}

	public String getProfileDate() {
		return profileDate;
	}
	/**
	 * @param string
	 */
	public void setColorProfile(String cprof) {
		this.colorprofile = cprof ;
	}
	public String getColorProfile() {
		return this.colorprofile ;
	}


}
