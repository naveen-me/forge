/*
 * Created on Sep 12, 2005
 *
 */
package com.svb.common.valueobjects;

import java.util.ArrayList;

/**
 * @author Neelima
 *
 */
public class CustomerProductOrderVO {

	private String custId = "";
	private String orderDate = "";
	private String firstName = "";
	private String lastName = "";
	private String email = "";
	private String profileFlag = "";
	private String orderFlag = "";
	private ArrayList orderList = new ArrayList();
	private ArrayList infoList = new ArrayList();
	private ArrayList orderProdIds = new ArrayList();
	private ArrayList infoProdIds = new ArrayList();
	private String count = "";

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getProfileFlag() {
		return profileFlag;
	}

	public void setProfileFlag(String profileFlag) {
		this.profileFlag = profileFlag;
	}

	public String getOrderFlag() {
		return orderFlag;
	}

	public void setOrderFlag(String orderFlag) {
		this.orderFlag = orderFlag;
	}

	public ArrayList getOrderList() {
		return orderList;
	}

	public void setOrderList(ArrayList orderList) {
		this.orderList = orderList;
	}

	public ArrayList getInfoList() {
		return infoList;
	}

	public void setInfoList(ArrayList infoList) {
		this.infoList = infoList;
	}

	public ArrayList getOrderProdIds() {
		return orderProdIds;
	}

	public void setOrderProdIds(ArrayList orderProdIds) {
		this.orderProdIds = orderProdIds;
	}

	public ArrayList getInfoProdIds() {
		return infoProdIds;
	}

	public void setInfoProdIds(ArrayList infoProdIds) {
		this.infoProdIds = infoProdIds;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}
}
