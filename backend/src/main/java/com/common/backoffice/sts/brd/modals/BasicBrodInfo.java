package com.common.backoffice.sts.brd.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="BasicBrodInfo : 컨텐츠 파일 상세 " )
public class BasicBrodInfo {

	private String basicCode;
	private String basicCodePre;
	private String basicGroupNm;
	private String basicGroupCnt;
	private String frstRegistPnttm;
	private String lastUpdtPnttm;
	private String frstRegisterId;
	private String lastUpdusrId;
	private String mode;
	
	
	

	
	
}
