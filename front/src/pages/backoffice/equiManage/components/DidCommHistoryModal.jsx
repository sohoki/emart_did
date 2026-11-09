import { useCallback, useEffect, useMemo, useState } from 'react';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';
import '@/style/Modal.css';

const PAGE_UNIT = 20;

// YYYY-MM-DD(네이티브 date input) → YYYYMMDD(DB 조회 형식)로 변환
const stripDash = (v) => (v || '').replace(/-/g, '');

// 장비통신 이력조회 — 레거시 pop_sendLst.do(전문통신현황 + didId 필터 팝업)를 모달로 옮김.
// 조회 API는 전문통신현황(SendMsgListPage)과 같은 SND_LIST를 쓰고 didId만 고정으로 넘긴다.
const DidCommHistoryModal = ({ open, didId, didNm, onClose }) => {
    const [processOptions, setProcessOptions] = useState([]);

    const initialFilters = useMemo(() => ({
        didId,
        xmlProcessName: '',
        schStartDay: '',
        schEndDay: '',
        searchCondition: '',
        searchKeyword: '',
    }), [didId]);

    useEffect(() => {
        if (!open) return undefined;
        let active = true;
        (async () => {
            const res = await fnAjaxFetch({ url: URL.SND_PROCESS_COMBO, method: 'GET', showLoading: false });
            if (active) setProcessOptions(res?.data?.result?.resultList || []);
        })();
        return () => { active = false; };
    }, [open]);

    const fetchHistory = useCallback(async (query) => {
        const res = await fnAjaxFetch({
            url: URL.SND_LIST, method: 'POST',
            data: { ...query, didId, schStartDay: stripDash(query.schStartDay), schEndDay: stripDash(query.schEndDay) },
            showLoading: false,
        });
        const data = res?.data;
        return {
            rows: data?.result?.resultList || [],
            total: data?.result?.totalCnt || 0,
        };
    }, [didId]);

    const {
        onGridReady,
        defaultColDef,
        tempParams,
        setTempParams,
        handleSearch,
        totalCount,
    } = useGridInfinite({
        fetchApi: fetchHistory,
        pageUnit: PAGE_UNIT,
        initialFilters,
    });

    const handleInputChange = useCallback((e) => {
        const { name, value } = e.target;
        setTempParams((prev) => ({ ...prev, [name]: value }));
    }, [setTempParams]);

    const onSearch = useCallback(async () => {
        if (tempParams.schStartDay && !tempParams.schEndDay) {
            await Swal.fire({ icon: 'warning', title: '입력 확인', text: '시작일 입력 시 종료일도 입력해야 합니다.' });
            return;
        }
        handleSearch(1);
    }, [handleSearch, tempParams.schStartDay, tempParams.schEndDay]);

    const handleReset = useCallback(() => {
        setTempParams(initialFilters);
    }, [setTempParams, initialFilters]);

    // 단말기가 고정이라 그룹명/DID명 컬럼은 제외하고 전문 위주로 표시
    const columnDefs = useMemo(() => ([
        { field: 'xmlProcessName', headerName: '명령어', width: 190, sortable: false },
        { field: 'processRemark', headerName: '전문명', flex: 1, minWidth: 160, sortable: false },
        { field: 'sendRegDate', headerName: '요청시간', width: 160, sortable: false },
        { field: 'didPlayTime', headerName: '응답시간', width: 160, sortable: false },
        { field: 'sendResult', headerName: '결과', width: 90, sortable: false },
        { field: 'errorMessage', headerName: '오류메시지', flex: 1, minWidth: 140, sortable: false },
    ]), []);

    if (!open) return null;

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered"
                    style={{ width: 1100, maxWidth: '95%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">장비통신 이력 — {didNm || didId}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body">
                            <div className="modal-body__content">
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, alignItems: 'center', marginBottom: 10 }}>
                                    <select name="xmlProcessName" className="form-select form-select-sm" style={{ width: 200 }}
                                        value={tempParams.xmlProcessName} onChange={handleInputChange}>
                                        <option value="">전체 전문</option>
                                        {processOptions.map((o) => (
                                            <option key={o.xmlProcessName} value={o.xmlProcessName}>{o.processRemark || o.xmlProcessName}</option>
                                        ))}
                                    </select>
                                    <input type="date" name="schStartDay" className="form-control form-control-sm" style={{ width: 150 }}
                                        value={tempParams.schStartDay} onChange={handleInputChange} />
                                    <span>~</span>
                                    <input type="date" name="schEndDay" className="form-control form-control-sm" style={{ width: 150 }}
                                        value={tempParams.schEndDay} onChange={handleInputChange} />
                                    <button type="button" className="btn btn-outline-dark btn-outline__gray btn-sm"
                                        onClick={onSearch}>검색</button>
                                    <button type="button" className="btn btn-outline-dark btn-outline__gray btn-sm"
                                        onClick={handleReset}>검색 초기화</button>
                                    <span style={{ marginLeft: 'auto', color: '#64748b', fontSize: 13 }}>총 {totalCount}건</span>
                                </div>

                                <div className="ag-theme-material" style={{ height: 520, width: '100%' }}>
                                    <AppAgGrid
                                        columnDefs={columnDefs}
                                        theme={gridTheme}
                                        defaultColDef={defaultColDef}
                                        rowModelType="infinite"
                                        pagination={true}
                                        paginationPageSize={PAGE_UNIT}
                                        cacheBlockSize={PAGE_UNIT}
                                        maxBlocksInCache={2}
                                        onGridReady={onGridReady}
                                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>통신 이력이 없습니다.</span>"
                                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-action__lightblue" onClick={onClose}>닫기</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default DidCommHistoryModal;
