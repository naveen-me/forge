package com.svb.common.valueobjects;

import java.util.ArrayList;
import java.util.Hashtable;


/*
 * // header - edit "Data/yourJavaHeader" to customize // contents - edit
 * "EventHandlers/Java file/onCreate" to customize //
 */
public class CustomerProfileVO {
	String customerId   		= "";
	String gender 				= "";
	String deliveryBase 		= "";
	String skinType 	  		= "";
	String age 					= "";
	String ethnicity            = "" ;
	String natural              = "" ;
	String customerName 		= "";
	String score 				= "";
	String customerEmail 		= "";
	ArrayList eyeConcernList 	= new ArrayList();
	ArrayList skinConditionList = new ArrayList();
	ArrayList skinConcernList 	= new ArrayList();
	ArrayList allergyList 		= new ArrayList();
	ArrayList specification 	= new ArrayList();

	ArrayList hbconcernList 	= new ArrayList();
	ArrayList productusage 		= new ArrayList();
	Hashtable profileHT 		= new Hashtable();
	private String centrydate;

	String hairtype = "" ;
    String haircolor = "" ;
    ArrayList hairprocess = new ArrayList();
    String hairthickness = "" ;
    String hairtexture = "" ;
    ArrayList hairconditions = new ArrayList();
    ArrayList hairstyles = new ArrayList();
    String hairfinish = "" ;
	/**
	 * @return Returns the haircolor.
	 */
	public String getHaircolor() {
		return haircolor;
	}
	/**
	 * @param haircolor The haircolor to set.
	 */
	public void setHaircolor(String haircolor) {
		this.haircolor = haircolor;
	}
	/**
	 * @return Returns the hairconditions.
	 */
	public ArrayList getHairconditions() {
		return hairconditions;
	}
	/**
	 * @param hairconditions The hairconditions to set.
	 */
	public void setHairconditions(ArrayList hairconditions) {
		this.hairconditions = hairconditions;
	}
	/**
	 * @return Returns the hairfinish.
	 */
	public String getHairfinish() {
		return hairfinish;
	}
	/**
	 * @param hairfinish The hairfinish to set.
	 */
	public void setHairfinish(String hairfinish) {
		this.hairfinish = hairfinish;
	}
	/**
	 * @return Returns the hairprocess.
	 */
	public ArrayList getHairprocess() {
		return hairprocess;
	}
	/**
	 * @param hairprocess The hairprocess to set.
	 */
	public void setHairprocess(ArrayList hairprocess) {
		this.hairprocess = hairprocess;
	}
	/**
	 * @return Returns the hairstyles.
	 */
	public ArrayList getHairstyles() {
		return hairstyles;
	}
	/**
	 * @param hairstyles The hairstyles to set.
	 */
	public void setHairstyles(ArrayList hairstyles) {
		this.hairstyles = hairstyles;
	}
	/**
	 * @return Returns the hairtexture.
	 */
	public String getHairtexture() {
		return hairtexture;
	}
	/**
	 * @param hairtexture The hairtexture to set.
	 */
	public void setHairtexture(String hairtexture) {
		this.hairtexture = hairtexture;
	}
	/**
	 * @return Returns the hairthickness.
	 */
	public String getHairthickness() {
		return hairthickness;
	}
	/**
	 * @param hairthickness The hairthickness to set.
	 */
	public void setHairthickness(String hairthickness) {
		this.hairthickness = hairthickness;
	}
	/**
	 * @return Returns the hbconcernList.
	 */
	public ArrayList getHbconcernList() {
		return hbconcernList;
	}
	/**
	 * @param hbconcernList The hbconcernList to set.
	 */
	public void setHbconcernList(ArrayList hbconcernList) {
		this.hbconcernList = hbconcernList;
	}
	/**
	 * @param skinConditionList The skinConditionList to set.
	 */
	public void setSkinConditionList(ArrayList skinConditionList) {
		this.skinConditionList = skinConditionList;
	}


	/**
	 * @return Returns the age.
	 */
	public String getAge() {
		return age;
	}
	/**
	 * @param age The age to set.
	 */
	public void setAge(String age) {
		this.age = age;
	}
	/**
	 * @return Returns the allergyList.
	 */
	public ArrayList getAllergyList() {
		return allergyList;
	}
	/**
	 * @param allergyList The allergyList to set.
	 */
	public void setAllergyList(ArrayList allergyList) {
		this.allergyList = allergyList;
	}
	/**
	 * @return Returns the customerEmail.
	 */
	public String getCustomerEmail() {
		return customerEmail;
	}
	/**
	 * @param customerEmail The customerEmail to set.
	 */
	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}
	/**
	 * @return Returns the customerId.
	 */
	public String getCustomerId() {
		return customerId;
	}
	/**
	 * @param customerId The customerId to set.
	 */
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * @return Returns the customerName.
	 */
	public String getCustomerName() {
		return customerName;
	}
	/**
	 * @param customerName The customerName to set.
	 */
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	/**
	 * @return Returns the deliveryBase.
	 */
	public String getDeliveryBase() {
		return deliveryBase;
	}
	/**
	 * @param deliveryBase The deliveryBase to set.
	 */
	public void setDeliveryBase(String deliveryBase) {
		this.deliveryBase = deliveryBase;
	}
	/**
	 * @return Returns the environmentList.
	 */
	public ArrayList getProductUsage() {
		return productusage;
	}
	/**
	 * @param environmentList The environmentList to set.
	 */
	public void setProductUsage(ArrayList productusage) {
		this.productusage = productusage;
	}
	/**
	 * @return Returns the profileHT.
	 */
	public Hashtable getProfileHT() {
		return profileHT;
	}
	/**
	 * @param profileHT The profileHT to set.
	 */
	public void setProfileHT(Hashtable profileHT) {
		this.profileHT = profileHT;
	}
	/**
	 * @return Returns the regimenHT.
	 */
	/**
	 * @return Returns the score.
	 */
	public String getScore() {
		return score;
	}
	/**
	 * @param score The score to set.
	 */
	public void setScore(String score) {
		this.score = score;
	}
	/**
	 * @return Returns the skinconcernList.
	 */
	public ArrayList getSkinConcernList() {
		return skinConcernList;
	}
	/**
	 * @param skinconcernList The skinconcernList to set.
	 */
	public void setSkinConcernList(ArrayList skinconcernList) {
		this.skinConcernList = skinconcernList;
	}
	/**
	 * @return Returns the skinconditionList.
	 */
	public ArrayList getSkinConditionList() {
		return skinConditionList;
	}
	/**
	 * @param skinconditionList The skinconditionList to set.
	 */
	public void setSkinconditionList(ArrayList skinconditionList) {
		this.skinConditionList = skinconditionList;
	}
	/**
	 * @return Returns the skinType.
	 */
	public String getSkinType() {
		return skinType;
	}
	/**
	 * @param skinType The skinType to set.
	 */
	public void setSkinType(String skinType) {
		this.skinType = skinType;
	}
	/**
	 * @param string
	 */
	public void setCustomerEntryDate(String centrydate) {
		this.centrydate = centrydate ;
	}
	public String getCustomerEntryDate()
	{
		return centrydate ;
	}


	public void setEthnicity(String ethnicity) {
		// TODO Auto-generated method stub
		this.ethnicity = ethnicity ;
	}
	public String getEthnicity()
	{
		return this.ethnicity ;
	}
	public void setNatural(String natural) {
		// TODO Auto-generated method stub
		this.natural = natural ;
	}
	public String getNatural()
	{
		return this.natural ;
	}
	public ArrayList getEyeConcernList() {
		return eyeConcernList;
	}
	/**
	 * @param skinconcernList The skinconcernList to set.
	 */
	public void setEyeConcernList(ArrayList eyecernList) {
		this.eyeConcernList = eyecernList;
	}
	/**
	 * @return Returns the hairtype.
	 */
	public String getHairtype() {
		return hairtype;
	}
	/**
	 * @param hairtype The hairtype to set.
	 */
	public void setHairtype(String hairtype) {
		this.hairtype = hairtype;
	}
	/**
	 * @return the specification
	 */
	public ArrayList getSpecification() {
		return specification;
	}
	/**
	 * @param specification the specification to set
	 */
	public void setSpecification(ArrayList specification) {
		this.specification = specification;
	}
	
}

