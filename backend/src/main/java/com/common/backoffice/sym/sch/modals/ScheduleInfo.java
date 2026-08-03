package com.common.backoffice.sym.sch.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="ScheduleInfo : DID 정보 상세 " )
public class ScheduleInfo {

	private String schCode;
	private String schName;
	private String schStartDay;
	private String schEndDay;
	private String groupCode;
	private String groupNm;
	private String contentCode;
	private String contentNm;
	private String schEmerGubun;
	private String schUseYn;
	private String frstRegistPnttm;
	private String lastUpdtPnttm;
	private String frstRegisterId;
	private String lastUpusrId;
	private String codeNm;
	private String mode;
	private String menuGubun;
	private String conNm;
	private String hisSeq;
	
	
	
	
	

	
	
	
}
