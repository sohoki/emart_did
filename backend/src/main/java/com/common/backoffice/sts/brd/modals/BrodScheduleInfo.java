package com.common.backoffice.sts.brd.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="BrodScheduleInfo : 컨텐츠 파일 상세 " )
public class BrodScheduleInfo {
	
    private String scheduleSeq;
    private String brodCode;
    private String brodName;
    private String centerId;
    private String brodDay;
    private String createCheck;
    private String createRegDate;
    private String centerNm;
    private String centerStartTime;
    private String centerEndTime;
    private String brodAnnGubun;
    private String didDownCheck;
    private String didDownLoadDate;
    private String basicBrodCode;
    private String didId;
    private String didMac;
    private String fileStreCours;
    private String streFileNm;

       
}
