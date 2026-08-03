package com.common.backoffice.use.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="Group : Group정보" )
public class Group {

	
	private  String groupId ;
	private  String groupNm;
	private  String groupCreatDe;
	private  String groupDc;
	private String parentGroupId ;
	private String useYn ;
	private String lv;
	private String mode;
	private String parentGroupNm;
	private String menuGubun;	
	

}
