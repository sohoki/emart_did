package com.common.backoffice.sts.cnt.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="ContentInfo : 컨텐츠 파일 상세 " )
public class ContentInfo {

	private String conSeq="";
	private String conType;
	private String conNm;
	private String conPlayType;
	private String conTimeInterval;
	private String conThumbnail;
	private String conUseYn;
	private String conWidth;
	private String conHeight;
	private String conNextConSeq;
	private String frstRegistPnttm;
	private String lastRegistPnttm;
	private String frstRegisterId;
	private String lastRegisterId;
	private String conText;
	private String mode;
	private String code;
	private String codeNm;
	private String conFile;
	private String conGubun;
	private String menuGubun;	

	
}
