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
import java.util.HashMap;
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
import com.svb.common.javabean.CustomerTrackBean;
import com.svb.common.valueobjects.* ;
import com.svb.ratings.javabeans.ProductRecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngineDebug;
import com.svb.struts.DAL.DetailsDB;

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AnalysisAction extends DispatchAction {

	

	private static Log log = LogFactory.getLog(AnalysisAction.class);
	
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
        String attribscore              = "" ;
        String attribname               = "" ;
        
		Hashtable productsHashtable 	= new Hashtable();
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
	    	if (request.getParameter("attributeScore") != null)
			       attribscore = request.getParameter("attributeScore") ;
	    	if (request.getParameter("attributeName") != null)
			       attribname = request.getParameter("attributeName") ;
	    	
	    	prid = prodId ;
		    prodId = "('"+prodId+"')" ;
		
		    String passString = "&customerId="+customerId+"&productId="+prid+"&attributeId="+attribid+"&attributeValue="+attribvalue+"&attributeScore="+attribscore+"&attributeName="+attribname ;
		    
			//	HttpSession session = request.getSession(false);
			//	String customerId = (String) session.getAttribute("cusid") ; 
				CustomerTrackBean ctb = new CustomerTrackBean() ;
			//	if(request.getParameter("scrname")!=null){
					ctb.insertCustomerTrack("Skin",customerId,"whyrec","See Active Ingredients",attribvalue,"") ;	
			//	}


		 try{
		 	
		 	 cp = (CustomerProfileVO) recEng.getCustomerProfile(customerId,cp,"skin") ;
             String skintype        = cp.getSkinType() ;
             String age             = cp.getAge() ;
 	         ArrayList skincondition   = cp.getSkinConditionList() ;
             ArrayList skinconcern     = cp.getSkinConcernList() ;
             ArrayList eyeconcern      = cp.getEyeConcernList() ;
 	         ArrayList allergies       = cp.getAllergyList();
             
 	         ArrayList textdesc         = new ArrayList() ;
 	         ArrayList attribfunctions  = new ArrayList() ;
 	         Hashtable functext         = new Hashtable() ;
 	         Hashtable funcingr         = new Hashtable() ;
 	         
 	         
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
 	         Hashtable master          = (Hashtable) recEng.getMasterDataTwo() ;
            System.out.println("1 "+funcnames.size()) ;     
 		     attribfunctions = (ArrayList) popRate.getAttributeFunctions(attribvalue) ;
        //      System.out.println("2") ;     
 		     String funcstring = (String) getString(attribfunctions) ;
        //     System.out.println("3") ;     
 		     functext = (Hashtable) popRate.getFunctionText(attribvalue) ;
       //      System.out.println("4") ;     
 		     funcingr = (Hashtable) popRate.getFunctionIngredients(attribvalue,prid,funcstring) ;
       //      System.out.println("5") ;     
 		     
 		    // log.debug("=funcingr= "+ funcingr);
 		     
 		     Enumeration en = funcingr.keys() ;
 		     while (en.hasMoreElements())
 		     {
 		     	String funckey = (String) en.nextElement() ;
 		     	ArrayList ingredients = (ArrayList) funcingr.get(funckey) ;
 		     	String ingrstring  = (String) getIngrString(ingredients,master,passString) ;
 		     	String[][] funcdata = (String[][]) funcnames.get(funckey) ;
 		 	  	//log.debug("=funckey="+ funckey);
		      	//log.debug("=ingredients="+ ingredients);
		      	//log.debug("=ingrstring="+ ingrstring);
		      	//log.debug("=funcdata="+ funcdata);
 		     	
 		     	String funcname = "" ;
 		     	if (ingredients.size() > 0)
 		     	{
 		     	  if (ingredients.size() > 1)
 		     	   funcname = "<strong>"+" "+funcdata[0][1]+"s</strong>," ;
 		     	  else
		     	   funcname = "<strong>"+" "+funcdata[0][0]+" "+funcdata[0][1]+"</strong>," ;
 		     	  String functiontext = (String) functext.get(funckey+attribvalue) ;
 		     	  String finaltext = ingrstring+" "+funcname+" "+functiontext+"" ;
 		     	  
 		     
 		     	  textdesc.add(finaltext) ;
 		     	}
 		     }
             master = new Hashtable() ; 		     
 		     request.setAttribute("funcnames",funcnames) ;
//	         request.setAttribute("hta",master) ;
//	         request.setAttribute("singleproduct",pr) ;
//	         request.setAttribute("singlecustomer",cp) ;
	         request.setAttribute("customerId",customerId) ;
	         request.setAttribute("prodId",prid) ;
	         request.setAttribute("attributeId",attribid) ;
	         request.setAttribute("attributeValue",attribvalue) ;
	         request.setAttribute("attributeName",attribname) ;
	         request.setAttribute("textdesc",textdesc) ;
	         request.setAttribute("attribscore",attribscore) ;
	         
	         //log.debug("=textdesc= "+ textdesc);
		    }catch(Exception e){
		    	////logger.log(Level.DEBUG,e);
		    	e.printStackTrace();
		    }
			finally
			{
				;
			}
 		 
		
		  //System.out.println("vbbbbbbbb 66") ;

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
    	  
          //System.out.println("vb5 funcstring "+fht.size()+" ingrlist "+ingrlist.length) ;
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
         //System.out.println("vvv "+ratings.size()) ;

    	return ratings;
    }
    public String getString(ArrayList values)
    {
    	String ret = "(" ;
    	for (int i=0 ; i < values.size(); i++)
        {
        	if (i == values.size()-1)
        	  ret = ret+"'"+(String)values.get(i)+"'"+")" ;
        	else
          	  ret = ret+"'"+(String)values.get(i)+"'"+"," ;
        }    	
    	return ret ;
    }
    public String getIngrString(ArrayList values,Hashtable master,String passString)
    {
    	String ret = "" ;
    	DetailsDB ddb = new DetailsDB() ;
    	HashMap ing = (HashMap) ddb.getIngredientNamesWithGlossary() ;
    	boolean glosflag = false ;
    	for (int i=0 ; i < values.size(); i++)
        {
    		glosflag = false ;
    		String ingrname = (String) master.get(((String)values.get(i)).trim()) ;
    		if (ing.get(ingrname.toString().toUpperCase())!=null)
    		  glosflag = true ;
        	if (i == values.size()-1)
        	{
        	  if (i == 0)
        	  {
        	  	if (glosflag)
        	     ret = ret+" <a href='productDetails.do?scrname=whyrec"+passString+"&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName="+ingrname+"'>"+master.get((String)values.get(i))+"</a>,"+"" ;
        	  	else
           	     ret = ret+" "+master.get((String)values.get(i))+","+"" ;
        	  }
        	  else
        	  {
        	  	if (glosflag)
        	  	 ret = ret+" and <a href='productDetails.do?scrname=whyrec"+passString+"&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName="+ingrname+"'>"+master.get((String)values.get(i))+"</a>,"+"" ;
        	  	else
           	  	 ret = ret+" and "+master.get((String)values.get(i))+","+"" ;
        	  }
        	}
        	else
        	{
        	 if (glosflag)
          	  ret = ret+"<a href='productDetails.do?scrname=whyrec"+passString+"&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName="+ingrname+"'>"+master.get((String)values.get(i))+"</a>"+"," ;
        	 else
           	  ret = ret+""+master.get((String)values.get(i))+""+"," ;
        	}
        }    	
    	return ret ;
    }
    public static void main(String[] a)
    {
    	AnalysisAction aa = new AnalysisAction();
    	aa.IngredientBenefits("301220261", "188533","51","T003","a combination of oily and dry","Excellent") ;
    	
    }
    public ArrayList IngredientBenefits(String customerid, String productid,String attribid, String attribvalue, String attribname, String attribscore)
    {
    	System.out.println("customer id "+customerid+"  product id "+productid) ;
    	String ret  = "" ;
		String repId     = "";
		String customerId     = "";
		String prodId = "" ;
		String dname = "" ;
		String prid = "" ;
        
		ArrayList textdesc         = new ArrayList() ;
		Hashtable productsHashtable 	= new Hashtable();
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
		
        SVDBOperation dbo = new SVDBOperation();
        
	    try{
			       customerId = customerid ;
			       prodId = productid ;
	    	
	    	       prid = prodId ;
		           prodId = "('"+prodId+"')" ;
		           
		
		    String passString = "&customerId="+customerId+"&productId="+prid+"&attributeId="+attribid+"&attributeValue="+attribvalue+"&attributeScore="+attribscore+"&attributeName="+attribname ;
		    
		    System.out.println("pass string "+passString) ;
		    
				CustomerTrackBean ctb = new CustomerTrackBean() ;


		 try{
		 	
		 	 cp = (CustomerProfileVO) recEng.getCustomerProfile(customerId,cp,"skin") ;
             String skintype        = cp.getSkinType() ;
             String age             = cp.getAge() ;
 	         ArrayList skincondition   = cp.getSkinConditionList() ;
             ArrayList skinconcern     = cp.getSkinConcernList() ;
             ArrayList eyeconcern      = cp.getEyeConcernList() ;
 	         ArrayList allergies       = cp.getAllergyList();
             
 	         textdesc         = new ArrayList() ;
 	         ArrayList attribfunctions  = new ArrayList() ;
 	         Hashtable functext         = new Hashtable() ;
 	         Hashtable funcingr         = new Hashtable() ;
 	         
 	         System.out.println(skintype+" conc "+skinconcern.size()+" cond "+skincondition.size()+" eye "+eyeconcern.size());
             Hashtable cprofile = new Hashtable() ;
             ArrayList cprofile1 = new ArrayList() ;
             
             if (skintype != null)
             {
               cprofile.put(skintype,"1") ;
               cprofile1.add(skintype) ;
             }
             for (int i=0 ; i < skincondition.size() ; i++)
             {
             	cprofile.put(skincondition.get(i),"1") ;
             	cprofile1.add(skincondition.get(i)) ;
             }
             for (int j=0 ; j< skinconcern.size() ; j++)
             {
             	cprofile.put(skinconcern.get(j),"1") ;
             	cprofile1.add(skinconcern.get(j)) ;
             }
             for (int j=0 ; j< eyeconcern.size() ; j++)
             {
             	cprofile.put(eyeconcern.get(j),"1") ;
             	cprofile1.add(eyeconcern.get(j)) ;
             }
             
             
             String  profilestring = "(" ;
	        	if (cprofile1.size() > 0)
	        	{
	        		for (int i=0; i < cprofile1.size() ; i++)
	        		{
	                	if (i == cprofile1.size()-1)
	                		profilestring = profilestring+"'"+(String)cprofile1.get(i)+"'"+")" ;
		                	else
		                		profilestring = profilestring+"'"+(String)cprofile1.get(i)+"'"+"," ;
	        		}
	        	}

             
 		     Hashtable funcnames    = (Hashtable) popRate.getFunctionDetailsHT(); // changed on 3.26.05
 	         Hashtable master          = (Hashtable) recEng.getMasterDataTwo() ;
 	         
            System.out.println("1 "+funcnames.size()) ;
            
 		     attribfunctions = (ArrayList) popRate.getAttributeFunctionsNew(profilestring) ;
             System.out.println("2 "+attribfunctions.size()) ;     
 		     String funcstring = (String) getString(attribfunctions) ;
             System.out.println("3 "+funcstring) ;     
 		     functext = (Hashtable) popRate.getFunctionTextNew(profilestring) ;
             System.out.println("4 "+functext) ;     
 		     funcingr = (Hashtable) popRate.getFunctionIngredientsNew(profilestring,prid,funcstring) ;
             System.out.println("5 "+funcingr) ;     
 		     
 		     Enumeration en = funcingr.keys() ;
 		     while (en.hasMoreElements())
 		     {
 		     	String funckey = (String) en.nextElement() ;
 		     	ArrayList ingredients = (ArrayList) funcingr.get(funckey) ;
 		     	String ingrstring  = (String) getIngrStringNew(ingredients,master,passString) ;
 		     	System.out.println("6 "+ingrstring) ;
 		     	
 		     	String[][] funcdata = (String[][]) funcnames.get(funckey) ;
 		     	String funcname = "" ;
 		     	if (ingredients.size() > 0)
 		     	{
 		     	  if (ingredients.size() > 1)
 		     	   funcname = "<strong>"+" "+funcdata[0][1]+"s</strong>," ;
 		     	  else
		     	   funcname = "<strong>"+" "+funcdata[0][0]+" "+funcdata[0][1]+"</strong>," ;
 		     	  String functiontext = (String) functext.get(funckey) ;
 		     	  
 		     	  System.out.println("7 "+functiontext) ;
 		     	  
 		     	  String finaltext = ingrstring+" "+funcname+" "+functiontext+"" ;
 		     	  System.out.println(" 8" +finaltext) ;
 		     	  textdesc.add(finaltext) ;
 		     	}
 		     }
             master = new Hashtable() ; 		     
	         
 		     
		    }catch(Exception e){
		    	////logger.log(Level.DEBUG,e);
		    	e.printStackTrace();
		    }
			finally
			{
				;
			}
 		 
		
		  //System.out.println("vbbbbbbbb 66") ;

	    }catch(Exception e){
	    	////logger.log(Level.DEBUG,e);
	    	e.printStackTrace();
	    }
           return textdesc ;
    }
    public String getIngrStringNew(ArrayList values,Hashtable master,String passString)
    {
    	String ret = "" ;
    	DetailsDB ddb = new DetailsDB() ;
    	HashMap ing = (HashMap) ddb.getIngredientNamesWithGlossary() ;
    	boolean glosflag = false ;
    	for (int i=0 ; i < values.size(); i++)
        {
    		glosflag = false ;
    		String ingrname = (String) master.get(((String)values.get(i)).trim()) ;
    		if (ing.get(ingrname.toString().toUpperCase())!=null)
    		  glosflag = true ;
        	if (i == values.size()-1)
        	{
        	  if (i == 0)
        	  {
        	  	if (glosflag)
        	     ret = ret+" <a href='productDetails.do?scrname=ingrbenefits"+passString+"&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName="+ingrname+"'>"+master.get((String)values.get(i))+"</a>,"+"" ;
        	  	else
           	     ret = ret+" "+master.get((String)values.get(i))+","+"" ;
        	  }
        	  else
        	  {
        	  	if (glosflag)
        	  	 ret = ret+" and <a href='productDetails.do?scrname=ingrbenefits"+passString+"&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName="+ingrname+"'>"+master.get((String)values.get(i))+"</a>,"+"" ;
        	  	else
           	  	 ret = ret+" and "+master.get((String)values.get(i))+","+"" ;
        	  }
        	}
        	else
        	{
        	 if (glosflag)
          	  ret = ret+"<a href='productDetails.do?scrname=ingrbenefits"+passString+"&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName="+ingrname+"'>"+master.get((String)values.get(i))+"</a>"+"," ;
        	 else
           	  ret = ret+""+master.get((String)values.get(i))+""+"," ;
        	}
        }    	
    	return ret ;
    }
}