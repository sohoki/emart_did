package com.common.backoffice.bas.code.modals.dto;


import jakarta.validation.constraints.NotBlank;

import org.apache.ibatis.type.Alias;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Alias("CmmnCodeReqDto")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor	
@Schema(title="CmmnCodeReqDto : 분류코드 DTO " )
public class CmmnCodeReqDto {

	/**
	* serialVersionUID
	*/
	private static final long serialVersionUID = 1L;
	
	@NotBlank(message="모드를 입력해 주세요.")
	@Schema(description="DB 처리 구분", example="Ins/Edt/Del")
	private String mode;
	
	@NotBlank(message="코드명 입력해 주세요.")	
	@Schema(description="대분류 코드")	
	private String clCode;
	
	@Schema(description="분류 코드")	
	private String codeId;
	
	@Schema(description="코드명")
	private String codeIdNm;
	
	@Schema(description="코드 상세 설명")
	private String codeIdDc;
	
	@Schema(description="사용 유무")
	private String useAt;
	
	@Schema(description="최초 등록자 아이디")
	private String frstRegisterId;
	
	@Schema(description="최종 수정자 아이디")
	private String lastUpdusrId;
	
	@Schema(description="등록자 아이디")
	private String userId;
	
}