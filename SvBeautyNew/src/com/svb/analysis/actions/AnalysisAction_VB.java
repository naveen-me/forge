/*
 * Created on Mar 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.analysis.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.svb.common.valueobjects.ProductVO;
import com.svb.analysis.formbeans.ProductAnalysisForm;
import com.svb.analysis.javabeans.PopupRatings;
import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.valueobjects.* ;
import com.svb.ratings.javabeans.ProductRecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngineDebug;

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AnalysisAction_VB extends DispatchAction {

	

	private static Log log = LogFactory.getLog(AnalysisAction_VB.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String target = "success";	
		String repId     = "";
		String customerId     = "";
		String prodId = "" ;
		String dname = "" ;
		String prid = "" ;
        String attribid = "" ;
        String attribvalue = "" ;
        
	    Hashtable regimenprodtype       = new Hashtable();
	    Hashtable regimenusage          = new Hashtable();
		Hashtable productsHashtable 	= new Hashtable();
		Hashtable fht				   	= new Hashtable();
		String ingrlist[][]             = new String[0][0] ;
        Hashtable ratings               = new Hashtable() ;
        Hashtable ingrratings		   	= new Hashtable();
        
        
		ProductVO[] product = new ProductVO[0];
		ProductVO pr = new ProductVO() ;
		CustomerProfileVO cp = new CustomerProfileVO() ;
        PopupRatings popRate = new PopupRatings() ;
        RecommendationEngine recEng = new RecommendationEngine() ;
        RecommendationEngineDebug recEngDebug = new RecommendationEngineDebug() ;
        ProductRecommendationEngine precEng = new ProductRecommendationEngine() ;
		
		ProductAnalysisForm analysisForm = (ProductAnalysisForm)form;
        SVDBOperation dbo = new SVDBOperation();
	    try{
	    	if (request.getParameter("customerId") != null)
			       customerId = request.getParameter("customerId") ;
	    	if (request.getParameter("productId") != null)
			       prodId = request.getParameter("productId") ;
	    	if (request.getParameter("attributeId") != null)
			       attribid = request.getParameter("attributeId") ;
	    	if (request.getParameter("attributeValue") != null)
			       attribvalue = request.getParameter("attributeValue") ;
	    	
	    	prid = prodId ;
		    prodId = "('"+prodId+"')" ;
		try{
			
          dbo.createConnection();
		  recEng.setDBO(dbo);
		  
		  cp = (CustomerProfileVO) recEng.getCustomerProfile(customerId,cp,"skin") ;

		  productsHashtable = (Hashtable) recEng.getBeautyProductInfo(prodId); //gets the product
		  productsHashtable  = recEngDebug.getProductsIngredients("", "prodId", "",productsHashtable,prodId, false); //Single changes Struts
		  product = recEng.getProductArray(product,productsHashtable);
		  ingrratings = recEng.getIngredientsRatings(cp, false);
		  product = (ProductVO[]) recEng.ratings(cp,product,ingrratings);
		  
		} catch (Exception edb)
		{
			////logger.log(Level.DEBUG,edb) ;
			edb.printStackTrace();
		}
		finally
		{
			dbo.closeConnection() ;
		}
		 // System.out.println("vbbbbbbbb 22 ggg "+productsHashtable.get(prid.trim())) ;
		  
		//  System.out.println("vbbbbbbbb 33") ;
		   pr = (ProductVO) product[0] ;
		   System.out.println("vbbbbbdvgdfgdfbbb 44 "+pr.getProdName()) ;
			  

		 try{
		     
 	         Hashtable hta          = (Hashtable) recEng.getMasterDataOne() ;
	         
	         dbo.createConnection();
		     popRate.setDBO(dbo);
		     
             String skintype        = cp.getSkinType() ;
             String age             = cp.getAge() ;
 	         ArrayList skincondition   = cp.getSkinConditionList() ;
             ArrayList skinconcern     = cp.getSkinConcernList() ;
             ArrayList eyeconcern      = cp.getEyeConcernList() ;
 	         ArrayList allergies       = cp.getAllergyList();
             
             Hashtable cprofile = new Hashtable() ;
             if (skintype != null)
               cprofile.put(skintype,"1") ;
             for (int i=0 ; i < skincondition.size() ; i++)
             	cprofile.put(skincondition.get(i),"1") ;
             for (int j=0 ; j< skinconcern.size() ; j++)
             	cprofile.put(skinconcern.get(j),"1") ;
             for (int j=0 ; j< eyeconcern.size() ; j++)
             	cprofile.put(eyeconcern.get(j),"1") ;
             
 		     Hashtable funcnames    = (Hashtable) popRate.getFunctionDetailsHT(); // changed on 3.26.05
 		     
 		     System.out.println("before attrib function size "+pr.getIndividualingrratings().size()+" fff f "+pr.getProdId()) ;
 		     
 		     Hashtable attribfunction = popRate.getAttribFunction(pr.getIndividualingrratings(),pr.getProdId());
 		     System.out.println("attrib function size "+attribfunction.size()) ;
 		     ArrayList receiveList = new ArrayList() ;
 		     
             receiveList = (ArrayList) popRate.getDesc(pr.getMasterProdType(),attribfunction, cprofile);
         
         
 		     Hashtable ht2          = (Hashtable) receiveList.get(0) ; // popRate.getDescIndividual();
 		     Hashtable combo        =  (Hashtable) receiveList.get(1) ;// popRate.getDescCombo();
 		     
             String textdesc = "" ;					

 		     if(ht2.get(attribid+"-"+attribvalue)!=null) 
 		     {

 		  	    Enumeration e = ht2.keys();
 		  	    boolean flag = true ; 
 		  	    if(ht2.get(attribid+"-"+attribvalue)!=null)
 		  	    {
 		  	      Hashtable funchash = (Hashtable)ht2.get(attribid+"-"+attribvalue);
 		  	      Enumeration en = funchash.keys();
 		  	      while(en.hasMoreElements()) 
 		  	      { 
 		  	        String key = (String)en.nextElement();
 		  	        FuncDataVO fd      = (FuncDataVO)funchash.get(key) ;
 		  	        String desc      = fd.getDesc();
 		  	        String ingrnames = fd.getIngrNames();
 		  	        int ingrcount    = fd.getCountIngr();
 		               
 		  	        if(!desc.trim().equals("NA")) 
 		  	        {
 		  		      if(ingrcount == 1) 
 		  		      { 
 		  		         textdesc = "contains"+ ingrnames+","+((String [][])funcnames.get(key))[0][0]+" "+((String [][])funcnames.get(key))[0][1]+" "+desc ;
 		  		      }
 		  		      else 
 		  		      { 
 		  			     textdesc = "contains"+ ingrnames+","+((String [][])funcnames.get(key))[0][1]+"s" +desc	 ;
 		  		      }
 		  	        } // if 
 		  	     }  	//while
 		  	 } // ht2(2)
 		   } // if ht2(1) 
        
 		   System.out.println("text desc "+textdesc) ;
  	       	     
 		     request.setAttribute("funcnames",funcnames) ;
 		     request.setAttribute("ht2",ht2) ;
 		     request.setAttribute("combo",combo) ;
 		     
	         request.setAttribute("hta",hta) ;
	         request.setAttribute("singleproduct",pr) ;
	         request.setAttribute("singlecustomer",cp) ;
	         request.setAttribute("customerId",customerId) ;
	         request.setAttribute("attributeId",attribid) ;
	         request.setAttribute("textdesc",textdesc) ;
	     
 		     
		    }catch(Exception e){
		    	////logger.log(Level.DEBUG,e);
		    	e.printStackTrace();
		    }
			finally
			{
				dbo.closeConnection() ;
			}
 		 
		
		  System.out.println("vbbbbbbbb 66") ;

	    }catch(Exception e){
	    	////logger.log(Level.DEBUG,e);
	    	e.printStackTrace();
	    }
		return (mapping.findForward(target));
	}
    public Hashtable getFuncRatingsCount(Hashtable fht, String[][] ingrlist, ProductVO prod)
    {
    	  Hashtable ratings = new Hashtable() ;
   	      String pmtype            = "" ;
    	  String pbase             = "" ;
    	  
          pmtype = (String) prod.getMasterProdType() ;
          pbase  = (String) prod.getBase() ;
    	  
          System.out.println("vb5 funcstring "+fht.size()+" ingrlist "+ingrlist.length) ;
          Enumeration feh = fht.keys() ;
          while (feh.hasMoreElements())
          {
                  int addval = 1 ;
          		String key = (String) feh.nextElement() ;

                  if ( key.trim().substring(0, 2).equals("51"))
  					addval = 2 ;
                  if ( key.trim().substring(0, 2).equals("52"))
  					addval = 2 ;

                StringTokenizer stkey1 = new StringTokenizer(key,"-") ;
                String key11 = stkey1.nextToken() ;
                String key21 = stkey1.nextToken() ;

          		Hashtable iht = (Hashtable) fht.get(key) ;
          		int ingrscore = 0 ;
          		int ingrcount = 0 ;
          		int ingrpercentage = 0 ;
                int basescore = 0 ;
                int basecount = 0 ;
          		int basepercentage = 0 ;
                
          		/*
                    boolean mbasecheck = false ;
          			String basekey = "01"+"-"+pbase+"-NA-"+pmtype ;
          			System.out.println("vffffffff "+basekey) ;
          			if (iht.get(basekey) != null)
          			{
          				basescore = basescore+( Integer.parseInt((String) iht.get(basekey)) * addval) ;
          				basecount++ ;
                          if (addval == 2)
                            basecount++ ;
          				mbasecheck = true ;
          			}
          			else
          			{
          				basekey = "01"+"-"+pbase+"-NA-"+"99" ;
          				if (!mbasecheck && iht.get(basekey) != null)
              			{
          				  basescore = basescore+( Integer.parseInt((String) iht.get(basekey)) * addval) ;
          				  basecount++ ;
                            if (addval == 2)
                               basecount++ ;
              			  mbasecheck = true ;
              			}
          			}
          			if (basecount != 0)
                    {
          				basepercentage = 100 * basescore / (basecount * 10);
                          pattribs2.add(key21+":"+basescore+":"+basecount+":"+basepercentage) ;
                    }
                  */     
          		for (int i=0; i < ingrlist.length; i++)
          		{
          			boolean mtypecheck = false ;
          			String ingrkey = "02"+"-"+ingrlist[i][0]+"-NA-"+pmtype ;
          		//	System.out.println(ingrkey) ;
          			if (iht.get(ingrkey) != null)
          			{
          				ingrscore = ingrscore+( Integer.parseInt((String) iht.get(ingrkey)) * addval) ;
          				ingrcount++ ;
                          if (addval == 2)
                            ingrcount++ ;
          				mtypecheck = true ;
          			}
          			else
          			{
          				ingrkey = "02"+"-"+ingrlist[i][0]+"-NA-"+"99" ;
          				if (!mtypecheck && iht.get(ingrkey) != null)
              			{
          				  String scr = (String) iht.get(ingrkey) ;
          				  if ( !scr.equals("5"))
          				  {
          				    ingrscore = ingrscore+( Integer.parseInt((String) iht.get(ingrkey)) * addval) ;
          				    ingrcount++ ;
                            if (addval == 2)
                               ingrcount++ ;
              			    mtypecheck = true ;
          				  }
              			}
          			}
          		}
          		if (ingrcount != 0)
                {
          				ingrpercentage = 100 * ingrscore / (ingrcount * 10);
          				ratings.put(key21,""+ingrpercentage) ;
                }
         } 
         System.out.println("vvv "+ratings.size()) ;

    	return ratings;
    }
}