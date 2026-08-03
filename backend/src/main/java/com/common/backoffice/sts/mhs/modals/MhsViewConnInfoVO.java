package com.common.backoffice.sts.mhs.modals;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="MhsViewConnInfoVO : 컨텐츠 파일 상세 " )
public class MhsViewConnInfoVO extends MhsViewConnInfo implements Serializable  {

	
	private static final long serialVersionUID = 1L;
	/** 검색조건 */
    private String searchCondition = "";    
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
    
    private String mhsMonitornm;
	private String mhsClassnm;
	private String mhsTeachernm;
	private String mhsClassstartday;
	private String mhsClassendday;
	private String mhsClassdayofweek;
	private String mhsClassstarttime;
	private String mhsClassendtime;
	private String mhsClassstatus;
	private String mhsClassintro;
    private String searchDay;
    private String mhsBrandcd;
    private String mhsCentercd;
    private String mhsNowgubun;
    

    
    
}
