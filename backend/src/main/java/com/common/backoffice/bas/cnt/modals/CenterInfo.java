package com.common.backoffice.bas.cnt.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="CenterInfo : 지점 정보" )
public class CenterInfo {

	private String centerId;
	private String centerNm;
	private String centerZipcode;
	private String centerZipcode1;
	private String centerZipcode2;
	private String centerAddr1;
	private String centerAddr2;
	private String centerTel;
	private String centerFax;
	private String centerUserId;
	private String centerRegdate;
	private String centerUpdateId;
	private String centerUpdateDt;
	private String centerImg;
	private String centerUrl;
	private String centerImgMap;
	private String centerEquipmentCmt;
	private String centerUseYn;
	private String centerLounge;
	private String centerMeetingRoom;
	private String centerInfo;
	private String mode;
	private String codeSeq;	
	private String menuGubun;
	private String centerStartTime;
	private String centerEndTime;
	private String roleCode;
	private String centerGubun;
	private String brodCode;
	private String centerSearchDay;
	private String author_Code;
	
	
    private String subMode;



	 
	
}
