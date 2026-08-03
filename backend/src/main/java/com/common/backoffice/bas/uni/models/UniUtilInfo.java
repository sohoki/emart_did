package com.common.backoffice.bas.uni.models;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(title="UniUtilInfo : 쿼리 공통 정보 " )
public class UniUtilInfo {

	@Schema(description = "공통 사용자 테이블", example = "tb_")
	private String inTable;
	
	@Schema(description = "공통 체크 쿼리", example = "check")
	private String inCheckName;
	
	@Schema(description = "공통 조권 쿼리")
	private String inCondition;
	
	@Schema(description = "프로시져 결과값 ")
	private int otCnt;
}
