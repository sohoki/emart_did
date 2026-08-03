package com.common.backoffice.sts.mhs.service;

import java.util.List;

import com.common.backoffice.sts.mhs.mapper.MhsClassManageMapper;
import com.common.backoffice.sts.mhs.modals.MhsClassInfo;
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

    @Transactional(readOnly = false)
	public int updateMhsClassInfo(MhsClassInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return vo.getMode().equals("Ins") ? mhsClassMapper.insertMhsClassInfo(vo) :
                mhsClassMapper.updateMhsClassInfo(vo);

	}

    @Transactional(readOnly = false)
	public int deleteMhsClassInfo(String mhsClasscd) throws Exception {
		// TODO Auto-generated method stub
		return mhsClassMapper.deleteMhsClassInfo(mhsClasscd);
	}

	

	

}
