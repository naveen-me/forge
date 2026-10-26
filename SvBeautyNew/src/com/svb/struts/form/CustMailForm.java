/*******************************************************************************
 * Copyright (c) 2005 Eteration A.S. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eteration A.S. - initial API and implementation
 ******************************************************************************/

package com.svb.struts.form;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class CustMailForm extends ActionForm {
	
	private java.lang.String to_email;
	private java.lang.String from_email;
	private java.lang.String password;
	private java.lang.String chkbox1;
	private java.lang.String attachfile;
	private java.lang.String to_name;
	private java.lang.String from_name;
	private java.lang.String customerMessage;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
		// Validate an attribute named "xxx"
		//if( getXXX() == null || getXXX().length() == 0 )
		//	errors.add("xxx",new ActionMessage("errors.required","xxx"));
		return errors;
	}
		
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		
		super.reset(mapping, request);
	}
		
	
	public java.lang.String getTo_email(){
		return to_email;
	}

	public void setTo_email(java.lang.String to_email) {
		this.to_email = to_email;
	}
	
	public java.lang.String getFrom_email(){
		return from_email;
	}

	public void setFrom_email(java.lang.String from_email) {
		this.from_email = from_email;
	}
	
	public java.lang.String getPassword(){
		return password;
	}

	public void setPassword(java.lang.String password) {
		this.password = password;
	}
	
	public java.lang.String getChkbox1(){
		return chkbox1;
	}

	public void setChkbox1(java.lang.String chkbox1) {
		this.chkbox1 = chkbox1;
	}
	
	public java.lang.String getAttachfile(){
		return attachfile;
	}

	public void setAttachfile(java.lang.String attachfile) {
		this.attachfile = attachfile;
	}
	public java.lang.String getTo_name(){
		return to_name;
	}

	public void setTo_name(java.lang.String to_name) {
		this.to_name =to_name;
	}
	public java.lang.String getFrom_name(){
		return from_name;
	}

	public void setFrom_name(java.lang.String from_name) {
		this.from_name =from_name;
	}
	
	public java.lang.String getCustomerMessage(){
		return customerMessage;
	}

	public void setCustomerMessage(java.lang.String customerMessage) {
		this.customerMessage = customerMessage;
	}
	
}