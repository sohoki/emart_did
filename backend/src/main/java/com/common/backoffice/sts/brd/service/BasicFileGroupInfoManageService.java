package com.common.backoffice.sts.brd.service;

import java.util.List;

import javax.annotation.Resource;

import com.common.backoffice.sts.brd.mapper.BasicBrodFileIntervalManageMapper;
import com.common.backoffice.sts.brd.mapper.BasicFileGroupInfoManageMapper;
import com.common.backoffice.sts.brd.modals.BasicBrodFileIntervalInfoVO;
import com.common.backoffice.sts.brd.modals.BasicFileGroupInfoVO;
import egovframework.com.cmm.service.Globals;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class BasicFileGroupInfoManageService  {

	private final BasicFileGroupInfoManageMapper basicGroupInfo;
	private final BasicBrodFileIntervalManageMapper fileIntervalinfo;
	
	
	public List<BasicFileGroupInfoVO> selectBasicGroupInfoLst(BasicFileGroupInfoVO searchVO) {
		// TODO Auto-generated method stub
		//System.out.println("searchVO:" + searchVO.getBasicCode() + ":" + searchVO.getBasicCode().length());
		return basicGroupInfo.selectBasicGroupInfoLst(searchVO);
	}

	
	public BasicFileGroupInfoVO selectBasicGroupInfoDetail(String groupSeq) {
		// TODO Auto-generated method stub
		return basicGroupInfo.selectBasicGroupInfoDetail(groupSeq);
	}
    public BasicFileGroupInfoVO selectBasicGroupPreCheck(
            BasicFileGroupInfoVO searchVO) throws Exception {
        // TODO Auto-generated method stub
        return basicGroupInfo.selectBasicGroupPreCheck(searchVO);
    }


    public List<BasicFileGroupInfoVO> selectBasicGroupInfoCombo() throws Exception {
        // TODO Auto-generated method stub
        return basicGroupInfo.selectBasicGroupInfoCombo();
    }

	
	public int insertBasicGroupInfo(BasicFileGroupInfoVO vo) {
		// TODO Auto-generated method stub
		return basicGroupInfo.insertBasicGroupInfo(vo);
	}

    @Transactional(readOnly = false)
	public int updateBasicGroupInfo(BasicFileGroupInfoVO vo) {
		// TODO Auto-generated method stub
		
		return vo.getMode().equals(Globals.SAVE_MODE_INSERT) ? basicGroupInfo.insertBasicGroupInfo(vo)
                                                : basicGroupInfo.updateBasicGroupInfo(vo);

	}

    @Transactional(readOnly = false)
	public int deleteBasicGroup(String groupSeq) {
		// TODO Auto-generated method stub
		fileIntervalinfo.deleteBasicBrodIntervalGroupFile(groupSeq);
		return basicGroupInfo.deleteBasicGroup(groupSeq);
	}

    @Transactional(readOnly = false)
	public int deleteBasicGroupBrodCode(String basicCode) {
		// TODO Auto-generated method stub
		return basicGroupInfo.deleteBasicGroupBrodCode(basicCode);
	}

	


    @Transactional(readOnly = false)
	public int updateBasicGroupInfoCopy(BasicFileGroupInfoVO vo) throws Exception {
		// TODO Auto-generated method stub
		int ret = basicGroupInfo.insertBasicGroupCopy(vo);
		if (ret > 0){
			String groupSeq = basicGroupInfo.selectMaxGroupSeq();			
			BasicBrodFileIntervalInfoVO fileVo = new BasicBrodFileIntervalInfoVO();
			fileVo.setBasicCode(vo.getBasicCode());
			fileVo.setBrodStarttime(vo.getGroupStarttime());
			fileVo.setBrodEndtime(vo.getGroupEndtime());
			fileVo.setGroupSeq(groupSeq);
			fileVo.setCp_copyGroupSeq(vo.getCp_copyGroupSeq());
			fileVo.setUserId(vo.getUserId());
			
			fileIntervalinfo.insertBasicBrodIntervalFileCopy(fileVo);
		}
		return 0;
	}
}
