package com.common.backoffice.sts.brd.service;


import java.util.List;
import java.util.Map;


import com.common.backoffice.sts.brd.mapper.BrodContentDetailManagerMapper;
import com.common.backoffice.sts.brd.modals.BrodContentDetail;
import com.common.backoffice.sts.brd.modals.BrodContentDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class BrodContentDetailManagerService {

	
	private final BrodContentDetailManagerMapper contentDetail;
	
	
	public List<BrodContentDetailVO> selectBrodContentDetailLst(
			BrodContentDetailVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.selectBrodContentDetailLst(searchVO);
	}

	
	public BrodContentDetailVO selectBrodContenDetailt(String brodSeq)
			throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.selectBrodContenDetailt(brodSeq);
	}

	
	public int selectBrodContentDetailPageCnt(BrodContentDetailVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.selectBrodContentDetailPageCnt(searchVO);
	}
    public List<BrodContentDetail> selectTimeCombo(String endTime)
            throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectTimeCombo(endTime);
    }


    public int selectContentRegCnt(BrodContentDetail vo) throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectContentRegCnt(vo);
    }


    public int selectContentRegTimeOverCheck(BrodContentDetail vo)
            throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectContentRegTimeOverCheck(vo);
    }
    public List<BrodContentDetail> selectTimeHourCombo() throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectTimeHourCombo();
    }
    public int selectContentFileTime(String atchFileId) throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectContentFileTime(atchFileId);
    }


    public int selectContentRegTimeImsiOverTableCheck(BrodContentDetail vo)
            throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectContentRegTimeImsiOverTableCheck(vo);
    }
    public List<BrodContentDetail> selectBrodFileList(BrodContentDetail vo) throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectBrodFileList(vo);
    }

    @Transactional(readOnly = false)
	public int insertBrodContentDetail(BrodContentDetail vo) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.insertBrodContentDetail(vo);
	}

    @Transactional(readOnly = false)
	public int updateBrodContentDetail(BrodContentDetail vo) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.updateBrodContentDetail(vo);
	}

    @Transactional(readOnly = false)
	public int insertLogFileMp3Data(Map<String, String> map) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.insertLogFileMp3Data(map);
	}

    @Transactional(readOnly = false)
	public int deleteBrodContentDetail(String brodSeq) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.deleteBrodContentDetail(brodSeq);
	}




    @Transactional(readOnly = false)
	public int deleteBrodContentTimeDel(BrodContentDetail vo) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.deleteBrodContentTimeDel(vo);
	}

	


    @Transactional(readOnly = false)
	public int insertBrodContentCopy(BrodContentDetail vo) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.insertBrodContentCopy(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBrodContentBrodCode(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.deleteBrodContentBrodCode(brodCode);
	}




    @Transactional(readOnly = false)
	public int insertBrodContentCenterCopy(BrodContentDetail vo)
			throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.insertBrodContentCenterCopy(vo);
	}

    @Transactional(readOnly = false)
	public int insertBrodContentScheduleOtherCopy(String brodCode)
			throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.insertBrodContentScheduleOtherCopy(brodCode);
	}

    @Transactional(readOnly = false)
	public int deleteBrodBasicBrod(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.deleteBrodBasicBrod(brodCode);
	}

    @Transactional(readOnly = false)
	public int deleteContentDetailBasicContent(BrodContentDetail vo)
			throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.deleteContentDetailBasicContent(vo);
	}



    @Transactional(readOnly = false)
	public int deleteBrodContentBrodCodeALL(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.deleteBrodContentBrodCodeALL(brodCode);
	}
	
	
}
