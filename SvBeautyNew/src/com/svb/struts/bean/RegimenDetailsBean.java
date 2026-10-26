package com.svb.struts.bean;

public class RegimenDetailsBean {
	String productId;
	String brandName;
	String productName;
	Float price;
	String sellCopy;
	String description;
	String imageMedium;
	
	public RegimenDetailsBean(){
		
	}
	
	public String getImageMedium() {
		return imageMedium;
	}
	public void setImageMedium(String imageMedium) {
		this.imageMedium = imageMedium;
	}
	public String getBrandName() {
		return brandName;
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Float getPrice() {
		return price;
	}
	public void setPrice(float f) {
		this.price = price;
		System.out.println(price);
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getSellCopy() {
		return sellCopy;
	}
	public void setSellCopy(String sellCopy) {
		this.sellCopy = sellCopy;
	}
}
