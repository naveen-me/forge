package com.svb.webservices.serviceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.svb.analysis.javabeans.PopupRatings;
import com.svb.common.valueobjects.ProductVO;
import com.svb.webservices.model.HeroIngredientModel;
import com.svb.webservices.model.HeroIngredientsListModel;
import com.svb.webservices.model.RecomondationProductModel;

public class HeroIngredientsSection{
	
	private Set<String> skinConcernSet = new HashSet<>();
	private StringBuilder skinTypeHeader = new StringBuilder();
	private StringBuilder skinConcernHeader = new StringBuilder();
	
	/**
	 * This method calculates and returns hero ingredients data for product
	 */
	public HeroIngredientsListModel getHeroIngredients(Hashtable<String,List<String>> ingredient, HashMap ingredientsWithGlossary,Hashtable<String, String> master,RecomondationProductModel productModel, Hashtable<String,String> ingredientsName, Hashtable<String, String> funcingr, Hashtable<String, String[][]> funcnames){
	
		HeroIngredientsListModel heroIngredients = new HeroIngredientsListModel(); 
		List<HeroIngredientModel> heroIngredientsList = new ArrayList<>();
		List<String> skinConcern = Arrays.asList(productModel.getSkin_concern());
		List<String> eyeConcern = Arrays.asList(productModel.getEye_concern()); 
		
		List<List<String>> ingredientList = new ArrayList<>(ingredient.values());
		List<String> ingredientKeyList = new ArrayList<>(ingredient.keySet());
		for(int p=0; p<ingredientList.size();p++) {
			HeroIngredientModel heroIngredientModel = new HeroIngredientModel();
		 	List<String> ingredientScoreList = ingredientList.get(p);
		 	String ingredientKey = ingredientKeyList.get(p);
			Boolean scoreFlagTen = false;
			Boolean scoreFlagNine = false;
			List<String> skinConcernTenList = new ArrayList<>();
			List<String> skinConcernNineList = new ArrayList<>();
			StringTokenizer stkTokenizer = new StringTokenizer(ingredientKey,"-") ;
			String val = stkTokenizer.nextToken();
			String idString = stkTokenizer.nextToken();

			if(!val.equals("01")) {
				
				for(String ingredientScore:ingredientScoreList) {
					StringTokenizer stk = new StringTokenizer(ingredientScore,"-") ;
					stk.nextToken(); 
					stk.nextToken();
					stk.nextToken();
					stk.nextToken();
					stk.nextToken();
					String skin =  String.valueOf(stk.nextToken());
					String scoreString = stk.nextToken();
					if(Integer.parseInt(scoreString) == 10) {
						if(!eyeConcern.contains(skin)) {
							skinConcernTenList.add(skin);
							scoreFlagTen = true;
						}
					}
					else if(Integer.parseInt(scoreString) == 9) {
						if(!eyeConcern.contains(skin)) {
							skinConcernNineList.add(skin);
							scoreFlagNine = true;
						}
					}
				}
				
				if(scoreFlagTen) {
					StringBuilder skinConcernText = getSkinConcernText(skinConcernTenList,productModel,master,skinConcern);		
					heroIngredientModel = getHeroIngredientModel(idString,funcnames,funcingr,ingredientsWithGlossary,skinConcernText,heroIngredientModel,ingredientsName);
					if(heroIngredientModel != null) {
						heroIngredientsList.add(0, heroIngredientModel);
					}
					ingredient.remove(ingredientKey);
				}
				else if(scoreFlagNine) {
					StringBuilder skinConcernText = getSkinConcernText(skinConcernNineList,productModel,master,skinConcern);
					heroIngredientModel = getHeroIngredientModel(idString,funcnames,funcingr,ingredientsWithGlossary,skinConcernText,heroIngredientModel,ingredientsName);
					if(heroIngredientModel != null) {
						heroIngredientsList.add(heroIngredientModel);
					}
					ingredient.remove(ingredientKey);
				}
		  }
		}
		if(!heroIngredientsList.isEmpty() &&heroIngredientsList != null) {
			heroIngredients.setHeaderText(getHeaderText());
			heroIngredients.setHeroIngredientsList(heroIngredientsList);
		}
		return heroIngredients;
	}

	/**
	 * This method returns header text of hero ingredients section for product
	 */
	private String getHeaderText() {
		
		StringBuilder headerText = new StringBuilder();
		headerText.append("For ");
		headerText.append(skinTypeHeader);
		if(skinConcernSet != null && !skinConcernSet.isEmpty()) {

			if(skinTypeHeader.length() == 0) {
				headerText.append("your skin concern of ");
			}
			else {
				headerText.append("and your skin concern of ");
			}		
			int index = skinConcernHeader.lastIndexOf(", ");
			skinConcernHeader.replace(index, index+2, ":");
			if(skinConcernSet.size()>1) {
				index = headerText.lastIndexOf("concern");
				headerText.replace(index,index+7, "concerns");
				index = skinConcernHeader.lastIndexOf(", ");
				skinConcernHeader.replace(index, index+1, " and");
			}
			headerText.append(skinConcernHeader);
		}
		
		return headerText.toString();
	}

	/**
	 * This method calculates and returns data of heroingredient model
	 */
	private HeroIngredientModel getHeroIngredientModel(String idString,
			Hashtable<String,String[][]> funcnames, Hashtable<String,String> funcingr, HashMap ingredientsWithGlossary, StringBuilder skinConcernText,
			HeroIngredientModel heroIngredientModel, Hashtable<String,String> ingredientsNameList) {
		
		    heroIngredientModel.setIngredientId(idString);
		    String[][] funcdata = funcnames.get(funcingr.get(idString));
			String funcname = "<strong>" + " " + funcdata[0][0].trim()+ " " + funcdata[0][1] + "</strong>, ";
		    String ingredientName = ingredientsNameList.get(idString).toString().trim();
			heroIngredientModel.setIngredientName(ingredientName);
			if (ingredientsWithGlossary.get(ingredientName.toUpperCase()) != null) {
				heroIngredientModel.setIngredientDescription(funcname + skinConcernText +(String)ingredientsWithGlossary.get(ingredientName.toUpperCase()));
			} else {
				heroIngredientModel.setIngredientDescription(funcname + skinConcernText + "");
			}
		     			
		return heroIngredientModel;
	}

	/**
	 * This method calculates and returns concern text for score 9 & 10 ingredients
	 */
	private StringBuilder getSkinConcernText(List<String> skinConcernList, RecomondationProductModel productModel,
		Hashtable<String, String> master, List<String> skinConcern) {
	
		StringBuilder skinConcernText = new StringBuilder("excellent for ");
		String skinType = new String();
		String skinConcernString = new String();
		
		if(skinConcernList.contains(productModel.getSkin_type())) {
			skinType = master.get(productModel.getSkin_type()).toLowerCase().toString();
			skinConcernText.append("your " + skinType +" type");
			if(skinTypeHeader.indexOf(skinType) == -1) {					
				skinTypeHeader.append("your " + skinType + " type ");
			}
			skinConcernList.remove(productModel.getSkin_type());
			if(!skinConcernList.isEmpty() && skinConcernList != null) {
				skinConcernText.append(" with concern of ");
			}
			else {
				skinConcernText.append(". ");
				return skinConcernText;
			}
		}
		
		for(String i:skinConcernList) {
			skinConcernString = master.get(i).toLowerCase();
			skinConcernText.append(skinConcernString + ", ");
			if(skinConcernHeader.indexOf(skinConcernString) == -1){
				skinConcernHeader.append(skinConcernString + ", ");
			}
			skinConcernSet.add(skinConcernString);
		}
		
		int index = skinConcernText.lastIndexOf(", ");
		skinConcernText.replace(index, index+2, ". ");
		
		if(skinConcernList.size()>1) {
			if(skinConcernText.indexOf("concern") != -1) {
				index = skinConcernText.indexOf("concern");
				skinConcernText.replace(index,index+7, "concerns");
			}
			index = skinConcernText.lastIndexOf(", ");
			skinConcernText.replace(index, index+1, " and");
		}
				
		return skinConcernText;
    }
}
