package com.common.backoffice.bas.code.modals.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.apache.ibatis.type.Alias;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Alias("CmmnClCodeReqDto")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor	
@Schema(title="CmmnClCodeReqDto : 대분류 DTO " )
public class CmmnClCodeReqDto {

	
	@NotBlank(message="모드를 입력해 주세요.")
	@Schema(description="DB 처리 구분", example="Ins/Edt/Del")
	private String mode;
	
	@NotBlank(message="대분류를 입력해 주세요.")
	@Schema(description="대분류 코드코 구분")
	@Size(min = 7, max = 7, message = "코드는 7자 이여야 합니다!") 
	private String clCode;
	
	@NotBlank(message="대분류명를 입력해 주세요.")
	@Schema(description="대분류명", example="전자정부 사업")
	private String clCodeNm;
		
	@Schema(description="대분류 상세 살명")
	private String clCodeDc;
	
	@NotBlank(message="사용 유무를 선택해 주세요.")
	@Schema(description="대분류 사용 유무")
	private String useAt;
	
	private String frstRegistPnttm;
	private String frstRegisterId;
	private String lastUpdtPnttm;
	private String lastUpdusrId;
	
	@NotBlank(message="등록자 아이디를 입력해 주세요.")
	@Schema(description="등록자 아이디")
	private String userId;
}