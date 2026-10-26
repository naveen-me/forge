package com.svb.webservices.serviceHelper;

import java.util.ArrayList;
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

import com.svb.analysis.javabeans.PopupRatings;
import com.svb.common.valueobjects.CustomerProfileVO;
import com.svb.struts.bean.ProductDetailsBean;
import com.svb.webservices.model.HeroIngredientModel;

public class NotForYourSkinSection {

	/**
	 * This method calculates and returns not for your skin ingredients data for product
	 * @param cautionHashtable 
	 * @param funcingr 
	 * @param funcnames 
	 * @param ingredientGrpFunc 
	 * @param grpfuncnames 
	 */
	public List<HeroIngredientModel> getNotForYourSkin(
			Hashtable<String, List<String>> ingredient, Hashtable<String, String> ingredientsName, Hashtable<String, String> cautionHashtable, List<String> ingredientsIdList, Hashtable<String, String> funcingr, Hashtable<String, String[][]> funcnames, Hashtable<String, String> ingredientGrpFunc, Hashtable<String, String[][]> grpfuncnames, Map<String, String> genericGroupFunctionTexts) {
	
		List<HeroIngredientModel> notForYourSkinList = new ArrayList<>();
		List<List<String>> ingredientList = new ArrayList<>(ingredient.values());
		List<String> ingredientKeyList = new ArrayList<>(ingredient.keySet());
		
		for(int p=0; p<ingredientList.size();p++) {
			
		 	List<String> ingredientScoreList = ingredientList.get(p);
		 	String ingredientKey = ingredientKeyList.get(p);
			List<String> skinList = new ArrayList<>();
			StringTokenizer stkTokenizer = new StringTokenizer(ingredientKey,"-") ;
			String val = stkTokenizer.nextToken();
			String idString = stkTokenizer.nextToken();

			if(!val.equals("01")) {
		
				for(String ingredientScore:ingredientScoreList) {
					StringTokenizer stk = new StringTokenizer(ingredientScore,"-") ;
					stk.nextToken();
					idString = stk.nextToken();
					stk.nextToken();
					stk.nextToken();
					stk.nextToken();
					String skin =  String.valueOf(stk.nextToken());
					Integer score = Integer.parseInt(stk.nextToken());
					//if(score > 0 && score < 4) { 
					//Changed to add ingredients only having 1 & 2 score to Caution Ingredients' list - Shivam - 18/01/2024
					if(score > 0 && score < 3) {
						skinList.add(skin);
					}
				}
			
				if(!skinList.isEmpty() && skinList != null) {
					notForYourSkinList.add(getNotForYourSkinModel(notForYourSkinList,skinList,idString,ingredientsName,cautionHashtable,funcingr,funcnames,ingredientGrpFunc,grpfuncnames, genericGroupFunctionTexts)); //Added genericGroupFunctionTexts by Shivam - 08/09/2023
					ingredient.remove(ingredientKey);
					ingredientsIdList.add(idString);
				}	
			}
		}
		
		return notForYourSkinList;
	}

	/**
	 * This method returns not for your skin ingredient data
	 * @param funcnames 
	 * @param funcingr 
	 * @param grpfuncnames 
	 * @param ingredientGrpFunc 
	 */
	private HeroIngredientModel getNotForYourSkinModel(List<HeroIngredientModel> notForYourSkinList,
			List<String> skinList, String idString, Hashtable<String, String> ingredientsName,
			Hashtable<String, String> cautionHashtable, Hashtable<String, String> funcingr, Hashtable<String, String[][]> funcnames, Hashtable<String, String> ingredientGrpFunc, Hashtable<String, String[][]> grpfuncnames, Map<String, String> genericGroupFunctionTexts) {
		
		HeroIngredientModel notForYourSkinModel = new HeroIngredientModel();
		notForYourSkinModel.setIngredientId(idString);
		notForYourSkinModel.setIngredientName(ingredientsName.get(idString));

		StringBuilder cautionText = new StringBuilder();
		Set<Entry<String, String>> entrySet = cautionHashtable.entrySet();
		
		while(skinList.size() != 0) {
		     String combination = idString;
		     String combinationtemp = idString;
		     Hashtable<String,String> ingrCaution = new Hashtable<>();
		     
		     for(int i=0; i<skinList.size();i++) {
		    	    combination = combinationtemp;
		    	    combination += skinList.get(i);
		    		Hashtable<String,String> ingrCombinationCaution = new Hashtable<>();
		    		for (Entry<String, String> entry : entrySet) {
		    			if ((entry.getKey()).startsWith(combination)) {
		    				ingrCombinationCaution.put(entry.getKey(), entry.getValue());
		    		    }
		            }
		    		if(ingrCombinationCaution.size() == 1) {
		    			cautionText.append(ingrCombinationCaution.get(combination));
		    			removeSkinConcern(combination,skinList);
		    			break;
		    		}
		    		else {
		    			if((ingrCombinationCaution.isEmpty() || ingrCombinationCaution==null) && (ingrCaution.isEmpty() || ingrCaution==null)) {
		    				Matcher m = Pattern.compile("[A-Z]{1}\\d{3}").matcher(combination);
		   	    			 while (m.find()) {
//		   	    			   cautionText.append("Placeholder"+m.group()+",");
		   	    			   skinList.remove(m.group());
		   	    			 }
		    				break;
		    			}
		    			else {
		    				if(!(ingrCombinationCaution.isEmpty() || ingrCombinationCaution==null)) {
		    					combinationtemp = combination;
		    					ingrCaution = new Hashtable<>(ingrCombinationCaution);
		    				}
		    				if((i==skinList.size()-1) && !ingrCaution.isEmpty()) {
		    	    			cautionText.append(ingrCaution.get(combinationtemp));
		    	    			removeSkinConcern(combinationtemp,skinList);
		    	    			break;
		    	    		}
						}
		    		}
		    		
		    	 }
		}
				
		if(cautionText.lastIndexOf(",") != -1) {
			int index = cautionText.lastIndexOf(",");
			cautionText.replace(index, index+1, ".");
		}
		
		if(cautionText.lastIndexOf(",") != -1) {
			int index = cautionText.lastIndexOf(",");
			cautionText.replace(index, index+1, " and ");
		}
		String funcAndGrpfunc = "";
		String[][] funcdata = funcnames.get(funcingr.get(idString));
		if(cautionText.length() == 0) {
			if(ingredientGrpFunc.get(idString) != null) {
				String[][] grpfuncdata = grpfuncnames.get(ingredientGrpFunc.get(idString));
				//Added by Shivam For caution section - 08/09/2023 - Start
				String genericGroupFunctionText = Objects.nonNull(genericGroupFunctionTexts.get(ingredientGrpFunc.get(idString))) 
						? ", "+genericGroupFunctionTexts.get(ingredientGrpFunc.get(idString)) 
						: ".";
				//Added by Shivam For caution section - 08/09/2023 - End
				funcAndGrpfunc = "<strong> " + funcdata[0][0].trim()+ " " + funcdata[0][1].toLowerCase() + "</strong>, is also "
						          + grpfuncdata[0][0].trim() + " " + grpfuncdata[0][1].toLowerCase()+genericGroupFunctionText;
			}
			else {
				funcAndGrpfunc = "<strong> " + funcdata[0][0].trim()+ " " + funcdata[0][1].toLowerCase() + "</strong>.";
			}
			
			cautionText.insert(0, funcAndGrpfunc);
		}else {
			
			if(ingredientGrpFunc.get(idString) != null) {
				String[][] grpfuncdata = grpfuncnames.get(ingredientGrpFunc.get(idString));
				funcAndGrpfunc = "<strong> " + funcdata[0][0].trim()+ " " + funcdata[0][1].toLowerCase() + "</strong>, is also "
						          + grpfuncdata[0][0].trim() + " " + grpfuncdata[0][1].toLowerCase()+" ,";
			}
			else {
				funcAndGrpfunc = "<strong> " + funcdata[0][0].trim()+ " " + funcdata[0][1].toLowerCase() + "</strong>, ";
			}
			
			cautionText.insert(0, funcAndGrpfunc);
		}
		
		
		notForYourSkinModel.setIngredientDescription(cautionText.toString());
		return notForYourSkinModel;
	}


	/**
	 * This method remove skin corcern(body attribute) from skinList
	 */
	private void removeSkinConcern(String combination, List<String> skinList) {
		Matcher m = Pattern.compile("[A-Z]{1}\\d{3}").matcher(combination);
		 while (m.find()) {
		   skinList.remove(m.group());
		 }
	}
	
}
