package com.svb.ratings.javabeans;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVBeautydba;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;

/*
 * // header - edit "Data/yourJavaHeader" to customize // contents - edit
 * "EventHandlers/Java file/onCreate" to customize //
 */
public class EleminateProducts {
	
	//Added for Elimination reason - Shivam - 26-08-2023 - Start
	//Note: This map is only used for Single product Api
	private List<String> allergicIngredients;

	public List<String> getAllergicIngredients() {
		return allergicIngredients;
	}

	public void setAllergicIngredients(List<String> allergicIngredients) {
		this.allergicIngredients = (ArrayList<String>) allergicIngredients;
	}
	//Added for Elimination reason - Shivam - 26-08-2023 - End

	private static Log log = LogFactory.getLog(EleminateProducts.class);
	SVBeautydba dba;
	SVDBOperation dbo;
	CustomerProfileVO cp;
	Hashtable allergies = new Hashtable();
	boolean externalDBA = false;

	public void initialize() {
		try {
			dba = new SVBeautydba();
			dbo = new SVDBOperation();
			dbo.createConnection();

		} catch (Exception e) {
			e.printStackTrace() ;
		}
	}

	public void setDBA(SVDBOperation dbo){
		this.dbo 			= dbo;
		this.externalDBA 	= true;
		dba = new SVBeautydba();
	}

	public void destroy() {
		try {
			dbo.closeConnection();

		} catch (Exception e) {
			e.printStackTrace() ;
		}
	}


	public void getAllergies() {

		try {
			dbo = new SVDBOperation();
			dba = new SVBeautydba() ;
			dbo.createConnection();
		String result[][] = dbo.executeQuery(dba.getIngredients_Allergies());
		for (int i = 0; i < result.length; i++) {
			String ingr = result[i][0];
			String ingrconc = result[i][1];
			String allergy = result[i][2];
			if (allergy == null)
				allergy = "";
			allergies.put(ingr, allergy);
		}
		} catch (Exception ex1) {
			ex1.printStackTrace() ;
		}
		finally {
			dbo.closeConnection() ;
		}
		//log.debug("allergy list = "+allergies);
	}

	public boolean isThisAllergy(String ingr) {
		////logger.log(Level.DEBUG,"in is this allergy");
		String allergy = "";
		boolean flag = false;
		ArrayList all = cp.getAllergyList();

		if (allergies.get(ingr) != null)
			allergy = (String) allergies.get(ingr);

		for (int i = 0; i < all.size(); i++) {
			////logger.log(Level.DEBUG,"allergy for customer is"+all.elementAt(i));
			if (all.get(i).equals(allergy)) {
				////logger.log(Level.DEBUG,"yess this is an allergy ingredient");
	//			System.out.println("allergy "+all.get(i)+"  "+allergy+"  "+ingr) ;
				flag = true;
				//Added for Elimination reason - Shivam - 26-08-2023 - Start
				if(Objects.nonNull(allergicIngredients) && !allergicIngredients.contains(allergy)) {
					System.out.println("allergy::::::::::::"+allergy);
					allergicIngredients.add(allergy); //Added for Elimination reason - Shivam - 26-08-2023
				}
				//Added for Elimination reason - Shivam - 26-08-2023 - End
				break;
			}
		}

		return flag;
	}

	public ProductVO[] eliminateBasedOnIngredients(ProductVO[] prod,
			Hashtable ingredients) {

	//	log.debug("eleminate ingredients list = "+ingredients);
		ProductVO tmpproduct = new ProductVO();
		
		
		for (int i = 0; i < prod.length; i++) {
			List ingredientList = new ArrayList<>();
			tmpproduct = (ProductVO) prod[i]; //get the product related to this
											  // ID
			
			
	// log.debug("product id = " + tmpproduct.getProdId() +"product name = " + tmpproduct.getProdName() + "brand id = " + tmpproduct.getProductLine());
			//		//logger.log(Level.DEBUG,"inside eliminateBasedOnIngredients"+prodid);
            

			String ingr[][] = tmpproduct.getIngredients();
			if (ingr != null)
				for (int j = 0; j < ingr.length; j++) {
				//	if (tmpproduct.getProdId().equals("76347"))
                      //    System.out.println("prod ingr vb "+prodid+"  "+ingr[j][0]) ;
					String tmp1 = "";

				/**
				 * Added by Synoptek (20-03-2020)
				 * Commented condition which eliminates products
				 * */	
			/* ADDED BY SYNOPTEK::HARESH for eliminate based on Ingredients
			 		if (producttype == 5)
						producttype = 4; */

/*					if (producttype == 1 || producttype == 2
							|| producttype == 3 || producttype == 4) //check
																	 // whther
																	 // it's one
																	 // of the
																	 // master
																	 // types
						tmp1 = "02" + "-" + ingr[j][0] + "-" + ingr[j][1] + "-"
								+ producttype + ""; //"02-ingr-ingrconc-producttype"
*/								
				
				//SYNOPTEK::HARESH	if (producttype == 4)
				//SYNOPTEK::HARESH		tmp1 = "02" + "-" + ingr[j][0] + "-" + ingr[j][1] + "-"
				//SYNOPTEK::HARESH	+ "0"+producttype + ""; //"02-ingr-ingrconc-producttype" VB fix
				//SYNOPTEK::HARESH	else 
						tmp1 = "02" + "-" + ingr[j][0] + "-" + ingr[j][1]
								+ "-99"; //"02-ingr-ingrconc-99"
                //    System.out.println("vb eleminate "+tmp1+ " "+prodid) ;
                //    System.out.println("vb eleminate size "+ingredients.size()) ;
					////logger.log(Level.DEBUG,"(((((((((("+prodid);
					////logger.log(Level.DEBUG,"((((((((((("+tmp1);
				//	if (tmpproduct.getProdId().equals("76347"))
				//	{
				//		System.out.println("in elemem 33 "+ingredients.get(tmp1)+"  "+"  "+ingr[j][0]+"  "+isThisAllergy(ingr[j][0]));
				//	}
						
//						if(tmpproduct.getProdName().equals("Pre-Treatment Face Toner")) {
//							System.out.println("(ingredients.get(tmp1):"+(ingredients.get(tmp1)));
//							System.out.println("(isThisAllergy:"+isThisAllergy(ingr[j][0]));
//							System.out.println("(ingr[j][0]:"+ingr[j][0]);
//							System.out.println("tmp1:"+tmp1);
//						}
						
						
					if (ingredients.get(tmp1) != null
							|| isThisAllergy(ingr[j][0])) //if the product
														  // ingredient is
														  // present in the
														  // ingredients list
														  // that is to be
														  // eliminated , set
														  // the flag as false
					{
						//log.debug("product  id "+tmpproduct.getProdId() +"  name = "+tmpproduct.getProdName());
						//log.debug("ingr id  "+ingr[j][0]+ " name "+ingr[j][2]);
						if(ingredients.get(tmp1) != null  && isThisAllergy(ingr[j][0])) {
							tmpproduct.setReason(ConstantDeclarations.ZERO_INGR_SCO_ALLERGY);
							tmpproduct.setEliminatedIngrName(ingr[j][0]);
						}
						else if(ingredients.get(tmp1) != null){
							tmpproduct.setReason(ConstantDeclarations.ZERO_INGR_SCO);
							tmpproduct.setEliminatedIngrName(ingr[j][0]);
							ingredientList.add(tmp1);
//							System.out.println(ingredientList.toString());
							
						}
						else if (isThisAllergy(ingr[j][0])) {
							tmpproduct.setReason(ConstantDeclarations.ALLERGY);
							tmpproduct.setEliminatedIngrName(ingr[j][0]);
						}
						// ////logger.log(Level.DEBUG,"********************");
						////logger.log(Level.DEBUG,"sorry this is going to get
						// deleted"+prodid);
			//			if (tmpproduct.getProdId().equals("76347"))
			//				System.out.println("in elimeiante 222 "+tmpproduct.getProdId()+"  "+flag) ;
						tmpproduct.setTobeDeleted(true);
						/*
						 * Commenting this useless if block - Shivam - 22/07/2024
						 * if(tmpproduct.getProdName().equals("Pre-Treatment Face Toner")) {
							System.out.println("==============setTobeDeleted : 1===================");
							System.out.println("tmpproduct.getProdName() :" + tmpproduct.getProdName());
							System.out.println("REASON " + tmpproduct.getReason());
						}*/
						prod[i] = tmpproduct;
//						break;
					}
					if(!(ingredientList == null) && !ingredientList.isEmpty() && (j == (ingr.length - 1))) {
						tmpproduct.setEliminatedIngrList(ingredientList);
					}
				}//end of for

		}

		return prod;

	}

	public ProductVO[] getProducts(CustomerProfileVO cp, ProductVO[] prod,
			Hashtable ingredients) {
//		if(!externalDBA)
//			initialize();
		this.cp = cp;
		getAllergies();
		prod = eliminateBasedOnIngredients(prod, ingredients);
//		ArrayList vbprods2 = new ArrayList();
		//System.out.println("ALL PRODUCTS IN " + prod.length);
		for(int i = 0; i< prod.length; i++) {
//			if (!productVOList[i].isTobeDeleted()) {
//				vbprods2.add(productVOList[i]);
				//System.out.println("PROD ALL IN " + prod[i]);
//			}
			
		}
		String age = cp.getAge();
		String cusskintype = cp.getSkinType();
		ArrayList cusskincond = cp.getSkinConditionList();
		ArrayList cusskinconcern = cp.getSkinConcernList();
		ArrayList cusspecification = cp.getSpecification();

		Hashtable regimen = new Hashtable();
		Hashtable hash = new Hashtable();

		////logger.log(Level.DEBUG,"%%%"+cp.getCustomerId());
		////logger.log(Level.DEBUG,"51-"+cusskintype);

		ArrayList vect = new ArrayList();

		//Age between below 30 and productline is Anewpositivity
		String str = "55-G001;08-0007";
		vect.add(str);
		//Age between 31-49 and product line clearskin and skincondition is not
		// Menopause Related and skintype is not oily
		//					vect.add("55-G002;08-0003;NA&51-T004&52-D002");
		vect.add("55-G002;08-0003;NA&51-T004&52-D002");
		//Age over 50 and product line clearskin and skincondition is not
		// Menopause Related and skintype is not oily
		//          vect.add("55-G003;08-0003;NA&51-T004&52-D002");
		//          vect.add("55-G003;08-0003;NA&51-T004&52-D002");
		vect.add("55-G003;08-0003;NA51-T004&52-D002");
		//Is sperate night time moisturizer used if no dont show that .
		//					vect.add("07-0002;Q004-A009");

		hash.put("51-" + cusskintype, "1");
		for (int i = 0; i < cusskincond.size(); i++)
			hash.put("52-" + cusskincond.get(i), "1");
		for (int i = 0; i < cusskinconcern.size(); i++)
			hash.put("53-" + cusskinconcern.get(i), "1");
		if (age != null)
			hash.put("55-" + age.trim(), "1");
		for (int i = 0; i < cusspecification.size(); i++)
			hash.put("60-" + cusspecification.get(i), "1");

		////logger.log(Level.DEBUG,"age is //////"+age.trim());
		////logger.log(Level.DEBUG,"age yuuyuus //////"+age.trim());
		
		//System.out.println("HASH " + hash.size());

		Enumeration en = regimen.keys();

		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			//System.out.println("INSIDE WHILE");
			////logger.log(Level.DEBUG,"&&&"+key+"-"+regimen.get(key)+"*");
			hash.put(key + "-" + regimen.get(key), "1");

		}
		

		for (int i = 0; i < prod.length; i++) {
			ProductVO pr = prod[i];
			String prodtype = pr.getProdType();
			String prodline = pr.getProductLine();
			String prodtypekey = "";
			String prodlinekey = "";
			//				 //logger.log(Level.DEBUG,"prodid"+pr.prodid);
			//			 //logger.log(Level.DEBUG,"prodtype"+prodtype);
			//	 //logger.log(Level.DEBUG,"prodline"+prodline);
			if (prodtype != null) {
				prodtypekey = "07-" + prodtype.trim();
				hash.put(prodtypekey, "1");
				

			}
			if (prodline != null) {
				prodlinekey = "08-" + prodline.trim();
				hash.put(prodlinekey, "1");
				
			}
			for (int j = 0; j < vect.size(); j++) {
				boolean flag = true;
				
				String str1 = (String) vect.get(j);
				StringTokenizer stk = new StringTokenizer(str1, ";");
				////logger.log(Level.DEBUG,"1");

				while (stk.hasMoreTokens()) {
					
					String st = stk.nextToken();
					//	 	 //logger.log(Level.DEBUG,"STTTTTTTTTTTTT is"+st);

					if (st.substring(0, 2).equals("NA")) //if the skin concern
														 // or condition matches
														 // then dont eliminate
					{
						//     //logger.log(Level.DEBUG,"vb TTTTTT is"+st);
						StringTokenizer stk1 = new StringTokenizer(st, "&");
						stk1.nextToken(); //this for moving NA away
						boolean flag1 = false;
						while (stk1.hasMoreTokens()) {
						
							String st2 = stk1.nextToken();
							if (st2.substring(0, 2).equals("NA")) {
								st2 = st2.substring(2, st2.length());
								
							}
							if (hash.get(st2) != null) {
								
								//										           flag1 = false ;
								if (!st2.equals("52-D002")) {
									
									flag1 = false;
								}
								else {
									
									flag1 = true;
								}
							}
						}
						if (flag1) {
							flag = false;
							
						}
						else {
							flag = true;
							
						}

					} else if (hash.get(st) == null) {
						//   //logger.log(Level.DEBUG,"vb ooooooo is"+st);
						
						flag = false;
						break;
					}

				}

				//			//logger.log(Level.DEBUG,"flag value "+flag+"product flag
				// "+pr.tobedeleted) ;
			//	if (pr.getProdId().equals("76347"))
			//		System.out.println("in elimeiante "+pr.getProdId()+"  "+flag) ;
				if (flag) {
					//    //logger.log(Level.DEBUG,"this product needs to be
					// eliminated");
					/*
					 * Commenting useless if block - Shivam - 22/07/2024
					 * if(pr.getProdName().equals("Facial Wash Oily/Problem Skin")) {
						System.out.println("==============setTobeDeleted : 2===================");
					}*/
					pr.setTobeDeleted(true);
					break;

				}

			}
			if (prodlinekey != null)
				hash.remove(prodlinekey);
			if (prodtypekey != null)
				hash.remove(prodtypekey);

			// added as per joe mail
			if (hash.get("52-D002") == null && prodline.trim().equals("0003")) {
				
				/*
				 * Commenting useless if block - Shivam - 22/07/2024
				 * if(pr.getProdName().equals("Facial Wash Oily/Problem Skin")) {
					System.out.println("==============setTobeDeleted : 3===================");
				}*/
				pr.setTobeDeleted(true);
			}
			prod[i] = pr;

		}
//		if(!externalDBA)
//			destroy();
		ArrayList vbprods2 = new ArrayList();
		for(int i = 0; i< prod.length; i++) {
			if (prod[i].isTobeDeleted()) {
				vbprods2.add(prod[i]);
			}
			
		}
		return prod;
	}

}

