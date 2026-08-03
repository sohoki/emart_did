package com.common.backoffice.bas.cnt.service;


import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.common.backoffice.bas.cnt.mapper.CenterAnniManagerMapper;
import com.common.backoffice.bas.cnt.modals.CenterInfoAnniversary;
import com.common.backoffice.bas.cnt.modals.CenterInfoAnniversaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class CenterAnniManagerService {
	
	
	private final CenterAnniManagerMapper centerAnni;

	
	public List<CenterInfoAnniversaryVO> selectCenterAnniManageListByPagination(
			CenterInfoAnniversaryVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return centerAnni.selectCenterAnniManageListByPagination(searchVO);
	}

	
	public CenterInfoAnniversaryVO selectCenterAnniManageDetail(
			String centerAnniDay) throws Exception {
		// TODO Auto-generated method stub
		return centerAnni.selectCenterAnniManageDetail(centerAnniDay);
	}

	
	public int selectCenterAnniManageListTotCnt_S(
			CenterInfoAnniversaryVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return centerAnni.selectCenterAnniManageListTotCnt_S(searchVO);
	}

    @Transactional(readOnly = false)
	public int insertCenterAnniManage(CenterInfoAnniversary vo)
			throws Exception {
		vo.setCenterAnniday(generateCenterAnniday(vo));
		return centerAnni.insertCenterAnniManage(vo);
	}

	/**
	 * 매장 기념일 ID를 생성한다. 형식: centerId(9) + centerAnniStartDay(8) + 1자리(0~9), 총 18자
	 * (CENTER_ANNIDAY 컬럼 폭과 동일).
	 * 2026-07-27에 DB 함수 FN_CENTERANNICODE(centerid, startday)가 생성된 것을 확인했으나,
	 * 실제 정의를 조회해보니 WHERE절이 매개변수 대신 특정 매장코드('C16102601')로
	 * 하드코딩되어 있어(버그로 추정) 다른 매장에 쓰면 카운트가 항상 그 매장 기준으로만 계산됨.
	 * DB측 수정 전까지는 이 애플리케이션 레벨 채번(재시도 방식)을 그대로 유지함.
	 */
	private String generateCenterAnniday(CenterInfoAnniversary vo) throws Exception {
		String base = vo.getCenterId() + vo.getCenterAnniStartDay();
		for (int i = 0; i < 10; i++) {
			String candidate = base + ThreadLocalRandom.current().nextInt(10);
			if (centerAnni.selectCenterAnniManageDetail(candidate) == null) {
				return candidate;
			}
		}
		throw new IllegalStateException("매장 기념일 ID 채번에 반복적으로 실패했습니다. 잠시 후 다시 시도해 주세요.");
	}

    @Transactional(readOnly = false)
	public int updateCenterAnniManage(CenterInfoAnniversary vo)
			throws Exception {
		// TODO Auto-generated method stub
		return centerAnni.updateCenterAnniManage(vo);
	}

    @Transactional(readOnly = false)
	public int deleteCenterAnniManage(String centerAnniDay) throws Exception {
		// TODO Auto-generated method stub
		return centerAnni.deleteCenterAnniManage(centerAnniDay);
	}

	
	public int selectCenterAnniRetgCheck(CenterInfoAnniversaryVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return centerAnni.selectCenterAnniRetgCheck(searchVO);
	}

    @Transactional(readOnly = false)
	public int deleteCenterID(String centerId) throws Exception {
		// TODO Auto-generated method stub
		return centerAnni.deleteCenterID(centerId);
	}
	
	

}
