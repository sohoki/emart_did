package com.common.backoffice.sts.brd.service;

import java.util.List;


import com.common.backoffice.sts.brd.mapper.BasicBrodScheduleManagerMapper;
import com.common.backoffice.sts.brd.modals.BasicBrodScheduleInfo;
import com.common.backoffice.sts.brd.modals.BasicBrodScheduleInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class BasicBrodScheduleInfoManageService {
	
	private final BasicBrodScheduleManagerMapper scheduleMapper;

	
	public List<BasicBrodScheduleInfoVO> selectBasicBrodScheduleLst(
			BasicBrodScheduleInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return scheduleMapper.selectBasicBrodScheduleLst(searchVO);
	}
    public List<BasicBrodScheduleInfoVO> selectBasicBrodScheduleCheckList(String centerGubun)
            throws Exception {
        // TODO Auto-generated method stub
        return scheduleMapper.selectBasicBrodScheduleCheckList(centerGubun);
    }
    public int selectBasicBrodScheduleLstCnt(BasicBrodScheduleInfoVO searchVO) throws Exception {
        // TODO Auto-generated method stub
        return scheduleMapper.selectBasicBrodScheduleLstCnt(searchVO);
    }

    public String selectBasicBrodContentlDownCheck(BasicBrodScheduleInfoVO searchVO) throws Exception {
        // TODO Auto-generated method stub
        return scheduleMapper.selectBasicBrodContentlDownCheck(searchVO);
    }

    @Transactional(readOnly = false)
	public int insertBasicBrodSchedule(BasicBrodScheduleInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return scheduleMapper.insertBasicBrodSchedule(vo);
	}

    @Transactional(readOnly = false)
	public int updateBasicBrodScheduleCenter(BasicBrodScheduleInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return scheduleMapper.updateBasicBrodScheduleCenter(vo);
	}

    @Transactional(readOnly = false)
	public int updateBasicBrodSchedule(BasicBrodScheduleInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return scheduleMapper.updateBasicBrodSchedule(vo);
	}

    @Transactional(readOnly = false)
	public int updateBasicBrodScheduleRestart(BasicBrodScheduleInfo vo) {
		// TODO Auto-generated method stub
		return scheduleMapper.updateBasicBrodScheduleRestart(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBasicBrodScheduleCenter(BasicBrodScheduleInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return scheduleMapper.deleteBasicBrodScheduleCenter(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBasicBrodSchedule(BasicBrodScheduleInfoVO vo)
			throws Exception {
		// TODO Auto-generated method stub
		return scheduleMapper.deleteBasicBrodSchedule(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBasicBrodScheduleOther(BasicBrodScheduleInfoVO vo)
			throws Exception {
		// TODO Auto-generated method stub
		return scheduleMapper.deleteBasicBrodScheduleOther(vo);
	}
	
	


    @Transactional(readOnly = false)
	public int updateBasicCodeCenterUpdate(BasicBrodScheduleInfoVO vo) throws Exception {
		// TODO Auto-generated method stub
		return scheduleMapper.updateBasicCodeCenterUpdate(vo);
	}

    @Transactional(readOnly = false)
	public int updateBasicBrodScheduleCenterE(BasicBrodScheduleInfoVO vo) throws Exception {
		// TODO Auto-generated method stub
		return scheduleMapper.updateBasicBrodScheduleCenterE(vo);
	}

    @Transactional(readOnly = false)
	public int updateBasicBrodScheduleCenterStateChange(BasicBrodScheduleInfoVO vo) throws Exception {
		// TODO Auto-generated method stub
		return scheduleMapper.updateBasicBrodScheduleCenterStateChange(vo);
	}

    @Transactional(readOnly = false)
	public int updateBasicBrodScheduleState(String basicCode) throws Exception {
		// TODO Auto-generated method stub
		return scheduleMapper.updateBasicBrodScheduleState(basicCode);
	}

	


    @Transactional(readOnly = false)
	public int insertBasicBrodScheduleDistribute(String basicCode) throws Exception {
		// TODO Auto-generated method stub
		return scheduleMapper.insertBasicBrodScheduleDistribute(basicCode);
	}

    @Transactional(readOnly = false)
	public int updateBasicCodeCenterReset(String basicCode) throws Exception {
		// TODO Auto-generated method stub
		return scheduleMapper.updateBasicCodeCenterReset(basicCode);
	}



    @Transactional(readOnly = false)
	public int updateBasicBrodScheduleCenterDownCheck(BasicBrodScheduleInfoVO vo) {
		// TODO Auto-generated method stub
		return scheduleMapper.updateBasicBrodScheduleCenterDownCheck(vo);
	}
}
