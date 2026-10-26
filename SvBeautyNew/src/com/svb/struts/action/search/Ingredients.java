/*******************************************************************************
 * Copyright (c) 2005 Eteration A.S. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eteration A.S. - initial API and implementation
 ******************************************************************************/
package com.svb.struts.action.search;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.svb.struts.service.IngredientSearchService;

public class Ingredients extends Action {

	public ActionForward execute(ActionMapping mapping,
									ActionForm form, 
									HttpServletRequest request, 
									HttpServletResponse response)
									throws IOException, ServletException {
		
		IngredientSearchService service = new IngredientSearchService();
		ArrayList ingredients = service.getIngredients();
		
		request.setAttribute("ingredients", ingredients);
		
		String target = "Success";
		return mapping.findForward(target);
	}
}