package com.common.backoffice.bas.menu.modals;

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
public class MenuCreatInfo {

    /** 메뉴번호 */
    private String menuNo;
    /** 맵생성ID */
    private String mapCreatId;
    /** 권한코드 */
    private String roleId;
    
    private String systemCode;
    
    private String menuBasicInfo;
    
    private String userId;
}
