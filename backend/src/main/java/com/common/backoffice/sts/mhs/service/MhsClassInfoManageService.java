package com.common.backoffice.sts.mhs.service;

import java.util.List;

import com.common.backoffice.sts.mhs.mapper.MhsClassManageMapper;
import com.common.backoffice.sts.mhs.mapper.MhsViewConnInfoManageMapper;
import com.common.backoffice.sts.mhs.modals.MhsClassInfo;
import com.common.backoffice.sts.mhs.modals.MhsViewConnInfo;
import com.common.backoffice.sts.mhs.modals.MhsClassInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class MhsClassInfoManageService  {
	
	private final MhsClassManageMapper mhsClassMapper;
	private final MhsViewConnInfoManageMapper connMapper;
	
	
	public List<MhsClassInfoVO> selectMhsClassList(MhsClassInfoVO mhsClassInfo) throws Exception {
		// TODO Auto-generated method stub
		return mhsClassMapper.selectMhsClassList(mhsClassInfo);
	}

	
	public int selectMhsClassListCnt(MhsClassInfoVO mhsClassInfo)
			throws Exception {
		// TODO Auto-generated method stub
		return mhsClassMapper.selectMhsClassListCnt(mhsClassInfo);
	}
    public List<MhsClassInfo> selectMhsMoniterClassList(MhsClassInfo vo)
            throws Exception {
        // TODO Auto-generated method stub
        return mhsClassMapper.selectMhsMoniterClassList(vo);
    }
	
	public MhsClassInfo selectMhsClassInfo(String mhsClasscd) throws Exception {
		// TODO Auto-generated method stub
		return mhsClassMapper.selectMhsClassInfo(mhsClasscd);
	}

	/**
	 * 신규 강의(MHS_CLASSCD) 채번. 원본은 EgovIdGnrService(egovUsrCnfrmMhsClassService) 빈을
	 * 사용했으나 did_emart에는 해당 빈이 구성되어 있지 않아 애플리케이션 레벨로 대체 채번함.
	 * 패턴: MCL_ + 16자리 zero-pad 일련번호(MAX+1).
	 */
	@Transactional(readOnly = false)
	public String generateMhsClasscd() throws Exception {
		String maxClasscd = mhsClassMapper.selectMaxMhsClasscd();
		long nextSeq = 1L;
		if (maxClasscd != null && maxClasscd.startsWith("MCL_")) {
			nextSeq = Long.parseLong(maxClasscd.substring("MCL_".length())) + 1;
		}
		return "MCL_" + String.format("%016d", nextSeq);
	}

	/**
	 * 강의 저장 + 모니터 송출 연결(TB_MHSVIEWCONNINFO) 자동 처리.
	 * 강의실(MHS_CLASSROOMNM) = 모니터코드이므로 별도 편성 화면 없이 강의실 모니터에 연결한다(레거시 동작).
	 * 수정 시에는 강의코드 기준으로 연결 모니터를 갱신하고(갱신일시 변경 → 단말 재조회), 연결이 없으면 새로 만든다.
	 */
    @Transactional(readOnly = false)
	public int updateMhsClassInfo(MhsClassInfo vo) throws Exception {
		boolean isInsert = "Ins".equals(vo.getMode());
		int ret = isInsert ? mhsClassMapper.insertMhsClassInfo(vo) : mhsClassMapper.updateMhsClassInfo(vo);

		if (ret > 0 && vo.getMhsClassroomnm() != null && !vo.getMhsClassroomnm().isBlank()) {
			MhsViewConnInfo conn = new MhsViewConnInfo();
			conn.setMhsMonitorcd(vo.getMhsClassroomnm());
			conn.setMhsClasscd(vo.getMhsClasscd());
			conn.setMhsDataregid(isInsert ? vo.getMhsRegid() : vo.getMhsUpdateid());

			if (isInsert || connMapper.updateMoniterClassInfo(conn) == 0) {
				connMapper.insertMoniterClassInfo(conn);
			}
		}
		return ret;
	}

    @Transactional(readOnly = false)
	public int deleteMhsClassInfo(String mhsClasscd) throws Exception {
		// 연결 정보가 남으면 단말 조회 시 고아 데이터가 되므로 함께 삭제
		connMapper.deleteMoniterClassInfoByClass(mhsClasscd);
		return mhsClassMapper.deleteMhsClassInfo(mhsClasscd);
	}

	

	

}
