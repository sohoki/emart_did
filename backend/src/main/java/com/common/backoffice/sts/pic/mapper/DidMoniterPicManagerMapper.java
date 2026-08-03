package com.common.backoffice.sts.pic.mapper;

import com.common.backoffice.sts.pic.modals.DidMoniterPic;
import com.common.backoffice.sts.pic.modals.DidMoniterPicVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;



@Mapper
public interface DidMoniterPicManagerMapper {

	public List<DidMoniterPicVO> selectDidMoniterPicManageListByPagination (DidMoniterPicVO SearchVO);
	
	public int selectDidMoniterPicManageListTotCnt_S  (DidMoniterPicVO SearchVO);
	
	public int insertDidMoniterPicManage(DidMoniterPic vo);
	
}
