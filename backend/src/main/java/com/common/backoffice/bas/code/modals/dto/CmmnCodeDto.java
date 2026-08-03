package com.common.backoffice.bas.code.modals.dto;


import org.apache.ibatis.type.Alias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Alias("CmmnCodeDto")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmmnCodeDto {

	private String clCode;
	private String codeId;
	private String codeIdNm;
	private String codeIdDc;
    private String useAt;
	private String menuGubun;
	// 공통
	private String mode; 			// 등록/수정 식별값 
	private String frstRegisterId; 	// 최초 등록자
	private String lastUpdusrId; 	// 최종 수정자
	private String lastUpdtPnttm;	// 최종 수정시점
	
	private Integer totalRecordCount;	 	// 리스트 총 갯수
	private Integer rnum;					// 행 번호
}
