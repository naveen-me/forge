package com.svb.webservices.model.topthreeproducts;

import java.io.Serializable;

public class Top3ProductsList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -699964578172904053L;
	
	private TopCleanserProductsList cleanserProductsList;
	private TopMoisturizerProductsList moisturizerProductsList;
	private TopDailyTreatmentProductsList dailyTreatmentProductsList;
	private TopTargetedOneTreatmentProductsList targetedOneTreatmentProductsList;
	private TopTargetedTwoTreatmentProductsList targetedTwoTreatmentProductsList;
	private TopWeeklyTreatmentProductsList weeklyTreatmentProductsList;
	private TopAtHomeFacialWeeklyTreatmentProductsList atHomeFacialWeeklyTreatmentProductsList;
	private TopEyeProductsList eyeProductsList;
	
	
	
	public Top3ProductsList() {
		super();
		this.cleanserProductsList = new TopCleanserProductsList();
		this.moisturizerProductsList = new TopMoisturizerProductsList();
		this.dailyTreatmentProductsList = new TopDailyTreatmentProductsList();
		this.targetedOneTreatmentProductsList = new TopTargetedOneTreatmentProductsList();
		this.targetedTwoTreatmentProductsList = new TopTargetedTwoTreatmentProductsList();
		this.weeklyTreatmentProductsList = new TopWeeklyTreatmentProductsList();
		this.atHomeFacialWeeklyTreatmentProductsList = new TopAtHomeFacialWeeklyTreatmentProductsList();
		this.eyeProductsList = new TopEyeProductsList();
	}
	public TopCleanserProductsList getCleanserProductsList() {
		return cleanserProductsList;
	}
	public void setCleanserProductsList(TopCleanserProductsList cleanserProductsList) {
		this.cleanserProductsList = cleanserProductsList;
	}
	public TopMoisturizerProductsList getMoisturizerProductsList() {
		return moisturizerProductsList;
	}
	public void setMoisturizerProductsList(TopMoisturizerProductsList moisturizerProductsList) {
		this.moisturizerProductsList = moisturizerProductsList;
	}
	public TopDailyTreatmentProductsList getDailyTreatmentProductsList() {
		return dailyTreatmentProductsList;
	}
	public void setDailyTreatmentProductsList(TopDailyTreatmentProductsList dailyTreatmentProductsList) {
		this.dailyTreatmentProductsList = dailyTreatmentProductsList;
	}
	public TopTargetedOneTreatmentProductsList getTargetedOneTreatmentProductsList() {
		return targetedOneTreatmentProductsList;
	}
	public void setTargetedOneTreatmentProductsList(TopTargetedOneTreatmentProductsList targetedOneTreatmentProductsList) {
		this.targetedOneTreatmentProductsList = targetedOneTreatmentProductsList;
	}
	public TopTargetedTwoTreatmentProductsList getTargetedTwoTreatmentProductsList() {
		return targetedTwoTreatmentProductsList;
	}
	public void setTargetedTwoTreatmentProductsList(TopTargetedTwoTreatmentProductsList targetedTwoTreatmentProductsList) {
		this.targetedTwoTreatmentProductsList = targetedTwoTreatmentProductsList;
	}
	public TopWeeklyTreatmentProductsList getWeeklyTreatmentProductsList() {
		return weeklyTreatmentProductsList;
	}
	public void setWeeklyTreatmentProductsList(TopWeeklyTreatmentProductsList weeklyTreatmentProductsList) {
		this.weeklyTreatmentProductsList = weeklyTreatmentProductsList;
	}
	public TopAtHomeFacialWeeklyTreatmentProductsList getAtHomeFacialWeeklyTreatmentProductsList() {
		return atHomeFacialWeeklyTreatmentProductsList;
	}
	public void setAtHomeFacialWeeklyTreatmentProductsList(
			TopAtHomeFacialWeeklyTreatmentProductsList atHomeFacialWeeklyTreatmentProductsList) {
		this.atHomeFacialWeeklyTreatmentProductsList = atHomeFacialWeeklyTreatmentProductsList;
	}
	public TopEyeProductsList getEyeProductsList() {
		return eyeProductsList;
	}
	public void setEyeProductsList(TopEyeProductsList eyeProductsList) {
		this.eyeProductsList = eyeProductsList;
	}
}
