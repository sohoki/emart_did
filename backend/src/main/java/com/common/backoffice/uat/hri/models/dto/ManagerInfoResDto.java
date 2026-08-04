package com.common.backoffice.uat.hri.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자(TB_MANAGERINFO) 상세 조회 응답 DTO.
 * basic/backend(com.common.backoffice.uat.hri.models.dto.ManagerInfoResDto)를 참조해서
 * did_emart 스키마 기준으로 이식함(systemcode_usecode/update_password 제외, centerId 추가).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "ManagerInfoResDto : 관리자 상세 정보")
public class ManagerInfoResDto {

	@Schema(description = "관리자 ID")
	private String managerId;

	@Schema(description = "비밀번호")
	private String managerPassword;

	@Schema(description = "관리자명")
	private String managerName;

	@Schema(description = "사용유무")
	private String useYn;

	@Schema(description = "부서아이디")
	private String partId;

	@Schema(description = "이메일")
	private String managerEmail;

	@Schema(description = "연락처")
	private String managerTel;

	@Schema(description = "최초 등록일")
	private String frstRegistPnttm;

	@Schema(description = "잠김여부")
	private String lockYn;

	@Schema(description = "사번")
	private String empNo;

	@Schema(description = "관리자 상태")
	private String managerStatus;

	@Schema(description = "최초 등록자")
	private String frstRegisterId;

	@Schema(description = "최종 수정자")
	private String lastUpdusrId;

	@Schema(description = "최종 수정일자")
	private String lastUpdtPnttm;

	@Schema(description = "권한 아이디")
	private String roleId;

	@Schema(description = "권한 구분")
	private String roleGubun;

	@Schema(description = "직급")
	private String managerPosition;

	@Schema(description = "사진 파일명")
	private String managerPic;

	@Schema(description = "패스워드 힌트")
	private String passwordHint;

	@Schema(description = "패스워드 답변")
	private String passwordCnsr;

	@Schema(description = "매장(센터) 스코프")
	private String centerId;

	@Schema(description = "삭제 여부")
	private String dltnYn;
}
