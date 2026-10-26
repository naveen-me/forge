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

import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;
import com.svb.analysis.formbeans.ProductAnalysisForm;
import com.svb.analysis.javabeans.PopupRatings;
import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.javabean.CustomerTrackBean;
import com.svb.common.valueobjects.* ;
import com.svb.ratings.javabeans.HairProductRecommendationEngine;
import com.svb.ratings.javabeans.HairRecommendationEngine;
import com.svb.ratings.javabeans.ProductRecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngineDebug;

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class HairAnalysisDetailAction extends DispatchAction {

	

	private static Log log = LogFactory.getLog(HairAnalysisDetailAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String target = "success";	
		String repId     = "";
		String customerId     = "";
		String prodId = "" ;
		String dname = "" ;
		String prid = "" ;
	    String pmtype =  "" ;
	    
	    Hashtable regimenprodtype           = new Hashtable();
	    Hashtable regimenusage           = new Hashtable();
		Hashtable productsHashtable 	= new Hashtable();
		Hashtable ingrratings		   	= new Hashtable();
		Hashtable fht				   	= new Hashtable();
		String ingrlist[][]             = new String[0][0] ;
        Hashtable ratings               = new Hashtable() ;
        ArrayList profiles              = new ArrayList() ;
        
		ProductVO product = new ProductVO();
		ProductVO pr = new ProductVO() ;
		CustomerProfileVO cp = new CustomerProfileVO() ;
        PopupRatings popRate = new PopupRatings() ;
        RecommendationEngineDebug recEngDebug = new RecommendationEngineDebug() ;
        HairRecommendationEngine recEng = new HairRecommendationEngine() ;
        HairProductRecommendationEngine precEng = new HairProductRecommendationEngine() ;
		
		ProductAnalysisForm analysisForm = (ProductAnalysisForm)form;
        SVDBOperation dbo = new SVDBOperation();
	    try{
	    	if (request.getParameter("customerId") != null)
			       customerId = request.getParameter("customerId") ;
	    	if (request.getParameter("productId") != null)
			       prodId = request.getParameter("productId") ;
	    	prid = prodId ;
		    prodId = "('"+prodId+"')" ;
				CustomerTrackBean ctb = new CustomerTrackBean() ;
			    ctb.insertCustomerTrack("Hair",customerId,"whyrec","Why Rec",prid,"") ;	
		try{
			
          dbo.createConnection();
		  recEng.setDBO(dbo);
		  
		  cp = (CustomerProfileVO) recEng.getCustomerProfile(customerId,cp,"skin") ;

		  productsHashtable = (Hashtable) recEng.getHairProductInfo(prodId); //gets the product
//		  productsHashtable  = recEngDebug.getProductsIngredients("", "prodId", "",productsHashtable,prodId); //this method
//          ingrlist = (String[][]) ((ProductVO)productsHashtable.get(prid)).getIngredients() ;
		  ingrratings = recEng.getManualRatings(cp, true);
		  System.out.println("ingr ratings size "+ingrratings.size()) ;
		  product = (ProductVO) productsHashtable.get(prid) ;
		  product = (ProductVO) recEng.ratingsIndividual(cp,product,ingrratings);
		  
		  
		  
		} catch (Exception edb)
		{
			////logger.log(Level.DEBUG,edb) ;
			edb.printStackTrace();
		}
		finally
		{
			dbo.closeConnection() ;
		}
		  
			  
		   
		 try{
		     
 	         Hashtable hta          = (Hashtable) recEng.getMasterDataOne() ;
	         request.setAttribute("hta",hta) ;
                	         
	     
        //     fht = (Hashtable) recEngDebug.getRatingsIndividual(prid,"func") ;
    //         System.out.println("vbbbbbbbb 444444affdsdgdg "+fht.size()) ;
    //         ratings  = (Hashtable) getFuncRatingsCount(fht, ingrlist, pr) ;

             pmtype = product.getMasterProdType() ;
             String age             = cp.getAge() ;
             String natural         = cp.getNatural() ;
 	         ArrayList allergies       = cp.getAllergyList();
             String ptype              = (String) product.getProdType() ;
             Hashtable bumpscores      = (Hashtable) recEngDebug.getBumpScores() ;
     	    String haircolor = "" ;
    	    String hairtype = "" ;
    	    String hairthickness = "" ;
    	    String hairtexture = "" ;
    	    String hairfinish = "" ;
    	    ArrayList hairprocess = new ArrayList() ;
    	    ArrayList hairconditions = new ArrayList() ;
    	    ArrayList hairstyles = new ArrayList() ;
    	    ArrayList concerns = new ArrayList() ;
            
            haircolor = cp.getHaircolor() ;
            hairtype = cp.getHairtype() ;
            hairthickness = cp.getHairthickness() ;
            hairtexture = cp.getHairtexture() ;
            hairfinish = cp.getHairfinish() ;
            hairprocess = (ArrayList) cp.getHairprocess() ;
            hairstyles = (ArrayList) cp.getHairstyles() ;
            hairconditions = (ArrayList) cp.getHairconditions() ;
            allergies = (ArrayList) cp.getAllergyList() ;
             
 	         if (pmtype.equals("1001") || pmtype.equals("1003") || pmtype.equals("1005"))
 	         {
 		         profiles.add(haircolor) ;
 	 	         profiles.add(hairtype) ;
 	 	         profiles.add(hairthickness) ;
 	 	         profiles.add(hairtexture) ;
 	          for (int i=0; i < hairconditions.size() ; i++)
 	          {
 	         	String scond = (String) hairconditions.get(i) ;
 	         	profiles.add(scond) ;
 	          }
 	          for (int i=0; i < hairprocess.size() ; i++)
 	          {
 	         	String sconc = (String) hairprocess.get(i) ;
 	         	profiles.add(sconc) ;
 	          }
 	          for (int i=0; i < hairstyles.size() ; i++)
 	          {
 	         	String econc = (String) hairstyles.get(i) ;
 	         	profiles.add(econc) ;
 	          }
 	         }
 	         if (pmtype.equals("1004") && hairstyles.get(0).equals("S999"))
 	         {
 		         profiles.add(haircolor) ;
 	 	         profiles.add(hairtype) ;
 	 	         profiles.add(hairthickness) ;
 	 	         profiles.add(hairtexture) ;
 	          for (int i=0; i < hairconditions.size() ; i++)
 	          {
 	         	String scond = (String) hairconditions.get(i) ;
 	         	profiles.add(scond) ;
 	          }
 	          for (int i=0; i < hairprocess.size() ; i++)
 	          {
 	         	String sconc = (String) hairprocess.get(i) ;
 	         	profiles.add(sconc) ;
 	          }
 	          for (int i=0; i < hairstyles.size() ; i++)
 	          {
 	         	String econc = (String) hairstyles.get(i) ;
 	         	profiles.add(econc) ;
 	          }
 	         }
 	         else if (pmtype.equals("1004") && !hairstyles.get(0).equals("S999"))
 	         {
 	          for (int i=0; i < hairstyles.size() ; i++)
 	          {
 	         	String econc = (String) hairstyles.get(i) ;
 	         	profiles.add(econc) ;
 	          }
 	         }
 	         if (pmtype.equals("1009"))
 	         {
 	 	         profiles.add(hairfinish) ;
 	         } 	         
	         Hashtable pManRatings = new Hashtable();
 	         	
 	         pManRatings = (Hashtable) product.getProfileBaseRatings() ; 
 	         ratings  = new Hashtable() ;
 	         
 	         for (int i=0; i < profiles.size() ; i++)
 	         {
 	         	int pbasescore = 0 ;
 	         	int pbasecount = 0 ;
 	         	int pbasepercen = 0 ;
 	         	
 	         	
 	         	String profileAtt = (String) profiles.get(i) ;
 	         	String bumpkey    = profileAtt+ptype ;
 	         	String bscore  = (String) bumpscores.get(bumpkey) ;
         		System.out.println("bump score "+bumpkey+"  "+bscore) ;
 	         	
 	         	if (pManRatings.get(profileAtt) != null)
 	         	{
 	         		String pbasescorestring = (String) pManRatings.get(profileAtt) ;
					StringTokenizer stp = new StringTokenizer(pbasescorestring,"::") ;
					String stps = stp.nextToken() ;
					String stpc = stp.nextToken() ;
					pbasescore =  Integer.parseInt(stps) ;
					pbasecount =  Integer.parseInt(stpc) ;
// 	         		pbasescore = Integer.parseInt(pbasescorestring) ;
 //	         		pbasecount = 1 ;
 	         		System.out.println("action base "+pbasescore+" "+pbasecount) ;
 	         	}
 	         	
 				float basepercentage = 0.0f;


 				if (pbasecount != 0)
 					pbasepercen = 100 * pbasescore / (pbasecount * 10);

 				float pbasepercentage = Float.parseFloat(String.valueOf(pbasepercen));
 				float score = 0.0f;
                float bumpscore = 0.0f;
                if (bscore != null && bscore.length() != 0)
                   bumpscore = Float.parseFloat(String.valueOf(bscore));
                
                
   				basepercentage = (pbasepercentage * 100f) / 100f;
 				score = basepercentage ;
 				score = score+bumpscore ;
 				
         		System.out.println("action score "+profileAtt+" "+score+" "+bumpscore) ;
    	         if (pmtype.equals("1009"))
     	         {
    	         	score = 100.0f ;
     	         } 			
 				ratings.put(profileAtt,""+score) ;
 	         }
	         request.setAttribute("hta",hta) ;
	         request.setAttribute("singleproduct",product) ;
	         request.setAttribute("singlecustomer",cp) ;
	         request.setAttribute("customerId",customerId) ;
	         
	         request.setAttribute("ratings",ratings) ;
             request.setAttribute("hairtype",hairtype) ;
             request.setAttribute("hairthickness",hairthickness) ;
             request.setAttribute("hairtexture",hairtexture) ;
             request.setAttribute("age",age) ;
             request.setAttribute("haircondition",hairconditions) ;
             request.setAttribute("hairprocess",hairprocess) ;
             request.setAttribute("hairstyles",hairstyles) ;
             request.setAttribute("allergies",allergies) ;
             request.setAttribute("natural",natural) ;
             
             
 		     
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
    public static void main(String[] a)
    {
    	HairAnalysisDetailAction aa = new HairAnalysisDetailAction();
  //  	aa.IngredientBenefits("301220261", "188533","51","T003","a combination of oily and dry","Excellent") ;
    	ArrayList prods = new ArrayList() ;
    	
    	prods.add("212303") ;
    	prods.add("339652") ;
    	prods.add("315851") ;
    	prods.add("226034") ;
    	
    	aa.getCustomerProductFeatures("301220263", prods) ;
    	
    }
    public ArrayList getCustomerProductFeatures(String customerId, ArrayList prods)
    {
		
    	ArrayList retList = new ArrayList() ;
		String repId     = "";
		String dname = "" ;
		String prid = "" ;
	    String pmtype =  "" ; 
	    String prodId = "" ;
	    String ptype = "" ;

	
	    Hashtable regimenprodtype       = new Hashtable();
	    Hashtable regimenusage          = new Hashtable();
		Hashtable productsHashtable 	= new Hashtable();
		Hashtable ingrratings		   	= new Hashtable();
		Hashtable fht				   	= new Hashtable();
		String ingrlist[][]             = new String[0][0] ;
        Hashtable ratings               = new Hashtable() ;
        ArrayList profiles              = new ArrayList() ;
        
        Hashtable hta =          new Hashtable();
        Hashtable bumpscores = new Hashtable();
        
		ProductVO product = new ProductVO();
		ProductVO pr = new ProductVO() ;
		CustomerProfileVO cp = new CustomerProfileVO() ;
        PopupRatings popRate = new PopupRatings() ;
        RecommendationEngineDebug recEngDebug = new RecommendationEngineDebug() ;
        HairRecommendationEngine recEng = new HairRecommendationEngine() ;
        HairProductRecommendationEngine precEng = new HairProductRecommendationEngine() ;
        SVDBOperation dbo = new SVDBOperation();


	    try{
		    
		    
		 try{
			
			  System.out.println("1111111111 "+customerId+"  prods size "+prods.size()) ;
	          dbo.createConnection();
			  recEng.setDBO(dbo);
			  
	          hta          = (Hashtable) recEng.getMasterDataOne() ;
              bumpscores      = (Hashtable) recEngDebug.getBumpScores() ;
			  cp = (CustomerProfileVO) recEng.getCustomerProfile(customerId,cp,"skin") ;
			  ingrratings = recEng.getIngredientsRatings(cp, false);
			  
	 	        ArrayList allergies       = cp.getAllergyList();
	     	    String haircolor = "" ;
	    	    String hairtype = "" ;
	    	    String hairthickness = "" ;
	    	    String hairtexture = "" ;
	    	    String hairfinish = "" ;
	    	    ArrayList hairprocess = new ArrayList() ;
	    	    ArrayList hairconditions = new ArrayList() ;
	    	    ArrayList hairstyles = new ArrayList() ;
	    	    ArrayList concerns = new ArrayList() ;
	            
	            haircolor = cp.getHaircolor() ;
	            hairtype = cp.getHairtype() ;
	            hairthickness = cp.getHairthickness() ;
	            hairtexture = cp.getHairtexture() ;
	            hairfinish = cp.getHairfinish() ;
	            
	            hairprocess = (ArrayList) cp.getHairprocess() ;
	            hairstyles = (ArrayList) cp.getHairstyles() ;
	            hairconditions = (ArrayList) cp.getHairconditions() ;
	            allergies = (ArrayList) cp.getAllergyList() ;

	             

	 	      ratings  = new Hashtable() ;
	 	      profiles = new ArrayList() ;
	 	      
	 	      System.out.println("  prods size "+prods.size()) ;
		      if (prods !=null && prods.size() > 0)
		      {
		    	 profiles = new ArrayList() ;
	 		     profiles.add(haircolor) ;
	 	 	     profiles.add(hairtype) ;
	 	 	     profiles.add(hairthickness) ;
	 	 	     profiles.add(hairtexture) ;
	 	         for (int j=0; j < hairconditions.size() ; j++)
	 	          {
	 	         	String scond = (String) hairconditions.get(j) ;
	 	         	profiles.add(scond) ;
	 	          }
	 	         for (int j=0; j < hairprocess.size() ; j++)
	 	          {
	 	         	String sconc = (String) hairprocess.get(j) ;
	 	         	profiles.add(sconc) ;
	 	          }
	 	         for (int j=0; j < hairstyles.size() ; j++)
	 	          {
	 	         	String econc = (String) hairstyles.get(j) ;
	 	         	profiles.add(econc) ;
	 	          }
	 	 	     profiles.add(hairfinish) ;
		         
	 	 	     for (int i =0; i < prods.size(); i++)
		         {
		        	
		        	
		    	    prodId = (String) prods.get(i) ;
			    	prid = prodId ;
				    prodId = "('"+prodId+"')" ;
				    
					  productsHashtable = (Hashtable) recEng.getHairProductInfo(prodId); //gets the product
//					  productsHashtable  = recEngDebug.getProductsIngredients("", "prodId", "",productsHashtable,prodId); //this method
//			          ingrlist = (String[][]) ((ProductVO)productsHashtable.get(prid)).getIngredients() ;
					  ingrratings = recEng.getManualRatings(cp, true);
					  System.out.println("ingr ratings size "+ingrratings.size()) ;
					  product = (ProductVO) productsHashtable.get(prid) ;
					  product = (ProductVO) recEng.ratingsIndividual(cp,product,ingrratings);
		            
				      pmtype = product.getMasterProdType() ;
    
		 	         
					 try{
			 	         
						 System.out.println(" profiles size inner try "+profiles.size()) ;
						 for (int j=0; j < profiles.size() ; j++)
			 	          {
			 	          	String profileAtt = (String) profiles.get(j) ;
			 	          	ratings = (Hashtable) getProductScore(cp, product, bumpscores, profileAtt, prid,ratings) ;        	 
			 	          }
					    }catch(Exception e){
					    	////logger.log(Level.DEBUG,e);
					    	e.printStackTrace();
					    }
		         } // for prods
		      } //if prods
	      
	  //    System.out.println(hta.get(profileAtt)+" "+scorehighlight) ;
		  
		} catch (Exception edb)
		{
			////logger.log(Level.DEBUG,edb) ;
			edb.printStackTrace();
		}
		finally
		{
			dbo.closeConnection() ;
		}
		  
		  System.out.println("vbbbbbbbb 66 ratings size  "+ratings.size()) ;
		  System.out.println("vbbbbbbbb 777 profiles size "+profiles.size()) ;
		  
		  Enumeration en = ratings.keys() ;
		  while (en.hasMoreElements())
		  {
			  String enkey = (String) en.nextElement();
			  String enval = (String) ratings.get(enkey) ;
			  System.out.println("dddddddd "+enkey+"  "+enval) ;
		  }
          for (int k=0 ; k < profiles.size(); k++)
          {
        	  System.out.println((String)hta.get(profiles.get(k))) ;
          }
          retList.add(hta) ;
          retList.add(profiles) ;
          retList.add(ratings) ;
          
	    }catch(Exception e){
	    	////logger.log(Level.DEBUG,e);
	    	e.printStackTrace();
	    }
        return retList ;
    }
    Hashtable getProductScore(CustomerProfileVO cp, ProductVO product, Hashtable bumpscores, String profileAtt, String prodId,Hashtable prodscores)
    {
    	
	    Hashtable pBaseRatings = new Hashtable();
 	    Hashtable pIngrRatings = new Hashtable();
        Hashtable pManRatings = new Hashtable();
      	
        
    	

      	int pbasescore = 0 ;
      	int pbasecount = 0 ;
      	int pbasepercen = 0 ;
      	
      	
	    String  pmtype             = product.getMasterProdType() ;
        String  ptype              = (String) product.getProdType() ;
        
      	String bumpkey    = profileAtt+ptype ;
      	String bscore  = (String) bumpscores.get(bumpkey) ;
 		System.out.println("bump score "+bumpkey+"  "+bscore) ;
      	
        
        pManRatings = (Hashtable) product.getProfileBaseRatings() ; 

      	if (pManRatings.get(profileAtt) != null)
      	{
      		String pbasescorestring = (String) pManRatings.get(profileAtt) ;
			StringTokenizer stp = new StringTokenizer(pbasescorestring,"::") ;
			String stps = stp.nextToken() ;
			String stpc = stp.nextToken() ;
			pbasescore =  Integer.parseInt(stps) ;
			pbasecount =  Integer.parseInt(stpc) ;
      		System.out.println("action base "+pbasescore+" "+pbasecount) ;
      	}
      	
			float basepercentage = 0.0f;


			if (pbasecount != 0)
				pbasepercen = 100 * pbasescore / (pbasecount * 10);

			float pbasepercentage = Float.parseFloat(String.valueOf(pbasepercen));
			float score = 0.0f;
            float bumpscore = 0.0f;
            if (bscore != null && bscore.length() != 0)
              bumpscore = Float.parseFloat(String.valueOf(bscore));
        
        
			  basepercentage = (pbasepercentage * 100f) / 100f;
			  score = basepercentage ;
			  score = score+bumpscore ;
			
 		      System.out.println("action score "+profileAtt+" "+score+" "+bumpscore) ;
              if (pmtype.equals("1009"))
	          {
         	    score = 100.0f ;
	          } 			
			
 		    System.out.println("action score "+profileAtt+" "+score+" "+bumpscore+" base "+basepercentage) ; 
			
 		    String scorehighlight = (String) getAnalysisRating(score) ;
 		
 		   if (!scorehighlight.equals("Not Ideal")) { 
 			   
 			   prodscores.put(profileAtt+prodId, scorehighlight) ;
 		   }
 		
						
			
      
    	
    	return prodscores;
    }
    String getAnalysisRating(float value)
    {
       String comment = "Not Ideal" ;
       float val = value ;
  /*     try
       {
          val = Float.parseFloat(value) ;
       } catch (Exception ex)
       {
          val = 0.0f ;
       } */
       if (val > 64.0f)
         comment = "Excellent" ;
       else if (val > 55.0f && val < 65.0f)
         comment = "Very Good" ;
       else if (val > 49.0f && val < 55.0f)
         comment = "Good" ;
       else if (val > 39.0f && val < 50.0f)
         comment = "Fair" ;
       else if (val < 40.0f)
         comment = "Not Ideal" ;
       
       System.out.println("value in method "+value+"  "+comment) ;
       return comment ;
    }
}