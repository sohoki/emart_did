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
@Schema(title="MhsViewConnInfo : 컨텐츠 파일 상세 " )
public class MhsViewConnInfo {

	
	private String mhsMonitorcd;
	private String mhsClasscd;
	private String mhsDataupdateyn;
	private String mhsDataupdatedate;
	private String mhsDataregid;
	private String mhsDataregdate;
	private String mhsDataupdateid;
	private String mhsDataupdatePlaydate;
	private String mhsDatastatus;
	private String mode;
	private String mhsConnSeq;
	

	
	

}
