package com.common.backoffice.util.service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.cmm.util.ResultHelper;

public class PaginationHelper {
    private PaginationHelper() {}

    /**
     * searchMap에서 페이징 파라미터를 읽어 {@link PaginationInfo}를 구성한다.
     * PAGE_FIRST_INDEX / PAGE_LAST_INDEX / PAGE_RECORD_PER_PAGE를 searchMap에 세팅한다.
     *
     * <ul>
     *   <li>PAGE_UNIT 값이 searchMap에 있으면 사용, 없으면 defaultUnit 사용</li>
     *   <li>PAGE_SIZE 값이 searchMap에 있으면 사용, 없으면 defaultSize 사용</li>
     *   <li>PAGE_INDEX 기본값: 1</li>
     * </ul>
     *
     * @param searchMap   요청 파라미터 맵
     * @param defaultUnit pageUnit 기본값 (@Value 또는 propertiesService에서 전달)
     * @param defaultSize pageSize 기본값
     * @return 구성된 PaginationInfo
     */
    public static PaginationInfo buildInfo(Map<String, Object> searchMap,
                                           int defaultUnit, int defaultSize) {
        int pageUnit = searchMap.get(Globals.PAGE_UNIT) == null
                ? defaultUnit
                : Integer.parseInt(searchMap.get(Globals.PAGE_UNIT).toString());
        int pageSize = searchMap.get(Globals.PAGE_SIZE) == null
                ? defaultSize
                : Integer.parseInt(searchMap.get(Globals.PAGE_SIZE).toString());

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(searchMap.get(Globals.PAGE_INDEX) == null
                ? 1
                : Integer.parseInt(searchMap.get(Globals.PAGE_INDEX).toString()));
        paginationInfo.setRecordCountPerPage(pageUnit);
        paginationInfo.setPageSize(pageSize);

        searchMap.put(Globals.PAGE_FIRST_INDEX, paginationInfo.getFirstRecordIndex());
        searchMap.put(Globals.PAGE_LAST_INDEX, paginationInfo.getLastRecordIndex());
        searchMap.put(Globals.PAGE_RECORD_PER_PAGE, paginationInfo.getRecordCountPerPage());
        return paginationInfo;
    }

    /**
     * 조회 결과와 페이지네이션 정보를 resultVO에 세팅한다. (PAGE_INFO 키 사용)
     *
     * @param resultVO       응답 객체
     * @param list           조회 결과 목록 (모든 타입 수용)
     * @param paginationInfo 페이지네이션 정보
     * @param searchMap      요청 파라미터 맵 (STATUS_REGINFO용)
     * @param totCnt         전체 건수 (호출부에서 DTO 타입에 맞게 직접 추출)
     */
    public static void setResult(ResultVO resultVO, List<?> list,
                                 PaginationInfo paginationInfo,
                                 Map<String, Object> searchMap, int totCnt) {
        setResult(resultVO, list, paginationInfo, searchMap, totCnt, Globals.PAGE_INFO);
    }

    /**
     * 페이지 정보 키를 직접 지정하는 오버로드.
     * ({@code Globals.PAGE_INFO} 대신 {@code Globals.JSON_PAGEINFO} 등을 써야 하는 컨트롤러용)
     *
     * @param pageInfoKey 결과 맵에 사용할 페이지 정보 키
     */
    public static void setResult(ResultVO resultVO, List<?> list,
                                 PaginationInfo paginationInfo,
                                 Map<String, Object> searchMap,
                                 int totCnt,
                                 String pageInfoKey) {
        paginationInfo.setTotalRecordCount(totCnt);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(Globals.STATUS_REGINFO, searchMap);
        resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
        resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
        resultMap.put(pageInfoKey, paginationInfo);
        ResultHelper.setSuccess(resultVO, resultMap);
    }

    /**
     * {@code List<Map<String,Object>>} 전용 오버로드.
     * 첫 번째 행의 {@code Globals.PAGE_TOTAL_RECORD_COUNT} 값에서 totCnt를 자동 추출한다.
     */
    public static void setResult(ResultVO resultVO, List<Map<String, Object>> list,
                                 PaginationInfo paginationInfo,
                                 Map<String, Object> searchMap) {
        int totCnt = list.isEmpty()
                ? 0
                : (list.get(0).get(Globals.PAGE_TOTAL_RECORD_COUNT) != null
                ? Integer.parseInt(list.get(0).get(Globals.PAGE_TOTAL_RECORD_COUNT).toString()) : list.size());
        setResult(resultVO, list, paginationInfo, searchMap, totCnt);
    }
}
