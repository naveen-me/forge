package com.svb.struts.action.Brand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;




import com.svb.struts.bean.BrandDetailsBean;
import com.svb.struts.bean.RegimenDetailsBean;
import com.svb.struts.service.BrandDetailService;
import com.svb.struts.DAL.BrandDetailsDB;



public class BrandDetail extends Action{
	public ActionForward execute(ActionMapping mapping,
			ActionForm form, 
			HttpServletRequest request, 
			HttpServletResponse response)
			throws IOException, ServletException {
		
		
		
		 String target = new String("Success");
		 ArrayList brands= new ArrayList();
		 
		 String[] brandId = new String[20];
		 
	
//	BrandDetailsDB detail= new BrandDetailsDB();
//	brandId=detail.getBrandID();
	
//	for(int i=0; i<brandId.length; i++){
//		
//		System.out.println("brand id of action:"+brandId[i]);
//	}
	
	BrandDetailService brandService= new BrandDetailService();
	brands = brandService.getBrandDetails();
	
	
	request.setAttribute("brands", brands);
	
	return mapping.findForward(target);

}
}
