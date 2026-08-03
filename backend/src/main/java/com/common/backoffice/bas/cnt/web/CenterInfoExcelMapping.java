package com.common.backoffice.bas.cnt.web;


import com.common.backoffice.bas.cnt.modals.CenterInfo;

import org.apache.poi.ss.usermodel.Row;
import org.egovframe.rte.fdl.excel.EgovExcelMapping;
import org.egovframe.rte.fdl.excel.util.EgovExcelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 매장(센터) 정보 엑셀 일괄 업로드용 행(Row) → VO 매핑 클래스.
 * 레거시(emart_cms3.2.1) egovframework.let.sym.cnt.web.CenterInfoExcelMapping을 참조해서
 * 패키지 경로만 현재 의존성(org.egovframe.rte.fdl.excel) 기준으로 바로잡음
 * (레거시가 egov 3.x 시절 패키지명인 egovframework.rte.fdl.excel.*을 그대로 쓰고 있어서
 * 존재하지 않는 패키지였음 — 실제로는 org.egovframe.rte.fdl.excel.*).
 * 2026-07-27: org.egovframe.rte.* 4.2.0(Spring 5.x/javax 시절) → 4.3.0(Spring Boot 3.x/
 * Jakarta EE 지원) 업그레이드에 맞춰 EgovExcelMapping.mappingColumn()이 더 이상
 * throws Exception을 선언하지 않아 오버라이드 시그니처도 함께 수정함.
 * CenterInfoManageController의 엑셀 업로드(/excelUpload.do)에서 사용함.
 */
public class CenterInfoExcelMapping extends EgovExcelMapping {

	private static final Logger LOGGER = LoggerFactory.getLogger(CenterInfoExcelMapping.class);

	@Override
	public CenterInfo mappingColumn(Row row) {
		// TODO Auto-generated method stub
		org.apache.poi.ss.usermodel.Cell cell0 =  row.getCell(0);
		org.apache.poi.ss.usermodel.Cell cell1 =  row.getCell(1);
		org.apache.poi.ss.usermodel.Cell cell2 =  row.getCell(2);
		org.apache.poi.ss.usermodel.Cell cell3 =  row.getCell(3);
		org.apache.poi.ss.usermodel.Cell cell4 =  row.getCell(4);
		org.apache.poi.ss.usermodel.Cell cell5 =  row.getCell(5);
		org.apache.poi.ss.usermodel.Cell cell6 =  row.getCell(6);
		org.apache.poi.ss.usermodel.Cell cell7 =  row.getCell(7);
		org.apache.poi.ss.usermodel.Cell cell8 =  row.getCell(8);
		org.apache.poi.ss.usermodel.Cell cell9 =  row.getCell(9);
		
		
		CenterInfo centerinfo = new CenterInfo();
		
		
		centerinfo.setCenterNm(EgovExcelUtil.getValue(cell0));
		centerinfo.setCenterZipcode(EgovExcelUtil.getValue(cell1));
		centerinfo.setCenterAddr1(EgovExcelUtil.getValue(cell2));
		centerinfo.setCenterAddr2(EgovExcelUtil.getValue(cell3));
		centerinfo.setCenterTel(EgovExcelUtil.getValue(cell4));
		centerinfo.setCenterFax(EgovExcelUtil.getValue(cell5));
		centerinfo.setCenterUserId(EgovExcelUtil.getValue(cell6));
		centerinfo.setCenterStartTime(EgovExcelUtil.getValue(cell7));
		centerinfo.setCenterEndTime(EgovExcelUtil.getValue(cell8));
		centerinfo.setCenterGubun(EgovExcelUtil.getValue(cell9));
		
		
		
		return centerinfo;
		
		
	}
	
	
	
	
}
