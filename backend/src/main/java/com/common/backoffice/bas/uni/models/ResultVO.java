package com.common.backoffice.bas.uni.models;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultVO {

	private int resultCode = 0;
	private String resultMessage = "OK";
	private Map<String, Object> result = new HashMap<String, Object>();
}
