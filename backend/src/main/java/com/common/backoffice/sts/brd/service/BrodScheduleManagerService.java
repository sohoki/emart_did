package com.common.backoffice.sts.brd.service;


import java.util.List;


import javax.annotation.Resource;

import com.common.backoffice.sts.brd.mapper.BrodScheduleManagerMapper;
import com.common.backoffice.sts.brd.modals.BrodScheduleInfo;
import com.common.backoffice.sts.brd.modals.BrodScheduleInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class BrodScheduleManagerService {

	private final BrodScheduleManagerMapper brodSchedule;
	
	
	public List<BrodScheduleInfoVO> selectBrodRigthLst(
			BrodScheduleInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.selectBrodRigthLst(searchVO);
	}

    public List<BrodScheduleInfoVO> selectBrodScheduleStatusLst(
            BrodScheduleInfoVO searchVO) throws Exception {
        // TODO Auto-generated method stub
        return brodSchedule.selectBrodScheduleStatusLst(searchVO);
    }


    public int selectBrodScheduleStatusPageCnt(BrodScheduleInfoVO searchVO)
            throws Exception {
        // TODO Auto-generated method stub
        return brodSchedule.selectBrodScheduleStatusPageCnt(searchVO);
    }


    public List<BrodScheduleInfoVO> selectBrodScheduleUpdateChanage(
            String bordCode) throws Exception {
        // TODO Auto-generated method stub
        return brodSchedule.selectBrodScheduleUpdateChanage(bordCode);
    }

    public String selectBrodScheduleAnniScheduleSeqDay(BrodScheduleInfo searchVO)
            throws Exception {
        // TODO Auto-generated method stub
        return brodSchedule.selectBrodScheduleAnniScheduleSeqDay(searchVO);
    }
    public BrodScheduleInfo selectBordScheduleCodeRedown(BrodScheduleInfo vo)
            throws Exception {
        // TODO Auto-generated method stub
        return brodSchedule.selectBordScheduleCodeRedown(vo);
    }

    public List<BrodScheduleInfo> selectBrodScheduleCreateCheckList(
            BrodScheduleInfo searchVO) throws Exception {
        // TODO Auto-generated method stub
        return brodSchedule.selectBrodScheduleCreateCheckList(searchVO);
    }

    public int selectBrodScheduleCenterCnt(String centerId) throws Exception {
        // TODO Auto-generated method stub
        return brodSchedule.selectBrodScheduleCenterCnt(centerId);
    }


    public int selectBrodScheduleStateCnt(BrodScheduleInfo vo) throws Exception {
        // TODO Auto-generated method stub
        return brodSchedule.selectBrodScheduleStateCnt(vo);
    }
    //신규 부분

    public BrodScheduleInfo selectBordScheduleCodeNew(BrodScheduleInfo vo)
            throws Exception {
        // TODO Auto-generated method stub
        return brodSchedule.selectBordScheduleCodeNew(vo);
    }


    public BrodScheduleInfo selectBordScheduleCodeRedownNew(BrodScheduleInfo vo)
            throws Exception {
        // TODO Auto-generated method stub
        return brodSchedule.selectBordScheduleCodeNew(vo);
    }
    //신규 부분  끝 끝 부분

    @Transactional(readOnly = false)
	public int insertBrodSchedule(BrodScheduleInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.insertBrodSchedule(vo);
	}

    @Transactional(readOnly = false)
	public int updateBrodSchedule(BrodScheduleInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.updateBrodSchedule(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBrodSchedule(BrodScheduleInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.deleteBrodSchedule(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBrodScheduleOther(BrodScheduleInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.deleteBrodScheduleOther(vo);
	}

    @Transactional(readOnly = false)
	public int updateCenterSchedule(BrodScheduleInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.updateCenterSchedule(vo);
	}



    @Transactional(readOnly = false)
	public int deleteBrodScheduleAll(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.deleteBrodScheduleAll(brodCode);
	}

    @Transactional(readOnly = false)
	public int selectBrodScheduleCnt(BrodScheduleInfo searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.selectBrodScheduleCnt(searchVO);
	}

    @Transactional(readOnly = false)
	public int updateBrodScheduleCenter(BrodScheduleInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.updateBrodScheduleCenter(vo);
	}

    @Transactional(readOnly = false)
	public int updateBrodScheduleCenterBrod(BrodScheduleInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.updateBrodScheduleCenterBrod(vo);
	}

	
	public int selectBordScheduleCount(BrodScheduleInfo vo) throws Exception {
		// TODO Auto-generated method stub				
		return brodSchedule.selectBordScheduleCount(vo);
	}

	
	public BrodScheduleInfo selectBordScheduleCode(BrodScheduleInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.selectBordScheduleCode(vo);
	}

    @Transactional(readOnly = false)
	public int updateBrodDidCenterID(BrodScheduleInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.updateBrodDidCenterID(vo);
	}

	


    @Transactional(readOnly = false)
	public int updateBrodScheduleReset(BrodScheduleInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.updateBrodScheduleReset(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBrodScheduleSeq(String scheduleSeq) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.deleteBrodScheduleSeq(scheduleSeq);
	}



    @Transactional(readOnly = false)
	public int updateBrodScheduleCenterNotUse(String centerId) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.updateBrodScheduleCenterNotUse(centerId);
	}



    @Transactional(readOnly = false)
	public int deleteBrodScheduleState(BrodScheduleInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodSchedule.deleteBrodScheduleState(vo);
	}

}
