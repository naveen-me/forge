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

import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;
import com.svb.analysis.formbeans.ProductAnalysisForm;
import com.svb.analysis.javabeans.HairPopupRatings;
import com.svb.analysis.javabeans.PopupRatings;
import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.javabean.CustomerTrackBean;
import com.svb.common.valueobjects.* ;
import com.svb.ratings.javabeans.HairProductRecommendationEngine;
import com.svb.ratings.javabeans.HairRecommendationEngine;
import com.svb.ratings.javabeans.HairRecommendationEngineDebug;
import com.svb.ratings.javabeans.ProductRecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngineDebug;
import com.svb.struts.DAL.DetailsDB;
import com.svb.struts.DAL.HairDetailsDB;

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class HairAnalysisAction extends DispatchAction {

	

	private static Log log = LogFactory.getLog(HairAnalysisAction.class);
	
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
        HairPopupRatings popRate = new HairPopupRatings() ;
        HairRecommendationEngine recEng = new HairRecommendationEngine() ;
        HairRecommendationEngineDebug recEngDebug = new HairRecommendationEngineDebug() ;
        HairProductRecommendationEngine precEng = new HairProductRecommendationEngine() ;
		
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

				CustomerTrackBean ctb = new CustomerTrackBean() ;
				ctb.insertCustomerTrack("Hair",customerId,"whyrec","See Active Ingredients",attribvalue,"") ;	
		 try{
		 	
		 	 cp = (CustomerProfileVO) recEng.getCustomerProfile(customerId,cp,"hair") ;
	     	    String haircolor = "" ;
	    	    String hairtype = "" ;
	    	    String hairthickness = "" ;
	    	    String hairtexture = "" ;
	    	    String hairfinish = "" ;
	    	    ArrayList hairprocess = new ArrayList() ;
	    	    ArrayList hairconditions = new ArrayList() ;
	    	    ArrayList hairstyles = new ArrayList() ;
	    	    ArrayList concerns = new ArrayList() ;
                ArrayList allergies  = new ArrayList() ;
                
	            haircolor = cp.getHaircolor() ;
	            hairtype = cp.getHairtype() ;
	            hairthickness = cp.getHairthickness() ;
	            hairtexture = cp.getHairtexture() ;
	            hairfinish = cp.getHairfinish() ;
	            hairprocess = (ArrayList) cp.getHairprocess() ;
	            hairstyles = (ArrayList) cp.getHairstyles() ;
	            hairconditions = (ArrayList) cp.getHairconditions() ;
	            allergies = (ArrayList) cp.getAllergyList() ;
             
 	         ArrayList textdesc         = new ArrayList() ;
 	         ArrayList attribfunctions  = new ArrayList() ;
 	         Hashtable functext         = new Hashtable() ;
 	         Hashtable funcingr         = new Hashtable() ;
 	         
 	         
             Hashtable cprofile = new Hashtable() ;
             
 		     Hashtable funcnames    = (Hashtable) popRate.getFunctionDetailsHT(); // changed on 3.26.05
 	         Hashtable master          = (Hashtable) recEng.getMasterDataTwo() ;
            System.out.println("1vvvvv "+funcnames.size()) ;     
 		     attribfunctions = (ArrayList) popRate.getAttributeFunctions(attribvalue) ;
       //      System.out.println("2") ;     
 		     String funcstring = (String) getString(attribfunctions) ;
             System.out.println("3 "+funcstring) ;     
 		     functext = (Hashtable) popRate.getFunctionText(attribvalue) ;
       //      System.out.println("4 "+functext) ;     
 		     funcingr = (Hashtable) popRate.getFunctionIngredients(attribvalue,prid,funcstring) ;
       //      System.out.println("5") ;     
 		     
 		     Enumeration en = funcingr.keys() ;
 		     while (en.hasMoreElements())
 		     {
 		     	String funckey = (String) en.nextElement() ;
 		     	ArrayList ingredients = (ArrayList) funcingr.get(funckey) ;
 		     	String ingrstring  = (String) getIngrString(ingredients,master,passString) ;
 		     	String[][] funcdata = (String[][]) funcnames.get(funckey) ;
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
    	if (values == null || values.size() == 0)
    		ret = "" ;
    	return ret ;
    }
    public String getIngrString(ArrayList values,Hashtable master,String passString)
    {
    	String ret = "" ;
    	HairDetailsDB ddb = new HairDetailsDB() ;
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
        	     ret = ret+" <a href='hairProductDetails.do?scrname=whyrec"+passString+"&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName="+ingrname+"'>"+master.get((String)values.get(i))+"</a>,"+"" ;
        	  	else
           	     ret = ret+" "+master.get((String)values.get(i))+","+"" ;
        	  }
        	  else
        	  {
        	  	if (glosflag)
        	  	 ret = ret+" and <a href='hairProductDetails.do?scrname=whyrec"+passString+"&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName="+ingrname+"'>"+master.get((String)values.get(i))+"</a>,"+"" ;
        	  	else
           	  	 ret = ret+" and "+master.get((String)values.get(i))+","+"" ;
        	  }
        	}
        	else
        	{
        	 if (glosflag)
          	  ret = ret+"<a href='hairProductDetails.do?scrname=whyrec"+passString+"&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName="+ingrname+"'>"+master.get((String)values.get(i))+"</a>"+"," ;
        	 else
           	  ret = ret+""+master.get((String)values.get(i))+""+"," ;
        	}
        }    	
     	return ret ;
    }
    public static void main(String[] a)
    {
    	HairAnalysisAction aa = new HairAnalysisAction();
    	aa.IngredientBenefits("301220401", "307517","73","H003","Relaxer","Excellent") ;
    	
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

 	    String haircolor = "" ;
	    String hairtype = "" ;
	    String hairthickness = "" ;
	    String hairtexture = "" ;
	    String hairfinish = "" ;
	    ArrayList hairprocess = new ArrayList() ;
	    ArrayList hairconditions = new ArrayList() ;
	    ArrayList hairstyles = new ArrayList() ;
	    ArrayList concerns = new ArrayList() ;
        ArrayList allergies  = new ArrayList() ;

     
		ProductVO[] product = new ProductVO[0];
		ProductVO pr = new ProductVO() ;
		CustomerProfileVO cp = new CustomerProfileVO() ;
        HairPopupRatings popRate = new HairPopupRatings() ;
        HairRecommendationEngine recEng = new HairRecommendationEngine() ;
        HairRecommendationEngineDebug recEngDebug = new HairRecommendationEngineDebug() ;
        HairProductRecommendationEngine precEng = new HairProductRecommendationEngine() ;
		
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
             
	            haircolor = cp.getHaircolor() ;
	            hairtype = cp.getHairtype() ;
	            hairthickness = cp.getHairthickness() ;
	            hairtexture = cp.getHairtexture() ;
	            hairfinish = cp.getHairfinish() ;
	            hairprocess = (ArrayList) cp.getHairprocess() ;
	            hairstyles = (ArrayList) cp.getHairstyles() ;
	            hairconditions = (ArrayList) cp.getHairconditions() ;
	            allergies = (ArrayList) cp.getAllergyList() ;
            
 	         textdesc         = new ArrayList() ;
 	         ArrayList attribfunctions  = new ArrayList() ;
 	         Hashtable functext         = new Hashtable() ;
 	         Hashtable funcingr         = new Hashtable() ;
 	         
             Hashtable cprofile = new Hashtable() ;
             ArrayList cprofile1 = new ArrayList() ;
             
             if (haircolor != null)
             {
               cprofile.put(haircolor,"1") ;
               cprofile1.add(haircolor) ;
             }
             if (hairtype != null)
             {
               cprofile.put(hairtype,"1") ;
               cprofile1.add(hairtype) ;
             }
             if (hairthickness != null)
             {
               cprofile.put(hairthickness,"1") ;
               cprofile1.add(hairthickness) ;
             }
             if (hairtexture != null)
             {
               cprofile.put(hairtexture,"1") ;
               cprofile1.add(hairtexture) ;
             }
             if (hairfinish != null)
             {
               cprofile.put(hairfinish,"1") ;
               cprofile1.add(hairfinish) ;
             }
             for (int i=0 ; i < hairconditions.size() ; i++)
             {
             	cprofile.put(hairconditions.get(i),"1") ;
             	cprofile1.add(hairconditions.get(i)) ;
             }
             for (int j=0 ; j< hairprocess.size() ; j++)
             {
             	cprofile.put(hairprocess.get(j),"1") ;
             	cprofile1.add(hairprocess.get(j)) ;
             }
             for (int j=0 ; j< hairstyles.size() ; j++)
             {
             	cprofile.put(hairstyles.get(j),"1") ;
             	cprofile1.add(hairstyles.get(j)) ;
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

             System.out.println("profile string "+profilestring) ;
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
 		     	System.out.println("func key ==> "+funckey) ;
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
    	HairDetailsDB ddb = new HairDetailsDB() ;
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
        	     ret = ret+" <a href='hairProductDetails.do?scrname=ingrbenefits"+passString+"&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName="+ingrname+"'>"+master.get((String)values.get(i))+"</a>,"+"" ;
        	  	else
           	     ret = ret+" "+master.get((String)values.get(i))+","+"" ;
        	  }
        	  else
        	  {
        	  	if (glosflag)
        	  	 ret = ret+" and <a href='hairProductDetails.do?scrname=ingrbenefits"+passString+"&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName="+ingrname+"'>"+master.get((String)values.get(i))+"</a>,"+"" ;
        	  	else
           	  	 ret = ret+" and "+master.get((String)values.get(i))+","+"" ;
        	  }
        	}
        	else
        	{
        	 if (glosflag)
          	  ret = ret+"<a href='hairProductDetails.do?scrname=ingrbenefits"+passString+"&popup=y&desc=n&direc=n&ingred=n&glos=y&ingName="+ingrname+"'>"+master.get((String)values.get(i))+"</a>"+"," ;
        	 else
           	  ret = ret+""+master.get((String)values.get(i))+""+"," ;
        	}
        }    	
     	return ret ;
    }
    
}