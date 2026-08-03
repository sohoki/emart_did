package com.common.backoffice.sts.mhs.service;

import java.util.List;

import com.common.backoffice.bas.cnt.modals.CenterInfoVO;
import com.common.backoffice.sts.mhs.mapper.MhsCenterManageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class MhsCenterInfoManageService  {

	private final MhsCenterManageMapper mhsCenterMapper;
			
	
	public List<CenterInfoVO> selectMhsBrandList(CenterInfoVO mhsCenterInfoVO) throws Exception {
		// TODO Auto-generated method stub
		return mhsCenterMapper.selectMhsBrandList(mhsCenterInfoVO);
	}
	
	
	public List<CenterInfoVO> selectMhsCenterList(CenterInfoVO mhsCenterInfoVO) throws Exception {
		// TODO Auto-generated method stub
		return mhsCenterMapper.selectMhsCenterList(mhsCenterInfoVO);
	}

	
	public List<CenterInfoVO> selectMhsComboList(String mhsBrandcd)
			throws Exception {
		// TODO Auto-generated method stub
		return mhsCenterMapper.selectMhsComboList(mhsBrandcd);
	}
    public CenterInfoVO selectMhsCenterInfo(String mhsCentercd)
            throws Exception {
        // TODO Auto-generated method stub
        return mhsCenterMapper.selectMhsCenterInfo(mhsCentercd);
    }


    public List<CenterInfoVO> selectMhsComboListMeber(String mhsBrandcd)
            throws Exception {
        // TODO Auto-generated method stub
        return mhsCenterMapper.selectMhsComboListMeber(mhsBrandcd);
    }



    @Transactional(readOnly = false)
	public int updateMhsCenter(CenterInfoVO vo) throws Exception {
		// TODO Auto-generated method stub
		return vo.getSubMode().equals("Ins")? mhsCenterMapper.insertMhsCenter(vo) :
                    mhsCenterMapper.updateMhsCenter(vo);

	}

    @Transactional(readOnly = false)
	public int deleteMhsCenter(String mhsCentercd) throws Exception {
		// TODO Auto-generated method stub
		return mhsCenterMapper.deleteMhsCenter(mhsCentercd);
	}

	

}
