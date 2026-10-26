package com.svb.common.valueobjects;



public class BrandVO {

	private String brandId;
	private String brandDescription;
	private String brandName;
	private String brandLogoFile;

	/**
	 * @return Returns the brandDescription.
	 */
	public String getBrandDescription() {
		return brandDescription;
	}
	/**
	 * @param brandDescription The brandDescription to set.
	 */
	public void setBrandDescription(String brandDescription) {
		this.brandDescription = brandDescription;
	}
	/**
	 * @return Returns the brandId.
	 */
	public String getBrandId() {
		return brandId;
	}
	/**
	 * @param brandId The brandId to set.
	 */
	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}
	/**
	 * @return Returns the brandLogoFile.
	 */
	public String getBrandLogoFile() {
		return brandLogoFile;
	}
	/**
	 * @param brandLogoFile The brandLogoFile to set.
	 */
	public void setBrandLogoFile(String brandLogoFile) {
		this.brandLogoFile = brandLogoFile;
	}
	/**
	 * @return Returns the brandName.
	 */
	public String getBrandName() {
		return brandName;
	}
	/**
	 * @param brandName The brandName to set.
	 */
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
}