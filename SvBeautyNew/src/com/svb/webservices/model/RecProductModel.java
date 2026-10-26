package com.svb.webservices.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @date 10-Dec-2019
 * @author Synoptek
 *
 */
public class RecProductModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String productId;
	private String productName;
	private String productCategory;
	private String score;
	private String ingrScore;
	private String baseScore;
	private String bumpScore;
	private String ingrCount;
	private String baseCount;
	private String brandName;
	private String ingrScores;
	private String baseScores;
	private String totalIngrCount;
	private String ingrWeightage;
	private String baseWeightage;
	private List<RecIngredientModel> ingredients;
	
	private String productSubCategory;
	private String productWhenToUse;
	private String reason;
	private String eliminatedIngrName;

	/**
	 * @return the productId
	 */
	public String getProductId() {
		return productId;
	}

	/**
	 * @param productId the productId to set
	 */
	public void setProductId(String productId) {
		this.productId = productId;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * @return the productCategory
	 */
	public String getProductCategory() {
		return productCategory;
	}

	/**
	 * @param productCategory the productCategory to set
	 */
	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
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

	/**
	 * @return the ingrScore
	 */
	public String getIngrScore() {
		return ingrScore;
	}

	/**
	 * @param ingrScore the ingrScore to set
	 */
	public void setIngrScore(String ingrScore) {
		this.ingrScore = ingrScore;
	}

	/**
	 * @return the baseScore
	 */
	public String getBaseScore() {
		return baseScore;
	}

	/**
	 * @param baseScore the baseScore to set
	 */
	public void setBaseScore(String baseScore) {
		this.baseScore = baseScore;
	}

	/**
	 * @return the bumpScore
	 */
	public String getBumpScore() {
		return bumpScore;
	}

	/**
	 * @param bumpScore the bumpScore to set
	 */
	public void setBumpScore(String bumpScore) {
		this.bumpScore = bumpScore;
	}

	/**
	 * @return the ingredients
	 */
	public List<RecIngredientModel> getIngredients() {
		return ingredients;
	}

	/**
	 * @param ingredients the ingredients to set
	 */
	public void setIngredients(List<RecIngredientModel> ingredients) {
		this.ingredients = ingredients;
	}

	/**
	 * @return the ingrCount
	 */
	public String getIngrCount() {
		return ingrCount;
	}

	/**
	 * @param ingrCount the ingrCount to set
	 */
	public void setIngrCount(String ingrCount) {
		this.ingrCount = ingrCount;
	}

	/**
	 * @return the baseCount
	 */
	public String getBaseCount() {
		return baseCount;
	}

	/**
	 * @param baseCount the baseCount to set
	 */
	public void setBaseCount(String baseCount) {
		this.baseCount = baseCount;
	}

	/**
	 * @return the brandName
	 */
	public String getBrandName() {
		return brandName;
	}

	/**
	 * @param brandName the brandName to set
	 */
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	/**
	 * @return the ingrScores
	 */
	public String getIngrScores() {
		return ingrScores;
	}

	/**
	 * @param ingrScores the ingrScores to set
	 */
	public void setIngrScores(String ingrScores) {
		this.ingrScores = ingrScores;
	}

	/**
	 * @return the baseScores
	 */
	public String getBaseScores() {
		return baseScores;
	}

	/**
	 * @param baseScores the baseScores to set
	 */
	public void setBaseScores(String baseScores) {
		this.baseScores = baseScores;
	}

	/**
	 * @return the totalIngrCount
	 */
	public String getTotalIngrCount() {
		return totalIngrCount;
	}

	/**
	 * @param totalIngrCount the totalIngrCount to set
	 */

	public void setTotalIngrCount(String totalIngrCount) {
		this.totalIngrCount = totalIngrCount;

	}

	/**
	 * @return the ingrWeightage
	 */
	public String getIngrWeightage() {
		return ingrWeightage;
	}

	/**
	 * @param ingrWeightage the ingrWeightage to set
	 */
	public void setIngrWeightage(String ingrWeightage) {
		this.ingrWeightage = ingrWeightage;
	}

	/**
	 * @return the baseWeightage
	 */
	public String getBaseWeightage() {
		return baseWeightage;
	}

	/**
	 * @param baseWeightage the baseWeightage to set
	 */
	public void setBaseWeightage(String baseWeightage) {
		this.baseWeightage = baseWeightage;
	}
	
	
	public String getProductSubCategory() {
		return productSubCategory;
	}

	public void setProductSubCategory(String productSubCategory) {
		this.productSubCategory = productSubCategory;
	}

	public String getProductWhenToUse() {
		return productWhenToUse;
	}

	public void setProductWhenToUse(String productWhenToUse) {
		this.productWhenToUse = productWhenToUse;
	}
	
	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * @return the eliminatedIngrName
	 */
	public String getEliminatedIngrName() {
		return eliminatedIngrName;
	}

	/**
	 * @param eliminatedIngrName the eliminatedIngrName to set
	 */
	public void setEliminatedIngrName(String eliminatedIngrName) {
		this.eliminatedIngrName = eliminatedIngrName;
	}

	@Override
	public String toString() {
		return "RecProductModel [productId=" + productId + ", productName=" + productName + ", productCategory="
				+ productCategory + ", score=" + score + ", ingrScore=" + ingrScore + ", baseScore=" + baseScore
				+ ", bumpScore=" + bumpScore + ", ingrCount=" + ingrCount + ", baseCount=" + baseCount + ", brandName="
				+ brandName + ", ingrScores=" + ingrScores + ", baseScores=" + baseScores + ", totalIngrCount="
				+ totalIngrCount + ", ingrWeightage=" + ingrWeightage + ", baseWeightage=" + baseWeightage
				+ ", ingredients=" + ingredients + ", productSubCategory=" + productSubCategory + ", productWhenToUse="
				+ productWhenToUse + ", reason=" + reason + ", eliminatedIngrName=" + eliminatedIngrName + "]";
	}

}
