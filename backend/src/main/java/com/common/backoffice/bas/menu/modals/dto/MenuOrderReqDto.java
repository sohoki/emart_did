package com.common.backoffice.bas.menu.modals.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "MenuOrderReqDto : 메뉴 트리 드래그&드롭 이동/순서변경 시 사용하는 DTO — MENU_ORDR/UPPER_MENU_NO만 일괄 반영한다")
public class MenuOrderReqDto {

    @Schema(description = "메뉴 아이디")
    private String menuNo;

    @Schema(description = "변경된 상위 메뉴 아이디 (최상위면 null)")
    private String upperMenuNo;

    @Schema(description = "변경된 메뉴 순서")
    private String menuOrdr;
}
