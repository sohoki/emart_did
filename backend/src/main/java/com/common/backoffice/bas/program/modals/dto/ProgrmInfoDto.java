package com.common.backoffice.bas.program.modals.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgrmInfoDto {

	//공통
	private String mode;
	private String progrmFileNm;
	private String progrmStrePath;
	private String progrmKoreannm;
	private String progrmDc;
	private String url;
	private String testUrl;
	private String userId;
	private Integer totalRecordCount;	 	// 리스트 총 갯수
	private Integer rnum;	
}
