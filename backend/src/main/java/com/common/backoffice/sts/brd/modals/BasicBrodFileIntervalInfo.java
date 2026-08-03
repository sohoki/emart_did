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
@Schema(title="BasicBrodFileIntervalInfo : 컨텐츠 파일 상세 " )
public class BasicBrodFileIntervalInfo {

	private String brodFileseq;
	private String brodStartday;
	private String brodEndday;
	private String brodStarttime;
	private String brodEndtime;
	private String basicCode;
	private String atchFileId;
	private String useyn;
	private String basicOrder;
	private String frstRegistPnttm;
	private String lastUpdtPnttm;
	private String frstRegisterId;
	private String lastUpdusrId;
	private String mode;
	private String brodPlaycnt;
	private String userId;
	private String groupSeq;

	
}
