/*
 * Created on Apr 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.svb.common.valueobjects;

/**
 * @author mahdav
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CampaignVO {
	private String campaignId;
	private String startDate;
	private String endDate;
	private String currentCampaignId;

	/**
	 * @return Returns the campaignEndDate.
	 */
	public String getEndDate() {
		return endDate;
	}
	/**
	 * @param campaignEndDate The campaignEndDate to set.
	 */
	public void setEndDate(String campaignEndDate) {
		this.endDate = campaignEndDate;
	}
	/**
	 * @return Returns the campaignId.
	 */
	public String getCampaignId() {
		return campaignId;
	}
	/**
	 * @param campaignId The campaignId to set.
	 */
	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}
	/**
	 * @return Returns the campaignStartDate.
	 */
	public String getStartDate() {
		return startDate;
	}
	/**
	 * @param campaignStartDate The campaignStartDate to set.
	 */
	public void setStartDate(String campaignStartDate) {
		this.startDate = campaignStartDate;
	}
	/**
	 * @param b
	 */
	public void setCurentCampaignId(String id) {
		// TODO Auto-generated method stub
		this.currentCampaignId = id;
	}
	public String getCurentCampaignId() {
		// TODO Auto-generated method stub
		return currentCampaignId;
	}
}
