package com.svb.webservices.model;

import java.io.Serializable;
import java.util.List;

public class SingleProductScoreResponseModel implements Serializable{
	
	private static final long serialVersionUID = 4657502967042970306L;
	private String productId;
	private String upc;
	private String sku;
//	private String productName;
//	private String brandId;
//	private String productType;
	private String productScore;
//	private String productMasterType;
//	private String whenToUse;
//	private String multiBenefits;
//	private String lifeStyleBasedRecs1;
//	private String lifeStyleBasedRecs2;
	private List<IngredientModel> ingredients;
	private List<WhyRecomondationModel> whyRecommended;
	private ProductScoreAnalysisModel productScoreAnalysis;
//	private String warningText;
// Get RX flag from the sv_product_rx_warning for ingredients in the product - start
//	private String flagRXAcne;
//	private String flagRXRetinol;
//	private String flagTropicalSteriods;
// Get RX flag from the sv_product_rx_warning for ingredients in the product - End
	private String responseCode;
	private String responseMessage;
	private String scoreMessage;
	
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
	 * @return the upc
	 */
	public String getUpc() {
		return upc;
	}

	/**
	 * @param upc the upc to set
	 */
	public void setUpc(String upc) {
		this.upc = upc;
	}

	/**
	 * @return the sku
	 */
	public String getSku() {
		return sku;
	}

	/**
	 * @param sku the sku to set
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}
// Get RX flag from the sv_product_rx_warning for ingredients in the product - Start
//	public String getFlagRXAcne() {
//		return flagRXAcne;
//	}
//
//	public void setFlagRXAcne(String flagRXAcne) {
//		this.flagRXAcne = flagRXAcne;
//	}
//
//	public String getFlagRXRetinol() {
//		return flagRXRetinol;
//	}
//
//	public void setFlagRXRetinol(String flagRXRetinol) {
//		this.flagRXRetinol = flagRXRetinol;
//	}
//
//	public String getFlagTropicalSteriods() {
//		return flagTropicalSteriods;
//	}
//
//	public void setFlagTropicalSteriods(String flagSteriods) {
//		this.flagTropicalSteriods = flagSteriods;
//	}
// Get RX flag from the sv_product_rx_warning for ingredients in the product - End
	/**
	 * @return the productName
	 */
//	public String getProductName() {
//		return productName;
//	}
//
//	/**
//	 * @param productName the productName to set
//	 */
//	public void setProductName(String productName) {
//		this.productName = productName;
//	}

	/**
	 * @return the brandId
	 */
//	public String getBrandId() {
//		return brandId;
//	}
//
//	/**
//	 * @param brandId the brandId to set
//	 */
//	public void setBrandId(String brandId) {
//		this.brandId = brandId;
//	}

	/**
	 * @return the productType
	 */
//	public String getProductType() {
//		return productType;
//	}
//
//	/**
//	 * @param productType the productType to set
//	 */
//	public void setProductType(String productType) {
//		this.productType = productType;
//	}

	/**
	 * @return the productScore
	 */
	public String getProductScore() {
		return productScore;
	}

	/**
	 * @param productScore the productScore to set
	 */
	public void setProductScore(String productScore) {
		this.productScore = productScore;
	}

	/**
	 * @return the productMasterType
	 */
//	public String getProductMasterType() {
//		return productMasterType;
//	}

	/**
	 * @param productMasterType the productMasterType to set
	 */
//	public void setProductMasterType(String productMasterType) {
//		this.productMasterType = productMasterType;
//	}

	/**
	 * @return the ingredients
	 */
	public List<IngredientModel> getIngredients() {
		return ingredients;
	}

	/**
	 * @param ingredients the ingredients to set
	 */
	public void setIngredients(List<IngredientModel> ingredients) {
		this.ingredients = ingredients;
	}

	/**
	 * @return the whyRecommended
	 */
	public List<WhyRecomondationModel> getWhyRecommended() {
		return whyRecommended;
	}

	/**
	 * @param whyRecommended the whyRecommended to set
	 */
	public void setWhyRecommended(List<WhyRecomondationModel> whyRecommended) {
		this.whyRecommended = whyRecommended;
	}
	
	/**
	 * @return the productScoreAnalysis
	 */
	public ProductScoreAnalysisModel getProductScoreAnalysis() {
		return productScoreAnalysis;
	}

	/**
	 * @param productScoreAnalysis the productScoreAnalysis to set
	 */
	public void setProductScoreAnalysis(ProductScoreAnalysisModel productScoreAnalysis) {
		this.productScoreAnalysis = productScoreAnalysis;
	}

	/**
	 * @return the whenToUse
	 */
//	public String getWhenToUse() {
//		return whenToUse;
//	}
//
//	/**
//	 * @param whenToUse the whenToUse to set
//	 */
//	public void setWhenToUse(String whenToUse) {
//		this.whenToUse = whenToUse;
//	}

	/**
	 * @return the multiBenefits
	 */
//	public String getMultiBenefits() {
//		return multiBenefits;
//	}
//
//	/**
//	 * @param multiBenefits the multiBenefits to set
//	 */
//	public void setMultiBenefits(String multiBenefits) {
//		this.multiBenefits = multiBenefits;
//	}

	/**
	 * @return the lifeStyleBasedRecs1
	 */
//	public String getLifeStyleBasedRecs1() {
//		return lifeStyleBasedRecs1;
//	}
//
//	/**
//	 * @param lifeStyleBasedRecs1 the lifeStyleBasedRecs1 to set
//	 */
//	public void setLifeStyleBasedRecs1(String lifeStyleBasedRecs1) {
//		this.lifeStyleBasedRecs1 = lifeStyleBasedRecs1;
//	}

	/**
	 * @return the lifeStyleBasedRecs2
	 */
//	public String getLifeStyleBasedRecs2() {
//		return lifeStyleBasedRecs2;
//	}
//
//	/**
//	 * @param lifeStyleBasedRecs2 the lifeStyleBasedRecs2 to set
//	 */
//	public void setLifeStyleBasedRecs2(String lifeStyleBasedRecs2) {
//		this.lifeStyleBasedRecs2 = lifeStyleBasedRecs2;
//	}

	/**
	 * @return the warningText
	 */
//	public String getWarningText() {
//		return warningText;
//	}
//
//	/**
//	 * @param warningText the warningText to set
//	 */
//	public void setWarningText(String warningText) {
//		this.warningText = warningText;
//	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getScoreMessage() {
		return scoreMessage;
	}

	public void setScoreMessage(String scoreMessage) {
		this.scoreMessage = scoreMessage;
	}

	@Override
	public String toString() {
		return "SingleProductScoreResponseModel [productId=" + productId + ", upc=" + upc + ", sku=" + sku
				+ ", productScore=" + productScore + ", ingredients=" + ingredients + ", whyRecommended="
				+ whyRecommended + ", productScoreAnalysis=" + productScoreAnalysis + ", responseCode=" + responseCode
				+ ", responseMessage=" + responseMessage + ", scoreMessage=" + scoreMessage + "]";
	}

//	@Override
//	public String toString() {
//		return "SingleProductScoreResponseModel [productId=" + productId + ", upc=" + upc + ", sku=" + sku
//				+ ", productName=" + productName + ", brandId=" + brandId + ", productType=" + productType
//				+ ", productScore=" + productScore + ", productMasterType=" + productMasterType + ", whenToUse="
//				+ whenToUse + ", multiBenefits=" + multiBenefits + ", lifeStyleBasedRecs1=" + lifeStyleBasedRecs1
//				+ ", lifeStyleBasedRecs2=" + lifeStyleBasedRecs2 + ", ingredients=" + ingredients + ", whyRecommended="
//				+ whyRecommended + ", warningText=" + warningText + ", flagRXAcne=" + flagRXAcne + ", flagRXRetinol="
//				+ flagRXRetinol + ", flagTropicalSteriods=" + flagTropicalSteriods + ", responseMessage="
//				+ responseMessage + "]";
//	}	
	
	
}
