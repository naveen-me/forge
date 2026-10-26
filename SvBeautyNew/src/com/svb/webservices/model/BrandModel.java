package com.svb.webservices.model;

import java.io.Serializable;
/**
 * 
 * @date 29-Nov-2019
 * @author Synoptek
 *
 */
public class BrandModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8434076070475418545L;
	private String brandId;

	public String getBrandId() {
		return brandId;
	}

	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}

	@Override
	public String toString() {
		return "BrandModel [brandId=" + brandId + "]";
	}

}
