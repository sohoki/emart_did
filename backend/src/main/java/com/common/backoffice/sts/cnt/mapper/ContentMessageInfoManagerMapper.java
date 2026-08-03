package com.common.backoffice.sts.cnt.mapper;

import com.common.backoffice.sts.cnt.modals.ContentMessageInfo;
import com.common.backoffice.sts.cnt.modals.ContentMessageInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;


@Mapper
public interface ContentMessageInfoManagerMapper {

	public List<ContentMessageInfoVO> selectContentMessageInfoListByPagination(ContentMessageInfoVO searchVO);
	
	public List<ContentMessageInfo> selectContentMessageInfoDidList(String didId);
	
    public int selectContentMessageInfoListTotCnt_S(ContentMessageInfoVO searchVO);
    
    public ContentMessageInfo selectContentMessageInfoDetail(String sendMsgId);

    public String selectMaxSendMsgId();

    public String selectSendDidId(String didId);

    public int insertContentMessageInfo(ContentMessageInfo vo);
    
    public int updateContentMessageInfo(ContentMessageInfo vo);
    
    public int updateContentMessageInfoClientManage(String sendMsgId);
    
    public int updateContentMessageInfoMsgId(ContentMessageInfo vo);
    
    public int deleteContentMessageInfo(String sendDidId);
    
    public int deleteContentMessageInfoMsgId(String sendMsgId);
	
	
	
}
