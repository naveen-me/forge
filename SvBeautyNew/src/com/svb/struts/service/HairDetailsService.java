package com.svb.struts.service;

import java.util.HashMap;

import com.svb.struts.DAL.DetailsDB;
import com.svb.struts.DAL.HairDetailsDB;
import com.svb.struts.bean.ProductDetailsBean;

public class HairDetailsService {
	
	HairDetailsDB db;
	
	public HashMap getProductNames(){
		db = new HairDetailsDB();
		return db.getProductNames();
	}
	
	public ProductDetailsBean getProductDetails(String productId){
		db = new HairDetailsDB();
		return db.getProductDetails(productId);
	}
	
	public HashMap getIngredientNamesWithGlossary(){
		db = new HairDetailsDB();
		return db.getIngredientNamesWithGlossary();
	}
	public String[][] getCustomerReviews(String prodid){
		db = new HairDetailsDB();
		return db.getCustomerReviews(prodid);
	}
}
