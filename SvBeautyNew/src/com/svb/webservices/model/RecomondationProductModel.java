package com.svb.webservices.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @date 29-Nov-2019
 * @author Synoptek
 *
 */
public class RecomondationProductModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1152041623140819555L;

	private String mode;
	private String cusid;
	private String scrname;
	private String skin_type;
	private String skin_concern[] = new String[0];
	private String eye_concern[] = new String[0];
	private String allergy[] = new String[0];
	private String age;
	private String specification[] = new String[0];
	/*
	 * @NotBlank(message = "not.empty")
	 * 
	 * @Pattern(regexp = "skin", message = "wrong.value") private String mode;
	 * 
	 * @Size(min = 9, max = 9, message = "cusid.length")
	 * 
	 * @NotBlank(message = "not.empty") private String cusid;
	 * 
	 * @Pattern(regexp = "general", message = "wrong.value")
	 * 
	 * @NotBlank(message = "not.empty") private String scrname;
	 * 
	 * @NotBlank(message = "not.empty") private String skin_type;
	 * 
	 * @Size(min = 1, message = "size.validation")
	 * 
	 * @NotEmpty(message = "not.empty")
	 * 
	 * @Valid private String skin_concern[] = new String[0];
	 * 
	 * @Size(min = 1, message = "size.validation")
	 * 
	 * @NotEmpty(message = "not.empty")
	 * 
	 * @Valid private String eye_concern[] = new String[0];
	 * 
	 * @Size(min = 1, message = "size.validation")
	 * 
	 * @NotEmpty(message = "not.empty") private String allergy[] = new String[0];
	 * 
	 * @NotBlank(message = "not.empty") private String age;
	 */
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getCusid() {
		return cusid;
	}
	public void setCusid(String cusid) {
		this.cusid = cusid;
	}
	public String getScrname() {
		return scrname;
	}
	public void setScrname(String scrname) {
		this.scrname = scrname;
	}
	public String getSkin_type() {
		return skin_type;
	}
	public void setSkin_type(String skin_type) {
		this.skin_type = skin_type;
	}
	public String[] getSkin_concern() {
		return skin_concern;
	}
	public void setSkin_concern(String[] skin_concern) {
		this.skin_concern = skin_concern;
	}
	public String[] getEye_concern() {
		return eye_concern;
	}
	public void setEye_concern(String[] eye_concern) {
		this.eye_concern = eye_concern;
	}
	public String[] getAllergy() {
		return allergy;
	}
	public void setAllergy(String[] allergy) {
		this.allergy = allergy;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String[] getSpecification() {
		return specification;
	}
	public void setSpecification(String[] specification) {
		this.specification = specification;
	}
	@Override
	public String toString() {
		return "RecomondationProductModel [mode=" + mode + ", cusid=" + cusid + ", scrname=" + scrname + ", skin_type="
				+ skin_type + ", skin_concern=" + Arrays.toString(skin_concern) + ", eye_concern="
				+ Arrays.toString(eye_concern) + ", allergy=" + Arrays.toString(allergy) + ", age=" + age
				+ ", specification=" + Arrays.toString(specification) + "]";
	}	
}
