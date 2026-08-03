package com.common.backoffice.sts.brd.mapper;

import java.util.List;

import com.common.backoffice.sts.brd.modals.BasicBrodInfo;
import com.common.backoffice.sts.brd.modals.BasicBrodInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

@Mapper
public interface BasicBrodManagerMapper {

	public List<BasicBrodInfoVO> selectBasicBrodLst(BasicBrodInfoVO searchVO) throws Exception;
	
	public BasicBrodInfo selectBasicBrod(String basicCode) throws Exception;
	
	public List<BasicBrodInfo> selectBasicBrodCombo()throws Exception;
	
	public int selectBasicBrodPageCnt(BasicBrodInfoVO searchVO) throws Exception;
	
	public String selectMaxBrodCode() throws Exception;
	
	public String selectBasicCode();
	
	public int insertBasicBrod(BasicBrodInfo vo)throws Exception;
	
	public int insertBasicBrodCopy(BasicBrodInfo vo)throws Exception;
	
	public int updateBasicBrod(BasicBrodInfo vo)throws Exception;
	
	public int updateBasicBrodCnt(String basicCode)throws Exception;
	
	public int updateBasicBrodCntPlus(String basicCode)throws Exception;
	
	public int updateBasicBrodCntMins(String basicCode)throws Exception;
	
	public int deleteBasicBrod (String basicCode)throws Exception;
	
	
}
