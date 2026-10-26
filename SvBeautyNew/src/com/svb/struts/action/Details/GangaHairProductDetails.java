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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.JSONException;
import org.json.JSONObject;

import com.svb.struts.bean.ProductDetailsBean;
import com.svb.struts.service.HairDetailsService;

public class GangaHairProductDetails extends Action {

	String target;
	ProductDetailsBean details;
	HashMap products;
	HashMap ingredientsWithGlossary;
	public ActionForward execute(ActionMapping mapping,
									ActionForm form, 
									HttpServletRequest request, 
									HttpServletResponse response)
									throws IOException, ServletException {
	    response.setContentType("application/json");
	    PrintWriter out=response.getWriter();
	    JSONObject jsonObject=new JSONObject();
	    List crList=new ArrayList();
		HairDetailsService service = new HairDetailsService();
		try{
			products = service.getProductNames();
			target = "Success";
			request.setAttribute("products", products);
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		
		try{
			String[][] creviews = new String[3][3] ;
			
			ProductDetailsBean details = service.getProductDetails(request.getParameter("productId"));
			ingredientsWithGlossary = service.getIngredientNamesWithGlossary();
			creviews = (String[][]) service.getCustomerReviews(request.getParameter("productId")) ;
			System.out.println("Product Description ==>"+details.getProductDescription());
			System.out.println("Product directions ==>"+details.getProductDirections());
			System.out.println("Product creviews ==>"+creviews);
		
			
			for (int r=0; r<creviews.length; r++) {
			     for (int c=0; c<creviews[r].length; c++) {
			         System.out.print("Product creviews: " + creviews[r][c]);
			         crList.add(creviews[r][c]);
			     }
			     System.out.println("");
			 }
		/*	List ingList=details.getIngredients();
			for (int i=0; i< ingList.size(); i++)
            {
                    System.out.println( ingList.get(i) );
            }*/
			
			/*Iterator entries = ingredientsWithGlossary.entrySet().iterator();
			while (entries.hasNext()) {
			    Map.Entry entry = (Map.Entry) entries.next();
			    String key = (String) entry.getKey();
			    String value = (String) entry.getValue();
			    System.out.println("Key = " + key + ", Value = " + value);
			}*/
			try {
				jsonObject.put("prod_des", details.getProductDescription());
				jsonObject.put("directions",details.getProductDirections());
				jsonObject.put("ingredient", details.getIngredients());
				jsonObject.put("creviews", crList);
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			request.setAttribute("productDetails", details);
			request.setAttribute("ingredientsWithGlossary", ingredientsWithGlossary);
			
			if(request.getParameter("popup").equals("y")){
				target = "Popup";
				System.out.println(request.getParameter("popup"));
			}
			
			/*if(request.getParameter("desc").equals("y")){
				HttpSession session = request.getSession(false);
				
				String customerId = (String) session.getAttribute("cusid") ; 
				System.out.println("customerId == >"+customerId);
				CustomerTrackBean ctb = new CustomerTrackBean() ;
				ctb.insertCustomerTrack("Hair",customerId,"Product Details","Product Description",request.getParameter("productId"),"") ;
				request.setAttribute("desc","y");
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
					ctb.insertCustomerTrack("Hair",customerId,request.getParameter("scrname"),"Ingredients Glossory",request.getParameter("ingName"),"") ;	
				}
				else 
					ctb.insertCustomerTrack("Hair",customerId,"Product Details","Ingredients Glossory",request.getParameter("ingName"),"") ;
			}
			if(request.getParameter("scrname")!=null){
				request.setAttribute("scrname", request.getParameter("scrname"));
				request.setAttribute("customerId", request.getParameter("customerId"));
				request.setAttribute("productId", request.getParameter("productId"));
				request.setAttribute("attributeId", request.getParameter("attributeId"));
				request.setAttribute("attributeValue", request.getParameter("attributeValue"));
				request.setAttribute("attributeName", request.getParameter("attributeName"));
				request.setAttribute("attributeScore", request.getParameter("attributeScore"));
			}*/
		
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		
		System.out.println(target);
		out.println(jsonObject);
		//return mapping.findForward(target);
		return null;
	}
	
}