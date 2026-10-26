package com.svb.webservices.model.topthreeproducts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.svb.webservices.model.ProductModel;

public class TopMoisturizerProductsList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7911591946746185364L;
	
	private List<ProductModel> moisturizerProductsWithSPFList;
	private List<ProductModel> moisturizerProductsWithoutSPFList;
	private List<ProductModel> nightlyMoisturizerProductsList;
	private List<ProductModel> facialSunscreensProductsList;
	
	
	public TopMoisturizerProductsList() {
		super();
		this.moisturizerProductsWithSPFList = new ArrayList<>();
		this.moisturizerProductsWithoutSPFList = new ArrayList<>();
		this.nightlyMoisturizerProductsList = new ArrayList<>();
		this.facialSunscreensProductsList = new ArrayList<>();
	}
	public List<ProductModel> getMoisturizerProductsWithSPFList() {
		return moisturizerProductsWithSPFList;
	}
	public void setMoisturizerProductsWithSPFList(List<ProductModel> moisturizerProductsWithSPFList) {
		this.moisturizerProductsWithSPFList = moisturizerProductsWithSPFList;
	}
	public List<ProductModel> getMoisturizerProductsWithoutSPFList() {
		return moisturizerProductsWithoutSPFList;
	}
	public void setMoisturizerProductsWithoutSPFList(List<ProductModel> moisturizerProductsWithoutSPFList) {
		this.moisturizerProductsWithoutSPFList = moisturizerProductsWithoutSPFList;
	}
	public List<ProductModel> getNightlyMoisturizerProductsList() {
		return nightlyMoisturizerProductsList;
	}
	public void setNightlyMoisturizerProductsList(List<ProductModel> nightlyMoisturizerProductsList) {
		this.nightlyMoisturizerProductsList = nightlyMoisturizerProductsList;
	}
	public List<ProductModel> getFacialSunscreensProductsList() {
		return facialSunscreensProductsList;
	}
	public void setFacialSunscreensProductsList(List<ProductModel> facialSunscreensProductsList) {
		this.facialSunscreensProductsList = facialSunscreensProductsList;
	}
}
