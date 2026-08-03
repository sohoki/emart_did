package com.common.backoffice.sts.pic.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="DidMoniterPic : 컨텐츠 파일 상세 " )
public class DidMoniterPic {

	private String didId;
	private String didMac;
	private String didNm;
	private String didRegDate;
	private String didFileNm;
	
	private String menuGubun;
	private String msgSeq;
	

	
	
}
