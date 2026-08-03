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
@Schema(title="BrodContentDetailTime : 컨텐츠 파일 상세 " )
public class BrodContentDetailTime {
	
	
	private String imsi_seq;
	private String brodCode;
	private String atchFileId;
	private String intervalSection;
	private String contentStartDay;
	private String contentEndDay;

	

}
