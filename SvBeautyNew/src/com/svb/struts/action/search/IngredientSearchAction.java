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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.svb.struts.service.IngredientSearchService;

public class IngredientSearchAction extends Action {

	String link;
	String search;
	
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}
	
	int index;
	String ing;
	
	public ActionForward execute(ActionMapping mapping,
									ActionForm form, 
									HttpServletRequest request, 
									HttpServletResponse response)
									throws IOException, ServletException {
		
		String offset = request.getParameter("pager.offset");
		String ing = request.getParameter("ing");
		int maxIndexPages = 100;
		int maxPageItems = 20;
		
		if(offset == null){
			setLink(request.getParameter("link"));
			setSearch(request.getParameter("search"));
		}else {
			try{
				setLink(request.getAttribute("linkOffset").toString());
				setSearch(request.getAttribute("searchOffset").toString());
			}catch(NullPointerException ne){
				ne.printStackTrace();
			}
		}
		
		request.setAttribute("maxIndexPages", ""+maxIndexPages);
		request.setAttribute("maxPageItems", ""+maxPageItems);
		request.setAttribute("linkOffset", getLink());
		request.setAttribute("searchOffset", getSearch());
		
		IngredientSearchService service = new IngredientSearchService();
		HashMap ingredientsMap = service.getIngredients(getLink(), getSearch());
		
		index = ingredientsMap.size();
		
		if(ing!=null){
			Set keys = ingredientsMap.keySet();
			Iterator it = keys.iterator();
			int count = 0;
			while(it.hasNext()){
				count++;
				if(it.next().toString().equals(ing)){
					index = count;
				}
			}
		    /*request.setAttribute("pager", index/maxPageItems);
			System.out.println(ing);*/
		}
		
		//System.out.println(offset);
		request.setAttribute("ingredientsMap", ingredientsMap);
		request.setAttribute("length", ""+index);
		
		return mapping.findForward("Success");
	}

}