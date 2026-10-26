package com.svb.webservices.model;

import java.util.Arrays;

public class MoreGreatIngredientsModel {

	private String[] ingredientId;
	private String[] ingredientName;
	private String functionDescription;
	
	public String[] getIngredientId() {
		return ingredientId;
	}
	public void setIngredientId(String[] ingredientId) {
		this.ingredientId = ingredientId;
	}
	public String[] getIngredientName() {
		return ingredientName;
	}
	public void setIngredientName(String[] ingredientName) {
		this.ingredientName = ingredientName;
	}
	public String getFunctionDescription() {
		return functionDescription;
	}
	public void setFunctionDescription(String functionDescription) {
		this.functionDescription = functionDescription;
	}
	
	@Override
	public String toString() {
		return "MoreGreatIngredientsModel [ingredientId=" + Arrays.toString(ingredientId) + ", ingredientName="
				+ Arrays.toString(ingredientName) + ", functionDescription=" + functionDescription + "]";
	}
	
	
}
