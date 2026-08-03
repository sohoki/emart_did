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
@Schema(title="BasicFileGroupInfo : 컨텐츠 파일 상세 " )
public class BasicFileGroupInfo {

	private String groupSeq = "";
	private String basicCode = "";	               
	private String groupStarttime = "";
	private String groupEndtime = "";
	private String groupTitle = "";
	private String frstRegistPnttm ="";
	private String lastUpdtPnttm ="";
	private String frstRegisterId ="";
	private String lastUpdusrId ="";
	private String mode = "";
	private String userId = "";
	private int fileCnt =  0;
	private String groupTimegubun = "";
	
	

	
}
