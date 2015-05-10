package com.skyseas.openfireplugins.webservice;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Result")
public class BaseResult {
	protected String errorMessage ;
	protected int errorCode;
	protected Object object;
	public BaseResult(){
		
	}
	public BaseResult(int errorCode,String errorMessage){
		this.errorCode=errorCode;
		this.errorMessage=errorMessage;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		this.object = object;
	}
	
}
