package com.svb.webservices.model;

import java.util.Arrays;
import java.util.List;

public class DoesContainModel {

	private String nonCleanGroupName;
	private List<IngredientNameModel> ingredients;
	public String getNonCleanGroupName() {
		return nonCleanGroupName;
	}
	public void setNonCleanGroupName(String nonCleanGroupName) {
		this.nonCleanGroupName = nonCleanGroupName;
	}
	
	
	public List<IngredientNameModel> getIngredients() {
		return ingredients;
	}
	public void setIngredients(List<IngredientNameModel> ingredients) {
		this.ingredients = ingredients;
	}
	@Override
	public String toString() {
		return "DoesContainModel [nonCleanGroupName=" + nonCleanGroupName + ", ingredients=" + ingredients + "]";
	}
	
	
	
}
