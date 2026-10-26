package com.svb.webservices.model;

import java.io.Serializable;
/**
 * 
 * @date 29-Nov-2019
 * @author Synoptek
 *
 */
public class BrandOrProfileAttributeModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -359256906520731828L;
	private String brandOrProfileAttributeId;
	private String brandOrProfileAttributeName;

	public String getBrandOrProfileAttributeId() {
		return brandOrProfileAttributeId;
	}

	public void setBrandOrProfileAttributeId(String brandOrProfileAttributeId) {
		this.brandOrProfileAttributeId = brandOrProfileAttributeId;
	}

	public String getBrandOrProfileAttributeName() {
		return brandOrProfileAttributeName;
	}

	public void setBrandOrProfileAttributeName(String brandOrProfileAttributeName) {
		this.brandOrProfileAttributeName = brandOrProfileAttributeName;
	}

	@Override
	public String toString() {
		return "BrandOrProfileAttributeModel [brandOrProfileAttributeId=" + brandOrProfileAttributeId
				+ ", brandOrProfileAttributeName=" + brandOrProfileAttributeName + "]";
	}

}
