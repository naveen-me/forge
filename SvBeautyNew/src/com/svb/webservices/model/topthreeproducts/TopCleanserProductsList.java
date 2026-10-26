package com.svb.webservices.model.topthreeproducts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.svb.webservices.model.ProductModel;

public class TopCleanserProductsList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7546595509905949714L;

	private List<ProductModel> preCleanseList;
	private List<ProductModel> tonersAndMistsList;
	private List<ProductModel> faceWashAndCleansersList;
	private List<ProductModel> facialWipesList;
	
	
	
	
	public TopCleanserProductsList() {
		super();
		this.preCleanseList = new ArrayList<>();
		this.tonersAndMistsList = new ArrayList<>();
		this.faceWashAndCleansersList = new ArrayList<>();
		this.facialWipesList = new ArrayList<>();
	}
	public List<ProductModel> getPreCleanseList() {
		return preCleanseList;
	}
	public void setPreCleanseList(List<ProductModel> preCleanseList) {
		this.preCleanseList = preCleanseList;
	}
	public List<ProductModel> getTonersAndMistsList() {
		return tonersAndMistsList;
	}
	public void setTonersAndMistsList(List<ProductModel> tonersAndMistsList) {
		this.tonersAndMistsList = tonersAndMistsList;
	}
	public List<ProductModel> getFaceWashAndCleansersList() {
		return faceWashAndCleansersList;
	}
	public void setFaceWashAndCleansersList(List<ProductModel> faceWashAndCleansersList) {
		this.faceWashAndCleansersList = faceWashAndCleansersList;
	}
	public List<ProductModel> getFacialWipesList() {
		return facialWipesList;
	}
	public void setFacialWipesList(List<ProductModel> facialWipesList) {
		this.facialWipesList = facialWipesList;
	}
}
