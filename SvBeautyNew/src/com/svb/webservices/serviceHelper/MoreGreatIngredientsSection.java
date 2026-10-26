package com.svb.webservices.serviceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.svb.analysis.javabeans.PopupRatings;
import com.svb.common.ConstantDeclarations;
import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.common.valueobjects.ProductVO;
import com.svb.webservices.model.FunctionDefinitionsWithGenericText;
import com.svb.webservices.model.MoreGreatFunctionGenericTextsDTO;
import com.svb.webservices.model.MoreGreatIngredientsModel;
import com.svb.webservices.serviceImpl.ProductRecommend;


public class MoreGreatIngredientsSection {
	
	private static Log log = LogFactory.getLog(MoreGreatIngredientsSection.class);

	boolean scoreFlagEight;
	boolean scoreFlagSeven;
	
	/**
	 * This method calculates and returns more great ingredients data for product
	 * @param functext 
	 */
	public List<MoreGreatIngredientsModel> getMoreGreatIngredients(Hashtable<String,String> ingredientsName,Hashtable<String,List<String>> ingredient, Hashtable<String, String> funcingr, Hashtable<String, String[][]> funcnames, Hashtable<String, String> functext,
			MoreGreatFunctionGenericTextsDTO dto) { // Added dto by Shivam For More Great change - 27/09/2023
		List<MoreGreatIngredientsModel> moreGreatIngredients = new ArrayList<>();
		Hashtable<String,List<String>> funcattributeingr = new Hashtable<>();
		
		
		List<List<String>> ingredientList = new ArrayList<>(ingredient.values());
		List<String> ingredientKeyList = new ArrayList<>(ingredient.keySet());
		for(int p=0; p<ingredientList.size();p++) {
			List<String> ingredientScoreList =  ingredientList.get(p);
			String ingredientKey = ingredientKeyList.get(p);
			StringTokenizer stkTokenizer = new StringTokenizer(ingredientKey,"-") ;
			String val = stkTokenizer.nextToken();
			String idString = stkTokenizer.nextToken();

			if(!val.equals("01")) {
				scoreFlagEight = false;
				scoreFlagSeven = false;
				
				funcattributeingr = getIngredientOfScoreEight(ingredientScoreList,funcattributeingr,funcingr);
			
				if(!scoreFlagEight) {				
					funcattributeingr = getIngredientOfScoreSeven(ingredientScoreList,funcattributeingr,funcingr);			
				}
				if(!scoreFlagEight && !scoreFlagSeven) {	
					funcattributeingr = getIngredientOfScoreSix(ingredientScoreList,funcattributeingr,funcingr);
				}
			}
		}
		
		ArrayList<FunctionDefinitionsWithGenericText> definitionsWithGenericTexts = (ArrayList<FunctionDefinitionsWithGenericText>) dto.getPopRate().getFunctionDefinitionsWithGenericText(dto.getProductType(), dto.getUserSkinType(), funcattributeingr.keySet());
		//Added (definitionsWithGenericTexts, concernOrConditionList) parameters in getMoreGreatIngredients() method - Shivam - 21/09/2023
		moreGreatIngredients = getMoreGreatIngredientsList(moreGreatIngredients,funcattributeingr,ingredientsName,funcnames,functext, definitionsWithGenericTexts, dto.getConcernOrConditionList());
		return moreGreatIngredients;
	}

	/**
	 * This method check any ingredient has score 8 for any body attribute
	 */
	private Hashtable<String, List<String>> getIngredientOfScoreEight(List<String> ingredientScoreList,
			Hashtable<String, List<String>> funcattributeingr, Hashtable<String, String> funcingr) {
		
		String idString = new String();
		for(String ingredientScore:ingredientScoreList) {

			StringTokenizer stk = new StringTokenizer(ingredientScore,"-") ;
			stk.nextToken();
			idString = stk.nextToken();
			stk.nextToken();
			stk.nextToken();
			stk.nextToken();
			String skin =  String.valueOf(stk.nextToken());
			int score = Integer.parseInt(stk.nextToken());
			if(score == 8) {
				if(funcattributeingr.get(funcingr.get(idString)+skin) != null) {
					List<String> tempList = funcattributeingr.get(funcingr.get(idString)+skin);
					tempList.add(idString);
					funcattributeingr.put(funcingr.get(idString)+skin, tempList);
				}
				else {
					List<String> tempList = new ArrayList<>();
					tempList.add(idString);
					funcattributeingr.put(funcingr.get(idString)+skin, tempList);
				}
				scoreFlagEight = true;
				break;
			}
		}
		return funcattributeingr;
	}

	/**
	 * This method check any ingredient has score 7 for any body attribute
	 */
	private Hashtable<String, List<String>> getIngredientOfScoreSeven(List<String> ingredientScoreList,
			Hashtable<String, List<String>> funcattributeingr, Hashtable<String, String> funcingr) {
		
		String idString = new String();
		for(String ingredientScore:ingredientScoreList) {
			StringTokenizer stk = new StringTokenizer(ingredientScore,"-") ;
			stk.nextToken();
			idString = stk.nextToken();
			stk.nextToken();
			stk.nextToken();
			stk.nextToken();
			String skin =  String.valueOf(stk.nextToken());
			int score = Integer.parseInt(stk.nextToken());
			if(score == 7) {
				if(funcattributeingr.get(funcingr.get(idString)+skin) != null) {
					List<String> tempList = funcattributeingr.get(funcingr.get(idString)+skin);
					tempList.add(idString);
					funcattributeingr.put(funcingr.get(idString)+skin, tempList);
				}
				else {
					List<String> tempList = new ArrayList<>();
					tempList.add(idString);
					funcattributeingr.put(funcingr.get(idString)+skin, tempList);
				}
				scoreFlagSeven = true;
				break;
			}
		}
		
		return funcattributeingr;
	}

	/**
	 * This method check any ingredient has score 6 for any body attribute
	 */
	private Hashtable<String, List<String>> getIngredientOfScoreSix(List<String> ingredientScoreList,
			Hashtable<String, List<String>> funcattributeingr, Hashtable<String, String> funcingr) {
		
		String idString = new String();
		for(String ingredientScore:ingredientScoreList) {
			StringTokenizer stk = new StringTokenizer(ingredientScore,"-") ;
			stk.nextToken();
			idString = stk.nextToken();
			stk.nextToken();
			stk.nextToken();
			stk.nextToken();
			String skin =  String.valueOf(stk.nextToken());
			int score = Integer.parseInt(stk.nextToken());
			if(score == 6) {
				if(funcattributeingr.get(funcingr.get(idString)+skin) != null) {
					List<String> tempList = funcattributeingr.get(funcingr.get(idString)+skin);
					tempList.add(idString);
					funcattributeingr.put(funcingr.get(idString)+skin, tempList);
				}
				else {
					List<String> tempList = new ArrayList<>();
					tempList.add(idString);
					funcattributeingr.put(funcingr.get(idString)+skin, tempList);
				}
				break;
			}
		}
		
		return funcattributeingr;
	}

	/**
	 * This method calculates and returns list of more great ingredients for product
	 */
	private List<MoreGreatIngredientsModel> getMoreGreatIngredientsList(
			List<MoreGreatIngredientsModel> moreGreatIngredients, Hashtable<String, List<String>> funcattributeingr, Hashtable<String, String> ingredientsName,
			Hashtable<String, String[][]> funcnames, Hashtable<String, String> functext,
			List<FunctionDefinitionsWithGenericText> definitionsWithGenericTexts, List<String> concernOrConditionList) {
		Enumeration<String> en = funcattributeingr.keys();
		while (en.hasMoreElements()) {
			MoreGreatIngredientsModel moreGreatIngredientsModel = new MoreGreatIngredientsModel();
			String funckey = en.nextElement();
			List<String> ingredients = funcattributeingr.get(funckey);
			String[] ingredientId = new String[ingredients.size()];
			String[] ingredientName = new String[ingredients.size()];
			for (int p = 0; p < ingredients.size(); p++) {
				ingredientId[p] = ingredients.get(p).toString();
				ingredientName[p] = ingredientsName.get(ingredients.get(p)).toString();
			}
			moreGreatIngredientsModel.setIngredientId(ingredientId);
			moreGreatIngredientsModel.setIngredientName(ingredientName);
			String[][] funcdata = (String[][]) funcnames.get(funckey.substring(0, 4));
			String funcname = "";
			if (ingredients.size() > 0) {
				if (ingredients.size() > 1)
					funcname = "<strong>" + " " + funcdata[0][1].toLowerCase() + "s</strong>, ";
				else
					funcname = "<strong>" + " " + funcdata[0][0].trim() + " " + funcdata[0][1].toLowerCase() + "</strong>, ";
//				StringBuilder functiontext = new StringBuilder(functext.get(funckey));
//				if(functiontext.length() != 0) {
//				    functiontext.replace(0, 0, Character.toLowerCase(functiontext.charAt(0)));
//					moreGreatIngredientsModel.setFunctionDescription(funcname + functiontext.toString());
//				}
//				else {
//					funcname = funcname.replace(", ", ".");
//					moreGreatIngredientsModel.setFunctionDescription(funcname + "");
//				}
				//String functiontext = functext.get(funckey); //Commented By Shivam for More Great Ingredients change (New Function Definitions) - 21/09/2023
				String functiontext = getFunctionDefinitionWithGenericText(funckey, definitionsWithGenericTexts, concernOrConditionList); //Added By Shivam for More Great Ingredients change (New Function Definitions) - 21/09/2023
				if(functiontext != null) {
				    functiontext = functiontext.replaceFirst(String.valueOf(functiontext.charAt(0)), String.valueOf(Character.toLowerCase(functiontext.charAt(0))));
					moreGreatIngredientsModel.setFunctionDescription(funcname + functiontext);
				}
				else {
					funcname = funcname.replace(", ", ".");
					moreGreatIngredientsModel.setFunctionDescription(funcname + "");
				}
				
				moreGreatIngredients.add(moreGreatIngredientsModel);
			}
		}
		return moreGreatIngredients;
	}
	
	//Added by Shivam For More Great Ingredients change (New Function Definitions) - 21/09/2023 - Start
	private String getFunctionDefinitionWithGenericText(String ingredientFunctionId, List<FunctionDefinitionsWithGenericText> definitionsWithGenericTexts, List<String> concernOrConditionList) {
		if(!definitionsWithGenericTexts.isEmpty()) {
			Map<String, String> concernOrConditionToFunctionText = definitionsWithGenericTexts.stream()
					.filter(p -> p.getFunctionId().equals(ingredientFunctionId.substring(0,4)))
					.collect(Collectors.toMap(
							FunctionDefinitionsWithGenericText::getConcernOrCondition, FunctionDefinitionsWithGenericText::getFunctionText));
			
			String functionText = (!concernOrConditionToFunctionText.isEmpty())? getCombinationWiseFunctionText(concernOrConditionList, concernOrConditionToFunctionText) : null;
			return functionText;
		}else {
			return null;
		}
		
	}
	
	private String getCombinationWiseFunctionText(List<String> skinList, Map<String, String> concernOrConditionToFunctionText) {
		
		String matchedCombo = "";
		
		for(String availableCombination : concernOrConditionToFunctionText.keySet()) {
			int count = getMatchedCount(skinList, availableCombination);
			
			if(availableCombination.length() == count*4 && availableCombination.length() > matchedCombo.length() ) {
				//Don't consider if any other concern is present in the combo 
				matchedCombo = availableCombination;
				
				
				/*if(availableCombination.length() == matchedCombo.length() && availableCombination.length() == 4
						&& skinList.indexOf(availableCombination) < skinList.indexOf(matchedCombo)) {
					//if matched combo is found for single concern then consider text for the first choice
						matchedCombo = availableCombination;
				}else {
					matchedCombo = availableCombination;
				}*/
				
			}
		}
		return matchedCombo.equals("") ? concernOrConditionToFunctionText.get("ANY") : concernOrConditionToFunctionText.get(matchedCombo) ;
	}
	
	private int getMatchedCount(List<String> skinList, String availableCombination) {
		int count = 0;
		for(int i=0; i<skinList.size(); i++) {
			if(availableCombination.contains(skinList.get(i))){
				count++;
			}
		}
		return count;
	}
	//Added by Shivam For More Great Ingredients change (New Function Definitions) - 21/09/2023 - End
}
