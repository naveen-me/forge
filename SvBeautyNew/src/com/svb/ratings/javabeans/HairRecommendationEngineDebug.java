package com.svb.ratings.javabeans;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;


import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVBeautydba;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.valueobjects.AttribRatingVO;
import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;
import com.svb.customerprofile.dal.CustomerProfileDAL;

/*
 * // header - edit "Data/yourJavaHeader" to customize // contents - edit
 * "EventHandlers/Java file/onCreate" to customize //
 */
public class HairRecommendationEngineDebug {
	SVBeautydba dba = new SVBeautydba();
	SVDBOperation dbo = new SVDBOperation();

	boolean externalDBO = false;
	boolean noingredients = false;


	public void initialize() throws Exception {
		dba = new SVBeautydba();
		dbo = new SVDBOperation();
		dbo.createConnection();

	}

	public void setDBO(SVDBOperation dbo){
		this.dbo 		 = dbo;
		this.externalDBO = true;
	}

	public void destroy(){
		dbo.closeConnection();
	}

	public Hashtable getProdDetails(String prodtype, String prodselect, String prodline,CustomerProfileVO cp) {
		String[][]  result        = null;
		String[][]  result1        = null;
		ProductVO[] productVOList = new ProductVO[0];
		Hashtable   prodhash      = new Hashtable();
		boolean prodflag = false ;
		boolean prodidsflag = false ;
		boolean clearskinflag = false ;
		ArrayList custSkinConcernList = cp.getSkinConcernList();
		ArrayList custSkinConditionList = cp.getSkinConditionList();
		boolean acneflag = false;
		boolean rosaceaflag = false;
		boolean agespotflag = false;
		for (int m = 0; m < custSkinConcernList.size(); m++) {
			if (custSkinConcernList.get(m).equals("C007"))
				agespotflag = true;
		}
		for (int m = 0; m < custSkinConditionList.size(); m++) {
			if (custSkinConditionList.get(m).equals("D008"))
				acneflag = true;
			if (custSkinConditionList.get(m).equals("D002"))
				acneflag = true;
			if (custSkinConditionList.get(m).equals("D003"))
				rosaceaflag = true;
		}
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			if (prodselect !=null && prodselect.equals("prodLine"))
			{
				result = dbo.executeQuery(dba.getBeautyProductIds(prodline,prodselect));
				result1 = dbo.executeQuery(dba.getBeautyProductIds("0009","prodType"));
				for (int i=0; i < result1.length ; i++)
				{
					ProductVO pr = new ProductVO();
					pr.setProdId(result1[i][0].trim());
					pr.setBase(result1[i][2]);
					pr.setProductLine(result1[i][3]);
					pr.setProdName(result1[i][4]);
					pr.setPrice(result1[i][5]);
					pr.setImage(result1[i][6]);
					pr.setSPFId(result1[i][7]) ;
					pr.setIngrScore("0");
					pr.setFunctionscore("0");
					pr.setBumpScore("0");
					pr.setTobeDeleted(false);
					String mcateg = "" ;
					mcateg = result1[i][1] ;
					pr.setMasterProdType(mcateg);
					prodhash.put(result1[i][0].trim(), pr);
					
				}
				
			}
			else if (prodselect !=null && prodselect.equals("prodType"))
			    result = dbo.executeQuery(dba.getBeautyProductIds(prodtype,prodselect));
			else 
			    result = dbo.executeQuery(dba.getBeautyProductIds("",""));
			
			
		for (int i = 0; i < result.length; i++) {
         
		 if   (    result[i][1].equals("0001") || result[i][1].equals("0003") 
		 		|| result[i][1].equals("0005") || result[i][1].equals("0004")
		 		|| result[i][1].equals("0011") || result[i][1].equals("0012")
		 		|| result[i][1].equals("0013") || result[i][1].equals("0014")
		 		|| result[i][1].equals("0015") || result[i][1].equals("0016")
		 		|| result[i][1].equals("0017") || result[i][1].equals("0009")
			  )
		 {
		     prodflag = true ;
		 }
		 if(prodflag)
		 {
//		    //logger.log(Level.DEBUG,"prodflag begin") ;
			ProductVO pr = new ProductVO();
			pr.setProdId(result[i][0].trim());
			pr.setBase(result[i][2]);
			pr.setProductLine(result[i][3]);
			pr.setProdName(result[i][4]);
			pr.setPrice(result[i][5]);
			pr.setImage(result[i][6]);
			pr.setSPFId(result[i][7]) ;
			pr.setIngrScore("0");
			pr.setFunctionscore("0");
			pr.setBumpScore("0");
			pr.setTobeDeleted(false);
			String mcateg = "" ;
			String pcateg = "" ;
			pcateg = result[i][1] ;
			mcateg = result[i][1] ;
				if (acneflag)
				{
					if (pcateg.equals("0011"))
						mcateg = "0005" ;
					if (pcateg.equals("0012"))
						mcateg = "0001" ;
					if (pcateg.equals("0013"))
						mcateg = "0003" ;
				}
				if (rosaceaflag)
				{
					if (pcateg.equals("0015"))
						mcateg = "0005" ;
					if (pcateg.equals("0016"))
						mcateg = "0001" ;
					if (pcateg.equals("0017"))
						mcateg = "0003" ;
				}
				if (agespotflag)
				{
					if (pcateg.equals("0014"))
						mcateg = "0005" ;
				}
			pr.setMasterProdType(mcateg);
/*			pr.setImage(result[i][1]);
			pr.setPrice(result[i][2]);
			pr.setShortDesc(result[i][3]);
			pr.setProdName(result[i][4]);
			pr.setProdType(result[i][5]); */
//			pr.setProductLinePrime(result[i][9]);
//			if(result[i].length > 10)
//				pr.setComments(result[i][10]);

			prodhash.put(result[i][0].trim(), pr);
//		    //logger.log(Level.DEBUG,"prodflag  end "+result[i][0]) ;
		 }
		}
		} catch (Exception ex1) {
			ex1.printStackTrace() ;
		}
		finally {
			dbo.closeConnection() ;
		}

		return prodhash;
	}

	public Hashtable getBeautyProductInfo(String prodids) {
		String[][]  result        = null;
        Hashtable prods = new Hashtable() ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			result = dbo.executeQuery(dba.getBeautyProductInfo(prodids));
			
		for (int i = 0; i < result.length; i++) {
         
		 
//		    //logger.log(Level.DEBUG,"prodflag begin") ;
			ProductVO pr = new ProductVO();
			pr.setProdId(result[i][0].trim());
			pr.setProdName(result[i][1]);
			pr.setPrice(result[i][2]);
			pr.setImage(result[i][3]);
			pr.setProductLinePrime(result[i][4]) ;
			pr.setSPFId(result[i][5]) ;
//			pr.setMasterProdType(result[i][6]) ;
			String mcateg = "" ;
			String pcateg = "" ;
			pcateg = result[i][6] ;
			mcateg = result[i][6] ;
					if (pcateg.equals("0011"))
						mcateg = "0005" ;
					if (pcateg.equals("0012"))
						mcateg = "0001" ;
					if (pcateg.equals("0013"))
						mcateg = "0003" ;
					if (pcateg.equals("0015"))
						mcateg = "0005" ;
					if (pcateg.equals("0016"))
						mcateg = "0001" ;
					if (pcateg.equals("0017"))
						mcateg = "0003" ;
					if (pcateg.equals("0014"))
						mcateg = "0005" ;
			pr.setMasterProdType(mcateg);
			pr.setBase(result[i][7]) ;
			prods.put(result[i][0].trim(), pr) ;
		 }
		} catch (Exception ex1) {
			ex1.printStackTrace() ;
		}
		finally {
			dbo.closeConnection() ;
		}

		return prods;
	}

	protected ProductVO getProductIngredients(ArrayList prodingredients,ProductVO tmpproduct) {
		String tmpprodingredients[][] = new String[prodingredients.size()][2];
		for (int j = 0; j < prodingredients.size(); j++) {
			StringTokenizer stk = new StringTokenizer((String) prodingredients
					.get(j), "-");
			while (stk.hasMoreTokens()) {

				String ingrid = stk.nextToken();
				String conc = stk.nextToken();
				if (conc == null || conc.trim().length() == 0
						|| conc.equals("null")) //if the product concentration
												// is not entered default it to
												// NA
					conc = "NA";
				tmpprodingredients[j][0] = ingrid;
				tmpprodingredients[j][1] = conc;
			}

		}
		tmpproduct.setIngredients(tmpprodingredients);
		return tmpproduct;

	}


	//this method eliminates the products that r need to be eliminated based on
	// the ingredients that got eliminated earlier

	//this methos gets the ingredients present in the products
	public Hashtable getProductsIngredients(String prodtype, String prodselect,
			String prodline,Hashtable prodhash,String prodid) {
		String result[][]  = null;
		ArrayList ingrprodvect = new ArrayList();
		ArrayList prodingredients = new ArrayList();
		boolean prodflag = false ;
		boolean prodidsflag = false ;
		boolean clearskinflag = false ;
        int iflag = 0 ;
        //System.out.println("prodhash size vb "+prodhash.size()) ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			
			if (prodselect !=null && prodselect.equals("prodLine"))
				result = dbo.executeQuery(dba.getProductsIngredientsLine(prodline,prodselect));
			else if (prodselect !=null && prodselect.equals("prodType"))
			    result = dbo.executeQuery(dba.getProductsIngredientsLine(prodtype,prodselect));
			else if (prodselect !=null && prodselect.equals("prodId"))
			    result = dbo.executeQuery(dba.getProductIngredients(prodid, false));
			else 
			    result = dbo.executeQuery(dba.getProductsIngredientsLine("",""));
			
		// analysis	result = dbo.executeQuery(dba.getProductsIngredients(prodselect));
		////logger.log(Level.DEBUG,"result ingr length "+result.length) ;
		//get the prodcuts along with ingredients
		//System.out.println("vbbb result length "+result.length) ;
		for (int i = 0; i < result.length; i++) {

		   prodflag = true ;
           if(prodflag)
           {
			if (iflag != 0 && !prodid.equals(result[i][0].trim())) //if the oldprodid is
														// diff from the one we
														// r checking now ,
			{
				prodhash.put(prodid, getProductIngredients(prodingredients, (ProductVO)prodhash.get(prodid)));
				prodingredients = new ArrayList();
				prodingredients.add(result[i][1] + "-" + result[i][2]); //add
																		// the
																		// ingredient
																		// and
																		// concentration
																		// list
																		// to
																		// the
																		// productingredient
																		// ArrayList
			}
			else
			{
				prodingredients.add(result[i][1] + "-" + result[i][2]); //add
																		// the
																		// ingredient
																		// and
																		// concentration
																		// list
																		// to
																		// the
																		// productingredient
																		// ArrayList

			}
			prodid = result[i][0].trim();
           	iflag = iflag+1 ;

		}
	  }
	  if (result.length > 0)
	  {
			prodhash.put(prodid,getProductIngredients(prodingredients, (ProductVO)prodhash.get(prodid.trim())));
	  }

		if (result.length == 0)
			noingredients = true;
	} catch (Exception ex1) {
		ex1.printStackTrace() ;
	}
	finally {
		dbo.closeConnection() ;
	}
		return prodhash;
	}

	public String[][] getCustomerProfileQuery(SVDBOperation dbo,SVBeautydba dba,String customid){
		return dbo.executeQuery(dba.getCustomerProfile(customid));
	}

	public String[][] getCustomerRegimenQuery(SVDBOperation dbo,SVBeautydba dba,String customid){
		return dbo.executeQuery(dba.getCustomerRegimen(customid));
	}

	public CustomerProfileVO getCustomerProfile(String customid, CustomerProfileVO cp,String tableselect) {

	try
	{
		dbo = new SVDBOperation() ;
		dbo.createConnection() ;
		Hashtable profile = new Hashtable();
		//String result[][] = dbo.executeQuery(dba.getCustomerProfile(customid));
		String result[][] = new String[0][0];
	  if (tableselect.equals("skin") || tableselect.equals("common"))
	  {
		result = getCustomerProfileQuery(dbo,dba,customid);
		for (int i = 0; i < result.length; i++) {

			cp.setCustomerId(customid);

			if (result[i][1] != null && result[i][2].trim().equals(ConstantDeclarations.SKIN_TYPE_ID))
				cp.setSkinType(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals(ConstantDeclarations.SKIN_CONDITION_ID))
				cp.getSkinConditionList().add(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals(ConstantDeclarations.SKIN_CONCERN_ID))
				cp.getSkinConcernList().add(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals("54"))
				cp.getEyeConcernList().add(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals(ConstantDeclarations.SKIN_AGE_ID))
				cp.setAge(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals(ConstantDeclarations.SKIN_ALLERGY_ID))
				cp.getAllergyList().add(result[i][3]);

			else if (result[i][1] != null && result[i][2].trim().equals("58"))
				cp.setEthnicity(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals("57"))
				cp.setNatural(result[i][3]);

			profile.put(result[i][2].trim() + "-" + result[i][3], "1");

		}
		cp.setProfileHT(profile);
	  }
		//result = dbo.executeQuery(dba.getCustomerRegimen(customid));
	  if (tableselect.equals("regimen") || tableselect.equals("common"))
	  {
/*		result = getCustomerRegimenQuery(dbo,dba,customid);
		for (int i = 0; i < result.length; i++) {
			String regquery = result[i][0].trim() ;
			String reganswer = result[i][1].trim() ;
			cp.getRegimenHT().put(regquery,reganswer );
			if (result[i][0] != null && result[i][0].trim().equals(ConstantDeclarations.REGIMEN_PRODUCT_USAGE))
				cp.getProductUsage().add(result[i][1]);
			if (regquery.equals(ConstantDeclarations.USAGE_QUERY_REGIMEN))
			{
				cp.setRegimenUsage(reganswer) ;
			}
		}
*/		
		result = dbo.executeQuery(dba.getCustomerGenProfile(customid));

		for (int i = 0; i < result.length; i++) {
			cp.setCustomerEntryDate(result[i][4]);
		}
	  }
	} catch (Exception e)
	{
		e.printStackTrace() ;
	}
	finally {
		dbo.closeConnection() ;
	}
       return cp ;
	}

	public CustomerProfileVO getCustomerGeneralProfile(String customid, CustomerProfileVO cp) {
		try {
			dbo = new SVDBOperation() ;
			dbo.createConnection() ;
			String result[][] = new String[0][0];

			result = dbo.executeQuery(dba.getCustomerGenProfile(customid));

			if(result.length > 0) {
				cp.setCustomerEntryDate(result[0][4]);
				cp.setGender(result[0][5].trim());
			}
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		finally {
			dbo.closeConnection() ;
		}

		return cp ;
	}


	protected String increaseScore(String value) {

		/*commented for performance reasons.
		try {

			float val = Float.parseFloat(value.trim());

			/*
			 * if(val == 7.0f) // commented on 03/25/03 val = val * 1.4f * 2f ;
			 * else if(val == 8.0f) val = val * 1.5f * 2f ; else if(val == 9.0f)
			 * val = val*1.75f * 2f ; else if(val==10f) val = val * 2.00f * 2f ;
			 */
		/*
			int v = Math.round(val);
			value = v + "";
		} catch (Exception e) {
			////logger.log(Level.DEBUG,e);
		}*/
		return value;

	}

	//ht1 is the hastable that contains prodid as key and the ingredeient
	// ArrayList related to that prodid as the value
	//ratinsgs is the array that contains the ingredient record which we got
	// from database .

	public Hashtable getIngredientsRatings(CustomerProfileVO cp, boolean manual) {
		Hashtable ratings = new Hashtable();
		String result[][] = new String[0][0];
		AttribRatingVO ar = new AttribRatingVO();
		String ingrlist = "";

		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			//System.out.println("I am here bn nmm") ;
		result = dbo.executeQuery(dba.getIngredientsRatings(cp.getSkinType(),
				cp.getSkinConditionList(), cp.getSkinConcernList(),
				cp.getEyeConcernList(), ingrlist));

		for (int i = 0; i < result.length; i++) {
			//key : 02-ingr-conc-mastertype

			String key = result[i][0].trim() + "-" + result[i][1].trim() + "-"
					+ result[i][2].trim() + "-" + result[i][3].trim();
			String value = result[i][6].trim();
			String sub = key + "-" + result[i][4] + "-" + result[i][5] + "-"
					+ value;
			//key+ 51-T003-4 51-skintype ; T003 - dry ; rating is 4

			//////logger.log(Level.DEBUG,"ingr"+key+"-"+result[i][4]+"-"+result[i][5]+"-"+","+value);



        //System.out.println("vbbb ingr"+key+"-"+result[i][4]+"-"+result[i][5]+"-"+","+value) ;
		if (ratings.get(key) != null) //if this is not the first rating
										  // value
			{

				if (value != null && !value.trim().equals("NA")) {
					String tmpval = value;

					if (!manual && (result[i][0].trim()).equals(ConstantDeclarations.ATTRIBUTE_INGR))//if not
																	  // manual
																	  // and if
																	  // not
																	  // base
					{
						value = increaseScore(value);

					}
					ar = (AttribRatingVO) ratings.get(key);
					value = String.valueOf(Integer.parseInt(ar.getScore())
							+ Integer.parseInt(value.trim()));
					ar.setScore(value.trim());
					ar.setCount(ar.getCount() + 1);
					ar.getIngrConcList().add(sub);

					ratings.put(key, ar);

				}
			} else if (value != null && !value.trim().equals("NA")) //if first
																	// one
			{
				ar = new AttribRatingVO();

				if (!manual && (result[i][0].trim()).equals(ConstantDeclarations.ATTRIBUTE_INGR)) {
					value = increaseScore(value);
				}
				ar.setScore(value.trim());
				ar.setCount(1);
				ar.getIngrConcList().add(sub);

				ratings.put(key, ar);

			}

		} //end of ratings

		} catch (Exception ex1) {
			ex1.printStackTrace() ;
		}
		finally {
			dbo.closeConnection() ;
		}
		return ratings;

	}
	
	
	public ProductVO ratingsIndividual(CustomerProfileVO cp,ProductVO product,Hashtable ingrratings) {


			ProductVO producttmp = product;
			String prodid = producttmp.getProdId();
			String base = producttmp.getBase();
			String ingr[][] = producttmp.getIngredients();
			String prodmastertype = producttmp.getMasterProdType();
			String pmtype = "" ;
			String prodtype = producttmp.getProdType();
			boolean tobedeleted = producttmp.isTobeDeleted();
			boolean faceflag = false;
			boolean eyeflag = false;
       			
			int ingrcount = 0;
			int ingrscore = 0;
			int basecount = 0;
			int basescore = 0;
			float percentage = 0.0f;
			Hashtable ht = new Hashtable();
            Hashtable pBaseRatings  = new Hashtable();       
            Hashtable pIngrRatings  = new Hashtable();
            

                pmtype = prodmastertype ;
				if (prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT) )
					eyeflag = true;
				if (prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT) )
					faceflag = true;

				if (prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_UV) || prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT))
					prodmastertype = ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT;
				if (!prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_CLEANSE)
						&& !prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_MOIST)
						&& !prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT))
					prodmastertype = "99";

				String basestring = "01-" + base + "-NA-" + prodmastertype;
				if (ingrratings.get(basestring) == null)
					basestring = "01-" + base + "-NA-99";

				if (ingrratings.get(basestring) != null) {
					AttribRatingVO att = (AttribRatingVO) ingrratings.get(basestring);

					ArrayList vect = att.getIngrConcList();
					for (int i = 0; i < vect.size(); i++) 
					{
						String tmp = (String) vect.get(i);
						StringTokenizer stk = new StringTokenizer(tmp, "-");
						
						if (stk.nextToken().equals(ConstantDeclarations.ATTRIBUTE_BASE))
							; //01
						{
							int addval = 1 ;
							stk.nextToken(); //ingrid
							stk.nextToken(); //ingrconc
							stk.nextToken(); //mastertype
							String skin = stk.nextToken() + "-"
									+ stk.nextToken();

							String tmskin = skin;
							if ( skin.trim().substring(0, 2).equals(ConstantDeclarations.SKIN_TYPE_ID))
								addval = 2 ;
							boolean concernflag = false;
							if ( skin.trim().substring(0, 2).equals(ConstantDeclarations.SKIN_CONCERN_ID))
							{
								String tskin = skin.trim().substring(3, 7);
								
								if (faceflag
										&&  ((tskin.trim().equals("C022")
												|| tskin.trim().equals("C003")
												|| tskin.trim().equals("C020")
												|| tskin.trim().equals("C004")
												|| tskin.trim().equals("C023")
												|| tskin.trim().equals("C012")
												|| tskin.trim().equals("C019")
												|| tskin.trim().equals("C007")
											 )
											))
									concernflag = true;
								if (concernflag)
									tmskin = skin;
								else
									tmskin = "99999";
							} 
							concernflag = false;
							if ( skin.trim().substring(0, 2).equals("54"))
							{
								String tskin = skin.trim().substring(3, 7);
								if (eyeflag
										&&  ((tskin.trim().equals("C001")
												|| tskin.trim().equals("C002")
												|| tskin.trim().equals("E001")
												|| tskin.trim().equals("E004")
											 )
											))
									concernflag = true;
								if (concernflag)
									tmskin = skin;
								else
									tmskin = "99999";
							} 
							else
								tmskin = skin;

							if (pmtype.equals("0005"))
							{
								if (tmskin.trim().equals("54-C001")
										|| tmskin.trim().equals("54-C002")
										|| tmskin.trim().equals("54-E001")
										|| tmskin.trim().equals("54-E004")
								   )
								   tmskin = "9999" ;
							}

							if (pmtype.equals("0004"))
							{
								if ((tmskin.trim().equals("53-C022")
												|| tmskin.trim().equals("53-C003")
												|| tmskin.trim().equals("53-C020")
												|| tmskin.trim().equals("53-C004")
												|| tmskin.trim().equals("53-C023")
												|| tmskin.trim().equals("53-C012")
												|| tmskin.trim().equals("53-C019")
												|| tmskin.trim().equals("53-C007")
												|| tmskin.trim().equals("52-D001")
												|| tmskin.trim().equals("52-D003")
												|| tmskin.trim().equals("52-D006")
												|| tmskin.trim().equals("52-D008")
												|| tmskin.trim().equals("52-D002")
												|| tmskin.trim().equals("51-T001")
												|| tmskin.trim().equals("51-T002")
												|| tmskin.trim().equals("51-T003")
												|| tmskin.trim().equals("51-T004")
												|| tmskin.trim().equals("54-C001")
												|| tmskin.trim().equals("54-C002")
												|| tmskin.trim().equals("54-E001")
												|| tmskin.trim().equals("54-E004")
											 )
											)
							//	System.out.println(" I am in base eyeconcerns filter "+tmskin) ;
								   tmskin = "9999" ;
							}
							//													//logger.log(Level.DEBUG,"tmskin "+tmskin);
							Hashtable profile = cp.getProfileHT();
							//													//logger.log(Level.DEBUG,"basestring "+basestring);
							if (profile.get(tmskin) != null) {
								int pbscore = 0 ;
                                int pbcount = 0 ;
								
								int sc = Integer.parseInt(stk.nextToken());
								basescore = basescore + (sc * addval); // VB 9/10/07 
								basecount++;
								if (addval == 2)
									basecount++ ;
								pbscore = sc*addval ;
								pbcount = pbcount+1 ;
								  if (addval == 2)
								  	pbcount = pbcount+1 ;
								if (pBaseRatings.get(tmskin.substring(3,tmskin.length())) != null)
								{
									//int pbscore1 = pbscore + Integer.parseInt((String)pBaseRatings.get(tmskin.substring(3,tmskin.length()))) ;
								//	pBaseRatings.put(tmskin.substring(3,tmskin.length()),""+pbscore1) ;
									
									String tps = (String)pBaseRatings.get(tmskin.substring(3,tmskin.length())) ;
									StringTokenizer stp = new StringTokenizer(tps,"::") ;
									String stps = stp.nextToken() ;
									String stpc = stp.nextToken() ;
									int iscore = Integer.parseInt(stps) ;
									int icount = Integer.parseInt(stpc) ;
									iscore = iscore+pbscore ;
									icount = icount+pbcount ;
									//System.out.println("tmskin iscore "+tmskin+"  "+iscore+"  "+icount);
									pBaseRatings.put(tmskin.substring(3,tmskin.length()),""+iscore+"::"+icount) ;
								}
								else
								{
									//pBaseRatings.put(tmskin.substring(3,tmskin.length()),""+basescore) ;
									pBaseRatings.put(tmskin.substring(3,tmskin.length()),""+pbscore+"::"+pbcount) ;
								}
								
								ht.put(basestring, ((AttribRatingVO) ingrratings.get(basestring)).getIngrConcList());
						}
						} //end of if stk

					}//end of for
//					ht.put(basestring, ((AttribRatingVO) ingrratings.get(basestring)).getIngrConcList());
				}
                producttmp.setProfileBaseRatings(pBaseRatings) ;

				if (ingr != null)
					for (int j = 0; j < ingr.length; j++) {
						//02-ingredientid-ingredientconc-productmastertype
						if (ingr[j][1] == null
								|| ingr[j][1].trim().length() == 0)
							ingr[j][1] = "NA";

						String ingrstring = "02" + "-" + ingr[j][0] + "-"
								+ ingr[j][1] + "-" + prodmastertype;
						
						if (ingrratings.get(ingrstring) == null)
							ingrstring = "02" + "-" + ingr[j][0] + "-"
									+ ingr[j][1] + "-99";
//                        System.out.println("ingr string "+ingrstring) ;
						if (ingrratings.get(ingrstring) != null) {
							AttribRatingVO att = (AttribRatingVO) ingrratings.get(ingrstring);
							////logger.log(Level.DEBUG,"inside");

							ArrayList vect = att.getIngrConcList();
//							System.out.println(" cccc vect size "+vect.size()) ;
							for (int i = 0; i < vect.size(); i++) {
								String tmp = (String) vect.get(i);
								StringTokenizer stk = new StringTokenizer(tmp, "-");
								//02"+"-"+ingr[j][0]+"-3-99" values : ArrayList
								// that holds 51-T003-4 , 52-0009-5
								if (stk.nextToken().equals(ConstantDeclarations.ATTRIBUTE_INGR))
									; //02
								{
									int addval = 1 ;
									stk.nextToken(); //ingrid
									stk.nextToken(); //ingrconc
									stk.nextToken(); //mastertype
									String skin = stk.nextToken() + "-"
											+ stk.nextToken();
									//													//logger.log(Level.DEBUG,"skin"+skin+"
									// "+prodid+" "+handbodyflag+" "+faceflag);

									String tmskin = skin;
//									System.out.println("cccc tmskin "+tmskin) ;
									boolean concernflag = false;
									if ( skin.trim().substring(0, 2).equals(ConstantDeclarations.SKIN_CONCERN_ID))
									{
										String tskin = skin.trim().substring(3, 7);
										if (faceflag
												&&  ((tskin.trim().equals("C022")
														|| tskin.trim().equals("C003")
														|| tskin.trim().equals("C020")
														|| tskin.trim().equals("C004")
														|| tskin.trim().equals("C023")
														|| tskin.trim().equals("C012")
														|| tskin.trim().equals("C019")
														|| tskin.trim().equals("C007")
													 )
													))
											concernflag = true;
										if (concernflag)
											tmskin = skin;
										else
											tmskin = "99999";
									} 
									concernflag = false;
									if ( skin.trim().substring(0, 2).equals("54"))
									{
										String tskin = skin.trim().substring(3, 7);
										if (eyeflag
												&& ( (tskin.trim().equals("C001")
														|| tskin.trim().equals("C002")
														|| tskin.trim().equals("E001")
														|| tskin.trim().equals("E004")
													 )
													))
											concernflag = true;
										if (concernflag)
											tmskin = skin;
										else
											tmskin = "99999";
									} 
									else
										tmskin = skin;
									
									// Not to apply eye scores to cleansers // 09-22-07 VB
									if (pmtype.equals("0001"))
									{
										if (tmskin.trim().equals("C001")
												|| tmskin.trim().equals("C002")
												|| tmskin.trim().equals("E001")
												|| tmskin.trim().equals("E004")
										   )
										   tmskin = "9999" ;
									}
									if (pmtype.equals("0005"))
									{
										if (tmskin.trim().equals("54-C001")
												|| tmskin.trim().equals("54-C002")
												|| tmskin.trim().equals("54-E001")
												|| tmskin.trim().equals("54-E004")
										   )
										   tmskin = "9999" ;
									}

									if (pmtype.equals("0004"))
									{
										if ((tmskin.trim().equals("53-C022")
														|| tmskin.trim().equals("53-C003")
														|| tmskin.trim().equals("53-C020")
														|| tmskin.trim().equals("53-C004")
														|| tmskin.trim().equals("53-C023")
														|| tmskin.trim().equals("53-C012")
														|| tmskin.trim().equals("53-C019")
														|| tmskin.trim().equals("53-C007")
														|| tmskin.trim().equals("52-D001")
														|| tmskin.trim().equals("52-D003")
														|| tmskin.trim().equals("52-D006")
														|| tmskin.trim().equals("52-D008")
														|| tmskin.trim().equals("52-D002")
														|| tmskin.trim().equals("51-T001")
														|| tmskin.trim().equals("51-T002")
														|| tmskin.trim().equals("51-T003")
														|| tmskin.trim().equals("51-T004")
													 )
													)
								//			System.out.println(" I am in base eyeconcerns filter "+tmskin) ;
										   tmskin = "9999" ;
									}

									//													//logger.log(Level.DEBUG,"finally "+prodid+"
									// "+prodtype+" "+tmskin) ;
									Hashtable profile = cp.getProfileHT();
//									System.out.println("vbbbbbbbbbbbbbbbbbb "+tmskin+"  "+prodmastertype) ;
									if (profile.get(tmskin) != null) {
							//			System.out.println("vfsdksh "+prodmastertype+"  "+tmskin) ;
										
										int fingrscore = 0 ;
										int fingrcoun  = 0 ;
										
										if ( tmskin.trim().substring(0, 2).equals(ConstantDeclarations.SKIN_TYPE_ID))
											addval = 2 ;
										if ( tmskin.trim().substring(0, 2).equals(ConstantDeclarations.SKIN_CONDITION_ID))
											addval = 2 ;
										
										int sc = Integer.parseInt(increaseScore(stk.nextToken()));
									//	if (sc !=5)
									//	{
										  ingrscore = ingrscore + ( sc * addval); // VB 09/10/07
										  ingrcount++;
										  if (addval == 2)
											ingrcount++ ;
										  fingrscore = sc * addval ;
										  fingrcoun = fingrcoun+1 ;
										  if (addval == 2)
											  fingrcoun = fingrcoun+1 ;
									//	}
							//			System.out.println("vbscore "+sc+"  "+tmskin.substring(3,tmskin.length())+"  "+fingrscore+" count "+fingrcoun);
										if (pIngrRatings.get(tmskin.substring(3,tmskin.length())) != null)
										{
											String tps = (String)pIngrRatings.get(tmskin.substring(3,tmskin.length())) ;
											StringTokenizer stp = new StringTokenizer(tps,"::") ;
											String stps = stp.nextToken() ;
											String stpc = stp.nextToken() ;
											int iscore = Integer.parseInt(stps) ;
											int icount = Integer.parseInt(stpc) ;
											iscore = iscore+fingrscore ;
											icount = icount+fingrcoun ;
											//System.out.println("tmskin iscore "+tmskin+"  "+iscore+"  "+icount);
											pIngrRatings.put(tmskin.substring(3,tmskin.length()),""+iscore+"::"+icount) ;
										}
										else
										{
											pIngrRatings.put(tmskin.substring(3,tmskin.length()),""+fingrscore+"::"+fingrcoun) ;
										}
										
										ht.put(ingrstring, ((AttribRatingVO) ingrratings.get(ingrstring)).getIngrConcList());
									}
								} //end of if stk

							}//end of for
//							ht.put(ingrstring, ((AttribRatingVO) ingrratings.get(ingrstring)).getIngrConcList());
						}//end if

					} //end of for
                    producttmp.setProfileIngrRatings(pIngrRatings) ;

			product = producttmp;


		return product ;
	}


	protected String calculatescore(ProductVO prod) {

		float ingrpercentage = Float.parseFloat(prod.getIngrScore());
		float basepercentage = Float.parseFloat(prod.getBaseScore());

		float funcpercentage = Float.parseFloat(prod.getFunctionScore());
		float bmpscore = Float.parseFloat(prod.getBumpScore());
		float score = 0.0f;

		if (funcpercentage != 0.0f) {
			basepercentage = (basepercentage * 10f) / 100f;
			ingrpercentage = (ingrpercentage * 70f) / 100f;
			funcpercentage = (funcpercentage * 20f) / 100f;
			score = ingrpercentage + basepercentage + funcpercentage + bmpscore;
			//				//logger.log(Level.DEBUG,"1211 "+score+" d "+ingrpercentage+" ff
			// "+basepercentage+" f "+funcpercentage) ;
		} else {
			basepercentage = (basepercentage * 20f) / 100f; // original 10
			ingrpercentage = (ingrpercentage * 80f) / 100f; // original 90
			score = ingrpercentage + basepercentage + bmpscore;
			//				//logger.log(Level.DEBUG,"55511 "+score+" d "+ingrpercentage+" ff
			// "+basepercentage+" f "+funcpercentage) ;
		}
		if (prod.getIngredients() == null) //if there r no ingredients present
										   // , then get the manual score
		{
			if (prod.getManualScore() != null
					&& prod.getManualScore().trim().length() > 0) {
				score = Float.parseFloat(prod.getManualScore());
				//			//logger.log(Level.DEBUG,"166666211 "+score+" d "+ingrpercentage+"
				// ff "+basepercentage+" f "+funcpercentage) ;
			}
		}
		//   //logger.log(Level.DEBUG,"score is "+prod.getProdid()+" "+score);
		return score + "";
	}

    // this method eliminates the ingredients that r not taken into ratings ,
	// ie , whose ratings values r less than 0
	protected Hashtable eliminateIngredients(String skintype, ArrayList skincondition) {

	Hashtable eliminatedingredients = new Hashtable();
	try
	{
		dbo = new SVDBOperation() ;
		dbo.createConnection() ;
		String result[][] = dbo.executeQuery(dba.eliminateIngredients(skintype,skincondition));
		for (int i = 0; i < result.length; i++) {

			////logger.log(Level.DEBUG,"eliminated ingredients"+result[i][0]);
			eliminatedingredients.put(result[i][0], "Y");
		}
	} catch (Exception e)
	{
		e.printStackTrace() ;
	}
	finally {
		dbo.closeConnection() ;
	}
		return eliminatedingredients;
	}

	public ProductVO[] getProductArray(ProductVO[] productVOList,Hashtable prodhash) {
		int count = 0;
		Enumeration en = prodhash.keys();
		productVOList = new ProductVO[prodhash.size()];
		while (en.hasMoreElements()) {
			ProductVO pr = (ProductVO) prodhash.get(en.nextElement());
			productVOList[count++] = pr;

		}
		return productVOList;
	}

	public String getProductLine(CustomerProfileVO cp) {
		String prodLine = "" ;
		ArrayList prodUsage = new ArrayList() ;
		prodUsage = (ArrayList) cp.getProductUsage() ;
		boolean anewflag = false ;
		boolean avonflag = false ;
		boolean becomeflag = false ;

		if (prodUsage != null)
		{
			if (prodUsage.size() > 2)
				prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
			else if (prodUsage.size() == 1)
			{
				    if ( ((String)prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_NATING) )
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
				    if ( ((String)prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_CUTEDT) )
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
				    if ( ((String)prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_AGEBNF) )
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
				    if ( ((String)prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_PRDSKT) )
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_AVON ;
				    if ( ((String)prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_BTFPKG) )
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_BCMG ;
				    if ( ((String)prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_ATHDTR) )
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
			}
			else if (prodUsage.size() == 2 )
			{
				    String pusage1 = (String)prodUsage.get(0) ;
				    String pusage2 = (String)prodUsage.get(1) ;
				    if (pusage1.equals(ConstantDeclarations.PRODUCT_USAGE_NATING) && pusage2.equals(ConstantDeclarations.PRODUCT_USAGE_BTFPKG) )
				    //	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_BCMG ;
			    	    prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
				    else
				    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
			}
			else if (prodUsage.size() == 0)
		    	prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
		 }
		 else
			prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW ;
		return prodLine;
	}

	public ArrayList getProdDetailsRegimen(String customerid, String regimenline)
	{
		String[][]  result        = null;
		ProductVO[] productVOList = new ProductVO[0];
		ArrayList   prodlist      = new ArrayList();
		String[][] result2 = null ;
		String sql = "" ;

		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select sv_product_id from customer_product_regimen where sv_customer_id = '"+customerid+"' "+
			      " and sv_product_regimen_line = '"+regimenline+"' " ;
			result2 = dbo.executeQuery(sql) ;

			result = dbo.executeQuery(dba.getProductRegimenCustomer(customerid,regimenline));

		  for (int j=0; j < result2.length ; j++)
		  {
		    for (int i = 0; i < result.length; i++)
		    {
              if (result2[j][0].equals(result[i][0]))
              {
			    ProductVO pr = new ProductVO();
			    pr.setProdId(result[i][0].trim());
			    pr.setImage(result[i][1]);
			    pr.setPrice(result[i][2]);
			    pr.setShortDesc(result[i][3]);
			    pr.setProdName(result[i][4]);
			    pr.setProdType(result[i][5]);
			    pr.setMasterProdType(result[i][6]);
			    pr.setBase(result[i][7]);
			    pr.setProductLine(result[i][8]);
			    pr.setIngrScore("0");
			    pr.setFunctionscore("0");
			    pr.setBumpScore("0");
			    pr.setTobeDeleted(false);
			    pr.setProductLinePrime(result[i][9]);
			    pr.setProductLaunchDate(result[i][10]);
			    pr.setProductRegimenDate(result[i][11]);
			    pr.setProductRegimenFlag(result[i][12]) ;
			    prodlist.add(pr);
			    break ;
              }
		    }
		  }
		} catch (Exception ex1) {
			ex1.printStackTrace() ;
		}
		finally {
			dbo.closeConnection() ;
		}

		return prodlist;
	}
    public Hashtable getMasterData()
    {
		String[][]  result        = null;
		String sql = "" ;
        Hashtable master = new Hashtable() ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select * from svb_view1 " ;
			result = dbo.executeQuery(sql) ;


		  for (int j=0; j < result.length ; j++)
		  {
		  	master.put(result[j][0],result[j][1]) ;
		  }
		} catch (Exception e)
		{
			System.out.println(e) ;
		}
		finally {
			dbo.closeConnection() ;
		}
		return master ;
    }
    
    public Hashtable getRatingsIndividual(String prodid, String type)
    {
		Hashtable ratings = new Hashtable();
		Hashtable ingrratings = new Hashtable();
		Hashtable funcratings = new Hashtable();
		AttribRatingVO ar = new AttribRatingVO();

		String[][]  rs2        = null;
		String sql = "" ;
        Hashtable master = new Hashtable() ;
//        String pmtype = (String) pr.getMasterProdType() ;
//        master = (Hashtable) checkMasterTypeValues(prodid) ;
        //System.out.println("I am in rec eng debug") ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select * from sv_ratings where sv_product_element_value in "+
                  " (select sv_ingredient_id from avon_product_ingredients where sv_product_id = '"+prodid+"' )  " +
				  " union "+
                  " select * from sv_ratings where sv_product_element_value in "+ 
                  " (select sv_product_base_id from temp_product_elements where sv_product_id = '"+prodid+"') " ;

			rs2 = dbo.executeQuery(sql) ;
			ingrratings = (Hashtable) getIngrRatingsCount(rs2) ;
			funcratings = (Hashtable) getFuncRatingsCount(rs2) ;
		  
		} catch (Exception e)
		{
			System.out.println(e) ;
		}
		finally {
			dbo.closeConnection() ;
		}
		if (type.equals("ingr"))
			ratings = ingrratings ;
		if (type.equals("func"))
			ratings = funcratings ;

		return ratings ;
    }
    public Hashtable getIngrRatingsCount(String[][] rs2)
    {
    	  Hashtable ratings = new Hashtable() ;
    	  Hashtable master = new Hashtable() ;
		  for (int j=0; j < rs2.length ; j++)
		  {
			String elid = rs2[j][0].trim() ;
			String elval = rs2[j][1].trim() ;
			String elconc = rs2[j][2].trim() ;
			String mtype = rs2[j][3].trim() ;
			String batid = rs2[j][4].trim() ;
			String batval = rs2[j][5].trim() ;
			String rating = rs2[j][6].trim() ;
			
			String key =elid.trim() + "-" + elval.trim() + "-" 	+ elconc.trim() + "-" + mtype.trim();
	        String value = rating.trim();
	        String sub =  batid + "-" + batval ;
//	        System.out.println(key+"\t"+sub+"\t"+value) ;        
			if (ratings.get(key) != null) 
            {
				 master = (Hashtable) ratings.get(key) ;
				 master.put(sub,value) ;
                 ratings.put(key, master);

           	     // 	  String tval = (String) ratings.get(key) ;
           	     // 	  StringTokenizer str = new StringTokenizer(tval,":") ;
           	     // 	  String val = str.nextToken() ;
           	     // 	  String coun = str.nextToken() ;
           	      	  
             	 //     value = String.valueOf(Integer.parseInt(val)
    			//			+ Integer.parseInt(value.trim()));
             	 //     coun = String.valueOf(Integer.parseInt(coun) + 1);
               	 //     ratings.put(key,master) ;
                      //System.out.println("333 "+key+"  "+sub+" "+value) ;           	 
            }
            else if (value != null && !value.trim().equals("NA"))   
            {
            	master = new Hashtable() ;
            	master.put(sub,value) ;
                 ratings.put(key, master);
                 //System.out.println("111 "+key+"  "+sub+" "+value) ;           	 
            } //end of ratings
		  } // for

    	return ratings;
    }
    public Hashtable getFuncRatingsCount(String[][] rs2)
    {
    	  Hashtable ratings = new Hashtable() ;
    	  Hashtable master = new Hashtable() ;
		  for (int j=0; j < rs2.length ; j++)
		  {
			String elid = rs2[j][0].trim() ;
			String elval = rs2[j][1].trim() ;
			String elconc = rs2[j][2].trim() ;
			String mtype = rs2[j][3].trim() ;
			String batid = rs2[j][4].trim() ;
			String batval = rs2[j][5].trim() ;
			String rating = rs2[j][6].trim() ;
			
	        String key =  batid + "-" + batval ;
	        String value = rating.trim();
			String sub =elid.trim() + "-" + elval.trim() + "-" 	+ elconc.trim() + "-" + mtype.trim();
	        
//	        System.out.println(key+"\t"+sub+"\t"+value) ;        
			if (ratings.get(key) != null) 
            {
				 master = (Hashtable) ratings.get(key) ;
				 master.put(sub,value) ;
                 ratings.put(key, master);
				 
				 /* 	  String tval = (String) ratings.get(key) ;
           	      	  StringTokenizer str = new StringTokenizer(tval,":") ;
           	      	  String isub = str.nextToken() ;
           	      	  String val = str.nextToken() ;
           	      	  String coun = str.nextToken() ;
           	      	  
             	      value = String.valueOf(Integer.parseInt(val)
    						+ Integer.parseInt(value.trim()));
             	      coun = String.valueOf(Integer.parseInt(coun) + 1);
               	      ratings.put(key,isub+":"+value+":"+coun) ;
               	 */
                      //System.out.println("333 "+key+"  "+sub+"  "+value) ;           	 
            }
            else if (value != null && !value.trim().equals("NA"))   
            {
            	 master = new Hashtable() ; 
				 master.put(sub,value) ;
                 ratings.put(key, master);
        //        System.out.println("111 "+key+"  "+sub+"  "+value) ;           	 
            } //end of ratings
		  } // for

    	return ratings;
    }
    public ArrayList getIngrRelated(String prodid)
    {
		String[][]  rs1        = null;
		String[][]  rs2        = null;
		String[][]  rs3        = null;
    	ArrayList master = new ArrayList() ;
    	Hashtable ingrfunc = new Hashtable() ;
    	Hashtable ingrrating = new Hashtable() ;
    	Hashtable ingrglossory = new Hashtable() ;
    	String sql1 = " select a.sv_ingredient_id, a.sv_function_id "+
                      " from sv_ingredients_function a, avon_product_ingredients b "+
                      " where a.sv_ingredient_id = b.sv_ingredient_id "+
                      " and b.sv_product_id = '"+prodid+"'" ;
    	
    	String sql2 = " select sv_product_element_value, count(*) from sv_ratings where sv_product_element_value in"+
                      " (select sv_ingredient_id from avon_product_ingredients where sv_product_id = '"+prodid+"' ) "+
                      " group by sv_product_element_value " ;
                      
    	String sql3 = " select sv_ingredient_id, sv_ingredient_glossory_text from sv_ingredients_glossory " ;
    	
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			
            rs1 = dbo.executeQuery(sql1) ;
            rs2 = dbo.executeQuery(sql2) ;
            rs3 = dbo.executeQuery(sql3) ;
            
            for (int i=0; i < rs1.length ; i++)
            {
            	//System.out.println("AAAAA "+rs1[i][0]) ;
            	if (rs1[i][1] == null || rs1[i][1].length() == 0)
            		rs1[i][1] = "N/A" ;
            	ingrfunc.put(rs1[i][0].trim(),rs1[i][1]) ;
            }
            
            for (int i=0; i < rs2.length ; i++)
           {
      //      	System.out.println("BBBBB "+rs2[i][0]) ;
            	if (rs2[i][1] == null || rs2[i][1].length() == 0)
            		rs2[i][1] = "N/A" ;
            	ingrrating.put(rs2[i][0].trim(),rs2[i][1]) ;
            }
            
            for (int i=0; i < rs3.length ; i++)
            {
     //       	System.out.println("CCCCC "+rs3[i][0]) ;
            	if (rs3[i][1] == null || rs3[i][1].length() == 0)
            		rs3[i][1] = "N/A" ;
            	ingrglossory.put(rs3[i][0].trim(),rs3[i][1]) ;
            }
            //System.out.println("21  "+ingrfunc.size()) ;
            //System.out.println("22  "+ingrrating.size()) ;
            //System.out.println("23  "+ingrglossory.size()) ;
            
            master.add(ingrfunc) ;
            master.add(ingrrating) ;
            master.add(ingrglossory) ;
            
		} catch (Exception e)
		{
			System.out.println(e) ;
		}
		finally {
			dbo.closeConnection() ;
		}
    	
    	return master ;
    }
	public static void main(String args[]) {
		HairRecommendationEngineDebug gp = new HairRecommendationEngineDebug();
//		String[] customers = new String[3];
	//	customers[0] = "C10062"; //leann
		//customers[1] = "C10085"; // theresa
	//	customers[2] = "C10130"; // vb2
	//	gp.getProduct("C10085", "01", "", "0002");
        String prodid = "163807" ;
        String prodids = "('"+prodid+"')" ; 
        String ingrstring = "(" ;
        
        Hashtable pht = (Hashtable) gp.getBeautyProductInfo(prodid) ;
        ProductVO pr = (ProductVO) pht.get(prodid) ;
       
        String pmtype = pr.getMasterProdType() ;
        if (pmtype == null || pmtype.length() == 0)
        	pmtype = "99" ;
        pht = (Hashtable) gp.getProductsIngredients("","prodId","",pht,prodids) ;
        String[][] ingrlist = (String[][]) ((ProductVO)pht.get(prodid)).getIngredients() ;
        
 /*     for (int i=0; i < ingrlist.length ; i++)
        {
        	if (i==ingrlist.length - 1)
        		ingrstring = ingrstring+"'"+ingrlist[i][0]+"' )" ;
        	else
        		ingrstring = ingrstring+"'"+ingrlist[i][0]+"'," ;
        }
        System.out.println("vb ingrstring "+ingrstring) ;
*/
        Hashtable iht = (Hashtable) gp.getRatingsIndividual(prodid,"ingr") ;
        
        for (int i=0; i < ingrlist.length; i++)
        {
        	String ingrkey = "02"+"-"+ingrlist[i][0]+"-NA-"+pmtype ;
        	boolean mtypecheck = false ;
        	//	System.out.println(ingrkey) ;
        	if (iht.get(ingrkey) != null)
        	{
              	Hashtable fht = (Hashtable) iht.get(ingrkey) ;
              	Enumeration fh = fht.keys() ;
              	while (fh.hasMoreElements())
              	{
              		String sub = (String) fh.nextElement() ;
              		String value = (String) fht.get(sub) ;
              		//System.out.println(ingrkey+"\t"+sub+"\t"+value) ;
              	}
        		mtypecheck = true ;
        	}
        	else
        	{
        		ingrkey = "02"+"-"+ingrlist[i][0]+"-NA-"+"99" ;
        		if (!mtypecheck && iht.get(ingrkey) != null)
            	{
               		Hashtable fht = (Hashtable) iht.get(ingrkey) ;
                  	Enumeration fh = fht.keys() ;
                  	while (fh.hasMoreElements())
                  	{
                  		String sub = (String) fh.nextElement() ;
                  		String value = (String) fht.get(sub) ;
                  		//System.out.println(ingrkey+"\t"+sub+"\t"+value) ;
                  	}
            		mtypecheck = true ;
            	}
        	}
//            		System.out.println(key+"\t"+ingrscore+"\t"+ingrcount+"\t"+ingrpercentage) ;
        } //for
      
/*        Hashtable fht = (Hashtable) gp.getRatingsIndividual(prodid,"func") ;
        //System.out.println("vb5 funcstring "+fht.size()+" ingrlist "+ingrlist.length) ;
        Enumeration feh = fht.keys() ;
        while (feh.hasMoreElements())
        {
        		String key = (String) feh.nextElement() ;
        		Hashtable iht = (Hashtable) fht.get(key) ;
        		int ingrscore = 0 ;
        		int ingrcount = 0 ;
        		int ingrpercentage = 0 ;
        		for (int i=0; i < ingrlist.length; i++)
        		{
        			boolean mtypecheck = false ;
        			String ingrkey = "02"+"-"+ingrlist[i][0]+"-NA-"+pmtype ;
        		//	System.out.println(ingrkey) ;
        			if (iht.get(ingrkey) != null)
        			{
        				ingrscore = ingrscore+Integer.parseInt((String) iht.get(ingrkey)) ;
        				ingrcount++ ;
        				mtypecheck = true ;
        			}
        			else
        			{
        				ingrkey = "02"+"-"+ingrlist[i][0]+"-NA-"+"99" ;
        				if (!mtypecheck && iht.get(ingrkey) != null)
            			{
            				ingrscore = ingrscore+Integer.parseInt((String) iht.get(ingrkey)) ;
            				ingrcount++ ;
            				mtypecheck = true ;
            			}
        			}
        			if (ingrcount != 0)
        				ingrpercentage = 100 * ingrscore / (ingrcount * 10);
        		}

        		//System.out.println(key+"\t"+ingrscore+"\t"+ingrcount+"\t"+ingrpercentage) ;
        }
*/	
	}
    public Hashtable getBumpScores()
    {
		String[][]  result        = null;
		String sql = "" ;
        Hashtable master = new Hashtable() ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select sv_attribute_value||sv_product_type_id, sv_bump_score from bump_rules " ;
			result = dbo.executeQuery(sql) ;


		  for (int j=0; j < result.length ; j++)
		  {
		  	master.put(result[j][0],result[j][1]) ;
		  }
		} catch (Exception e)
		{
			System.out.println(e) ;
		}
		finally {
			dbo.closeConnection() ;
		}
		return master ;
    }
    public Hashtable getProductUsage(String prodstring)
    {
		String[][]  result        = null;
		String sql = "" ;
        Hashtable master = new Hashtable() ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			
			sql = " select a.sv_product_id, lower(b.sv_product_usage_desc) "+ 
			      " from temp_product_elements a, avon_product_usage b "+
			      " where a.sv_product_usage_id = b.sv_product_usage_id "+
			      " and a.sv_product_id in "+prodstring ;

			//sql = " select sv_product_id, sv_product_usage_id from temp_product_elements where sv_product_id in "+prodstring ;
			result = dbo.executeQuery(sql) ;


		  for (int j=0; j < result.length ; j++)
		  {
		  	master.put(result[j][0].trim(),result[j][1]) ;
		  }
		} catch (Exception e)
		{
			System.out.println(e) ;
		}
		finally {
			dbo.closeConnection() ;
		}
		return master ;
    }
}//end of class

