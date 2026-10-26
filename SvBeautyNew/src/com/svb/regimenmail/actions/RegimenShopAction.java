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
import com.svb.ratings.javabeans.ProductRecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngine;
import com.svb.regimenmail.javabeans.*;

/**
 * @author Neelima
 * 
 */
public class RegimenShopAction extends DispatchAction {

	private static Log log = LogFactory.getLog(RegimenShopAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String target = "success";
		
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
        
		ProductVO product[] = new ProductVO[0];
		CustomerProfileVO cp = new CustomerProfileVO() ;
		ApplicationGuidelinesForm applicationGuidelinesForm = (ApplicationGuidelinesForm)form;
		ArrayList prodlist = new ArrayList();
		Hashtable prodhash = new Hashtable() ;
        Hashtable master = new Hashtable() ;
        
        String prodType = "" ;
        String prodLine = "" ;
        String cusid = "" ;
        
        String cleanseCheck = "" ;
        String moisturizeCheck = "" ;
        String treatCheck = "" ;
        String etreatCheck = "" ;
        String spfCheck = "" ;
        
        String cleanseQty = "" ;
        String moisturizeQty = "" ;
        String treatQty = "" ;
        String etreatQty = "" ;
        String spfQty = "" ;
        
        String shopcartstring = "" ;
        String shopcartstring2 = "" ;
        String shopcartsep1 = "~" ;
        String shopcartsep2 = "|" ;

        String cleanseselect = "" ;
        String moisturizeselect = "" ;
        String treatselect = "" ;
        String etreatselect = "" ;
        
		customerId = applicationGuidelinesForm.getCustomerId();
		prodType   = applicationGuidelinesForm.getProdType();
		prodLine   = applicationGuidelinesForm.getProdLine();
	//	cusid = applicationGuidelinesForm.getCusid();
		
		HttpSession session = request.getSession(false);
		System.out.println("cleanseprod ==> "+request.getParameter("cleanseprod")) ;
    	System.out.println("moisturizeprod ==> "+request.getParameter("moisturizeprod")) ;
    	System.out.println("treatprod ==> "+request.getParameter("treatprod")) ;
    	System.out.println("etreatprod ==> "+request.getParameter("etreatprod")) ;
    	System.out.println("spfprod ==> "+request.getParameter("spfprod")) ;
    	
    	System.out.println("cleanseqty ==> "+request.getParameter("cleanseqty")) ;
    	System.out.println("moisturizeqty ==> "+request.getParameter("moisturizeqty")) ;
    	System.out.println("treatqty ==> "+request.getParameter("treatqty")) ;
    	System.out.println("etreatqty ==> "+request.getParameter("etreatqty")) ;
    	System.out.println("spfqty ==> "+request.getParameter("spfqty")) ;

		
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
	    
        if (request.getParameter("cleanseqty") != null)
        {
        	cleanseQty = request.getParameter("cleanseqty") ;
        }
        if (request.getParameter("moisturizeqty") != null)
        {
        	moisturizeQty = request.getParameter("moisturizeqty") ;
        }
        if (request.getParameter("treatqty") != null)
        {
        	treatQty = request.getParameter("treatqty") ;
        }
        if (request.getParameter("etreatqty") != null)
        {
        	etreatQty = request.getParameter("etreatqty") ;
        }
        if (request.getParameter("spfqty") != null)
        {
        	spfQty = request.getParameter("spfqty") ;
        }
        
        if (request.getParameter("cleanseCheck") != null)
        {
        	cleanseCheck = request.getParameter("cleanseCheck") ;
        }
        if (request.getParameter("moisturizeCheck") != null)
        {
        	moisturizeCheck = request.getParameter("moisturizeCheck") ;
        }
        if (request.getParameter("treatCheck") != null)
        {
        	treatCheck = request.getParameter("treatCheck") ;
        }
        if (request.getParameter("etreatCheck") != null)
        {
        	etreatCheck = request.getParameter("etreatCheck") ;
        }
        
        if (request.getParameter("cleanseSelect") != null)
        {
        	cleanseselect = request.getParameter("cleanseSelect") ;
        	cleanseCheck = "on" ;
        	cleanseQty = "1" ;
        }
        if (request.getParameter("moisturizeSelect") != null)
        {
        	moisturizeselect = request.getParameter("moisturizeSelect") ;
        	moisturizeCheck = "on" ;
        	moisturizeQty = "1" ;
        }
        if (request.getParameter("treatSelect") != null)
        {
        	treatselect = request.getParameter("treatSelect") ;
        	treatCheck = "on" ;
        	treatQty = "1" ;
        }
        if (request.getParameter("etreatSelect") != null)
        {
        	etreatselect = request.getParameter("etreatSelect") ;
        	etreatCheck = "on" ;
        	etreatQty = "1" ;
        }
        
        
        if (cleanseprod != null && !cleanseprod.equalsIgnoreCase("null") && cleanseprod.length() > 0)
        	shopcartstring2 = cleanseprod+shopcartsep1+cleanseQty+shopcartsep2 ;
        	
        if (moisturizeprod != null && !moisturizeprod.equalsIgnoreCase("null") && moisturizeprod.length() > 0)
        	shopcartstring2 = shopcartstring2+moisturizeprod+shopcartsep1+moisturizeQty+shopcartsep2 ;
        
        if (treatprod != null && !treatprod.equalsIgnoreCase("null") && treatprod.length() > 0)
        	shopcartstring2 = shopcartstring2+treatprod+shopcartsep1+treatQty+shopcartsep2 ;
        
        if (etreatprod != null && !etreatprod.equalsIgnoreCase("null") && etreatprod.length() > 0)
        	shopcartstring2 = shopcartstring2+etreatprod+shopcartsep1+etreatQty+shopcartsep2 ;
        	
        if (!request.getParameter("spfprod").isEmpty() && request.getParameter("spfprod") != null && !request.getParameter("spfprod").equalsIgnoreCase("null") && !request.getParameter("spfprod").equalsIgnoreCase("") &&
        		request.getParameter("spfqty") != null && !request.getParameter("spfqty").equalsIgnoreCase("null") && !request.getParameter("spfqty").equalsIgnoreCase(""))
        {
        	//finishCheck = request.getParameter("finishCheck") ;
        	//System.out.println("vb "+finishCheck) ;
        	shopcartstring2 = shopcartstring2+spfprod+shopcartsep1+spfQty+shopcartsep2 ;
        }
        
        shopcartstring = shopcartstring+shopcartstring2 ;
        System.out.println("shopcartstring ==>"+shopcartstring);
//        shopcartstring = "customerId="+customerId+"&"+shopcartstring+shopcartstring2 ;
		RegimenTrackingDAL rtd = new RegimenTrackingDAL() ;
        rtd.insertCustomerShoppingCart(customerId,shopcartstring,"01") ;
        
		System.out.println("dsdf cleanse "+cleanseCheck+" "+cleanseQty+cleanseprod) ;
		System.out.println("dsdf moisturize "+moisturizeCheck+" "+moisturizeQty+moisturizeprod) ;
		System.out.println("dsdf treat "+treatCheck+" "+treatQty+treatprod) ;
		System.out.println("dsdf etreat "+etreatCheck+" "+etreatQty+etreatprod) ;
		System.out.println("dsdf spf "+spfCheck+" "+spfQty+spfprod) ;
		
		System.out.println("dsdf shopcart string "+shopcartstring) ;
		
			request.setAttribute("customerId", customerId);
		    request.setAttribute("productlist", prodlist);
		    request.setAttribute("producthash", prodhash);
		    request.setAttribute("masterData", master);
		    //session.setAttribute("product",product) ;
            request.setAttribute("customerprofile",cp) ;
            request.setAttribute("prodline",prodLine) ;
            request.setAttribute("shopcartstring",shopcartstring) ;
            request.setAttribute("cleanseprod",cleanseprod) ;
            request.setAttribute("moisturizeprod",moisturizeprod) ;
            request.setAttribute("treatprod",treatprod) ;
            request.setAttribute("etreatprod",etreatprod) ;
            request.setAttribute("spfprod",spfprod) ;
            request.setAttribute("spfexists",spfexists) ;
            request.setAttribute("spf",spf) ;
			request.setAttribute("mid", mid);
			request.setAttribute("catid", catid);
			request.setAttribute("mode", mode);
			
			
		//	return null;
			return (mapping.findForward(target));
	}
  	
}