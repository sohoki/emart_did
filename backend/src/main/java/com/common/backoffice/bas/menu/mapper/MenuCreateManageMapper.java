package com.common.backoffice.bas.menu.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import com.common.backoffice.bas.menu.modals.MenuCreatInfo;


@Mapper
public interface MenuCreateManageMapper {

	public List<Map<String, Object>>selectMenuCreatManageList_D(@Param("params") Map<String, Object> vo);
	
	public int selectMenuCreatManageTotCnt_S(String searchKeyword);
	
	public MenuCreatInfo selectAuthorByUsr(String empNo);
	
	public int selectUsrByPk(String empNo);
	
	public int selectMenuCreatCnt_S(MenuCreatInfo params);
	//cache menu 삭제 설정 
	public List<Map<String, Object>> selectMenuCacheList(@Param("params") Map<String, Object> vo);
	
	public List<Map<String, Object>> selectMenuCreatList_D(String authorCode);
	//tree 메뉴 설정
	public List<Map<String, Object>> selectMenuCreatList_Author(String roleId, String hidMenuGubun);
	//systemMenu 설정
	public List<Map<String, Object>> selectMenuCreatList_System(String roleId, String hidMenuGubun);
	
	public int insertMenuCreat_S(MenuCreatInfo info);
	
	public int updateMenuCreat_S(MenuCreatInfo info);
	
	public int deleteMenuCreat_S(MenuCreatInfo info);
}
