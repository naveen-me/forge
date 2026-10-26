package com.svb.webservices.model;

import java.util.ArrayList;
import java.util.List;

import com.svb.analysis.javabeans.PopupRatings;

public class MoreGreatFunctionGenericTextsDTO {
	
	private String productType;
	private String userSkinType;
	private ArrayList<String> concernOrConditionList;
	private PopupRatings popRate;
	
	public String getProductType() {
		return productType;
	}
	public void setProductType(String productType) {
		this.productType = productType;
	}
	public String getUserSkinType() {
		return userSkinType;
	}
	public void setUserSkinType(String userSkinType) {
		this.userSkinType = userSkinType;
	}
	public List<String> getConcernOrConditionList() {
		return concernOrConditionList;
	}
	public void setConcernOrConditionList(List<String> concernOrConditionList) {
		this.concernOrConditionList = (ArrayList<String>) concernOrConditionList;
	}
	public PopupRatings getPopRate() {
		return popRate;
	}
	public void setPopRate(PopupRatings popRate) {
		this.popRate = popRate;
	}
	
	@Override
	public String toString() {
		return "FunctionGenericTextsDTO [productType=" + productType + ", userSkinType=" + userSkinType
				+ ", concernOrConditionList=" + concernOrConditionList + ", popRate=" + popRate + "]";
	}
	
}
