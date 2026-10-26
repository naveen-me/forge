package com.svb.webservices.model;

import java.util.Arrays;
import java.util.List;

public class NotHaveTheseBadIngredientsListModel {

	private String groupFunctionName;
	private String groupFunctionText;
	private List<IngredientNameModel> ingredients;
	
	public String getGroupFunctionName() {
		return groupFunctionName;
	}
	public void setGroupFunctionName(String groupFunctionName) {
		this.groupFunctionName = groupFunctionName;
	}
	public String getGroupFunctionText() {
		return groupFunctionText;
	}
	public void setGroupFunctionText(String groupFunctionText) {
		this.groupFunctionText = groupFunctionText;
	}
	
	public List<IngredientNameModel> getIngredients() {
		return ingredients;
	}
	public void setIngredients(List<IngredientNameModel> ingredients) {
		this.ingredients = ingredients;
	}
	@Override
	public String toString() {
		return "NotHaveTheseBadIngredientsModel [groupFunctionName=" + groupFunctionName + ", groupFunctionText="
				+ groupFunctionText + ", ingredients=" + ingredients + "]";
	}
	
}
