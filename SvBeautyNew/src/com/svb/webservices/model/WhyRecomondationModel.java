package com.svb.webservices.model;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @date 29-Nov-2019
 * @author Synoptek
 *
 */
public class WhyRecomondationModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4285093698861016050L;
	private String skinConcern;
	private String productRating;
	private List<SeeIngredientModel> activeIngredient;

	public String getSkinConcern() {
		return skinConcern;
	}

	public void setSkinConcern(String skinConcern) {
		this.skinConcern = skinConcern;
	}

	public String getProductRating() {
		return productRating;
	}

	public void setProductRating(String productRating) {
		this.productRating = productRating;
	}

	public List<SeeIngredientModel> getActiveIngredient() {
		return activeIngredient;
	}

	public void setActiveIngredient(List<SeeIngredientModel> activeIngredient) {
		this.activeIngredient = activeIngredient;
	}

	@Override
	public String toString() {
		return "WhyRecomondationModel [skinConcern=" + skinConcern + ", productRating=" + productRating
				+ ", activeIngredient=" + activeIngredient + "]";
	}

}
