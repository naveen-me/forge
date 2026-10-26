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


import com.svb.analysis.actions.HairAnalysisDetailAction;
import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.javabean.CustomerTrackBean;
import com.svb.common.valueobjects.* ;
import com.svb.ratings.dal.ProductRegimenDAL;
import com.svb.ratings.formbeans.ProductRegimenForm ;
import com.svb.ratings.javabeans.HairProductRecommendationEngine;
import com.svb.ratings.javabeans.HairRecommendationEngine ;
import com.svb.ratings.javabeans.RecommendationEngineDebug;
import com.svb.regimenmail.javabeans.RegimenMailDAL;
import com.svb.documents.pdf.HairRegimenDetailsBean;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class HairRegimenDetailAction extends DispatchAction {

	private static Log log = LogFactory.getLog(HairRegimenDetailAction.class);
    private boolean finishflag ;
    private String finishexists;
    
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String target = "success";
		String mid = "" ;
		String catid = "" ;
		String mode = "" ;
		String customerId     = "";
		String prodType = "" ;
		String prodLine = "" ;
		String prodLineName = "" ;
		String scrname = "" ;
		String scrname2= "" ;
		String prodLineFlag = "" ;
		String prodTypeFlag = "" ;
		String prodtypestring = "" ;
		String basicstring = "" ;		
		String mailmsg = "" ;
		String cusid = "" ;
		boolean prodrec = false ;
		String printPage = "";
		finishexists = "no" ;
	    String finishprod = "" ;
	    String shampooprod = "" ;
	    String conditionerprod = "" ;
	    String htreatprod = "" ;
	    String stylingprod = "" ;
        String shampooskip = "" ;
        String conditionerskip = "" ;
        String htreatskip = "" ;
        String stylingskip = "" ;
        finishexists = "" ;
        String singlebrand = "no" ;
        String bnamestring = "" ;
        
        String prodstring = "" ;
        ArrayList categprods = new ArrayList()  ;
        ProductVO finish[] = new ProductVO[0] ;
        ArrayList dynamic = new ArrayList() ;
        
	    ArrayList shampoo = new ArrayList() ;
	    ArrayList conditioner = new ArrayList() ;
	    ArrayList htreat = new ArrayList() ;
	    ArrayList styling = new ArrayList() ;
	    ArrayList newfinish  = new ArrayList() ;
	    ArrayList prodidlist = new ArrayList() ;
	    ArrayList bnames = new ArrayList() ;
	    
		ArrayList newprodlist = new ArrayList();
		ArrayList newprodlist2 = new ArrayList() ;
		ArrayList nptypes = new ArrayList() ;
        boolean shampoobrandflag = false ;
        boolean conditionerbrandflag = false ;
        boolean htreatbrandflag = false ;
        boolean stylingbrandflag = false ;

	    
        finishflag = false ;
        ArrayList finishlist = new ArrayList() ;
        
		ProductVO product[] = new ProductVO[0];
		CustomerProfileVO cp = new CustomerProfileVO() ;
		
		ProductRegimenForm regimenForm = (ProductRegimenForm)form;
		
		ArrayList prodlist = new ArrayList();
        ArrayList sessionProds = new ArrayList() ;
		Hashtable prodhash = new Hashtable() ;
        Hashtable master = new Hashtable() ;
        Hashtable brands = new Hashtable() ;
        
		customerId = regimenForm.getCustomerId();
		prodType   = regimenForm.getProdType();
		prodLine   = regimenForm.getProdLine();
		HttpSession session = request.getSession(false);
		
		cusid = regimenForm.getCusid();
	    //System.out.println("I am in regimen VB 2s...... "+cusid) ;
	    
        HairRecommendationEngine rec = new HairRecommendationEngine() ;
        RecommendationEngineDebug recEngDebug = new RecommendationEngineDebug() ;
        HairProductRecommendationEngine recEng = new HairProductRecommendationEngine() ;
		ConstantDeclarations cdl = new ConstantDeclarations() ;
		ProductRegimenDAL prodRegimenDAL = new ProductRegimenDAL();

        SVDBOperation dbo = new SVDBOperation();
        
		if(regimenForm.getPrintPage() != null)
				printPage = regimenForm.getPrintPage();
		
		String bnm[] = new String[0] ;
		if (request.getParameterValues("brandname") !=null)
		{
			bnm = (String[]) request.getParameterValues("brandname") ;
		}
		if (bnm.length > 0)
		{
			for (int i=0; i < bnm.length ; i++)
		       bnames.add(bnm[i]) ;
		}
		request.setAttribute("brandnames",bnames) ;
		
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
        
	    
	    
        if (request.getParameter("prodLineFlag") != null)
        	prodLineFlag = request.getParameter("prodLineFlag") ;
        if (request.getParameter("prodTypeFlag") != null)
        	prodTypeFlag = request.getParameter("prodTypeFlag") ;
        
        if (request.getParameter("catid") != null)
    	    catid = request.getParameter("catid") ;
        if ( request.getParameter("singlebrandflag") !=null )
	    	singlebrand = request.getParameter("singlebrandflag");
        if ( request.getParameter("bnamestring") !=null )
	    	bnamestring = request.getParameter("bnamestring");
        if (request.getParameter("prodLineName") != null)
        	prodLineName = request.getParameter("prodLineName") ;
        
        System.out.println("bname string in action "+bnamestring) ;
        System.out.println("prod Line Flag "+prodLineFlag) ;
	    
//        if (request.getParameterValues("finish") != null)
//        	finish = request.getParameterValues("finish") ;
        
//        if (request.getParameter("finishexists") != null)
//        	finishexists = request.getParameter("finishexists") ;
        
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
        if (request.getParameter("finishprod") != null)
        {
        	finishprod = request.getParameter("finishprod") ;
        	categprods.add(finishprod) ;
        	finishexists = "yes" ;
        }
        if (request.getParameter("scrname") != null)
        {
        	scrname = request.getParameter("scrname") ;
        }
        if (request.getParameter("scrname2") != null)
        {
        	scrname2 = request.getParameter("scrname2") ;
        }
    	System.out.println("finish prod "+finishprod+"  "+scrname) ;
        
		//System.out.println("vb finish ....1 "+scrname);
		
		cp = (CustomerProfileVO) rec.getCustomerProfile(customerId,cp,"hair") ;
		//System.out.println("vb finish ....3 "+newfinish.size());
              
        prodstring = "(" ;
        
/*        for (int i=0 ; i < categprods.size(); i++)
        {
        	if (i == categprods.size()-1)
        	  prodstring = prodstring+"'"+(String)categprods.get(i)+"'"+")" ;
        	else
          	  prodstring = prodstring+"'"+(String)categprods.get(i)+"'"+"," ;
        }
*/
        shampooskip = (String) request.getParameter("shampooSkip") ;    
        conditionerskip = (String) request.getParameter("conditionerSkip") ;    
        htreatskip = (String) request.getParameter("htreatSkip") ;    
        stylingskip = (String) request.getParameter("stylingSkip") ;    
        
        
        customerId = cusid ;
        master = (Hashtable) rec.getMasterDataOne() ;
        //System.out.println("vbgkhdkghdkfhkdk") ;
        brands = (Hashtable) rec.getBrandDetails() ;
        
		try {

			
			prodrec = true ;
			if (prodrec) {
                    
				    if (prodLineFlag != null && prodLineFlag.equals("yes"))
				    {
						 CustomerTrackBean ctb = new CustomerTrackBean() ;
						 ctb.insertCustomerTrack("Hair",customerId,"Product Recommendation","Brand Regimen",prodLine,"") ;
						 
					    prodlist = new ArrayList() ;
					    prodlist = (ArrayList) recEng.getProduct(customerId, "","",prodLine, cp,"prodLine");
					    categprods = new ArrayList() ;  
		                shampoo = (ArrayList) prodlist.get(0) ;
		                conditioner =    (ArrayList) prodlist.get(1) ;           
		                htreat =    (ArrayList) prodlist.get(2) ;           
		                styling =    (ArrayList) prodlist.get(3) ;           
		                newfinish  = (ArrayList) prodlist.get(4) ;
		                
		                
		                if (shampoo.size() > 0)
		                	shampoobrandflag = true;
		                else
		                {
		                	nptypes.add("1001") ;
		                }
		                if (conditioner.size() > 0)
		                	conditionerbrandflag = true ;
		                else
		                {
		                	nptypes.add("1003") ;
		                }
		                if (htreat.size() > 0)
		                	htreatbrandflag = true ;
		                else
		                {
		                	nptypes.add("1005") ;
		                }
		                if (styling.size() > 0)
		                	stylingbrandflag = true ;
		                else
		                {
		                	nptypes.add("1004") ;
		                }
		                
	                    prodtypestring = "(" ;
			        	//ptypes.add("0009") ;
			        	if (nptypes.size() > 0)
			        	{
			        		for (int i=0; i < nptypes.size() ; i++)
			        		{
			                	if (i == nptypes.size()-1)
				                	  prodtypestring = prodtypestring+"'"+(String)nptypes.get(i)+"'"+")" ;
				                	else
				                  	  prodtypestring = prodtypestring+"'"+(String)nptypes.get(i)+"'"+"," ;
			        		}
			        	}
					    newprodlist2 = new ArrayList() ;
						System.out.println("I am here "+prodtypestring) ;
					    newprodlist2 = (ArrayList) recEng.getProduct(customerId, prodtypestring,"","", cp,"prodType");
		                
		                
		                
		                if (shampoobrandflag)
		                	newprodlist.add(shampoo) ;
		                else
		                {
		                	newprodlist.add(newprodlist2.get(0)) ;
		                }
		                if (conditionerbrandflag)
		                	newprodlist.add(conditioner) ;
		                else
		                {
		                	newprodlist.add(newprodlist2.get(1)) ;
		                }
		                if (htreatbrandflag)
		                	newprodlist.add(htreat) ;
		                else
		                {
		                	newprodlist.add(newprodlist2.get(2)) ;
		                }
		                if (stylingbrandflag)
		                	newprodlist.add(styling) ;
		                else
		                {
		                	newprodlist.add(newprodlist2.get(3)) ;
		                }
		                newprodlist.add(prodlist.get(4)) ;
		                newprodlist.add(prodlist.get(5)) ;
		               
		                prodlist = newprodlist ;

		                
		                shampoo = (ArrayList) prodlist.get(0) ;
		                conditioner =    (ArrayList) prodlist.get(1) ;           
		                htreat =    (ArrayList) prodlist.get(2) ;           
		                styling =    (ArrayList) prodlist.get(3) ;           
		                newfinish  = (ArrayList) prodlist.get(4) ;
		                
		                
		                
		                
                        if (shampoo.size() >0)
                        {
                        	if (singlebrand.equals("yes"))
                        	{
                        		shampooprod = request.getParameter("shampooprod") ;
                        		categprods.add(shampooprod) ;
                        	}
                        	else
                        	{
                        		shampooprod = (String)((ProductVO) shampoo.get(0)).getProdId() ;
                        	
                        	    for (int j=0; j < shampoo.size(); j++)
                        	    {
                        		   String prnewid = (String)((ProductVO) shampoo.get(j)).getProdId() ;
        				           categprods.add(prnewid) ;	
                        		
                           	    }
                        	}
//					        categprods.add(shampooprod) ;
                        }
                        if (conditioner.size() >0)
                        {
                        	if (singlebrand.equals("yes"))
                        	{
                        		conditionerprod = request.getParameter("conditionerprod") ;
                        		categprods.add(conditionerprod) ;
                        	}
                        	else
                        	{
                        		conditionerprod = (String)((ProductVO) conditioner.get(0)).getProdId() ;
                        	
                        	    for (int j=0; j < conditioner.size(); j++)
                        	    {
                        		   String prnewid = (String)((ProductVO) conditioner.get(j)).getProdId() ;
        				           categprods.add(prnewid) ;	
                        		
                           	    }
                        	}
//					        categprods.add(conditionerprod) ;
                        }
                        if (htreat.size() >0)
                        {
                        	if (singlebrand.equals("yes"))
                        	{
                        		htreatprod = request.getParameter("htreatprod") ;
                        		categprods.add(htreatprod) ;
                        	}
                        	else
                        	{
                        		htreatprod = (String)((ProductVO) htreat.get(0)).getProdId() ;
                        	
                        	    for (int j=0; j < htreat.size(); j++)
                        	    {
                        		   String prnewid = (String)((ProductVO) htreat.get(j)).getProdId() ;
        				           categprods.add(prnewid) ;	
                        		
                           	    }
                        	}
//					        categprods.add(htreatprod) ;
                        }
                        if (styling.size() >0)
                        {
                        	if (singlebrand.equals("yes"))
                        	{
                        		stylingprod = request.getParameter("stylingprod") ;
                        		categprods.add(stylingprod) ;
                        	}
                        	else
                        	{
                        		htreatprod = (String)((ProductVO) styling.get(0)).getProdId() ;
                        	
                        	    for (int j=0; j < styling.size(); j++)
                        	    {
                        		   String prnewid = (String)((ProductVO) styling.get(j)).getProdId() ;
        				           categprods.add(prnewid) ;	
                        		
                           	    }
                        	}
//					        categprods.add(stylingprod) ;
                        }
		                if (newfinish.size() > 0)
		                { 
					        finishprod = (String)((ProductVO) newfinish.get(0)).getProdId() ;
					        categprods.add(finishprod) ;
					        finishexists = "yes" ;
		                }
		                else
		                {
				              ArrayList nprlist = (ArrayList) recEng.getProduct(customerId, "'1009'","","", cp,"prodType");
							  if (nprlist.size() > 3)
							   newfinish  = (ArrayList) nprlist.get(4) ;           
							  if (newfinish.size() > 0)
							  {
								finishprod = (String)( (ProductVO)newfinish.get(0)).getProdId() ;
								categprods.add(finishprod) ;
							  }
		                }
			            prodlist = (ArrayList) recEng.getCustomerReviewStars(prodlist) ;
		                if (singlebrand != null && singlebrand.equals("yes"))
		                   target="svbeautybrand"; 
		                else
		                   target="svbeautybrand";
				    }
				   
					if (scrname.equals("skinanalysis") )
					{
						if (prodLineFlag == null || !prodLineFlag.equals("yes"))
					    {
						 String allskip = shampooskip+"::"+conditionerskip+"::"+htreatskip+"::"+stylingskip ;
						 CustomerTrackBean ctb = new CustomerTrackBean() ;
						 ctb.insertCustomerTrack("Hair",customerId,"Product Recommendation","Create Regimen",allskip,"") ;
					    }
//						sessionProds = (ArrayList) session.getAttribute("sessionProductList") ;
						System.out.println("i am in skin analysis "+prodLine);
						prodType = "" ;
						if (!finishexists.equals("yes"))
						{
			              ArrayList nprlist = (ArrayList) recEng.getProduct(customerId, "'1009'","","", cp,"prodType");
						  if (nprlist.size() > 3)
						   newfinish  = (ArrayList) nprlist.get(4) ;           
						  if (newfinish.size() > 0)
						  {
							finishprod = (String)( (ProductVO)newfinish.get(0)).getProdId() ;
							categprods.add(finishprod) ;
						  }
						}
					}
					else if (finishexists.equals("yes"))
					{
						categprods.add(finishprod) ;
					}
				    
				    System.out.println("categprods.size()"+categprods.size()) ;
	                for (int i=0 ; i < categprods.size(); i++)
	                {
	                	if (i == categprods.size()-1)
	                	  prodstring = prodstring+"'"+(String)categprods.get(i)+"'"+")" ;
	                	else
	                  	  prodstring = prodstring+"'"+(String)categprods.get(i)+"'"+"," ;
	                }
	                //System.out.println("product string "+prodstring) ;
				    prodhash = (Hashtable) rec.getHairProductInfo(prodstring) ;
				    prodhash  = (Hashtable) rec.getCustomerReviewStars2(prodhash) ;
				    
				//    prodhash  = recEngDebug.getProductsIngredients("", "prodId", "",prodhash,prodstring); //this method
				    
					
                    ArrayList ptypes = new ArrayList() ;
			        if (shampooskip != null && shampooskip.length() > 0 && shampooskip.equals("shampooSkip"))
			        {
			        	prodType = "1001" ;
			        	prodTypeFlag = "yes" ;
			        	ptypes.add(prodType) ;
			        }
			        if (conditionerskip != null && conditionerskip.length() > 0 && conditionerskip.equals("conditionerSkip"))
			        {
			        	prodType = "1003" ;
			        	prodTypeFlag = "yes" ;
			        	ptypes.add(prodType) ;
			        }
			        if (htreatskip != null && htreatskip.length() > 0 && htreatskip.equals("htreatSkip"))
			        {
			        	prodType = "1005" ;
			        	prodTypeFlag = "yes" ;
			        	ptypes.add(prodType) ;
			        }
			        if (stylingskip != null && stylingskip.length() > 0 && stylingskip.equals("stylingSkip"))
			        {
			        	prodType = "1004" ;
			        	prodTypeFlag = "yes" ;
			        	ptypes.add(prodType) ;
			        }
					if (prodTypeFlag != null && prodTypeFlag.equals("yes"))
					{
						CustomerTrackBean ctb = new CustomerTrackBean() ;
						ctb.insertCustomerTrack("Hair",customerId,"Product Recommendation","Product Type Recommendation",prodType,"") ;
						 
						System.out.println("I am here "+prodType) ;
	                    prodtypestring = "(" ;
			        	ptypes.add(prodType) ;
//			        	ptypes.add("1009") ;
			        	if (ptypes.size() > 0)
			        	{
			        		for (int i=0; i < ptypes.size() ; i++)
			        		{
			                	if (i == ptypes.size()-1)
				                	  prodtypestring = prodtypestring+"'"+(String)ptypes.get(i)+"'"+")" ;
				                	else
				                  	  prodtypestring = prodtypestring+"'"+(String)ptypes.get(i)+"'"+"," ;
			        		}
			        	}
					    prodlist = new ArrayList() ;
						System.out.println("I am here "+prodtypestring) ;
					    prodlist = (ArrayList) recEng.getProduct(customerId, prodtypestring,"","", cp,"prodType");
			            prodlist = (ArrayList) recEng.getCustomerReviewStars(prodlist) ;
					   // if (prodlistlist.size() > 3)
					    	//vbh				newfinish  = (ArrayList) prodlist.get(4) ;           
					}
		        //    dynamic = (ArrayList) recEng.getDynamicText(prodhash,cp,master) ;
		      //      if (dynamic != null && dynamic.size() > 0)
		              //System.out.println("finally dynamic "+dynamic.size()) ;
					  System.out.println(" prodlist size at the end "+prodlist.size()) ;
					  
					  HairRegimenDetailsBean hrb = new HairRegimenDetailsBean() ;
					  basicstring = hrb.getCustomerProfileString(cp, master) ;
			}

			    
				request.setAttribute("customerId", customerId);
			    request.setAttribute("productlist", prodlist);
			    request.setAttribute("producthash", prodhash);
			    request.setAttribute("masterData", master);
			    request.setAttribute("brands", brands);
			    //session.setAttribute("product",product) ;
	            request.setAttribute("customerprofile",cp) ;
	            request.setAttribute("prodline",prodLine) ;
	            request.setAttribute("prodlineflag",prodLineFlag) ;
	            request.setAttribute("prodtypeflag",prodTypeFlag) ;
                request.setAttribute("shampooprod",shampooprod) ;
                request.setAttribute("conditionerprod",conditionerprod) ;
                request.setAttribute("htreatprod",htreatprod) ;
                request.setAttribute("stylingprod",stylingprod) ;
                request.setAttribute("finishprod",finishprod) ;
                request.setAttribute("finishexists",finishexists) ;
                request.setAttribute("finish",finish) ;
                request.setAttribute("dynamic",dynamic) ;
                request.setAttribute("basicstring",basicstring) ;
                request.setAttribute("scrname2",scrname2) ;
                request.setAttribute("bnamestring",bnamestring) ;
                
                request.setAttribute("shampoobrandflag",""+shampoobrandflag) ;
                request.setAttribute("conditionerbrandflag",""+conditionerbrandflag) ;
                request.setAttribute("htreatbrandflag",""+htreatbrandflag) ;
                request.setAttribute("stylingbrandflag",""+stylingbrandflag) ;
                
	            request.setAttribute("shampooskip",shampooskip) ;
	            request.setAttribute("conditionerskip",conditionerskip) ;
	            request.setAttribute("htreatskip",htreatskip) ;
	            request.setAttribute("stylingskip",stylingskip) ;

				request.setAttribute("catid", catid);
				request.setAttribute("mid", mid);
				request.setAttribute("mode", mode);

	           

	            if (request.getAttribute("mailsent") != null)
	            	request.setAttribute("mailsent",request.getAttribute("mailsent")) ;

	            request.setAttribute("cusid",cusid);
	            
	            
	         	
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
	            
	  
			

		

            HairAnalysisDetailAction hda = new HairAnalysisDetailAction() ;
            ArrayList retuList = (ArrayList) hda.getCustomerProductFeatures(customerId, prodidlist) ;
            request.setAttribute("retuList", retuList) ;
            System.out.println("bbbbbb"+retuList);
		
		} catch(Exception e) {
			e.printStackTrace() ;
		}
		finally {
//			dbo.closeConnection() ;
		}
		return (mapping.findForward(target));
	}
}