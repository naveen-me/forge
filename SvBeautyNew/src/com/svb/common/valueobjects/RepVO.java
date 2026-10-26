/*
 * Created on May 4, 2005
 *
 */
package com.svb.common.valueobjects;

/**
 * @author Neelima
 *
 **/
public class RepVO {

	private String repId;
	private String firstName;
	private String lastName;
	private String workPhone;
	private String email;
	private String addressLine1;
	private String addressLine2;
	private String city;
	private String state;
	private String zipcode;
	private String repType;


	/**
	 * @return Returns the addressLine1.
	 */
	public String getAddressLine1() {
		return addressLine1;
	}
	/**
	 * @param addressLine1 The addressLine1 to set.
	 */
	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}
	/**
	 * @return Returns the addressLine2.
	 */
	public String getAddressLine2() {
		return addressLine2;
	}
	/**
	 * @param addressLine2 The addressLine2 to set.
	 */
	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}
	/**
	 * @return Returns the city.
	 */
	public String getCity() {
		return city;
	}
	/**
	 * @param city The city to set.
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return Returns the state.
	 */
	public String getState() {
		return state;
	}
	/**
	 * @param state The state to set.
	 */
	public void setState(String state) {
		this.state = state;
	}
	public void setRepId(String repId){
		this.repId=repId;
	}

	public String getRepId(){
		return repId;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}

	public String getWorkPhone() {
		return workPhone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


	/**
	 * @return Returns the zipcode.
	 */
	public String getZipcode() {
		return zipcode;
	}
	/**
	 * @param zipcode The zipcode to set.
	 */
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	/**
	 * @return Returns the Rep Type.
	 */
	public String getRepType() {
		return repType;
	}
	/**
	 * @param zipcode The zipcode to set.
	 */
	public void setRepType(String reptype) {
		this.repType = reptype;
	}
}
