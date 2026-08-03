package com.common.backoffice.sts.brd.mapper;


import com.common.backoffice.sts.brd.modals.BrodContentDetailTime;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

@Mapper
public interface BrodContentDetailTimeManagerMapper {

	public int insertBrodContentDetailTime(BrodContentDetailTime vo);
	
	public int deleteBrodContentDetailTime(String imsiSeq);
	
	public int deleteBrodContentDetailTimeBrodCode(String brodCode);
	
	
}
