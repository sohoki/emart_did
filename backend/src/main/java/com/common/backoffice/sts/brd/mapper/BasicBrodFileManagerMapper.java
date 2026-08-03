package com.common.backoffice.sts.brd.mapper;


import java.util.List;

import com.common.backoffice.sts.brd.modals.BasicBrodFileInfo;
import com.common.backoffice.sts.brd.modals.BasicBrodFileInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

@Mapper
public interface BasicBrodFileManagerMapper {

	
	public List<BasicBrodFileInfoVO> selectBasicBrodFileLst(String basicCode) throws Exception;
	
	public List<BasicBrodFileInfoVO> selectBasicBrodSchFileLst(BasicBrodFileInfoVO vo) throws Exception;
	
	public List<BasicBrodFileInfoVO> selectBasicBrodSchFileLstNew(String basicCode) throws Exception;
	
	public int insertBasicBrodFile(BasicBrodFileInfo vo)throws Exception;
	
	public int insertBasicBrodFileCopy(BasicBrodFileInfoVO vo)throws Exception;
	
	public int deleteBasicBrodFile(String basicSeq) throws Exception;
	
	public int deleteBasicBrodBasicCode(String basicCode);
}
