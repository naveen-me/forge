package com.svb.webservices.model;

import java.util.List;

public class RecProductWithIngrScoreModel {

	private List<RecProductModel> cleanserList;
	private List<RecProductModel> moisturizerList;
	private List<RecProductModel> treatmentList;
	private List<RecProductModel> eyeProductList;
	/**
	 * @return the cleanserList
	 */
	public List<RecProductModel> getCleanserList() {
		return cleanserList;
	}
	/**
	 * @param cleanserList the cleanserList to set
	 */
	public void setCleanserList(List<RecProductModel> cleanserList) {
		this.cleanserList = cleanserList;
	}
	/**
	 * @return the moisturizerList
	 */
	public List<RecProductModel> getMoisturizerList() {
		return moisturizerList;
	}
	/**
	 * @param moisturizerList the moisturizerList to set
	 */
	public void setMoisturizerList(List<RecProductModel> moisturizerList) {
		this.moisturizerList = moisturizerList;
	}
	/**
	 * @return the treatmentList
	 */
	public List<RecProductModel> getTreatmentList() {
		return treatmentList;
	}
	/**
	 * @param treatmentList the treatmentList to set
	 */
	public void setTreatmentList(List<RecProductModel> treatmentList) {
		this.treatmentList = treatmentList;
	}
	/**
	 * @return the eyeProductList
	 */
	public List<RecProductModel> getEyeProductList() {
		return eyeProductList;
	}
	/**
	 * @param eyeProductList the eyeProductList to set
	 */
	public void setEyeProductList(List<RecProductModel> eyeProductList) {
		this.eyeProductList = eyeProductList;
	}
}
