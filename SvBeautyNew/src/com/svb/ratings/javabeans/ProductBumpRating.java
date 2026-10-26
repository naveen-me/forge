/*
 * Created on Mar 17, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.ratings.javabeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;


import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVBeautydba;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;

/**
 * @author Rao
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ProductBumpRating {

	SVDBOperation dbo = new SVDBOperation();
	SVBeautydba dba = new SVBeautydba();

	public Hashtable getProdBumpRating()
	{
		ProductVO[] productVOList = new ProductVO[0];
		Hashtable   prodlist      = new Hashtable();
		String[][] result2 = null ;
		String sql = "" ;
        
		try 
		{
			dbo = new SVDBOperation();
			dbo.createConnection();
//			dbo.createConnectionStatic();
			sql = "select * from sv_product_bump_ratings where sv_product_status = 'Y' order by sv_product_id, sv_profile_attribute_id  " ;
			result2 = dbo.executeQuery(sql) ;
            String prodid = "" ;
            String attribid = "" ;
            String attribscore  = "" ;
            if (attribscore == null || attribscore.length() == 0)
            	attribscore = "0" ;
            
  //  		System.out.println("aaa "+result2.length) ;
  //  		System.out.println("bbb "+prodlist.size()) ;
            
		    for (int j=0; j < result2.length ; j++)
		    {
		    	prodid = result2[j][0] ;
		    	attribid =  result2[j][1] ;
		    	attribscore = result2[j][2] ;
		    	
	  //  		System.out.println("ccc "+prodlist.size()) ;
		    	if (prodlist !=null && prodlist.get(prodid.trim())!=null)
		    	{
	//	    		System.out.println("ffff 3 "+prodid) ;
		    		Hashtable attribScore = (Hashtable) prodlist.get(prodid.trim()) ;
		    		attribScore.put(attribid,attribscore) ;
		    		prodlist.put(prodid.trim(),attribScore) ;
		    	}else if (prodlist !=null && prodlist.get(prodid.trim())== null)
		    	{
	//	    		System.out.println("ffff 1 "+prodid) ;
		    		Hashtable attribScore = new Hashtable() ;
		    		attribScore.put(attribid,attribscore) ;
		    		prodlist.put(prodid.trim(),attribScore) ;
		    	}
		    }
	//	    System.out.println("1 "+prodlist.size()) ;
		    /*
		     * commented useless loop for performance improvement - Shivam - 22/07/2024
		     * 
		     * Enumeration e3 = prodlist.keys() ;
		    while (e3.hasMoreElements())
		    {
		    	String pd = (String) e3.nextElement() ;
		    	Hashtable h3 = (Hashtable) prodlist.get(pd) ;
		//	    System.out.println("2 "+pd) ;
		//	    System.out.println("3 "+h3.size()) ;
			    Enumeration e4 = h3.keys() ;
			    while (e4.hasMoreElements())
			    {
			    	String ad = (String) e4.nextElement() ;
			    	String avl = (String) h3.get(ad) ;
			//    	System.out.println("4 "+ad+"  "+avl) ;
			    }
		    }*/
		} catch (Exception ex1) {
			ex1.printStackTrace() ;
		}
		finally {
			dbo.closeConnection() ;
		}

		return prodlist;
	}
	public ProductVO[] getProducts(CustomerProfileVO cp, ProductVO[] products) {
		try {
			//initialize();
			//commented for performance improvement
			//ingrFunctions();
			Hashtable pbratings = (Hashtable) getProdBumpRating() ;
			products = getProductBumpScore(cp, products,pbratings);
			//destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return products;
	}
	@SuppressWarnings("unchecked")
	public ProductVO[] getProductBumpScore(CustomerProfileVO cp, ProductVO[] products,Hashtable pbratings) {

		String cAge = cp.getAge();
		String cSkinType = cp.getSkinType();
		String cNatural = cp.getNatural() ;
		String cEthnicity = cp.getEthnicity() ;
		
		ArrayList custEyeConcernList = cp.getEyeConcernList();
		ArrayList custSkinConcernList = cp.getSkinConcernList();
		ArrayList custSkinConditionList = cp.getSkinConditionList();
		ArrayList cusSpecificationList = cp.getSpecification();//Added 01-18-2021 synoptek New exception rules review and perform changes
		
		
	//	System.out.println("cprofile "+cSkinType) ;
	//	System.out.println("cprofile 2 "+cSkinType.substring(3,cSkinType.length())) ;
		ArrayList cProfile = new ArrayList() ;
		cProfile.add(cSkinType) ;
		cProfile.add(cAge) ;
		cProfile.add(cNatural) ;
		cProfile.add(cEthnicity) ;

		for (int m = 0; m < custEyeConcernList.size(); m++) {
			String cId = (String) custEyeConcernList.get(m) ;
			cProfile.add(cId) ;
		}
		for (int m = 0; m < custSkinConcernList.size(); m++) {
			String cId = (String) custSkinConcernList.get(m) ;
			cProfile.add(cId) ;
		}
		for (int m = 0; m < custSkinConditionList.size(); m++) {
			String cId = (String) custSkinConditionList.get(m) ;
			cProfile.add(cId) ;
		}
		//Added 01-18-2021 synoptek New exception rules review and perform changes
		for (int m = 0; m < cusSpecificationList.size(); m++) {
			String cId = (String) cusSpecificationList.get(m) ;
			cProfile.add(cId) ;
		}
		//Added 01-18-2021 synoptek New exception rules review and perform changes
		//Added 01-18-2021 synoptek add 10 points to the total score for products with sub category = "Serums-Retinol"
		/*
		 * boolean retinolsFlag=false;
		 * if(custSkinConcernList.contains(ConstantDeclarations.SKIN_CONCONCERN_DSK) &&
		 * !cp.getAllergyList().contains(ConstantDeclarations.SKIN_TAKING_RENTIOL) &&
		 * !cp.getSpecification().contains(ConstantDeclarations.DOESNOT_CONTAIN_RENTIOL)
		 * ) { retinolsFlag=true; } //Added 01-18-2021 synoptek add 10 points to the
		 * total score for products with sub category = "Serums-Retinol"
		 */
		//Added 03-22-2021 synoptek add 10 points to the total score for products with sub category = "Serums-Retinol"
		boolean retinolsFlag=false;
		if(custSkinConcernList.contains(ConstantDeclarations.SKIN_CONCONCERN_DSK)) {
			retinolsFlag=true;			
		}
		//Added 03-22-2021 synoptek add 10 points to the total score for products with sub category = "Serums-Retinol"
		
	//	System.out.println("vb 0128 "+products.length+" pbratings size "+pbratings.size()) ;
		for (int i=0; i < products.length; i++) {
			ProductVO prod = (ProductVO) products[i] ;
			String prodid = prod.getProdId() ;
	//		if (prodid.equals("147836"))
	//		   System.out.println("vb 0128 2 "+prodid) ;
			//Added 03-22-2021 synoptek add 10 points to the total score for products with sub category = "Serums-Retinol"
			if(retinolsFlag) {
				if(prod.getProdType().equalsIgnoreCase(ConstantDeclarations.SERUM_RETINOL)) {
					String bscore = prod.getBumpScore();
					float bscore2 = Float.parseFloat(bscore) ;
					float newbumpscore = 10+bscore2 ;
					prod.setBumpScore(""+newbumpscore) ;
				}
				
			}
			//Added 03-22-2021 synoptek add 10 points to the total score for products with sub category = "Serums-Retinol"
			if (pbratings !=null && pbratings.get(prodid.trim())!=null)
			{
				Hashtable attrib = (Hashtable) pbratings.get(prodid.trim());
				// Added For bump rule with AND condition-- Start
				Set<String> setOfKey = attrib.keySet();
				// for-each loop
				for (String key : setOfKey) {
					int index = key.indexOf(",");
				
					if (index != -1) {
						String KeyValue=(String) attrib.get(key);
						multipleAttributes(cProfile, key, KeyValue, prod);
					} else {
						for (int j = 0; j < cProfile.size(); j++) {
							String cProf = (String) cProfile.get(j);

							if (attrib.get(cProf) != null) {
								String atribScore = (String) attrib.get(cProf);
								float atrscore = Float.parseFloat(atribScore);
								String bscore = prod.getBumpScore();
								float bscore2 = Float.parseFloat(bscore);
								float newbumpscore = atrscore + bscore2;
								prod.setBumpScore("" + newbumpscore);
								// System.out.println("prod score bumpreatings "+prod.getProdId()+"
								// "+prod.getBumpScore()) ;
							}

						}
					}
				}
			}
		}
		return products;
	}
	
//Added For bump rule with AND condition
	public void multipleAttributes(ArrayList cProfile, String attribValue, String KeyValue, ProductVO prod) {

		String[] values = attribValue.trim().split(",");
		ArrayList commaSeprateSet = new ArrayList(Arrays.asList(values));

		if (cProfile.containsAll(commaSeprateSet)) {
					String atribScore = KeyValue;
					float atrscore = Float.parseFloat(atribScore);
					String bscore = prod.getBumpScore();
					float bscore2 = Float.parseFloat(bscore);
					float newbumpscore = atrscore + bscore2;
					prod.setBumpScore("" + newbumpscore);
		
		}
	}
	//Added For bump rule with AND condition End

	public Hashtable getBumpScore()
	{
		String[][]  result        = null;
		Hashtable   bumplist      = new Hashtable();
		String[][] result2 = null ;
		String sql = "" ;
        
		try 
		{
			dbo = new SVDBOperation();
			dbo.createConnection();
//			dbo.createConnectionStatic();
			sql = " select sv_attribute_value, sv_product_type_id, sv_product_line_id, sv_bump_score from bump_rules order by sv_product_type_id  " ;
			result2 = dbo.executeQuery(sql) ;
            String attribvalue = "" ;
            String ptypeid = "" ;
            String plineid = "" ;
            String bumpscore  = "" ;
            if (bumpscore == null || bumpscore.length() == 0)
            	bumpscore = "0" ;
            
    		System.out.println("aaa "+result2.length) ;
            
		    for (int j=0; j < result2.length ; j++)
		    {
		    	attribvalue = result2[j][0] ;
		    	ptypeid =  result2[j][1] ;
		    	plineid = result2[j][2] ;
		    	bumpscore = result2[j][3] ;
		    	
		    		bumplist.put(attribvalue.trim()+ptypeid,bumpscore) ;
		    }
		} catch (Exception ex1) {
			ex1.printStackTrace() ;
		}
		finally {
			dbo.closeConnection() ;
		}

		return bumplist;
	}
	
	
	public static void main(String[] args) {
		
		ProductBumpRating pbr = new ProductBumpRating() ;
		pbr.getProdBumpRating() ;
	}
}
