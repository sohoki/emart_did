package com.common.backoffice.sts.brd.mapper;


import com.common.backoffice.sts.brd.modals.BasicFileGroupPlayInfo;
import com.common.backoffice.sts.brd.modals.BasicFileGroupPlayInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface BasicFileGroupPlayInfoManageMapper {

	public List<BasicFileGroupPlayInfoVO> selectPlayListInfo (BasicFileGroupPlayInfoVO searchVO );
	
	public List<BasicFileGroupPlayInfoVO> selectPlayListInfoNotCenter (BasicFileGroupPlayInfoVO searchVO );
	
	public int selectFileInserCheck (BasicFileGroupPlayInfoVO searchVO );
	
	public int insertFilePlay (BasicFileGroupPlayInfo vo );
	
	public int updateFilePlay (BasicFileGroupPlayInfo vo );
	
	public int deleteFilePlay (BasicFileGroupPlayInfo vo );
}
