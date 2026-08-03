package com.common.backoffice.sts.mhs.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="MhsCenterInfo : 컨텐츠 파일 상세 " )
public class MhsCenterInfo {
	
	private String mhsBrandcd;
	private String mhsCentercd;
	private String mhsCenternm;
	private String mhsParentcentercd;
	private String mhsCenterregid;
	private String mhsCenterregdate;
	private String mhsCenterupdateid;
	private String mhsCenterupdatedate;
	private String mhsCenterstatus;
	private String mode;
	private String subMode;
	private String centerId;
	private String centerNm;
	


	
	

}
