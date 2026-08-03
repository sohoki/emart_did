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
@Schema(title="BasicBrodFileInfo : 컨텐츠 파일 상세 " )
public class BasicBrodFileInfo {

	private String basicSeq;
	private String basicCode;
	private String atchFileId;
	private String useYn;
	private String basicOrder;
	private String frstRegistPnttm;
	private String lastUpdtPnttm;
	private String frstRegisterId;
	private String lastUpdusrId;
	private String mode;
	private String fileGubun;
	private String basicStartDay;
	private String basicEndDay;
	private String basicStartTime;
	private String basicEndTime;
	private String basicTimeDiv;
	private String searchDay;
	

}
