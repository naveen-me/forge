/*******************************************************************************
 * Copyright (c) 2005 Eteration A.S. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eteration A.S. - initial API and implementation
 ******************************************************************************/

package com.svb.struts.form.search;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class IngredientSearchform extends ActionForm {
	
	private java.lang.String search;

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
		
	
	public java.lang.String getSearch(){
		return search;
	}

	public void setSearch(java.lang.String search) {
		this.search = search;
	}
	
}