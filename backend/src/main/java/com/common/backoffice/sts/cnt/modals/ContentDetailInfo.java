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
@Schema(title="ContentDetailInfo : 컨텐츠 파일 상세 " )
public class ContentDetailInfo {

	private String detailSeq;
	private String conSeq;
	private String regDate;
	private String conRemark;
	private String detailOrder;
	private String conType;
	private String timeInterval;
	private String mode;
	private String imageSlidtype;
	
	private String pageGubun;
	
	private String didId;
	private String schCode;
	private String hisSeq;
	private String menuGubun;	
	private String conFile;
	
	
	
	
	
	

	
}
