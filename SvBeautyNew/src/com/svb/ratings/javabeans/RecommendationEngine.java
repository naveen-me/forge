package com.svb.ratings.javabeans;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.svb.common.ConstantDeclarations;
import com.svb.common.dba.SVBeautydba;
import com.svb.common.dba.SVDBOperation;
import com.svb.common.javabean.BaseRatings;
import com.svb.common.valueobjects.AttribRatingVO;
import com.svb.common.valueobjects.BrandVO;
import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;

/*
 * // header - edit "Data/yourJavaHeader" to customize // contents - edit
 * "EventHandlers/Java file/onCreate" to customize //
 */
public class RecommendationEngine {
	private static Log log = LogFactory.getLog(RecommendationEngine.class);
	SVBeautydba dba = new SVBeautydba();
	SVDBOperation dbo = new SVDBOperation();

	boolean externalDBO = false;
	boolean noingredients = false;

	public void initialize() throws Exception {
		dba = new SVBeautydba();
		dbo = new SVDBOperation();
		dbo.createConnection();

	}

	public void setDBO(SVDBOperation dbo) {
		this.dbo = dbo;
		this.externalDBO = true;
	}

	public void destroy() {
		dbo.closeConnection();
	}

	public Hashtable getProdDetails(String prodtype, String prodselect, String prodline, CustomerProfileVO cp, String upcId) {
		String[][] result = null;
		ProductVO[] productVOList = new ProductVO[0];
		Hashtable prodhash = new Hashtable();
		Hashtable eliminated_products = new Hashtable();
		Hashtable all_products = new Hashtable();
		boolean prodflag = false;

		//String cAge = cp.getAge(); //Removed unused variables & their use for performance improvement - Shivam - 22/07/2024
		//String cSkinType = cp.getSkinType();
		String cNatural = cp.getNatural();
		//String cEthnicity = cp.getEthnicity();
		
		// Exception rules - Shivam - 21/02/2025 - Start
		ArrayList<String> custSkinConcernList = cp.getSkinConcernList();
		ArrayList<String> custSkinConditionList = cp.getSkinConditionList();
		ArrayList<String> custEyeConcernList = cp.getEyeConcernList();

		boolean naturalflag = false;

		Set<String> customerPreferences = new HashSet<>();
		
		customerPreferences.add(cp.getSkinType());
		customerPreferences.addAll(custSkinConditionList);
		customerPreferences.addAll(custSkinConcernList);
		customerPreferences.addAll(custEyeConcernList);
		
		// Exception rules - Shivam - 21/02/2025 - End
		/*
		 * Removed unused variables & their use for performance improvement - Shivam - 22/07/2024
		 * 
		if (cSkinType.equals("T002"))
			normalflag = true;
		if (cSkinType.equals("T003"))
			comboflag = true;
		if (cSkinType.equals("T004"))
			oilyflag = true;
		if (cAge.equals("G004"))
			under25flag = true;
		if (cAge.equals("G001"))
			age25flag = true;
		if (cAge.equals("G002"))
			age36flag = true;
		if (cAge.equals("G003"))
			age46flag = true;
		if (cAge.equals("G005"))
			age56flag = true;
		if (cAge.equals("G006"))
			age66flag = true;
		if (cAge.equals("G007"))
			age75flag = true; 
		if (cEthnicity.equals("N002"))
			asianflag = true;
			
		*/

		if (cNatural.equals("A008"))
			naturalflag = true;


		/*
		 * Removed unused variables & their use for performance improvement - Shivam - 22/07/2024
		 * for (int m = 0; m < custEyeConcernList.size(); m++) {
			if (custEyeConcernList.get(m).equals("C002"))
				dceflag = true;
			if (custEyeConcernList.get(m).equals("E001"))
				pufflag = true;
			if (custEyeConcernList.get(m).equals("C001"))
				eyeslinesflag = true;
			if (custEyeConcernList.get(m).equals("E004"))
				eyescreasesflag = true;
			if (custEyeConcernList.get(m).equals("E999"))
				noeyeconcernsflag = true;
		}
		
		// System.out.println("dce flag "+dceflag) ;
		*/

		/*
			if (cp.getSpecification().contains("MF03")) {
				makeupflag = true;
			} else {
				makeupflag = false;
			}
		*/
		
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			if(upcId == null || upcId.length() <= 0 || upcId == "") {
				if (prodselect != null && prodselect.equals("prodLine")) {
					System.out.println("PRODSELECT :: " + prodselect);
					result = dbo.executeQuery(dba.getBeautyProductIds(prodline, prodselect));
//					  result1 = dbo.executeQuery(dba.getBeautyProductIds("('0009')","prodType"));
				} else if (prodselect != null && prodselect.equals("prodType")) {
					System.out.println("PRODSELECT :: " + prodselect);
					result = dbo.executeQuery(dba.getBeautyProductIds(prodtype, prodselect));
				}
				else {
					System.out.println("PRODSELECT :: " + prodselect);
					result = dbo.executeQuery(dba.getBeautyProductIds("", ""));
				}
			}
			else {
				if (prodselect != null && prodselect.equals("prodLine")) {
					System.out.println("PRODSELECT :: " + prodselect);
					result = dbo.executeQuery(dba.getSingleBeautyProductIds(prodline, prodselect,upcId));
//					  result1 = dbo.executeQuery(dba.getBeautyProductIds("('0009')","prodType"));
				} else if (prodselect != null && prodselect.equals("prodType")) {
					System.out.println("PRODSELECT :: " + prodselect);
					result = dbo.executeQuery(dba.getSingleBeautyProductIds(prodtype, prodselect,upcId));
				}
				else {
					System.out.println("PRODSELECT :: " + prodselect);
					result = dbo.executeQuery(dba.getSingleBeautyProductIds("", "",upcId));
				}
			}
		} catch (Exception ex1) {
			ex1.printStackTrace();
		} finally {
			dbo.closeConnection();
		}
		Map<String, String> eliminationRuleMap = getEliminationRuleProducts();
		Set<String> eliminatedProducts = getEliminatedProducts(cp,eliminationRuleMap);
		
		// Exception rules - Shivam - 21/02/2025 - Start
		Map<String, Set<String>> exceptionRuleProductsMap = getExceptionRuleProductsMap(false);
		// Exception rules - Shivam - 21/02/2025 - End
		
		/*
		 * if (prodselect !=null && prodselect.equals("prodLine")) { for (int i=0; i <
		 * result1.length ; i++) { ProductVO pr = new ProductVO();
		 * pr.setProdId(result1[i][0].trim()); pr.setBase(result1[i][2]);
		 * pr.setProductLine(result1[i][3]); pr.setProdName(result1[i][4]);
		 * pr.setPrice(result1[i][5]); pr.setImage(result1[i][6]);
		 * pr.setSPFId(result1[i][7]) ; pr.setProdType(result1[i][8]) ;
		 * pr.setSellCopy(result1[i][9]) ; pr.setIngrScore("0");
		 * pr.setFunctionscore("0"); pr.setBumpScore("0"); pr.setTobeDeleted(false);
		 * String mcateg = "" ; mcateg = result1[i][1] ; pr.setMasterProdType(mcateg);
		 * prodhash.put(result1[i][0].trim(), pr); } }
		 */
		
		for (int i = 0; i < result.length; i++) {

			if (result[i][1].equals("0001") || result[i][1].equals("0003") || result[i][1].equals("0005")
					|| result[i][1].equals("0004")
//		 		|| result[i][1].equals("0011") || result[i][1].equals("0012")
//		 		|| result[i][1].equals("0013") || result[i][1].equals("0014")
//		 		|| result[i][1].equals("0015") || result[i][1].equals("0016")
//		 		|| result[i][1].equals("0017") 
					|| result[i][1].equals("0009")) {
				// System.out.println("0... "+result[i][0]+" "+result[i][4]+" "+prodflag) ;
				prodflag = true;
				if (naturalflag && (result[i][3].equals("1001") || result[i][3].equals("1002")
						|| result[i][3].equals("1007") || result[i][3].equals("1008") || result[i][3].equals("1015")
						|| result[i][3].equals("1016") || result[i][3].equals("1023") || result[i][3].equals("1028")
						|| result[i][3].equals("1036") || result[i][3].equals("1046") || result[i][3].equals("1052")
						|| result[i][3].equals("1060") || result[i][3].equals("1079") || result[i][3].equals("1080")
						|| result[i][3].equals("1083") || result[i][3].equals("1091")))
					prodflag = true;
				else if (naturalflag)
					prodflag = false;
				/*
				 * 1012 Acne Cleanser 0001 Skin 1016 Rosacea Cleanser 0001 Skin 1020 Dry Skin
				 * Cleanser 0001 Skin 1021 Oily Skin Cleanser 0001 Skin 1023 Sesitive Skin
				 * Cleanser 0001 Skin 1029 Combination Cleanser 0001 Skin 1028 Redness Cleanser
				 * 0001 Skin
				 * 
				 * 1013 Acne Moisturizer 0003 Skin 1024 Sesitive Skin Moisturizer 0003 Skin 1034
				 * Oily Skin Moisturizer 0003 Skin 1035 Enlarged Pores Moisturizer 0003 Skin
				 * 1030 Redness Moisturizer 0003 Skin 1017 Rosacea Moisturizer 0003 Skin
				 * 
				 * 1018 Eyes Dark Circles 0004 Skin 1019 Eyes Puffiness 0004 Skin
				 * 
				 * 1010 Lip Treatment 0005 Skin 1015 Rosacea Treatment 0005 Skin 1025 Sesitive
				 * Skin Treatment 0005 Skin 1027 Pores Treatment 0005 Skin 1032 Combination
				 * Treatment 0005 Skin 1037 Face Creases Treatment 0005 Skin 1038 Age 56-65
				 * Treatment 0005 Skin 1036 Oily Skin Treatment 0005 Skin 1031 Pores Treatment
				 * 0005 Skin 1026 Redness Treatment 0005 Skin 1022 Hyperpigmentation Treatment
				 * 0005 Skin 1014 Age spot Treatment 0005 Skin 1011 Acne Treatment 0005 Skin
				 */
				/*if (result[i][0].trim().equals("201456"))
					System.out.println("vbbbbbbbbbb...   " + result[i][0] + " " + prodflag);*/
				/**
				 * Added By Synoptek (03-06-2020) Commented static conditions for elimination of
				 * products
				 */
				
				String upc = result[i][11];
				if (eliminatedProducts.toString().contains(upc)) {
					prodflag = false;
				}
				
				
				// Exception rules - Shivam - 21/02/2025 - Start
				if (prodflag && exceptionRuleProductsMap.containsKey(result[i][0])) {
					prodflag = exceptionRuleProductsMap.get(result[i][0]).stream().anyMatch(customerPreferences::contains);
				}
				// Exception rules - Shivam - 21/02/2025 - End
				
				/*
				 * Commented below condition to remove Exception Rule: 
				 * 
				 * Only show HydroPeptide Makeup Melt Botanical Cleansing Balm (818951021353) 
				 * if the customer selects "Wears full coverage makeup" in Question #6. All other filters would still apply.
				 * 
				 * if(makeupflag == false) {
				if(upc.equalsIgnoreCase(ConstantDeclarations.REGULAR_MAKEUP_MELT))
					prodflag = false;
				elimProdflag = true;
				}*/
				
				
				/*
				 * boolean duplicate= false;  String reason = "";
				 * if(hSet.toString().contains(upc)){ for (String name : eliProducts.keySet()) {
				 * if (eliProducts.get(name).contains(upc)) { if
				 * (custEyeConcernList.contains(name) || custSkinConcernList.contains(name) ||
				 * custSkinConditionList.contains(name)|| cAge.equalsIgnoreCase(name)) {
				 * prodflag = true; elimProdflag = false; duplicate= true; } else {
				 * if(duplicate) { prodflag = true; elimProdflag = false; } else { prodflag =
				 * false; elimProdflag = true; reason = (reason == "") ? name:
				 * reason.concat(" and ").concat(name); } } } } }
				 */
				/*
				 * if (prodflag) { //
				 * System.out.println("1... "+result[i][0]+"  "+result[i][4]+" "+prodflag) ;
				 * 
				 * if (result[i][0].trim().equals("201456")) {
				 * System.out.println("********* &&&&&&&&& finelines flag "+eyeslinesflag+"  "
				 * +result[i][0]+" "+result[i][8]) ; }
				 * 
				 * if (prodflag && !age46flag && result[i][1].equals("0003") &&
				 * result[i][8].equals("1033")) prodflag = false ; if (prodflag && !age56flag &&
				 * result[i][1].equals("0005") && result[i][8].equals("1038")) prodflag = false
				 * ;
				 * 
				 * if (prodflag && !dryflag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1020")) prodflag = false ; if (prodflag && !comboflag &&
				 * result[i][1].equals("0001") && result[i][8].equals("1029")) prodflag = false
				 * ; if (prodflag && !oilyflag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1021")) prodflag = false ; if (prodflag && !acneflag &&
				 * result[i][1].equals("0001") && result[i][8].equals("1012")) prodflag = false
				 * ; if (prodflag && !rosaceaflag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1016")) prodflag = false ; if (prodflag &&
				 * !sensitiveflag && result[i][1].equals("0001") && result[i][8].equals("1023"))
				 * prodflag = false ; if (prodflag && !rednessflag &&
				 * result[i][1].equals("0001") && result[i][8].equals("1028")) prodflag = false
				 * ;
				 * 
				 * if (prodflag && !acneflag && result[i][1].equals("0003") &&
				 * result[i][8].equals("1013")) prodflag = false ; if (prodflag && !rosaceaflag
				 * && result[i][1].equals("0003") && result[i][8].equals("1017")) prodflag =
				 * false ; if (prodflag && !sensitiveflag && result[i][1].equals("0003") &&
				 * result[i][8].equals("1024")) prodflag = false ; if (prodflag && !rednessflag
				 * && result[i][1].equals("0003") && result[i][8].equals("1030")) prodflag =
				 * false ;
				 * 
				 * if (prodflag && !dceflag && result[i][1].equals("0004") &&
				 * result[i][8].equals("1018")) { prodflag = false ; //
				 * System.out.println("etreat 1 "+dceflag+"  "+result[i][8]+"  "+result[i][4]
				 * +" "+prodflag) ; } if (prodflag && !pufflag && result[i][1].equals("0004") &&
				 * result[i][8].equals("1019")) { prodflag = false ; //
				 * System.out.println("etreat 2"+dceflag+"  "+result[i][8]+"  "+result[i][4]+" "
				 * +prodflag) ; } //
				 * System.out.println("etreat 3"+dceflag+"  "+pufflag+"  "+result[i][8]+"  "
				 * +result[i][4]+" "+prodflag) ;
				 * 
				 * if (prodflag && !poresflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1031")) prodflag = false ; if (prodflag && !acneflag &&
				 * result[i][1].equals("0005") && result[i][8].equals("1011")) prodflag = false
				 * ; if (prodflag && !agespotflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1014")) prodflag = false ; if (prodflag && !rosaceaflag
				 * && result[i][1].equals("0005") && result[i][8].equals("1015")) prodflag =
				 * false ; if (prodflag && !sensitiveflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1025")) prodflag = false ; if (prodflag && !hyperflag &&
				 * result[i][1].equals("0005") && result[i][8].equals("1022")) prodflag = false
				 * ; if (prodflag && !rednessflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1026")) prodflag = false ; if (prodflag && !poresflag &&
				 * result[i][1].equals("0005") && result[i][8].equals("1031")) prodflag = false
				 * ; if (prodflag && !comboflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1032")) prodflag = false ;
				 * 
				 * if (prodflag && !oilyflag && result[i][1].equals("0003") &&
				 * result[i][8].equals("1034")) prodflag = false ; if (prodflag && !oilyflag &&
				 * result[i][1].equals("0005") && result[i][8].equals("1036")) prodflag = false
				 * ;
				 * 
				 * if (prodflag && !poresflag && result[i][1].equals("0003") &&
				 * result[i][8].equals("1035")) prodflag = false ; if (prodflag &&
				 * !facecreasesflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1037")) prodflag = false ;
				 * 
				 * if (prodflag && !occnacneflag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1039")) prodflag = false ; if (prodflag && !persacneflag
				 * && result[i][1].equals("0001") && result[i][8].equals("1040")) prodflag =
				 * false ; if (prodflag && !dryflakyflag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1041")) prodflag = false ; if (prodflag && !poresflag &&
				 * result[i][1].equals("0001") && result[i][8].equals("1042")) prodflag = false
				 * ; if (prodflag && !age46flag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1043")) prodflag = false ; if (prodflag && !age56flag &&
				 * result[i][1].equals("0001") && result[i][8].equals("1044")) prodflag = false
				 * ; if (prodflag && !age66flag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1045")) prodflag = false ; if (prodflag && !age75flag &&
				 * result[i][1].equals("0001") && result[i][8].equals("1046")) prodflag = false
				 * ; if (prodflag && !asianflag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1047")) prodflag = false ;
				 * 
				 * if (prodflag && !hyperflag && result[i][1].equals("0003") &&
				 * result[i][8].equals("1048")) prodflag = false ; if (prodflag && !normalflag
				 * && result[i][1].equals("0003") && result[i][8].equals("1049")) prodflag =
				 * false ; if (prodflag && !dryflag && result[i][1].equals("0003") &&
				 * result[i][8].equals("1050")) prodflag = false ; if (prodflag && !comboflag &&
				 * result[i][1].equals("0003") && result[i][8].equals("1051")) prodflag = false
				 * ; if (prodflag && !occnacneflag && result[i][1].equals("0003") &&
				 * result[i][8].equals("1052")) prodflag = false ; if (prodflag && !persacneflag
				 * && result[i][1].equals("0003") && result[i][8].equals("1053")) prodflag =
				 * false ; if (prodflag && !asianflag && result[i][1].equals("0003") &&
				 * result[i][8].equals("1054")) prodflag = false ;
				 * 
				 * if (prodflag && !lossflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1055")) prodflag = false ; if (prodflag &&
				 * !facecreasesflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1056")) prodflag = false ; if (prodflag && !hyperflag &&
				 * result[i][1].equals("0005") && result[i][8].equals("1057")) prodflag = false
				 * ; if (prodflag && !poresflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1058")) prodflag = false ; if (prodflag && !agespotflag
				 * && result[i][1].equals("0005") && result[i][8].equals("1059")) prodflag =
				 * false ; if (prodflag && !rosaceaflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1060")) prodflag = false ; if (prodflag &&
				 * !facelinesflag && result[i][1].equals("0005") && result[i][8].equals("1061"))
				 * prodflag = false ; if (prodflag && !age56flag && result[i][1].equals("0005")
				 * && result[i][8].equals("1062")) prodflag = false ; if (prodflag && !age66flag
				 * && result[i][1].equals("0005") && result[i][8].equals("1063")) prodflag =
				 * false ; if (prodflag && !age75flag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1064")) prodflag = false ; if (prodflag && !oilyflag &&
				 * result[i][1].equals("0005") && result[i][8].equals("1065")) prodflag = false
				 * ; if (prodflag && !sensitiveflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1066")) prodflag = false ; if (prodflag && !dullflag &&
				 * result[i][1].equals("0005") && result[i][8].equals("1067")) prodflag = false
				 * ; if (prodflag && !dryflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1068")) prodflag = false ; if (prodflag && !comboflag &&
				 * result[i][1].equals("0005") && result[i][8].equals("1069")) prodflag = false
				 * ; if (prodflag && !occnacneflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1070")) prodflag = false ; if (prodflag && !persacneflag
				 * && result[i][1].equals("0005") && result[i][8].equals("1071")) prodflag =
				 * false ; if (result[i][0].equals("201456"))
				 * System.out.println("eyes lines flag "+
				 * eyeslinesflag+" sub category "+result[i][8]) ;
				 * 
				 * if (prodflag && !eyeslinesflag && result[i][1].equals("0004") &&
				 * result[i][8].equals("1072")) prodflag = false ;
				 * 
				 * if (prodflag && !dullflag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1073")) prodflag = false ; if (prodflag && !age66flag &&
				 * result[i][1].equals("0003") && result[i][8].equals("1074")) prodflag = false
				 * ; if (prodflag && !age46flag && result[i][1].equals("0004") &&
				 * result[i][8].equals("1075")) prodflag = false ;
				 * 
				 * if (prodflag && !normalflag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1076")) prodflag = false ; if (prodflag &&
				 * !eyescreasesflag && result[i][1].equals("0004") &&
				 * result[i][8].equals("1077")) prodflag = false ; if (prodflag &&
				 * !noskinconcernsflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1083")) prodflag = false ; if (prodflag && !dryflag &&
				 * result[i][1].equals("0009") && result[i][8].equals("1078")) prodflag = false
				 * ; if (prodflag && !normalflag && result[i][1].equals("0009") &&
				 * result[i][8].equals("1079")) prodflag = false ; if (prodflag && !comboflag &&
				 * result[i][1].equals("0009") && result[i][8].equals("1080")) prodflag = false
				 * ; if (prodflag && !oilyflag && result[i][1].equals("0009") &&
				 * result[i][8].equals("1081")) prodflag = false ; if (prodflag &&
				 * !sensitiveflag && result[i][1].equals("0009") && result[i][8].equals("1082"))
				 * prodflag = false ;
				 * 
				 * if (prodflag && !age46flag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1084")) prodflag = false ;
				 * 
				 * if (prodflag && !age25flag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1085")) prodflag = false ; if (prodflag && !age56flag &&
				 * result[i][1].equals("0003") && result[i][8].equals("1086")) prodflag = false
				 * ; if (prodflag && !dryflakyflag && result[i][1].equals("0003") &&
				 * result[i][8].equals("1087")) prodflag = false ; if (prodflag &&
				 * !facelinesflag && result[i][1].equals("0003") && result[i][8].equals("1088"))
				 * prodflag = false ; if (prodflag && !noeyeconcernsflag &&
				 * result[i][1].equals("0004") && result[i][8].equals("1089")) prodflag = false
				 * ; if (prodflag && !age36flag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1090")) prodflag = false ;
				 * 
				 * if (prodflag && !under25flag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1091")) prodflag = false ; if (prodflag && !under25flag
				 * && result[i][1].equals("0003") && result[i][8].equals("1092")) prodflag =
				 * false ; if (prodflag && !age25flag && result[i][1].equals("0003") &&
				 * result[i][8].equals("1093")) prodflag = false ; if (prodflag && !age25flag &&
				 * result[i][1].equals("0005") && result[i][8].equals("1094")) prodflag = false
				 * ;
				 * 
				 * if (prodflag && !age46flag && result[i][1].equals("0009") &&
				 * result[i][8].equals("1095")) prodflag = false ; if (prodflag && !asianflag &&
				 * result[i][1].equals("0005") && result[i][8].equals("1096")) prodflag = false
				 * ; if (prodflag && !dryflakyflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1097")) prodflag = false ; if (prodflag && !rosaceaflag
				 * && result[i][1].equals("0009") && result[i][8].equals("1098")) prodflag =
				 * false ;
				 * 
				 * if (prodflag && !normalflag && result[i][1].equals("0005") &&
				 * result[i][8].equals("1099")) prodflag = false ; if (prodflag &&
				 * !noskinconcernsflag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1100")) prodflag = false ; if (prodflag && !agespotflag
				 * && result[i][1].equals("0003") && result[i][8].equals("1101")) prodflag =
				 * false ; if (prodflag && !sensitiveflag && result[i][1].equals("0004") &&
				 * result[i][8].equals("1102")) prodflag = false ;
				 * 
				 * if (prodflag && !age36flag && result[i][1].equals("0001") &&
				 * result[i][8].equals("1103")) prodflag = false ; if (prodflag && !age36flag &&
				 * result[i][1].equals("0004") && result[i][8].equals("1104")) prodflag = false
				 * ; if (prodflag && !comboflag && result[i][1].equals("0004") &&
				 * result[i][8].equals("1105")) prodflag = false ; if (prodflag && !lossflag &&
				 * result[i][1].equals("0003") && result[i][8].equals("1106")) prodflag = false
				 * ; if (prodflag && !age36flag && result[i][1].equals("0003") &&
				 * result[i][8].equals("1107")) prodflag = false ; if (prodflag && !agespotflag
				 * && result[i][1].equals("0001") && result[i][8].equals("1108")) prodflag =
				 * false ; if (prodflag && !age25flag && result[i][1].equals("0004") &&
				 * result[i][8].equals("1109")) prodflag = false ; if (prodflag && !dullflag &&
				 * result[i][1].equals("0003") && result[i][8].equals("1110")) prodflag = false
				 * ; if (prodflag && !dryflag && result[i][1].equals("0004") &&
				 * result[i][8].equals("1111")) prodflag = false ;
				 * 
				 * }
				 */ // if (prodflag)
//         else if (!result[i][8].equals("9999"))
				// prodflag = false ;
//		    System.out.println("2... "+result[i][0]+"  "+result[i][4]+" "+prodflag)  ;

				if (prodflag) {
//		    //logger.log(Level.DEBUG,"prodflag begin") ;

					ProductVO pr = new ProductVO();
					pr.setProdId(result[i][0].trim());
					pr.setBase(result[i][2]);
					pr.setProductLine(result[i][3]);
					pr.setProdName(result[i][4]);
					pr.setPrice(result[i][5]);
					pr.setImage(result[i][6]);
					pr.setSPFId(result[i][7]);
					pr.setProdType(result[i][8]);
					pr.setSellCopy(result[i][9]);
//************************************** Added by Synoptek(29-11-2019) set data of upc & sku ********************************************* 
					// ------------------Start--------------------------------------
					pr.setSku(result[i][10]);
					pr.setUpc(result[i][11]);
//************************************** Added by Synoptek(30-12-2019) set data of master category ********************************************* 
					pr.setMcategoryName(result[i][12]);
					pr.setWhenToUse(result[i][13]);
					pr.setMultiBenefits(result[i][14]);
					pr.setLifeStyleBasedRecs1(result[i][15]);
					pr.setLifeStyleBasedRecs2(result[i][16]);
//-------------------End----------------------------------------------------
					// Get brand name for product recommendation api - Deep - Start
					pr.setProductLineName(result[i][17]);
					// Get brand name for product recommendation api - Deep - End
					pr.setIngrScore("0");
					pr.setFunctionscore("0");
					pr.setBumpScore("0");
					pr.setTobeDeleted(false);
					String mcateg = "";
					mcateg = result[i][1];
					/*
					 * if (acneflag) { if (pcateg.equals("0011")) mcateg = "0005" ; if
					 * (pcateg.equals("0012")) mcateg = "0001" ; if (pcateg.equals("0013")) mcateg =
					 * "0003" ; } if (rosaceaflag) { if (pcateg.equals("0015")) mcateg = "0005" ; if
					 * (pcateg.equals("0016")) mcateg = "0001" ; if (pcateg.equals("0017")) mcateg =
					 * "0003" ; } if (agespotflag) { if (pcateg.equals("0014")) mcateg = "0005" ; }
					 */
					pr.setMasterProdType(mcateg);
					/*
					 * pr.setImage(result[i][1]); pr.setPrice(result[i][2]);
					 * pr.setShortDesc(result[i][3]); pr.setProdName(result[i][4]);
					 * pr.setProdType(result[i][5]);
					 */
//			pr.setProductLinePrime(result[i][9]);
//			if(result[i].length > 10)
//				pr.setComments(result[i][10]);
					
					prodhash.put(result[i][0].trim(), pr);
//		    //logger.log(Level.DEBUG,"prodflag  end "+result[i][0]) ;
				} // if prodflag
				/**
				 * Added By Synoptek List of all elminated products who fail to satisfy static
				 * condition
				 */
				else {
					ProductVO tmpproduct = new ProductVO();
					tmpproduct.setProdId(result[i][0].trim());
					tmpproduct.setBase(result[i][2]);
					tmpproduct.setProductLine(result[i][3]);
					tmpproduct.setProdName(result[i][4]);
					tmpproduct.setSPFId(result[i][7]);
					tmpproduct.setProdType(result[i][8]);
					tmpproduct.setSku(result[i][10]);
					tmpproduct.setUpc(result[i][11]);
					tmpproduct.setMcategoryName(result[i][12]);
					tmpproduct.setWhenToUse(result[i][13]);
					tmpproduct.setMultiBenefits(result[i][14]);
					tmpproduct.setLifeStyleBasedRecs1(result[i][15]);
					tmpproduct.setLifeStyleBasedRecs2(result[i][16]);
					// Get brand name for product recommendation api - Deep - Start
					tmpproduct.setProductLineName(result[i][17]);
					// Get brand name for product recommendation api - Deep - End
					tmpproduct.setTobeDeleted(true);
					eliminated_products.put(result[i][0].trim(), tmpproduct);
				}
			} // if
		} // for
			// System.out.println("vbbbbbb "+prodhash.size()) ;
			// Enumeration en = prodhash.keys() ;
			// while (en.hasMoreElements())
			// System.out.println("kdsfskh "+en.nextElement());
		all_products.put("eligible products", prodhash);
		all_products.put("eliminated products", eliminated_products);
		return all_products;
	}

	//Exception rules - Shivam - 24/02/2025 - Start
	/**
	 * @param isTrimRequired
	 * @return exceptional products map for elimination as per <b>Master Exception_Bump Rules 09.29.2025.xlsx</b>
	 */
	public Map<String, Set<String>> getExceptionRuleProductsMap(boolean isTrimRequired) {
		
		Map<String, Set<String>> exceptionRuleProductsMap = new HashMap<>();
		String productId;
		String ruleConcerns;
		Set<String> ruleConcernsSet;
		String key;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			ResultSet resultSet = dbo.executeQueryRS(dba.ExceptionRuleProducts());
			
			while (resultSet.next()) {
				ruleConcernsSet = new HashSet<>();
				productId = Objects.nonNull(resultSet.getString("SV_PRODUCT_ID")) ? resultSet.getString("SV_PRODUCT_ID") : "";
				ruleConcerns = Objects.nonNull(resultSet.getString("SELECTED_SKIN_CONCERNS")) ? resultSet.getString("SELECTED_SKIN_CONCERNS") : "";
				
				ruleConcernsSet.addAll(Arrays.asList(ruleConcerns.split(",")));
				
				key = isTrimRequired ? productId.trim() : productId;
				
				if (exceptionRuleProductsMap.containsKey(key)) {
					exceptionRuleProductsMap.get(key)
							.addAll(ruleConcernsSet);
				} else {
					exceptionRuleProductsMap.put(key, ruleConcernsSet);
				}
			}
		} catch (Exception ex1) {
			ex1.printStackTrace();
		} finally {
			dbo.closeConnection();
		}
		return exceptionRuleProductsMap;
	}
	//Exception rules - Shivam - 24/02/2025 - End

	public Set<String> getEliminatedProducts(CustomerProfileVO cp, Map<String, String> eliminationRuleMap) {
		Set<String> eliminatedProducts = new HashSet<String>(new ArrayList<>(eliminationRuleMap.values()));
		Set<String> recomdateProdList = new HashSet<String>(); //Added
		boolean flag=false;
		for (String name : eliminationRuleMap.keySet()) {
		String regex = ",";
		if(name.contains(regex)) {
			String proAttribute[] = name.split(",");
			int attributeFlag = 0;
			for(int i = 0; i < proAttribute.length; i ++) {
				
				if (cp.getEyeConcernList().contains(proAttribute[i]) || cp.getSkinConcernList().contains(proAttribute[i])
						|| cp.getSkinConditionList().contains(proAttribute[i]) || cp.getAge(
								).equalsIgnoreCase(proAttribute[i]) || cp.getSpecification().contains(proAttribute[i])
								|| cp.getSkinType().equalsIgnoreCase(proAttribute[i])) {
					attributeFlag ++;
				}
			}
			
			if(proAttribute.length == attributeFlag) {
				recomdateProdList.add(eliminationRuleMap.get(name));// Added
				eliminatedProducts.remove(eliminationRuleMap.get(name));
			}
			
		}
		else {
			if (cp.getEyeConcernList().contains(name) || cp.getSkinConcernList().contains(name)
					|| cp.getSkinConditionList().contains(name) || cp.getAge(
							).equalsIgnoreCase(name) || cp.getSpecification().contains(name)
							|| cp.getSkinType().equalsIgnoreCase(name)) {
						recomdateProdList.add(eliminationRuleMap.get(name));// Added
						eliminatedProducts.remove(eliminationRuleMap.get(name));
				}
		}	
	}
	
		//Added - Start for multiple same name attribute
		Set<String> eliminatedProductsfinal = new HashSet<String>();
		String[] listEp = eliminatedProducts.toArray(new String[0]);
		if (recomdateProdList.size() > 0) {
			for (String elementProdutList : recomdateProdList) {
				String item[] = elementProdutList.split(",");
				if (item != null && item.length > 0) {
					for (String productId : item) {
						for (int i = 0; i < listEp.length; i++) {
							if (listEp[i].contains(productId)) {
								listEp[i] = listEp[i].replaceAll(productId, "");
								listEp[i] = listEp[i].replaceAll(",,", ",");
							}
						}
					}
				}
			}
			eliminatedProductsfinal = new HashSet<>(Arrays.asList(listEp)); 
		}else {
			eliminatedProductsfinal = new HashSet<String>(new ArrayList<>(eliminationRuleMap.values()));
		}
		//Added - end
		return eliminatedProductsfinal;
		}


	public Map<String, String> getEliminationRuleProducts() {
		Map<String, String> eliminationRuleMap = new HashMap<String, String>();
		String[][] eliminationRuleList = null;
		String sql = "";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = "select * from product_elimination_rules";
			eliminationRuleList = dbo.executeQuery(sql);
			Map<String, String> eliminationMapForCopy = new HashMap<String, String>();
			for (int j = 0; j < eliminationRuleList.length; j++) {
				String value = "";
				if (!eliminationMapForCopy.containsKey(eliminationRuleList[j][0])) {
					eliminationMapForCopy.put(eliminationRuleList[j][0], eliminationRuleList[j][1]);
					value = eliminationMapForCopy.get(eliminationRuleList[j][0]);
				} else {
					value = eliminationRuleMap.get(eliminationRuleList[j][0]).concat(",").concat(eliminationRuleList[j][1]);
				}
				
				eliminationRuleMap.put(eliminationRuleList[j][0], value);
			}
			
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			dbo.closeConnection();
		}
		return eliminationRuleMap;
	}

	public Hashtable getBeautyProductInfo(String prodids) {
		String[][] result = null;
		Hashtable prods = new Hashtable();
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			result = dbo.executeQuery(dba.getBeautyProductInfo(prodids));
		} catch (Exception ex1) {
			ex1.printStackTrace();
		} finally {
			dbo.closeConnection();
		}

		for (int i = 0; i < result.length; i++) {

//		    //logger.log(Level.DEBUG,"prodflag begin") ;
			ProductVO pr = new ProductVO();
			pr.setProdId(result[i][0].trim());
			pr.setProdName(result[i][1]);
			pr.setPrice(result[i][2]);
			pr.setImage(result[i][3]);
			pr.setProductLinePrime(result[i][4]);
			pr.setSPFId(result[i][5]);
			pr.setMasterProdType(result[i][6]);
			pr.setBase(result[i][7]);
			pr.setProdType(result[i][8]);
			pr.setSellCopy(result[i][9]);
			if (result[i][9] != null && result[i][9].trim().length() == 0)
				pr.setSellCopy(" ");
			pr.setShortDesc(result[i][10]);
			prods.put(result[i][0].trim(), pr);
		}

		return prods;
	}

	protected ProductVO getProductIngredients(ArrayList prodingredients, ProductVO tmpproduct) {
		String tmpprodingredients[][] = new String[prodingredients.size()][3];
		String prodid = (String) tmpproduct.getProdId();
		/**
		 * Added by synoptek (20-02-2020) seperated ingredients id - name combination
		 */
		/*
		 * commented useless sysout for performance improvement - Shivam - 22/07/2024
		 * 
		if(prodingredients != null &&  prodingredients.size() > 0) {
			System.out.println("prodingredients :: " + prodingredients.size());
		}*/
		for (int j = 0; j < prodingredients.size(); j++) {
			StringTokenizer stk = new StringTokenizer((String) prodingredients.get(j), "-");
			while (stk.hasMoreTokens()) {

				String ingrid = stk.nextToken();
				String conc = stk.nextToken();

				String ingrname = stk.nextToken();
				if (ingrname.contains("@@@@")) {
					ingrname = ingrname.replace("@@@@", "-");
				}
				if (conc == null || conc.trim().length() == 0 || conc.equals("null")) // if the product concentration
																						// is not entered default it to
																						// NA
					conc = "NA";
				// if (prodid.equals("52663"))
				// System.out.println("^^^^^^ "+ingrid) ;
				tmpprodingredients[j][0] = ingrid;
				tmpprodingredients[j][1] = conc;
				tmpprodingredients[j][2] = ingrname;

			}
		}
//		System.out.println("csdhfhks "+tmpproduct.getProdId()) ;
		tmpproduct.setIngredients(tmpprodingredients);
		return tmpproduct;

	}

	// this method eliminates the products that r need to be eliminated based on
	// the ingredients that got eliminated earlier

	// this methos gets the ingredients present in the products
	public Hashtable getProductsIngredients(String prodtype, String prodselect, String prodline, Hashtable<String, ProductVO> prodhash) {
		/*
		 * Enumeration eph = prodhash.keys() ; while (eph.hasMoreElements()) { String
		 * ephkey = (String) eph.nextElement() ; // System.out.println("&&&&&& "+ephkey)
		 * ; }
		 */
		String result[][] = null;
		String prodid = "begin";;
		if(prodhash.size() == 1) {
			Enumeration en = prodhash.keys();
			prodid = (String) en.nextElement();
			prodselect = "oneProdId";
		}
		ArrayList prodingredients = new ArrayList();
		boolean prodflag = false;
		int iflag = 0;
//       System.out.println("dghhhhhh  prodhash size vb "+prodhash.size()) ;
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();

			if (prodselect != null && prodselect.equals("prodLine"))
				result = dbo.executeQuery(dba.getProductsIngredientsLine(prodline, prodselect));
			else if (prodselect != null && prodselect.equals("prodType"))
				result = dbo.executeQuery(dba.getProductsIngredientsLine(prodtype, prodselect));
			else if (prodselect != null && prodselect.equals("prodId"))
				result = dbo.executeQuery(dba.getProductIngredients(prodid, false));
			else if (prodselect != null && prodselect.equals("oneProdId"))
				result = dbo.executeQuery(dba.getProductIngredientsDetail(prodid));
			else
				result = dbo.executeQuery(dba.getProductsIngredientsLine("", ""));

			// analysis result = dbo.executeQuery(dba.getProductsIngredients(prodselect));
			//// logger.log(Level.DEBUG,"result ingr length "+result.length) ;
			// get the prodcuts along with ingredients
			// System.out.println("vbbb result length "+result.length) ;
			for (int i = 0; i < result.length; i++) {

//************************************** Added by Synoptek(30-12-2019)****************************************************** 
//--------------------------start----------------------------------------------	
				if (result[i][3].contains("-")) {
					result[i][3] = result[i][3].replace("-", "@@@@");
				}
//--------------------------End------------------------------------------------
				prodflag = true;
				if (prodflag) {
					if (iflag != 0 && !prodid.equals(result[i][0].trim())) // if the oldprodid is
					// diff from the one we
					// r checking now ,
					{
						// System.out.println("dfgddfdg prodid 2 "+prodid) ;
						if (prodhash.get(prodid) != null) {
							// System.out.println("dfgddfdg prodid 3
							// "+((ProductVO)prodhash.get(prodid)).getProdId()) ;
							prodhash.put(prodid,
									getProductIngredients(prodingredients, (ProductVO) prodhash.get(prodid)));
//	                System.out.println("proding 1"+prodid+"  "+result[i][1]) ;
							// System.out.println("@@@@@@ 2 "+prodid+" result "+result[i][0]+"
							// "+result[i][1] ) ;
						}
						if (prodhash.get(result[i][0].trim()) != null) {
							prodingredients = new ArrayList();
							// System.out.println("@@@@@@ 2A "+prodid+" result "+result[i][0]+"
							// "+result[i][1] ) ;
							prodingredients.add(result[i][1] + "-" + result[i][2] + "-" + result[i][3]);// add
						}
					} else {
						if (i == 0) {
							prodingredients = new ArrayList();
							// System.out.println("@@@@@@ 0 "+prodid+" result "+result[i][0]+"
							// "+result[i][1] ) ;
							prodingredients.add(result[i][1] + "-" + result[i][2] + "-" + result[i][3]);// add
						} else if (prodhash.get(prodid.trim()) != null) {
							// System.out.println("@@@@@@ 1 "+prodid+" result "+result[i][0]+"
							// "+result[i][1] ) ;
							prodingredients.add(result[i][1] + "-" + result[i][2] + "-" + result[i][3]); // add
							// System.out.println("proding 2"+prodid+" "+result[i][1]) ;
						} // the
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
					iflag = iflag + 1;
					// System.out.println("dfgddfdg prodid 1 "+prodid) ;
				}
			}
			if (result.length > 0) {
				if (prodhash.get(prodid) != null)
					prodhash.put(prodid,
							getProductIngredients(prodingredients, (ProductVO) prodhash.get(prodid.trim())));
			}

			if (result.length == 0)
				noingredients = true;
		} catch (Exception ex1) {
			ex1.printStackTrace();
		} finally {
			dbo.closeConnection();
		}
		/*
		 * Enumeration em = prodhash.keys() ; while (em.hasMoreElements()) { //
		 * System.out.println("vb2 11 "+em.nextElement()) ; String tt = (String)
		 * em.nextElement() ; if (tt.equals("76347")) { ProductVO tmpproduct =
		 * (ProductVO) prodhash.get(tt) ; String ingr[][] = tmpproduct.getIngredients();
		 * // for (int i=0; i < ingr.length ; i++) //
		 * System.out.println("vb prodingrs 555 "+tmpproduct.getProdId()+"  "+ingr[i][0]
		 * ) ; } }
		 */
		return prodhash;
	}

	public String[][] getCustomerProfileQuery(SVDBOperation dbo, SVBeautydba dba, String customid) {
		return dbo.executeQuery(dba.getCustomerProfile(customid));
		/*
		 * String result[][] = new String[12][3] ; result[0][0] = "100181141" ;
		 * result[0][1] = "51" ; result[0][2] ="T003" ; result[1][0] = "100181141" ;
		 * result[1][1] = "52" ; result[1][2] ="D003" ; result[2][0] = "100181141" ;
		 * result[2][1] = "52" ; result[2][2] ="D006" ; result[3][0] = "100181141" ;
		 * result[3][1] = "53" ; result[3][2] ="C001" ; result[4][0] = "100181141" ;
		 * result[4][1] = "53" ; result[4][2] ="C003" ; result[5][0] = "100181141" ;
		 * result[5][1] = "53" ; result[5][2] ="C009" ; result[6][0] = "100181141" ;
		 * result[6][1] = "53" ; result[6][2] ="C010" ; result[7][0] = "100181141" ;
		 * result[7][1] = "55" ; result[7][2] ="G003" ; result[8][0] = "100181141" ;
		 * result[8][1] = "56" ; result[8][2] ="L001" ; result[9][0] = "100181141" ;
		 * result[9][1] = "56" ; result[9][2] ="L002" ; result[10][0] = "100181141" ;
		 * result[10][1] = "57" ; result[10][2] ="X002" ; result[11][0] = "100181141" ;
		 * result[11][1] = "59" ; result[11][2] ="S002" ; return result ;
		 */
	}

	public String[][] getCustomerRegimenQuery(SVDBOperation dbo, SVBeautydba dba, String customid) {
		return dbo.executeQuery(dba.getCustomerRegimen(customid));
	}

	public CustomerProfileVO getCustomerProfile(String customid, CustomerProfileVO cp, String tableselect) {

		Hashtable profile = new Hashtable();
		// String result[][] = dbo.executeQuery(dba.getCustomerProfile(customid));
		String result[][] = new String[0][0];
		ArrayList scondition = new ArrayList();
		ArrayList econcern = new ArrayList();
		ArrayList sconcern = new ArrayList();
		ArrayList sallergy = new ArrayList();
		ArrayList specification = new ArrayList();

		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			result = getCustomerProfileQuery(dbo, dba, customid);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbo.closeConnection();
		}
		for (int i = 0; i < result.length; i++) {

			cp.setCustomerId(customid);

			if (result[i][1] != null && result[i][2].trim().equals(ConstantDeclarations.SKIN_TYPE_ID))
				cp.setSkinType(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals(ConstantDeclarations.SKIN_CONDITION_ID))
				scondition.add(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals(ConstantDeclarations.SKIN_CONCERN_ID))
				sconcern.add(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals("54")) {
				if (!result[i][3].equals("E999"))
					econcern.add(result[i][3]);
				else if (result[i][3].equals("E999")) {
					econcern.add("E999");
					// econcern.add("E001");
					// econcern.add("E004");
					// econcern.add("C001");
					// econcern.add("C002");
				}

			} else if (result[i][1] != null && result[i][2].trim().equals(ConstantDeclarations.SKIN_AGE_ID))
				cp.setAge(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals(ConstantDeclarations.SKIN_ALLERGY_ID))
				sallergy.add(result[i][3]);

			else if (result[i][1] != null && result[i][2].trim().equals("58"))
				cp.setEthnicity(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals("57"))
				cp.setNatural(result[i][3]);
			else if (result[i][1] != null && result[i][2].trim().equals("60"))
				specification.add(result[i][3]);
			profile.put(result[i][2].trim() + "-" + result[i][3], "1");

		}
		cp.setSkinconditionList(scondition);
		cp.setSkinConcernList(sconcern);
		cp.setEyeConcernList(econcern);
		cp.setAllergyList(sallergy);

		cp.setProfileHT(profile);
		cp.setSpecification(specification);

		// System.out.println("customer skin condition list szie
		// "+cp.getSkinConditionList().size());
		// System.out.println("customer skin concern list szie
		// "+cp.getSkinConcernList().size());
		// System.out.println("customer eye concern list szie
		// "+cp.getEyeConcernList().size());
		return cp;
	}

	public CustomerProfileVO getCustomerGeneralProfile(String customid, CustomerProfileVO cp) {
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String result[][] = new String[0][0];

			result = dbo.executeQuery(dba.getCustomerGenProfile(customid));

			if (result.length > 0) {
				cp.setCustomerEntryDate(result[0][4]);
				cp.setGender(result[0][5].trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbo.closeConnection();
		}

		return cp;
	}

	protected String increaseScore(String value) {

		/*
		 * commented for performance reasons. try {
		 * 
		 * float val = Float.parseFloat(value.trim());
		 * 
		 * /* if(val == 7.0f) // commented on 03/25/03 val = val * 1.4f * 2f ; else
		 * if(val == 8.0f) val = val * 1.5f * 2f ; else if(val == 9.0f) val = val*1.75f
		 * * 2f ; else if(val==10f) val = val * 2.00f * 2f ;
		 */
		/*
		 * int v = Math.round(val); value = v + ""; } catch (Exception e) {
		 * ////logger.log(Level.DEBUG,e); }
		 */
		return value;

	}

	// ht1 is the hastable that contains prodid as key and the ingredeient
	// ArrayList related to that prodid as the value
	// ratinsgs is the array that contains the ingredient record which we got
	// from database .

	public Hashtable getIngredientsRatings(CustomerProfileVO cp, boolean manual) {
		Hashtable ratings = new Hashtable();
		String result[][] = new String[0][0];
		AttribRatingVO ar = new AttribRatingVO();
		String ingrlist = "";

		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			// System.out.println("I am here bn nmm") ;
			result = dbo.executeQuery(dba.getIngredientsRatings(cp.getSkinType(), cp.getSkinConditionList(),
					cp.getSkinConcernList(), cp.getEyeConcernList(), ingrlist));

			for (int i = 0; i < result.length; i++) {
				// key : 02-ingr-conc-mastertype
				String key = result[i][0].trim() + "-" + result[i][1].trim() + "-" + result[i][2].trim() + "-"
						+ result[i][3].trim();
				String value = result[i][6].trim();
				String sub = key + "-" + result[i][4] + "-" + result[i][5] + "-" + value;
				// key+ 51-T003-4 51-skintype ; T003 - dry ; rating is 4

				////// logger.log(Level.DEBUG,"ingr"+key+"-"+result[i][4]+"-"+result[i][5]+"-"+","+value);

				// System.out.println("vbbb
				// ingr"+key+"-"+result[i][4]+"-"+result[i][5]+"-"+","+value) ;
				if (ratings.get(key) != null) // if this is not the first rating
												// value
				{
					if (value != null && !value.trim().equals("NA")) {
						String tmpval = value;

						if (!manual && (result[i][0].trim()).equals(ConstantDeclarations.ATTRIBUTE_INGR))// if not
						// manual
						// and if
						// not
						// base
						{
							value = increaseScore(value);

						}
						ar = (AttribRatingVO) ratings.get(key);
						value = String.valueOf(Integer.parseInt(ar.getScore()) + Integer.parseInt(value.trim()));
//						value = String.valueOf(Math.round(Float.parseFloat(ar.getScore()) + Float.parseFloat(value.trim())));
						ar.setScore(value.trim());
						ar.setCount(ar.getCount() + 1);
						ar.getIngrConcList().add(sub);

						ratings.put(key, ar);

					}
				} else if (value != null && !value.trim().equals("NA")) // if first
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

			} // end of ratings

		} catch (Exception ex1) {
			ex1.printStackTrace();
		} finally {
			dbo.closeConnection();
		}
		return ratings;
	}
	
	public Hashtable getIngredientsRatingsAboveZero(CustomerProfileVO cp, boolean manual) {
		return getIngredientsRatingsAboveZero(cp, manual, "");
	}
	public Hashtable getIngredientsRatingsAboveZero(CustomerProfileVO cp, boolean manual, String productId) {
		Hashtable ratings = new Hashtable();
		String result[][] = new String[0][0];
		AttribRatingVO ar = new AttribRatingVO();
		String ingrlist = "";

		try {
			Long startTime = System.currentTimeMillis();
			dbo = new SVDBOperation();
			dbo.createConnection();
			Long endTime = System.currentTimeMillis();
			System.out.println("connection creation time "+(endTime-startTime) + " " + productId);
			// System.out.println("I am here bn nmm") ;
			if(productId != null && productId != "") {
				result = dbo.executeQuery(dba.getProductIngredients(productId, true)); //Single product API changes - Shivam - 04/09/2024
				for(int i = 0; i < result.length; i++) {
//					System.out.println("RESULT INGR ID :: "+ (i+1) + " " + result[i][1]);
					ingrlist+="'"+result[i][1]+"'";
					if(i != result.length-1) {
						ingrlist += ",";
					}
				}
			}
			result = new String[0][0];
			startTime = System.currentTimeMillis();
			result = dbo.executeQuery(dba.getIngredientsRatingsAboveZero(cp.getSkinType(), cp.getSkinConditionList(),
					cp.getSkinConcernList(), cp.getEyeConcernList(), ingrlist, cp.getSpecification()));
			endTime = System.currentTimeMillis();
			System.out.println("query time "+(endTime-startTime));
			startTime = System.currentTimeMillis();
			for (int i = 0; i < result.length; i++) {
				// key : 02-ingr-conc-mastertype

				String key = result[i][0].trim() + "-" + result[i][1].trim() + "-" + result[i][2].trim() + "-"
						+ result[i][3].trim();
				String value = result[i][6].trim();
				String sub = key + "-" + result[i][4] + "-" + result[i][5] + "-" + value;
//				System.out.println("KEY KEY :: " + (i+1) + " " + key);
//				System.out.println("SUB SUB :: " + (i+1) + " " + sub);
				// key+ 51-T003-4 51-skintype ; T003 - dry ; rating is 4

				////// logger.log(Level.DEBUG,"ingr"+key+"-"+result[i][4]+"-"+result[i][5]+"-"+","+value);

				// System.out.println("vbbb
				// ingr"+key+"-"+result[i][4]+"-"+result[i][5]+"-"+","+value) ;
				if (ratings.get(key) != null) // if this is not the first rating
												// value
				{
//					System.out.println("RATING NOT NULL");
					if (value != null && !value.trim().equals("NA")) {
						String tmpval = value;

						if (!manual && (result[i][0].trim()).equals(ConstantDeclarations.ATTRIBUTE_INGR))// if not
						// manual
						// and if
						// not
						// base
						{
							value = increaseScore(value);

						}
						ar = (AttribRatingVO) ratings.get(key);
						value = String.valueOf(Integer.parseInt(ar.getScore()) + Integer.parseInt(value.trim()));
//						value = String.valueOf(Math.round(Float.parseFloat(ar.getScore()) + Float.parseFloat(value.trim())));
						ar.setScore(value.trim());
						ar.setCount(ar.getCount() + 1);
						ar.getIngrConcList().add(sub);
//						System.out.println("AR AR :: " + ar);
						ratings.put(key, ar);

					}
				} else if (value != null && !value.trim().equals("NA")) // if first
																		// one
				{
//					System.out.println("RATING NULL");
					ar = new AttribRatingVO();

					if (!manual && (result[i][0].trim()).equals(ConstantDeclarations.ATTRIBUTE_INGR)) {
						value = increaseScore(value);
					}
					ar.setScore(value.trim());
					ar.setCount(1);
					ar.getIngrConcList().add(sub);
//					System.out.println("AR AR :: " + ar);
					ratings.put(key, ar);

				}

			} // end of ratings
			endTime = System.currentTimeMillis();
			System.out.println("loop time "+(endTime-startTime));
		} catch (Exception ex1) {
			ex1.printStackTrace();
		} finally {
			dbo.closeConnection();
		}
//		if(!ratings.isEmpty() && ratings.size() > 0) {
//			for(int i = 0; i < ratings.size(); i++) {
//				String key = ratings.keySet().toArray()[i].toString();
////				System.out.println("ratings ratings :: " + key + " " + ratings.get(key));
//			}
//		}
		return ratings;
	}
	public ProductVO[] ratings(CustomerProfileVO cp, ProductVO[] product, Hashtable ingrratings) {

		// //logger.log(Level.DEBUG,"product array size is"+product.length);
		BaseRatings baseRatings = new BaseRatings();
		Hashtable<String, String> baseDetail = (Hashtable) baseRatings.getProductBase();
		
		for (int count = 0; count < product.length; count++) {
			ProductVO producttmp = product[count];
			// log.debug(" product i = " + prodid + " product name = " +
			// producttmp.getProdName() + " brand id ="
			// + producttmp.getProductLine() + " is to be deleted = " +
			// producttmp.isTobeDeleted()+" base = "+producttmp.getBase()
			// +" category "+producttmp.getMasterProdType()+" category
			// name"+producttmp.getMcategoryName());
			String base = producttmp.getBase();
			String ingr[][] = producttmp.getIngredients();
			// if (ingr != null)
			// for (int j = 0; j < ingr.length; j++) {
			// if (product[count].getProdId().equals("52663"))
			// System.out.println("%%%%%%% "+ingr[j][0]) ;
			// }
			String prodmastertype = producttmp.getMasterProdType();
			String pmtype = "";
			boolean tobedeleted = producttmp.isTobeDeleted();
			boolean faceflag = false;
			boolean eyeflag = false;

			int ingrcount = 0;
			int ingrscore = 0;
			int basecount = 0;
			int basescore = 0;
			Hashtable ht = new Hashtable();
			Hashtable baseHT = new Hashtable();
			Hashtable ingredeintwithScore = new Hashtable();
			String ingredientname = "";
			//// logger.log(Level.DEBUG,"prodid "+prodid+" prodmaster type
			// is"+prodmastertype);

			if (!tobedeleted) // if this product is not to be deleted
			{
				//// logger.log(Level.DEBUG,"prodid id"+prodid);
				pmtype = prodmastertype;
				if (prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT))
					eyeflag = true;
				if (prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT))
					faceflag = true;

				if (prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_UV)
						|| prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_ETREAT))
					prodmastertype = ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT;
				if (!prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_CLEANSE)
						&& !prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_MOIST)
						&& !prodmastertype.equals(ConstantDeclarations.PRODUCT_MASTER_TYPE_TREAT))
					prodmastertype = "99";
				// prodmastertype = "99";
				String basestring = "01-" + base + "-NA-" + prodmastertype;
//				System.out.println("BASESTRING :: " + basestring);
				if (ingrratings.get(basestring) == null)
					basestring = "01-" + base + "-NA-99";

				//// logger.log(Level.DEBUG,prodid);
				//// logger.log(Level.DEBUG,"-start-");
				//// logger.log(Level.DEBUG,"base string"+basestring);

				// below is commented earlier this was functioning
				/**
				 * if(ingrratings.get(basestring) !=null ) { ////logger.log(Level.DEBUG,"base
				 * string"+basestring); String tmp =
				 * ((AttribRating)(ingrratings.get(basestring))).score; if(
				 * !tmp.trim().equals(":n")) { basescore = basescore +
				 * Integer.parseInt(((AttribRating)(ingrratings.get(basestring))).score) ;
				 * basecount = basecount + ((AttribRating)ingrratings.get(basestring)).count ;
				 * if(basecount==0) basecount = 1 ;
				 * ht.put(basestring,((AttribRating)ingrratings.get(basestring)).ingrconc); } }
				 */
				
//				System.out.println("BASESTRING :: " + basestring);
//				if(!ingrratings.isEmpty() && ingrratings.size() > 0) {
//					for(int i = 0; i < ingrratings.size(); i++) {
//						String key = ingrratings.keySet().toArray()[i].toString();
////						System.out.println("ingrratings ingrratings :: " + key + " " + ingrratings.get(key));
//					}
//				}
				
//				System.out.println("ingrratings :: " + ingrratings.get(basestring));
				
				if (ingrratings.get(basestring) != null) {
					AttribRatingVO att = (AttribRatingVO) ingrratings.get(basestring);
					//// logger.log(Level.DEBUG,"inside");

					ArrayList vect = att.getIngrConcList();
//					System.out.println("vect vect :: " + vect);
					for (int i = 0; i < vect.size(); i++) {
						String tmp = (String) vect.get(i);
						StringTokenizer stk = new StringTokenizer(tmp, "-");
						// 02"+"-"+ingr[j][0]+"-3-99" values : ArrayList that holds
						// 51-T003-4 , 52-0009-5
//						System.out.println("STK STK :: " + stk);
						if (stk.nextToken().equals(ConstantDeclarations.ATTRIBUTE_BASE))
							 // 02
						{
							int addval = 1;
							stk.nextToken(); // ingrid
							stk.nextToken(); // ingrconc
							stk.nextToken(); // mastertype
							String skin = stk.nextToken() + "-" + stk.nextToken();
//							System.out.println("SKIN SKIN :: " + skin);
							String tmskin = skin;
							// if ( skin.trim().substring(0, 2).equals(ConstantDeclarations.SKIN_TYPE_ID))
							// addval = 2 ;
							boolean concernflag = false;

							if (skin.trim().substring(0, 2).equals(ConstantDeclarations.SKIN_CONCERN_ID)) {
								String tskin = skin.trim().substring(3, 7);
								if (faceflag && ((tskin.trim().equals("C022") || tskin.trim().equals("C003")
										|| tskin.trim().equals("C020") || tskin.trim().equals("C004")
										|| tskin.trim().equals("C023") || tskin.trim().equals("C012")
										|| tskin.trim().equals("C019") || tskin.trim().equals("C007"))))
									concernflag = true;
								if (concernflag)
									tmskin = skin;
								else
									tmskin = "99999";
							}
							concernflag = false;
							/*
							 * vbbb 01/28 if ( skin.trim().substring(0, 2).equals("54")) { String tskin =
							 * skin.trim().substring(3, 7); if (eyeflag && ((tskin.trim().equals("C001") ||
							 * tskin.trim().equals("C002") || tskin.trim().equals("E001") ||
							 * tskin.trim().equals("E004") ) )) concernflag = true; if (concernflag) tmskin
							 * = skin; else tmskin = "99999"; } else vbb
							 */
							tmskin = skin;

							if (pmtype.equals("0005")) {
								if (tmskin.trim().equals("54-C001") || tmskin.trim().equals("54-C002")
										|| tmskin.trim().equals("54-E001") || tmskin.trim().equals("54-E004"))
									tmskin = "9999";
							}

							if (pmtype.equals("0001")) {
								if (tmskin.trim().equals("54-C001") || tmskin.trim().equals("54-C002")
										|| tmskin.trim().equals("54-E001") || tmskin.trim().equals("54-E004"))
									tmskin = "9999";
							}

							if (pmtype.equals("0004")) {
								if ((tmskin.trim().equals("53-C022") || tmskin.trim().equals("53-C003")
										|| tmskin.trim().equals("53-C020") || tmskin.trim().equals("53-C004")
										|| tmskin.trim().equals("53-C023") || tmskin.trim().equals("53-C012")
										|| tmskin.trim().equals("53-C019") || tmskin.trim().equals("53-C007")
//												|| tmskin.trim().equals("52-D001")
//												|| tmskin.trim().equals("52-D003")
//												|| tmskin.trim().equals("52-D006")
//												|| tmskin.trim().equals("52-D008")
//												|| tmskin.trim().equals("52-D002")
//												|| tmskin.trim().equals("51-T001")
//												|| tmskin.trim().equals("51-T002")
//												|| tmskin.trim().equals("51-T003")
//												|| tmskin.trim().equals("51-T004")
//												|| tmskin.trim().equals("54-C001")
//												|| tmskin.trim().equals("54-C002")
//												|| tmskin.trim().equals("54-E001")
//												|| tmskin.trim().equals("54-E004")
								))
//									System.out.println(" I am in base eyeconcerns filter "+tmskin) ;
									tmskin = "9999";
							}
							// //logger.log(Level.DEBUG,"tmskin "+tmskin);
							Hashtable profile = cp.getProfileHT();
							// //logger.log(Level.DEBUG,"basestring "+basestring);
//							System.out.println("PROFILE PROFILE :: " + profile);
							if (profile.get(tmskin) != null) {
								String tstkvb = (String) stk.nextToken();
								int sc = Integer.parseInt(tstkvb);
//								System.out.println("TSTKVB TSTKVB :: " + tstkvb);
								// if (product[count].getProdId().equals("52663"))
								// {
								// System.out.println("tetk "+tmskin+" "+sc) ;
								// }
								
								if (sc != 5) {
									// vb comm basescore = basescore + (sc * addval); // VB 9/10/07
									// vb comm basecount++;
									// vb comm if (addval == 2)
									// vb comm basecount++ ;
									baseHT.put(tmskin, "Y");
									basescore = basescore + (calculatescoreNew(sc * addval, pmtype, "base", 0)); // VB
//									System.out.println("basescore basescore// :: " + basescore);																		// 9/10/07
									basecount++;
//									System.out.println("basecount out :: " + basecount);
									if (addval == 2) {
										basecount++;
//										System.out.println("basecount IN :: " + basecount);
									}
										
								}
								if (sc == 0) // Added on 03/14/2005
								{
									producttmp.setReason(ConstantDeclarations.ZERO_FORMU_SCO);
									if (base != null && !base.isEmpty()) {
										producttmp.setEliminatedIngrName(baseDetail.get(base));
									} else {
										producttmp.setEliminatedIngrName(ConstantDeclarations.FORM_NOT_FOUND);
									}
									producttmp.setTobeDeleted(true); // Added on
																		// 03/14/2005
																		// to
																		// eleminate
																		// base
																		// product
																		// tmp fix
									// //logger.log(Level.DEBUG,"prodid
									// "+prodid+"basescore "+ sc +"basestring
									// "+basestring);
//										
								}
								// if (pmtype.equals("0004"))
								// System.out.println("VB Base string "+pmtype+" "+skin+" "+basestring) ;
								ht.put(basestring, ((AttribRatingVO) ingrratings.get(basestring)).getIngrConcList());
							}
						} // end of if stk

					} // end of for
//					ht.put(basestring, ((AttribRatingVO) ingrratings.get(basestring)).getIngrConcList());

				}

				//// logger.log(Level.DEBUG,"ingr");

				int score = 0;
				if (ingr != null)

					for (int j = 0; j < ingr.length; j++) {
						// 02-ingredientid-ingredientconc-productmastertype
						if (ingr[j][1] == null || ingr[j][1].trim().length() == 0)
							ingr[j][1] = "NA";
						String ingrstring = "02" + "-" + ingr[j][0] + "-" + ingr[j][1] + "-" + prodmastertype;
						// if (product[count].getProdId().equals("52663"))
						// {
						// if (ingr[j][0].equals("100148"))
						// System.out.println("vbbbbb ingr 1 "+ingrstring) ;
						// }

						if (ingrratings.get(ingrstring) == null)
							ingrstring = "02" + "-" + ingr[j][0] + "-" + ingr[j][1] + "-99";
						// if (product[count].getProdId().equals("52663"))
						// {
						// if (ingr[j][0].equals("100148"))
						// System.out.println("vbbbbb ingr 2 "+ingrstring) ;
						// }

						//// logger.log(Level.DEBUG,ingrstring);
						// System.out.println("ingr string "+ingrstring) ;

						if (ingrratings.get(ingrstring) != null) {
							ingredientname = ingr[j][0];
							// if (product[count].getProdId().equals("52663"))
							// {
							// System.out.println("vb ingr string "+ingrstring) ;
							// }
							AttribRatingVO att = (AttribRatingVO) ingrratings.get(ingrstring);
							//// logger.log(Level.DEBUG,"inside");

							ArrayList vect = att.getIngrConcList();
//							System.out.println(" cccc vect size "+vect.size()) ;
							for (int i = 0; i < vect.size(); i++) {
								String tmp = (String) vect.get(i);
								StringTokenizer stk = new StringTokenizer(tmp, "-");

								// 02"+"-"+ingr[j][0]+"-3-99" values : ArrayList
								// that holds 51-T003-4 , 52-0009-5
								if (stk.nextToken().equals(ConstantDeclarations.ATTRIBUTE_INGR))
									 // 02
								{
									int addval = 1;
									stk.nextToken(); // ingrid
									stk.nextToken(); // ingrconc
									stk.nextToken(); // mastertype
									String skin = stk.nextToken() + "-" + stk.nextToken();
									// //logger.log(Level.DEBUG,"skin"+skin+"
									// "+prodid+" "+handbodyflag+" "+faceflag);

									String tmskin = skin;
//									System.out.println("cccc tmskin "+tmskin) ;
									boolean concernflag = false;
									if (skin.trim().substring(0, 2).equals(ConstantDeclarations.SKIN_CONCERN_ID)) {
										String tskin = skin.trim().substring(3, 7);
										if (faceflag && ((tskin.trim().equals("C022") || tskin.trim().equals("C003")
												|| tskin.trim().equals("C020") || tskin.trim().equals("C004")
												|| tskin.trim().equals("C023") || tskin.trim().equals("C012")
												|| tskin.trim().equals("C019") || tskin.trim().equals("C007"))))
											concernflag = true;
										if (concernflag)
											tmskin = skin;
										else
											tmskin = "99999";
									}
									concernflag = false;
									if (skin.trim().substring(0, 2).equals("54")) {
										String tskin = skin.trim().substring(3, 7);
										if (eyeflag && ((tskin.trim().equals("C001") || tskin.trim().equals("C002")
												|| tskin.trim().equals("E001") || tskin.trim().equals("E004"))))
											concernflag = true;
										if (concernflag)
											tmskin = skin;
										else
											tmskin = "99999";
									} else
										tmskin = skin;

									// Not to apply eye scores to cleansers // 09-22-07 VB
									if (pmtype.equals("0001")) {
										if (tmskin.trim().equals("54-C001") || tmskin.trim().equals("54-C002")
												|| tmskin.trim().equals("54-E001") || tmskin.trim().equals("54-E004"))
											tmskin = "9999";
									}

									if (pmtype.equals("0005")) {
										if (tmskin.trim().equals("54-C001") || tmskin.trim().equals("54-C002")
												|| tmskin.trim().equals("54-E001") || tmskin.trim().equals("54-E004"))
											tmskin = "9999";
									}

									if (pmtype.equals("0004")) {
										if ((tmskin.trim().equals("53-C022") || tmskin.trim().equals("53-C003")
												|| tmskin.trim().equals("53-C020") || tmskin.trim().equals("53-C004")
												|| tmskin.trim().equals("53-C023") || tmskin.trim().equals("53-C012")
												|| tmskin.trim().equals("53-C019") || tmskin.trim().equals("53-C007")
												|| tmskin.trim().equals("52-D001") || tmskin.trim().equals("52-D003")
												|| tmskin.trim().equals("52-D006") || tmskin.trim().equals("52-D008")
												|| tmskin.trim().equals("52-D002") || tmskin.trim().equals("51-T001")
												|| tmskin.trim().equals("51-T002") || tmskin.trim().equals("51-T003")
												|| tmskin.trim().equals("51-T004")
												//Added by Shivam for eye score calculations - 06/09/2023 - Start
												|| tmskin.trim().equals("53-C025") //Flakiness / Dryness
												|| tmskin.trim().equals("53-C030") //Neck Tightening
												//Added by Shivam for eye score calculations - 06/09/2023 - End
												))
											// System.out.println(" I am in ingr eyeconcerns filter "+tmskin) ;
											tmskin = "9999";
									}
									// //logger.log(Level.DEBUG,"finally "+prodid+"
									// "+prodtype+" "+tmskin) ;
									Hashtable profile = cp.getProfileHT();
//									System.out.println("vbbbbbbbbbbbbbbbbbb "+tmskin+"  "+prodmastertype) ;
									if (profile.get(tmskin) != null) {

										// System.out.println("vfsdksh "+prodmastertype+" "+tmskin) ;

										// if ( tmskin.trim().substring(0, 2).equals(ConstantDeclarations.SKIN_TYPE_ID))
										// addval = 2 ;
										// if ( tmskin.trim().substring(0,
										// 2).equals(ConstantDeclarations.SKIN_CONDITION_ID))
										// addval = 2 ;
										String tstkvb = (String) stk.nextToken();

										int sc = Integer.parseInt(increaseScore(tstkvb));
										// if (product[count].getProdId().equals("52663"))
										// {
										// System.out.println("tstk "+tmskin+" "+sc) ;
										// }
										if (sc != 5) {
											// if (product[count].getProdId().equals("146228"))
											// System.out.println("VB Ingr string "+pmtype+" "+skin+" "+ingrstring) ;
											// comment vb ingrscore = ingrscore + ( sc * addval); // VB 09/10/07
											// comment vb ingrcount++;
											// comment vb if (addval == 2)
											// comment vb ingrcount++ ;
											int tmpingrpercen = 0;
											if (baseHT.get(tmskin) == null)
												tmpingrpercen = 100;
											score = calculatescoreNew(sc * addval, pmtype, "ingr", tmpingrpercen);

											ingrscore = ingrscore + score; // VB 9/10/07
//											System.out.println("ingrscore ingrscore// :: " + ingrscore);
											ingrcount++;

											if (addval == 2)
												ingrcount++;

										}
										if (ingredeintwithScore.containsKey(ingredientname)) {
											int value = (int) ingredeintwithScore.get(ingredientname);
											value += score;
											ingredeintwithScore.put(ingredientname, value);
										} else {
											ingredeintwithScore.put(ingredientname, score);
										}

										// if (product[count].getProdId().equals("146228"))
										// System.out.println("vbscore "+pmtype+" "+skin+" "+ingrscore+" count
										// "+ingrcount);
										ht.put(ingrstring,
												((AttribRatingVO) ingrratings.get(ingrstring)).getIngrConcList());

										score = 0;

									}

								} // end of if stk

							} // end of for
//							ht.put(ingrstring, ((AttribRatingVO) ingrratings.get(ingrstring)).getIngrConcList());
						} // end if

					} // end of for

				//// logger.log(Level.DEBUG,"-end");

			} // end of is to be deleted
				//// logger.log(Level.DEBUG,"producttmp"+producttmp.prodid);
//            System.out.println("ht size "+ht.size()) ;
			product[count] = producttmp;
			int ingrpercentage = 0;
			int basepercentage = 0;

			if (ingrcount != 0)
				ingrpercentage = 100 * ingrscore / (ingrcount * 10);
			
//			System.out.println("basescore// ::" + basescore);
			if (basecount != 0)
				basepercentage = 100 * basescore / (basecount * 10);
			// if (product[count].getProdId().equals("52663"))
			// {
			/// System.out.println("basescore bn "+basescore+" "+basecount+"
			// "+basepercentage) ;
			// System.out.println("ingrscore bn "+ingrscore+" "+ingrcount+"
			// "+ingrpercentage) ;
			// }
//			System.out.println("prodid"+product[count].getProdId()+"ingrpercentage"+ingrpercentage+"  "+ingrcount+"  "+"base percentage "+basepercentage+" "+basecount);
			// uncmmnet this , if u want to see the percentAGES
			// if(ingrpercentage > 100) // commented on 02/26/03
			// ingrpercentage = 100 ;

			product[count].setIngrfnccnt(String.valueOf(ingrcount));
			product[count].setBaseCount(String.valueOf(basecount));
			product[count].setIngrScore(String.valueOf(ingrpercentage));
			product[count].setBaseScore(String.valueOf(basepercentage));
			
//			System.out.println("String.valueOf(ingrcount) : " + String.valueOf(ingrcount));
//			System.out.println("String.valueOf(basecount) : " + String.valueOf(basecount));
//			System.out.println("String.valueOf(ingrpercentage) : " + String.valueOf(ingrpercentage));
//			System.out.println("String.valueOf(basepercentage) : " + String.valueOf(basepercentage));

			product[count].setIndividualingrratings(ht);
			// //logger.log(Level.DEBUG,"***"+cp.getCustomerId());
			// //logger.log(Level.DEBUG,"vbbbbbbscore "+product[count].score);
			product[count].setScore(calculatescore(product[count]));
			product[count].setIngrWeightage(String.valueOf(ingrscore));
			product[count].setBaseWeightage(String.valueOf(basescore));

			String[][] ingredeints = product[count].getIngredients();
			for (int r = 0; r < ingredeints.length; r++) {
				for (int c = 0; c < ingredeints[r].length; c++) {
					if (ingredeintwithScore.containsKey(ingredeints[r][c])) {
						ingredeints[r][2] = ingredeints[r][2]
								.concat("===" + (ingredeintwithScore.get(ingredeints[r][0])));
					} else
						break;
				}
			}
			product[count].setIngredients(ingredeints);
//		}
			//// logger.log(Level.DEBUG,"end of products");
		} // end of for for final products

		return product;
	}

	protected String calculatescore(ProductVO prod) {

		float ingrpercentage = Float.parseFloat(prod.getIngrScore());
		float basepercentage = Float.parseFloat(prod.getBaseScore());

		float funcpercentage = 0.0f;
		float bmpscore = 0.0f;
		try {
			bmpscore = Float.parseFloat(prod.getBumpScore());
		} catch (Exception bm) {
			bmpscore = 0.0f;
		}
		float score = 0.0f;

		if (funcpercentage != 0.0f) {
			basepercentage = (basepercentage * 10f) / 100f;
			ingrpercentage = (ingrpercentage * 70f) / 100f;
			funcpercentage = (funcpercentage * 20f) / 100f;
			score = ingrpercentage + basepercentage + funcpercentage + bmpscore;
			// //logger.log(Level.DEBUG,"1211 "+score+" d "+ingrpercentage+" ff
			// "+basepercentage+" f "+funcpercentage) ;
		} else {
			String pmtype = (String) prod.getMasterProdType();
			/*
			 * nnewvb comment if (pmtype.equals("0004")) { ingrpercentage = (ingrpercentage
			 * * 100f) / 100f; // original 90 score = ingrpercentage + bmpscore; } else if
			 * (pmtype.equals("0005")) { basepercentage = (basepercentage * 10f) / 100f; //
			 * original 10 ingrpercentage = (ingrpercentage * 90f) / 100f; // original 90
			 * score = ingrpercentage + bmpscore; } else { basepercentage = (basepercentage
			 * * 20f) / 100f; // original 10 ingrpercentage = (ingrpercentage * 80f) / 100f;
			 * // original 90 score = ingrpercentage + basepercentage + bmpscore; }
			 */
			score = ingrpercentage + basepercentage + bmpscore;
			// //logger.log(Level.DEBUG,"55511 "+score+" d "+ingrpercentage+" ff
			// "+basepercentage+" f "+funcpercentage) ;
		}
		// System.out.println("fianlly prodid"+prod.getProdId()+"score "+score+"
		// ingrpercentage"+ingrpercentage+" "+"base percentage "+basepercentage+" ");
		if (prod.getIngredients() == null) // if there r no ingredients present
											// , then get the manual score
		{
			if (prod.getManualScore() != null && prod.getManualScore().trim().length() > 0) {
				score = Float.parseFloat(prod.getManualScore());
				// //logger.log(Level.DEBUG,"166666211 "+score+" d "+ingrpercentage+"
				// ff "+basepercentage+" f "+funcpercentage) ;
			}
		}
		// //logger.log(Level.DEBUG,"score is "+prod.getProdid()+" "+score);
		return score + "";
	}

	protected int calculatescoreNew(int score, String pmtype, String attribtype, int ingrcalcpercen) {

		int basecalcpercen = 0;
		if (attribtype.equals("base")) {
			if (pmtype.equals("0004"))
				basecalcpercen = 20;
			else if (pmtype.equals("0005"))
				basecalcpercen = 10;
			else
				basecalcpercen = 20;
		}
		if (attribtype.equals("ingr")) {
			if (pmtype.equals("0004"))
				basecalcpercen = 100;
			else if (pmtype.equals("0005"))
				basecalcpercen = 90;
			else
				basecalcpercen = 80;
			if (ingrcalcpercen == 100)
				basecalcpercen = 100;
		}
		score = (score * basecalcpercen) / 100;

		return score;
	}

	// this method eliminates the ingredients that r not taken into ratings ,
	// ie , whose ratings values r less than 0
	protected Hashtable eliminateIngredients(String skintype, ArrayList skincondition) {

		Hashtable eliminatedingredients = new Hashtable();
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String result[][] = dbo.executeQuery(dba.eliminateIngredients(skintype, skincondition));
			for (int i = 0; i < result.length; i++) {

				//// logger.log(Level.DEBUG,"eliminated ingredients"+result[i][0]);
				eliminatedingredients.put(result[i][0], "Y");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbo.closeConnection();
		}
		return eliminatedingredients;
	}

	// this method eliminates the ingredients that r not taken into ratings ,
	// ie , whose ratings values r less than 0
	public Hashtable eliminateIngredientsBasedOnAttribute(String skintype, ArrayList skincondition, ArrayList specification) {

		Hashtable eliminatedingredients = new Hashtable();
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String result[][] = dbo.executeQuery(dba.eliminateIngredientsBasedOnAttribute(skintype, skincondition,specification));
			for (int i = 0; i < result.length; i++) {

				//// logger.log(Level.DEBUG,"eliminated ingredients"+result[i][0]);
				eliminatedingredients.put(result[i][0], "Y");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbo.closeConnection();
		}
		return eliminatedingredients;
	}

	public Hashtable eliminateAllIngredientsBasedOnAttribute(String skintype, ArrayList skincondition, ArrayList specification,Hashtable eliminatedAllIngredients) {

		Hashtable eliminatedingredients = new Hashtable();
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			String result[][] = dbo.executeQuery(dba.eliminateAllIngredientsBasedOnAttribute(skintype, skincondition,specification));
			String tempString= "";
			List attributeList = new ArrayList();
			for (int i = 0; i < result.length; i++) {

				//// logger.log(Level.DEBUG,"eliminated ingredients"+result[i][0]);
				if(!tempString.equals(result[i][0])) {
					eliminatedingredients.put(result[i][0], "Y");
					eliminatedAllIngredients.put(result[i][0], attributeList);
					attributeList = new ArrayList();
					tempString = result[i][0];
				}
				attributeList.add(result[i][1]);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbo.closeConnection();
		}
		return eliminatedingredients;
	}

	
	public ProductVO[] getProductArray(ProductVO[] productVOList, Hashtable prodhash) {
		int count = 0;
		Enumeration en = prodhash.keys();
		productVOList = new ProductVO[prodhash.size()];
		while (en.hasMoreElements()) {
			ProductVO pr = (ProductVO) prodhash.get(en.nextElement());
			productVOList[count++] = pr;

		}
		return productVOList;
	}

	public ProductVO[] getProductVO(CustomerProfileVO cp,ProductVO[] productVOList,Hashtable eliminatedRuleProducts, Hashtable master) {
		
		int count = 0;
		
		Map<String, String> eliminationRuleMap = getEliminationRuleProducts();
		Enumeration en = eliminatedRuleProducts.keys();
		productVOList = new ProductVO[eliminatedRuleProducts.size()];
		while (en.hasMoreElements()) {
			ProductVO pr = (ProductVO) eliminatedRuleProducts.get(en.nextElement());
			pr.setReason(ConstantDeclarations.ELIMINATION_RULE);
			String reason = "";
			String eliminationReason = "";
			for (String attribute : eliminationRuleMap.keySet()) {
				if (eliminationRuleMap.get(attribute).contains(pr.getUpc())) {
					// Change made by Anish Khandelwal 12-04-2022
					//Till now this logic is checking single attribute at once. But with this it will check multiple attribute in AND condition.
					String regex = ",";
					System.out.println(attribute.contains(regex));
					if(attribute.contains(regex)) {
						String proAttribute[] = attribute.split(",");
						int attributeFlag = 0;
						reason = "";
						for(int i = 0; i < proAttribute.length; i++) {
							String altReason = "";
							altReason = (altReason == "") ? (String) master.get(proAttribute[i]): altReason.concat(" or ").concat((String) master.get(proAttribute[i]));
							if(reason != null  && reason != "" && !altReason.equalsIgnoreCase(reason) && !reason.contains(altReason)) {
								reason = reason +" or " + altReason;
							}
							if(reason == null || reason == "") {
								reason = altReason;
							}
						}
					}
					else {
					reason = (reason == "") ? (String) master.get(attribute): reason.concat(" or ").concat((String) master.get(attribute));
					}
				}
			}
			
			// Exception rules - Shivam - 25/02/2025 - Start
			if (reason.trim().isEmpty()) {
				Map<String, Set<String>> exceptionRuleProductsMap = getExceptionRuleProductsMap(true);

				if (exceptionRuleProductsMap.containsKey(pr.getProdId())) {
					reason = exceptionRuleProductsMap.get(pr.getProdId()).stream()
							.map(attribute -> (String) master.get(attribute)).collect(Collectors.joining(" or "));
				}

			}
			// Exception rules - Shivam - 25/02/2025 - End
			
			eliminationReason = reason.concat(" is not selected by user");
			pr.setEliminatedIngrName(eliminationReason);
			productVOList[count++] = pr;
		}
		
		return productVOList;
	}
	
	
	public static void main(String args[]) {
		RecommendationEngine gp = new RecommendationEngine();
		String[] customers = new String[3];
		customers[0] = "C10062"; // leann
		customers[1] = "C10085"; // theresa
		customers[2] = "C10130"; // vb2
		// gp.getProduct("C10085", "01", "", "0002");

	}

	public String getProductLine(CustomerProfileVO cp) {
		String prodLine = "";
		ArrayList prodUsage = new ArrayList();
		prodUsage = (ArrayList) cp.getProductUsage();
		boolean anewflag = false;
		boolean avonflag = false;
		boolean becomeflag = false;

		if (prodUsage != null) {
			if (prodUsage.size() > 2)
				prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW;
			else if (prodUsage.size() == 1) {
				if (((String) prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_NATING))
					prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW;
				if (((String) prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_CUTEDT))
					prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW;
				if (((String) prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_AGEBNF))
					prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW;
				if (((String) prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_PRDSKT))
					prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_AVON;
				if (((String) prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_BTFPKG))
					prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_BCMG;
				if (((String) prodUsage.get(0)).equals(ConstantDeclarations.PRODUCT_USAGE_ATHDTR))
					prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW;
			} else if (prodUsage.size() == 2) {
				String pusage1 = (String) prodUsage.get(0);
				String pusage2 = (String) prodUsage.get(1);
				if (pusage1.equals(ConstantDeclarations.PRODUCT_USAGE_NATING)
						&& pusage2.equals(ConstantDeclarations.PRODUCT_USAGE_BTFPKG))
					// prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_BCMG ;
					prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW;
				else
					prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW;
			} else if (prodUsage.size() == 0)
				prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW;
		} else
			prodLine = ConstantDeclarations.PRODUCT_LINE_PRIME_ANEW;
		return prodLine;
	}

	public ArrayList getProdDetailsRegimen(String customerid, String regimenline) {
		String[][] result = null;
		ProductVO[] productVOList = new ProductVO[0];
		ArrayList prodlist = new ArrayList();
		String[][] result2 = null;
		String sql = "";

		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select sv_product_id from customer_product_regimen where sv_customer_id = '" + customerid + "' "
					+ " and sv_product_regimen_line = '" + regimenline + "' ";
			result2 = dbo.executeQuery(sql);

			result = dbo.executeQuery(dba.getProductRegimenCustomer(customerid, regimenline));

			for (int j = 0; j < result2.length; j++) {
				for (int i = 0; i < result.length; i++) {
					if (result2[j][0].equals(result[i][0])) {
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
						pr.setProductRegimenFlag(result[i][12]);
						prodlist.add(pr);
						break;
					}
				}
			}
		} catch (Exception ex1) {
			ex1.printStackTrace();
		} finally {
			dbo.closeConnection();
		}

		return prodlist;
	}

	public Hashtable getMasterDataOne() {
		long start = 0L;
		long end = 0L;
		
		start = System.currentTimeMillis();
		
		String[][] result = null;
		String sql = "";
		Hashtable master = new Hashtable();
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select * from svb_view1 ";
			result = dbo.executeQuery(sql);

			for (int j = 0; j < result.length; j++) {
				master.put(result[j][0], result[j][1]);
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			dbo.closeConnection();
		}
		end = System.currentTimeMillis();
		System.out.println("TOTAL SECONDS TAKEN BY getMasterDataOne :: " + (end-start)/1000 + " Sec + " + (end-start) + " ms" );
		return master;
	}

	public Hashtable getMasterDataTwo() {
		String[][] result = null;
		String sql = "";
		Hashtable master = new Hashtable();
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select * from svb_view2 ";
			result = dbo.executeQuery(sql);

			for (int j = 0; j < result.length; j++) {
				master.put(result[j][0], result[j][1]);
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			dbo.closeConnection();
		}
		return master;
	}

	public Hashtable getBrandDetails() {
		String[][] result = null;
		String sql = "";
		Hashtable brands = new Hashtable();
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select * from sv_brand_description ";
			result = dbo.executeQuery(sql);

			for (int j = 0; j < result.length; j++) {
				String brandid = "";
				String branddesc = "";
				String brandlogo = "";
				brandid = result[j][0].trim();
				branddesc = result[j][1].trim();
				brandlogo = result[j][2].trim();
				BrandVO brand = new BrandVO();
				brand.setBrandId(brandid);
				brand.setBrandDescription(branddesc);
				brand.setBrandLogoFile(brandlogo);
				// System.out.println("Brand "+brandid+" "+branddesc+" "+brandlogo) ;
				brands.put(result[j][0], brand);
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			dbo.closeConnection();
		}
		// System.out.println("Brand size..."+brands.size()) ;
		return brands;
	}

	public ArrayList getDynamicText(Hashtable prodhash, CustomerProfileVO cp, Hashtable master1) {
//		 System.out.println("I am in getdynamictext ") ;
		String[][] result = null;
		ArrayList dynamic = new ArrayList();
		String sql = "";
		String tableid = "";
		String ingrgroup = "";
		String ingrid = "";
		String ingrname = "";
		String attribvalues = "";
		String grouptext = "";
		String attribdesc = "";
		String warnings = "";
		String commontext = "";
		String prodname = "";
		boolean dryskinflag = false;
		boolean sensitiveflag = false;
		boolean hyperflag = false;
		boolean rosaceaflag = false;
		boolean dynamicflag = false;
		boolean ethnicityflag = false;
		ArrayList prodAHA = new ArrayList();
		ArrayList prodBHA = new ArrayList();
		ArrayList prodHQ = new ArrayList();
		ArrayList prodVIT = new ArrayList();
		ArrayList prodAZ = new ArrayList();
		ArrayList prodRET = new ArrayList();
		ArrayList prodAFR = new ArrayList();
		ArrayList AHA = new ArrayList();
		ArrayList BHA = new ArrayList();
		ArrayList HQ = new ArrayList();
		ArrayList VIT = new ArrayList();
		ArrayList AZ = new ArrayList();
		ArrayList RET = new ArrayList();
		ArrayList AFR = new ArrayList();

		String[] txtAHA = new String[3];
		String[] txtBHA = new String[3];
		String[] txtHQ = new String[3];
		String[] txtVIT = new String[3];
		String[] txtAZ = new String[3];
		String[] txtRET = new String[3];
		String[] txtAFR = new String[3];

		String cSkinType = cp.getSkinType();
		String cEthnicity = cp.getEthnicity();

		ArrayList custSkinConcernList = cp.getSkinConcernList();
		ArrayList custSkinConditionList = cp.getSkinConditionList();
		ArrayList concerns = new ArrayList();

		if (cSkinType.equals("T001")) {
			dryskinflag = true;
			concerns.add(cSkinType);
		}
		// System.out.println("cust skin condition list "+custSkinConditionList.size());
		for (int m = 0; m < custSkinConditionList.size(); m++) {

			if (custSkinConditionList.get(m).equals("D001")) {
				concerns.add(custSkinConditionList.get(m));
				sensitiveflag = true;
			}
			if (custSkinConditionList.get(m).equals("D003")) {
				concerns.add(custSkinConditionList.get(m));
				rosaceaflag = true;
			}
			if (custSkinConditionList.get(m).equals("D006")) {
				concerns.add(custSkinConditionList.get(m));
				hyperflag = true;
			}
		}
		// System.out.println("concerns 1 size "+concerns.size());
		if (cEthnicity.equals("N001")) {
			ethnicityflag = true;
			dynamicflag = true;
		}

		if (dryskinflag || ethnicityflag || sensitiveflag || rosaceaflag || hyperflag) {
			dynamicflag = true;

			try {
				dbo = new SVDBOperation();
				dbo.createConnection();
				sql = " select * from sv_ingredients_warnings ";
				result = dbo.executeQuery(sql);
				// System.out.println("I am before for "+result.length) ;
			} catch (Exception e) {
				System.out.println(e);
			} finally {
				dbo.closeConnection();
			}

			for (int j = 0; j < result.length; j++) {
				tableid = result[j][0].trim();
				ingrgroup = result[j][1].trim();
				ingrid = result[j][2].trim();
				grouptext = result[j][3].trim();
				attribvalues = result[j][4].trim();
				warnings = result[j][5].trim();
				//commontext = result[j][6].trim();

				if (ingrgroup.equals("AHA")) {
					AHA.add(ingrid);
					txtAHA = new String[3];
					txtAHA[0] = grouptext;
					txtAHA[1] = warnings;
					//txtAHA[2] = commontext;
				}
				if (ingrgroup.equals("BHA")) {
					BHA.add(ingrid);
					txtBHA = new String[3];
					txtBHA[0] = grouptext;
					txtBHA[1] = warnings;
					//txtBHA[2] = commontext;
				}
				if (ingrgroup.equals("HQ")) {
					HQ.add(ingrid);
					txtHQ = new String[3];
					txtHQ[0] = grouptext;
					txtHQ[1] = warnings;
					//txtHQ[2] = commontext;
				}
				if (ingrgroup.equals("VIT")) {
					VIT.add(ingrid);
					txtVIT = new String[3];
					txtVIT[0] = grouptext;
					txtVIT[1] = warnings;
					//txtVIT[2] = commontext;
				}
				if (ingrgroup.equals("AZ")) {
					AZ.add(ingrid);
					txtAZ = new String[3];
					txtAZ[0] = grouptext;
					txtAZ[1] = warnings;
					//txtAZ[2] = commontext;
				}
				if (ingrgroup.equals("RET")) {
					RET.add(ingrid);
					txtRET = new String[3];
					txtRET[0] = grouptext;
					txtRET[1] = warnings;
					//txtRET[2] = commontext;
				}
				if (ingrgroup.equals("AFR")) {
					AFR.add(ingrid);
					txtAFR = new String[3];
					txtAFR[0] = grouptext;
					txtAFR[1] = warnings;
					//txtAFR[2] = commontext;
				}
			}
		}

		if (dynamicflag) {
			// System.out.println("I am in dynamic flag ") ;
			Enumeration en = prodhash.keys();

			Hashtable master = getMasterDataTwo();

			while (en.hasMoreElements()) {
				// System.out.println("I am in while ") ;
				String prodid = (String) en.nextElement();
				ProductVO prod = new ProductVO();
				try {
					// System.out.println("I am in try ") ;
					prod = (ProductVO) prodhash.get(prodid);
					// System.out.println("I am in try 2 "+prod) ;
					prodname = (String) prod.getProdName();
					// System.out.println("I am in try 3 "+prodname) ;
					String[][] ingrlist = (String[][]) prod.getIngredients();
					// System.out.println("I am in prod "+prodid+ " " +prodname+" "+ingrlist.length)
					// ;

					if (ingrlist != null)
						for (int n = 0; n < ingrlist.length; n++) {
							String ingr = ingrlist[n][0];
							// System.out.println(prodid+" "+ingr) ;
							if (AHA.contains(ingr)) {
								// System.out.println("I am in AHA ") ;
								if (ethnicityflag)
									prodAFR.add(ingr);
								else
									prodAHA.add(ingr);
							}
							if (BHA.contains(ingr)) {
								// System.out.println("I am in BHA ") ;
								if (ethnicityflag)
									prodAFR.add(ingr);
								else
									prodBHA.add(ingr);
							}
							if (HQ.contains(ingr)) {
								// System.out.println("I am in HQ ") ;
								if (ethnicityflag)
									prodAFR.add(ingr);
								else
									prodHQ.add(ingr);
							}
							if (VIT.contains(ingr)) {
								// System.out.println("I am in VIT ") ;
								if (ethnicityflag)
									prodAFR.add(ingr);
								else
									prodVIT.add(ingr);
							}
							if (AZ.contains(ingr)) {
								// System.out.println("I am in AZ ") ;
								if (ethnicityflag)
									prodAFR.add(ingr);
								else
									prodAZ.add(ingr);
							}
							if (RET.contains(ingr)) {
								// System.out.println("I am in RET ") ;
								if (ethnicityflag)
									prodAFR.add(ingr);
								else
									prodRET.add(ingr);
							}
							if (AFR.contains(ingr)) {
								// System.out.println("I am in AFR ") ;
								prodAFR.add(ingr);
							}
						} // if ingr list
				} catch (Exception exp) // try
				{
					// exp.printStackTrace() ;
				}
			} // while enum
				// boolean repeat = false ;
				// System.out.println("HQ "+prodHQ.size()) ;
				// System.out.println("AHA "+prodAHA.size()) ;
				// System.out.println("BHA "+prodBHA.size()) ;
				// System.out.println("VIT "+prodVIT.size()) ;
				// System.out.println("AZ "+prodAZ.size()) ;
				// System.out.println("RET "+prodRET.size()) ;
			if (prodAFR != null && prodAFR.size() > 0) {
				// System.out.println("I am in AFR 2 ") ;
				/*
				 * if (ethnicityflag) { dynamic.add(txtAFR[1]) ; dynamic.add(txtAFR[2]) ; }
				 */
				String wtext = getText(prodAFR, master, txtAFR, concerns, false, true, master1);
				dynamic.add(wtext);
				dynamic.add(txtAFR[2]);
				// repeat = true ;
			}
			if (prodHQ != null && prodHQ.size() > 0 && hyperflag) {
				// System.out.println("I am in HQ 2 ") ;
				String wtext = getText(prodHQ, master, txtHQ, concerns, hyperflag, false, master1);
				dynamic.add(wtext);
				dynamic.add(txtHQ[2]);
				// repeat = true ;
			}
			if (prodAHA != null && prodAHA.size() > 0) {
				// System.out.println("I am in AHA 2 ") ;
				String wtext = getText(prodAHA, master, txtAHA, concerns, false, false, master1);
				dynamic.add(wtext);
				dynamic.add(txtAHA[2]);
				// repeat = true ;
			}
			if (prodBHA != null && prodBHA.size() > 0) {
				// System.out.println("I am in BHA 2 ") ;
				String wtext = getText(prodBHA, master, txtBHA, concerns, false, false, master1);
				dynamic.add(wtext);
				dynamic.add(txtBHA[2]);
				// repeat = true ;
			}
			if (prodVIT != null && prodVIT.size() > 0) {
				// System.out.println("I am in VIT 2 ") ;
				String wtext = getText(prodVIT, master, txtVIT, concerns, false, false, master1);
				dynamic.add(wtext);
				dynamic.add(txtVIT[2]);
				// repeat = true ;
			}
			if (prodAZ != null && prodAZ.size() > 0) {
				// System.out.println("I am in AZ 2 ") ;
				String wtext = getText(prodAZ, master, txtAZ, concerns, false, false, master1);
				dynamic.add(wtext);
				dynamic.add(txtAZ[2]);
				// repeat = true ;
			}
			if (prodRET != null && prodRET.size() > 0) {
				// System.out.println("I am in RET 2 ") ;
				String wtext = getText(prodRET, master, txtRET, concerns, false, false, master1);
				dynamic.add(wtext);
				dynamic.add(txtRET[2]);
				// repeat = true ;
			}
		} // if dynamic

		// System.out.println("dynamic "+dynamic.size()) ;
		return dynamic;
	}

	public ArrayList getDynamicTextNew(Hashtable prodhash, CustomerProfileVO cp, Hashtable master1,Hashtable master) {
//		 System.out.println("I am in getdynamictext ") ;
		String[][] result = null;
		ArrayList dynamic = new ArrayList();
		String sql = "";
		String tableid = "";
		String ingrgroup = "";
		String ingrid = "";
		String ingrname = "";
		String attribvalues = "";
		String grouptext = "";
		String attribdesc = "";
		String warnings = "";
		String commontext = "";
		String prodname = "";
		boolean dryskinflag = false;
		boolean sensitiveflag = false;
		boolean hyperflag = false;
		boolean rosaceaflag = false;
		boolean dynamicflag = false;
		boolean ethnicityflag = false;
		ArrayList prodAHA = new ArrayList();
		ArrayList prodBHA = new ArrayList();
		ArrayList prodHQ = new ArrayList();
		ArrayList prodVIT = new ArrayList();
		ArrayList prodAZ = new ArrayList();
		ArrayList prodRET = new ArrayList();
		ArrayList prodAFR = new ArrayList();
		ArrayList AHA = new ArrayList();
		ArrayList BHA = new ArrayList();
		ArrayList HQ = new ArrayList();
		ArrayList VIT = new ArrayList();
		ArrayList AZ = new ArrayList();
		ArrayList RET = new ArrayList();
		ArrayList AFR = new ArrayList();

		String[] txtAHA = new String[3];
		String[] txtBHA = new String[3];
		String[] txtHQ = new String[3];
		String[] txtVIT = new String[3];
		String[] txtAZ = new String[3];
		String[] txtRET = new String[3];
		String[] txtAFR = new String[3];

		String cSkinType = cp.getSkinType();
		String cEthnicity = cp.getEthnicity();

		ArrayList custSkinConcernList = cp.getSkinConcernList();
		ArrayList custSkinConditionList = cp.getSkinConditionList();
		ArrayList concerns = new ArrayList();

		if (cSkinType.equals("T001")) {
			dryskinflag = true;
			concerns.add(cSkinType);
		}
		// System.out.println("cust skin condition list "+custSkinConditionList.size());
		for (int m = 0; m < custSkinConditionList.size(); m++) {

			if (custSkinConditionList.get(m).equals("D001")) {
				concerns.add(custSkinConditionList.get(m));
				sensitiveflag = true;
			}
			if (custSkinConditionList.get(m).equals("D003")) {
				concerns.add(custSkinConditionList.get(m));
				rosaceaflag = true;
			}
			if (custSkinConditionList.get(m).equals("D006")) {
				concerns.add(custSkinConditionList.get(m));
				hyperflag = true;
			}
		}
		// System.out.println("concerns 1 size "+concerns.size());
		if (cEthnicity.equals("N001")) {
			ethnicityflag = true;
			dynamicflag = true;
		}

		if (dryskinflag || ethnicityflag || sensitiveflag || rosaceaflag || hyperflag) {
			dynamicflag = true;

			try {
				dbo = new SVDBOperation();
				dbo.createConnection();
				sql = " select * from sv_ingredients_warnings ";
				result = dbo.executeQuery(sql);
				// System.out.println("I am before for "+result.length) ;
			} catch (Exception e) {
				System.out.println(e);
			} finally {
				dbo.closeConnection();
			}

			for (int j = 0; j < result.length; j++) {
				tableid = result[j][0].trim();
				ingrgroup = result[j][1].trim();
				ingrid = result[j][2].trim();
				grouptext = result[j][3].trim();
				attribvalues = result[j][4].trim();
				warnings = result[j][5].trim();
				//commontext = result[j][6].trim();

				if (ingrgroup.equals("AHA")) {
					AHA.add(ingrid);
					txtAHA = new String[3];
					txtAHA[0] = grouptext;
					txtAHA[1] = warnings;
					//txtAHA[2] = commontext;
				}
				if (ingrgroup.equals("BHA")) {
					BHA.add(ingrid);
					txtBHA = new String[3];
					txtBHA[0] = grouptext;
					txtBHA[1] = warnings;
					//txtBHA[2] = commontext;
				}
				if (ingrgroup.equals("HQ")) {
					HQ.add(ingrid);
					txtHQ = new String[3];
					txtHQ[0] = grouptext;
					txtHQ[1] = warnings;
					//txtHQ[2] = commontext;
				}
				if (ingrgroup.equals("VIT")) {
					VIT.add(ingrid);
					txtVIT = new String[3];
					txtVIT[0] = grouptext;
					txtVIT[1] = warnings;
					//txtVIT[2] = commontext;
				}
				if (ingrgroup.equals("AZ")) {
					AZ.add(ingrid);
					txtAZ = new String[3];
					txtAZ[0] = grouptext;
					txtAZ[1] = warnings;
					//txtAZ[2] = commontext;
				}
				if (ingrgroup.equals("RET")) {
					RET.add(ingrid);
					txtRET = new String[3];
					txtRET[0] = grouptext;
					txtRET[1] = warnings;
					//txtRET[2] = commontext;
				}
				if (ingrgroup.equals("AFR")) {
					AFR.add(ingrid);
					txtAFR = new String[3];
					txtAFR[0] = grouptext;
					txtAFR[1] = warnings;
					//txtAFR[2] = commontext;
				}
			}
		}

		if (dynamicflag) {
			// System.out.println("I am in dynamic flag ") ;
			Enumeration en = prodhash.keys();

			while (en.hasMoreElements()) {
				// System.out.println("I am in while ") ;
				String prodid = (String) en.nextElement();
				ProductVO prod = new ProductVO();
				try {
					// System.out.println("I am in try ") ;
					prod = (ProductVO) prodhash.get(prodid);
					// System.out.println("I am in try 2 "+prod) ;
					prodname = (String) prod.getProdName();
					// System.out.println("I am in try 3 "+prodname) ;
					String[][] ingrlist = (String[][]) prod.getIngredients();
					// System.out.println("I am in prod "+prodid+ " " +prodname+" "+ingrlist.length)
					// ;

					if (ingrlist != null)
						for (int n = 0; n < ingrlist.length; n++) {
							String ingr = ingrlist[n][0];
							// System.out.println(prodid+" "+ingr) ;
							if (AHA.contains(ingr)) {
								// System.out.println("I am in AHA ") ;
								if (ethnicityflag)
									prodAFR.add(ingr);
								else
									prodAHA.add(ingr);
							}
							if (BHA.contains(ingr)) {
								// System.out.println("I am in BHA ") ;
								if (ethnicityflag)
									prodAFR.add(ingr);
								else
									prodBHA.add(ingr);
							}
							if (HQ.contains(ingr)) {
								// System.out.println("I am in HQ ") ;
								if (ethnicityflag)
									prodAFR.add(ingr);
								else
									prodHQ.add(ingr);
							}
							if (VIT.contains(ingr)) {
								// System.out.println("I am in VIT ") ;
								if (ethnicityflag)
									prodAFR.add(ingr);
								else
									prodVIT.add(ingr);
							}
							if (AZ.contains(ingr)) {
								// System.out.println("I am in AZ ") ;
								if (ethnicityflag)
									prodAFR.add(ingr);
								else
									prodAZ.add(ingr);
							}
							if (RET.contains(ingr)) {
								// System.out.println("I am in RET ") ;
								if (ethnicityflag)
									prodAFR.add(ingr);
								else
									prodRET.add(ingr);
							}
							if (AFR.contains(ingr)) {
								// System.out.println("I am in AFR ") ;
								prodAFR.add(ingr);
							}
						} // if ingr list
				} catch (Exception exp) // try
				{
					// exp.printStackTrace() ;
				}
			} // while enum
				// boolean repeat = false ;
				// System.out.println("HQ "+prodHQ.size()) ;
				// System.out.println("AHA "+prodAHA.size()) ;
				// System.out.println("BHA "+prodBHA.size()) ;
				// System.out.println("VIT "+prodVIT.size()) ;
				// System.out.println("AZ "+prodAZ.size()) ;
				// System.out.println("RET "+prodRET.size()) ;
			if (prodAFR != null && prodAFR.size() > 0) {
				// System.out.println("I am in AFR 2 ") ;
				/*
				 * if (ethnicityflag) { dynamic.add(txtAFR[1]) ; dynamic.add(txtAFR[2]) ; }
				 */
				String wtext = getText(prodAFR, master, txtAFR, concerns, false, true, master1);
				dynamic.add(wtext);
				dynamic.add(txtAFR[2]);
				// repeat = true ;
			}
			if (prodHQ != null && prodHQ.size() > 0 && hyperflag) {
				// System.out.println("I am in HQ 2 ") ;
				String wtext = getText(prodHQ, master, txtHQ, concerns, hyperflag, false, master1);
				dynamic.add(wtext);
				dynamic.add(txtHQ[2]);
				// repeat = true ;
			}
			if (prodAHA != null && prodAHA.size() > 0) {
				// System.out.println("I am in AHA 2 ") ;
				String wtext = getText(prodAHA, master, txtAHA, concerns, false, false, master1);
				dynamic.add(wtext);
				dynamic.add(txtAHA[2]);
				// repeat = true ;
			}
			if (prodBHA != null && prodBHA.size() > 0) {
				// System.out.println("I am in BHA 2 ") ;
				String wtext = getText(prodBHA, master, txtBHA, concerns, false, false, master1);
				dynamic.add(wtext);
				dynamic.add(txtBHA[2]);
				// repeat = true ;
			}
			if (prodVIT != null && prodVIT.size() > 0) {
				// System.out.println("I am in VIT 2 ") ;
				String wtext = getText(prodVIT, master, txtVIT, concerns, false, false, master1);
				dynamic.add(wtext);
				dynamic.add(txtVIT[2]);
				// repeat = true ;
			}
			if (prodAZ != null && prodAZ.size() > 0) {
				// System.out.println("I am in AZ 2 ") ;
				String wtext = getText(prodAZ, master, txtAZ, concerns, false, false, master1);
				dynamic.add(wtext);
				dynamic.add(txtAZ[2]);
				// repeat = true ;
			}
			if (prodRET != null && prodRET.size() > 0) {
				// System.out.println("I am in RET 2 ") ;
				String wtext = getText(prodRET, master, txtRET, concerns, false, false, master1);
				dynamic.add(wtext);
				dynamic.add(txtRET[2]);
				// repeat = true ;
			}
		} // if dynamic

		// System.out.println("dynamic "+dynamic.size()) ;
		return dynamic;
	}
	
	
	public String getText(ArrayList prod, Hashtable master, String[] txt, ArrayList concerns, boolean hyperflag,
			boolean ethnicityflag, Hashtable master1) {
		// System.out.println("I am in gettext ") ;
		String textM = "";
		String text1 = "";
		String text2 = "";
		text1 = txt[0];
		for (int i = 0; i < prod.size(); i++) {
			String ingrname = (String) master.get((String) prod.get(i));
			if (i == 0)
				//text1 = text1 + " <strong>" + ingrname.toLowerCase() + "</strong>";
				text1 = text1 + " " + ingrname.toLowerCase() + " ";
			else if (i == prod.size() - 1) {
				text1 = text1 + " and " + ingrname.toLowerCase();
			} else
				text1 = text1 + ", " + ingrname.toLowerCase();

		}
		text2 = "";
		// System.out.println("text2 concerns "+concerns.size()) ;

		if (!ethnicityflag)
			for (int i = 0; i < concerns.size(); i++) {
				String attribname = (String) master1.get((String) concerns.get(i));
				if (attribname.equals("tight or dry"))
					attribname = "tight or dry skin";
				if (attribname.equals("sensitive skin"))
					attribname = "sensitivity";
				//attribname = "<strong>" + attribname + "</strong>";
				attribname = attribname;
				if (i == 0)
					text2 = text2 + attribname;
				else if (i == concerns.size() - 1) {
					if (concerns.size() == 2)
						text2 = text2 + " with " + attribname;
					else
						text2 = text2 + " and " + attribname;
				} else
					text2 = text2 + ", " + attribname;

			}

		if (!hyperflag)
			textM = text1 + " " + txt[1] + " " + text2 + ".";
		else
			textM = text1 + " " + txt[1];
		// System.out.println("text 1 "+text1) ;
		// System.out.println("txt[1] "+txt[1]) ;
		// System.out.println("text 2 "+text2) ;
		// System.out.println("text M "+textM) ;
		return textM;
	}

	public ArrayList getCustomerReviewStars(ArrayList prodlist) {
		Hashtable creviews = new Hashtable();
		String[][] result = null;
		String sql = "";
		Hashtable brands = new Hashtable();
		String starlogo = "";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select sv_product_id, sv_avg_customer_rating from customer_reviews where sv_avg_customer_rating <> '0'";
			// System.out.println(sql);
			result = dbo.executeQuery(sql);

			for (int j = 0; j < result.length; j++) {
				String prodid = "";
				String avgrating = "";
				prodid = result[j][0].trim();
				avgrating = result[j][1].trim();
				creviews.put(prodid, avgrating);
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			dbo.closeConnection();
		}
		ArrayList cleanse = (ArrayList) prodlist.get(0);
		ArrayList tone = (ArrayList) prodlist.get(1);
		ArrayList exfoliant = (ArrayList) prodlist.get(2);
		ArrayList moisturize = (ArrayList) prodlist.get(3);
		ArrayList nmoisturize = (ArrayList) prodlist.get(4);
		ArrayList treat = (ArrayList) prodlist.get(5);
		ArrayList etreat = (ArrayList) prodlist.get(6);
		ArrayList spf = (ArrayList) prodlist.get(7);
		Hashtable fbrand = (Hashtable) prodlist.get(8);
		ArrayList sunCare = (ArrayList) prodlist.get(9);
		ArrayList primer = (ArrayList) prodlist.get(10);
		// System.out.println("vbbbb 11");
		cleanse = (ArrayList) setStarrating(creviews, cleanse);
		moisturize = (ArrayList) setStarrating(creviews, moisturize);
		treat = (ArrayList) setStarrating(creviews, treat);
		etreat = (ArrayList) setStarrating(creviews, etreat);
		sunCare = (ArrayList) setStarrating(creviews, sunCare);
		primer = (ArrayList) setStarrating(creviews, primer);

		prodlist = new ArrayList();
		prodlist.add(cleanse);
		prodlist.add(tone);
		prodlist.add(exfoliant);
		prodlist.add(moisturize);
		prodlist.add(nmoisturize);
		prodlist.add(treat);
		prodlist.add(etreat);
		prodlist.add(spf);
		prodlist.add(fbrand);
		prodlist.add(sunCare);
		prodlist.add(primer);
		return prodlist;
	}

	public ArrayList setStarrating(Hashtable creviews, ArrayList prodlist) {
		// System.out.println("vbbbb 2332 "+prodlist.size());
		for (int i = 0; i < prodlist.size(); i++) {
			String starlogo = "";
			ProductVO prod = (ProductVO) prodlist.get(i);
			String pid = prod.getProdId();
			String arating = (String) creviews.get(pid);
			if (arating != null) {
				float art = Float.parseFloat(arating);
				if (art == 0.0)
					starlogo = "";
				else if (art == 1.0)
					starlogo = "rating_10_bty.gif";
				else if (art > 1.0 && art <= 1.5)
					starlogo = "rating_15_bty.gif";
				else if (art > 1.5 && art <= 2.0)
					starlogo = "rating_20_bty.gif";
				else if (art > 2.0 && art <= 2.5)
					starlogo = "rating_25_bty.gif";
				else if (art > 2.5 && art <= 3.0)
					starlogo = "rating_30_bty.gif";
				else if (art > 3.0 && art <= 3.5)
					starlogo = "rating_35_bty.gif";
				else if (art > 3.5 && art <= 4.0)
					starlogo = "rating_40_bty.gif";
				else if (art > 4.0 && art <= 4.5)
					starlogo = "rating_45_bty.gif";
				else if (art > 4.5 && art <= 5.0)
					starlogo = "rating_50_bty.gif";
				prod.setShadeImage(starlogo);
			}
			// System.out.println("recommendation "+prod.getProdId()+" "+starlogo) ;
		}
		return prodlist;
	}

	public Hashtable getCustomerReviewStars2(Hashtable prodlist) {
		Hashtable creviews = new Hashtable();
		String[][] result = null;
		String sql = "";
		String starlogo = "";
		try {
			dbo = new SVDBOperation();
			dbo.createConnection();
			sql = " select sv_product_id, sv_avg_customer_rating from customer_reviews where sv_avg_customer_rating <> '0'";
			// System.out.println(sql);
			result = dbo.executeQuery(sql);

			for (int j = 0; j < result.length; j++) {
				String prodid = "";
				String avgrating = "";
				prodid = result[j][0].trim();
				avgrating = result[j][1].trim();
				creviews.put(prodid, avgrating);
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			dbo.closeConnection();
		}
		ArrayList prods = new ArrayList();
		Enumeration eth = prodlist.keys();
		while (eth.hasMoreElements()) {
			String prodid = (String) eth.nextElement();
			ProductVO pr = (ProductVO) prodlist.get(prodid);
			prods.add(pr);
		}
		prods = (ArrayList) setStarrating(creviews, prods);
		for (int i = 0; i < prods.size(); i++) {
			ProductVO pr = (ProductVO) prods.get(i);
			String prid = (String) pr.getProdId();
			prodlist.put(prid, pr);
		}
		return prodlist;
	}
}// end of class
