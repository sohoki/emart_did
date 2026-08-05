import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { useResetForm } from '@/hooks/use-form.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

// YYYY-MM-DD(네이티브 date input) → YYYYMMDD(DB 조회 형식)로 변환
const stripDash = (v) => (v || '').replace(/-/g, '');

const INITIAL_SEARCH_FORM = {
    searchCondition: '',
    searchKeyword: '',
    centerId: '',
    xmlProcessName: '',
    schStartDay: '',
    schEndDay: '',
};

// DID 전문통신현황 — 레거시 sendResultList.jsp 참고.
export default function SendMsgListPage() {
    const gridApiRef = useRef(null);
    const [centerOptions, setCenterOptions] = useState([]);
    const [processOptions, setProcessOptions] = useState([]);

    useEffect(() => {
        let active = true;
        (async () => {
            const [centerRes, processRes] = await Promise.all([
                fnAjaxFetch({ url: URL.SND_CENTER_COMBO, method: 'GET', showLoading: false }),
                fnAjaxFetch({ url: URL.SND_PROCESS_COMBO, method: 'GET', showLoading: false }),
            ]);
            if (!active) return;
            setCenterOptions(centerRes?.data?.result?.resultList || []);
            setProcessOptions(processRes?.data?.result?.resultList || []);
        })();
        return () => { active = false; };
    }, []);

    const fetchSendMsgList = useCallback(async (query) => {
        const res = await fnAjaxFetch({
            url: URL.SND_LIST, method: 'POST',
            data: { ...query, schStartDay: stripDash(query.schStartDay), schEndDay: stripDash(query.schEndDay) },
        });
        const data = res?.data;
        return {
            rows: data?.result?.resultList || [],
            total: data?.result?.totalCnt || 0,
        };
    }, []);

    const {
        onGridReady,
        defaultColDef,
        tempParams,
        setTempParams,
        handleSearch,
    } = useGridInfinite({
        fetchApi: fetchSendMsgList,
        pageUnit: 30,
        initialFilters: INITIAL_SEARCH_FORM,
    });

    const handleInputChange = useCallback((e) => {
        const { name, value } = e.target;
        setTempParams((prev) => ({ ...prev, [name]: value }));
    }, [setTempParams]);

    const onSearch = useCallback(async (pageIndex) => {
        if (tempParams.schStartDay && !tempParams.schEndDay) {
            await Swal.fire({ icon: 'warning', title: '입력 확인', text: '시작일 입력 시 종료일도 입력해야 합니다.' });
            return;
        }
        handleSearch(pageIndex || 1);
    }, [handleSearch, tempParams.schStartDay, tempParams.schEndDay]);

    const onSearchKeyDown = useCallback((e) => {
        if (e.key === 'Enter') onSearch(1);
    }, [onSearch]);

    const { handleReset } = useResetForm(setTempParams, INITIAL_SEARCH_FORM);

    const columnDefs = useMemo(() => ([
        { field: 'groupNm', headerName: '그룹명', width: 130 },
        { field: 'didNm', headerName: 'DID명', width: 150 },
        { field: 'xmlProcessName', headerName: '명령어', width: 160 },
        { field: 'processRemark', headerName: '전문명', flex: 1, minWidth: 160 },
        { field: 'sendRegDate', headerName: '요청시간(전송일시)', width: 160 },
        { field: 'didPlayTime', headerName: '응답시간', width: 160 },
        { field: 'sendResult', headerName: '결과', width: 100 },
        { field: 'didIpAddr', headerName: 'IP', width: 130 },
        { field: 'errorMessage', headerName: '오류메시지', flex: 1, minWidth: 160 },
    ]), []);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">전문통신현황</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">운영 관리</li>
                        <li className="breadcrumb-item">전문통신현황</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <select id="centerId" name="centerId"
                            value={tempParams.centerId} onChange={handleInputChange}>
                            <option value="">전체 매장</option>
                            {centerOptions.map((o) => (
                                <option key={o.centerId} value={o.centerId}>{o.centerNm}</option>
                            ))}
                        </select>
                        <select id="xmlProcessName" name="xmlProcessName"
                            value={tempParams.xmlProcessName} onChange={handleInputChange}>
                            <option value="">전체 전문</option>
                            {processOptions.map((o) => (
                                <option key={o.xmlProcessName} value={o.xmlProcessName}>{o.processRemark || o.xmlProcessName}</option>
                            ))}
                        </select>
                        <input type="date" id="schStartDay" name="schStartDay"
                            value={tempParams.schStartDay} onChange={handleInputChange} />
                        <span style={{ alignSelf: 'center' }}>~</span>
                        <input type="date" id="schEndDay" name="schEndDay"
                            value={tempParams.schEndDay} onChange={handleInputChange} />
                        <select id="searchCondition" name="searchCondition"
                            value={tempParams.searchCondition} onChange={handleInputChange}>
                            <option value="">선택</option>
                            <option value="XML_PROCESS_NAME">전문명</option>
                            <option value="DID_ID">DID ID</option>
                        </select>
                        <input type="text" id="searchKeyword" name="searchKeyword" placeholder="검색어를 입력하세요"
                            value={tempParams.searchKeyword}
                            onChange={handleInputChange}
                            onKeyDown={onSearchKeyDown}
                        />
                    </div>
                    <div className="col-auto content-search__action">
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={() => onSearch(1)}>검색</button>
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={handleReset}>검색 초기화</button>
                    </div>
                </div>
            </div>

            <div className="col-12 content-table content-table__main">
                <div className="ag-theme-material" style={{ height: 760, width: '100%' }}>
                    <AppAgGrid
                        columnDefs={columnDefs}
                        theme={gridTheme}
                        defaultColDef={defaultColDef}
                        rowModelType="infinite"
                        pagination={true}
                        paginationPageSize={30}
                        cacheBlockSize={30}
                        maxBlocksInCache={2}
                        onGridReady={(params) => { gridApiRef.current = params.api; onGridReady(params); }}
                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                    />
                </div>
            </div>
        </div>
    );
}
