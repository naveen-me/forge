package com.svb.webservices.model;

import java.util.Arrays;

public class SingleProductScoreModel {
	
	private String mode;
	private String upc;
	private String cusid;
	private String scrname;
	private String skin_type;
	private String skin_concern[] = new String[0];
	private String eye_concern[] = new String[0];
	private String allergy[] = new String[0];
	private String age;
	private String specification[] = new String[0];
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getUpc() {
		return upc;
	}
	public void setUpc(String upc) {
		this.upc = upc;
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
		return "SingleProductScoreModel [mode=" + mode + ", upc=" + upc + ", cusid=" + cusid + ", scrname=" + scrname
				+ ", skin_type=" + skin_type + ", skin_concern=" + Arrays.toString(skin_concern) + ", eye_concern="
				+ Arrays.toString(eye_concern) + ", allergy=" + Arrays.toString(allergy) + ", age=" + age
				+ ", specification=" + Arrays.toString(specification) + ", getMode()=" + getMode() + ", getUpc()="
				+ getUpc() + ", getCusid()=" + getCusid() + ", getScrname()=" + getScrname() + ", getSkin_type()="
				+ getSkin_type() + ", getSkin_concern()=" + Arrays.toString(getSkin_concern()) + ", getEye_concern()="
				+ Arrays.toString(getEye_concern()) + ", getAllergy()=" + Arrays.toString(getAllergy()) + ", getAge()="
				+ getAge() + ", getSpecification()=" + Arrays.toString(getSpecification()) + ", getClass()="
				+ getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
	}
}
