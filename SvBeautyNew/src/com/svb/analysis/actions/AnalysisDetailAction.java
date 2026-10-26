/*
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
import javax.servlet.http.HttpSession;

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

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AnalysisDetailAction extends DispatchAction {

	

	private static Log log = LogFactory.getLog(AnalysisDetailAction.class);
	
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
        /**
         * Added by Synoptek(29-11-2019)
         * */ 
      //------------------------Start--------------------------- 
        Hashtable ingredientPercentage = new Hashtable() ;
        Hashtable basePercentage       = new Hashtable() ;
        Hashtable bumpScore            = new Hashtable() ;
	 //-------------------------End---------------------------
        ArrayList profiles             = new ArrayList() ;
        
		ProductVO product = new ProductVO();
		ProductVO pr = new ProductVO() ;
		CustomerProfileVO cp = new CustomerProfileVO() ;
        PopupRatings popRate = new PopupRatings() ;
        RecommendationEngineDebug recEngDebug = new RecommendationEngineDebug() ;
        RecommendationEngine recEng = new RecommendationEngine() ;
        ProductRecommendationEngine precEng = new ProductRecommendationEngine() ;
		
		ProductAnalysisForm analysisForm = (ProductAnalysisForm)form;
        SVDBOperation dbo = new SVDBOperation();
	    try{
	    	if (request.getParameter("customerId") != null)
			       customerId = request.getParameter("customerId") ;
	    	if (request.getParameter("productId") != null)
			       prodId = request.getParameter("productId") ;
	    	prid = prodId ;
		    prodId = "('"+prodId+"')" ;
		    
		//	HttpSession session = request.getSession(false);
		//	String customerId = (String) session.getAttribute("cusid") ; 
			CustomerTrackBean ctb = new CustomerTrackBean() ;
		//	if(request.getParameter("scrname")!=null){
				ctb.insertCustomerTrack("Skin",customerId,"whyrec","Why Rec",prid,"") ;	
		//	}
		    
		try{
			
          dbo.createConnection();
		  recEng.setDBO(dbo);
		  
		  cp = (CustomerProfileVO) recEng.getCustomerProfile(customerId,cp,"skin") ;

		  productsHashtable = (Hashtable) recEng.getBeautyProductInfo(prodId); //gets the product
		  productsHashtable  = recEngDebug.getProductsIngredients("", "prodId", "",productsHashtable,prodId, false); //Single changes Struts
//          ingrlist = (String[][]) ((ProductVO)productsHashtable.get(prid)).getIngredients() ;
		  ingrratings = recEng.getIngredientsRatings(cp, false);
		 // log.debug("ingrratings "+ ingrratings);
		  product = (ProductVO) productsHashtable.get(prid) ;
		  product = (ProductVO) recEngDebug.ratingsIndividual(cp,product,ingrratings);
		  
		  
		  
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
             String skintype        = cp.getSkinType() ;
             String age             = cp.getAge() ;
 	         ArrayList skincondition   = cp.getSkinConditionList() ;
             ArrayList skinconcern     = cp.getSkinConcernList() ;
             ArrayList eyeconcern      = cp.getEyeConcernList() ;
 	         ArrayList allergies       = cp.getAllergyList(); 
             String ptype              = (String) product.getProdType() ;
             Hashtable bumpscores      = (Hashtable) recEngDebug.getBumpScores() ;
             
 	         profiles.add(skintype) ;
 	         if (!pmtype.equals("0009"))
 	         {
 	          for (int i=0; i < skincondition.size() ; i++)
 	          {
 	         	String scond = (String) skincondition.get(i) ;
 	         	profiles.add(scond) ;
 	          }
 	          for (int i=0; i < skinconcern.size() ; i++)
 	          {
 	         	String sconc = (String) skinconcern.get(i) ;
 	         	profiles.add(sconc) ;
 	          }
 	          for (int i=0; i < eyeconcern.size() ; i++)
 	          {
 	         	String econc = (String) eyeconcern.get(i) ;
 	         	if (econc.equals("E999"))
 	             break ;
 	         	profiles.add(econc) ;
 	          }
 	         }
	         Hashtable pBaseRatings = new Hashtable();
 	         Hashtable pIngrRatings = new Hashtable();
 	         	
 	         pBaseRatings = (Hashtable) product.getProfileBaseRatings() ; 
 	         pIngrRatings = (Hashtable) product.getProfileIngrRatings() ;
 	         ratings  = new Hashtable() ;
 	         
 	         for (int i=0; i < profiles.size() ; i++)
 	         {
 	         	int pbasescore = 0 ;
 	         	int pbasecount = 0 ;
 	         	int pbasepercen = 0 ;
 	         	
 	         	int pingrscore = 0 ;
 	         	int pingrcount = 0 ;
 	         	int pingrpercen = 0 ;
 	         	
 	         	String profileAtt = (String) profiles.get(i) ;
 	         	String bumpkey    = profileAtt+ptype ;
 	         	String bscore  = (String) bumpscores.get(bumpkey) ;
         		System.out.println("bump score "+bumpkey+"  "+bscore) ;
 	         	
 	         	if (pBaseRatings.get(profileAtt) != null)
 	         	{
 	         		String pbasescorestring = (String) pBaseRatings.get(profileAtt) ;
					StringTokenizer stp = new StringTokenizer(pbasescorestring,"::") ;
					String stps = stp.nextToken() ;
					String stpc = stp.nextToken() ;
					pbasescore =  Integer.parseInt(stps) ;
					pbasecount =  Integer.parseInt(stpc) ;
// 	         		pbasescore = Integer.parseInt(pbasescorestring) ;
 //	         		pbasecount = 1 ;
 	         		System.out.println("action base "+pbasescore+" "+pbasecount) ;
 	         	}
 	         	if (pIngrRatings.get(profileAtt) != null)
 	         	{
 	         		String pingrscorestring = (String) pIngrRatings.get(profileAtt) ;
					StringTokenizer stp = new StringTokenizer(pingrscorestring,"::") ;
					String stps = stp.nextToken() ;
					String stpc = stp.nextToken() ;
					pingrscore =  Integer.parseInt(stps) ;
					pingrcount =  Integer.parseInt(stpc) ;
 	         		System.out.println("action ingr "+pingrscore+" "+pingrcount) ;
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
         		    System.out.println("I am in else vbbbbb "+pmtype) ;
                  if (pbasepercentage == 0.0f)
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
                }
 				score = score+bumpscore ;
 				
         		System.out.println("action score "+profileAtt+" "+score+" "+bumpscore+" base "+basepercentage+" ingr "+ingrpercentage) ; 
 				
 				// log.debug("1- Profile Attribute == "+ profileAtt);
         		// log.debug("2-Ingredient Percentage =="+ingrpercentage);
         		// log.debug("3- base Percentage =="+ basepercentage);
 				// log.debug("4- bump Score =="+ bumpscore);
         		// log.debug("5- score "+ score);
         		
         		/**
                 * Added by Synoptek(29-11-2019)
                 * */ 
              //------------------------Start--------------------------- 
         		ingredientPercentage.put(profileAtt,""+ingrpercentage) ;
         		basePercentage.put(profileAtt,""+basepercentage) ;
         		bumpScore.put(profileAtt,""+bumpscore) ;
         	 //-------------------------End---------------------------
 				ratings.put(profileAtt,""+score) ;
 	         }
 	         
	         request.setAttribute("hta",hta) ;
	         request.setAttribute("singleproduct",product) ;
	         request.setAttribute("singlecustomer",cp) ;
	         request.setAttribute("customerId",customerId) ;
	         
	         /**
              * Added by Synoptek(29-11-2019)
              * */ 
           //------------------------Start---------------------------  	         
	         request.setAttribute("ingredientPercentage",ingredientPercentage) ;
	         request.setAttribute("basePercentage",basePercentage) ;
	         request.setAttribute("bumpScore",bumpScore) ;
	      //--------------------------End----------------------------	   	  
	         
	         request.setAttribute("ratings",ratings) ;
             request.setAttribute("skintype",skintype) ;
             request.setAttribute("age",age) ;
             request.setAttribute("skincondition",skincondition) ;
             request.setAttribute("skinconcern",skinconcern) ;
             request.setAttribute("allergies",allergies) ;
             
             
 		     
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
    	  String pmtype2 = "" ;
    	  pmtype2 = pmtype ;
    	  if (pmtype.equals("0004"))
    	  	pmtype2 = "0005" ;
          System.out.println("vb5 funcstring "+fht.size()+" ingrlist "+ingrlist.length+"  "+prod.getProdName()) ;
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
                
                boolean mbasecheck = false ;
      			String basekey = "01"+"-"+pbase+"-NA-"+pmtype2 ;
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
      				System.out.println("base score "+basescore) ;
      				basepercentage = 100 * basescore / (basecount * 10);
      				basepercentage = (basepercentage * 20) / 100;
                }
          		
                
          	    boolean cleanseflag = false ;
          	    boolean faceflag = false ;
          	    boolean eyeflag = false ;
          	    boolean ratingsflag = true ;
          	    
          	    if (pmtype.equals("0005"))
          	    	faceflag = true ;
          	    if (pmtype.equals("0004"))
          	    	eyeflag = true ;
          	    if (pmtype.equals("0001"))
          	    	cleanseflag = true ;
          	    System.out.println("cleanse "+cleanseflag+" face "+faceflag+"  eye "+eyeflag) ;
				if (faceflag
						&& ((key21.trim().equals("C001")
								|| key21.trim().equals("C002")
								|| key21.trim().equals("E001")
								|| key21.trim().equals("E004")
							 ) 
							))
				{
					System.out.println("I am in faceflag vb "+pmtype+"  key21") ;
					ratingsflag = false;
				}
				if (eyeflag
						&& ( (key21.trim().equals("C022")
								|| key21.trim().equals("C003")
								|| key21.trim().equals("C020")
								|| key21.trim().equals("C004")
								|| key21.trim().equals("C023")
								|| key21.trim().equals("C012")
								|| key21.trim().equals("C019")
								|| key21.trim().equals("C007")
							 ) 
							))
				 {
					System.out.println("I am in eyeflag vb "+pmtype+"  key21") ;
					ratingsflag = false;
				 }
          		 if (cleanseflag &&  (key21.trim().equals("C001")
						|| key21.trim().equals("C002")
						|| key21.trim().equals("E001")
						|| key21.trim().equals("E004"))
				   )
          		 {
					System.out.println("I am in cleanseflag vb "+pmtype+"  key21") ;
                   ratingsflag = false; ; 
          		 }
          		 
				System.out.println("ratings falg "+ratingsflag+" "+key21+ " "+pmtype) ;
			if (ratingsflag)
			{	

				for (int i=0; i < ingrlist.length; i++)
          		{
          			boolean mtypecheck = false ;
          			String ingrkey = "02"+"-"+ingrlist[i][0]+"-NA-"+pmtype2 ;
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
			}
          		if (ingrcount != 0)
                {
      				   System.out.println("ingr score "+ingrscore) ;
          				ingrpercentage = 100 * ingrscore / (ingrcount * 10);
          				ingrpercentage = (ingrpercentage * 80) / 100;
          				int percentage = basepercentage+ingrpercentage ;
      				    System.out.println("percentage "+percentage) ;
          				ratings.put(key21,""+percentage) ;
                }
          }
       
         System.out.println("vvv "+ratings.size()) ;

    	return ratings;
    }
    public static void main(String[] a)
    {
    	AnalysisDetailAction aa = new AnalysisDetailAction();
  //  	aa.IngredientBenefits("301220261", "188533","51","T003","a combination of oily and dry","Excellent") ;
    	ArrayList prods = new ArrayList() ;
    	ArrayList cfeatures = new ArrayList() ;
    	
    	prods.add("151188") ;
    	prods.add("210809") ;
    	prods.add("141894") ;
    	prods.add("86830") ;
    	
    		
    	cfeatures = (ArrayList) aa.getCustomerProductFeatures("301220361", prods) ;
    	
    	Hashtable hta1 = (Hashtable) cfeatures.get(0) ;
    	ArrayList features1 = (ArrayList) cfeatures.get(1) ;
    	Hashtable ratings1 = (Hashtable) cfeatures.get(2) ;
    	
    	
    
    }
    public ArrayList getCustomerProductFeatures(String customerId, ArrayList prods)
    {
    	ArrayList retList = new ArrayList() ;
    	String target = "success";	
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
        RecommendationEngine recEng = new RecommendationEngine() ;
        ProductRecommendationEngine precEng = new ProductRecommendationEngine() ;
        SVDBOperation dbo = new SVDBOperation();
        

	    try{
		    
		    
		 try{
			
	          dbo.createConnection();
			  recEng.setDBO(dbo);
			  
	          hta          = (Hashtable) recEng.getMasterDataOne() ;
              bumpscores      = (Hashtable) recEngDebug.getBumpScores() ;
			  cp = (CustomerProfileVO) recEng.getCustomerProfile(customerId,cp,"skin") ;
			  ingrratings = recEng.getIngredientsRatings(cp, false);
			  
	           String skintype        = cp.getSkinType() ;
	           String age             = cp.getAge() ;
	 	       ArrayList skincondition   = cp.getSkinConditionList() ;
	           ArrayList skinconcern     = cp.getSkinConcernList() ;
	           ArrayList eyeconcern      = cp.getEyeConcernList() ;
	 	       ArrayList allergies       = cp.getAllergyList();

	             
	 	          profiles.add(skintype) ;
	 	         
	 	          for (int i=0; i < skincondition.size() ; i++)
	 	          {
	 	         	String scond = (String) skincondition.get(i) ;
	 	         	profiles.add(scond) ;
	 	          }
	 	          for (int i=0; i < skinconcern.size() ; i++)
	 	          {
	 	         	String sconc = (String) skinconcern.get(i) ;
	 	         	profiles.add(sconc) ;
	 	          }
	 	          for (int i=0; i < eyeconcern.size() ; i++)
	 	          {
	 	         	String econc = (String) eyeconcern.get(i) ;
	 	         	if (econc.equals("E999"))
	 	             break ;
	 	         	profiles.add(econc) ;
	 	          }
	 	          
	 	         //if (!pmtype.equals("0009"))
	 	         //{	 	         }
	 	         
              System.out.println("prods size "+prods.size()) ;
              for (int i=0; i < prods.size(); i++)
              {
            	  System.out.println("fffff  ffff  ffff "+prods.get(i)) ;
              }
	 	      ratings  = new Hashtable() ;
	 	      
		      if (prods !=null && prods.size() > 0)
		      {
		         for (int i =0; i < prods.size(); i++)
		         {
		        	System.out.println("iiiiiiiiiii "+i) ;
		    	    prodId = (String) prods.get(i) ;
			    	prid = prodId ;
				    prodId = "('"+prodId+"')" ;
				    System.out.println("I am mm here "+prodId) ;
				    productsHashtable = (Hashtable) recEng.getBeautyProductInfo(prodId); //gets the product
				    productsHashtable  = recEngDebug.getProductsIngredients("", "prodId", "",productsHashtable,prodId, false); //Single changes Struts
				    product = (ProductVO) productsHashtable.get(prid) ;
				    product = (ProductVO) recEngDebug.ratingsIndividual(cp,product,ingrratings);
				    System.out.println("I am mm here") ;
					 try{
			 	          
						 for (int j=0; j < profiles.size() ; j++)
			 	          {
			 	          	String profileAtt = (String) profiles.get(j) ;
			 	          	ratings = (Hashtable) getProductScore(cp, product, bumpscores, profileAtt, prid,ratings) ;      
			 	          	//log.debug("rating val"+ratings);
			 	          }
					    }catch(Exception e){
					    	////logger.log(Level.DEBUG,e);
					    	e.printStackTrace();
					    }
					 
					 //log.debug("rating val after exception "+ratings);
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
		  
		  System.out.println("vbbbbbbbb 66 "+ratings.size()) ;
		  
		  Enumeration en = ratings.keys() ;
		  while (en.hasMoreElements())
		  {
			  String enkey = (String) en.nextElement();
			  String enval = (String) ratings.get(enkey) ;
		//	  System.out.println("dddddddd "+enkey+"  "+enval) ;
		//	  System.out.println("eeeeeeeee "+ratings.get(enkey)) ;
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
        
    	

      	int pbasescore = 0 ;
      	int pbasecount = 0 ;
      	int pbasepercen = 0 ;
      	
      	int pingrscore = 0 ;
      	int pingrcount = 0 ;
      	int pingrpercen = 0 ;
      	
	    String  pmtype             = product.getMasterProdType() ;
        String  ptype              = (String) product.getProdType() ;
        
      	String bumpkey    = profileAtt+ptype ;
      	String bscore  = (String) bumpscores.get(bumpkey) ;
 		System.out.println("bump score "+bumpkey+"  "+bscore+"  pmtype "+pmtype+"  ptype "+ptype+" profileAtt "+profileAtt) ;
      	
        pBaseRatings = (Hashtable) product.getProfileBaseRatings() ; 
        pIngrRatings = (Hashtable) product.getProfileIngrRatings() ;
        
      	if (pBaseRatings.get(profileAtt) != null)
      	{
      		String pbasescorestring = (String) pBaseRatings.get(profileAtt) ;
			StringTokenizer stp = new StringTokenizer(pbasescorestring,"::") ;
			String stps = stp.nextToken() ;
			String stpc = stp.nextToken() ;
			pbasescore =  Integer.parseInt(stps) ;
			pbasecount =  Integer.parseInt(stpc) ;
      		System.out.println("action base "+pbasescore+" "+pbasecount) ;
      	}
      	if (pIngrRatings.get(profileAtt) != null)
      	{
      		String pingrscorestring = (String) pIngrRatings.get(profileAtt) ;
			StringTokenizer stp = new StringTokenizer(pingrscorestring,"::") ;
			String stps = stp.nextToken() ;
			String stpc = stp.nextToken() ;
			pingrscore =  Integer.parseInt(stps) ;
			pingrcount =  Integer.parseInt(stpc) ;
      		System.out.println("action ingr "+pingrscore+" "+pingrcount) ;
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
 		      System.out.println("I am in else vbbbbb "+pmtype) ;
              if (pbasepercentage == 0.0f)
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
            }
			score = score+bumpscore ;
			// log.debug("product id"+product.getProdId());
			// log.debug("product base score"+product.getBaseScore());
			// log.debug("product bump score"+product.getBumpScore());
			// log.debug("product ingr score"+product.getIngrScore());
 		    System.out.println("action score "+profileAtt+" "+score+" "+bumpscore+" base "+basepercentage+" ingr "+ingrpercentage) ; 
			
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