package com.common.backoffice.sys.log.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterfaceInfo {

	private String requstId ;
	private String occrrncDe ;
	private String trsmrcvSeCode ;
	private String integId ;
	private String provdInsttId ;
	private String provdSysId ;
	private String provdSvcId ;
	
	private String requstInsttId ;
	private String requstSysId ;
	private String requstTrnsmitTm ;
	private String requstRecptnTm ;
	private String rspnsTrnsmitTm ;
	private String rspnsRecptnTm ;
	private String resultCode ;
	private String resultMessage ;
	private String frstRegistPnttm ;
	private String rqesterId  ;  //요청자 id
	private String sendMessage ;
	private int ret = 0;
}
