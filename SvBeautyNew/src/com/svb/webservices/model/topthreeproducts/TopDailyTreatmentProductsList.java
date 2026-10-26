package com.svb.webservices.model.topthreeproducts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.svb.webservices.model.ProductModel;

public class TopDailyTreatmentProductsList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2153439134953173482L;
	
	private List<ProductModel> rednessProductsList;
	private List<ProductModel> acneProductsList;
	private List<ProductModel> ageSpotsAndSunDamageProductsList;
	private List<ProductModel> serumProductsList;
	private List<ProductModel> exfoliatorProductsList;
	private List<ProductModel> serumHydratorProductsList;
	
	public TopDailyTreatmentProductsList() {
		super();
		this.rednessProductsList = new ArrayList<>();
		this.acneProductsList = new ArrayList<>();
		this.ageSpotsAndSunDamageProductsList = new ArrayList<>();
		this.serumProductsList = new ArrayList<>();
		this.exfoliatorProductsList = new ArrayList<>();
		this.serumHydratorProductsList = new ArrayList<>();
	}
	public List<ProductModel> getRednessProductsList() {
		return rednessProductsList;
	}
	public void setRednessProductsList(List<ProductModel> rednessProductsList) {
		this.rednessProductsList = rednessProductsList;
	}
	public List<ProductModel> getAcneProductsList() {
		return acneProductsList;
	}
	public void setAcneProductsList(List<ProductModel> acneProductsList) {
		this.acneProductsList = acneProductsList;
	}
	public List<ProductModel> getAgeSpotsAndSunDamageProductsList() {
		return ageSpotsAndSunDamageProductsList;
	}
	public void setAgeSpotsAndSunDamageProductsList(List<ProductModel> ageSpotsAndSunDamageProductsList) {
		this.ageSpotsAndSunDamageProductsList = ageSpotsAndSunDamageProductsList;
	}
	public List<ProductModel> getSerumProductsList() {
		return serumProductsList;
	}
	public void setSerumProductsList(List<ProductModel> serumProductsList) {
		this.serumProductsList = serumProductsList;
	}
	public List<ProductModel> getExfoliatorProductsList() {
		return exfoliatorProductsList;
	}
	public void setExfoliatorProductsList(List<ProductModel> exfoliatorProductsList) {
		this.exfoliatorProductsList = exfoliatorProductsList;
	}
	public List<ProductModel> getSerumHydratorProductsList() {
		return serumHydratorProductsList;
	}
	public void setSerumHydratorProductsList(List<ProductModel> serumHydratorProductsList) {
		this.serumHydratorProductsList = serumHydratorProductsList;
	}
}
