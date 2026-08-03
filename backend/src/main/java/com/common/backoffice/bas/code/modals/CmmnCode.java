package com.common.backoffice.bas.code.modals;

import java.io.Serializable;

import org.apache.ibatis.type.Alias;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Alias("CmmnCode")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmmnCode implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	private String clCode;
	
	private String codeId;
	@Schema(description="대분류 코드코 구분")
	private String codeIdNm;
	@Schema(description="대분류 코드코 구분")
	private String codeIdDc;
	@Schema(description="대분류 코드코 구분")
    private String useAt;
	@Schema(description="대분류 코드코 구분")
    private String frstRegisterId;
	@Schema(description="대분류 코드코 구분")
    private String lastUpdusrId;
	@Schema(description="사용자 구분")
    private String userId;
	@Schema(description="시스템 코드 구분")
    private String systemCode;
    
}