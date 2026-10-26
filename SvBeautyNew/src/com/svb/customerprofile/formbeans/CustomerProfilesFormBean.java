/*
 * Created on Mar 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.customerprofile.formbeans;

import org.apache.struts.action.ActionForm;

/**
 * @author David  murphy
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CustomerProfilesFormBean extends ActionForm

{

	String repid;
	String option1;
	String option2;
	String add_modify;
	String cusid;
	String searchname;
	int pagenum;
	String sorttype;

	public String getRepid() {
		//logger.log(Level.DEBUG,repid);
		return repid;
	}
	public void setRepid(String repid) {
		this.repid = repid;
		//logger.log(Level.DEBUG,this.repid);
	}

	public String getSorttype() {
		//logger.log(Level.DEBUG,sorttype);
		return sorttype;
	}
	public void setSorttype(String sorttype) {
		this.sorttype = sorttype;
		//logger.log(Level.DEBUG,this.sorttype);
	}

	public String getOption1() {
		//logger.log(Level.DEBUG,option1);
		return option1;
	}
	public void setOption1(String option1) {
		this.option1 = option1;
		//logger.log(Level.DEBUG,this.option1);
	}

	public String getOption2() {
		//logger.log(Level.DEBUG,option2);
		return option2;
	}
	public void setOption2(String option2) {
		this.option2 = option2;
		//logger.log(Level.DEBUG,this.option2);
	}

	public String getAdd_modify(){
		return add_modify;
	}
	public void setAdd_modify(String add_modify){
		this.add_modify = add_modify;
	}

	public String getCusid() {
		//logger.log(Level.DEBUG,cusid);
		return cusid;
	}
	public void setCusid(String cusid) {
		this.cusid = cusid;
		//logger.log(Level.DEBUG,this.cusid);
	}
	public String getSearchname() {
		//logger.log(Level.DEBUG,searchname);
		return searchname;
	}
	public void setSearchname(String searchname) {
		this.searchname = searchname;
		//logger.log(Level.DEBUG,this.searchname);
	}

	public int getPagenum() {
		//logger.log(Level.DEBUG,pagenum);
		return pagenum;
	}
	public void setPagenum(int pagenum) {
		this.pagenum = pagenum;
		//logger.log(Level.DEBUG,this.pagenum);
	}
}
