package com.common.backoffice.bas.role.modals.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="RoleInfoRequestDto : 권한 정보" )
public class RoleInfoRequestDto {

	@NotBlank(message="등록구분을 입력해 주세요.")
	private String mode;
	
	@NotBlank(message="ROLE ID 입력해 주세요.")
	private String roleId;
	@NotBlank(message="ROLE 명을 입력해 주세요.")
	private String roleName;
	private String userId;
	private String roleDc;
	// TB_ROLEINFO에 SYSTEM_CODE 컬럼이 없어 저장에 쓰이지 않음 — 필수 검증 시 화면에서 값을 안 보내
	// 등록/수정/사용유무 변경이 모두 실패하던 원인이라 @NotBlank 제거
	private String systemCode;
	private String roleUseyn;
	private String frstRegistPnttm;
	private String frstRegisterId;
	private String lastUpdtPnttm;
	private String lastUpdusrId;
	private String roleUserGubun;
}
