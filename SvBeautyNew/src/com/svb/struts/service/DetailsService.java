package com.svb.struts.service;

import java.util.HashMap;

import com.svb.struts.DAL.DetailsDB;
import com.svb.struts.bean.ProductDetailsBean;

public class DetailsService {
	
	DetailsDB db;
	
	public HashMap getProductNames(){
		db = new DetailsDB();
		return db.getProductNames();
	}
	
	public ProductDetailsBean getProductDetails(String productId){
		db = new DetailsDB();
		return db.getProductDetails(productId);
	}
	
	public HashMap getIngredientNamesWithGlossary(){
		db = new DetailsDB();
		return db.getIngredientNamesWithGlossary();
	}
	
	public HashMap getIngredientNamesWithGlossaryNew(String prodid){
		db = new DetailsDB();
		return db.getIngredientNamesWithGlossaryNew(prodid);
	}
	
	public String[][] getCustomerReviews(String prodid){
		db = new DetailsDB();
		return db.getCustomerReviews(prodid);
	}
	/**
	* Added By Synoptek (10-01-2020)
	* added new method to get category-subcategory combination
	*/
	public String[] getProductCategorySubCategory(String prodid){
		db = new DetailsDB();
		return db.getProductCategorySubCategory(prodid);
	}

}
