package com.svb.webservices.model;

import java.io.Serializable;

public class ProductNotRecommededModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2325760292051550973L;
	private String ingredientId;
	private String ingredientName;
	private String functionName;
	private String eliminationText;
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
	public String getFunctionName() {
		return functionName;
	}
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	public String getEliminationText() {
		return eliminationText;
	}
	public void setEliminationText(String eliminationText) {
		this.eliminationText = eliminationText;
	}
	@Override
	public String toString() {
		return "ProductNotRecommededModel [ingredientId=" + ingredientId + ", ingredientName=" + ingredientName
				+ ", functionName=" + functionName + ", eliminationText=" + eliminationText + "]";
	}
	

}
