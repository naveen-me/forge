package com.svb.struts.action.Brand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;




import com.svb.common.javabean.CustomerTrackBean;
import com.svb.struts.bean.BrandDetailsBean;
import com.svb.struts.service.BrandDetailService;
import com.svb.struts.DAL.BrandDetailsDB;



public class BrandDiscriptionAction extends Action{
	public ActionForward execute(ActionMapping mapping,
			ActionForm form, 
			HttpServletRequest request, 
			HttpServletResponse response)
			throws IOException, ServletException {
		
		
		
		 String target = new String("Success");
		 ArrayList brands= new ArrayList();
		 String brandid= request.getParameter("brandparam");
		 System.out.println("brandid:"+brandid);
		 
		 HttpSession session = request.getSession(false);
		 String customerId = (String) session.getAttribute("cusid") ; 
		 CustomerTrackBean ctb = new CustomerTrackBean() ;
		 ctb.insertCustomerTrack("Skin",customerId,"Product Regimen","Brand Description",brandid,"") ;
		 
	BrandDetailService brandService= new BrandDetailService();
	brands = brandService.getBrandDiscription(brandid);
	
	
	request.setAttribute("brands", brands);
	
	return mapping.findForward(target);

}
}
