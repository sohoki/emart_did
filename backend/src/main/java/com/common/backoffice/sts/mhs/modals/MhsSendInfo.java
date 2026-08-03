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
@Schema(title="MhsSendInfo : 컨텐츠 파일 상세 " )
public class MhsSendInfo {

	
	private String mhsSendcd;
	private String mhsSendtype;
	private String mhsSendinput;
	private String mhsSendoutput;
	private String mhsSenddate;

	
}
