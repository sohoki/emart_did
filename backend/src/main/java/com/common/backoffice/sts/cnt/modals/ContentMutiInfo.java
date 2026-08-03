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
@Schema(title="ContentMutiInfo : 컨텐츠 파일 상세 " )
public class ContentMutiInfo {

	
	
	private String conSeq;
	private String conScreen;
	private String conType;
	private String conUseYn;
	private String conWidth;
	private String conHeight;
	private String conMid;
	
	private String frstRegistPnttm;
	private String lastRegistPnttm;
	private String frstRegisterId;
	private String lastRegisterId;
	
	private String codeNm;
	private String conNm;
	private String mode;
	
	
	private String hisSeq;
	private String schCode;
	
	
	
	private String didId;
	private String didMac;
	private String menuGubun;	
	
	
	private String conTime;
	private String conNextSeq;
	private String conFile;
	
	
	
	private String detailSeq;
	private String detailOrder;	
	private String imageSlidType;
	private String timeIntervalD;
	private String conTypeD;
	
	//신규 
	private String conPlayType;
	private String conBasicUrl;
	private String conUrlType;
    private String conNextTitle;	
	private String conLocalfile;
	
	private String schEmerGubun;
	
	

}
