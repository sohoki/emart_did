package com.common.backoffice.bas.code.service;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.backoffice.bas.code.mapper.EgovCmmnDetailCodeManageMapper;
import com.common.backoffice.bas.code.modals.CmmnDetailCode;
import com.common.backoffice.bas.code.modals.dto.CmmnDetailCodeDto;
import com.common.backoffice.bas.code.service.repository.ComtccmmndetailcodeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import egovframework.com.cmm.service.Globals;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional(value = "txManager", readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class EgovCcmCmmnDetailCodeManageService {

	private final EgovCmmnDetailCodeManageMapper detailMapper;
	private final RedisTemplate<String, String> redisTemplate;
	private final ComtccmmndetailcodeRepository repository;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	public List<CmmnDetailCodeDto> selectCmmnDetailCodeListByPagination(String codeId){
		return detailMapper.selectCmmnDetailCodeListByPagination(codeId );
	}
	
	public List<CmmnDetailCodeDto> selectCmmnDetailCodeList(String codeId){
		return detailMapper.selectCmmnDetailCodeListByPagination(codeId);
	}
	
	
	public List<CmmnDetailCodeDto> selectCmmnDetailCombo (String code){
		return detailMapper.selectCmmnDetailCombo(code);
	}
	
	public List<CmmnDetailCodeDto> selectCmmnDetailComboLamp(String code) {
		// 1. Redis에서 데이터 가져오기 (Object 타입을 String으로 명시적 형변환)
		Object cachedData;
		try {
			cachedData = redisTemplate.opsForValue().get(Globals.DETAIL_CODE_KEY);
		} catch (Exception e) {
			// Redis 서버가 없어도 콤보 조회 자체는 DB로 대체해서 계속 진행되게 함
			log.warn("Redis unavailable, falling back to DB: {}", e.toString());
			return detailMapper.selectCmmnDetailComboLamp(code);
		}

		if (cachedData == null) {
			log.info("============= Redis 데이터 없음: DB 조회 실행");
			return detailMapper.selectCmmnDetailComboLamp(code);
		}

		try {
			String jsonCodes = String.valueOf(cachedData);
			
			// 2. JSON 문자열을 바로 Dto 리스트로 역직렬화 (Map 단계를 거치지 않아 더 효율적)
			List<CmmnDetailCodeDto> allList = objectMapper.readValue(jsonCodes, new TypeReference<List<CmmnDetailCodeDto>>() {});
			log.info("============= Redis 데이터로 전송");
			return allList.stream()
					.filter(dto -> {
						if (dto.getCode() == null || code == null) return false;
						// 공백 제거 후 비교
						return dto.getCodeId().trim().equals(code.trim());
					})
					.collect(Collectors.toList());
	
		} catch (Exception e) {
			log.error("Redis 데이터 파싱 에러 - DB로 대체 실행: {}", e.getMessage());
			// 에러 발생 시 시스템 중단 방지를 위해 DB에서 조회
			return detailMapper.selectCmmnDetailComboLamp(code);
		}
	}
	
	public List<CmmnDetailCodeDto> selectCmmnDetailComboEtc(String code){
		return detailMapper.selectCmmnDetailComboEtc(code);
	}
	
	public CmmnDetailCodeDto selectCmmnDetailCodeDetail(String code) {
		return  detailMapper.selectCmmnDetailCodeDetail(code);
	}
	
	public CmmnDetailCodeDto selectCmmnDetail(String code){
		return detailMapper.selectCmmnDetail(code);
	}

	@Transactional(readOnly = false)
	public int updateCmmnDetailCode(CmmnDetailCode vo) {
		
		try {
			int result = Globals.SAVE_MODE_INSERT.equals(vo.getMode()) 
							? detailMapper.insertCmmnDetailCode(vo) 
							: detailMapper.updateCmmnDetailCode(vo);
	
			// 2. 결과가 0보다 크면(성공) Redis 갱신
			if (result > 0) {
				commonCodeRedisRest();
			}
			return result;
		}catch(Exception e) {
			
			return -1;
		}
		 
	}
	@Transactional(readOnly = false)
	public int deleteCmmnDetailCode(String code) {
		try {
			int ret = detailMapper.deleteCmmnDetailCode(code);
			if (ret > 0) {
				commonCodeRedisRest();
			}
			return ret;
		}catch(Exception e) {
			return -1;
		}
		 
	}
	@Transactional(readOnly = false)
	public int deleteCmmnDetailCodeId(String value) {
		try {
			int ret = detailMapper.deleteCmmnDetailCodeId(value);
			if (ret > 0) {
				commonCodeRedisRest();
			}
			return ret;
		}catch(Exception e) {
			return -1;
		}
	}
	//redis 캐시 재 설정
	private boolean commonCodeRedisRest() {
		try {
			List<Map<String, Object>> list = detailMapper.selectCmmnDetailList();
			String jsonCodes = objectMapper.writeValueAsString(list);
			redisTemplate.opsForValue().set(Globals.DETAIL_CODE_KEY, jsonCodes);
			return true;
		}catch(JsonProcessingException  e) {
			log.error("Redis 캐시 갱신 실패", e);
			return false;
		}catch(Exception e) {
			// Redis 서버가 없어도 호출부(등록/수정/삭제)가 -1(실패)로 오해하지 않게 여기서
			// 삼킴 — DB 쓰기 자체는 이미 성공한 뒤에 캐시만 갱신하는 단계이기 때문
			log.warn("Redis unavailable, common code cache update skipped: {}", e.toString());
			return false;
		}
	}
	
	public List<CmmnDetailCodeDto> selectCmmnDetailResTypeCombo (Map<String, Object> vo){
		return detailMapper.selectCmmnDetailResTypeCombo(vo);
	}
    @Transactional("jpaTransactionManager")
    public int updateUseAtOnly(CmmnDetailCode vo) {
        try {

            int ret  = repository.updateUseAtOnly(vo.getUseAt(), vo.getUserId(), vo.getCodeId(),  vo.getCode());
            // 사용유무 변경도 콤보 캐시에 반영 (기존에는 미갱신 → 콤보에 stale 데이터 노출)
            if (ret > 0) {
                commonCodeRedisRest();
            }
            return ret;
        }catch(Exception e) {

            log.error("updateUseAtOnly error:"+ e.toString());
            return -1;
        }
    }
	
}
