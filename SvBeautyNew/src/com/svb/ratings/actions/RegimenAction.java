/*
 * Created on Mar 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.ratings.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;


import com.svb.analysis.actions.AnalysisDetailAction;
import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.javabean.CustomerTrackBean;
import com.svb.common.valueobjects.* ;
import com.svb.customerprofile.formbeans.ProfileSummaryFormBean;
import com.svb.ratings.dal.ProductRegimenDAL;
import com.svb.ratings.formbeans.ProductRegimenForm ;
import com.svb.ratings.javabeans.ProductRecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngine ;
import com.svb.regimenmail.javabeans.RegimenMailDAL;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RegimenAction extends DispatchAction {

	private static Log log = LogFactory.getLog(RegimenAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

        //System.out.println("I am in regimen VB 1......") ;
		String target = "success";
		String customerId     = "";
		String cusid = "" ;
		String mid = "" ;
		String mode = "" ;
        String catid = "" ;
        
		String prodType = "" ;
		String prodLine = "" ;
		String printPage = "";
		boolean prodrec = false ;

		ProductVO product[] = new ProductVO[0];
		CustomerProfileVO cp = new CustomerProfileVO() ;
		ArrayList prodlist = new ArrayList();
        Hashtable master = new Hashtable() ;
        RecommendationEngine rec = new RecommendationEngine() ;
        ProductRecommendationEngine recEng = new ProductRecommendationEngine() ;
		ConstantDeclarations cdl = new ConstantDeclarations() ;
        SVDBOperation dbo = new SVDBOperation();

        String cleanseprod = "" ;
        String moisturizeprod = "" ;
        String treatprod = "" ;
        String etreatprod = "" ;
        String cleanseskip = "" ;
        String moisturizeskip = "" ;
        String treatskip = "" ;
        String etreatskip = "" ;
        String scrname = "" ;
        ArrayList prodidlist = new ArrayList() ;
        ArrayList cleanse = new ArrayList() ;
	    ArrayList moisturize = new ArrayList() ;
	    ArrayList treat = new ArrayList() ;
	    ArrayList etreat = new ArrayList() ;
        
		HttpSession session = request.getSession(false);
		ProductRegimenForm regimenForm = (ProductRegimenForm)form;
		//ProfileSummaryFormBean profilesForm = (ProfileSummaryFormBean)form;
        
			customerId = regimenForm.getCustomerId();
			prodType   = regimenForm.getProdType();
			prodLine   = regimenForm.getProdLine();
		    cusid = regimenForm.getCusid();
		    
	//		if (profilesForm.getCusid() != null)
	//			cusid = profilesForm.getCusid();
			
	//		if (profilesForm.getMid() != null)
	//			mid = profilesForm.getMid();
			
	//		if (profilesForm.getMode() != null)
	//			mode = profilesForm.getMode();
			
		    if (cusid == null  || cusid.length() == 0)
			   cusid = (String) request.getAttribute("cusid");
	        if (cusid.trim().length() == 0)
	        	cusid = (String) session.getAttribute("cusid") ;
	        
		    if (mid.trim().length() == 0)
		        	mid = (String) session.getAttribute("mid") ;
		    if (mode.trim().length() == 0)
	        	mode = (String) session.getAttribute("mode") ;
		    if (catid.trim().length() == 0)
	        	catid = (String) session.getAttribute("catid") ;
		        
			if(regimenForm.getPrintPage() != null)
				printPage = regimenForm.getPrintPage();
	        customerId = cusid ;
	        
	        cleanseprod = (String) request.getParameter("cleanseprod") ;    
	        moisturizeprod = (String) request.getParameter("moisturizeprod") ;    
	        treatprod = (String) request.getParameter("treatprod") ;    
	        etreatprod = (String) request.getParameter("etreatprod") ;
	        
	        cleanseskip = (String) request.getParameter("cleanseSkip") ;    
	        moisturizeskip = (String) request.getParameter("moisturizeSkip") ;    
	        treatskip = (String) request.getParameter("treatSkip") ;    
	        etreatskip = (String) request.getParameter("etreatSkip") ;    
		    
	        scrname = (String) request.getParameter("scrname") ;
		try {
           // dbo.createConnection();
    	//	recEng.setDBO(dbo);

			 CustomerTrackBean ctb = new CustomerTrackBean() ;
			 ctb.insertCustomerTrack("Skin",customerId,request.getParameter("scrname"),"Product Recommendation","PROD","") ;


	        master = (Hashtable) rec.getMasterDataOne() ;
			prodrec = true ;
			if (prodrec) {
				
				    prodlist = new ArrayList() ;
				    
				//	cp = (CustomerProfileVO) rec.getCustomerProfile(customerId,cp,"skin") ;
					prodlist = (ArrayList) recEng.getProduct(customerId, prodType,"",prodLine, cp,"");
				//recEng.manageCustomerRegimen(customerId,repId,prodLine,product) ;
			}
        //    System.out.println("prodlist" +prodlist) ;
            System.out.println("customer ID" +customerId) ;
            
			if (prodlist != null && prodlist.size() != 0) {
	            System.out.println("prodlist size " +prodlist.size()) ;
	            prodlist = (ArrayList) recEng.getCustomerReviewStars(prodlist) ;
			}
			if (prodlist != null && prodlist.size() != 0) {
				
			    request.setAttribute("productlist", prodlist);
	            request.setAttribute("customerprofile",cp) ;
	            
	            request.setAttribute("cleanseprod",cleanseprod) ;
	            request.setAttribute("moisturizeprod",moisturizeprod) ;
	            request.setAttribute("treatprod",treatprod) ;
	            request.setAttribute("etreatprod",etreatprod) ;
	            
	            request.setAttribute("cleanseskip",cleanseskip) ;
	            request.setAttribute("moisturizeskip",moisturizeskip) ;
	            request.setAttribute("treatskip",treatskip) ;
	            request.setAttribute("etreatskip",etreatskip) ;
	            
	            request.setAttribute("scrname",scrname) ;
	            
				target="svbeautynobrand";
				
			}
			else
				target = "failure" ;

			request.setAttribute("customerId", customerId);
			request.setAttribute("mid", mid);
			request.setAttribute("catid", catid);
			request.setAttribute("mode", mode);
			request.setAttribute("masterData", master);
//		    session.setAttribute("sessionProductList",prodlist) ;
            request.setAttribute("prodline",prodLine) ;
            
            for (int i=0; i < prodlist.size() ; i++)
            {
            	cleanse = (ArrayList) prodlist.get(0) ;
            	moisturize =    (ArrayList) prodlist.get(3) ;           
            	treat =    (ArrayList) prodlist.get(5) ;           
            	etreat =    (ArrayList) prodlist.get(6) ;           
            	
                
            }
            
            for (int i=0; i < cleanse.size() ; i++)
            {	
        	  ProductVO prod = (ProductVO) cleanse.get(i) ;
        	  String prodId = (String) prod.getProdId() ;
        //	  System.out.println(prodId) ;
        	  prodidlist.add(prodId) ;
            }
            for (int i=0; i < moisturize.size() ; i++)
            {	
        	  ProductVO prod = (ProductVO) moisturize.get(i) ;
        	  String prodId = (String) prod.getProdId() ;
       // 	  System.out.println(prodId) ;
        	  prodidlist.add(prodId) ;
            }
            for (int i=0; i < treat.size() ; i++)
            {	
        	  ProductVO prod = (ProductVO) treat.get(i) ;
        	  String prodId = (String) prod.getProdId() ;
       // 	  System.out.println(prodId) ;
        	  prodidlist.add(prodId) ;
            }
            for (int i=0; i < etreat.size() ; i++)
            {	
        	  ProductVO prod = (ProductVO) etreat.get(i) ;
        	  String prodId = (String) prod.getProdId() ;
      //  	  System.out.println(prodId) ;
        	  prodidlist.add(prodId) ;
            }
            
  
		

	

        AnalysisDetailAction hda = new AnalysisDetailAction() ;
        ArrayList retuList = (ArrayList) hda.getCustomerProductFeatures(customerId, prodidlist) ;
       // log.debug("hda"+retuList.get(0));
       // log.debug("profile"+retuList.get(1));
       // log.debug("ratings"+retuList.get(2));
         request.setAttribute("retuList", retuList) ;
    //    System.out.println("bbbbbb"+retuList);



		} catch(Exception e) {
			e.printStackTrace() ;
		}
		finally {
			//dbo.closeConnection() ;
		}
		return (mapping.findForward(target));
	}

}