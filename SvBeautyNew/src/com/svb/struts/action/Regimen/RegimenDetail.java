/*******************************************************************************
 * Copyright (c) 2005 Eteration A.S. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eteration A.S. - initial API and implementation
 ******************************************************************************/
package com.svb.struts.action.Regimen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


import com.svb.struts.bean.RegimenDetailsBean;
import com.svb.struts.service.RegimenDetailService;

public class RegimenDetail extends Action {

	public ActionForward execute(ActionMapping mapping,
									ActionForm form, 
									HttpServletRequest request, 
									HttpServletResponse response)
									throws IOException, ServletException {
		
		ArrayList regimenList;
		String[] productId = {"1", "2", "3", "4"};
		String[] image = new String[productId.length];
		
		RegimenDetailService service = new RegimenDetailService();
		regimenList = service.getRegimenDetails(productId);
		int count = 0;
		
		Iterator i = regimenList.iterator();
		
		while(i.hasNext()){
			RegimenDetailsBean bean = (RegimenDetailsBean)i.next();
			image[count++] = bean.getImageMedium();
		}
		
		request.setAttribute("regimenList", regimenList);
		request.setAttribute("image",image);
		
		String target = "Success";
		return mapping.findForward(target);
	}
	

}