import { useCallback, useMemo, useRef, useState, Suspense, lazy } from 'react';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { useResetForm } from '@/hooks/use-form.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const BrodDeployTimeModal = lazy(() => import('./components/BrodDeployTimeModal.jsx'));

const PAGE_UNIT = 20;

const INITIAL_SEARCH_FORM = {
    searchCondition: 'CON_NM',
    searchKeyword: '',
};

// 방송 배포 스케줄 관리 — 레거시 brodScheduleStatus.jsp 참고. 매장 배포 시간대 입력에
// 쓰이던 Swal.fire({html:'<input>...'}) 프롬프트를 BrodDeployTimeModal로 교체.
export default function BrodScheduleStatusPage() {
    const gridApiRef = useRef(null);

    const [selectedBrod, setSelectedBrod] = useState(null);
    const [centerList, setCenterList] = useState([]);
    const [rightSearchKeyword, setRightSearchKeyword] = useState('');

    const [deployModalOpen, setDeployModalOpen] = useState(false);
    const [deployTarget, setDeployTarget] = useState(null);

    const fetchContentList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: URL.BROD_SCHEDULE_LEFT_LIST, method: 'POST', data: query });
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
        fetchApi: fetchContentList,
        pageUnit: PAGE_UNIT,
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

    // ===== 배포 매장 패널 =====
    const loadCenterList = useCallback(async (brodCode, keyword) => {
        const res = await fnAjaxFetch({
            url: URL.BROD_SCHEDULE_RIGHT_LIST, method: 'GET',
            param: { brodCode, rightSearchKeyword: keyword ?? '' }, showLoading: false,
        });
        setCenterList(res?.data?.result?.resultList ?? []);
    }, []);

    const handleSelectContent = useCallback((row) => {
        setSelectedBrod(row);
        setRightSearchKeyword('');
        loadCenterList(row.brodCode, '');
    }, [loadCenterList]);

    const handleRightSearch = useCallback(() => {
        if (selectedBrod) loadCenterList(selectedBrod.brodCode, rightSearchKeyword);
    }, [selectedBrod, rightSearchKeyword, loadCenterList]);

    const applyToggle = useCallback(async (center, connect, centerStartTime, centerEndTime) => {
        await fnAjaxFetch({
            url: URL.BROD_SCHEDULE_RIGHT_UPDATE,
            method: 'POST',
            data: {
                brodCode: selectedBrod.brodCode,
                checkVal: connect ? 'Y' : 'N',
                centerId: center.centerId,
                centerStartTime,
                centerEndTime,
            },
        });
        loadCenterList(selectedBrod.brodCode, rightSearchKeyword);
    }, [selectedBrod, rightSearchKeyword, loadCenterList]);

    const handleToggle = useCallback(async (center, connect) => {
        if (!selectedBrod) return;

        if (connect) {
            setDeployTarget(center);
            setDeployModalOpen(true);
            return;
        }

        const result = await Swal.fire({
            icon: 'question', title: '배포 해제', text: `${center.centerNm}의 배포를 해제하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        await applyToggle(center, false, center.centerStartTime || '0000', center.centerEndTime || '2359');
    }, [selectedBrod, applyToggle]);

    const handleDeploySubmit = useCallback(async ({ start, end }) => {
        setDeployModalOpen(false);
        await applyToggle(deployTarget, true, start, end);
    }, [deployTarget, applyToggle]);

    const columnDefs = useMemo(() => ([
        {
            headerName: '방송명', field: 'brodName', flex: 1, minWidth: 160,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => handleSelectContent(p.data)}>{p.value}</button>
            ),
        },
        { field: 'brodCode', headerName: '방송코드', width: 150 },
        { field: 'centerNm', headerName: '매장', width: 120 },
    ]), [handleSelectContent]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">방송 배포 스케줄 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">방송 관리</li>
                        <li className="breadcrumb-item">방송 배포 스케줄 관리</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <input type="text" name="searchKeyword" placeholder="방송명 검색"
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
                        paginationPageSize={PAGE_UNIT}
                        cacheBlockSize={PAGE_UNIT}
                        maxBlocksInCache={2}
                        onGridReady={(params) => { gridApiRef.current = params.api; onGridReady(params); }}
                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                    />
                </div>
            </div>

            {/* 선택한 방송의 배포 매장 관리 — 그리드 하단 상세 패널 */}
            {selectedBrod && (
                <div className="col-12 content-table" style={{
                    marginTop: 20, border: '1px solid #dde2eb', borderRadius: 8,
                    background: 'var(--bs-body-bg, #fff)', overflow: 'hidden',
                }}>
                    <div style={{
                        padding: '14px 20px 12px', borderBottom: '1px solid #dde2eb',
                        display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 8,
                    }}>
                        <span style={{ fontSize: 15, fontWeight: 700 }}>배포 매장 관리 — {selectedBrod.brodName}</span>
                        <div className="content-search__action">
                            <input type="text" placeholder="매장명 검색"
                                value={rightSearchKeyword}
                                onChange={(e) => setRightSearchKeyword(e.target.value)}
                                onKeyDown={(e) => { if (e.key === 'Enter') handleRightSearch(); }}
                            />
                            <button type="button" className="btn btn-outline-dark btn-outline__gray"
                                onClick={handleRightSearch}>검색</button>
                        </div>
                    </div>
                    <div style={{ padding: 16 }}>
                        <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: 13 }}>
                            <thead>
                                <tr style={{ background: '#f8fafc' }}>
                                    <th style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'left' }}>매장</th>
                                    <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 160 }}>배포시간</th>
                                    <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 120 }}>배포여부</th>
                                    <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 100 }} />
                                </tr>
                            </thead>
                            <tbody>
                                {centerList.map((center) => (
                                    <tr key={center.centerId}>
                                        <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>{center.centerNm}</td>
                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>
                                            {center.centerStartTime} ~ {center.centerEndTime}
                                        </td>
                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>
                                            {center.brodCode === 'Y' ? '배포중' : '미배포'}
                                        </td>
                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>
                                            {center.brodCode === 'Y' ? (
                                                <button type="button" className="btn btn-outline-danger btn-outline__gray btn-sm"
                                                    style={{ fontSize: 12 }}
                                                    onClick={() => handleToggle(center, false)}>해제</button>
                                            ) : (
                                                <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                                                    style={{ fontSize: 12 }}
                                                    onClick={() => handleToggle(center, true)}>배포</button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                                {centerList.length === 0 && (
                                    <tr><td colSpan={4} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>매장 정보가 없습니다.</td></tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            <Suspense fallback={null}>
                {deployModalOpen && (
                    <BrodDeployTimeModal
                        open={deployModalOpen}
                        centerNm={deployTarget?.centerNm}
                        onClose={() => setDeployModalOpen(false)}
                        onSubmit={handleDeploySubmit}
                    />
                )}
            </Suspense>
        </div>
    );
}
