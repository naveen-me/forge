package com.svb.webservices.model;

import java.util.List;

public class RecProductAvailibility {
	private List<ProductStatusModel> productAvailability;

	/**
	 * @return the productAvailability
	 */
	public List<ProductStatusModel> getProductAvailability() {
		return productAvailability;
	}

	/**
	 * @param productAvailability the productAvailability to set
	 */
	public void setProductAvailability(List<ProductStatusModel> productAvailability) {
		this.productAvailability = productAvailability;
	}

}
