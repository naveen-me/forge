package com.svb.webservices.model;

import java.util.Arrays;

/**
 * 
 * @date 29-Nov-2019
 * @author Synoptek
 *
 */
public class SeeIngredientModel {

	private String[] ingredientId;
	private String[] ingredientName;
	private String ingredientDescription;

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

	public String getIngredientDescription() {
		return ingredientDescription;
	}

	public void setIngredientDescription(String ingredientDescription) {
		this.ingredientDescription = ingredientDescription;
	}

	@Override
	public String toString() {
		return "SeeIngredientModel [ingredientId=" + Arrays.toString(ingredientId) + ", ingredientName="
				+ Arrays.toString(ingredientName) + ", ingredientDescription=" + ingredientDescription + "]";
	}

}
