package com.common.backoffice.bas.menu.modals.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuInfoRequestDto {

    @NotBlank(message="업데이트 구분을 입력해 주세요.")
    @Schema(description="DB 처리 구분", example="Ins/Edt/Del")
    private String mode;

    @Schema(description="메뉴명", example="123") // 제약에 맞춰 example도 숫자로 변경하는 것을 추천합니다.
    @Pattern(regexp = "^[0-9]+$", message = "숫자만 입력 가능합니다.")
    private String menuNo = "";

    @NotBlank(message="메뉴 아이디을 입력해 주세요.")
    @Schema(description="메뉴명")
    private String menuNm = "";

    @NotBlank(message="프로그램을 선택해 주세요.")
    @Schema(description="프로그램명")
    private String progrmFileNm  = "";
    @Schema(description="프로그램 한글")
    private String progrmKoreannm = "";
    private String upperMenuNo  = "";
    private String upperMenuNm  = "";
    @Schema(description = "메뉴 순서", example = "1")
    @NotBlank(message = "메뉴 순서를 입력해 주세요.")
    @Pattern(regexp = "^[0-9]+$", message = "메뉴 순서는 숫자만 입력 가능합니다.")
    private String menuOrdr  = "";

    @Schema(description="메뉴 상세 설명")
    private String menuDc  = "";

    @Schema(description = "메뉴 사용 여부", example = "Y", defaultValue = "Y")
    @Pattern(regexp = "^[YN]$", message = "사용 여부는 Y 또는 N만 입력 가능합니다.")
    private String menuUseyn  = "";
    private String relateImagePath = "";
    private String relateImageNm = "";

    @Schema(description="등록자 아이디")
    private String userId;
    @Schema(description="팝업 연결 구분")
    private String menuPageTarget;
    @Schema(description="팝업 구분")
    private String menuPopupnfo;
    @Schema(description="개인정보 취급 여부")
    private String menuPrivacy;
    //신규
    @Schema(description="아이콘 타입")
    private String menuIconType;

    @Schema(description="아이콘 class")
    private String menuClass;

    private int cnt = 0;
}
