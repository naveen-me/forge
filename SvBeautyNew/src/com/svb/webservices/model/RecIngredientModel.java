package com.svb.webservices.model;

import java.io.Serializable;

/**
 * 
 * @date 10-Dec-2019
 * @author Synoptek
 *
 */
public class RecIngredientModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String ingredientName;
	private String score;

	/**
	 * @return the ingredientName
	 */
	public String getIngredientName() {
		return ingredientName;
	}

	/**
	 * @param ingredientName the ingredientName to set
	 */
	public void setIngredientName(String ingredientName) {
		this.ingredientName = ingredientName;
	}

	/**
	 * @return the score
	 */
	public String getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(String score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return "RecIngredientModel [ingredientName=" + ingredientName + ", score=" + score + "]";
	}

}
