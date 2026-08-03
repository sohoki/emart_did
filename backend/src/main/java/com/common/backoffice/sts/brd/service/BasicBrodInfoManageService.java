package com.common.backoffice.sts.brd.service;

import java.util.List;

import com.common.backoffice.sts.brd.mapper.BasicBrodManagerMapper;
import com.common.backoffice.sts.brd.modals.BasicBrodInfo;
import com.common.backoffice.sts.brd.modals.BasicBrodInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class BasicBrodInfoManageService {

	private final BasicBrodManagerMapper basicMapper;

	
	public List<BasicBrodInfoVO> selectBasicBrodLst(BasicBrodInfoVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.selectBasicBrodLst(searchVO);
	}

	
	public BasicBrodInfo selectBasicBrod(String basicCode) throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.selectBasicBrod(basicCode);
	}

	
	public int selectBasicBrodPageCnt(BasicBrodInfoVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.selectBasicBrodPageCnt(searchVO);
	}

    public String selectMaxBrodCode() throws Exception {
        // TODO Auto-generated method stub
        return basicMapper.selectMaxBrodCode();
    }


    public List<BasicBrodInfo> selectBasicBrodCombo() throws Exception {
        // TODO Auto-generated method stub
        return basicMapper.selectBasicBrodCombo();
    }


    public String selectBasicCode() throws Exception {
        // TODO Auto-generated method stub
        return basicMapper.selectBasicCode();
    }

    @Transactional(readOnly = false)
	public int insertBasicBrod(BasicBrodInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.insertBasicBrod(vo);
	}

    @Transactional(readOnly = false)
	public int insertBasicBrodCopy(BasicBrodInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.insertBasicBrodCopy(vo);
	}

    @Transactional(readOnly = false)
	public int updateBasicBrod(BasicBrodInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.updateBasicBrod(vo);
	}

    @Transactional(readOnly = false)
	public int updateBasicBrodCnt(String basicCode) throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.updateBasicBrodCnt(basicCode);
	}

    @Transactional(readOnly = false)
	public int updateBasicBrodCntPlus(String basicCode) throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.updateBasicBrodCntPlus(basicCode);
	}

    @Transactional(readOnly = false)
	public int updateBasicBrodCntMins(String basicCode) throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.updateBasicBrodCntMins(basicCode);
	}

    @Transactional(readOnly = false)
	public int deleteBasicBrod(String basicCode) throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.deleteBasicBrod(basicCode);
	}

	

}
