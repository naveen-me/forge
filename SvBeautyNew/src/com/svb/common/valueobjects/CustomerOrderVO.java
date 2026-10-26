/*
 * Created on Feb 03, 2006
 *
 */
package com.svb.common.valueobjects;

import java.util.ArrayList;

/**
 * @author Neelima
 *
 */
public class CustomerOrderVO {

	private ArrayList profileList = new ArrayList();
	private ArrayList skincareOrderList = new ArrayList();
	private ArrayList colorOrderList = new ArrayList();

	public ArrayList getProfileList() {
		return profileList;
	}
	public void setProfileList(ArrayList profileList) {
		this.profileList = profileList;
	}

	public ArrayList getSkincareOrderList() {
		return skincareOrderList;
	}

	public void setSkincareOrderList(ArrayList skincareOrderList) {
		this.skincareOrderList = skincareOrderList;
	}

	public ArrayList getColorOrderList() {
		return colorOrderList;
	}

	public void setColorOrderList(ArrayList colorOrderList) {
		this.colorOrderList = colorOrderList;
	}

}
