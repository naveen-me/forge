package com.svb.customerregistration.actions;

/**
 * @author David  murphy
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.svb.common.ConstantDeclarations;
import com.svb.customerprofile.dal.CustomerProfileDAL;
import com.svb.customerprofile.formbeans.ProfileSummaryFormBean;
import com.svb.customerregistration.dal.CustomerRegistrationDAL;

public class CustomerRegistrationAction extends Action {
	private static Log log = LogFactory.getLog(CustomerRegistrationAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		
		String s2="";
		String target = "";
		String cusid="";
		String mid="";
		String mode="";
		String msg="";
		String cre[][] = new String[0][0];
		String remoteIP = "";
		String sessionID = "";
        String catid = "" ;
        String customerId = "" ;
        System.out.println("At CustomerRegistrationAction") ;
		HttpSession session = request.getSession(true) ;
		 System.out.println("ccccccccc") ;
		CustomerRegistrationDAL rid = new CustomerRegistrationDAL();
		ProfileSummaryFormBean formbean = new ProfileSummaryFormBean();

		if (request.getParameter("mid") != null){
			mid = request.getParameter("mid") ;
			System.out.println("MID: " + mid) ;
		}
			
		if (request.getParameter("mode") != null){
			mode = request.getParameter("mode") ;
			System.out.println("Mode: " + mode) ;
		}
		
		if (request.getParameter("catid") != null){
			catid = request.getParameter("catid") ;
			System.out.println("CatId: " + catid) ;
		}
		
		if (request.getParameter("customerId") != null){
			customerId = request.getParameter("customerId") ;
			System.out.println("Customer Id: " + customerId) ;
		}
		
		System.out.println("mode  "+mode+"  catid "+catid+"  customerId "+customerId) ;
		if (customerId == null || customerId.length() == 0)
		{
           System.out.println("ccccccccc in if") ;
		   cusid=rid.setcustid();
		}
		else
		   cusid = customerId ;
	
		System.out.println("customer ID in registration action "+cusid) ;
		
		target = "general" ;
		
		if (mode != null && mode.equals("skin"))
		    target="general";
		else if (mode != null && mode.equals("hair"))
		    target="haircare";
		else if (mode != null && mode.equals("skinNew"))
		    target="skinNewUI";
		
		session.setAttribute("cusid",cusid);
  		request.setAttribute("cusid",cusid);

		session.setAttribute("mid",mid);
  		request.setAttribute("mid",mid);

		session.setAttribute("mode",mode);
  		request.setAttribute("mode",mode);
  		
		session.setAttribute("catid",catid);
  		request.setAttribute("catid",catid);
  		
        formbean.setCusid(cusid) ;
        formbean.setMid(mid) ;
        formbean.setMode(mode) ;
        System.out.println("Target: " + target) ;
		System.out.println("111 done customerid "+cusid) ;
		return (mapping.findForward(target));
	}


	public String insertCustDetails(String cfname,String clname,String eid,String repid) {	String result= "";
		CustomerRegistrationDAL rid2 = new CustomerRegistrationDAL();
		try {
			result = rid2.insertCusDetails(cfname,clname,eid,repid);
		} catch(Exception e2) {
			e2.printStackTrace();
		}
		return result;
	}

}