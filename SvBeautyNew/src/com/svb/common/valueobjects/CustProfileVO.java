/*
 * Created on Mar 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.valueobjects;

/**
 * @author Neelima
 *
**/

public class CustProfileVO {

	private String custid = "";
	private String firstname = "";
	private String middlename = "";
	private String lastname = "";
	private String email = "";
	private String homephone = "";
	private String workphone = "";
	private String faxphone = "";
	private String mobilephone = "";
	private String address1 = "";
	private String address2 = "";
	private String city = "";
	private String state = "";
	private String zip = "";
	private String gender = "";
	private String bday = "";
	private String anvday = "";
	private String status = "";
	private String comments = "";
	private String ethnicity = "";
	private int score;
	public String scorestring = "";


	public String getCustId() {
		return custid;
	}

	public void setCustId(String cusid) {
		//logger.log(Level.DEBUG,"cust id " + cusid);
		this.custid = cusid;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getMiddlename() {
		return middlename;
	}

	public void setMiddlename(String middlename) {
		this.middlename = middlename;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getHomephone() {
		return homephone;
	}

	public void setHomephone(String homephone) {
		this.homephone = homephone;
	}

	public String getWorkphone() {
		return workphone;
	}

	public void setWorkphone(String workphone) {
		this.workphone = workphone;
	}

	public String getFaxphone() {
		return faxphone;
	}

	public void setFaxphone(String faxphone) {
		this.faxphone = faxphone;
	}

	public String getMobilephone() {
		return mobilephone;
	}

	public void setMobilephone(String mobilephone) {
		this.mobilephone = mobilephone;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}


	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBday() {
		return bday;
	}

	public void setBday(String bday) {
		this.bday = bday;
	}

	public String getAnvday() {
		return anvday;
	}

	public void setAnvday(String anvday) {
		this.anvday = anvday;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		/*if (status.equals("1"))
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
			status = " ";*/
		this.status = status;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getEthnicity() {
		return ethnicity;
	}

	public void setEthnicity(String ethnicity) {
		this.ethnicity = ethnicity;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score1) {
		this.score = score1;
		this.scorestring = "" + score1;
	}

	public void test() {
		//logger.log(Level.DEBUG,"Printing ok.......");
	}
}
