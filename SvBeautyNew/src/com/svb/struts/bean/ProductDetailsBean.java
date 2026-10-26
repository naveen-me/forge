package com.svb.struts.bean;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ProductDetailsBean {
	String productId;
	String productName;
	String productDescription;
	String brandName;
	String productDirections;
	ArrayList ingredients;
	String mediumImage = "";
	String ingrcount = "";
	/**
	* Added by Synoptek(29-11-2019)
	* added variables to get required result columns 
	**/
//---------------------------------------- start ---------------------------------------------------
	ArrayList displayOrder;
	ArrayList ingredientIdList;
	String productType;
	String productMasterType;
//---------------------------------------- end ---------------------------------------------------

//---------------------------------------- start ---------------------------------------------------
	
	Hashtable<String,String> ingredientsName;
	
	public Hashtable<String, String> getIngredientsName() {
		return ingredientsName;
	}

	public void setIngredientsName(Hashtable<String, String> ingredientsName) {
		this.ingredientsName = ingredientsName;
	}

//---------------------------------------- end ---------------------------------------------------

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public ArrayList getIngredients() {
		return ingredients;
	}

	public void setIngredients(ArrayList ingredients) {
		this.ingredients = ingredients;
	}

	public String getProductDescription() {
		return productDescription;
	}

	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}

	public String getProductDirections() {
		return productDirections;
	}

	public void setProductDirections(String productDirections) {
		this.productDirections = productDirections;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	/**
	 * @param string
	 */
	public void setMediumImage(String imagename) {
		// TODO Auto-generated method stub
		this.mediumImage = imagename;
	}

	public String getMediumImage() {
		return this.mediumImage;
	}

	/**
	 * @param string
	 */
	public void setIngredientsCount(String ingrcount) {
		// TODO Auto-generated method stub
		this.ingrcount = ingrcount;
	}

	public String getIngredientsCount() {
		return this.ingrcount;
	}

//************************************** Added by Synoptek(29-11-2019) ********************************************* 
//---------------------------------------- start ---------------------------------------------------
	public ArrayList getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(ArrayList displayOrder) {
		this.displayOrder = displayOrder;
	}

	public ArrayList getIngredientIdList() {
		return ingredientIdList;
	}

	public void setIngredientIdList(ArrayList ingredientIdList) {
		this.ingredientIdList = ingredientIdList;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getProductMasterType() {
		return productMasterType;
	}

	public void setProductMasterType(String productMasterType) {
		this.productMasterType = productMasterType;
	}


	// ---------------------------------------- end
	// ---------------------------------------------------
	

}
