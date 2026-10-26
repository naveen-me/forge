package com.svb.webservices.model.topthreeproducts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.svb.webservices.model.ProductModel;

public class TopWeeklyTreatmentProductsList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2369380290001485439L;
	
	private List<ProductModel> weeklyTreatmentExfoliatorProductsList;
	private List<ProductModel> weeklyTreatmentMasksProductsList;
	private List<ProductModel> weeklyTreatmentPeelsProductsList;
	
	public TopWeeklyTreatmentProductsList() {
		super();
		this.weeklyTreatmentExfoliatorProductsList = new ArrayList<>();
		this.weeklyTreatmentMasksProductsList = new ArrayList<>();
		this.weeklyTreatmentPeelsProductsList = new ArrayList<>();
	}
	public List<ProductModel> getWeeklyTreatmentExfoliatorProductsList() {
		return weeklyTreatmentExfoliatorProductsList;
	}
	public void setWeeklyTreatmentExfoliatorProductsList(List<ProductModel> weeklyTreatmentExfoliatorProductsList) {
		this.weeklyTreatmentExfoliatorProductsList = weeklyTreatmentExfoliatorProductsList;
	}
	public List<ProductModel> getWeeklyTreatmentMasksProductsList() {
		return weeklyTreatmentMasksProductsList;
	}
	public void setWeeklyTreatmentMasksProductsList(List<ProductModel> weeklyTreatmentMasksProductsList) {
		this.weeklyTreatmentMasksProductsList = weeklyTreatmentMasksProductsList;
	}
	public List<ProductModel> getWeeklyTreatmentPeelsProductsList() {
		return weeklyTreatmentPeelsProductsList;
	}
	public void setWeeklyTreatmentPeelsProductsList(List<ProductModel> weeklyTreatmentPeelsProductsList) {
		this.weeklyTreatmentPeelsProductsList = weeklyTreatmentPeelsProductsList;
	}
}
