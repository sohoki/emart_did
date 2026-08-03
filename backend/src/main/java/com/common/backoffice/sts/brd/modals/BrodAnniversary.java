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
@Schema(title="BrodAnniversary : 컨텐츠 파일 상세 " )
public class BrodAnniversary {

	private String brodAnnSeq;
	private String brodCode;
	private String atchFileId;
	private String anniversaryGubun;
	private String anniverStartDay;
	private String anniverEndDay;
	private String anniversaryTime;
	private String frstRegistPnttm;
	private String lastUpdtPnttm;
	private String frstRegisterId;
	private String lastUpdusrId;
	private String mode;
	private String anniverOrder;
	private String codeNm;
	private String code;
	
	private String anniversaryTimeHour;
	private String anniversaryTimeTime;
	private String anniverName;
	private String secGubun;
	private String prebrodCode;
	private String anniversaryStartTime;
	private String playTime;
	
	private String insert_brodCode;
	private String contentStartDay;
	private String contentEndDay;
	private String brodDay;
	

	
	
	
}
