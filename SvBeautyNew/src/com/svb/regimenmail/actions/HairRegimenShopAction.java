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
import com.svb.regimenmail.javabeans.*;

/**
 * @author Neelima
 * 
 */
public class HairRegimenShopAction extends DispatchAction {

	private static Log log = LogFactory.getLog(HairRegimenShopAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		System.out.println("in shop cart action") ;
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
        String finishexists = "" ;
        String prodstring = "" ;
        ArrayList categprods = new ArrayList()  ;
        String finish[] = new String[0] ;
        
		ProductVO product[] = new ProductVO[0];
		CustomerProfileVO cp = new CustomerProfileVO() ;
		ApplicationGuidelinesForm applicationGuidelinesForm = (ApplicationGuidelinesForm)form;
		ArrayList prodlist = new ArrayList();
		Hashtable prodhash = new Hashtable() ;
        Hashtable master = new Hashtable() ;
        
        String prodType = "" ;
        String prodLine = "" ;
        String cusid = "" ;
        
        String shampooCheck = "" ;
        String conditionerCheck = "" ;
        String htreatCheck = "" ;
        String stylingCheck = "" ;
        String finishCheck = "" ;
        
        String shampooQty = "" ;
        String conditionerQty = "" ;
        String htreatQty = "" ;
        String stylingQty = "" ;
        String finishQty = "" ;
        
        String shopcartstring = "" ;
        String shopcartstring2 = "" ;
        String shopcartsep1 = "~" ;
        String shopcartsep2 = "|" ;

        String shampooselect = "" ;
        String conditionerselect = "" ;
        String htreatselect = "" ;
        String stylingselect = "" ;
        
		customerId = applicationGuidelinesForm.getCustomerId();
		prodType   = applicationGuidelinesForm.getProdType();
		prodLine   = applicationGuidelinesForm.getProdLine();
	//	cusid = applicationGuidelinesForm.getCusid();
		
		HttpSession session = request.getSession(false);
		
    	System.out.println("vb rrrr33 "+request.getParameter("finishCheck1")) ;
    	
    	
    	System.out.println("shampooprod ==> "+request.getParameter("shampooprod")) ;
    	System.out.println("conditionerprod ==> "+request.getParameter("conditionerprod")) ;
    	System.out.println("htreatprod ==> "+request.getParameter("htreatprod")) ;
    	System.out.println("stylingprod ==> "+request.getParameter("stylingprod")) ;
    	System.out.println("finishprod ==> "+request.getParameter("finishprod")) ;
    	
    	System.out.println("shampooqty ==> "+request.getParameter("shampooqty")) ;
    	System.out.println("conditionerqty ==> "+request.getParameter("conditionerqty")) ;
    	System.out.println("htreatqty ==> "+request.getParameter("htreatqty")) ;
    	System.out.println("stylingqty ==> "+request.getParameter("stylingqty")) ;
    	System.out.println("finishqty ==> "+request.getParameter("finishqty")) ;
    	
    	//System.out.println("vb rrrr33 "+request.getParameter("shampooqty")) ;
		
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
	    
        if (request.getParameterValues("finish") != null)
        	finish = request.getParameterValues("finish") ;
        if (request.getParameter("finishexists") != null)
        	finishexists = request.getParameter("finishexists") ;
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
	    
        if (request.getParameter("shampooqty") != null)
        {
        	shampooQty = request.getParameter("shampooqty") ;
        }
        if (request.getParameter("conditionerqty") != null)
        {
        	conditionerQty = request.getParameter("conditionerqty") ;
        }
        if (request.getParameter("htreatqty") != null)
        {
        	htreatQty = request.getParameter("htreatqty") ;
        }
        if (request.getParameter("stylingqty") != null)
        {
        	stylingQty = request.getParameter("stylingqty") ;
        }
        if (request.getParameter("finishqty") != null)
        {
        	finishQty = request.getParameter("finishqty") ;
        }
        
        if (request.getParameter("shampooCheck") != null)
        {
        	shampooCheck = request.getParameter("shampooCheck") ;
        }
        if (request.getParameter("conditionerCheck") != null)
        {
        	conditionerCheck = request.getParameter("conditionerCheck") ;
        }
        if (request.getParameter("htreatCheck") != null)
        {
        	htreatCheck = request.getParameter("htreatCheck") ;
        }
        if (request.getParameter("stylingCheck") != null)
        {
        	stylingCheck = request.getParameter("stylingCheck") ;
        }
        if (request.getParameter("finishCheck") != null)
        {
        	finishCheck = request.getParameter("finishCheck") ;
        }        
        if (request.getParameter("shampooSelect") != null)
        {
        	shampooselect = request.getParameter("shampooSelect") ;
        	shampooCheck = "on" ;
        	shampooQty = "1" ;
        }
        if (request.getParameter("conditionerSelect") != null)
        {
        	conditionerselect = request.getParameter("conditionerSelect") ;
        	conditionerCheck = "on" ;
        	conditionerQty = "1" ;
        }
        if (request.getParameter("htreatSelect") != null)
        {
        	htreatselect = request.getParameter("htreatSelect") ;
        	htreatCheck = "on" ;
        	htreatQty = "1" ;
        }
        if (request.getParameter("stylingSelect") != null)
        {
        	stylingselect = request.getParameter("stylingSelect") ;
        	stylingCheck = "on" ;
        	stylingQty = "1" ;
        }
        
        
        if (shampooprod != null && !shampooprod.equalsIgnoreCase("null") && shampooprod.length() > 0)
        	shopcartstring2 = shampooprod+shopcartsep1+shampooQty+shopcartsep2 ;
        	
        if (conditionerprod != null && !conditionerprod.equalsIgnoreCase("null") &&  conditionerprod.length() > 0)
        	shopcartstring2 = shopcartstring2+conditionerprod+shopcartsep1+conditionerQty+shopcartsep2 ;
        
        if (htreatprod != null && !htreatprod.equalsIgnoreCase("null") &&  htreatprod.length() > 0)
        	shopcartstring2 = shopcartstring2+htreatprod+shopcartsep1+htreatQty+shopcartsep2 ;
        
        if (stylingprod != null &&  !stylingprod.equalsIgnoreCase("null") &&  stylingprod.length() > 0)
        	shopcartstring2 = shopcartstring2+stylingprod+shopcartsep1+stylingQty+shopcartsep2 ;
        
        if (!request.getParameter("finishprod").isEmpty() && request.getParameter("finishprod") != null && !request.getParameter("finishprod").equalsIgnoreCase("null") && 
        		!request.getParameter("finishprod").equalsIgnoreCase(" ") && request.getParameter("finishqty") != null && 
        		!request.getParameter("finishqty").equalsIgnoreCase("null")&& !request.getParameter("finishqty").equalsIgnoreCase(" "))
        {
        	//finishCheck = request.getParameter("finishCheck") ;
        	//System.out.println("vb "+finishCheck) ;
        	shopcartstring2 = shopcartstring2+finishprod+shopcartsep1+finishQty+shopcartsep2 ;
        }
        
        shopcartstring = shopcartstring+shopcartstring2 ;
        System.out.println("shopcartstring ==>"+shopcartstring);
//        shopcartstring = "customerId="+customerId+"&"+shopcartstring+shopcartstring2 ;
		RegimenTrackingDAL rtd = new RegimenTrackingDAL() ;
        rtd.insertCustomerShoppingCart(customerId,shopcartstring,"02") ;
        
		System.out.println("dsdf shampoo "+shampooCheck+" "+shampooQty+shampooprod) ;
		System.out.println("dsdf conditioner "+conditionerCheck+" "+conditionerQty+conditionerprod) ;
		System.out.println("dsdf htreat "+htreatCheck+" "+htreatQty+htreatprod) ;
		System.out.println("dsdf styling "+stylingCheck+" "+stylingQty+stylingprod) ;
		System.out.println("dsdf finish "+finishCheck+" "+finishQty+finishprod) ;
		
		System.out.println("dsdf shopcart string "+shopcartstring) ;
		
			request.setAttribute("customerId", customerId);
		    request.setAttribute("productlist", prodlist);
		    request.setAttribute("producthash", prodhash);
		    request.setAttribute("masterData", master);
		    //session.setAttribute("product",product) ;
            request.setAttribute("customerprofile",cp) ;
            request.setAttribute("prodline",prodLine) ;
            request.setAttribute("shopcartstring",shopcartstring) ;
            request.setAttribute("shampooprod",shampooprod) ;
            request.setAttribute("conditionerprod",conditionerprod) ;
            request.setAttribute("htreatprod",htreatprod) ;
            request.setAttribute("stylingprod",stylingprod) ;
            request.setAttribute("finishprod",finishprod) ;
            request.setAttribute("finishexists",finishexists) ;
            request.setAttribute("finish",finish) ;
			request.setAttribute("mid", mid);
			request.setAttribute("catid", catid);
			request.setAttribute("mode", mode);
			
			
    	
			return (mapping.findForward(target));
	}
  	
}