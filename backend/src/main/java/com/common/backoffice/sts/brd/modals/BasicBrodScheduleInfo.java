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
@Schema(title="BasicBrodScheduleInfo : 컨텐츠 파일 상세 " )
public class BasicBrodScheduleInfo {

	private String basicScheduleSeq;
	private String centerId;
	private String basicCode;
	private String brodDay;
	private String createCheck;
	private String createRegdate;
	private String didDowncheck;
	private String didDownloaddate;
	private String mode;
	

}
