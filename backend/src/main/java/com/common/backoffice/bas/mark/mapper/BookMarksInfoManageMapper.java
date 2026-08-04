package com.common.backoffice.bas.mark.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import com.common.backoffice.bas.mark.models.dto.BookMarksInfoReqDto;

@Mapper
public interface BookMarksInfoManageMapper {

	public List<Map<String, Object>> selectBookMarksInfoList(@Param("params") Map<String, Object> params);

	public int insertBookMarksInfo(@Param("list") List<BookMarksInfoReqDto> list);

	public int updateBookMarksOrder(@Param("userId") String userId, @Param("list") List<BookMarksInfoReqDto> list);

	public int deleteBookMarksInfo(@Param("list") List<BookMarksInfoReqDto> list);
}
