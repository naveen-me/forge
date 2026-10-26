package com.svb.webservices.model.topthreeproducts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.svb.webservices.model.ProductModel;

public class TopEyeProductsList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8504475471928556422L;
	
	private List<ProductModel> eyeTreatmentAndSerumProductsList;
	private List<ProductModel> eyeMasksProductsList;
	
	public TopEyeProductsList() {
		super();
		this.eyeTreatmentAndSerumProductsList = new ArrayList<>();
		this.eyeMasksProductsList = new ArrayList<>();
	}
	public List<ProductModel> getEyeTreatmentAndSerumProductsList() {
		return eyeTreatmentAndSerumProductsList;
	}
	public void setEyeTreatmentAndSerumProductsList(List<ProductModel> eyeTreatmentAndSerumProductsList) {
		this.eyeTreatmentAndSerumProductsList = eyeTreatmentAndSerumProductsList;
	}
	public List<ProductModel> getEyeMasksProductsList() {
		return eyeMasksProductsList;
	}
	public void setEyeMasksProductsList(List<ProductModel> eyeMasksProductsList) {
		this.eyeMasksProductsList = eyeMasksProductsList;
	}
}
