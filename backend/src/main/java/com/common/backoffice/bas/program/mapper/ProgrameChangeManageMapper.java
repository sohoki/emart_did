package com.common.backoffice.bas.program.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import com.common.backoffice.bas.program.modals.ProgrameChangeInfo;

@Mapper
public interface ProgrameChangeManageMapper {

	/**
	 * 프로그램변경요청 목록을 조회
	 * @param vo ComDefaultVO
	 * @return List
	 * @exception Exception
	 */

	public List<Map<String, Object>> selectProgrmChangeRequstList(@Param("params") Map<String, Object> params);
	/**
	 * 프로그램변경요청 정보를 조회
	 * @param vo ProgrmManageDtlVO
	 * @return ProgrmManageDtlVO
	 * @exception Exception
	 */
	public Map<String, Object> selectProgrmChangeRequst(@Param("params") Map<String, Object> params);
	/**
	 * 프로그램변경요청을 등록
	 * @param vo ProgrmManageDtlVO
	 * @exception Exception
	 */
	public int insertProgrmChangeRequst(ProgrameChangeInfo vo);
	/**
	 * 프로그램변경요청을 수정
	 * @param vo ProgrmManageDtlVO
	 * @exception Exception
	 */
	public int updateProgrmChangeRequst(ProgrameChangeInfo vo);
	/**
	 * 프로그램변경요청을 삭제
	 * @param vo ProgrmManageDtlVO
	 * @exception Exception
	 */
	public int deleteProgrmChangeRequst(ProgrameChangeInfo vo);
	/**
	 * 프로그램변경요청 요청번호MAX 정보를 조회
	 * @param vo ProgrmManageDtlVO
	 * @return ProgrmManageDtlVO
	 * @exception Exception
	 */
	public String selectProgrmChangeRequstNo();
	/**
	 * 프로그램변경요청 목록을 조회
	 * @param vo ComDefaultVO
	 * @return List
	 * @exception Exception
	 */
	public List<Map<String, Object>> selectChangeRequstProcessList(@Param("params") Map<String, Object> params) ;
	/**
	 * 프로그램변경요청 처리 수정
	 * @param vo ProgrmManageDtlVO
	 * @exception Exception
	 */
	public int updateProgrmChangeRequstProcess(ProgrameChangeInfo vo);
	/**
	 * 프로그램변경내역 전체삭제 초기화
	 * @return boolean
	 * @exception Exception
	 */
	public int deleteAllProgrmDtls();
}
