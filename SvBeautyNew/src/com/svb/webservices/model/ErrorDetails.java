package com.svb.webservices.model;

import java.io.Serializable;

/**
 * 
 * @date 29-Nov-2019
 * @author Synoptek
 *
 */
public class ErrorDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4799249857803144187L;
	private int status;
	private String responseMessage;

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the responseMessage
	 */
	public String getResponseMessage() {
		return responseMessage;
	}

	/**
	 * @param responseMessage the responseMessage to set
	 */
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public static ErrorDetails build(int status,String responseMessage) {
		ErrorDetails error = new ErrorDetails();
		error.setStatus(status);
		error.setResponseMessage(responseMessage);
		return error;
	}

	@Override
	public String toString() {
		return "ErrorDetails [status=" + status + ", responseMessage=" + responseMessage + "]";
	}
	

}