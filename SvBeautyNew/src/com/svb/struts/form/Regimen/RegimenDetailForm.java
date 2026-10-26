/*******************************************************************************
 * Copyright (c) 2005 Eteration A.S. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eteration A.S. - initial API and implementation
 ******************************************************************************/

package com.svb.struts.form.Regimen;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class RegimenDetailForm extends ActionForm {
	
	private String qty1;
	private String qty2;
	private String qty3;
	private String qty4;
	private List selection;
	
	public List getSelection() {
		return selection;
	}

	public void setSelection(List selection) {
		this.selection = selection;
	}

	public java.lang.String getQty1(){
		return qty1;
	}

	public void setQuantity(java.lang.String qty1) {
		this.qty1 = qty1;
	}

	public String getQty2() {
		return qty2;
	}

	public void setQty2(String qty2) {
		this.qty2 = qty2;
	}

	public String getQty3() {
		return qty3;
	}

	public void setQty3(String qty3) {
		this.qty3 = qty3;
	}

	public String getQty4() {
		return qty4;
	}

	public void setQty4(String qty4) {
		this.qty4 = qty4;
	}

	public void setQty1(String qty1) {
		this.qty1 = qty1;
	}

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
	
}