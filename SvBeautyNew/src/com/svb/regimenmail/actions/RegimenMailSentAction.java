/*
 * Created on Mar 23, 2005
 *
 */
package com.svb.regimenmail.actions;
import java.io.File;
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
import com.svb.documents.pdf.RegimenDescriptionPdf;
import com.svb.ratings.formbeans.ProductRegimenForm;
import com.svb.ratings.javabeans.ProductRecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngine;
import com.svb.regimenmail.javabeans.RegimenMail;
import com.svb.regimenmail.javabeans.RegimenMailDAL;
import com.svb.regimenmail.javabeans.RegimenTrackingDAL;

/**
 * @author Neelima
 * 
 */
public class RegimenMailSentAction extends DispatchAction {

	private static Log log = LogFactory.getLog(RegimenMailSentAction.class);
	
  	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String target = "productregimen";
		
		String customerId = "" ;
		
		String mid = "" ;
		String catid = "" ;
		String mode = "" ;
	    String spfprod = "" ;
	    String cleanseprod = "" ;
	    String moisturizeprod = "" ;
	    String treatprod = "" ;
	    String etreatprod = "" ;
        String spfexists = "" ;
        String prodstring = "" ;
        ArrayList categprods = new ArrayList()  ;
        String spf[] = new String[0] ;
        String attachpdf = "" ;
        
		ApplicationGuidelinesForm applicationGuidelinesForm = (ApplicationGuidelinesForm)form;
		ProductVO product[] = new ProductVO[0];
		CustomerProfileVO cp = new CustomerProfileVO() ;
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
        String cleanseskip = "" ;
        String moisturizeskip = "" ;
        String treatskip = "" ;
        String etreatskip = "" ;
		
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
        
        cleanseskip = (String) request.getParameter("cleanseSkip") ;    
        moisturizeskip = (String) request.getParameter("moisturizeSkip") ;    
        treatskip = (String) request.getParameter("treatSkip") ;    
        etreatskip = (String) request.getParameter("etreatSkip") ;    
        if (request.getParameter("prodLineFlag") != null)
        	prodLineFlag = request.getParameter("prodLineFlag") ;
        System.out.println("prodline flag "+prodLineFlag) ;
        if (request.getParameterValues("spf") != null)
        	spf = request.getParameterValues("spf") ;
        if (request.getParameter("spfexists") != null)
        	spfexists = request.getParameter("spfexists") ;
        if (request.getParameter("spfprod") != null)
        {
        	spfprod = request.getParameter("spfprod") ;
        	categprods.add(spfprod) ;
        }
        if (request.getParameter("cleanseprod") != null)
        {
        	cleanseprod = request.getParameter("cleanseprod") ;
        	categprods.add(cleanseprod) ;
        }
        if (request.getParameter("moisturizeprod") != null)
        {
        	moisturizeprod = request.getParameter("moisturizeprod") ;
        	categprods.add(moisturizeprod) ;
        }
        if (request.getParameter("treatprod") != null)
        {
        	treatprod = request.getParameter("treatprod") ;
        	categprods.add(treatprod) ;
        }
        if (request.getParameter("etreatprod") != null)
        {
        	etreatprod = request.getParameter("etreatprod") ;
        	categprods.add(etreatprod) ;
        }
	    
	    String salutation = "";
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
		RegimenMailDAL rad = new RegimenMailDAL() ;
		RegimenMail regimenMail = new RegimenMail();
		RegimenTrackingDAL rtd = new RegimenTrackingDAL() ;
		    
		
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
       	
		System.out.println("dffdgdfg customer name "+customerName) ;
		
       	if (request.getParameter("customerEmail") != null)
       	 	customerEmail = request.getParameter("customerEmail") ;
       	else
       		customerEmail = "";
       	
		System.out.println("dffdgdfg customer email "+customerEmail) ;		
           	    
       	if (request.getParameter("customerMessage") != null)
       	 	customerMessage = request.getParameter("customerMessage") ;
       	else
       		customerMessage = "";
        if (request.getParameter("attachPdf") != null)
        	attachpdf = request.getParameter("attachPdf") ;
        else
       	    attachpdf = "no" ;
        String filename = "" ;
        if (attachpdf.equals("yes"))
        {
        //	String filePath = getServlet().getServletContext().getRealPath("/") + "pdfFiles";
        //	System.out.println("filepath in action "+filePath) ;
       // 	filename = filePath+"/sv"+customerId+".pdf" ;
      //  	System.out.println("filename in action "+filename) ;
        	
    		String regimenId = "sv"+customerId ;
    		String filePath = getServlet().getServletContext().getRealPath("/") + "pdfFiles";
    //		File folder = new File(filePath);
    //		folder.mkdir();
    		
    		File pdfFile = new File(filePath, regimenId+".pdf");
    		File page1 = new File(filePath, regimenId+"page1.pdf");
    		File page2 = new File(filePath, regimenId+"page2.pdf");
    		File page3 = new File(filePath, regimenId+"page3.pdf");
    		File page4 = new File(filePath, regimenId+"page4.pdf");
    		
    		RegimenDescriptionPdf pdf = new RegimenDescriptionPdf();
    		pdf.pdfForRegimen(regimenId, pdfFile, page1, page2, page3, page4, filePath, request);

//    		request.setAttribute("fileName", regimenId+".pdf");
    		page1.delete();
    		page2.delete();
    		page3.delete();
    		page4.delete();
        	
            filePath = getServlet().getServletContext().getRealPath("/") + "pdfFiles";
            //	System.out.println("filepath in action "+filePath) ;
           	filename = filePath+"/sv"+customerId+".pdf" ;
          //  	System.out.println("filename in action "+filename) ;
        	
        }
           	 	
           	try {
           		
//					 mailId = mm.saveMail(customerId, subj, mailtype,prodids, messagesent, "", personalMsg, fromMsg, themeMsg);
					   				
					target = "productregimen" ;
			    	 				 
					regimenMail.sendEmail(customerId,customerName,customerEmail,customerMessage,friendsName,filename,attachpdf,friendsEmail,request);
					rtd.insertCustomerEmail(customerId,"01",customerEmail,friendsEmail,attachpdf) ;
			
					 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		        
		        
		        //get Email Status
            String emailStatus = "";
		    emailStatus = rad.getCustEmailStatus(customerId);
		    request.setAttribute("emailStatus",emailStatus);
		        
			
			ProductRecommendationEngine prodRecEng = new ProductRecommendationEngine() ;
			RecommendationEngine recEng = new RecommendationEngine();
			cp = recEng.getCustomerProfile(customerId,cp,"skin") ;
			
			ArrayList products = new ArrayList();
			
//			products = recEng.getProdDetailsRegimen(customerId,prodLine);
			request.setAttribute("customerId", customerId);
		    request.setAttribute("productlist", prodlist);
		    request.setAttribute("producthash", prodhash);
		    request.setAttribute("masterData", master);
		    //session.setAttribute("product",product) ;
            request.setAttribute("customerprofile",cp) ;
            request.setAttribute("prodline",prodLine) ;
            request.setAttribute("prodlineflag",prodLineFlag) ;
            request.setAttribute("cleanseprod",cleanseprod) ;
            request.setAttribute("moisturizeprod",moisturizeprod) ;
            request.setAttribute("treatprod",treatprod) ;
            request.setAttribute("etreatprod",etreatprod) ;
            request.setAttribute("spfprod",spfprod) ;
            request.setAttribute("spfexists",spfexists) ;
            request.setAttribute("spf",spf) ;
            request.setAttribute("mailmessage","yes") ;
            request.setAttribute("cleanseskip",cleanseskip) ;
            request.setAttribute("moisturizeskip",moisturizeskip) ;
            request.setAttribute("treatskip",treatskip) ;
            request.setAttribute("etreatskip",etreatskip) ;
			request.setAttribute("mid", mid);
			request.setAttribute("catid", catid);
			request.setAttribute("mode", mode);
         			
 			return (mapping.findForward(target));
	}
}