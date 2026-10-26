package com.svb.webservices.model;

import java.io.Serializable;
import java.util.List;

public class HeroIngredientsListModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String headerText;
	private List<HeroIngredientModel> heroIngredientModel;
	
	public String getHeaderText() {
		return headerText;
	}
	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}
	public List<HeroIngredientModel> getHeroIngredientsList() {
		return heroIngredientModel;
	}
	public void setHeroIngredientsList(List<HeroIngredientModel> heroIngredientModel) {
		this.heroIngredientModel = heroIngredientModel;
	}
	
	@Override
	public String toString() {
		return "HeroIngredientsListModel [headerText=" + headerText + ", heroIngredientModel=" + heroIngredientModel
				+ "]";
	}
}
