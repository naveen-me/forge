package com.svb.ratings.javabeans;

import java.util.*;
import com.svb.common.dba.*;
import com.svb.common.valueobjects.* ;
import com.svb.common.ConstantDeclarations;
import java.sql.ResultSet;

/*
 * // header - edit "Data/yourJavaHeader" to customize // contents - edit
 * "EventHandlers/Java file/onCreate" to customize //
 */
public class BumpRating {
	private Hashtable ingrfunctions = new Hashtable(); //ingredientid-concentration is the key
									 // and ingredient functions List is the
									 // value

	private SVBeautydba dba;

	private SVDBOperation dbo;

	private float bumpscore = 3.0f; //this value has been set in the databse
									// table bump_ratings .


	public void initialize() throws Exception {
		dba = new SVBeautydba();
		dbo = new SVDBOperation();
		dbo.createConnection();
	}

	public void setDBA(SVDBOperation dbo){
		dba = new SVBeautydba();
		this.dbo = dbo;
	}

	public void destroy() throws Exception {
		dbo.closeConnection();
	}


	public void ingrFunctions() {

	//String result[][] = dbo.executeQuery(dba.getIngrFunctions());

	String oldkey = "";
	ArrayList funcList = new ArrayList();
	int i = 0;
	try{
			dbo = new SVDBOperation();
			dba = new SVBeautydba() ;
			dbo.createConnection();
		ResultSet rs = dbo.executeQueryRS(dba.getIngredientsFunction());
	    while(rs.next()){

		String id		=  rs.getString("sv_ingredient_id");
		String conc 	= rs.getString("sv_ingredient_conc");
		String function = rs.getString("sv_function_id");
		if (id == null)
			id = "";
		if (conc == null)
			conc = "";
		String key = id.trim() + "-" + conc.trim();
		if (i != 0 && !key.equals(oldkey)) {
			funcList.add(function);
			ingrfunctions.put(oldkey, funcList);
			funcList = new ArrayList();
		} else
			funcList.add(function);
		oldkey = key;
		i++;
	}
	}catch(Exception e){
		e.printStackTrace();
	}
	finally {
		dbo.closeConnection() ;
	}
	ingrfunctions.put(oldkey, funcList);
}

	public ProductVO[] getBumpScore(CustomerProfileVO cp,

			ProductVO[] products) {
		Hashtable regimenquery = new Hashtable();
		String result[][] = new String[0][0];
		String skintype = cp.getSkinType();
		String age = cp.getAge();
		String ethnicity = cp.getEthnicity();
		ArrayList skincondition = cp.getSkinConditionList();
		ArrayList skinconcern = cp.getSkinConcernList();
		ArrayList eyeconcern = cp.getEyeConcernList();

		try {
			dbo = new SVDBOperation();
			dba = new SVBeautydba() ;
			dbo.createConnection();
		result = dbo.executeQuery(dba.getBumpRules());
		for (int i = 0; i < products.length; i++) {

			String prodid = products[i].getProdId();
			String prodtype = products[i].getProdType();
			String prodmastertype = products[i].getMasterProdType();
			String prodline = products[i].getProductLine();
			String prodingr[][] = products[i].getIngredients();
			ArrayList functions = new ArrayList();

			boolean acneflag = false;
			float acscore = 0.0f;

			if (prodingr != null)
				for (int k = 0; k < prodingr.length; k++) {
					if (prodingr[k][1] != null && prodingr[k][1].equals("NA")) {
						prodingr[k][1] = "NA";
					}

					String ingrconc = prodingr[k][0] + "-" + prodingr[k][1];
					ArrayList functions1 = (ArrayList) ingrfunctions.get(ingrconc);
					if (functions1 != null) {
						for (int j = 0; j < functions1.size(); j++) {
							String function = (String) functions1.get(j);
							if (function != null
									&& function.trim().length() != 0)
								functions.add(function);

						}//end of functions available
					} //end of if
				}//end of prodingr functions available
//			result = dbo.executeQuery(dba.getBumpRules());
			for (int i1 = 0; i1 < result.length; i1++) {

				String bmpquery = result[i1][0];
				String bmpanswer = result[i1][1];
				String bmpprodtype = result[i1][2];
				String bmpprodmastertype = result[i1][3];
				String bmpingrfunction = result[i1][4];
				String bmpprodline = result[i1][5];
				bumpscore = Float.parseFloat(result[i1][6]);

				// if the regimen query and answer of the bump rules table r
				// present in the customer regimen

				if (regimenquery.get(bmpquery.trim()) != null
						&& ((String) regimenquery.get(bmpquery.trim()))
								.equals(bmpanswer.trim())) {
					float bmpscore = getBumpScore(bmpprodtype,
							bmpprodmastertype, bmpingrfunction, bmpprodline,
							prodtype, prodmastertype, functions, prodline,
							prodid);
					products[i].setBumpScore(String.valueOf(Float
							.parseFloat(products[i].getBumpScore())
							+ bmpscore));

				}//end of if regimen
				//begin of skintype
				if (bmpquery.trim().equals(ConstantDeclarations.SKIN_TYPE_ID) && bmpanswer.equals(skintype)) {
					products[i].setBumpScore(String
							.valueOf(Float.parseFloat(products[i].getBumpScore())
									+ getBumpScore(bmpprodtype,
											bmpprodmastertype, bmpingrfunction,
											bmpprodline, prodtype,
											prodmastertype, functions,
											prodline, prodid)));

				}
				if (bmpquery.trim().equals("55") && bmpanswer.equals(age)) {
					products[i].setBumpScore(String
							.valueOf(Float.parseFloat(products[i].getBumpScore())
									+ getBumpScore(bmpprodtype,
											bmpprodmastertype, bmpingrfunction,
											bmpprodline, prodtype,
											prodmastertype, functions,
											prodline, prodid)));

				}
				if (bmpquery.trim().equals("58") && bmpanswer.equals(ethnicity)) {
					products[i].setBumpScore(String
							.valueOf(Float.parseFloat(products[i].getBumpScore())
									+ getBumpScore(bmpprodtype,
											bmpprodmastertype, bmpingrfunction,
											bmpprodline, prodtype,
											prodmastertype, functions,
											prodline, prodid)));

				}
				for (int j1 = 0; j1 < skincondition.size(); j1++) {
					if (!acneflag) {
						if (bmpquery.trim().equals(ConstantDeclarations.SKIN_CONDITION_ID) && bmpanswer.equals(skincondition.get(j1))) {
							acscore = getBumpScore(bmpprodtype,
									bmpprodmastertype, bmpingrfunction,
									bmpprodline, prodtype, prodmastertype,
									functions, prodline, prodid);
							products[i].setBumpScore(String.valueOf(Float
									.parseFloat(products[i].getBumpScore())
									+ acscore));
							if (acscore > 0.0f)
								acneflag = true;
						}
					}
				}
				for (int j1 = 0; j1 < skinconcern.size(); j1++) {

					if (bmpquery.trim().equals(ConstantDeclarations.SKIN_CONCERN_ID) && bmpanswer.equals(skinconcern.get(j1))) {
						products[i].setBumpScore(String.valueOf(Float
								.parseFloat(products[i].getBumpScore())
								+ getBumpScore(bmpprodtype, bmpprodmastertype,
										bmpingrfunction, bmpprodline, prodtype,
										prodmastertype, functions, prodline,
										prodid)));

					}
				}
				for (int j1 = 0; j1 < eyeconcern.size(); j1++) {

					if (bmpquery.trim().equals("54") && bmpanswer.equals(eyeconcern.get(j1))) {
						products[i].setBumpScore(String.valueOf(Float
								.parseFloat(products[i].getBumpScore())
								+ getBumpScore(bmpprodtype, bmpprodmastertype,
										bmpingrfunction, bmpprodline, prodtype,
										prodmastertype, functions, prodline,
										prodid)));

					}
				}

			}

		}//end of for all products

		} catch (Exception ex1) {
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
		return products;
	}

	public float getBumpScore(String bmpprodtype, String bmpprodmastertype,
			String bmpingrfunction, String bmpprodline, String prodtype,
			String prodmastertype, ArrayList functions, String prodline,
			String prodid) {

		float score = 0.0f;
		boolean prodtypeflag = true;
		boolean prodmastertypeflag = true;
		boolean functionflag = true;
		boolean prodlineflag = true;
        if (prodid.equals("155387"))
        {
        	System.out.println(prodid+" pt "+prodtype+" pl "+prodline+"  "+"bmpprodtype "+bmpprodtype+" "+"bmpprodmastertype "+bmpprodmastertype+" "+"bmpprodline "+bmpprodline) ;
        }
		//	////logger.log(Level.DEBUG,"bmptype"+bmpprodtype+",bmpmaster
		// type"+bmpprodmastertype+",bmp prod line"+bmpprodline);
		//	////logger.log(Level.DEBUG,"type"+prodtype+",master type"+prodmastertype+",
		// prod line"+prodline);

		if (bmpprodtype != null && bmpprodtype.equals(prodtype)) {
			prodtypeflag = true;
			////logger.log(Level.DEBUG,"prodtype flag"+prodtype);
		} else if (bmpprodtype != null && bmpprodtype.trim().length() != 0
				&& !bmpprodtype.equals(prodtype))
			prodtypeflag = false;

		//////logger.log(Level.DEBUG,"before prodmaster type");
		if (bmpprodmastertype != null
				&& bmpprodmastertype.trim().equals(prodmastertype)) {
			prodmastertypeflag = true;
			////logger.log(Level.DEBUG,"prodmastertype flag"+prodmastertype);
		} else if (bmpprodmastertype != null
				&& bmpprodmastertype.trim().length() != 0
				&& !bmpprodmastertype.equals(prodmastertype))
			prodmastertypeflag = false;

		//								//logger.log(Level.DEBUG,"before function "+functions.size());
		if (functions != null) {
			if (functions.size() == 0 && bmpingrfunction != null
					&& bmpingrfunction.trim().length() > 0) {
				functionflag = false;
			}
			for (int j = 0; j < functions.size(); j++) {
				if (bmpingrfunction != null
						&& bmpingrfunction.equals(functions.get(j))) {
					functionflag = true;
					break; //break out of function loop since the function is
				} else if (bmpingrfunction != null
						&& bmpingrfunction.trim().length() != 0
						&& !bmpingrfunction.equals(functions.get(j)))
					functionflag = false;
			}
		} else if (bmpingrfunction != null && functions == null)
			functionflag = false;

		if (prodline == null)
			prodline = "";
		if (bmpprodline != null && bmpprodline.trim().length() != 0
				&& (bmpprodline.trim()).equals(prodline.trim())) {
			prodlineflag = true;
		} else if (bmpprodline != null && bmpprodline.trim().length() != 0
				&& !bmpprodline.equals(prodline))
			prodlineflag = false;
		if (prodtypeflag && prodmastertypeflag && prodlineflag) {
			score = bumpscore;
		}
		return score;
	}

	public ProductVO[] getExtraBumpTreatment(CustomerProfileVO cp,
			ProductVO[] products) {
		ArrayList cskinconcern = new ArrayList();
		ArrayList cskincondition = new ArrayList();
		cskinconcern = (ArrayList) cp.getSkinConcernList();
		cskincondition = (ArrayList) cp.getSkinConditionList();
		boolean acneflag = false;
		boolean darkcircleflag = false;
		boolean furrowflag = false;
		boolean dullflag = false;
		boolean neckflag = false;
		boolean agefaceflag = false;
		boolean agelipsflag = false;
		boolean enlargeflag = false;
		boolean unevenflag = false ;
		boolean lossflag = false ;
        String age = (String) cp.getAge();

		for (int j = 0; j < cskincondition.size(); j++) {
			if (((String) cskincondition.get(j)).trim().equals(ConstantDeclarations.SKIN_CONDITION_AA))
				acneflag = true;
		}
		for (int i = 0; i < cskinconcern.size(); i++) {
			/*
			 * commented unused variable for performance improvement - Shivam - 22/07/2024
			 * if (((String) cskinconcern.get(i)).trim().equals(ConstantDeclarations.SKIN_CONCONCERN_FLE))
				finelineflag = true;*/
			if (((String) cskinconcern.get(i)).trim().equals(ConstantDeclarations.SKIN_CONCONCERN_DCE))
				darkcircleflag = true;
			if (((String) cskinconcern.get(i)).trim().equals(ConstantDeclarations.SKIN_CONCONCERN_FFH))
				furrowflag = true;
			if (((String) cskinconcern.get(i)).trim().equals(ConstantDeclarations.SKIN_CONCONCERN_DSK))
				dullflag = true;
			if (((String) cskinconcern.get(i)).trim().equals(ConstantDeclarations.SKIN_CONCONCERN_ASF))
				agefaceflag = true;
			if (((String) cskinconcern.get(i)).trim().equals(ConstantDeclarations.SKIN_CONCONCERN_NWR))
				neckflag = true;
			if (((String) cskinconcern.get(i)).trim().equals(ConstantDeclarations.SKIN_CONCONCERN_ALP))
				agelipsflag = true;
			/*
			 * commented unused variable for performance improvement - Shivam - 22/07/2024
			 * if (((String) cskinconcern.get(i)).trim().equals(ConstantDeclarations.SKIN_CONCONCERN_ASH))
				agehandsflag = true;*/
			if (((String) cskinconcern.get(i)).trim().equals(ConstantDeclarations.SKIN_CONCONCERN_EFP))
				enlargeflag = true;
			if (((String) cskinconcern.get(i)).trim().equals("C019"))
				enlargeflag = true;
			if (((String) cskinconcern.get(i)).trim().equals("C020"))
				lossflag = true;
		}
		for (int k = 0; k < products.length; k++) {
			String prodid = (String) products[k].getProdId();
			float bscore = 0.0f;
			if ((age.equals("G002") || age.equals("G003")) && prodid.equals("SK10000321") && acneflag)
			{
				bscore = 8.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore)) ;
			}
/*			if (acneflag && prodid.trim().equals("SK10000248")) {
				bscore = 6.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore)) ;
			}*/
/*			if (neckflag && prodid.trim().equals("SK10000253")) {
				bscore = 3.0f;
				products[k].setBumpScore( String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			} */
			if (neckflag && prodid.trim().equals("SK10000303")) {
				bscore = 9.0f;
				products[k].setBumpScore( String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (lossflag && prodid.trim().equals("SK10000303")) {
				bscore = 9.0f;
				products[k].setBumpScore( String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (lossflag && prodid.trim().equals("SK10000354")) {
				bscore = 6.0f;
				products[k].setBumpScore( String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (lossflag && prodid.trim().equals("SK10000355")) {
				bscore = 6.0f;
				products[k].setBumpScore( String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (enlargeflag && prodid.trim().equals("SK10000306")) {
				bscore = 6.0f;
				products[k].setBumpScore( String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (agefaceflag && prodid.trim().equals("SK10000352")) {
				bscore = 9.0f;
				products[k].setBumpScore( String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (furrowflag && prodid.trim().equals("SK10000083")) {
				bscore = 6.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (furrowflag && prodid.trim().equals("SK10000314")) {
				bscore = 9.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (dullflag && prodid.trim().equals("SK10000304")) {
				bscore = 6.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (furrowflag && prodid.trim().equals("SK10000101")) {
				bscore = 0.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (agelipsflag && prodid.trim().equals("SK10000101")) {
				bscore = 3.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (dullflag && prodid.trim().equals("SK10000063")) {
				bscore = 9.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (unevenflag && prodid.trim().equals("SK10000029")) {
				bscore = 6.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (dullflag && prodid.trim().equals("SK10000355")) {
				bscore = 6.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (agefaceflag && prodid.trim().equals("SK10000247")) {
				bscore = 3.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (agelipsflag && prodid.trim().equals("SK10000080")) {
				bscore = 3.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (agelipsflag && prodid.trim().equals("SK10000074")) {
				bscore = 3.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
			if (darkcircleflag && prodid.trim().equals("SK10000072")) {
				bscore = 6.0f;
				products[k].setBumpScore(String.valueOf(Float
						.parseFloat(products[k].getBumpScore())
						+ bscore));
			}
		}
		return products;
	}

	public ProductVO[] getProducts(CustomerProfileVO cp, ProductVO[] products) {
		try {
			//initialize();
			//commented for performance improvement
			//ingrFunctions();
			products = getBumpScore(cp, products);
			products = getExtraBumpTreatment(cp, products);
			//destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return products;
	}

}

