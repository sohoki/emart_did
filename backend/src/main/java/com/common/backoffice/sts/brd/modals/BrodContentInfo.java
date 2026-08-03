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
@Schema(title="BrodContentInfo : 컨텐츠 파일 상세 " )
public class BrodContentInfo {

	    private String brodCode;
	    private String brodName;
	    private String brodTotalTime;
	    private String brodInterval;
	    private String brodUseYn;
	    private String frstRegistPnttm;
		private String lastUpdtPnttm;
		private String frstRegisterId;
		private String lastUpdusrId;
		
		private String codeNm;
		private String code;
		private String mode;
		private String codeDc;
		private String basicFileId;
		private String orignlFileNm;
		private String secGubun;
		private String copBrodCode;
		private String prebrodCode;
		
		private String contentStartDay;
		private String contentEndDay;
		private String centerId;
		private String otherCenterId;
		private String centerAnniverDay;
		
		private String brodChangeInfo;
		private String brodStartDay;
		private String brodEndDay;
		private String basicBrodCode;
		
		private String centerGubun;
		
		//신규 음원 콘텐츠 필요
		private String brodSeq;
		private String atchFileId;		
		private String playTime;
		private String fileThumnail;
		private String intervalSection;
		private String centerNm;
		private String insert_brodCode;
		private String anniversaryTime;
		private String anniversaryStartTime;
		private String tbGubun;
		private String anniversaryGubun;
		private String anniversaryTimeHour;
		private String anniversaryTimeTime;		
		private String anniverName;
		private String centerRelCnt;
		

	
	
	
}
