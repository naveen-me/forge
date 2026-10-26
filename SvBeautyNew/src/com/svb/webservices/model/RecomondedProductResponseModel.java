package com.svb.webservices.model;

import java.io.Serializable;
import java.util.List;

import com.svb.webservices.model.topthreeproducts.Top3ProductsList;

/**
 * 
 * @date 29-Nov-2019
 * @author Synoptek
 *
 */
public class RecomondedProductResponseModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2996883235922275567L;

	private List<ProductModel> cleanserList;
	private List<ProductModel> moisturizerList;
	private List<ProductModel> dailyTreatmentList;
	private List<ProductModel> targetedOneTreatmentList;
	private List<ProductModel> targetedTwoTreatmentList;
	private List<ProductModel> weeklyTreatmentList;
	private List<ProductModel> atHomeFacialWeeklyTreatmentList;
	private List<ProductModel> eyeProductList;
	private Top3ProductsList top3ProductsList; //Added to Display top 3 products from each sub-category - Shivam - 21/02/2024
	
	/**
	 * @return the cleanserList
	 */
	public List<ProductModel> getCleanserList() {
		return cleanserList;
	}
	/**
	 * @param cleanserList the cleanserList to set
	 */
	public void setCleanserList(List<ProductModel> cleanserList) {
		this.cleanserList = cleanserList;
	}
	/**
	 * @return the moisturizerList
	 */
	public List<ProductModel> getMoisturizerList() {
		return moisturizerList;
	}
	/**
	 * @param moisturizerList the moisturizerList to set
	 */
	public void setMoisturizerList(List<ProductModel> moisturizerList) {
		this.moisturizerList = moisturizerList;
	}
	/**
	 * @return the dailyTreatmentList
	 */
	public List<ProductModel> getDailyTreatmentList() {
		return dailyTreatmentList;
	}
	/**
	 * @param dailyTreatmentList the dailyTreatmentList to set
	 */
	public void setDailyTreatmentList(List<ProductModel> dailyTreatmentList) {
		this.dailyTreatmentList = dailyTreatmentList;
	}
	/**
	 * @return the targetedOneTreatmentList
	 */
	public List<ProductModel> getTargetedOneTreatmentList() {
		return targetedOneTreatmentList;
	}
	/**
	 * @param targetedOneTreatmentList the targetedOneTreatmentList to set
	 */
	public void setTargetedOneTreatmentList(List<ProductModel> targetedOneTreatmentList) {
		this.targetedOneTreatmentList = targetedOneTreatmentList;
	}
	/**
	 * @return the targetedTwoTreatmentList
	 */
	public List<ProductModel> getTargetedTwoTreatmentList() {
		return targetedTwoTreatmentList;
	}
	/**
	 * @param targetedTwoTreatmentList the targetedTwoTreatmentList to set
	 */
	public void setTargetedTwoTreatmentList(List<ProductModel> targetedTwoTreatmentList) {
		this.targetedTwoTreatmentList = targetedTwoTreatmentList;
	}
	/**
	 * @return the weeklyTreatmentList
	 */
	public List<ProductModel> getWeeklyTreatmentList() {
		return weeklyTreatmentList;
	}
	/**
	 * @param weeklyTreatmentList the weeklyTreatmentList to set
	 */
	public void setWeeklyTreatmentList(List<ProductModel> weeklyTreatmentList) {
		this.weeklyTreatmentList = weeklyTreatmentList;
	}
	/**
	 * @return the eyeProductList
	 */
	public List<ProductModel> getEyeProductList() {
		return eyeProductList;
	}
	/**
	 * @param eyeProductList the eyeProductList to set
	 */
	public void setEyeProductList(List<ProductModel> eyeProductList) {
		this.eyeProductList = eyeProductList;
	}
	/**
	 * @return the atHomeFacialWeeklyTreatmentList
	 */
	public List<ProductModel> getAtHomeFacialWeeklyTreatmentList() {
		return atHomeFacialWeeklyTreatmentList;
	}
	/**
	 * @param atHomeFacialWeeklyTreatmentList the atHomeFacialWeeklyTreatmentList to set
	 */
	public void setAtHomeFacialWeeklyTreatmentList(List<ProductModel> atHomeFacialWeeklyTreatmentList) {
		this.atHomeFacialWeeklyTreatmentList = atHomeFacialWeeklyTreatmentList;
	}
	
	/**
	 * @return Response Model containing List of top 3 products from each category
	 */
	public Top3ProductsList getTop3ProductsList() {
		return top3ProductsList;
	}
	/**
	 * @param Model containing List of top 3 products from each category
	 */
	public void setTop3ProductsList(Top3ProductsList top3ProductsList) {
		this.top3ProductsList = top3ProductsList;
	}
}
