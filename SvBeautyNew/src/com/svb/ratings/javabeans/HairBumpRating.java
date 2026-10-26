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
public class HairBumpRating {
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
		String ethnicity = cp.getEthnicity();
		String haircolor = cp.getHaircolor() ;
		String hairtype = cp.getHairtype() ;
		String hairthickness = cp.getHairthickness() ;
		String hairtexture = cp.getHairtexture() ;
		String age = cp.getAge() ;
		
		ArrayList hairprocess = cp.getHairprocess();
		ArrayList hairconditions = cp.getHairconditions();
		ArrayList hairstyles = cp.getHairstyles();

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

				//begin of skintype
				if (bmpquery.trim().equals("71") && bmpanswer.equals(haircolor)) {
					products[i].setBumpScore(String
							.valueOf(Float.parseFloat(products[i].getBumpScore())
									+ getBumpScore(bmpprodtype,
											bmpprodmastertype, bmpingrfunction,
											bmpprodline, prodtype,
											prodmastertype, functions,
											prodline, prodid)));

				}
				if (bmpquery.trim().equals("72") && bmpanswer.equals(hairtype)) {
					products[i].setBumpScore(String
							.valueOf(Float.parseFloat(products[i].getBumpScore())
									+ getBumpScore(bmpprodtype,
											bmpprodmastertype, bmpingrfunction,
											bmpprodline, prodtype,
											prodmastertype, functions,
											prodline, prodid)));

				}
				if (bmpquery.trim().equals("74") && bmpanswer.equals(hairthickness)) {
					products[i].setBumpScore(String
							.valueOf(Float.parseFloat(products[i].getBumpScore())
									+ getBumpScore(bmpprodtype,
											bmpprodmastertype, bmpingrfunction,
											bmpprodline, prodtype,
											prodmastertype, functions,
											prodline, prodid)));

				}
				if (bmpquery.trim().equals("75") && bmpanswer.equals(hairtexture)) {
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
				for (int j1 = 0; j1 < hairconditions.size(); j1++) {
					
						if (bmpquery.trim().equals("77") && bmpanswer.equals(hairconditions.get(j1))) {
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
				for (int j1 = 0; j1 < hairprocess.size(); j1++) {

					if (bmpquery.trim().equals("73") && bmpanswer.equals(hairprocess.get(j1))) {
						products[i].setBumpScore(String.valueOf(Float
								.parseFloat(products[i].getBumpScore())
								+ getBumpScore(bmpprodtype, bmpprodmastertype,
										bmpingrfunction, bmpprodline, prodtype,
										prodmastertype, functions, prodline,
										prodid)));

					}
				}
				for (int j1 = 0; j1 < hairstyles.size(); j1++) {

					if (bmpquery.trim().equals("76") && bmpanswer.equals(hairstyles.get(j1))) {
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
		if (prodtypeflag && prodmastertypeflag && functionflag && prodlineflag) {
			score = bumpscore;
		}
		return score;
	}

	public ProductVO[] getProducts(CustomerProfileVO cp, ProductVO[] products) {
		try {
			//initialize();
			//commented for performance improvement
			//ingrFunctions();
			products = getBumpScore(cp, products);
			//destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return products;
	}

}

