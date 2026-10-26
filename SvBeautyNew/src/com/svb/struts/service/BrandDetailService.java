package com.svb.struts.service;

import java.util.ArrayList;


import com.svb.struts.DAL.BrandDetailsDB;

import com.svb.struts.bean.BrandDetailsBean;
import com.svb.struts.bean.RegimenDetailsBean;

public class BrandDetailService {
	
	
	public ArrayList getBrandDetails() {
		
		
		ArrayList brands= new ArrayList();
		
	   BrandDetailsDB brandDb= new BrandDetailsDB();
	   
	   brands = brandDb.getBrandList();
	   
	   
		
		return brands;

		
	}
	
public ArrayList getBrandDiscription(String brandId) {
		
		
	   ArrayList brands= new ArrayList();
		
	   BrandDetailsDB brandDb= new BrandDetailsDB();
	   
	   brands = brandDb.getBrandDisc(brandId);
	   
		return brands;

		
	}
}
