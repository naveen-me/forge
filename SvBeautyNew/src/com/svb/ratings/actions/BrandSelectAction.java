/*
 * Created on Mar 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.ratings.actions;

import java.io.IOException;
import java.util.ArrayList;


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
import com.svb.common.valueobjects.* ;
import com.svb.ratings.dal.ProductBrandProcess;


/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class BrandSelectAction extends DispatchAction {

	private static Log log = LogFactory.getLog(BrandSelectAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
            String target = "success" ;
    		
            HttpSession session = request.getSession(false);
            ArrayList prodlist = new ArrayList() ;
        	ArrayList cleanse = new ArrayList();
        	ArrayList tone = new ArrayList();
        	ArrayList treat = new ArrayList();
        	ArrayList etreat = new ArrayList();
        	ArrayList moisturize = new ArrayList();
        	ArrayList nightmoisturize = new ArrayList();
        	ArrayList exfoliant = new ArrayList();
            ArrayList spf = new ArrayList() ;
            ArrayList prodids = new ArrayList() ;
            String prodstring = "" ;
            
    /*        prodlist = (ArrayList) session.getAttribute("prodList") ;
            cleanse = (ArrayList) prodlist.get(0) ;
            tone =    (ArrayList) prodlist.get(1) ;           
            exfoliant =    (ArrayList) prodlist.get(2) ;           
            moisturize =    (ArrayList) prodlist.get(3) ;           
            treat =    (ArrayList) prodlist.get(5) ;           
            etreat =    (ArrayList) prodlist.get(6) ;           
            spf  = (ArrayList) prodlist.get(7) ;
            
            prodids = (ArrayList) setProdIds(cleanse,  prodids) ;
            prodids = (ArrayList) setProdIds(moisturize,  prodids) ;
            prodids = (ArrayList) setProdIds(treat,  prodids) ;
            prodids = (ArrayList) setProdIds(etreat,  prodids) ;
            prodstring = "(" ;
            for (int i=0 ; i < prodids.size(); i++)
            {
            	if (i == prodids.size()-1)
            	  prodstring = prodstring+"'"+(String)prodids.get(i)+"'"+")" ;
            	else
              	  prodstring = prodstring+"'"+(String)prodids.get(i)+"'"+"," ;
            }
            ProductBrandProcess pbp = new ProductBrandProcess() ;
            String brands[][] = (String[][]) pbp.getBrands(prodstring) ;
        */    
            System.out.println("I am out of brand select") ;
      //      request.setAttribute("brandinfo",brands) ;
			return (mapping.findForward(target));
	}
	
	 public ArrayList setProdIds(ArrayList prod, ArrayList prodids)
	 {
        for (int i=0; i < prod.size() ; i++)
        { 
	        String prodid = (String)((ProductVO) prod.get(i)).getProdId() ;
            if ( prodid != null && prodid.length() > 0) 
            {
            	prodids.add(prodid) ;
            }
        }
	 	return prodids ;
	 }

}