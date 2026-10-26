/*
 * Created on May 13, 2005
 *
 */

package com.svb.common.valueobjects;

/**
 * @author Neelima
 *
 */

public class SubscriptionVO {

	private String serviceType = "";
	private String serviceTypeDesc = "";
	private String trialStartDate = "";
	private String trialEndDate = "";
	private String annualStartDate = "";
	private String annualEndDate = "";
	private String paymentDate = "";
	private String paymentType = "";
	private String paymentStatus = "";
	private String checkNum = "";

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceTypeDesc() {
		return serviceTypeDesc;
	}

	public void setServiceTypeDesc(String serviceTypeDesc) {
		this.serviceTypeDesc = serviceTypeDesc;
	}

	public String getTrialStartDate() {
		return trialStartDate;
	}

	public void setTrialStartDate(String trialStartDate) {
		this.trialStartDate = trialStartDate;
	}

	public String getTrialEndDate() {
		return trialEndDate;
	}

	public void setTrialEndDate(String trialEndDate) {
		this.trialEndDate = trialEndDate;
	}

	public String getAnnualStartDate() {
		return annualStartDate;
	}

	public void setAnnualStartDate(String annualStartDate) {
		this.annualStartDate = annualStartDate;
	}

	public String getAnnualEndDate() {
		return annualEndDate;
	}

	public void setAnnualEndDate(String annualEndDate) {
		this.annualEndDate = annualEndDate;
	}

	public String getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getCheckNum() {
		return checkNum;
	}

	public void setCheckNum(String checkNum) {
		this.checkNum = checkNum;
	}

}
