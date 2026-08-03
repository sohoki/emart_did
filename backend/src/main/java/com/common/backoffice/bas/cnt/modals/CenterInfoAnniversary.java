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
@Schema(title="CenterInfoAnniversary : 지점 정보" )
public class CenterInfoAnniversary {

	
	private String centerAnniday;
	private String centerId;
	private String centerAnniStartDay;
	private String centerAnniEndDay;
	private String startTime;
	private String endTime;
	private String timeOver;
	private String mode;
	private String frstRegistPnttm;
	private String lastUpdtPnttm;
	private String frstRegisterId;
	private String lastUpdusrId;
	private String brodCode;
	private String brodName;
	
	

	
	
}
