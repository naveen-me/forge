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
import com.svb.ratings.javabeans.ProductRecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngine ;
import com.svb.ratings.javabeans.RecommendationEngineDebug;
import com.svb.regimenmail.javabeans.RegimenMailDAL;
import com.svb.analysis.actions.*;;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RegimenDetailAction extends DispatchAction {

	private static Log log = LogFactory.getLog(RegimenDetailAction.class);
    private boolean spfflag ;
    private String spfexists;
    
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
		String scrname = "" ;
		String scrname2 = "" ;
		String prodLineFlag = "" ;
		String prodTypeFlag = "" ;
		String prodtypestring = "" ;
        String bnamestring = "" ;
        
		String mailmsg = "" ;
		String cusid = "" ;
		boolean prodrec = false ;
		String printPage = "";
		spfexists = "no" ;
		String spfprodexists = "" ;
	    String spfprod = "" ;
	    String cleanseprod = "" ;
	    String moisturizeprod = "" ;
	    String treatprod = "" ;
	    String etreatprod = "" ;
        String cleanseskip = "" ;
        String moisturizeskip = "" ;
        String treatskip = "" ;
        String etreatskip = "" ;
        spfexists = "" ;
        String prodstring = "" ;
        String singlebrand = "no" ;
        ArrayList categprods = new ArrayList()  ;
        ProductVO spf[] = new ProductVO[0] ;
        ArrayList dynamic = new ArrayList() ;
        
	    ArrayList cleanse = new ArrayList() ;
	    ArrayList moisturize = new ArrayList() ;
	    ArrayList treat = new ArrayList() ;
	    ArrayList etreat = new ArrayList() ;
	    ArrayList newspf  = new ArrayList() ;
	    ArrayList prodidlist = new ArrayList() ;
	    ArrayList bnames = new ArrayList() ;
	    
        spfflag = false ;
        ArrayList spflist = new ArrayList() ;
        
		ProductVO product[] = new ProductVO[0];
		CustomerProfileVO cp = new CustomerProfileVO() ;
		
		ProductRegimenForm regimenForm = (ProductRegimenForm)form;
		
		ArrayList prodlist = new ArrayList();
		ArrayList newprodlist = new ArrayList();
		ArrayList newprodlist2 = new ArrayList() ;
		ArrayList nptypes = new ArrayList() ;
		
        ArrayList sessionProds = new ArrayList() ;
		Hashtable prodhash = new Hashtable() ;
        Hashtable master = new Hashtable() ;
        Hashtable brands = new Hashtable() ;
        
        boolean cleansebrandflag = false ;
        boolean moisturizebrandflag = false ;
        boolean treatbrandflag = false ;
        boolean etreatbrandflag = false ;
        
		customerId = regimenForm.getCustomerId();
		prodType   = regimenForm.getProdType();
		prodLine   = regimenForm.getProdLine();
		HttpSession session = request.getSession(false);
		
		cusid = regimenForm.getCusid();
	    //System.out.println("I am in regimen VB 2s...... "+cusid) ;
	    
        RecommendationEngine rec = new RecommendationEngine() ;
        RecommendationEngineDebug recEngDebug = new RecommendationEngineDebug() ;
        ProductRecommendationEngine recEng = new ProductRecommendationEngine() ;
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
	    if ( request.getParameter("singlebrandflag") !=null )
		    	singlebrand = request.getParameter("singlebrandflag");
		
		
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
      
//        if (request.getParameterValues("spf") != null)
//        	spf = request.getParameterValues("spf") ;
        
//        if (request.getParameter("spfexists") != null)
//        	spfexists = request.getParameter("spfexists") ;
        
        if (request.getParameter("spfprod") != null)
        {
        	spfprod = request.getParameter("spfprod") ;
        	categprods.add(spfprod) ;
        	spfprodexists = "yes" ;
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
        if (request.getParameter("scrname") != null)
        {
        	scrname = request.getParameter("scrname") ;
        }
        if (request.getParameter("scrname2") != null)
        {
        	scrname2 = request.getParameter("scrname2") ;
        }
        if (request.getParameter("bnamestring") != null)
        {
        	bnamestring = request.getParameter("bnamestring") ;
        }
        
	    System.out.println("scrname22222222 "+scrname2);
//		if (scrname.equals("skinanalysis"))
//		{
//			sessionProds = (ArrayList) session.getAttribute("sessionProductList") ;
//			prodType = "" ;
//			ArrayList nprlist = (ArrayList) recEng.getProduct(customerId, "'0009'","","", cp,"prodType");
//			newspf  = (ArrayList) nprlist.get(7) ;           
//            cleanse = (ArrayList) prodlist.get(0) ;
//            moisturize =    (ArrayList) prodlist.get(3) ;           
//            treat =    (ArrayList) prodlist.get(5) ;           
//            etreat =    (ArrayList) prodlist.get(6) ;           
//		}
		
//		cp = (CustomerProfileVO) rec.getCustomerProfile(customerId,cp,"skin") ;
		//System.out.println("vb spf ....3 "+newspf.size());
              
        prodstring = "(" ;
        
/*        for (int i=0 ; i < categprods.size(); i++)
        {
        	if (i == categprods.size()-1)
        	  prodstring = prodstring+"'"+(String)categprods.get(i)+"'"+")" ;
        	else
          	  prodstring = prodstring+"'"+(String)categprods.get(i)+"'"+"," ;
        }
*/
        cleanseskip = (String) request.getParameter("cleanseSkip") ;    
        moisturizeskip = (String) request.getParameter("moisturizeSkip") ;    
        treatskip = (String) request.getParameter("treatSkip") ;    
        etreatskip = (String) request.getParameter("etreatSkip") ;    
        
        
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
						 ctb.insertCustomerTrack("Skin",customerId,"Product Recommendation","Brand Regimen",prodLine,"") ;

					    prodlist = new ArrayList() ;
					    prodlist = (ArrayList) recEng.getProduct(customerId, "","",prodLine, cp,"prodLine");
					    
//					    prodlist = (ArrayList) recEng.getProduct(customerId, prodType,"","", cp,"");
					    categprods = new ArrayList() ;  
		                cleanse = (ArrayList) prodlist.get(0) ;
		                moisturize =    (ArrayList) prodlist.get(3) ;           
		                treat =    (ArrayList) prodlist.get(5) ;           
		                etreat =    (ArrayList) prodlist.get(6) ;           
//		                newspf  = (ArrayList) prodlist.get(7) ;
		                
		                
		                
//		                cleanse = (ArrayList) this.getProdArrayList(cleanse,prodLine) ;
	//	                moisturize = (ArrayList) this.getProdArrayList(moisturize,prodLine) ;
		//                treat = (ArrayList) this.getProdArrayList(treat,prodLine) ;
		  //              etreat = (ArrayList) this.getProdArrayList(etreat,prodLine) ;
		
		                System.out.println("prodLine    "+prodLine) ;
		                System.out.println("cleanse size   "+cleanse.size()) ;
		                System.out.println("moisturze size   "+moisturize.size()) ;
		                System.out.println("treat size   "+treat.size()) ;
		                System.out.println("etreat size   "+etreat.size()) ;
		                
		                if (cleanse.size() > 0)
		                	cleansebrandflag = true;
		                else
		                {
		                	nptypes.add("0001") ;
		                }
		                if (moisturize.size() > 0)
		                	moisturizebrandflag = true ;
		                else
		                {
		                	nptypes.add("0003") ;
		                }
		                if (treat.size() > 0)
		                	treatbrandflag = true ;
		                else
		                {
		                	nptypes.add("0005") ;
		                }
		                if (etreat.size() > 0)
		                	etreatbrandflag = true ;
		                else
		                {
		                	nptypes.add("0004") ;
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
		                
		                
		                
		                if (cleansebrandflag)
		                	newprodlist.add(cleanse) ;
		                else
		                {
		                	newprodlist.add(newprodlist2.get(0)) ;
		                }
		                newprodlist.add(prodlist.get(1)) ;
	                	newprodlist.add(prodlist.get(2)) ;
		                if (moisturizebrandflag)
		                	newprodlist.add(moisturize) ;
		                else
		                {
		                	newprodlist.add(newprodlist2.get(3)) ;
		                }
		                newprodlist.add(prodlist.get(4)) ;
		                if (treatbrandflag)
		                	newprodlist.add(treat) ;
		                else
		                {
		                	newprodlist.add(newprodlist2.get(5)) ;
		                }
		                if (etreatbrandflag)
		                	newprodlist.add(etreat) ;
		                else
		                {
		                	newprodlist.add(newprodlist2.get(6)) ;
		                }
		                newprodlist.add(prodlist.get(7)) ;
		                newprodlist.add(prodlist.get(8)) ;
		               
		                prodlist = newprodlist ;
		                cleanse = (ArrayList) prodlist.get(0) ;
		                moisturize =    (ArrayList) prodlist.get(3) ;           
		                treat =    (ArrayList) prodlist.get(5) ;           
		                etreat =    (ArrayList) prodlist.get(6) ;           
		                
                         if (cleanse.size() >0)
                         {
                        	if (singlebrand.equals("yes"))
                        	{
                        		cleanseprod = request.getParameter("cleanseprod") ;
                        		categprods.add(cleanseprod) ;
                        	}
                        	else
                        	{
                        	    cleanseprod = (String)((ProductVO) cleanse.get(0)).getProdId() ;
                        	
		                	
        				        for (int j=0; j < cleanse.size(); j++)
                        	    {
                        		   String prnewid = (String)((ProductVO) cleanse.get(j)).getProdId() ;
        				           categprods.add(prnewid) ;	
                        		
                        	    }
                        	}
//					        categprods.add(cleanseprod) ;

					        /*					        String prodspf = (String)((ProductVO) cleanse.get(0)).getSPFId() ;
		                    if ( !prodspf.equals("U001")) 
		                    {
		                      spfflag = true ;
		                      spfexists = "yes" ;
		                    }
		                    if (spfflag)
		                       spflist.add((String)((ProductVO) cleanse.get(0)).getProdName()) ;
		                    spfflag = false ;
*/		                    
                        }
                        if (moisturize.size() >0)
                        {
                        	if (singlebrand.equals("yes"))
                        	{
                        		moisturizeprod = request.getParameter("moisturizeprod") ;
                        		categprods.add(moisturizeprod) ;
                        	}
                        	else
                        	{
                        	    moisturizeprod = (String)((ProductVO) moisturize.get(0)).getProdId() ;
                        	

                        	    for (int j=0; j < moisturize.size(); j++)
                        	    {
                        		   String prnewid = (String)((ProductVO) moisturize.get(j)).getProdId() ;
        				           categprods.add(prnewid) ;	
                        		
                        	    }
                        	}
//					        categprods.add(moisturizeprod) ;
/*					        String prodspf = (String)((ProductVO) moisturize.get(0)).getSPFId() ;
		                    if ( !prodspf.equals("U001")) 
		                    {
		                      spfflag = true ;
		                      spfexists = "yes" ;
		                    }
		                    if (spfflag)
		                       spflist.add((String)((ProductVO) moisturize.get(0)).getProdName()) ;
		                    spfflag = false ;
*/		                    
                        }
                        if (treat.size() >0)
                        {
                        	if (singlebrand.equals("yes"))
                        	{
                        		treatprod = request.getParameter("treatprod") ;
                        		categprods.add(treatprod) ;
                        	}
                        	else
                        	{
                        	    treatprod = (String)((ProductVO) treat.get(0)).getProdId() ;
                        	
                        	
                        	    for (int j=0; j < treat.size(); j++)
                        	    {
                        		  String prnewid = (String)((ProductVO) treat.get(j)).getProdId() ;
        				          categprods.add(prnewid) ;	
                        		
                        	    }
                        	}
//					        categprods.add(treatprod) ;
/*					        String prodspf = (String)((ProductVO) treat.get(0)).getSPFId() ;
		                    if ( !prodspf.equals("U001")) 
		                    {
		                      spfflag = true ;
		                      spfexists = "yes" ;
		                    }
		                    if (spfflag)
		                       spflist.add((String)((ProductVO) treat.get(0)).getProdName()) ;
		                    spfflag = false ;
*/		                    
                        }
                        if (etreat.size() >0)
                        {
                        	if (singlebrand.equals("yes"))
                        	{
                        		etreatprod = request.getParameter("etreatprod") ;
                        		categprods.add(etreatprod) ;
                        	}
                        	else
                        	{
                        	    etreatprod = (String)((ProductVO) etreat.get(0)).getProdId() ;
                        	
                        	    for (int j=0; j < etreat.size(); j++)
                        	    {
                        		   String prnewid = (String)((ProductVO) etreat.get(j)).getProdId() ;
        				           categprods.add(prnewid) ;	
                        		
                           	    }
                        	}
//					        categprods.add(etreatprod) ;
/*					        String prodspf = (String)((ProductVO) etreat.get(0)).getSPFId() ;
		                    if ( !prodspf.equals("U001")) 
		                    {
		                      spfflag = true ;
		                      spfexists = "yes" ;
		                    }
		                    if (spfflag)
		                       spflist.add((String)((ProductVO) etreat.get(0)).getProdName()) ;
		                    spfflag = false ;
*/		                    
                        }
/*		                if (spflist.size() == 0)
		                {
		                  if (newspf.size() > 0)
		                  for (int i=0; i < newspf.size() ; i++)
		                  { 
					        spfprod = (String)((ProductVO) newspf.get(i)).getProdId() ;
		                    break ;
		                  }
		                }
*/
			            prodlist = (ArrayList) recEng.getCustomerReviewStars(prodlist) ;
                        target="svbeautybrand"; 
				    }
					if (scrname.equals("skinanalysis"))
					{
						if (prodLineFlag == null || !prodLineFlag.equals("yes"))
					    {
						 String allskip = cleanseskip+"::"+moisturizeskip+"::"+treatskip+"::"+etreatskip ;
						 CustomerTrackBean ctb = new CustomerTrackBean() ;
						 ctb.insertCustomerTrack("Skin",customerId,"Product Recommendation","Create Regimen",allskip,"") ;
					    }
//						sessionProds = (ArrayList) session.getAttribute("sessionProductList") ;
						System.out.println("i am in skin analysis "+prodLine);
						prodType = "" ;
						ArrayList nprlist = new ArrayList();
						if (prodLineFlag != null && prodLineFlag.equals("yes"))
			               nprlist = (ArrayList) recEng.getProduct(customerId, "'0009'","",prodLine, cp,"prodLine");
						else
						   nprlist = (ArrayList) recEng.getProduct(customerId, "'0009'","","", cp,"prodType");
						if (nprlist.size() > 6)
						   newspf = (ArrayList) nprlist.get(7) ;           
						if (newspf.size() > 0)
						{
							spfprod = (String)( (ProductVO)newspf.get(0)).getProdId() ;
							categprods.add(spfprod) ;
						}
						else 
						{
							nprlist = (ArrayList) recEng.getProduct(customerId, "'0009'","","", cp,"prodType");
							if (nprlist.size() > 6)
								   newspf = (ArrayList) nprlist.get(7) ;           
							if (newspf.size() > 0)
							{
							  	   spfprod = (String)( (ProductVO)newspf.get(0)).getProdId() ;
								   categprods.add(spfprod) ;
							}
						}
					}
					else if (spfprodexists.equals("yes"))
					{
						categprods.add(spfprod) ;
						spfexists = "no" ;
					}
				    System.out.println("vb spfprod "+spfprod+"  "+spfprodexists) ;
				    System.out.println("categprod size "+categprods.size()) ;
				    
				    

				    
				    
	                for (int i=0 ; i < categprods.size(); i++)
	                {
	                	if (i == categprods.size()-1)
	                	  prodstring = prodstring+"'"+(String)categprods.get(i)+"'"+")" ;
	                	else
	                  	  prodstring = prodstring+"'"+(String)categprods.get(i)+"'"+"," ;
	                }
	                System.out.println("product string "+prodstring) ;
				    prodhash = (Hashtable) rec.getBeautyProductInfo(prodstring) ;
				    
				    prodhash  = recEngDebug.getProductsIngredients("", "prodId", "",prodhash,prodstring, false); //Single changes Struts
				    prodhash  = (Hashtable) rec.getCustomerReviewStars2(prodhash) ;
					
                    ArrayList ptypes = new ArrayList() ;
			        if (cleanseskip != null && cleanseskip.length() > 0 && cleanseskip.equals("cleanseSkip"))
			        {
			        	prodType = "0001" ;
			        	prodTypeFlag = "yes" ;
			        	ptypes.add(prodType) ;
			        }
			        if (moisturizeskip != null && moisturizeskip.length() > 0 && moisturizeskip.equals("moisturizeSkip"))
			        {
			        	prodType = "0003" ;
			        	prodTypeFlag = "yes" ;
			        	ptypes.add(prodType) ;
			        }
			        if (treatskip != null && treatskip.length() > 0 && treatskip.equals("treatSkip"))
			        {
			        	prodType = "0005" ;
			        	prodTypeFlag = "yes" ;
			        	ptypes.add(prodType) ;
			        }
			        if (etreatskip != null && etreatskip.length() > 0 && etreatskip.equals("etreatSkip"))
			        {
			        	prodType = "0004" ;
			        	prodTypeFlag = "yes" ;
			        	ptypes.add(prodType) ;
			        }
					if (prodTypeFlag != null && prodTypeFlag.equals("yes"))
					{
						 CustomerTrackBean ctb = new CustomerTrackBean() ;
						 ctb.insertCustomerTrack("Skin",customerId,"Product Recommendation","Product Type Recommendation",prodType,"") ;
						
						System.out.println("I am here "+prodType) ;
	                    prodtypestring = "(" ;
			        	ptypes.add(prodType) ;
			        	//ptypes.add("0009") ;
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
					//	newspf  = (ArrayList) prodlist.get(7) ;           
					}
					if (!spfprodexists.equals("yes"))
					{
					   prodhash = (Hashtable) checkSPF(categprods,newspf,prodhash,master) ;
					   spfprod = (String) prodhash.get("spfprodid") ;
					   spf = (ProductVO[])prodhash.get("spf") ;
					   System.out.println("spf ,,,,") ;
					}
		            dynamic = (ArrayList) recEng.getDynamicText(prodhash,cp,master) ;
		      //      if (dynamic != null && dynamic.size() > 0)
		              //System.out.println("finally dynamic "+dynamic.size()) ;
				//recEng.manageCustomerRegimen(customerId,repId,prodLine,product) ;
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
                request.setAttribute("cleanseprod",cleanseprod) ;
                request.setAttribute("moisturizeprod",moisturizeprod) ;
                request.setAttribute("treatprod",treatprod) ;
                request.setAttribute("etreatprod",etreatprod) ;
                request.setAttribute("spfprod",spfprod) ;
                request.setAttribute("spfexists",spfexists) ;
                request.setAttribute("spf",spf) ;
                request.setAttribute("dynamic",dynamic) ;
                request.setAttribute("scrname2",scrname2) ;
                request.setAttribute("bnamestring",bnamestring) ;
                
                request.setAttribute("cleansebrandflag",""+cleansebrandflag) ;
                request.setAttribute("moisturizebrandflag",""+moisturizebrandflag) ;
                request.setAttribute("treatbrandflag",""+treatbrandflag) ;
                request.setAttribute("etreatbrandflag",""+etreatbrandflag) ;

                
	            request.setAttribute("cleanseskip",cleanseskip) ;
	            request.setAttribute("moisturizeskip",moisturizeskip) ;
	            request.setAttribute("treatskip",treatskip) ;
	            request.setAttribute("etreatskip",etreatskip) ;

				request.setAttribute("catid", catid);
				request.setAttribute("mid", mid);
				request.setAttribute("mode", mode);

	           

	            if (request.getAttribute("mailsent") != null)
	            	request.setAttribute("mailsent",request.getAttribute("mailsent")) ;

	            request.setAttribute("cusid",cusid);
	            
	        /*    for (int i=0; i < prodlist.size() ; i++)
	            {
	            	cleanse = (ArrayList) prodlist.get(0) ;
	            	moisturize =    (ArrayList) prodlist.get(3) ;           
	            	treat =    (ArrayList) prodlist.get(5) ;           
	            	etreat =    (ArrayList) prodlist.get(6) ;           
	            	
	                
	            }
	         */   
	            for (int i=0; i < cleanse.size() ; i++)
	            {	
            	  ProductVO prod = (ProductVO) cleanse.get(i) ;
            	  String prodId = (String) prod.getProdId() ;
            	  prodidlist.add(prodId) ;
	            }
	            for (int i=0; i < moisturize.size() ; i++)
	            {	
            	  ProductVO prod = (ProductVO) moisturize.get(i) ;
            	  String prodId = (String) prod.getProdId() ;
            	  prodidlist.add(prodId) ;
	            }
	            for (int i=0; i < treat.size() ; i++)
	            {	
            	  ProductVO prod = (ProductVO) treat.get(i) ;
            	  String prodId = (String) prod.getProdId() ;
            	  prodidlist.add(prodId) ;
	            }
	            for (int i=0; i < etreat.size() ; i++)
	            {	
            	  ProductVO prod = (ProductVO) etreat.get(i) ;
            	  String prodId = (String) prod.getProdId() ;
            	  prodidlist.add(prodId) ;
	            }
	            
	  
			

		

            AnalysisDetailAction hda = new AnalysisDetailAction() ;
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
	private ArrayList getProdArrayList(ArrayList prodlist, String prodline)
	{
		System.out.println("I am here in getProdArrayList "+prodlist.size()+"   prodline "+prodline) ;
		ArrayList newList = new ArrayList() ;
        if (prodlist.size() >0)
        {
        	
        	for (int i=0 ; i < prodlist.size() ; i++)
        	{
        		System.out.println("iiiiiiiiiii "+i) ;
        		String npLine = (String)((ProductVO) prodlist.get(i)).getProductLine() ;
        		System.out.println("npline  "+npLine+"  pline "+prodline) ;
        		if (npLine.equals(prodline))
        		{
        			newList.add(prodlist.get(i)) ;
            		break ;
        		}
        	}
        }
        System.out.println("new List size "+newList.size()) ;
        if (newList.size() > 0)
        {
        	    
        		System.out.println("I mammmm here vbbbbbbb "+newList.size()) ;
        }
        else
        {
        		newList = prodlist ;		
        }
       
		return newList ;
	}
    public Hashtable checkSPF(ArrayList productlist, ArrayList spflist, Hashtable prodhash, Hashtable master)
    {
    	spfexists = "no" ;
    	String pmtype = "" ;
        ArrayList spfnamelist = new ArrayList() ;
    	for (int i=0; i < productlist.size() ; i++)
    	{
    		String prodid = (String)productlist.get(i) ;
            String prodspf = "" ;
            spfflag = false ;
            ProductVO prod = new ProductVO() ;
            //System.out.println("vb spf prods "+prodid) ;
    		if (prodhash.get(prodid.trim()) != null)
    		{
    			prod = (ProductVO) prodhash.get(prodid.trim()) ;
    			prodspf = (String) prod.getSPFId() ;
    			pmtype = (String) prod.getMasterProdType() ;
                //System.out.println("vb spf prods 33 "+prodspf) ;
    		}
            if ( prodspf != null && prodspf.trim().length() > 0 && !prodspf.equals("U001") && pmtype.equals("0003")) 
            {
              //System.out.println("vb spf prods 44 "+prodspf) ;
              spfflag = true ;
              spfexists = "yes" ;
            }
            if (spfflag)
            {
//              spfnamelist.add((String)prod.getProdName()) ;
                spfnamelist.add(prod) ;
            }
    	}
        //System.out.println("vb spf exists "+spfexists) ;
    	if (spfexists.equals("yes"))
    	{
//    		String[] spf = new String[spfnamelist.size()] ;
    		ProductVO[] spf = new ProductVO[spfnamelist.size()] ;
    		for (int j=0; j < spfnamelist.size() ; j++)
    		{
//    			if (spfnamelist.get(j) != null &&((String) spfnamelist.get(j)).length() > 0)
    			if (spfnamelist.get(j) != null )
    			   spf[j] = (ProductVO) spfnamelist.get(j) ;
    		}
    		prodhash.put("spf",spf) ;
            System.out.println("vb spf length "+spf.length) ;
    	}
    	else
    	{
      	  String spfprod = ""  ;
          //System.out.println("vb spf list size "+spflist.size()) ;
          if (spflist.size() > 0)
            for (int i=0; i < spflist.size() ; i++)
            { 
                //System.out.println("vb spf list size 222 ") ;
              ProductVO spfpr = (ProductVO) spflist.get(i) ;
		      spfprod = (String)((ProductVO) spfpr).getProdId() ;
		      spfpr.setProductLinePrime((String)master.get(spfpr.getProductLine())) ;
		      prodhash.put(spfprod,spfpr) ;
		      prodhash.put("spfprodid",spfprod) ;
              //System.out.println("vb spf list size 222 "+spfprod) ;
              break ;
            }
    		
    	}
    	return prodhash ;
    }
}