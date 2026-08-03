package com.common.backoffice.sts.mhs.service;

import java.util.List;

import com.common.backoffice.sts.mhs.mapper.MhsMonitorManageMapper;
import com.common.backoffice.sts.mhs.modals.MhsMonitorInfo;
import com.common.backoffice.sts.mhs.modals.MhsMonitorInfoVO;
import egovframework.com.cmm.service.Globals;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class MhsMonitorInfoManageService {
	
	private final MhsMonitorManageMapper mhsMonitorMapper;
	
	
	public List<MhsMonitorInfoVO> selectMhsMonitorList(MhsMonitorInfoVO mhsMonitorInfoVO) throws Exception {
		// TODO Auto-generated method stub
		return mhsMonitorMapper.selectMhsMonitorList(mhsMonitorInfoVO);
	}

	
	public int selectMhsMonitorListCnt(MhsMonitorInfoVO mhsMonitorInfoVO)
			throws Exception {
		// TODO Auto-generated method stub
		return mhsMonitorMapper.selectMhsMonitorListCnt(mhsMonitorInfoVO);
	}

	
	public MhsMonitorInfoVO selectMhsMonitorInfo(String mhsMonitorcd)
			throws Exception {
		// TODO Auto-generated method stub
		return mhsMonitorMapper.selectMhsMonitorInfo(mhsMonitorcd);
	}
	
	
	public List<MhsMonitorInfoVO> selectMhsMonitorCombo(String mhsCentercd) throws Exception {
		// TODO Auto-generated method stub
		return mhsMonitorMapper.selectMhsMonitorCombo(mhsCentercd);
	}

	/**
	 * 신규 모니터(MHS_MONITORCD) 채번. DB 함수 FN_MHSMONITERID(centercd)를 그대로 사용함.
	 */
	@Transactional(readOnly = false)
	public String generateMhsMonitorcd(String mhsCentercd) throws Exception {
		return mhsMonitorMapper.selectMhsMonitorId(mhsCentercd);
	}



    @Transactional(readOnly = false)
	public int updateMhsMonitorInfo(MhsMonitorInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return vo.getMode().equals(Globals.SAVE_MODE_INSERT) ?
                mhsMonitorMapper.insertMhsMonitorInfo(vo) :
			    mhsMonitorMapper.updateMhsMonitorInfo(vo);

	}

    @Transactional(readOnly = false)
	public int updateMhsMonitorInfoIpMac(MhsMonitorInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return mhsMonitorMapper.updateMhsMonitorInfoIpMac(vo);
	}

    @Transactional(readOnly = false)
	public int deleteMhsMonitorInfo(String mhsMonitorcd) throws Exception {
		// TODO Auto-generated method stub
		return mhsMonitorMapper.deleteMhsMonitorInfo(mhsMonitorcd);
	}

    @Transactional(readOnly = false)
	public int updateMhsMonitorInfoStatus(String mhsMonitorcd) throws Exception {
		// TODO Auto-generated method stub
		return mhsMonitorMapper.updateMhsMonitorInfoStatus(mhsMonitorcd);
	}

    @Transactional(readOnly = false)
	public int updateMoniterDidUpdateDayChange(String mhsMonitorcd)
			throws Exception {
		// TODO Auto-generated method stub
		return mhsMonitorMapper.updateMhsMonitorInfoStatus(mhsMonitorcd);
	}

	
	

}
