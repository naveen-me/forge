/*
 * Created on Oct 11, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.documents.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


import com.svb.common.valueobjects.ProductVO;
import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.valueobjects.* ;
import com.svb.ratings.javabeans.ProductRecommendationEngine;
import com.svb.ratings.javabeans.RecommendationEngine ;
import com.svb.ratings.javabeans.RecommendationEngineDebug;

/**
 * @author VBRao M
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RegimenDetailsBean {

	public ArrayList getProductInfo(HttpServletRequest request)
			throws IOException, ServletException {

		ArrayList ret = new ArrayList() ;
		String customerId     = "";
		
		boolean prodrec = false ;
	    String spfprod = "" ;
	    String cleanseprod = "" ;
	    String moisturizeprod = "" ;
	    String treatprod = "" ;
	    String etreatprod = "" ;
        String prodstring = "" ;
        ArrayList categprods = new ArrayList()  ;
        ArrayList dynamic = new ArrayList() ;
        Hashtable master  = new Hashtable() ;
        Hashtable prodhash = new Hashtable() ;
        String profilestring = "" ;
        String cAge  =  "" ;
        Hashtable productratings = new Hashtable() ;
        Hashtable productusage = new Hashtable() ;
        
		CustomerProfileVO cp = new CustomerProfileVO() ;
        
		if (request.getParameter("customerId") != null)
			customerId = request.getParameter("customerId") ;
		
		HttpSession session = request.getSession(false);
		
        RecommendationEngine rec = new RecommendationEngine() ;
        RecommendationEngineDebug recEngDebug = new RecommendationEngineDebug() ;
        ProductRecommendationEngine recEng = new ProductRecommendationEngine() ;
		ConstantDeclarations cdl = new ConstantDeclarations() ;

        SVDBOperation dbo = new SVDBOperation();
        
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
        if (request.getParameter("spfprod") != null)
        {
        	spfprod = request.getParameter("spfprod") ;
        	categprods.add(spfprod) ;
        }
        
              
        prodstring = "(" ;
        
        for (int i=0 ; i < categprods.size(); i++)
        {
        	if (i == categprods.size()-1)
        	  prodstring = prodstring+"'"+(String)categprods.get(i)+"'"+")" ;
        	else
          	  prodstring = prodstring+"'"+(String)categprods.get(i)+"'"+"," ;
        }

        
		try {

			cp = (CustomerProfileVO) rec.getCustomerProfile(customerId,cp,"skin") ;
	        master = (Hashtable) rec.getMasterDataOne() ;
		    cAge = (String) cp.getAge()	;
			prodrec = true ;
			if (prodrec) {
                    
		            profilestring = (String) getCustomerProfileString(cp,master) ;
		            
				    prodhash = (Hashtable) rec.getBeautyProductInfo(prodstring) ;
				    prodhash  = recEngDebug.getProductsIngredients("", "prodId", "",prodhash,prodstring, false); //this method
		            dynamic = (ArrayList) recEng.getDynamicText(prodhash,cp,master) ;
		            productratings = (Hashtable) getProductRatings(cp, prodstring,master) ;
		            productusage = (Hashtable) recEngDebug.getProductUsage(prodstring) ;
			}
            ret.add(profilestring) ;
            ret.add(cAge) ;
            ret.add(dynamic) ;
            ret.add(productratings) ;
            ret.add(productusage) ;
		
		} catch(Exception e) {
			e.printStackTrace() ;
		}
		finally {
		}
		return ret ;
	}
	
	public String getCustomerProfileString(CustomerProfileVO cp, Hashtable master)
	{
		String basicstring = "" ;
        String age     = "" ;
        String skintype = "" ;
        String skinconcern = "" ;
        String skincondition = "" ;
        String natural = "" ;
        String thattext = "" ;
        String naturaltext = "" ;
        ArrayList skinconcerns = new ArrayList() ;
        ArrayList skinconditions = new ArrayList() ;
        String eyeconcern = "" ;
        ArrayList eyeconcerns = new ArrayList() ;
        ArrayList concerns = new ArrayList() ;
        ArrayList allergies = new ArrayList() ;
        
        skintype = (String) master.get(cp.getSkinType()) ;
        skinconcerns = (ArrayList) cp.getSkinConcernList() ;
        eyeconcerns = (ArrayList) cp.getEyeConcernList() ;
        skinconditions = (ArrayList) cp.getSkinConditionList() ;
        allergies = (ArrayList) cp.getAllergyList() ;
        natural = (String) cp.getNatural() ;
        skinconcern = "" ;
        
        for (int i=0; i < skinconcerns.size() ; i++) 
        {
            concerns.add(skinconcerns.get(i)) ;
        }
        for (int i=0; i < skinconditions.size() ; i++) 
        {
            concerns.add(skinconditions.get(i)) ;
        }
        
        if (concerns.size() > 0 && !((String)concerns.get(0)).equals("C999"))
        {
          for (int i=0; i < concerns.size() ; i++) 
          {
            String ctext = "" ;

            ctext = "<strong>"+(String) master.get(concerns.get(i))+"</strong>" ;
            if (i == 0)
               skinconcern = skinconcern+ctext ;
            else if (i == concerns.size()-1)
            {
               if (!((String)eyeconcerns.get(0)).equals("E999"))
                  skinconcern = skinconcern+", "+ctext ;
               else
                  skinconcern = skinconcern+" and "+ctext ;
            }
            else
               skinconcern = skinconcern+", "+ctext ;
        }
       }
       if (eyeconcerns.size() > 0 &&!((String)eyeconcerns.get(0)).equals("E999"))
       {
        for (int i=0; i < eyeconcerns.size() ; i++) 
        {
            
            String ctext1 = "" ;
            if (!((String) concerns.get(0)).equals("C999"))
             ctext1 = " and eye concerns of " ;
            else
             ctext1 = " eye concerns of " ;
            String ctext = "" ;
            ctext = "<strong>"+(String) master.get(eyeconcerns.get(i))+"</strong>" ;
            if (i == 0)
               skinconcern = skinconcern+ctext1+ctext ;
            else if (i == eyeconcerns.size()-1)
            {
                  skinconcern = skinconcern+" and "+ctext ;
            }
            else
               skinconcern = skinconcern+", "+ctext ;
        }
       }

       if (concerns.size() > 0 && ((String)concerns.get(0)).equals("C999") && !((String)eyeconcerns.get(0)).equals("E999"))
       {
         //System.out.println("1111sff");
         thattext =  " that address " ;
       }
       else if (concerns.size() > 0 && !((String)concerns.get(0)).equals("C999") && ((String)eyeconcerns.get(0)).equals("E999"))
       {
         //System.out.println("2222sff");
         thattext =  " that address " ;
       }
       else if (concerns.size() > 0 && !((String)concerns.get(0)).equals("C999") && !((String)eyeconcerns.get(0)).equals("E999"))
       {
         //System.out.println("333sff");
         thattext =  " that address " ;
       }
       else 
       {
         //System.out.println("4444sff");
         thattext =  " " ;
       } 

        if (natural.equals("A008"))
        {
           //System.out.println("I am in if natural") ;
           if (allergies.size() > 0 &&  !((String)allergies.get(0)).equals("L999"))
           {
              //System.out.println("I am in if natural yes allergies") ;
              naturaltext = "Your results also include products with <strong>natural and organic </strong>ingredients" ;
              for (int i=0; i < allergies.size() ; i++) 
              {
            
                 String ctext = "" ;
                 ctext = "<strong>"+(String) master.get(allergies.get(i))+"</strong>" ;
                 if (i == 0)
                 {
                   if (allergies.size() > 1)
                      naturaltext = naturaltext+" and that do not contain "+ctext ;
                   else
                      naturaltext = naturaltext+" and that do not contain "+ctext+"." ;
                 }
                 else if (i == allergies.size()-1)
                 {
                  naturaltext = naturaltext+" or "+ctext+"." ;
                 }
                 else
                  naturaltext = naturaltext+", "+ctext+"" ;
              }
           }
           else
           {
              //System.out.println("I am in if natural no allergies") ;
              naturaltext = "Your results also include products with <strong>natural and organic </strong>ingredients." ;
           }
        }
        else
        {  
           //System.out.println("I am in else natural") ;
           naturaltext = "" ;
           if (allergies.size() > 0 && !((String)allergies.get(0)).equals("L999"))
           {
              //System.out.println("I am in else natural yes allergies") ;
              naturaltext = "Your results also include products that do not contain " ;
              for (int i=0; i < allergies.size() ; i++) 
              {
            
                 String ctext = "" ;
                 ctext = "<strong>"+(String) master.get(allergies.get(i))+"</strong>" ;
                 if (i == 0)
                   if (allergies.size() > 1)
                     naturaltext = naturaltext+ctext ;
                   else
                     naturaltext = naturaltext+ctext+"." ;
                 else if (i == allergies.size()-1)
                 {
                  naturaltext = naturaltext+" and "+ctext+"." ;
                 }
                 else
                  naturaltext = naturaltext+", "+ctext+"" ;
              }
           }
           else
           {
              //System.out.println("I am in else natural no allergies") ;
              naturaltext = "" ;
           }
        }
        
        basicstring = "<p>Based on your responses, we've selected the ideal products for <strong>"+skintype+"</strong> "+
                      "skin"+thattext+""+skinconcern+". "+naturaltext+"</p>" ;
		return basicstring ;
	}
	public Hashtable getProductRatings(CustomerProfileVO cp, String productstring, Hashtable master)
	{
		Hashtable productratings = new Hashtable() ;
		ArrayList ratings = new ArrayList() ;
		Hashtable ratings0 = new Hashtable() ;
        Hashtable productsHashtable = new Hashtable() ;
        Hashtable ingrratings		   	= new Hashtable();
		ProductVO products[] = new ProductVO[0];
		ProductVO product = new ProductVO() ;
        String pmtype = "" ;
        String prodid = "" ;
        
        ArrayList profiles = new ArrayList() ;
        
        RecommendationEngine rec = new RecommendationEngine() ;
        RecommendationEngineDebug recEngDebug = new RecommendationEngineDebug() ;
        ProductRecommendationEngine recEng = new ProductRecommendationEngine() ;
		ConstantDeclarations cdl = new ConstantDeclarations() ;
        
		productsHashtable = (Hashtable) recEng.getBeautyProductInfo(productstring); //gets the product
		productsHashtable  = recEngDebug.getProductsIngredients("", "prodId", "",productsHashtable,productstring, false); //Single changes Struts
		ingrratings = recEng.getIngredientsRatings(cp, false);
		products = recEng.getProductArray(products,productsHashtable);
	    System.out.println("vb prods 23 "+products.length) ;
	    productratings.put("products",productsHashtable) ;
	    
        String skintype        = cp.getSkinType() ;
        String age             = cp.getAge() ;
        String natural         = cp.getNatural() ;
        ArrayList skincondition   = cp.getSkinConditionList() ;
        ArrayList skinconcern     = cp.getSkinConcernList() ;
        ArrayList eyeconcern      = cp.getEyeConcernList() ;
        ArrayList allergies       = cp.getAllergyList();
        
        Hashtable bumpscores      = (Hashtable) recEngDebug.getBumpScores() ;
        
        profiles.add(skintype) ;
        if (!pmtype.equals("0009"))
         {
          for (int m=0; m < skincondition.size() ; m++)
          {
         	String scond = (String) skincondition.get(m) ;
         	profiles.add(scond) ;
          }
          for (int m=0; m < skinconcern.size() ; m++)
          {
         	String sconc = (String) skinconcern.get(m) ;
         	profiles.add(sconc) ;
          }
          for (int m=0; m < eyeconcern.size() ; m++)
          {
         	String econc = (String) eyeconcern.get(m) ;
         	if (econc.equals("E999"))
             break ;
         	profiles.add(econc) ;
          }
         }

		   int profilesize = 7 ;
		   
		   if (allergies.size() > 0)
	        if (!allergies.get(0).equals("L999"))  
		   	  profilesize = profilesize - allergies.size() ;
		     	  
		   if (natural != null && natural.equals("A008"))
		   	profilesize = profilesize - 1 ;
		   
		   System.out.println("profile size "+profilesize+"  profiles size "+profiles.size()) ;

//		   if (profilesize > profiles.size())
	//	   	 profilesize = profiles.size() ;
           int profsize = profilesize ;
           
	    for (int i=0; i < products.length ; i++)
	    {
	    	 
	    	 product = (ProductVO) products[i] ;
             prodid  = (String) product.getProdId() ;
	    	 System.out.println("  dsd  "+prodid+profilesize) ;
            
    	     product = (ProductVO) recEngDebug.ratingsIndividual(cp,product,ingrratings);
            
    		 System.out.println("1  "+prodid) ;
    		
             pmtype = product.getMasterProdType() ;
            
             String ptype              = (String) product.getProdType() ;
 	    	 ratings = new ArrayList() ;
	         Hashtable pBaseRatings = new Hashtable();
	         Hashtable pIngrRatings = new Hashtable();
	         	
	         pBaseRatings = (Hashtable) product.getProfileBaseRatings() ; 
	         pIngrRatings = (Hashtable) product.getProfileIngrRatings() ;
	         Hashtable pratings  = new Hashtable() ;
	         
		   
	         for (int m=0; m < profiles.size() ; m++)
	         {
	         	int pbasescore = 0 ;
	         	int pbasecount = 0 ;
	         	int pbasepercen = 0 ;
	         	
	         	int pingrscore = 0 ;
	         	int pingrcount = 0 ;
	         	int pingrpercen = 0 ;
	         	
	         	String profileAtt = (String) profiles.get(m) ;
	         	String bumpkey    = profileAtt+ptype ;
	         	String bscore  = (String) bumpscores.get(bumpkey) ;
        //		System.out.println("bump score "+bumpkey+"  "+bscore) ;
	         	
	         	if (pBaseRatings.get(profileAtt) != null)
	         	{
	         		String pbasescorestring = (String) pBaseRatings.get(profileAtt) ;
					StringTokenizer stp = new StringTokenizer(pbasescorestring,"::") ;
					String stps = stp.nextToken() ;
					String stpc = stp.nextToken() ;
					pbasescore =  Integer.parseInt(stps) ;
					pbasecount =  Integer.parseInt(stpc) ;
//	         		pbasescore = Integer.parseInt(pbasescorestring) ;
//	         		pbasecount = 1 ;
	        // 		System.out.println("action base "+pbasescore+" "+pbasecount) ;
	         	}
	         	if (pIngrRatings.get(profileAtt) != null)
	         	{
	         		String pingrscorestring = (String) pIngrRatings.get(profileAtt) ;
					StringTokenizer stp = new StringTokenizer(pingrscorestring,"::") ;
					String stps = stp.nextToken() ;
					String stpc = stp.nextToken() ;
					pingrscore =  Integer.parseInt(stps) ;
					pingrcount =  Integer.parseInt(stpc) ;
	      //   		System.out.println("action ingr "+pingrscore+" "+pingrcount) ;
	         	}
	         	
				float ingrpercentage = 0.0f;
				float basepercentage = 0.0f;

				if (pingrcount != 0)
					pingrpercen = 100 * pingrscore / (pingrcount * 10);

				if (pbasecount != 0)
					pbasepercen = 100 * pbasescore / (pbasecount * 10);

	         		System.out.println("action ingrpercen "+pingrpercen) ;
				float pingrpercentage = Float.parseFloat(String.valueOf(pingrpercen));
				float pbasepercentage = Float.parseFloat(String.valueOf(pbasepercen));
				float score = 0.0f;
                float bumpscore = 0.0f;
                if (bscore != null && bscore.length() != 0)
                  bumpscore = Float.parseFloat(String.valueOf(bscore));
               
               
                if (pmtype.equals("0004"))
                {
  				  ingrpercentage = (pingrpercentage * 100f) / 100f;
				  score = ingrpercentage ;
               }
               else
               {
  				  basepercentage = (pbasepercentage * 20f) / 100f;
				  ingrpercentage = (pingrpercentage * 80f) / 100f;
        		  System.out.println("action ingrpercentage "+profileAtt+" "+pingrpercentage+" "+bumpscore) ;
				  score = ingrpercentage+basepercentage ;
               }
			   score = score+bumpscore ;
				
        	   System.out.println("action score "+profileAtt+" "+score+" "+bumpscore) ;
        	 
        	   String attribstring = "" ;
			   String allergystring = "" ;
			   String naturalstring = "" ;
        	 
			   String attribrating = (String) getAnalysisRating(""+score) ;
	//		   System.out.println("ksdhkfhsdkhfs "+attribrating) ; 
			   
		   	   if (!attribrating.equals("Not Ideal"))
		   	   {
	//			   System.out.println("ksdhkfhsdkhfs 3 "+attribrating) ; 
			      attribstring = "<strong>"+attribrating+"</strong> for <strong>"+master.get(profileAtt)+"</strong>" ;
		   	   }
		   	   else
		   	   {
	//			   System.out.println("ksdhkfhsdkhfs 5 "+attribrating) ; 
				   attribrating = "not" ;
		   	   }
			   System.out.println("cbb "+prodid+" "+profileAtt+"  "+m+"  "+profilesize+" "+attribstring) ;
			   if (m < profilesize)
			   	if (!attribrating.equals("not"))
			   	{
	//		   	   System.out.println("dfkdskh "+attribrating) ;
//			   	   profilesize = profilesize-1 ;
			       ratings.add(attribstring) ;
			   	}
	    } //end inside for profiles
			   int allergysize = allergies.size() ;
			   
		       for (int n=0; n < allergysize ; n++) {
		        if (!allergies.get(n).equals("L999")) { 
		          String allergystring = "Does not contain "+master.get(allergies.get(n))+" ";
				    ratings.add(allergystring) ;
		        } 
		       }
		     if (natural != null && natural.equals("A008")) { 
		      String naturalstring = "Formulation is Natural/Botanical" ;
			    ratings.add(naturalstring) ;
		     } 
//	    System.out.println("ratings "+prodid+" "+ratings.size()) ;
	    productratings.put(prodid,ratings) ;
	 } // end of for products
	    
	 return productratings;
	 
	} // end of method
	
   public String getAnalysisRating(String value)
   {
   	   System.out.println("inside method "+value) ;
	   String comment = "Not Ideal" ;
	   float val = 0.0f ;
	   try
	   {
	      val = Float.parseFloat(value) ;
	   } catch (Exception ex)
	   {
	      val = 0.0f ;
	   }
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
	   
//	   System.out.println("value in method "+value+"  "+comment) ;
	   return comment ;
	}
}