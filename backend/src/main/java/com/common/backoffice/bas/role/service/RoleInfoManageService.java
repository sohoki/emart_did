package com.common.backoffice.bas.role.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.common.backoffice.bas.role.service.repository.RoleTRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.backoffice.bas.role.mapper.RoleInfoManageMapper;
import com.common.backoffice.bas.role.modals.RoleInfo;
import com.common.backoffice.bas.role.modals.dto.RoleInfoRequestDto;
import egovframework.com.util.RedisUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import egovframework.com.cmm.service.Globals;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional(value = "txManager", readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class RoleInfoManageService {

	private final RoleInfoManageMapper roleMapper;
    private final RoleTRepository repository;
	
	private final RedisTemplate<String, String> redisTemplate;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	private RedisUtil redisUtil;
	

	public List<Map<String, Object>> selectRoleInfoPageList(Map<String, Object> params){
		return roleMapper.selectRoleInfoPageList(params);
	}

    public List<Map<String, Object>> selectRoleInfoComboList(Map<String, Object> params) {
        Object cachedData;
        try {
            cachedData = redisTemplate.opsForValue().get(Globals.ROLE_KEY);
        } catch (Exception e) {
            // Redis 서버가 없어도 콤보 조회 자체는 DB로 대체해서 계속 진행되게 함
            log.warn("Redis unavailable, falling back to DB: {}", e.toString());
            return roleMapper.selectRoleInfoComboList(params);
        }

        if (cachedData == null) {
            log.info("============= Redis 데이터 없음: DB 조회 실행");
            return roleMapper.selectRoleInfoComboList(params);
        }

        try {
            String jsonCodes = String.valueOf(cachedData);

            // JSON 문자열을 Dto(Map) 리스트로 역직렬화
            List<Map<String, Object>> allList = objectMapper.readValue(jsonCodes, new TypeReference<List<Map<String, Object>>>() {});
            log.info("============= Redis 데이터로 전송");

            return allList.stream()
                    .filter(dto -> {
                        // 1. 고정 조건 체크: ROLE_USEYN == 'Y'
                        Object roleUseYn = dto.get("ROLE_USEYN");
                        if (roleUseYn == null || !"Y".equals(String.valueOf(roleUseYn))) {
                            return false;
                        }

                        // 2. 동적 조건 체크: searchRoleGubun 파라미터가 있을 경우 ROLE_USER_GUBUN과 비교
                        if (params != null && params.containsKey("searchRoleGubun")) {
                            Object searchRoleGubun = params.get("searchRoleGubun");

                            // StringUtils.isEmpty 처리 (null이거나 빈 값이 아닐 때만 검사)
                            if (searchRoleGubun != null && !String.valueOf(searchRoleGubun).trim().isEmpty()) {
                                Object roleUserGubun = dto.get("ROLE_USER_GUBUN"); // DTO(캐시)에서는 컬럼명으로 꺼냄

                                if (roleUserGubun == null || !String.valueOf(roleUserGubun).equals(String.valueOf(searchRoleGubun))) {
                                    return false;
                                }
                            }
                        }

                        // 위 조건들을 모두 통과하면 true
                        return true;
                    })
                    // 3. (선택 사항) 쿼리의 SELECT A.ROLE_ID, A.ROLE_NAME 처럼 필요한 컬럼만 리턴하고 싶을 경우
                    .map(dto -> {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("ROLE_ID", dto.get("ROLE_ID"));
                        resultMap.put("ROLE_NAME", dto.get("ROLE_NAME"));
                        return resultMap;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Redis 데이터 파싱 에러 - DB로 대체 실행: {}", e.getMessage());
            // 에러 발생 시 시스템 중단 방지를 위해 DB에서 조회
            return roleMapper.selectRoleInfoComboList(params);
        }
    }
	
	public Optional<RoleInfo> selectRoleInfoDetail(String roleId) {
		return roleMapper.selectRoleInfoDetail(roleId);
	}

    public boolean existsByRoleId(String roleId) {
        return repository.existsByRoleId(roleId);
    }

	@Transactional(readOnly = false)
	public int updateRoleInfo(RoleInfoRequestDto vo) {
		try {
			int result = Globals.SAVE_MODE_INSERT.equals(vo.getMode()) 
							? roleMapper.insertRoleInfo(vo) 
							: roleMapper.updateRoleInfo(vo);
	
			// 2. 결과가 0보다 크면(성공) Redis 갱신
			if (result > 0) {
				Map<String, Object> params = new HashMap<String, Object>();
				redisUtil.commonCodeRedisRest(roleMapper.selectRoleInfoComboList(params), Globals.ROLE_KEY);
			}
			return result;
		}catch(Exception e) {
			log.error("updateRoleInfo error:", e);
			return -1;
		}

	}

    @Transactional("jpaTransactionManager")
    public int updateRoleUseynInfo(RoleInfoRequestDto vo) {
        return roleMapper.updateRoleUseynInfo(vo);

    }
	@Transactional("jpaTransactionManager")
	public int deleteRoleInfo(String roleId) {
		int ret = 0;
		try {
            repository.deleteByRoleId(roleId);
			if (ret > 0) {
				Map<String, Object> params = new HashMap<String, Object>();
				redisUtil.commonCodeRedisRest(roleMapper.selectRoleInfoComboList(params), Globals.ROLE_KEY);
			}
			ret = 1;
		}catch(Exception e) {
			log.error("deleteRoleInfo error:" + e.toString());
		}
		return ret;
	}
	
}
