package com.svb.struts.service;

import java.util.ArrayList;

import com.svb.struts.DAL.RegimenDetailsDetailsDB;
import com.svb.struts.bean.RegimenDetailsBean;

public class RegimenDetailService {
	public ArrayList getRegimenDetails(String[] productId) {
		ArrayList list = new ArrayList();
		RegimenDetailsDetailsDB details = new RegimenDetailsDetailsDB();
		
		for(int i=0; i<productId.length; i++)
			list.add(details.getRegimenDetails(productId[i]));
		
		return list;
	}
}
