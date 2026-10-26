/*
 * Created on Mar 23, 2005
 *
 */
package com.svb.regimenmail.actions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.*;
import org.apache.struts.actions.DispatchAction;

import com.svb.aplgd.formbeans.ApplicationGuidelinesForm;
import com.svb.common.ConstantDeclarations;
import com.svb.common.mail.ManageMail;
import com.svb.common.utils.SVAvonUtils;
import com.svb.common.valueobjects.*;
import com.svb.ratings.formbeans.ProductRegimenForm;
import com.svb.ratings.javabeans.HairProductRecommendationEngine;
import com.svb.ratings.javabeans.HairRecommendationEngine;
import com.svb.regimenmail.javabeans.HairRegimenMail;

/**
 * @author Neelima
 * 
 */
public class HairRegimenMailAction extends DispatchAction {

	private static Log log = LogFactory.getLog(HairRegimenMailAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String target = "success";
		
		String customerId = "" ;
		
		String mid = "" ;
		String catid = "" ;
		String mode = "" ;
	    String finishprod = "" ;
	    String shampooprod = "" ;
	    String conditionerprod = "" ;
	    String htreatprod = "" ;
	    String stylingprod = "" ;
        String finsihexists = "" ;
        String prodstring = "" ;
        ArrayList categprods = new ArrayList()  ;
        String finsih[] = new String[0] ;
        String shampooskip = "" ;
        String conditionerskip = "" ;
        String htreatskip = "" ;
        String stylingskip = "" ;
        
		ProductVO product[] = new ProductVO[0];
		CustomerProfileVO cp = new CustomerProfileVO() ;
		ApplicationGuidelinesForm applicationGuidelinesForm = (ApplicationGuidelinesForm)form;
		ArrayList prodlist = new ArrayList();
		Hashtable prodhash = new Hashtable() ;
        Hashtable master = new Hashtable() ;
        
        String prodType = "" ;
        String prodLine = "" ;
        String prodLineFlag = "" ;
        
        String cusid = "" ;
        
		customerId = applicationGuidelinesForm.getCustomerId();
		prodType   = applicationGuidelinesForm.getProdType();
		prodLine   = applicationGuidelinesForm.getProdLine();
	//	cusid = applicationGuidelinesForm.getCusid();
		
		HttpSession session = request.getSession(false);
		
	    if (request.getParameter("catid") != null)
	    	catid = request.getParameter("catid") ;
	    if (request.getParameter("mid") != null)
	    	mid = request.getParameter("mid") ;
	    if (request.getParameter("mode") != null)
	    	mode = request.getParameter("mode") ;
	    
	    if (mid == null || mid.trim().length() == 0)
        	mid = (String) session.getAttribute("mid") ;
        if (mode == null || mode.trim().length() == 0)
        	mode = (String) session.getAttribute("mode") ;
	    if (catid == null || catid.trim().length() == 0)
        	catid = (String) session.getAttribute("catid") ;
        
        shampooskip = (String) request.getParameter("shampooSkip") ;    
        conditionerskip = (String) request.getParameter("conditionerSkip") ;    
        htreatskip = (String) request.getParameter("htreatSkip") ;    
        stylingskip = (String) request.getParameter("stylingSkip") ;    
        if (request.getParameter("prodLineFlag") != null)
        	prodLineFlag = request.getParameter("prodLineFlag") ;
        
        if (request.getParameterValues("finsih") != null)
        	finsih = request.getParameterValues("finsih") ;
        if (request.getParameter("finsihexists") != null)
        	finsihexists = request.getParameter("finsihexists") ;
        if (request.getParameter("finishprod") != null)
        {
        	finishprod = request.getParameter("finishprod") ;
        	categprods.add(finishprod) ;
        }
        if (request.getParameter("shampooprod") != null)
        {
        	shampooprod = request.getParameter("shampooprod") ;
        	categprods.add(shampooprod) ;
        }
        if (request.getParameter("conditionerprod") != null)
        {
        	conditionerprod = request.getParameter("conditionerprod") ;
        	categprods.add(conditionerprod) ;
        }
        if (request.getParameter("htreatprod") != null)
        {
        	htreatprod = request.getParameter("htreatprod") ;
        	categprods.add(htreatprod) ;
        }
        if (request.getParameter("stylingprod") != null)
        {
        	stylingprod = request.getParameter("stylingprod") ;
        	categprods.add(stylingprod) ;
        }
	    
		System.out.println("dsdf finish "+finishprod) ;	
			request.setAttribute("customerId", customerId);
		    request.setAttribute("productlist", prodlist);
		    request.setAttribute("producthash", prodhash);
		    request.setAttribute("masterData", master);
		    //session.setAttribute("product",product) ;
            request.setAttribute("customerprofile",cp) ;
            request.setAttribute("prodline",prodLine) ;
            request.setAttribute("prodlineflag",prodLineFlag) ;
            request.setAttribute("shampooprod",shampooprod) ;
            request.setAttribute("conditionerprod",conditionerprod) ;
            request.setAttribute("htreatprod",htreatprod) ;
            request.setAttribute("stylingprod",stylingprod) ;
            request.setAttribute("finishprod",finishprod) ;
            request.setAttribute("finsihexists",finsihexists) ;
            request.setAttribute("finsih",finsih) ;
            request.setAttribute("shampooskip",shampooskip) ;
            request.setAttribute("conditionerskip",conditionerskip) ;
            request.setAttribute("htreatskip",htreatskip) ;
            request.setAttribute("stylingskip",stylingskip) ;
			request.setAttribute("mid", mid);
			request.setAttribute("catid", catid);
			request.setAttribute("mode", mode);
			
			
			return (mapping.findForward(target));
	}
  	
	public ActionForward sendEmail(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String target = "productregimen";
		
		String customerId = "" ;
		
	    String finishprod = "" ;
	    String shampooprod = "" ;
	    String conditionerprod = "" ;
	    String htreatprod = "" ;
	    String stylingprod = "" ;
        String finsihexists = "" ;
        String prodstring = "" ;
        ArrayList categprods = new ArrayList()  ;
        String finsih[] = new String[0] ;
        
		ApplicationGuidelinesForm applicationGuidelinesForm = (ApplicationGuidelinesForm)form;
		ProductVO product[] = new ProductVO[0];
		CustomerProfileVO cp = new CustomerProfileVO() ;
		ArrayList prodlist = new ArrayList();
		Hashtable prodhash = new Hashtable() ;
        Hashtable master = new Hashtable() ;
        
        String prodType = "" ;
        String prodLine = "" ;
        String cusid = "" ;
		customerId = applicationGuidelinesForm.getCustomerId();
		prodType   = applicationGuidelinesForm.getProdType();
		prodLine   = applicationGuidelinesForm.getProdLine();
	//	cusid = applicationGuidelinesForm.getCusid();
		
		HttpSession session = request.getSession(false);
		
        if (request.getParameterValues("finsih") != null)
        	finsih = request.getParameterValues("finsih") ;
        if (request.getParameter("finsihexists") != null)
        	finsihexists = request.getParameter("finsihexists") ;
        if (request.getParameter("finishprod") != null)
        {
        	finishprod = request.getParameter("finishprod") ;
        	categprods.add(finishprod) ;
        }
        if (request.getParameter("shampooprod") != null)
        {
        	shampooprod = request.getParameter("shampooprod") ;
        	categprods.add(shampooprod) ;
        }
        if (request.getParameter("conditionerprod") != null)
        {
        	conditionerprod = request.getParameter("conditionerprod") ;
        	categprods.add(conditionerprod) ;
        }
        if (request.getParameter("htreatprod") != null)
        {
        	htreatprod = request.getParameter("htreatprod") ;
        	categprods.add(htreatprod) ;
        }
        if (request.getParameter("stylingprod") != null)
        {
        	stylingprod = request.getParameter("stylingprod") ;
        	categprods.add(stylingprod) ;
        }
	    
	    String salutation = "Hello";
   		String personalMsg = "";
   		String closing = "All the best to you";
   		String signature = "";
		String mailId = "";
		String friendsName = "" ;
		String friendsEmail = "" ;
		String customerName = "" ;
		String customerEmail = "" ;
		String customerMessage = "" ;
		
		String unsubscribePrev = "";
	//	RegimenMailDAL rad = new RegimenMailDAL() ;
		HairRegimenMail regimenMail = new HairRegimenMail();
		    
		
		unsubscribePrev = regimenMail.getCustomerUnSubScribePrv("customerEmail");
		request.setAttribute("unsubscribePrev",unsubscribePrev);
		
   	    String subj = "Personalized skincare regimen" ;
        String mailtype = "D" ;
        String messagesent = "Yes" ;
        String date;
		
       	if (request.getParameter("friendsName") != null)
       	 	friendsName = request.getParameter("friendsName") ;
       	else
       		friendsName = "";
				
       	if (request.getParameter("friendsEmail") != null)
       	 	friendsEmail = request.getParameter("friendsEmail") ;
       	else
       		friendsEmail = "";
       	
       	if (request.getParameter("customerName") != null)
       	 	customerName = request.getParameter("customerName") ;
       	else
       		customerName = "";
				
       	if (request.getParameter("customerEmail") != null)
       	 	customerEmail = request.getParameter("customerEmail") ;
       	else
       		customerEmail = "";
           	    
       	if (request.getParameter("customerMessage") != null)
       	 	customerMessage = request.getParameter("customerMessage") ;
       	else
       		customerMessage = "";
       	
           	 	
           	try {
           		
//					 mailId = mm.saveMail(customerId, subj, mailtype,prodids, messagesent, "", personalMsg, fromMsg, themeMsg);
					   				
					target = "productregimen" ;
			    	 				 
					regimenMail.sendEmail(customerId,customerName,customerEmail,customerMessage,salutation+" "+friendsName+",",closing,signature,friendsEmail,request);
					 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		        
		        
		        //get Email Status
            String emailStatus = "";
		 //   emailStatus = rad.getCustEmailStatus(customerId);
		 //   request.setAttribute("emailStatus",emailStatus);
		        
			
			HairProductRecommendationEngine prodRecEng = new HairProductRecommendationEngine() ;
			HairRecommendationEngine recEng = new HairRecommendationEngine();
			cp = recEng.getCustomerProfile(customerId,cp,"hair") ;
			
			ArrayList products = new ArrayList();
			
//			products = recEng.getProdDetailsRegimen(customerId,prodLine);
			request.setAttribute("customerId", customerId);
		    request.setAttribute("productlist", prodlist);
		    request.setAttribute("producthash", prodhash);
		    request.setAttribute("masterData", master);
		    //session.setAttribute("product",product) ;
            request.setAttribute("customerprofile",cp) ;
            request.setAttribute("prodline",prodLine) ;
            request.setAttribute("shampooprod",shampooprod) ;
            request.setAttribute("conditionerprod",conditionerprod) ;
            request.setAttribute("htreatprod",htreatprod) ;
            request.setAttribute("stylingprod",stylingprod) ;
            request.setAttribute("finishprod",finishprod) ;
            request.setAttribute("finsihexists",finsihexists) ;
            request.setAttribute("finsih",finsih) ;
            request.setAttribute("mailmessage",messagesent) ;
         			
 			return (mapping.findForward(target));
	}
}