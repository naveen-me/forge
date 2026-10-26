package com.svb.analysis.javabeans;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.ArrayList;

import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVBeautydba;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.valueobjects.FuncDataVO;
import com.svb.common.valueobjects.PopUpDataVO;

/*
 * // header - edit "Data/yourJavaHeader" to customize // contents - edit
 * "EventHandlers/Java file/onCreate" to customize //
 */

public class HairPopupRatings {

	SVBeautydba dba;

	SVDBOperation dbo;

	Hashtable ingredientnames = new Hashtable();

	Hashtable skintype = new Hashtable();

	Hashtable skinconcern = new Hashtable();

	Hashtable skincondition = new Hashtable();

	Hashtable ingredientfunctions = new Hashtable();

	Hashtable functionnames = new Hashtable();

	Hashtable attribfunction = new Hashtable();

	Hashtable keyfundescingrcombo = new Hashtable();

	Hashtable keyfundescingrcombonot = new Hashtable();

	Hashtable keyfundescingrindividual = new Hashtable();

	Hashtable attribbase = new Hashtable();

	String productid = "";

	boolean externalDBO = false;

	public void initialize() {
		try {
			dba = new SVBeautydba();
			dbo = new SVDBOperation();
			dbo.createConnection();

		} catch (Exception e) {
			////logger.log(Level.DEBUG,e);
			e.printStackTrace();
		}
	}
	public void setDBO(SVDBOperation dbo){
		dba = new SVBeautydba();
		this.dbo 		 = dbo;
		this.externalDBO = true;
	}

	public void destroy(){
		dbo.closeConnection();
	}

	public Hashtable getFunctionsDetails() {
		return functionnames;
	}

	private void getFunctionDetails() { // modified on 03/26/05
		if(!externalDBO)
		   initialize() ;
		Hashtable functionnames = new Hashtable() ;
		String result[][] = dbo.executeQuery(dba.getFunctionsDetails());
		for (int i = 0; i < result.length; i++) {
			String tmp[][] = new String[1][2];
			tmp[0][0] = result[i][3];
			tmp[0][1] = result[i][1];
			functionnames.put(result[i][0], tmp);
		}
		if(!externalDBO)
	       destroy() ;
	}

	public Hashtable getFunctionDetailsHT() { // modified on 03/26/05
		
		Hashtable functionnames = new Hashtable() ;
		try
		{
		 dbo = new SVDBOperation();
		 dba = new SVBeautydba();
		 dbo.createConnection();
		 String result[][] = dbo.executeQuery(dba.getFunctionsDetails());
		// System.out.println(result.length) ;
		 for (int i = 0; i < result.length; i++) {
			String tmp[][] = new String[1][2];
			tmp[0][0] = result[i][3];
			tmp[0][1] = result[i][1];
			functionnames.put(result[i][0], tmp);
		 }
		} catch (Exception e)
		{
			System.out.println(e) ;
		}
		finally {
			dbo.closeConnection() ;
		}
         return functionnames ;
	}

	public ArrayList getDescription(Hashtable ht2, String attrib, String skin,
			Hashtable funcnames) {
		Enumeration e = ht2.keys();
		ArrayList vect = new ArrayList();
		Hashtable htd = new Hashtable();

		if (ht2.get(attrib + "-" + skin) != null) {
			PopUpDataVO popup = (PopUpDataVO) ht2
					.get(attrib + "-" + skin);
			Hashtable h = popup.getFunctionDesc();
			Hashtable h1 = popup.getFunctionIngr();
			Enumeration en = h.keys();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				String func[][] = (String[][]) funcnames.get(key);
				htd.put("ingr", h1.get(key));
				htd.put("func", func[0][1]);
				htd.put("desc", h.get(key));
				vect.add(htd);
			}
		}

		return vect;

	}

	private void getIngredientFunction() {
		if(!externalDBO)
			   initialize() ;
		String result[][] = dbo.executeQuery(dba.getIngredientsFunction());
		ArrayList vect = new ArrayList();
	//	System.out.println("result length functions "+result.length) ;
		for (int i = 0; i < result.length; i++) {
			
			String key = result[i][0].trim() ;
//			System.out.println("result  "+result[i][0]+"  "+result[i][3]) ;
			if (ingredientfunctions.get(key) == null) {
				vect = new ArrayList();

			} else
				vect = (ArrayList) ingredientfunctions.get(key);
			vect.add(result[i][3]); //each ingredient-conc may have more than
									// one function associated so make it a
									// ArrayList
			ingredientfunctions.put(key, vect);
		}
		if(!externalDBO)
		       destroy() ;
	}

	private void getIngredientUserFriendlyNames() {
		if(!externalDBO)
			   initialize() ;
		String result[][] = dbo.executeQuery(dba.getIngredients());
		for (int i = 0; i < result.length; i++) {
			String tmp = result[i][1];
			if (tmp == null || tmp.trim().length() == 0)
				//  tmp = result[i][1] ;
				tmp = "";
			ingredientnames.put(result[i][0], tmp);
		}
		if(!externalDBO)
		       destroy() ;
	}

	private void getSkinTypeNames() {
		if(!externalDBO)
		   initialize();
		String result[][] = dbo.executeQuery(dba.getSkinType());
		for (int i = 0; i < result.length; i++) {
			skintype.put(result[i][0], result[i][1]);
		}
		if(!externalDBO)
		       destroy() ;
	}

	private void getSkinConditionNames() {
		if(!externalDBO)
			   initialize() ;
		String result[][] = dbo.executeQuery(dba.getSkinCondition());
		for (int i = 0; i < result.length; i++) {
			skincondition.put(result[i][0], result[i][1]);
		}
		if(!externalDBO)
		       destroy() ;

	}

	private void getSkinConcernNames() {
		if(!externalDBO)
			   initialize() ;
		String result[][] = dbo.executeQuery(dba.getSkinConcern());
		for (int i = 0; i < result.length; i++) {
			skinconcern.put(result[i][0], result[i][1]);
		}
		if(!externalDBO)
		       destroy() ;

	}

	public Hashtable getDescCombo() {
		return keyfundescingrcombo;
	}

	public Hashtable getDescComboNot() {
		return keyfundescingrcombonot;
	}

	public Hashtable getDescIndividual() {
		return keyfundescingrindividual;
	}

	public ArrayList getDesc(String prodtype, Hashtable attribfunction, Hashtable cprofile) {

		ArrayList returnList = new ArrayList() ; // added on 03/26/05
		Hashtable keyfundescingrcombo = new Hashtable();
		Hashtable keyfundescingrcombonot = new Hashtable();
		Hashtable keyfundescingrindividual = new Hashtable();
		Hashtable verifyduplicate = new Hashtable() ;

		Enumeration en = attribfunction.keys(); //attribfunc is the hashtable
												  // with 51-T003 as the key and
												  // another hashtable with
												  // functions as key
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			//System.out.println(" key is" + key);
			if ( key.substring(0, 2).equals(ConstantDeclarations.SKIN_CONCERN_ID)
			   )
			  {
				if (cprofile.get(key.substring(3,key.length())) != null)
				{
				 if ( key.substring(0, 2).equals(ConstantDeclarations.SKIN_CONCERN_ID))
				 {
				   Hashtable tmp = getDescIndividual(key, prodtype, verifyduplicate, attribfunction);
				   if (tmp.size() > 0)
					 keyfundescingrindividual.put(key, tmp);

				   tmp = getDescCombo(key, prodtype);
				   if (tmp.size() > 0)
					keyfundescingrcombo.put(key, tmp);
				 }
				}

			} else if (key.substring(0, 2).equals(ConstantDeclarations.SKIN_TYPE_ID)
					|| key.substring(0, 2).equals(ConstantDeclarations.SKIN_CONDITION_ID)
					|| key.substring(0, 2).equals(ConstantDeclarations.HAND_CONCERN_ID)
					)
			   {
                //System.out.println("I am here vb ") ;
				if (cprofile.get(key.substring(3,key.length())) != null)
				{
				  Hashtable tmp = getDescCombo(key, prodtype);
				  //logger.log(Level.DEBUG,"O ,a oim puitonngf");
				  if (tmp.size() > 0)
					keyfundescingrcombo.put(key, tmp);

				  if (key.substring(0, 2).equals(ConstantDeclarations.SKIN_TYPE_ID)) {
					tmp = getDescIndividual(key, prodtype, verifyduplicate,attribfunction);
					if (tmp.size() > 0)
						keyfundescingrindividual.put(key, tmp);
				  }
				  if (key.substring(0, 2).equals(ConstantDeclarations.SKIN_CONDITION_ID)) {
					tmp = getDescIndividual(key, prodtype, verifyduplicate,attribfunction);
	//				tmp = compareFuncIngr(tmp) ;
					if (tmp.size() > 0)
						keyfundescingrindividual.put(key, tmp);
				  }
					 if ( key.substring(0, 2).equals(ConstantDeclarations.HAND_CONCERN_ID))
					 {
					    tmp = getDescIndividual(key, prodtype, verifyduplicate,attribfunction);
					    if (tmp.size() > 0)
						  keyfundescingrindividual.put(key, tmp);


					 }
				}
			}

		}
		returnList.add(keyfundescingrindividual) ;
		returnList.add(keyfundescingrcombo) ;
		returnList.add(keyfundescingrcombonot) ;
        return returnList ;
	}

	/**
	 * @param tmp
	 * @return
	 */
/*	private Hashtable compareFuncIngr(Hashtable tmp) {
		Hashtable tmp1 = new Hashtable() ;
		boolean rpflag = true ;
		   Enumeration enum = tmp.keys();
		   while(enum.hasMoreElements()) {
		      String key       = (String)enum.nextElement();
		       FuncDataVO fd1      = (FuncDataVO) tmp.get(key) ;
		        String ingrtxt = (String) fd1.getIngrNames() ;
			    if (tmp1.get(key) !=null)
				{
				  //logger.log(Level.DEBUG,"first time"+key) ;
				  rpflag = false ;
				}
				else
				{
					if (rpflag)
					    tmp1.put(key,fd1) ;
				}
		   }
		return tmp1;
	}
*/
	public String getProductUsage(String query, String answer, String prodtype) {
		if(!externalDBO)
			   initialize() ;
		//	  //logger.log(Level.DEBUG,"in getproduct usage");
		String desc = "";
		String result[][] = new String[0][0];
		if (!answer.equals(ConstantDeclarations.USAGE_ANSWER_NO))
			answer = ConstantDeclarations.USAGE_ANSWER_YES;

		result = dbo.executeQuery(dba.getProductUsage(query, answer, prodtype));
		for (int i = 0; i < result.length; i++)
			desc = result[i][3];

		if (desc == null || desc.trim().length() == 0) {
			result = dbo.executeQuery(dba
					.getProductUsage(query, answer, "9999"));
			for (int i = 0; i < result.length; i++)
				desc = result[i][3];
		}
		if(!externalDBO)
		       destroy() ;
		return desc;

	}

	public Hashtable getDescCombo(String key, String prodtype) {
		if(!externalDBO)
			   initialize() ;
		Hashtable combo = new Hashtable();
		Hashtable functions = (Hashtable) attribfunction.get(key); //get the
																   // functions
																   // hashtable
																   // that
																   // pertains
																   // to this
																   // key
																   // 51-T003
		Hashtable htf = new Hashtable(); //function - ingrlist master ;
		StringTokenizer stk = new StringTokenizer(key, "-");
		String attrib = stk.nextToken();
		String value = stk.nextToken();
		String desc = "";
		String[][] result;
		//logger.log(Level.DEBUG,"hert " + key);

		result = dbo.executeQuery(dba.getDesc(attrib, value, "99", "03",
				productid));
		//logger.log(Level.DEBUG," i came fhe");
		if (result.length != 0 && result[0][5] != null
				&& !result[0][5].trim().equals("N/A")
				&& !result[0][5].trim().equals("NA")) {
			desc = result[0][5];
			//logger.log(Level.DEBUG,"mmmmmmmmm " + key + "      " + desc);
			combo.put(key, desc);
		}
		//logger.log(Level.DEBUG,"fffffvnn " + combo.size());
		if(!externalDBO)
		       destroy() ;

		return combo;

	}

	public Hashtable getDescComboNot(String key, String prodtype) {
		if(!externalDBO)
			   initialize() ;
		Hashtable combo = new Hashtable();
		Hashtable functions = (Hashtable) attribfunction.get(key); //get the
																   // functions
																   // hashtable
																   // that
																   // pertains
																   // to this
																   // key
																   // 51-T003
		Hashtable htf = new Hashtable(); //function - ingrlist master ;
		StringTokenizer stk = new StringTokenizer(key, "-");
		String attrib = stk.nextToken();
		String value = stk.nextToken();
		String desc = "";
		String[][] result;


		// added for have not functions
		result = dbo.executeQuery(dba.getDescHaveNot(attrib, value, "99", "03",
				productid));
		if (result.length != 0
				&& (!result[0][5].trim().equals("N/A") || !result[0][5].trim()
						.equals("NA"))) {
			desc = result[0][5];
			//logger.log(Level.DEBUG,"kkkk " + key + "   " + desc);
			combo.put(key, desc);
		}
		if(!externalDBO)
		       destroy() ;
		return combo;

	}

	public Hashtable getDescIndividual(String key, String prodtype, Hashtable verifyduplicate,Hashtable attribfunction) {
		if(!externalDBO)
			   initialize() ;
		Hashtable ht = new Hashtable();
		String funcingr = "" ;
		Hashtable functions = (Hashtable) attribfunction.get(key); //get the
																   // functions
																   // hashtable
																   // that
																   // pertains
																   // to this
																   // key
																   // 51-T003
		Hashtable htf = new Hashtable(); //function - ingrlist master ;
		StringTokenizer stk = new StringTokenizer(key, "-");
		String attrib = stk.nextToken();
		String value = stk.nextToken();
		Hashtable individual = new Hashtable();
        //System.out.println("functions size "+functions.size()) ;
		String[][] result;
		boolean rpflag = false ;

        //System.out.println("attrib "+attrib+"  value "+value) ;
		result = dbo.executeQuery(dba.getDesc(attrib, value, "99", "03"));

		for (int i = 0; i < result.length; i++) {
	        //logger.log(Level.DEBUG,"result i[4] "+result[i][4]) ;
			Enumeration en = functions.keys();

			while (en.hasMoreElements()) //for function ID , get the desc
			{

				String funcid = (String) en.nextElement();
				ArrayList ingr = (ArrayList) functions.get(funcid); //get the
															  // ingredients
															  // that correspond
															  // to this
															  // function
				String desc = result[i][5];
				//       //logger.log(Level.DEBUG,"desc is"+desc);
				if (desc == null)
					desc = "";
				//     //logger.log(Level.DEBUG,"func id"+funcid);
				//     //logger.log(Level.DEBUG,"resulti4"+result[i][4]);
				if (funcid.trim().equals(result[i][4].trim()) && !desc.trim().equals("N/A")) {
					FuncDataVO fd = new FuncDataVO();
                    //System.out.println("ffdgdfgd vbdfd "+ingr.size());
					for (int j = 0; j < ingr.size(); j++) {
						//				  //logger.log(Level.DEBUG,"ingredient
						// id"+ingr.get(j));
						if (ingredientnames.get(ingr.get(j)) != null
								&& ((String) ingredientnames.get(ingr
										.get(j))).trim().length() != 0) {
							fd.setIngrNames((String) ingredientnames.get(ingr
									.get(j)));
							fd.setCountIngr(fd.getCountIngr() + 1);
						}
					}
					fd.setDesc(desc.trim());
					String tmptxt =(String) fd.getIngrNames() ;
					funcingr = funcid+tmptxt.trim()+desc.trim() ;
				    if (verifyduplicate.get(funcingr) !=null)
					{
					  rpflag = false ;
					}
					else
					{
					  verifyduplicate.put(funcingr,"repeat") ;
					  rpflag = true ;
					}
				    if (rpflag)

					  individual.put(funcid, fd);

				}

			}
		}
		if(!externalDBO)
		       destroy() ;
		return individual;

	}

	public Hashtable getAttribFunction(Hashtable individualingrratings,
			String prodid) {
		this.productid = prodid;
		//System.out.println("productid pop "+prodid) ;
		
		if(!externalDBO)
			   initialize() ;
		getIngredientUserFriendlyNames();
		getIngredientFunction();
		getFunctionDetails();

		//	 //logger.log(Level.DEBUG,"in");
		Enumeration enum2 = individualingrratings.keys();
		while (enum2.hasMoreElements()) {
			String str = (String) enum2.nextElement();
			//System.out.println("vb2 str "+str) ;
		}
		Enumeration enum1 = individualingrratings.keys();
		//	//logger.log(Level.DEBUG,"in");
		while (enum1.hasMoreElements()) {
			//	   //logger.log(Level.DEBUG,"in enum");
			String str = (String) enum1.nextElement();
			
			//System.out.println("vb1 str "+str) ;
			
			ArrayList vect = (ArrayList) individualingrratings.get(str);
			ArrayList vect1 = new ArrayList();
			for (int i = 0; i < vect.size(); i++) {
				//				  //logger.log(Level.DEBUG,"in vect");
				String str1 = (String) vect.get(i);
				//				    //logger.log(Level.DEBUG,"vb2 str "+str1) ;
				StringTokenizer stk = new StringTokenizer(str1, "-");
				String att = "02";
				String ingrfriendlyname = "";
				String conc = "";
				String type = "";
				String ratings = "";
				String profiletype = ""; //whether its skintype or
										 // skincondition etc
				String profilevalue = "";
				String ingrid = "";

				att = stk.nextToken();
				ingrid = stk.nextToken();
				conc = stk.nextToken();
				type = stk.nextToken();
				profiletype = stk.nextToken();
				profilevalue = stk.nextToken();
				ratings = stk.nextToken();
				String key = profiletype + "-" + profilevalue;
				String functionkey = ingrid ;
				
			    //System.out.println("funckey"+functionkey);
			    //System.out.println("key "+key);
			    
				ArrayList functionids = (ArrayList) ingredientfunctions.get(functionkey.trim());

				Hashtable func_hash = new Hashtable();
				ArrayList funcingr = new ArrayList(); //the ingredients that r
												// present in the functiion

				//					//logger.log(Level.DEBUG,"key is"+key);

				//			//logger.log(Level.DEBUG,"cocnetration"+conc);

				//the below if statements performs a lot of functions .
				//for a single ingr-conc combination , check the available
				// functions
				//first it checks whether the rating value for a
				// 02-ingrname-conc-prodmastyertype-51-T003 is > 5
				//if so store those ingredeints along with their functions for
				// puling description text.
				//Attribfuntion structure : Hashtable a-Hashtable b-ArrayList c.
				//here hashtable "a" will have key as "51-T003" and the value
				// is hashtable "B"
				//hashtable 'B' has "function id" as the "key" and the ArrayList
				// "c"
				//ArrayList "c" has the ingredients that corresponds to 'function
				// id' key in hashtable "B"

				////logger.log(Level.DEBUG,"1 fucntiondis size "+functionids.size());

				if (att.equals("02") && ratings != null
						&& ratings.trim().length() != 0
						&& Integer.parseInt(ratings) > 4) {
					//logger.log(Level.DEBUG,"2");
					//logger.log(Level.DEBUG," agin key is" + key);
					//check whether the key is already present in hashtable "A"
					// key is , 51-T003
					if (attribfunction.get(key) == null) //if not create a new
														 // hashtable "B"
					{
						//logger.log(Level.DEBUG,"new key");
						func_hash = new Hashtable();
					} else //get the Hashtable "B"
					{
						//logger.log(Level.DEBUG,"old key");
						func_hash = (Hashtable) attribfunction.get(key);
					}

					//				//logger.log(Level.DEBUG,"3");
					if (functionids != null)
					   //System.out.println("function ids "+functionids.size()) ;
					if (functionids != null)
						for (int j = 0; j < functionids.size(); j++) //for all
																	 // functions
																	 // which
																	 // corresponds
																	 // to
																	 // ingredients
						{
							//logger.log(Level.DEBUG,"4");
							String funcid = (String) functionids.get(j);
							//logger.log(Level.DEBUG,"function id" + funcid);
							//System.out.println("function id "+funcid) ;

							if (func_hash.get(funcid.trim()) == null) //check
																	  // whether
																	  // this
																	  // function
																	  // entry
																	  // is
																	  // already
																	  // made in
																	  // to
																	  // hashtable
																	  // "b"
							{
								funcingr = new ArrayList(); //no so create a n
								//							//logger.log(Level.DEBUG,"new function id"+funcid);
							} else {

								//				    //logger.log(Level.DEBUG,"old funcid"+funcid);
								funcingr = (ArrayList) func_hash
										.get(funcid.trim()); //yes get the
															 // ArrayList "c" which
															 // corresponds to
															 // functionid key
															 // in hashtable "b"

							}
							//funcingr holds all the ingredients that
							// corresponds to that function
							//	   //logger.log(Level.DEBUG,"5");
							funcingr.add(ingrid); //add that ingredeint to that
												  // functionid
							//	 //logger.log(Level.DEBUG,"6");
							//System.out.println("ingr id "+ingrid) ;
							func_hash.put(funcid.trim(), funcingr); //add the
																	// ArrayList
																	// "C" to
																	// hashtable
																	// "B"
							//		 //logger.log(Level.DEBUG,"7");

						}
					//	       //logger.log(Level.DEBUG,"8");
					attribfunction.put(key, func_hash); //Add hashtable "B" to
														// hashtable "A"
					//System.out.println("9");
				}
			}

		}
		if(!externalDBO)
		       destroy() ;

		return attribfunction;
	}

	public Hashtable getAttribBase(Hashtable individualingrratings,
			String prodtype) {
		if(!externalDBO)
			   initialize() ;
		Enumeration enum1 = individualingrratings.keys();
		while (enum1.hasMoreElements()) {

			String str = (String) enum1.nextElement();
			ArrayList vect = (ArrayList) individualingrratings.get(str);
			ArrayList vect1 = new ArrayList();
			for (int i = 0; i < vect.size(); i++) {
				String str1 = (String) vect.get(i);
				StringTokenizer stk = new StringTokenizer(str1, "-");
				String att = "01";
				String baseid = "";
				String conc = "";
				String type = "";
				String ratings = "";
				String profiletype = ""; //whether its skintype or
										 // skincondition etc
				String profilevalue = "";
				String ingrid = "";

				att = stk.nextToken();
				if (att.equals("01")) {
					baseid = stk.nextToken();
					conc = stk.nextToken();
					type = stk.nextToken();
					profiletype = stk.nextToken();
					profilevalue = stk.nextToken();
					ratings = stk.nextToken();
					String key = profiletype + "-" + profilevalue;
					String functionkey = baseid + "-" + conc;
					//				//logger.log(Level.DEBUG,"att is"+att);
					//				//logger.log(Level.DEBUG,"base funckey"+functionkey);
					//			//logger.log(Level.DEBUG,"base key is"+key);
					//		//logger.log(Level.DEBUG,"base cocnetration"+conc);

					//the below if statements performs a lot of functions .
					//for a single ingr-conc combination , check the available
					// functions
					//first it checks whether the rating value for a
					// 02-ingrname-conc-prodmastyertype-51-T003 is > 5
					//if so store those ingredeints along with their functions
					// for puling description text.
					//Attribfuntion structure : Hashtable a-Hashtable b-ArrayList
					// c.
					//here hashtable "a" will have key as "51-T003" and the
					// value is hashtable "B"
					//hashtable 'B' has "function id" as the "key" and the
					// ArrayList "c"
					//ArrayList "c" has the ingredients that corresponds to
					// 'function id' key in hashtable "B"

					//			//logger.log(Level.DEBUG,"1");

					if (att.equals("01") && ratings != null
							&& ratings.trim().length() != 0
							&& Integer.parseInt(ratings) > 4) {
						//			    //logger.log(Level.DEBUG,"2");
						//				//logger.log(Level.DEBUG,"key in base is"+key);
						//check whether the key is already present in hashtable
						// "A" key is , 51-T003
						String result[][] = new String[0][0];
						if (profiletype.equals(ConstantDeclarations.SKIN_CONCERN_ID))
							result = dbo.executeQuery(dba.getDesc(profiletype,
									"9999", "99", "01"));
						else
							result = dbo.executeQuery(dba.getDesc(profiletype,
									profilevalue, prodtype, "01"));

						if (attribbase.get(key) == null) //if not , create a
														 // new hashtable "B"
						{
							//     //logger.log(Level.DEBUG,"new key");
							//  base_hash = new Hashtable();
							for (int j = 0; j < result.length; j++) {
								//			   //logger.log(Level.DEBUG,"baseid"+result[j][4]);
								//				 //logger.log(Level.DEBUG,"baseee id"+baseid);
								if (baseid.trim().equals(result[j][4].trim())) {
									//					   //logger.log(Level.DEBUG,"yesssssssss");
									attribbase.put(key, result[j][5]);

								}
							}

						}

						//					//logger.log(Level.DEBUG,"3");
						//attribbase.put(key,base_hash); //Add hashtable "B" to
						// hashtable "A"
						//			  //logger.log(Level.DEBUG,"9");
					}

				}//end of if
			}//end of for

		}//end of while
		if(!externalDBO)
		       destroy() ;
		return attribbase;
	} //end of attrib base

	public Hashtable getOtherSelling() {
		Hashtable otherselling = new Hashtable();
		try {
			if(!externalDBO)
				   initialize() ;
			String[][] rs1 = new String[0][0];
			rs1 = dbo.executeQuery(dba.getOtherSellingTools());
			if (rs1.length > 0) {
				for (int x = 0; x < rs1.length; x++) {
					otherselling.put(rs1[x][0], rs1[x][1]);
				}
			}
			if(!externalDBO)
			       destroy() ;
		} catch (Exception mx_1) {
			////logger.log(Level.DEBUG,mx_1);
			mx_1.printStackTrace();
		}
		finally {
			;
		}
        return otherselling;
	}

	public Hashtable getMarketingText() {
		Hashtable marketing = new Hashtable();
		try {
			if(!externalDBO)
				   initialize() ;
			String[][] rs1 = new String[0][0];
			String sql = " SELECT ltrim(rtrim(a.sv_product_id)), ltrim(rtrim(a.sv_attribute_value)), "
					+ " ltrim(rtrim(a.sv_function_text)),LTrim(RTrim(a.sv_function_benefit)) "
					+ " FROM function_combined_benefits a "
					+ " WHERE a.sv_attribute_id = '02' "
					+ " AND LTrim(RTrim(a.sv_product_id)) IS NOT NULL "
					+ " AND LTrim(RTrim(a.sv_function_benefit)) <> 'NA' "
					+ " order by a.sv_product_id ";
			rs1 = dbo.executeQuery(sql);
			if (rs1.length > 0) {
				for (int x = 0; x < rs1.length; x++) {
					marketing.put(rs1[x][0] + rs1[x][1], rs1[x][2] + "::"
							+ rs1[x][3]);
				}
			}
			if(!externalDBO)
			       destroy() ;
		} catch (Exception ex1) {
			////logger.log(Level.DEBUG,mx_1);
			ex1.printStackTrace();
		}
		finally {
			;
		}
        return marketing;
	}

	public ArrayList getAttributeFunctions(String attribvalue) {
		ArrayList attribfunctions = new ArrayList() ;
		
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String[][] rs1 = new String[0][0];
			String sql = " select sv_profile_attribute_value, sv_function_id "+ 
                         " from hair_Prof_attrib_functions "+
                         " where sv_profile_attribute_value = '"+attribvalue+"' "+
                         " and sv_function_text is not null " +
                         " order by sv_function_id      ";
			rs1 = dbo.executeQuery(sql);
			if (rs1.length > 0) {
				for (int x = 0; x < rs1.length; x++) {
					attribfunctions.add(rs1[x][1]);
				}
			}
			if(!externalDBO)
			       destroy() ;
		} catch (Exception ex1) {
			////logger.log(Level.DEBUG,mx_1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
        return attribfunctions ;
	}
	public ArrayList getAttributeFunctionsNew(String attribvalue) {
		ArrayList attribfunctions = new ArrayList() ;
		
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String[][] rs1 = new String[0][0];
			String sql = " select sv_profile_attribute_value, sv_function_id "+ 
                         " from hair_Prof_attrib_functions "+
                         " where sv_profile_attribute_value in "+attribvalue+" "+
                         " and sv_function_text is not null " +
                         " order by sv_function_id      ";
			rs1 = dbo.executeQuery(sql);
			if (rs1.length > 0) {
				for (int x = 0; x < rs1.length; x++) {
					attribfunctions.add(rs1[x][1]);
				}
			}
			if(!externalDBO)
			       destroy() ;
		} catch (Exception ex1) {
			////logger.log(Level.DEBUG,mx_1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
        return attribfunctions ;
	}

	public Hashtable getFunctionText(String attribvalue) {
		Hashtable functext = new Hashtable();
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String[][] rs1 = new String[0][0];
			String sql = " select sv_function_id||sv_profile_attribute_value, ltrim(rtrim(sv_function_text)) "+
                         " from hair_Prof_attrib_functions "+ 
                         " where sv_profile_attribute_value = '"+attribvalue+"' "+
                         " and sv_function_text is not null "+
                         " order by sv_function_id  ";
			
			rs1 = dbo.executeQuery(sql);
			if (rs1.length > 0) {
				for (int x = 0; x < rs1.length; x++) {
					functext.put(rs1[x][0], rs1[x][1]);
				}
			}
			if(!externalDBO)
			       destroy() ;
		} catch (Exception ex1) {
			////logger.log(Level.DEBUG,mx_1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
        return functext;
	}

	public Hashtable getFunctionTextNew(String attribvalue) {
		Hashtable functext = new Hashtable();
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String[][] rs1 = new String[0][0];
			String sql = " select sv_function_id, ltrim(rtrim(sv_function_text)) "+
                         " from hair_Prof_attrib_functions "+ 
                         " where sv_profile_attribute_value in "+attribvalue+" "+
                         " and sv_function_text is not null "+
                         " order by sv_function_id  ";
			
			rs1 = dbo.executeQuery(sql);
			if (rs1.length > 0) {
				for (int x = 0; x < rs1.length; x++) {
					functext.put(rs1[x][0], rs1[x][1]);
				}
			}
			if(!externalDBO)
			       destroy() ;
		} catch (Exception ex1) {
			////logger.log(Level.DEBUG,mx_1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
        return functext;
	}
	public Hashtable getFunctionIngredients(String attribvalue,String prodid, String functions) {
		Hashtable funcingr = new Hashtable();
		String[][] rs1 = new String[0][0];
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String sql = " select  b.sv_function_id,a.sv_ingredient_id "+
                         " from hair_product_ingredients a, sv_ingredients_function_hair b "+ 
                         " where a.sv_ingredient_id = b.sv_ingredient_id "+
                         " and a.sv_product_id = '"+prodid+"' "+
                         " and sv_function_id in "+functions+" "+
                         " order by b.sv_function_id, a.sv_ingredient_id   ";
            System.out.println(sql) ;			
			rs1 = dbo.executeQuery(sql);
		} catch (Exception ex1) {
			////logger.log(Level.DEBUG,mx_1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
			
			ArrayList functioningr = new ArrayList() ;
			String oldfunc = "" ;
			
			if (rs1.length > 0) {
				for (int x = 0; x < rs1.length; x++) {
					String funcid = rs1[x][0] ;
					String ingrid = rs1[x][1] ;
					
					if (funcid != null && funcingr.get(funcid) == null)
					{
						functioningr = new ArrayList() ;
						functioningr.add(ingrid) ;
						funcingr.put(funcid,functioningr) ;
					}
					else
					{
						functioningr = (ArrayList) funcingr.get(funcid) ;
						functioningr.add(ingrid) ;
						funcingr.put(funcid,functioningr) ;
					}
				}
			}
        return funcingr;
	}
	public Hashtable getFunctionIngredientsNew(String attribvalue,String prodid, String functions) {
		Hashtable funcingr = new Hashtable();
		String[][] rs1 = new String[0][0];
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String sql = " select  b.sv_function_id,a.sv_ingredient_id "+
                         " from hair_product_ingredients a, sv_ingredients_function_hair b "+ 
                         " where a.sv_ingredient_id = b.sv_ingredient_id "+
                         " and a.sv_product_id = '"+prodid+"' "+
                         " and sv_function_id in "+functions+" "+
                         " order by b.sv_function_id, a.sv_ingredient_id   ";
            System.out.println(sql) ;			
			rs1 = dbo.executeQuery(sql);
		} catch (Exception ex1) {
			////logger.log(Level.DEBUG,mx_1);
			ex1.printStackTrace();
		}
		finally {
			dbo.closeConnection() ;
		}
			
			ArrayList functioningr = new ArrayList() ;
			String oldfunc = "" ;
			
			if (rs1.length > 0) {
				for (int x = 0; x < rs1.length; x++) {
					String funcid = rs1[x][0] ;
					String ingrid = rs1[x][1] ;
					
					if (funcid != null && funcingr.get(funcid) == null)
					{
						functioningr = new ArrayList() ;
						functioningr.add(ingrid) ;
						funcingr.put(funcid,functioningr) ;
					}
					else
					{
						functioningr = (ArrayList) funcingr.get(funcid) ;
						functioningr.add(ingrid) ;
						funcingr.put(funcid,functioningr) ;
					}
				}
			}
        return funcingr;
	}

}

