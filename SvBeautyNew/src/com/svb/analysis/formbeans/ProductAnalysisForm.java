/*
 * Created on Mar 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.analysis.formbeans;

import org.apache.struts.action.ActionForm;


/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ProductAnalysisForm extends ActionForm {

	String repId = "";
	String customerId   = "";
	String prodType     = "";
	String prodLine    = "";
    String prodId = "" ;	
    String dname = "" ;
	
	
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
	
	public String getProdType() {
		return prodType;
	}
	public void setProdType(String prodType) {
		this.prodType = prodType;
	}
	
	public String getProdLine() {
		return prodLine;
	}
	
	public void setProdId(String prodId) {
		this.prodId = prodId;
	}
	
	public String getProdId() {
		return prodId;
		
	}public void setProdLine(String prodLine) {
		this.prodLine = prodLine;
	}
	
}
