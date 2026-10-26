package com.svb.webservices.serviceHelper;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.svb.analysis.javabeans.PopupRatings;
import com.svb.struts.bean.ProductDetailsBean;
import com.svb.webservices.model.CleanScoreModel;
import com.svb.webservices.model.DoesContainModel;
import com.svb.webservices.model.IngredientNameModel;

public class CleanScoreSection {

	/**
	 * This method calculates and returns clean score section data for product
	 */
	public CleanScoreModel getCleanScore(ProductDetailsBean ingredientDetail, PopupRatings popRate, Hashtable<String, String> ingredientsName) {
		
		CleanScoreModel cleanScore = new CleanScoreModel();	
		TreeMap<String, List<String>> nonCleanGroupIngredientIds = new TreeMap<String, List<String>>();
		Hashtable<String,List<IngredientNameModel>> nonCleanGroup = popRate.getNonCleanGroupIngredients(nonCleanGroupIngredientIds);
		TreeMap<String,DoesContainModel> nonCleanGroupDetail = popRate.getNonCleanGroupAllDetails(nonCleanGroup);
		List<String> ingredientIds = ingredientDetail.getIngredientIdList();
   
	    List<List<IngredientNameModel>> nonCleanGroupList = new ArrayList<>();
	    List<List<String>> nonCleanGroupsList = getNonCleanGroupsList(nonCleanGroupIngredientIds,nonCleanGroupList);

	    
		nonCleanGroupList = getNonCleanGroupList(nonCleanGroupList,ingredientIds,nonCleanGroupsList,ingredientsName);
		cleanScore = getCleanScoreModel(nonCleanGroupDetail,nonCleanGroupList,cleanScore);

		return cleanScore;
	}

	/**
	 * This method calculates and returns clean score,list of does not contain ingredients and list of does contain ingredients for product
	 */
	private CleanScoreModel getCleanScoreModel(TreeMap<String, DoesContainModel> nonCleanGroupDetail,
			List<List<IngredientNameModel>> nonCleanGroupList, CleanScoreModel cleanScore) {

		int count = 0;
		List<DoesContainModel> doesnotContain = new ArrayList<>();
		List<DoesContainModel> doesContain = new ArrayList<>();
		DoesContainModel doesContainModel = new DoesContainModel();
		Set<String> set2 = nonCleanGroupDetail.keySet();
		int temp=0;
		for(String key:set2) {
			if(nonCleanGroupList.get(temp) != null && !nonCleanGroupList.get(temp).isEmpty()) {
				doesContainModel = nonCleanGroupDetail.get(key);
				doesContainModel.setIngredients(nonCleanGroupList.get(temp));
				doesContain.add(doesContainModel);
				count++;
			}
			else {
				doesnotContain.add(nonCleanGroupDetail.get(key));
			}
			temp++;
		}
		
		int result = 100 - (count*100/9);
		String score = result + "%";
		
		cleanScore.setScore(score);
		cleanScore.setDoesContain(doesContain);
		cleanScore.setDoesNotContain(doesnotContain);
		return cleanScore;
	}

	/**
	 * This method calculates and returns list of ingredients for particular non clean group
	 */
	private List<List<IngredientNameModel>> getNonCleanGroupList(List<List<IngredientNameModel>> nonCleanGroupList,
			List<String> ingredientIds, List<List<String>> nonCleanGroupsList, Hashtable<String, String> ingredientsName) {

		List<IngredientNameModel> nonCleanGrp = new ArrayList<>();
		
		for(String ingredientId:ingredientIds) {
			for(int i = 0; i < nonCleanGroupsList.size(); i++) {
				if(nonCleanGroupsList != null && nonCleanGroupsList.get(i) != null 
						&& nonCleanGroupsList.get(i).contains(ingredientId)) {
					IngredientNameModel ingr = new IngredientNameModel();
					ingr.setIngredientId(ingredientId);
					ingr.setIngredientName(ingredientsName.get(ingredientId).toString().trim());
					if(nonCleanGroupList.get(i) != null && !nonCleanGroupList.get(i).isEmpty()) {
						nonCleanGrp = nonCleanGroupList.get(i);
						nonCleanGrp.add(ingr);
						nonCleanGroupList.set(i, nonCleanGrp);
					}
					else {
						nonCleanGrp = new ArrayList<>();
						nonCleanGrp.add(ingr);
						nonCleanGroupList.set(i, nonCleanGrp);
					}
					
				}
			}
		}
		return nonCleanGroupList;
	}

	/**
	 * This method returns list of ingredientIds for non clean groups
	 */
	private List<List<String>> getNonCleanGroupsList(TreeMap<String, List<String>> nonCleanGroupIngredientIds, List<List<IngredientNameModel>> nonCleanGroupList) {


		List<List<String>> nonCleanGroupsList = new ArrayList<>();
		Set<String> set = nonCleanGroupIngredientIds.keySet();
		
		for(String key : set) {
			nonCleanGroupsList.add(nonCleanGroupIngredientIds.get(key));
			List<IngredientNameModel> nonCleanGrpList = null;
			nonCleanGroupList.add(nonCleanGrpList);
			
		}
		
		return nonCleanGroupsList;
	}
}
