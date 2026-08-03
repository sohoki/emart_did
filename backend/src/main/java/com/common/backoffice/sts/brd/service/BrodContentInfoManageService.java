package com.common.backoffice.sts.brd.service;


import java.util.List;

import javax.annotation.Resource;

import com.common.backoffice.sts.brd.mapper.BrodContentInfoManagerMapper;
import com.common.backoffice.sts.brd.modals.BrodContentInfo;
import com.common.backoffice.sts.brd.modals.BrodContentInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class BrodContentInfoManageService {

	
	private final BrodContentInfoManagerMapper brodContentMapper;
	
	
	public List<BrodContentInfoVO> selectBrodContentLst(
			BrodContentInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.selectBrodContentLst(searchVO);
	}

	
	public BrodContentInfoVO selectBrodContentInfo(String brodCode)
			throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.selectBrodContentInfo(brodCode);
	}

	
	public int selectBrodContentPageCnt(BrodContentInfoVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.selectBrodContentPageCnt(searchVO);
	}

	/**
	 * 신규 방송(BROD_CODE) 채번. 원본은 EgovIdGnrService(egovBrodIdGnrService) 빈을 사용했으나
	 * did_emart에는 해당 빈이 구성되어 있지 않아 애플리케이션 레벨로 대체 채번함.
	 * 패턴: BROD_ + 10자리 zero-pad 일련번호(MAX+1).
	 */
	@Transactional(readOnly = false)
	public String generateBrodCode() throws Exception {
		String maxCode = brodContentMapper.selectMaxBrodCode();
		long nextSeq = 1L;
		if (maxCode != null && maxCode.startsWith("BROD_")) {
			nextSeq = Long.parseLong(maxCode.substring("BROD_".length())) + 1;
		}
		return "BROD_" + String.format("%010d", nextSeq);
	}

    @Transactional(readOnly = false)
	public int insertBrodContent(BrodContentInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.insertBrodContent(vo);
	}
    public String selectBrodContentTimeInfo(String brodCode) throws Exception {
        // TODO Auto-generated method stub
        return brodContentMapper.selectBrodContentTimeInfo(brodCode);
    }


    public List<BrodContentInfo> selectBrodContentCopy(String brodCode) throws Exception {
        // TODO Auto-generated method stub
        return brodContentMapper.selectBrodContentCopy(brodCode);
    }
    public List<BrodContentInfo> selectBrodContentCombo() throws Exception {
        // TODO Auto-generated method stub
        return brodContentMapper.selectBrodContentCombo();
    }
    public List<BrodContentInfo> selectBrodContentComboAnn() throws Exception {
        // TODO Auto-generated method stub
        return brodContentMapper.selectBrodContentComboAnn();
    }


    public String selectBrodContentTimeInfoChar(String brodCode)
            throws Exception {
        // TODO Auto-generated method stub
        return brodContentMapper.selectBrodContentTimeInfoChar(brodCode);
    }
    public String selectBrodContentCenterCheckBrodCode(String centerId)
            throws Exception {
        // TODO Auto-generated method stub
        return brodContentMapper.selectBrodContentCenterCheckBrodCode(centerId);
    }
    public String selectBrodContentCenterPreBrodCode(String centerId)
            throws Exception {
        // TODO Auto-generated method stub
        return brodContentMapper.selectBrodContentCenterPreBrodCode(centerId);
    }


    public List<BrodContentInfoVO> selectBrodRight(
            BrodContentInfo brodContentInfo) throws Exception {
        // TODO Auto-generated method stub
        return brodContentMapper.selectBrodRight(brodContentInfo);
    }


    public List<BrodContentInfo> selectBrodContentBasicContent(String brodCode)
            throws Exception {
        // TODO Auto-generated method stub
        return brodContentMapper.selectBrodContentBasicContent(brodCode);
    }
    public String selectBrodContentBasicBrodCodePreBrodCode(String brodCode)
            throws Exception {
        // TODO Auto-generated method stub
        return brodContentMapper.selectBrodContentBasicBrodCodePreBrodCode(brodCode);
    }


    public String selectBrodContentBasicBrodCode(String brodCode)
            throws Exception {
        // TODO Auto-generated method stub
        return brodContentMapper.selectBrodContentBasicBrodCode(brodCode);
    }
    public String selectBrodContentCenterNm(String brodCode) throws Exception {
        // TODO Auto-generated method stub
        return brodContentMapper.selectBrodContentCenterNm(brodCode);
    }

    @Transactional(readOnly = false)
	public int updateBrodContent(BrodContentInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.updateBrodContent(vo);
	}

    @Transactional(readOnly = false)
	public int updateBrodContentTimeInterval(BrodContentInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.updateBrodContentTimeInterval(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBrodContent(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.deleteBrodContent(brodCode);
	}

	


    @Transactional(readOnly = false)
	public int insertBrodContentCopy(BrodContentInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.insertBrodContentCopy(vo);
	}




    @Transactional(readOnly = false)
	public int updateBrodContentSchChange(BrodContentInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.updateBrodContentSchChange(vo);
	}

	


    @Transactional(readOnly = false)
	public int updateBrodContentBasicInfo(BrodContentInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.updateBrodContentBasicInfo(vo);
	}

	


    @Transactional(readOnly = false)
	public int insertBrodContentCenterBrodCodeCopy(BrodContentInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.insertBrodContentCenterBrodCodeCopy(vo);
	}

    @Transactional(readOnly = false)
	public int updateBrodContentCenterCntPlus(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.updateBrodContentCenterCntPlus(brodCode);
	}

    @Transactional(readOnly = false)
	public int updateBrodContentCenterCntMin(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.updateBrodContentCenterCntMin(brodCode);
	}

    @Transactional(readOnly = false)
	public int updateBrodContentCenter(BrodContentInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.updateBrodContentCenter(vo);
	}

	


    @Transactional(readOnly = false)
	public int updateBrodBasicCodeCntMin(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.updateBrodBasicCodeCntMin(brodCode);
	}




    @Transactional(readOnly = false)
	public int updateBrodContentBasicInfoName(BrodContentInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.updateBrodContentBasicInfoName(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBrodContentAll(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.deleteBrodContentAll(brodCode);
	}

    @Transactional(readOnly = false)
	public int updateBrodContentBasicFileInfo(BrodContentInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return brodContentMapper.updateBrodContentBasicFileInfo(vo);
	}

	
}
