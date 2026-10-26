package com.svb.webservices.model;

public class IngredientNameModel {

	private String ingredientId;
	private String ingredientName;
	
	public String getIngredientId() {
		return ingredientId;
	}
	public void setIngredientId(String ingredientId) {
		this.ingredientId = ingredientId;
	}
	public String getIngredientName() {
		return ingredientName;
	}
	public void setIngredientName(String ingredientName) {
		this.ingredientName = ingredientName;
	}
	
	@Override
	public String toString() {
		return "IngredientNameModel [ingredientId=" + ingredientId + ", ingredientName=" + ingredientName + "]";
	}
	
}
