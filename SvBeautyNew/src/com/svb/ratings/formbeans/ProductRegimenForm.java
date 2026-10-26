/*
 * Created on Mar 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.ratings.formbeans;

import org.apache.struts.action.ActionForm;


/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ProductRegimenForm extends ActionForm {

	String repId = "";
	String customerId   = "";
	String prodType     = "";
	String prodLine    = "";
    String prodId = "" ;
    String dname = "" ;
    String regimenusage = "" ;
    String productusageflag = "" ;
    String profilestatus = "" ;
    String prodrecflag = "" ;
    String selfprofileflag = "" ;
    String scrname = "" ;
    String mailmsg = "" ;
	String fname = "" ;
	String lname = "" ;
	String email = "" ;
	String cusid = "" ;
	String repid = "" ;
	String printPage = "";
	String ethnicity = "" ;

	public String getSelfprofileflag() {
		return selfprofileflag;
	}
	public void setSelfprofileflag(String selfprofileflag) {
		this.selfprofileflag = selfprofileflag;
	}
	public String getProdrecflag() {
		return prodrecflag;
	}
	public void setProdrecflag(String prodrecflag) {
		this.prodrecflag = prodrecflag;
	}
	public String getProfileStatus() {
		return profilestatus;
	}
	public void setProfileStatus(String profilestatus) {
		this.profilestatus = profilestatus;
	}
	public String getRegimenusage() {
		return regimenusage;
	}
	public void setRegimenusage(String regimenusage) {
		this.regimenusage = regimenusage;
	}
	public String getDname() {
		return dname;
	}
	public void setDname(String dname) {
		this.dname = dname;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getRepId() {
		return repId;
	}
	public void setRepId(String repId) {
		this.repId = repId;
	}
	public String getProdId() {
		return prodId;
	}
	public void setProdId(String prodId) {
		this.prodId = prodId;
	}

	public String getProdType() {
		return prodType;
	}
	public void setProdType(String prodType) {
		this.prodType = prodType;
	}

	public String getProdLine() {
		return prodLine;
	}
	public void setProdLine(String prodLine) {
		this.prodLine = prodLine;
	}

	public String getMailMsg() {
		return mailmsg;
	}
	public void setMailMsg(String mailmsg) {
		this.mailmsg = mailmsg;
	}
	/**
	 * @return
	 */
	public String getProductusageflag() {

		return productusageflag;
	}
	public void setProductusageflag(String productusageflag) {

		this.productusageflag = productusageflag;
	}
	public String getCusid() {
		//logger.log(Level.DEBUG,cusid);
		return cusid;
	}
	public void setCusid(String cusid) {
		this.cusid = cusid;
		//logger.log(Level.DEBUG,this.cusid);
	}
	public String getLname() {
		return lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	public String getRepid() {
		//logger.log(Level.DEBUG,repid);
		return repid;
	}
	public void setRepid(String repid) {
		this.repid = repid;
		//logger.log(Level.DEBUG,this.repid);
	}
	public String getFname() {
		return fname;
		}

		public void setFname(String fname) {
			this.fname = fname;
		}

   public String getPrintPage() {
   		return printPage;
   }

   public void setPrintPage(String printPage) {
   	this.printPage = printPage;
   }
	public void setCethnicity(String ethnicity) {
		// TODO Auto-generated method stub
	   this.ethnicity  = ethnicity ;
	}
	public String getCethnicity(){
		return ethnicity;
	}

}
