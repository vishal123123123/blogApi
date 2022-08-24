package com.blogapi.config;

import org.springframework.http.HttpStatus;

public class ResponseStatus {
	
	 public static ResponseModel create(String message, Object obj, HttpStatus httpStatus, int httpStatusCode) {
	        ResponseModel rs = new ResponseModel();
	        rs.setMessage(message);
	        rs.setData(obj);
	        rs.setStatus(httpStatus);
	        rs.setStatusCode(httpStatusCode);
	        return rs;
	    }
	    
	    public static ResponseModel createforImport(String message, Object obj, HttpStatus httpStatus, int httpStatusCode, Boolean isSuspensed) {
	        ResponseModel rs = new ResponseModel();
	        rs.setMessage(message);
	        rs.setData(obj);
	        rs.setStatus(httpStatus);
	        rs.setStatusCode(httpStatusCode);
	        rs.setIsSuspensed(isSuspensed);
	        return rs;
	    }

}
