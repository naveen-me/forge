/*******************************************************************************
 * Copyright (c) 2005 Eteration A.S. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eteration A.S. - initial API and implementation
 ******************************************************************************/
package com.svb.struts.action.Details;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.svb.common.javabean.CustomerTrackBean;
import com.svb.struts.bean.ProductDetailsBean;
import com.svb.struts.service.DetailsService;

public class ProductDetails extends Action {

	String target;
	ProductDetailsBean details;
	HashMap products;
	HashMap ingredientsWithGlossary;
	
	String[][] creviews = new String[3][3] ;
	
	public ActionForward execute(ActionMapping mapping,
									ActionForm form, 
									HttpServletRequest request, 
									HttpServletResponse response)
									throws IOException, ServletException {

		DetailsService service = new DetailsService();
		try{
			products = service.getProductNames();
			target = "Success";
			request.setAttribute("products", products);
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		
		try{
			
			ProductDetailsBean details = service.getProductDetails(request.getParameter("productId"));
			request.setAttribute("productDetails", details);
			
			ingredientsWithGlossary = service.getIngredientNamesWithGlossary();
			request.setAttribute("ingredientsWithGlossary", ingredientsWithGlossary);
			
			details.getIngredients().forEach(ingrediant->{
				if(ingredientsWithGlossary.containsKey(details.getIngredients())) {
				}
			});
			
			if(request.getParameter("popup").equals("y")){
				target = "Popup";
//				System.out.println(request.getParameter("popup"));
			}
			if(request.getParameter("desc").equals("y")){
				request.setAttribute("desc","y");
				
				HttpSession session = request.getSession(false);
				String customerId = (String) session.getAttribute("cusid") ; 
				CustomerTrackBean ctb = new CustomerTrackBean() ;
				ctb.insertCustomerTrack("Skin",customerId,"Product Details","Product Description",request.getParameter("productId"),"") ;
				 				
			}
			if(request.getParameter("direc").equals("y")){
				request.setAttribute("direc","y");
			}
			if(request.getParameter("ingred").equals("y")){
				request.setAttribute("ingred","y");
			}
			if(request.getParameter("revw") != null && request.getParameter("revw").equals("y")){
				request.setAttribute("revw","y");
	            creviews = (String[][]) service.getCustomerReviews(request.getParameter("productId")) ;
				request.setAttribute("creviews",creviews) ;
			}
			if(request.getParameter("glos").equals("y")){
				request.setAttribute("glos","y");
				request.setAttribute("ingName", request.getParameter("ingName"));
				
				HttpSession session = request.getSession(false);
				String customerId = (String) session.getAttribute("cusid") ; 
				CustomerTrackBean ctb = new CustomerTrackBean() ;
				if(request.getParameter("scrname")!=null){
					ctb.insertCustomerTrack("Skin",customerId,request.getParameter("scrname"),"Ingredients Glossory",request.getParameter("ingName"),"") ;	
				}
				else 
					ctb.insertCustomerTrack("Skin",customerId,"Product Details","Ingredients Glossory",request.getParameter("ingName"),"") ;
			}
			if(request.getParameter("scrname")!=null){
				request.setAttribute("scrname", request.getParameter("scrname"));
				request.setAttribute("customerId", request.getParameter("customerId"));
				request.setAttribute("productId", request.getParameter("productId"));
				request.setAttribute("attributeId", request.getParameter("attributeId"));
				request.setAttribute("attributeValue", request.getParameter("attributeValue"));
				request.setAttribute("attributeName", request.getParameter("attributeName"));
				request.setAttribute("attributeScore", request.getParameter("attributeScore"));
			}
		
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		
		System.out.println(target);
		return mapping.findForward(target);
	}
	
}