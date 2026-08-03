package com.common.backoffice.sts.brd.modals;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="BasicFileGroupInfoVO : 컨텐츠 파일 상세 " )
public class BasicFileGroupInfoVO extends BasicFileGroupInfo implements Serializable {
	
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
    private int rnum = 0;
    
    private String filePls = "";
    private String fileMin = "";
    
    private String timeCnt = "";
    private String inutCnt = "";
    
    private String cp_copyGroupSeq = "";
    
    
    



}
