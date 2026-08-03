package com.common.backoffice.sts.brd.modals;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="BasicFileGroupPlayInfo : 컨텐츠 파일 상세 " )
public class BasicFileGroupPlayInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;	

	private String centerId = "";
	private String basicCode = "";
	private String playDay = "";
	private String atchFileId = "";
	private String playCnt = "";
	private String mode = "";
	

	
	
}
