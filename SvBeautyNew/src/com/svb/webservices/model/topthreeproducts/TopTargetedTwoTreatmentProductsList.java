package com.svb.webservices.model.topthreeproducts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.svb.webservices.model.ProductModel;

public class TopTargetedTwoTreatmentProductsList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -84759952429088968L;

	private List<ProductModel> decolleteAndNeckProductsList;
	private List<ProductModel> acneSpotTreatmentProductsList;
	private List<ProductModel> ageSpotsSunDamageProductsList;
	private List<ProductModel> rednessProductsList;
	private List<ProductModel> serumsRetinolProductsList;
	private List<ProductModel> serumsVitaminCProductsList;
	
	public TopTargetedTwoTreatmentProductsList() {
		super();
		this.decolleteAndNeckProductsList = new ArrayList<>();
		this.acneSpotTreatmentProductsList = new ArrayList<>();
		this.ageSpotsSunDamageProductsList = new ArrayList<>();
		this.rednessProductsList = new ArrayList<>();
		this.serumsRetinolProductsList = new ArrayList<>();
		this.serumsVitaminCProductsList = new ArrayList<>();
	}
	
	public List<ProductModel> getDecolleteAndNeckProductsList() {
		return decolleteAndNeckProductsList;
	}
	public void setDecolleteAndNeckProductsList(List<ProductModel> decolleteAndNeckProductsList) {
		this.decolleteAndNeckProductsList = decolleteAndNeckProductsList;
	}
	public List<ProductModel> getAcneSpotTreatmentProductsList() {
		return acneSpotTreatmentProductsList;
	}
	public void setAcneSpotTreatmentProductsList(List<ProductModel> acneSpotTreatmentProductsList) {
		this.acneSpotTreatmentProductsList = acneSpotTreatmentProductsList;
	}
	public List<ProductModel> getAgeSpotsSunDamageProductsList() {
		return ageSpotsSunDamageProductsList;
	}
	public void setAgeSpotsSunDamageProductsList(List<ProductModel> ageSpotsSunDamageProductsList) {
		this.ageSpotsSunDamageProductsList = ageSpotsSunDamageProductsList;
	}
	public List<ProductModel> getRednessProductsList() {
		return rednessProductsList;
	}
	public void setRednessProductsList(List<ProductModel> rednessProductsList) {
		this.rednessProductsList = rednessProductsList;
	}

	public List<ProductModel> getSerumsRetinolProductsList() {
		return serumsRetinolProductsList;
	}

	public void setSerumsRetinolProductsList(List<ProductModel> serumsRetinolProductsList) {
		this.serumsRetinolProductsList = serumsRetinolProductsList;
	}

	public List<ProductModel> getSerumsVitaminCProductsList() {
		return serumsVitaminCProductsList;
	}

	public void setSerumsVitaminCProductsList(List<ProductModel> serumsVitaminCProductsList) {
		this.serumsVitaminCProductsList = serumsVitaminCProductsList;
	}
}
