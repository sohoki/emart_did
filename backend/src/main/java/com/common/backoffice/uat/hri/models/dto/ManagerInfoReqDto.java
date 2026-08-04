package com.common.backoffice.uat.hri.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자(TB_MANAGERINFO) 등록/수정 요청 DTO.
 * basic/backend(com.common.backoffice.uat.hri.models.dto.ManagerInfoReqDto)를 참조해서
 * did_emart 스키마 기준으로 이식함(systemcode_usecode/update_password는 실제 컬럼이 아니라 제외,
 * centerId는 did_emart 전용 매장 스코프 컬럼이라 추가).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "ManagerInfoReqDto : 관리자 정보 입력/수정 요청 클래스")
public class ManagerInfoReqDto {

	@Schema(description = "DB 처리 구분", example = "Ins/Edt")
	private String mode;

	@Schema(description = "관리자 ID", example = "admin")
	private String managerId;

	@Schema(description = "관리자 ID 중복체크 여부", example = "Y")
	private String idCheck;

	@Schema(description = "비밀번호")
	private String managerPassword;

	@Schema(description = "관리자명")
	private String managerName;

	@Schema(description = "사용유무", defaultValue = "Y")
	private String useYn;

	@Schema(description = "부서아이디")
	private String partId;

	@Schema(description = "이메일")
	private String managerEmail;

	@Schema(description = "연락처")
	private String managerTel;

	@Schema(description = "잠김여부", example = "Y")
	private String lockYn;

	@Schema(description = "사번")
	private String empNo;

	@Schema(description = "관리자 상태(공통코드 COM003)", example = "STATE_01")
	private String managerStatus;

	@Schema(description = "패스워드 힌트")
	private String passwordHint;

	@Schema(description = "패스워드 답변")
	private String passwordCnsr;

	@Schema(description = "권한 아이디")
	private String roleId;

	@Schema(description = "권한 구분")
	private String roleGubun;

	@Schema(description = "직급")
	private String managerPosition;

	@Schema(description = "사진 파일명")
	private String managerPic;

	@Schema(description = "매장(센터) 스코프 — NULL이면 전체 매장 접근")
	private String centerId;

	@Schema(description = "삭제 여부")
	private String dltnYn;

	@Schema(description = "처리자(등록/수정) ID", example = "userId")
	private String userId;
}
