import { useCallback, useMemo, useRef, useState, Suspense, lazy } from 'react';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { useResetForm } from '@/hooks/use-form.jsx';
import { gridDateFormatter, gridDateTimeFormatter } from '@/lib/formatters.js';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const DidSendMessageModal = lazy(() => import('./components/DidSendMessageModal.jsx'));

// DID 발송 메시지(자막) 현황 — 레거시 didSendMessagelst.jsp 참고.
// "메시지 등록" 버튼은 didSendMessage.jsp(그룹→단말 선택 후 등록 팝업)를 하나의 모달로 통합한
// DidSendMessageModal을 연다(레거시는 두 화면이었지만 별도 페이지 이동 없이 모달로 처리).
const INITIAL_SEARCH_FORM = {
    searchCondition: '',
    searchKeyword: '',
};

export default function DidSendMessageList() {
    const gridApiRef = useRef(null);
    const [sendModalOpen, setSendModalOpen] = useState(false);

    const fetchMessageList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: URL.CON_MESSAGE_LIST, method: 'POST', data: query });
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
        refreshGrid,
    } = useGridInfinite({
        fetchApi: fetchMessageList,
        pageUnit: 20,
        initialFilters: INITIAL_SEARCH_FORM,
    });

    const handleInputChange = useCallback((e) => {
        const { name, value } = e.target;
        setTempParams((prev) => ({ ...prev, [name]: value }));
    }, [setTempParams]);

    const onSearch = useCallback((pageIndex) => {
        handleSearch(pageIndex || 1);
    }, [handleSearch]);

    const onSearchKeyDown = useCallback((e) => {
        if (e.key === 'Enter') onSearch(1);
    }, [onSearch]);

    const { handleReset } = useResetForm(setTempParams, INITIAL_SEARCH_FORM);

    const handleDeleteRow = useCallback(async (sendDidId, sendMessage) => {
        const ok = await Swal.fire({
            icon: 'question', title: '메시지 삭제',
            html: `<b>${sendMessage || ''}</b> 삭제하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const res = await fnAjaxFetch({
            url: URL.CON_MESSAGE_DELETE_BULK, method: 'POST', data: { sendDidIds: [sendDidId] },
        });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            refreshGrid();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || '삭제 중 오류가 발생했습니다.' });
        }
    }, [refreshGrid]);

    const columnDefs = useMemo(() => ([
        { field: 'groupNm', headerName: '그룹명', width: 140 },
        { field: 'didNm', headerName: '단말기명', width: 140 },
        {
            field: 'sendMessage', headerName: '메시지', flex: 1, minWidth: 180,
            valueFormatter: (p) => (p.value && p.value.length > 20 ? `${p.value.slice(0, 20)}...` : p.value),
        },
        { field: 'sendMessageStartDay', headerName: '시작일', width: 110, valueFormatter: gridDateFormatter },
        { field: 'sendMessageEndDay', headerName: '종료일', width: 110, valueFormatter: gridDateFormatter },
        { field: 'sendMessageStartTime', headerName: '시작시간', width: 90 },
        { field: 'sendMessageEndTime', headerName: '종료시간', width: 90 },
        {
            field: 'sendDidCheckDate', headerName: '단말기 전송여부', width: 140,
            valueFormatter: (p) => (p.value ? gridDateTimeFormatter(p) : '미전송'),
        },
        {
            headerName: '삭제', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={() => handleDeleteRow(p.data?.sendDidId, p.data?.sendMessage)}
                >삭제</button>
            ),
        },
    ]), [handleDeleteRow]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">메시지 현황</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">장비 관리</li>
                        <li className="breadcrumb-item">메시지 현황</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <select id="searchCondition" name="searchCondition"
                            value={tempParams.searchCondition} onChange={handleInputChange}>
                            <option value="">그룹명</option>
                            <option value="SEND_MESSAGE">메시지 내용</option>
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
                        <button type="button" className="btn btn-primary btn-default__blue"
                            onClick={() => setSendModalOpen(true)}>메시지 등록</button>
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
                        paginationPageSize={20}
                        cacheBlockSize={20}
                        maxBlocksInCache={2}
                        onGridReady={(params) => { gridApiRef.current = params.api; onGridReady(params); }}
                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                    />
                </div>
            </div>

            <Suspense fallback={null}>
                {sendModalOpen && (
                    <DidSendMessageModal
                        open={sendModalOpen}
                        onClose={() => setSendModalOpen(false)}
                        onSent={() => { setSendModalOpen(false); refreshGrid(); }}
                    />
                )}
            </Suspense>
        </div>
    );
}
