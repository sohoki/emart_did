package com.common.backoffice.sys.log.models;

public enum sendEnum {

	REQ("REQ", "전송요청"), 
	RES("REO", "전송완료"), 
	REF("REF", "전송실패"), 
	RPQ("RPQ", "수신요청"), 
	RPS("RPS", "수신완료"), 
	RPF("RPF", "수신실패"); 
	
	
	
	
	
	private String code; 
	private String name; 
	sendEnum(String code, String name) { 
		this.code = code; 
		this.name = name; 
	} 
	public String getCode() { 
		return this.code; 
	} 
	public String getName() { 
		return this.name; 
	}
}
