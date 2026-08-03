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
@Schema(title="MhsMonitorInfo : 컨텐츠 파일 상세 " )
public class MhsMonitorInfo {

	private String mhsMonitorcd;
	private String mhsMonitornm;
	private String mhsBrandcd;
	private String mhsCentercd;
	private String mhsMviewtype;
	private String mhsIpaddr;
	private String mhsMacaddr;
	private String mhsMregid;
	private String mhsMregdate;
	private String mhsMupdateid;
	private String mhsMupdatedate;
	private String mhsLastconn;
	private String mhsRemark;
	private String mhsMonitorstatus;
	private String mode;
	

	
	
}
