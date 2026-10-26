package com.svb.webservices.model.topthreeproducts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.svb.webservices.model.ProductModel;

public class TopAtHomeFacialWeeklyTreatmentProductsList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7855810992551313176L;

	private List<ProductModel> atHomeWeeklyTreatmentExfoliatorProductsList;
	private List<ProductModel> atHomeWeeklyTreatmentMasksProductsList;
	private List<ProductModel> atHomeWeeklyTreatmentPeelsProductsList;
	
	public TopAtHomeFacialWeeklyTreatmentProductsList() {
		super();
		this.atHomeWeeklyTreatmentExfoliatorProductsList = new ArrayList<>();
		this.atHomeWeeklyTreatmentMasksProductsList = new ArrayList<>();
		this.atHomeWeeklyTreatmentPeelsProductsList = new ArrayList<>();
	}
	public List<ProductModel> getAtHomeWeeklyTreatmentExfoliatorProductsList() {
		return atHomeWeeklyTreatmentExfoliatorProductsList;
	}
	public void setAtHomeWeeklyTreatmentExfoliatorProductsList(
			List<ProductModel> atHomeWeeklyTreatmentExfoliatorProductsList) {
		this.atHomeWeeklyTreatmentExfoliatorProductsList = atHomeWeeklyTreatmentExfoliatorProductsList;
	}
	public List<ProductModel> getAtHomeWeeklyTreatmentMasksProductsList() {
		return atHomeWeeklyTreatmentMasksProductsList;
	}
	public void setAtHomeWeeklyTreatmentMasksProductsList(List<ProductModel> atHomeWeeklyTreatmentMasksProductsList) {
		this.atHomeWeeklyTreatmentMasksProductsList = atHomeWeeklyTreatmentMasksProductsList;
	}
	public List<ProductModel> getAtHomeWeeklyTreatmentPeelsProductsList() {
		return atHomeWeeklyTreatmentPeelsProductsList;
	}
	public void setAtHomeWeeklyTreatmentPeelsProductsList(List<ProductModel> atHomeWeeklyTreatmentPeelsProductsList) {
		this.atHomeWeeklyTreatmentPeelsProductsList = atHomeWeeklyTreatmentPeelsProductsList;
	}
}
