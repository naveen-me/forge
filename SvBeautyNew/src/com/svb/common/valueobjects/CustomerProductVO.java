/*
 * Created on Jan 12, 2006
 *
 */
package com.svb.common.valueobjects;

/**
 * @author Neelima
 *
 */

public class CustomerProductVO {
	private String customerId;
	private String customerFname;
	private String customerLname;
	private String customerEmail;

	private String productId;
	private String productName;
	private String productImage;
	private String productPrice;
	private String productShadeName;
	private String productShadeImage;
	private String productResource;

	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getCustomerFname() {
		return customerFname;
	}
	public void setCustomerFname(String customerFname) {
		this.customerFname = customerFname;
	}

	public String getCustomerLname() {
		return customerLname;
	}
	public void setCustomerLname(String customerLname) {
		this.customerLname = customerLname;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}
	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
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

	public String getProductImage() {
		return productImage;
	}
	public void setProductImage(String productImage) {
		this.productImage = productImage;
	}

	public String getProductPrice() {
		return productPrice;
	}
	public void setProductPrice(String productPrice) {
		this.productPrice = productPrice;
	}

	public String getProductShadeName() {
		return productShadeName;
	}
	public void setProductShadeName(String productShadeName) {
		this.productShadeName = productShadeName;
	}

	public String getProductShadeImage() {
		return productShadeImage;
	}
	public void setProductShadeImage(String productShadeImage) {
		this.productShadeImage = productShadeImage;
	}

	public String getProductResource() {
		return productResource;
	}
	public void setProductResource(String productResource) {
		this.productResource = productResource;
	}

}
