package com.svb.webservices.model;

import java.io.Serializable;

/**
 * 
 * @date 29-Nov-2019
 * @author Synoptek
 *
 */
public class IngredientModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7856037228859699730L;
	private String ingredientId;
	private String ingredientName;
	private String displayOrder;
	private String ingredientDefination;

	public String getIngredientName() {
		return ingredientName;
	}

	public void setIngredientName(String ingredientName) {
		this.ingredientName = ingredientName;
	}

	public String getIngredientDefination() {
		return ingredientDefination;
	}

	public void setIngredientDefination(String ingredientDefination) {
		this.ingredientDefination = ingredientDefination;
	}

	public String getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(String displayOrder) {
		this.displayOrder = displayOrder;
	}

	public String getIngredientId() {
		return ingredientId;
	}

	public void setIngredientId(String ingredientId) {
		this.ingredientId = ingredientId;
	}

	@Override
	public String toString() {
		return "IngredientModel [ingredientId=" + ingredientId + ", ingredientName=" + ingredientName
				+ ", displayOrder=" + displayOrder + ", ingredientDefination=" + ingredientDefination + "]";
	}

}
