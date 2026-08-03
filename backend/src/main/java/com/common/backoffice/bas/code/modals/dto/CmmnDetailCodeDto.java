package com.common.backoffice.bas.code.modals.dto;


import org.apache.ibatis.type.Alias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Alias("CmmnDetailCodeDto")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CmmnDetailCodeDto {

	private String clCode;
	private String clCodeNm;
	private String codeId;
	private String codeIdNm;
	private String code;
	private String codeNm;
	private String codeDc;
	private String useAt;
	private String menuGubun;
	private String searchCodedc;
	private String codeEtc1;
	private String codeEtc2;

	// 공통
	private String mode; 			// 등록/수정 식별값 
	private String frstRegisterId; 	// 최초 등록자자
	private String lastUpdusrId; 	// 최종 수정자
	private String lastUpdtPnttm;	// 최종 수정시점
	
	private Integer totalRecordCount;	 	// 리스트 총 갯수
	private Integer rnum;					// 행 번호
}