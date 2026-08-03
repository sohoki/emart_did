package com.common.backoffice.bas.program.mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import com.common.backoffice.bas.program.modals.ProgrmInfo;
import com.common.backoffice.bas.program.modals.dto.ProgrmInfoDto;

@Mapper
public interface ProgrmInfoManageMapper {

	public List<ProgrmInfoDto> selectProgrmInfoList(@Param("params") Map<String, Object> params);
		
	public Optional<ProgrmInfoDto> selectProgrmInfoDetail(String progrmFileNm);
	
	public int selectProgrmListTotCnt();
	
	public int insertProgrmInfo(ProgrmInfo vo);
		
	public int updateProgrmInfo(ProgrmInfo vo);
	
	public int deleteProgrmInfo(String progrmFileNm);
	
	public int deleteProgrmManageList(@Param("programFiles") List<String> programFiles);
	/**
	 * 프로그램목록 전체삭제 초기화
	 * @return boolean
	 * @exception Exception
	 */
	public int deleteAllProgrm();
}
