package com.svb.webservices.model;

import java.io.Serializable;
import java.util.List;

public class NotHaveTheseBadIngredientsModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String headerText;
	//Added to add Does Not Contain Text - Shivam - 07/12/2023 - Start
	private String doesNotContainText;
	//Added to add Does Not Contain Text - Shivam - 07/12/2023 - End
	private List<NotHaveTheseBadIngredientsListModel> notHaveTheseBadIngredientsList;
	
	public String getHeaderText() {
		return headerText;
	}
	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}
	
	//Added to add Does Not Contain Text - Shivam - 07/12/2023 - Start
	public String getDoesNotContainText() {
		return doesNotContainText;
	}
	public void setDoesNotContainText(String doesNotContainText) {
		this.doesNotContainText = doesNotContainText;
	}
	//Added to add Does Not Contain Text - Shivam - 07/12/2023 - End
	
	public List<NotHaveTheseBadIngredientsListModel> getNotHaveTheseBadIngredientsList() {
		return notHaveTheseBadIngredientsList;
	}
	public void setNotHaveTheseBadIngredientsList(List<NotHaveTheseBadIngredientsListModel> notHaveTheseBadIngredientsList) {
		this.notHaveTheseBadIngredientsList = notHaveTheseBadIngredientsList;
	}
	@Override
	public String toString() {
		return "NotHaveTheseBadIngredientsModel [headerText=" + headerText + ", notHaveTheseBadIngredientsList="
				+ notHaveTheseBadIngredientsList + "]";
	}
	
}
