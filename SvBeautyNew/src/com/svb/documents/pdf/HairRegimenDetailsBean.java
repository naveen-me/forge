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
import com.svb.ratings.javabeans.HairProductRecommendationEngine;
import com.svb.ratings.javabeans.HairRecommendationEngine ;
import com.svb.ratings.javabeans.HairRecommendationEngineDebug;

/**
 * @author VBRao M
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class HairRegimenDetailsBean {

	public ArrayList getProductInfo(HttpServletRequest request)
			throws IOException, ServletException {

		ArrayList ret = new ArrayList() ;
		String customerId     = "";
		
		boolean prodrec = false ;
	    String finishprod = "" ;
	    String shampooprod = "" ;
	    String conditionerprod = "" ;
	    String htreatprod = "" ;
	    String stylingprod = "" ;
        String prodstring = "" ;
        ArrayList categprods = new ArrayList()  ;
        ArrayList dynamic = new ArrayList() ;
        Hashtable master  = new Hashtable() ;
        Hashtable prodhash = new Hashtable() ;
        String profilestring = "" ;
        String cThickness  =  "" ;
        String cTexture  =  "" ;
        Hashtable productratings = new Hashtable() ;
        Hashtable productusage = new Hashtable() ;
        String cHairstyle = "" ;
        
		CustomerProfileVO cp = new CustomerProfileVO() ;
        
		if (request.getParameter("customerId") != null)
			customerId = request.getParameter("customerId") ;
		
		HttpSession session = request.getSession(false);
		
        HairRecommendationEngine rec = new HairRecommendationEngine() ;
        HairRecommendationEngineDebug recEngDebug = new HairRecommendationEngineDebug() ;
        HairProductRecommendationEngine recEng = new HairProductRecommendationEngine() ;
		ConstantDeclarations cdl = new ConstantDeclarations() ;

        SVDBOperation dbo = new SVDBOperation();
        
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

			cp = (CustomerProfileVO) rec.getCustomerProfile(customerId,cp,"hair") ;
	        master = (Hashtable) rec.getMasterDataOne() ;
		    cThickness = (String) cp.getHairthickness()	;
		    cTexture = (String) cp.getHairtexture()	;
		    ArrayList hairstyles = (ArrayList) cp.getHairstyles() ;
		    
		    if (hairstyles.size() > 0 &&!((String)hairstyles.get(0)).equals("S999"))
		    {
		        for (int i=0; i < hairstyles.size() ; i++) 
		        {
		        	 String hs = (String)hairstyles.get(i) ;
		             if (hs.equals("S004"))
		             {
		             	cHairstyle = "S004" ;
		             	break ;
		             }
		             if (hs.equals("S005"))
		             {
		             	cHairstyle = "S005" ;
		             	break ;
		             }
		        } 
		    }

		    
			prodrec = true ;
			if (prodrec) {
                    
		            profilestring = (String) getCustomerProfileString(cp,master) ;
		            
				    prodhash = (Hashtable) rec.getHairProductInfo(prodstring) ;
				    prodhash  = recEngDebug.getProductsIngredients("", "prodId", "",prodhash,prodstring); //this method
		            dynamic = (ArrayList) recEng.getDynamicText(prodhash,cp,master) ;
		            productratings = (Hashtable) getProductRatings(cp, prodstring,master) ;
		            productusage = (Hashtable) recEngDebug.getProductUsage(prodstring) ;
			}
            ret.add(profilestring) ;
            ret.add(cThickness) ;
            ret.add(cTexture) ;
            ret.add(productratings) ;
            ret.add(productusage) ;
            ret.add(cHairstyle) ;
		
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
	    String haircolor = "" ;
	    String hairtype = "" ;
	    String hairthickness = "" ;
	    String hairtexture = "" ;
	    String hairconcern = "" ;
	    String natural = "" ;
	    String natural_1 = "" ;
	    String natural_2 = "" ;
	    String atext = "" ;
	    String thattext = "" ;
	    String concerntext = "" ;
	    String styletext = "" ;
	    String naturaltext = "" ;
	    ArrayList hairprocess = new ArrayList() ;
	    ArrayList hairconditions = new ArrayList() ;
	    ArrayList hairstyles = new ArrayList() ;
	    ArrayList concerns = new ArrayList() ;
	    ArrayList allergies = new ArrayList() ;
        
        haircolor = (String) master.get(cp.getHaircolor()) ;
        hairtype = (String) master.get(cp.getHairtype()) ;
        hairthickness = (String) master.get(cp.getHairthickness()) ;
        hairtexture = (String) master.get(cp.getHairtexture()) ;
        hairprocess = (ArrayList) cp.getHairprocess() ;
        hairstyles = (ArrayList) cp.getHairstyles() ;
        hairconditions = (ArrayList) cp.getHairconditions() ;
        allergies = (ArrayList) cp.getAllergyList() ;
        natural = (String) cp.getNatural() ;
        hairconcern = "" ;
      //  System.out.println("I am upto here ...."+hairtexture) ;
        if (hairtexture.equals("average"))
           atext = "an" ;
        else
           atext = "a" ;
        if (hairprocess.size() > 0 && !hairprocess.get(0).equals("H999"))
             haircolor = haircolor+", chemically treated"  ;
        for (int i=0; i < hairconditions.size() ; i++) 
        {
            concerns.add(hairconditions.get(i)) ;
        }

 /*       for (int i=0; i < hairstyles.size() ; i++) 
        {
            concerns.add(hairstyles.get(i)) ;
        }
        for (int i=0; i < hairprocess.size() ; i++) 
        {
            concerns.add(hairprocess.get(i)) ;
        }
  */      
      if (concerns.size() > 0 && !((String)concerns.get(0)).equals("R999") )
      {
        for (int i=0; i < concerns.size() ; i++) 
        {
            String ctext = "" ;
            ctext = "<strong>"+(String) master.get(concerns.get(i))+"</strong>" ;
            if (i == 0)
               hairconcern = hairconcern+ctext ;
            else if (i == concerns.size()-1)
            {
               //if (!((String)hairstyles.get(0)).equals("E999"))
                //  hairconcern = hairconcern+", "+ctext ;
               //else
                  hairconcern = hairconcern+" and "+ctext ;
            }
            else
               hairconcern = hairconcern+", "+ctext ;
        }
       }

       if (hairstyles.size() > 0 &&!((String)hairstyles.get(0)).equals("S999"))
       {
        for (int i=0; i < hairstyles.size() ; i++) 
        {
            
            String ctext1 = "" ;
             ctext1 = " as well as styling results that include " ;
            String ctext = "" ;
            ctext = "<strong>"+(String) master.get(hairstyles.get(i))+"</strong>" ;
            if (i == 0)
               styletext = styletext+ctext1+ctext ;
            else if (i == hairstyles.size()-1)
            {
                  styletext = styletext+" and "+ctext ;
            }
            else
               styletext = styletext+", "+ctext ;
        }
       }

       if (concerns.size() > 0 && !((String)concerns.get(0)).equals("R999"))
       {
         //System.out.println("1111sff");
         concerntext =  " The products selected address concerns of  " ;
       }
      thattext = " that is <strong>"+hairthickness+"</strong> in density and generally has "+atext+" <strong>"+hairtexture+"</strong> texture." ;
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
        
        basicstring = "<p>Based on your responses, we've selected the ideal products for <strong>"+haircolor+"</strong>, <strong>"+hairtype+"</strong> "+
                      "hair"+thattext+""+concerntext+""+hairconcern+"; "+styletext+". "+naturaltext+"</p>" ;
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
        
        HairRecommendationEngine rec = new HairRecommendationEngine() ;
        HairRecommendationEngineDebug recEngDebug = new HairRecommendationEngineDebug() ;
        HairProductRecommendationEngine recEng = new HairProductRecommendationEngine() ;
		ConstantDeclarations cdl = new ConstantDeclarations() ;
        
		productsHashtable = (Hashtable) recEng.getHairProductInfo(productstring); //gets the product
		productsHashtable  = recEngDebug.getProductsIngredients("", "prodId", "",productsHashtable,productstring); //this method
		ingrratings = recEng.getManualRatings(cp, true);
		products = recEng.getProductArray(products,productsHashtable);
	  //  System.out.println("vb prods 23 "+products.length) ;
	    productratings.put("products",productsHashtable) ;
	    
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
        
           
	    for (int i=0; i < products.length ; i++)
	    {
	    	 
	    	 product = (ProductVO) products[i] ;
             prodid  = (String) product.getProdId() ;
             profiles = new ArrayList() ;
             pmtype = (String) product.getMasterProdType() ;
             ptype              = (String) product.getProdType() ;
  	    	 ratings = new ArrayList() ;
 	         Hashtable pManRatings = new Hashtable();
    	     product = (ProductVO) recEng.ratingsIndividual(cp,product,ingrratings);
             
             
             if (pmtype.equals("1001") || pmtype.equals("1003"))
             {
      	         profiles.add(hairtype) ;
      	         profiles.add(hairthickness) ;
      	         profiles.add(hairtexture) ;
                   for (int k=0; k < hairprocess.size() ; k++)
                   {
            	       String sconc = (String) hairprocess.get(k) ;
            	      profiles.add(sconc) ;
                   }
             }
             if (pmtype.equals("1005"))
             {
               for (int k=0; k < hairconditions.size() ; k++)
                {
               	String scond = (String) hairconditions.get(k) ;
               	if (!scond.equals("R001") && !scond.equals("R008"))
               	  profiles.add(scond) ;
                }
             }
             if (pmtype.equals("1004"))
             {
               for (int k=0; k < hairstyles.size() ;k++)
                {
               	String econc = (String) hairstyles.get(k) ;
               	profiles.add(econc) ;
                }
             }
             if (pmtype.equals("1009"))
             {
              	profiles.add(hairfinish) ;
             }
      		   int profilesize = 7 ;
      		   
      		   if (allergies.size() > 0)
      	        if (!allergies.get(0).equals("L999"))  
      		   	  profilesize = profilesize - allergies.size() ;
      		     	  
      		   if (natural != null && natural.equals("A008"))
      		   	profilesize = profilesize - 1 ;
      		   
      		//   System.out.println("profile size "+profilesize+"  profiles size "+profiles.size()) ;

//      		   if (profilesize > profiles.size())
      	//	   	 profilesize = profiles.size() ;
                 int profsize = profilesize ;
             
             
	    	// System.out.println("  dsd  "+prodid+profilesize) ;
    		// System.out.println("1  "+prodid) ;
    		
	         	
	         pManRatings = (Hashtable) product.getProfileBaseRatings() ; 
	         Hashtable pratings  = new Hashtable() ;
	         
		   
	         for (int m=0; m < profiles.size() ; m++)
	         {
 	         	int pbasescore = 0 ;
 	         	int pbasecount = 0 ;
 	         	int pbasepercen = 0 ;
 	         	
 	         	
 	         	String profileAtt = (String) profiles.get(m) ;
 	         	String bumpkey    = profileAtt+ptype ;
 	         	String bscore  = (String) bumpscores.get(bumpkey) ;
         //		System.out.println("bump score "+bumpkey+"  "+bscore) ;
 	         	
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
 	      //   		System.out.println("action base "+pbasescore+" "+pbasecount) ;
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
 				
   	            if (pmtype.equals("1009"))
 	            {
	         	  score = 100.0f ;
 	            } 			
				
        //	   System.out.println("action score "+profileAtt+" "+score+" "+bumpscore) ;
        	 
        	   String attribstring = "" ;
			   String allergystring = "" ;
			   String naturalstring = "" ;
        	 
			   String attribrating = (String) getAnalysisRating(""+score) ;
	//		   System.out.println("ksdhkfhsdkhfs "+attribrating) ; 
			   
		   	   if (!attribrating.equals("Not Ideal"))
		   	   {
	//			   System.out.println("ksdhkfhsdkhfs 3 "+attribrating) ;
		   	   	  String attribtext = (String) master.get(profileAtt) ;
		            if (profileAtt.equals("Y001"))
		            	attribtext = "dry hair" ;
		            if (profileAtt.equals("Y002"))
		            	attribtext = "oily hair" ;
		            if (profileAtt.equals("Y003"))
		            	attribtext = "normal hair" ;
		            if (profileAtt.equals("I003"))
		            	attribtext = "average (thickness)" ;
		            if (profileAtt.equals("X003"))
		            	attribtext = "average (texture)" ;
			      attribstring = "<strong>"+attribrating+"</strong> for <strong>"+attribtext+"</strong>" ;
		   	   }
		   	   else
		   	   {
	//			   System.out.println("ksdhkfhsdkhfs 5 "+attribrating) ; 
				   attribrating = "not" ;
		   	   }
		//	   System.out.println("cbb "+prodid+" "+profileAtt+"  "+m+"  "+profilesize+" "+attribstring) ;
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
  // 	   System.out.println("inside method "+value) ;
	   String comment = "Not Ideal" ;
	   float val = 0.0f ;
	   try
	   {
	      val = Float.parseFloat(value) ;
	   } catch (Exception ex)
	   {
	      val = 0.0f ;
	   }
	   if (val > 89.0f)
	     comment = "Excellent" ;
	   else if (val > 69.0f && val < 90.0f)
	     comment = "Very Good" ;
	   else if (val > 59.0f && val < 70.0f)
	     comment = "Very Good" ;
	   else if (val > 49.0f && val < 60.0f)
	     comment = "Good" ;
	   else if (val < 50.0f)
	     comment = "Not Ideal" ;
	   
//	   System.out.println("value in method "+value+"  "+comment) ;
	   return comment ;
	}
}