package com.svb.webservices.model;

import java.io.Serializable;
import java.util.List;


public class ProductScoreAnalysisModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3905054658861909286L;
	private String headerText;
	private HeroIngredientsListModel heroIngredients;
	private List<MoreGreatIngredientsModel> moreGreatIngredients;
	private NotHaveTheseBadIngredientsModel notHaveTheseBadIngredients;
	private List<HeroIngredientModel> notForYourSkin;
	private CleanScoreModel cleanScore;
	private List<ProductNotRecommededModel> productNotRecommeded;
	
	public List<ProductNotRecommededModel> getProductNotRecommeded() {
		return productNotRecommeded;
	}
	public void setProductNotRecommeded(List<ProductNotRecommededModel> productNotRecommeded) {
		this.productNotRecommeded = productNotRecommeded;
	}
	
	
	public HeroIngredientsListModel getHeroIngredients() {
		return heroIngredients;
	}
	public void setHeroIngredients(HeroIngredientsListModel heroIngredients) {
		this.heroIngredients = heroIngredients;
	}
	public List<MoreGreatIngredientsModel> getMoreGreatIngredients() {
		return moreGreatIngredients;
	}
	public void setMoreGreatIngredients(List<MoreGreatIngredientsModel> moreGreatIngredients) {
		this.moreGreatIngredients = moreGreatIngredients;
	}
		
	public NotHaveTheseBadIngredientsModel getNotHaveTheseBadIngredients() {
		return notHaveTheseBadIngredients;
	}
	public void setNotHaveTheseBadIngredients(NotHaveTheseBadIngredientsModel notHaveTheseBadIngredients) {
		this.notHaveTheseBadIngredients = notHaveTheseBadIngredients;
	}
	public List<HeroIngredientModel> getNotForYourSkin() {
		return notForYourSkin;
	}
	public void setNotForYourSkin(List<HeroIngredientModel> notForYourSkin) {
		this.notForYourSkin = notForYourSkin;
	}
	public CleanScoreModel getCleanScore() {
		return cleanScore;
	}
	public void setCleanScore(CleanScoreModel cleanScore) {
		this.cleanScore = cleanScore;
	}
	
	public String getHeaderText() {
		return headerText;
	}
	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}
	
	
	@Override
	public String toString() {
		return "ProductScoreAnalysisModel [headerText=" + headerText + ", heroIngredients=" + heroIngredients
				+ ", moreGreatIngredients=" + moreGreatIngredients + ", notHaveTheseBadIngredients="
				+ notHaveTheseBadIngredients + ", notForYourSkin=" + notForYourSkin + ", cleanScore=" + cleanScore
				+ ", productNotRecommeded=" + productNotRecommeded + "]";
	}
	
}
