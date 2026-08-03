package com.common.backoffice.sts.brd.service;


import java.util.List;

import javax.annotation.Resource;

import com.common.backoffice.sts.brd.mapper.BrodOrganizationManagerMapper;
import com.common.backoffice.sts.brd.modals.BrodOrganization;
import com.common.backoffice.sts.brd.modals.BrodOrganizationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class BrodOrganizationManagerService {


	private final BrodOrganizationManagerMapper brodOrgMapper;

	
	public List<BrodOrganizationVO> selectBrodOrganizationLst(
			BrodOrganizationVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return brodOrgMapper.selectBrodOrganizationLst(searchVO);
	}

	
	public BrodOrganizationVO selectBrodOrganizationInfo(String orgSeq)
			throws Exception {
		// TODO Auto-generated method stub
		return brodOrgMapper.selectBrodOrganizationInfo(orgSeq);
	}

	
	public int selectOrganizationPageCnt(BrodOrganizationVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return brodOrgMapper.selectOrganizationPageCnt(searchVO);
	}
    public List<BrodOrganization> selectBrodOrgnizationPage(BrodOrganization vo)
            throws Exception {
        // TODO Auto-generated method stub
        return brodOrgMapper.selectBrodOrgnizationPage(vo);
    }
    public List<BrodOrganization> selectBrodOrgnizationDid(BrodOrganization vo)
            throws Exception {
        // TODO Auto-generated method stub
        return brodOrgMapper.selectBrodOrgnizationDid(vo);
    }

    @Transactional(readOnly = false)
	public int insertBrodOrganization(BrodOrganization vo) throws Exception {
		// TODO Auto-generated method stub
		return brodOrgMapper.insertBrodOrganization(vo);
	}

    @Transactional(readOnly = false)
	public int updateBrodOrganization(BrodOrganization vo) throws Exception {
		// TODO Auto-generated method stub
		return brodOrgMapper.insertBrodOrganization(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBrodOrganization(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return brodOrgMapper.deleteBrodOrganization(brodCode);
	}

    @Transactional(readOnly = false)
	public int deleteBrodOrganizationCenterId(BrodOrganization vo)
			throws Exception {
		// TODO Auto-generated method stub
		return brodOrgMapper.deleteBrodOrganizationCenterId(vo);
	}

    @Transactional(readOnly = false)
	public int deleteContentToOrg(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return brodOrgMapper.deleteContentToOrg(brodCode);
	}

	

}