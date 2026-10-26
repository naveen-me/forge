package com.svb.webservices.model;

public class FunctionDefinitionsWithGenericText {
	private String concernOrCondition;
	private String functionId;
	private String functionText;
	
	
	public String getConcernOrCondition() {
		return concernOrCondition;
	}
	public void setConcernOrCondition(String concernOrCondition) {
		this.concernOrCondition = concernOrCondition;
	}
	public String getFunctionId() {
		return functionId;
	}
	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}
	public String getFunctionText() {
		return functionText;
	}
	public void setFunctionText(String functionText) {
		this.functionText = functionText;
	}
	
	@Override
	public String toString() {
		return "FunctionDefinitionsWithGenericText [concernOrCondition=" + concernOrCondition + ", functionId="
				+ functionId + ", functionText=" + functionText + "]";
	}
	
}
