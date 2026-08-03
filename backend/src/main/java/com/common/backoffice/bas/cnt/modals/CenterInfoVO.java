package com.common.backoffice.bas.cnt.modals;


import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="CenterInfoAnniversary : 지점 정보" )
public class CenterInfoVO extends  CenterInfo implements Serializable{
	
	
	private static final long serialVersionUID = 1L;
	/** 검색조건 */
    private String searchCondition = "";    
    /** 검색Keyword */
    private String searchKeyword = "";    
    private String selectRoleCode = "";
    /** 검색사용여부 */
    private String searchUseYn = "";    
    /** 현재페이지 */
    private int pageIndex = 1;    
    /** 페이지갯수 */
    private int pageUnit = 10;    
    /** 페이지사이즈 */
    private int pageSize = 10;    
    private int firstIndex = 1;
    private int lastIndex = 1;    
    private int recordCountPerPage = 10;
    
	/** 권한 */
	private String authorCode;
	/** 부서ID */
	private String groupId;
	/** 사용자ID */
	private String mberId;
	/** 문화센터 부서코드 */
	private String mhsBrandcd;
	/** 문화센터 점포명 */
	private String mhsCentercd;
	/** 문화센터 점포명 */
	private String mhsCenternm;
	/** 문화센터 모드 */
	private String mode;
	/** 문화센터 사용 유무*/
	private String mhsCenterstatusTxt;
    
	/** Que */
	private String mhsParentbrandcd;
    private String mhsBrandlv;
    private String mhsBrandnm;


}
