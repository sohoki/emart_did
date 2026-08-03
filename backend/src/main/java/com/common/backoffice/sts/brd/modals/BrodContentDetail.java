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
@Schema(title="BrodContentDetail : 컨텐츠 파일 상세 " )
public class BrodContentDetail {
	private String brodSeq;
	private String brodCode;
	private String atchFileId;
	private String intervalSection;
	private String contentStartDay;
	private String contentEndDay;
	private String frstRegistPnttm;
	private String lastUpdtPnttm;
	private String frstRegisterId;
	private String lastUpdusrId;
	private String mode;
	private String contentOrder;
	private String timeCode;
	private String endTime;
	private String orignlFileNm;
	private String playTime;
	private String fileThumnail;
	private String prebrodCode;
	private String brodSearch;
	private String contentInsert;
	private String contentInsertInterval;
	private String timeIntervalResult;
	private String insert_brodCode;
	private String anniverName;
	private String streFileNm;
	private String fileStreCours;
	
	private String didId;
    private String didMac;
	private String brodDay;
	/*private String centerNm;*/

}
