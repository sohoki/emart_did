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
@Schema(title="BrodOrganization : 컨텐츠 파일 상세 " )
public class BrodOrganization {

	
	private String orgSeq ;
	private String brodCode;
	private String atchFileId;
	
	private String brodTime;
	private String brodGubun;
	private String contentPlayDay;
	private String brodSeq;
	private String brodAnnSeq;
	private String centerId;
	private String orignlFileNm;
	private String streFileNm;
	private String didId;
	private String didMac;
	
	

	
	
}
