/*
 * Created on Mar 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.ratings.actions;

import java.io.IOException;
import java.util.ArrayList;
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


import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.javabean.CustomerTrackBean;
import com.svb.common.valueobjects.* ;
import com.svb.ratings.formbeans.ProductRegimenForm ;
import com.svb.ratings.javabeans.HairProductRecommendationEngine;
import com.svb.ratings.javabeans.HairRecommendationEngine ;

import com.svb.analysis.actions.HairAnalysisDetailAction ;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class HairRegimenAction extends DispatchAction {

	private static Log log = LogFactory.getLog(HairRegimenAction.class);

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
        
        ArrayList prodidlist = new ArrayList() ;
        
        HairRecommendationEngine rec = new HairRecommendationEngine() ;
        HairProductRecommendationEngine recEng = new HairProductRecommendationEngine() ;
		ConstantDeclarations cdl = new ConstantDeclarations() ;
        SVDBOperation dbo = new SVDBOperation();

        String shampooprod = "" ;
        String conditionerprod = "" ;
        String htreatprod = "" ;
        String stylingprod = "" ;
        String shampooskip = "" ;
        String conditionerskip = "" ;
        String htreatskip = "" ;
        String stylingskip = "" ;
        String scrname = "" ;
        
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
	        
	        shampooprod = (String) request.getParameter("shampooprod") ;    
	        conditionerprod = (String) request.getParameter("conditionerprod") ;    
	        htreatprod = (String) request.getParameter("htreatprod") ;    
	        stylingprod = (String) request.getParameter("stylingprod") ;
	        
	        shampooskip = (String) request.getParameter("shampooSkip") ;    
	        conditionerskip = (String) request.getParameter("conditionerSkip") ;    
	        htreatskip = (String) request.getParameter("htreatSkip") ;    
	        stylingskip = (String) request.getParameter("stylingSkip") ;    
		    
	        scrname = (String) request.getParameter("scrname") ;
		try {
//        dbo.createConnection();
//		recEng.setDBO(dbo);

			 CustomerTrackBean ctb = new CustomerTrackBean() ;
			 ctb.insertCustomerTrack("Hair",customerId,request.getParameter("scrname"),"Product Recommendation","PROD","") ;


	        master = (Hashtable) rec.getMasterDataOne() ;
			prodrec = true ;
			if (prodrec) {
				
				    prodlist = new ArrayList() ;
				    
				//	cp = (CustomerProfileVO) rec.getCustomerProfile(customerId,cp,"skin") ;
					prodlist = (ArrayList) recEng.getProduct(customerId, prodType,"",prodLine, cp,"");
		            prodlist = (ArrayList) recEng.getCustomerReviewStars(prodlist) ;
				//recEng.manageCustomerRegimen(customerId,repId,prodLine,product) ;
			}
        //    System.out.println("prodlist" +prodlist) ;
            System.out.println("customer ID" +customerId) ;
            
			if (prodlist != null && prodlist.size() != 0) {
	            System.out.println("prodlist size " +prodlist.size()) ;
			}
			if (prodlist != null && prodlist.size() != 0) {
				
			    request.setAttribute("productlist", prodlist);
	            request.setAttribute("customerprofile",cp) ;
	            
	            request.setAttribute("shampooprod",shampooprod) ;
	            request.setAttribute("conditionerprod",conditionerprod) ;
	            request.setAttribute("htreatprod",htreatprod) ;
	            request.setAttribute("stylingprod",stylingprod) ;
	            
	            request.setAttribute("shampooskip",shampooskip) ;
	            request.setAttribute("conditionerskip",conditionerskip) ;
	            request.setAttribute("htreatskip",htreatskip) ;
	            request.setAttribute("stylingskip",stylingskip) ;
	            
	            request.setAttribute("scrname",scrname) ;
	            
	            ArrayList shampoo = new ArrayList() ;
            	ArrayList conditioner = new ArrayList() ;           
            	ArrayList htreat =  new ArrayList() ;          
            	ArrayList styling =   new ArrayList() ;          
            	
	            for (int i=0; i < prodlist.size() ; i++)
	            {
	            	shampoo = (ArrayList) prodlist.get(0) ;
	            	conditioner =    (ArrayList) prodlist.get(1) ;           
	            	htreat =    (ArrayList) prodlist.get(2) ;           
	            	styling =    (ArrayList) prodlist.get(3) ;           
	            	
	                
	            }
	            
	            for (int i=0; i < shampoo.size() ; i++)
	            {	
            	  ProductVO prod = (ProductVO) shampoo.get(i) ;
            	  String prodId = (String) prod.getProdId() ;
            	  prodidlist.add(prodId) ;
	            }
	            for (int i=0; i < conditioner.size() ; i++)
	            {	
            	  ProductVO prod = (ProductVO) conditioner.get(i) ;
            	  String prodId = (String) prod.getProdId() ;
            	  prodidlist.add(prodId) ;
	            }
	            for (int i=0; i < htreat.size() ; i++)
	            {	
            	  ProductVO prod = (ProductVO) htreat.get(i) ;
            	  String prodId = (String) prod.getProdId() ;
            	  prodidlist.add(prodId) ;
	            }
	            for (int i=0; i < styling.size() ; i++)
	            {	
            	  ProductVO prod = (ProductVO) styling.get(i) ;
            	  String prodId = (String) prod.getProdId() ;
            	  prodidlist.add(prodId) ;
	            }
	            
	            if (prodLine != null && !prodLine.equals("null") && prodLine.length() > 0) 
	            {
					target="svbeautynobrand";
	            }
	            else
	            {
				    target="svbeautynobrand";
	            }
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

            HairAnalysisDetailAction hda = new HairAnalysisDetailAction() ;
            ArrayList retuList = (ArrayList) hda.getCustomerProductFeatures(customerId, prodidlist) ;
            request.setAttribute("retuList", retuList) ;
          //  System.out.println("bbbbbb"+retuList);
		} 
		catch(NullPointerException e){e.printStackTrace();}
		catch(Exception e) {
			e.printStackTrace() ;
		}
		finally {
//			dbo.closeConnection() ;
		}
		return (mapping.findForward(target));
	}

}