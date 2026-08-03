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
@Schema(title="MhsClassInfo : 컨텐츠 파일 상세 " )
public class MhsClassInfo {

	private String mhsBrandcd;
	private String mhsCentercd;
	private String mhsClasscd;
	private String mhsClassroomnm;
	private String mhsClassnm;
	private String mhsTeachernm;
	private String mhsClassstartday;
	private String mhsClassendday;
	private String mhsClassdayofweek;
	private String mhsClassstarttime;
	private String mhsClassendtime;
	private String mhsRegid;
	private String mhsRegdate;
	private String mhsUpdateid;
	private String mhsUpdatedate;
	private String mhsClassstatus;
	private String mode;
	private String mhsClassintro;

	
	
}
