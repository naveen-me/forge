/*
 * Created on Apr 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.customerregistration.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.svb.common.javabean.CustomerTrackBean;
import com.svb.common.valueobjects.CustProfileVO;
import com.svb.customerprofile.dal.CustomerProfileDAL;
import com.svb.customerprofile.formbeans.ProfileSummaryFormBean;

import com.svb.customerregistration.dal.CustomerRegistrationDAL;

/**
 * @author VB Rao
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SvCustomerProfileHairConcernsAction extends DispatchAction {


	public ActionForward get(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
		String target = "";
		String cusid = "" ;
		String mid = "" ;
		String catid = "" ;
		String mode = "" ;
		
		String message = "" ;
		String[][] crec = new String[20][20] ;
		ArrayList vtb = new ArrayList() ;
		String[][] rs = new String[1][8] ;
		String[][] rs1 = new String[0][0] ;
		Hashtable profdesc = new Hashtable() ;
		
		CustomerProfileDAL skinConProfileDAL = new CustomerProfileDAL();
		CustProfileVO custProfileVO = new CustProfileVO();
		try {
			target = "hairconcernsprofile";

			for (int m=0; m < rs.length ; m++)
				 for (int n=0; n < rs[m].length ; n++)
					rs[m][n] = "" ;

			for (int a=0; a < crec.length ; a++)
			  	for (int b=0; b < crec[a].length ; b++)
					crec[a][b] = "" ;

			ProfileSummaryFormBean profilesForm = (ProfileSummaryFormBean)form;
            HttpSession session  = request.getSession(true) ;
            
                    
			if (profilesForm.getCusid() != null)
					cusid = profilesForm.getCusid();
			if (profilesForm.getMid() != null)
				mid = profilesForm.getMid();
			
			if (profilesForm.getCatid() != null)
				catid = profilesForm.getCatid();
			
			if (profilesForm.getMode() != null)
				mode = profilesForm.getMode();
			
			if (cusid == null || cusid.trim().length() == 0)
				cusid = (String) session.getAttribute("cusid") ;
			
			if (mid == null || mid.trim().length() == 0)
				mid = (String) session.getAttribute("mid") ;
			
			if (mode == null || mode.trim().length() == 0)
				mode = (String) session.getAttribute("mode") ;
			
			if (catid == null || catid.trim().length() == 0)
				catid = (String) session.getAttribute("catid") ;
			
			System.out.println(" 333  mode  "+mode+"  catid "+catid+"  customerId "+cusid+"  cusid "+cusid) ;
			
			profilesForm.setCusid(cusid);
			profilesForm.setMid(mid);
			profilesForm.setMode(mode);
			
			vtb = (ArrayList) skinConProfileDAL.getCustomerHairRecord(cusid,"") ;
			
		    profilesForm.setVtb(vtb);
            profdesc = (Hashtable) skinConProfileDAL.getProfileDescriptions("hair") ; ;
		    
			 CustomerTrackBean ctb = new CustomerTrackBean() ;
		//	 ctb.insertCustomerTrack("Hair",cusid,"Product Recommendation","Edit Responses","Change Profile","") ;
			 
			request.setAttribute("ProfilesFormBean",profilesForm);
			request.setAttribute("cusid",cusid);
			request.setAttribute("mid",mid);
			request.setAttribute("mode",mode);
			request.setAttribute("catid",catid);
			request.setAttribute("profdesc",profdesc);
			System.out.println("I am done here 333333 ") ;
		} catch(Exception ex) {
			////logger.log(Level.DEBUG,ex);
			ex.printStackTrace();
		}
		return(mapping.findForward(target));
	}

	public ActionForward add(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
		String target = "";
		String cusid = "" ;
		String message = "" ;
		String mid = "" ;
		String mode = "" ;
        String catid = "" ;
        
		CustomerProfileDAL skinConProfileDAL = new CustomerProfileDAL();
		CustProfileVO custProfileVO = new CustProfileVO();
		
		try {
			ProfileSummaryFormBean profilesForm = (ProfileSummaryFormBean)form;



			if (profilesForm.getCusid() != null)
				cusid = profilesForm.getCusid();

			if (profilesForm.getMid() != null)
				mid = profilesForm.getMid();
			
			if (profilesForm.getMode() != null)
				mode = profilesForm.getMode();
			
			if (profilesForm.getCatid() != null)
				catid = profilesForm.getCatid();
			
			HttpSession session  = request.getSession(false);

			message = skinConProfileDAL.getProfileData(profilesForm,cusid,"") ;

			target = "hairregimenprofile";
			System.out.println(" 444  mode  "+mode+"  catid "+catid+"  customerId "+cusid+"  cusid "+cusid) ;
			profilesForm.setCusid(cusid);
			profilesForm.setMid(mid);
			profilesForm.setMode(mode);
			profilesForm.setCatid(catid);
			
			request.setAttribute("cusid",cusid);
		 	request.setAttribute("ProfilesFormBean",profilesForm);
			request.setAttribute("mid",mid);
			request.setAttribute("mode",mode);
			request.setAttribute("catid",catid);
		 	
		} catch (Exception ex) {
			////logger.log(Level.DEBUG,ex) ;
			ex.printStackTrace();
		}
		return(mapping.findForward(target));
	}

	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
		String target = "";
		String cusid = "" ;
		String message = "" ;
		ArrayList vtb = new ArrayList() ;
		String[][] rs = new String[1][8] ;
		String[][] rs1 = new String[0][0] ;
		CustomerProfileDAL skinConProfileDAL = new CustomerProfileDAL();
		CustProfileVO custProfileVO = new CustProfileVO();
		try {

			ProfileSummaryFormBean profilesForm = (ProfileSummaryFormBean)form;


			if (profilesForm.getCusid() != null)
				cusid = profilesForm.getCusid();


			 		target = "profilesummary";


					message = skinConProfileDAL.getProfileData(profilesForm,cusid,"");

				//insert in contact history when the profile is updated by the customer
		    	//skinConProfileDAL.addProfileInfoToContactHistory(repid,cusid,dname,"updated");

		    	request.setAttribute("cusid",cusid);
			 	request.setAttribute("ProfilesFormBean",profilesForm);

		} catch(Exception ex) {
			////logger.log(Level.DEBUG,ex);
			ex.printStackTrace();
		}
		return(mapping.findForward(target));
	}
}
