package com.common.backoffice.sts.cnt.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="ContentMutiInfo : 컨텐츠 파일 상세 " )
public class ContentMutiInfoVO extends ContentMutiInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	/** 검색조건 */
    private String searchCondition = "";    
	/** 정렬 컬럼(그리드 colId) */
	private String sortField = "";
	/** 정렬 방향(asc/desc) */
	private String sortOrder = "";
    /** 검색Keyword */
    private String searchKeyword = "";    
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
    
    private String conDc ;
    /** 연동스케줄갯수*/
    private String schCnt;
	/** 권한 */
	private String authorCode;
	/** 부서ID */
	private String groupId;
	/** 지점ID */
	private String centerId;
	/** 사용자ID */
	private String mberId;
	/** 화면 목록 브랜드(그룹) 필터 — 하위 그룹 포함, 문화센터 룸 관리 화면 관리 탭에서 사용 */
	private String searchGroupId;
	/** 화면 목록 매장(센터) 필터 */
	private String searchCenterId;

}
