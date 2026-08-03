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
@Schema(title="ContentSendHistoryInfo : DID 정보 상세 " )
public class ContentSendHistoryInfo {

	
	
	private String hisSeq;
	private String didId;
	private String schCode;
	private String didUpdateCheck;
	
	private String conUpdateCheck;
	private String conPageUpdateCheck;
	private String conFileUpdateCheck;
	private String mode;
	private String menuGubun;

	
}
