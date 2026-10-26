package com.svb.struts.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.svb.struts.DAL.IngredientDetailsDB;

public class IngredientSearchService {
	HashMap map = new HashMap();
	ArrayList list;

	public ArrayList getList() {
		return list;
	}

	public void setList(ArrayList list) {
		this.list = list;
	}

	public HashMap getMap() {
		return map;
	}

	public void setMap(HashMap map) {
		this.map = map;
	}
	
	
	
	public HashMap getIngredients(String link, String search) {
		
		IngredientDetailsDB details = new IngredientDetailsDB();
		setMap(details.getIngredientDetails(link, search));
		return getMap();
	}
	
	public ArrayList getIngredients() {
		IngredientDetailsDB details = new IngredientDetailsDB();
		setList(details.getIngredients());
		return getList();
	}
}
