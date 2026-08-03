package com.common.backoffice.sts.mhs.service;


import java.util.List;

import com.common.backoffice.sts.mhs.mapper.MhsViewConnInfoManageMapper;
import com.common.backoffice.sts.mhs.modals.MhsViewConnInfo;
import com.common.backoffice.sts.mhs.modals.MhsViewConnInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class MhsViewConnInfoManageService  {

	private final MhsViewConnInfoManageMapper ConnMapper;
	
	
	public List<MhsViewConnInfoVO> selectViewMoniterClassInfo(
			MhsViewConnInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return ConnMapper.selectViewMoniterClassInfo(searchVO);
	}

    public int selectViewMoniterClassUpdateInfoChange(String mhsMonitorcd)
            throws Exception {
        // TODO Auto-generated method stub
        return ConnMapper.selectViewMoniterClassUpdateInfoChange(mhsMonitorcd);
    }
    public List<MhsViewConnInfoVO> selectViewMoniterClassUninPageInfo(
            MhsViewConnInfoVO searchVO) throws Exception {
        // TODO Auto-generated method stub
        return ConnMapper.selectViewMoniterClassUninPageInfo(searchVO);
    }

    @Transactional(readOnly = false)
	public int insertMoniterClassInfo(MhsViewConnInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return ConnMapper.insertMoniterClassInfo(vo);
	}

    @Transactional(readOnly = false)
	public int updateMoniterClassInfo(MhsViewConnInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return ConnMapper.updateMoniterClassInfo(vo);
	}

    @Transactional(readOnly = false)
	public int deleteMoniterClassInfo(String  mhsConnSeq) throws Exception {
		// TODO Auto-generated method stub
		return ConnMapper.deleteMoniterClassInfo(mhsConnSeq);
	}


    //통신 관련 내용 변경 
    @Transactional(readOnly = false)
	public int updateMoniterDidUpdateChange(String mhsMonitorcd)
			throws Exception {
		// TODO Auto-generated method stub
		return ConnMapper.updateMoniterDidUpdateChange(mhsMonitorcd);
	}

    @Transactional(readOnly = false)
	public int updateMoniterClassChangeInfo(String mhsClasscd) throws Exception {
		// TODO Auto-generated method stub
		return ConnMapper.updateMoniterClassChangeInfo(mhsClasscd);
	}

	


	
}
