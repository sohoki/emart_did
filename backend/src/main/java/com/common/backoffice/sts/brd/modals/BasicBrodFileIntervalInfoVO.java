package com.common.backoffice.sts.brd.modals;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="BasicBrodFileIntervalInfoVO : 컨텐츠 파일 상세 " )
public class BasicBrodFileIntervalInfoVO extends BasicBrodFileIntervalInfo implements Serializable {

	
	
	private static final long serialVersionUID = 1L;

    private String searchCondition = "";    

    private String searchKeyword = "";    

    private String searchUseYn = "";    

    private int pageIndex = 1;    

    private int pageUnit = 10;    

    private int pageSize = 10;    
    private int firstIndex = 1;
    private int lastIndex = 1;    
    private int recordCountPerPage = 10;
    private String fileGubun = "";
    
    private String cp_copyGroupSeq = "";
    
    private String groupTimeGubun = "";
    private String groupTimeGubunTxt = "";
    private String streFileNm = "";
    private String orignlFileNm = "";
    private String fileStreCours = "";
    

    
    
    
}
