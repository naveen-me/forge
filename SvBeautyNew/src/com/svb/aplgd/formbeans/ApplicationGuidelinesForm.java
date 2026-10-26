/*
 * Created on Mar 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.aplgd.formbeans;

import org.apache.struts.action.ActionForm;


/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ApplicationGuidelinesForm extends ActionForm {

	String repId = "" ;
	String dname = "" ;
	String customerId = "" ;
	String regimenusage = "";

    String exstatus = "no" ;
    String ntrstatus = "no" ;
    String nmstatus = "no" ;
    String mstatus = "no" ;
    String tnstatus = "no" ;
    String dtstatus = "no" ;
    String clstatus = "no" ;
    String cleansename = "" ;
    String tonename = "" ;
    String moistname = "" ;
    String nmoistname = "" ;
    String etreatname = "" ;
    String netreatname = "" ;
    String treatname = "" ;
    String ntreatname = "" ;
    String exfoliname = "" ;
	String prodtype = "" ;
	String treatprodtype = "" ;
	String etreatprodtype = "" ;
    String cleanseprice = "" ;
    String toneprice = "" ;
    String moistprice = "" ;
    String nmoistprice = "" ;
    String etreatprice = "" ;
    String treatprice = "" ;
    String exfoliprice = "" ;
	String prodline = "" ;
	String scrname = "" ;
    String cleanseid = "" ;
    String toneid = "" ;
    String moistid = "" ;
    String nmoistid = "" ;
    String etreatid = "" ;
    String treatid = "" ;
    String exfoliid = "" ;

    String salutation = "";
    String personalMsg = "";
    String closing = "";
    String signature = "";
    String email = "";
    String phoneNum = "";
    String productMsg = "";
    String prodids = "";
    String methodtocall = "" ;
    

    public String getMethodToCall() {
		return methodtocall;
	}
	public void setMethodToCall(String methodtocall) {
		this.methodtocall = methodtocall;
	}

	public String getScrname() {
		return scrname;
	}
	public void setScrname(String scrname) {
		this.scrname = scrname;
	}
	public String getProdLine() {
		return prodline;
	}
	public void setProdLine(String prodLine) {
		this.prodline = prodLine;
	}
	public String getDname() {
		return dname;
	}
	public void setDname(String dname) {
		this.dname = dname;
	}
	public String getExStatus() {
		return exstatus;
	}
	public void setExStatus(String exstatus) {
		this.exstatus = exstatus;
	}
	public String getNtrStatus() {
		return ntrstatus;
	}
	public void setNtrStatus(String ntrstatus) {
		this.ntrstatus = ntrstatus;
	}

	public String getNmStatus() {
		return nmstatus;
	}
	public void setNmStatus(String nmstatus) {
		this.nmstatus = nmstatus;
	}
	public String getMStatus() {
		return mstatus;
	}
	public void setMStatus(String mstatus) {
		this.mstatus = mstatus;
	}
	public String getTnStatus() {
		return tnstatus;
	}
	public void setTnStatus(String tnstatus) {
		this.tnstatus = tnstatus;
	}
	public String getDtStatus() {
		return dtstatus;
	}
	public void setDtStatus(String dtstatus) {
		this.dtstatus = dtstatus;
	}
	public String getClStatus() {
		return clstatus;
	}
	public void setClStatus(String clstatus) {
		this.clstatus = clstatus;
	}
	public String getCleanseName() {
		return cleansename;
	}
	public void setCleanseName(String cleansename) {
		this.cleansename = cleansename;
	}
	public String getToneName() {
		return tonename;
	}
	public void setToneName(String tonename) {
		this.tonename = tonename;
	}
	public String getMoistName() {
		return moistname;
	}
	public void setMoistName(String moistname) {
		this.moistname = moistname;
	}
	public String getNmoistName() {
		return nmoistname;
	}
	public void setNmoistName(String nmoistname) {
		this.nmoistname = nmoistname;
	}
	public String getTreatName() {
		return treatname;
	}
	public void setTreatName(String treatname) {
		this.treatname = treatname;
	}
	public String getNtreatName() {
		return ntreatname;
	}
	public void setNtreatName(String ntreatname) {
		this.ntreatname = ntreatname;
	}
	public String getEtreatName() {
		return etreatname;
	}
	public void setEtreatName(String etreatname) {
		this.etreatname = etreatname;
	}
	public String getNetreatName() {
		return netreatname;
	}
	public void setNetreatName(String netreatname) {
		this.netreatname = netreatname;
	}
	public String getExfoliName() {
		return exfoliname;
	}
	public void setExfoliName(String exfoliname) {
		this.exfoliname = exfoliname;
	}
    public String getRepId() {
		return repId;
	}
	public void setRepId(String repId) {
		this.repId = repId;
	}
    public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getRegimenusage() {
		return regimenusage;
	}
	public void setRegimenusage(String regimenusage) {
		this.regimenusage = regimenusage;
	}
    public String getProdType() {
		return prodtype;
	}
	public void setProdType(String prodtype) {
		this.prodtype = prodtype;
	}
    public String getTreatProdType() {
		return treatprodtype;
	}
	public void setTreatProdType(String treatprodtype) {
		this.treatprodtype = treatprodtype;
	}
    public String getEtreatProdType() {
		return etreatprodtype;
	}
	public void setEtreatProdType(String etreatprodtype) {
		this.etreatprodtype = etreatprodtype;
	}

	public String getCleanseId() {
		return cleanseid;
	}
	public void setCleanseId(String cleanseid) {
		this.cleanseid = cleanseid;
	}
	public String getToneId() {
		return toneid;
	}
	public void setToneId(String toneid) {
		this.toneid = toneid;
	}
	public String getMoistId() {
		return moistid;
	}
	public void setMoistId(String moistid) {
		this.moistid = moistid;
	}
	public String getNmoistId() {
		return nmoistid;
	}
	public void setNmoistId(String nmoistid) {
		this.nmoistid = nmoistid;
	}
	public String getTreatId() {
		return treatid;
	}
	public void setTreatId(String treatid) {
		this.treatid = treatid;
	}
	public String getEtreatId() {
		return etreatid;
	}
	public void setEtreatId(String etreatid) {
		this.etreatid = etreatid;
	}
	public String getExfoliId() {
		return exfoliid;
	}
	public void setExfoliId(String exfoliid) {
		this.exfoliid = exfoliid;
	}

	public String getCleansePrice() {
		return cleanseprice;
	}
	public void setCleansePrice(String cleanseprice) {
		this.cleanseprice = cleanseprice;
	}
	public String getTonePrice() {
		return toneprice;
	}
	public void setTonePrice(String toneprice) {
		this.toneprice = toneprice;
	}
	public String getMoistPrice() {
		return moistprice;
	}
	public void setMoistPrice(String moistprice) {
		this.moistprice = moistprice;
	}
	public String getNmoistPrice() {
		return nmoistprice;
	}
	public void setNmoistPrice(String nmoistprice) {
		this.nmoistprice = nmoistprice;
	}
	public String getTreatPrice() {
		return treatprice;
	}
	public void setTreatPrice(String treatprice) {
		this.treatprice = treatprice;
	}
	public String getEtreatPrice() {
		return etreatprice;
	}
	public void setEtreatPrice(String etreatprice) {
		this.etreatprice = etreatprice;
	}
	public String getExfoliPrice() {
		return exfoliprice;
	}
	public void setExfoliPrice(String exfoliprice) {
		this.exfoliprice = exfoliprice;
	}

    public String getSalutation() {
		return salutation;
	}
	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}

	public String getPersonalMsg() {
		return personalMsg;
	}
	public void setPersonalMsg(String personalMsg) {
		this.personalMsg = personalMsg;
	}

	public String getClosing() {
		return closing;
	}
	public void setClosing(String closing) {
		this.closing = closing;
	}

	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getProductMsg() {
		return productMsg;
	}
	public void setProductMsg(String productMsg) {
		this.productMsg = productMsg;
	}

	public String getProdids() {
		return prodids;
	}
	public void setProdids(String prodids) {
		this.prodids = prodids;
	}
}
