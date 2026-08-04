package com.common.backoffice.bas.mark.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "BookMarksInfoReqDto : 개인 즐겨찾기 정보")
public class BookMarksInfoReqDto {

	private String userId;
	private String userGubun;
	private String progrmFileNm;
	private String bookmarksOrder;
	private String bookmarksName;
}
