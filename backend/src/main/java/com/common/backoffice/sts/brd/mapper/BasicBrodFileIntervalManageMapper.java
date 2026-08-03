package com.common.backoffice.sts.brd.mapper;

import com.common.backoffice.sts.brd.modals.BasicBrodFileIntervalInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface BasicBrodFileIntervalManageMapper {

	public List<BasicBrodFileIntervalInfoVO> selectBasicBrodIntervalFileLst(BasicBrodFileIntervalInfoVO  searchVO);
	
	public BasicBrodFileIntervalInfoVO selectBasicBrodIntervalFileDetail(String brodFileseq);
	
	public List<BasicBrodFileIntervalInfoVO> selectAgentFileList (String basicCode);
	
	public List<BasicBrodFileIntervalInfoVO> selectAgentFileDownList (String basicCode);
	
    public int insertBasicBrodIntervalFile(BasicBrodFileIntervalInfoVO vo);
    
    public int updateBasicBrodIntervalFile(BasicBrodFileIntervalInfoVO vo);
    
    public int insertBasicBrodIntervalFileCopy(BasicBrodFileIntervalInfoVO vo);
    
    public int deleteBasicBrodIntervalFile (String brodFileseq);
    
    public int deleteBasicBrodIntervalGroupFile (String groupSeq);
    
    public int deleteBasicBrodBasicCodeInterval(String basicCode);
	
}
