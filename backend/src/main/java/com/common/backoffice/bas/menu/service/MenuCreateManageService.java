package com.common.backoffice.bas.menu.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.backoffice.bas.menu.mapper.MenuCreateManageMapper;
import com.common.backoffice.bas.menu.mapper.MenuInfoManageMapper;
import com.common.backoffice.bas.menu.modals.MenuCreatInfo;
import egovframework.com.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.common.backoffice.bas.uni.service.UtilInfoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
@Service
public class MenuCreateManageService {

	private final MenuCreateManageMapper createMapper;

	private final MenuInfoManageMapper menuMapper;
	
	private final RedisTemplate<String, String> redisTemplate;
	
	private final RedisUtil redis;
	
	
	public List<Map<String, Object>> selectMenuCreatList(final String authorCode) throws Exception {
		return createMapper.selectMenuCreatList_D(authorCode);
	}

	
	public List<Map<String, Object>> selectMenuCreatList_Author(String roleId, String hidMenuGubun) throws Exception {
		return createMapper.selectMenuCreatList_Author(roleId, hidMenuGubun);
	}
	
	public int selectMenuCreatManagTotCnt(MenuCreatInfo searchKeyword) throws Exception {
		return createMapper.selectMenuCreatCnt_S(searchKeyword);
	}

	
	public int selectUsrByPk(final String empNo) throws Exception {
		return createMapper.selectUsrByPk(empNo);
	}

	
	public MenuCreatInfo selectAuthorByUsr(String empNo) throws Exception {
		return createMapper.selectAuthorByUsr(empNo);
	}

	
	public List<Map<String, Object>> selectMenuCreatManagList(Map<String, Object> searchVO) throws Exception {
		return createMapper.selectMenuCreatManageList_D(searchVO);
	}

	@Transactional(readOnly = false)
	public int insertMenuCreatList(String authorCode, String userId, 
									String checkedMenuNoForInsert, String hid_menuGubun, List<Map<String, Object>> menuList) throws Exception {
		try {
			int AuthorCnt = 0;
			
			MenuCreatInfo menuCreatVO = new MenuCreatInfo();
			menuCreatVO.setRoleId(authorCode);
			
			AuthorCnt = createMapper.selectMenuCreatCnt_S(menuCreatVO);
			if (AuthorCnt > 0) {
				createMapper.deleteMenuCreat_S(menuCreatVO);
			}
			if (hid_menuGubun.equals("MENU_GUBUN_1")) {
				
				checkedMenuNoForInsert = checkedMenuNoForInsert.contains(",0") == false ? checkedMenuNoForInsert = checkedMenuNoForInsert.concat(",0") : checkedMenuNoForInsert;
				List<String> insertMenuNo =  UtilInfoService.dotToList(checkedMenuNoForInsert).stream().distinct().collect(Collectors.toList());			
				
				for (String menu : insertMenuNo) {
					menuCreatVO.setRoleId(authorCode);
					menuCreatVO.setMenuNo(menu);
					menuCreatVO.setUserId(userId);
					createMapper.insertMenuCreat_S(menuCreatVO);
				}
			}else {
				for (Map<String, Object> menuBasic: menuList) {
					menuCreatVO.setRoleId(authorCode);
					menuCreatVO.setMenuNo(menuBasic.get("id").toString());
					menuCreatVO.setMenuBasicInfo(menuBasic.get("basicMenu").toString());
					menuCreatVO.setUserId(userId);
					createMapper.insertMenuCreat_S(menuCreatVO);
				}
			}
			//메뉴가 변경 되었을시 Redis Cache 데이터 삭제
			try {
				Map<String, Object> searchCmd = new HashMap<String, Object>();
				searchCmd.put("roleId", authorCode);
				ObjectMapper mapper = new ObjectMapper();

				String jsonValue =  mapper.writeValueAsString(menuMapper.selectMainMenuLeft(authorCode, ""));

				for (Map<String, Object> cache: createMapper.selectMenuCacheList(searchCmd)) {
					String key = "findLeftMenu::"+cache.get("managerId")+"";
					log.info("key:" +key);
					if (redis.isKeyNullCheck(key)== true) {
						redis.keyDelete(key);
					}
					redisTemplate.opsForValue().set(key, jsonValue);
				}
			} catch (Exception e) {
				// Redis 서버가 없어도 메뉴 등록 자체(위 DB 반영)는 이미 끝난 뒤라 실패로 취급하지 않음
				log.warn("Redis unavailable, menu cache refresh skipped: {}", e.toString());
			}

			return 1;
		}catch(NullPointerException e) {
			log.error("insertMenuCreatList service error:" + e.toString());
			return -1;
		}catch(Exception e) {
			log.error("insertMenuCreatList service error:" + e.toString());
			return -1;
		}
		
		
		
	}
	
	@Transactional(readOnly = false)
	public int deleteMenuCreat_S(String authorCode) throws Exception {
		try {
			MenuCreatInfo menuCreatVO = new MenuCreatInfo();
			menuCreatVO.setRoleId(authorCode);
			int ret = createMapper.deleteMenuCreat_S(menuCreatVO);
			
			if (ret > 0) {
				//메뉴가 삭제 되었을시 Redis Cache 데이터 삭제 
				Map<String, Object> searchCmd = new HashMap<String, Object>();
				searchCmd.put("roleId", authorCode);
				for (Map<String, Object> cache: createMapper.selectMenuCacheList(searchCmd)) {
					String key = "findLeftMenu::"+cache.get("managerId")+"";
					log.debug("key:" +key);
					if (redis.isKeyNullCheck(key)== true)
						redis.keyDelete(key);
				}
			}
			return 1;
		}catch(NullPointerException e) {
			log.error("deleteMenuCreat_S service error:" + e.toString());
			return -1;
		}catch (Exception e) {
			log.error("deleteMenuCreat_S error:" + e.toString());
			return -1;
		}
	}
}
