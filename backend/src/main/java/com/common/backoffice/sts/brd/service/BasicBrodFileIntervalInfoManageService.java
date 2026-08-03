package com.common.backoffice.sts.brd.service;

import java.util.List;

import javax.annotation.Resource;

import com.common.backoffice.sts.brd.mapper.BasicBrodFileIntervalManageMapper;
import com.common.backoffice.sts.brd.mapper.BasicFileGroupInfoManageMapper;
import com.common.backoffice.sts.brd.modals.BasicBrodFileIntervalInfoVO;
import com.common.backoffice.sts.brd.modals.BasicFileGroupInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class BasicBrodFileIntervalInfoManageService  {
	
	private final BasicBrodFileIntervalManageMapper fileIntervalinfo;
	private final BasicFileGroupInfoManageMapper groupInfo;
	
	
	public List<BasicBrodFileIntervalInfoVO> selectBasicBrodIntervalFileLst(
			BasicBrodFileIntervalInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return fileIntervalinfo.selectBasicBrodIntervalFileLst(searchVO);
	}

	
	public BasicBrodFileIntervalInfoVO selectBasicBrodIntervalFileDetail(
			String brodFileseq) throws Exception {
		// TODO Auto-generated method stub
		return fileIntervalinfo.selectBasicBrodIntervalFileDetail(brodFileseq);
	}
    public List<BasicBrodFileIntervalInfoVO> selectAgentFileList(String basicCode) throws Exception {
        // TODO Auto-generated method stub
        return fileIntervalinfo.selectAgentFileList(basicCode);
    }


    public List<BasicBrodFileIntervalInfoVO> selectAgentFileDownList(String basicCode) throws Exception {
        // TODO Auto-generated method stub
        return fileIntervalinfo.selectAgentFileDownList(basicCode);
    }

    @Transactional(readOnly = false)
	public int insertBasicBrodIntervalFile(BasicBrodFileIntervalInfoVO vo)
			throws Exception {
		// TODO Auto-generated method stub
		
		BasicFileGroupInfoVO group = groupInfo.selectBasicGroupInfoDetail(vo.getGroupSeq());
		
		group.setFilePls("pls");
		
		vo.setBrodStarttime(group.getGroupStarttime());
		vo.setBrodEndtime(group.getGroupEndtime());
		int ret = fileIntervalinfo.insertBasicBrodIntervalFile(vo);
		if (ret > 0){
			groupInfo.updateGroupFileCnt(group);  
		}
		return ret;
	}

    @Transactional(readOnly = false)
	public int updateBasicBrodIntervalFile(BasicBrodFileIntervalInfoVO vo)
			throws Exception {
		// TODO Auto-generated method stub
		return fileIntervalinfo.updateBasicBrodIntervalFile(vo);
	}

    @Transactional(readOnly = false)
	public int insertBasicBrodIntervalFileCopy(BasicBrodFileIntervalInfoVO vo)
			throws Exception {
		// TODO Auto-generated method stub
		return fileIntervalinfo.insertBasicBrodIntervalFileCopy(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBasicBrodIntervalFile(BasicBrodFileIntervalInfoVO vo) throws Exception {
		// TODO Auto-generated method stub
		
		//파일 수량 확인 하기 
		BasicFileGroupInfoVO group  = new BasicFileGroupInfoVO();
        group.setFileMin("Min");
        group.setGroupSeq(vo.getGroupSeq());
		
		int ret = fileIntervalinfo.deleteBasicBrodIntervalFile(vo.getBrodFileseq());
		if (ret > 0){
			groupInfo.updateGroupFileCnt(group);  
		}
		return ret;
		
	}

    @Transactional(readOnly = false)
	public int deleteBasicBrodBasicCodeInterval(String basicCode)
			throws Exception {
		// TODO Auto-generated method stub
		return fileIntervalinfo.deleteBasicBrodBasicCodeInterval(basicCode);
	}

	


}
