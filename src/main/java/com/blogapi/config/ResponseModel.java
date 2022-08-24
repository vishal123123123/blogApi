package com.blogapi.config;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseModel {
    
	 private HttpStatus status;
    private int statusCode;
    private String message;
    private Object data;
    private Boolean isSuspensed = Boolean.FALSE;
    
}