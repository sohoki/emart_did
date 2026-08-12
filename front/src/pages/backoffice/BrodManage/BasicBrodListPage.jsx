import { useCallback, useMemo, useRef, useState, Suspense, lazy } from 'react';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { useResetForm } from '@/hooks/use-form.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const BasicBrodFormModal = lazy(() => import('./components/BasicBrodFormModal.jsx'));

const PAGE_UNIT = 20;

const INITIAL_SEARCH_FORM = {
    searchCondition: 'basicGroupNm',
    searchKeyword: '',
};

const EMPTY_BROD_FORM = { mode: 'Ins', basicCode: '', basicGroupNm: '' };

// 기초 방송(템플릿) 관리 — 레거시 basicBrodList.jsp 참고. 방송명 등록/수정은 원래
// Swal.fire({input:'text'}) 프롬프트였던 것을 다른 목록 화면들과 동일한 모달로 교체.
export default function BasicBrodListPage() {
    const gridApiRef = useRef(null);

    const [modalOpen, setModalOpen] = useState(false);
    const [brodForm, setBrodForm] = useState(EMPTY_BROD_FORM);

    const [selected, setSelected] = useState(null);
    const [centerList, setCenterList] = useState([]);

    const fetchBasicBrodList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: URL.BASIC_BROD_LIST, method: 'POST', data: query });
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
        fetchApi: fetchBasicBrodList,
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
    const loadCenterList = useCallback(async (basicCode) => {
        const res = await fnAjaxFetch({
            url: URL.BASIC_BROD_CENTER_LIST, method: 'GET',
            param: { centerGubun: '' }, showLoading: false,
        });
        const centers = res?.data?.result?.centerInfo ?? [];
        setCenterList(centers.map((c) => ({ ...c, connected: c.basicCode === basicCode })));
    }, []);

    const handleSelect = useCallback((row) => {
        setSelected(row);
        loadCenterList(row.basicCode);
    }, [loadCenterList]);

    // ===== 등록/수정 모달 =====
    const handleOpenBrodModal = useCallback((row) => {
        setBrodForm(row
            ? { mode: 'Upd', basicCode: row.basicCode, basicGroupNm: row.basicGroupNm }
            : EMPTY_BROD_FORM);
        setModalOpen(true);
    }, []);

    const handleSubmit = useCallback(async () => {
        const isInsert = brodForm.mode === 'Ins';
        const res = await fnAjaxFetch({
            url: URL.BASIC_BROD_UPDATE, method: 'POST',
            data: isInsert
                ? { mode: 'Ins', basicGroupNm: brodForm.basicGroupNm }
                : { mode: 'Upd', basicCode: brodForm.basicCode, basicGroupNm: brodForm.basicGroupNm },
        });
        if (res?.data?.resultCode === '00' || res?.data?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: `${isInsert ? '등록' : '수정'}되었습니다.` });
            setModalOpen(false);
            refreshGrid();
            if (!isInsert && selected?.basicCode === brodForm.basicCode) {
                setSelected((prev) => ({ ...prev, basicGroupNm: brodForm.basicGroupNm }));
            }
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || `${isInsert ? '등록' : '수정'} 중 오류가 발생했습니다.` });
        }
    }, [brodForm, refreshGrid, selected]);

    const handleCopy = useCallback(async () => {
        if (!selected) return;
        const result = await Swal.fire({
            icon: 'question', title: '기초 방송 복사', text: `'${selected.basicGroupNm}'을(를) 복사하시겠습니까? (편성 파일도 함께 복사됩니다)`,
            showCancelButton: true, confirmButtonText: '복사', cancelButtonText: '취소',
        });
        if (!result.isConfirmed) return;

        await fnAjaxFetch({
            url: URL.BASIC_BROD_UPDATE, method: 'POST',
            data: { mode: 'Cpy', basicCode: selected.basicCode, basicGroupNm: `${selected.basicGroupNm}_복사` },
        });
        refreshGrid();
    }, [selected, refreshGrid]);

    const handleDelete = useCallback(async () => {
        if (!selected) return;
        const result = await Swal.fire({
            icon: 'warning', title: '기초 방송 삭제', text: `'${selected.basicGroupNm}'을(를) 삭제하시겠습니까? 배포중인 매장이 있으면 함께 해제됩니다.`,
            showCancelButton: true, confirmButtonText: '삭제', cancelButtonText: '취소',
        });
        if (!result.isConfirmed) return;

        await fnAjaxFetch({
            url: URL.BASIC_BROD_DELETE, method: 'DELETE',
            data: { delBasicSeq: selected.basicCode },
        });
        setSelected(null);
        setCenterList([]);
        refreshGrid();
    }, [selected, refreshGrid]);

    const handleToggle = useCallback(async (center, connect) => {
        if (!selected) return;
        const result = await Swal.fire({
            icon: 'question',
            title: connect ? '배포' : '배포 해제',
            text: connect
                ? `${center.centerNm}에 '${selected.basicGroupNm}'을(를) 배포하시겠습니까?`
                : `${center.centerNm}의 배포를 해제하시겠습니까?`,
            showCancelButton: true,
            confirmButtonText: connect ? '배포' : '예',
            cancelButtonText: connect ? '취소' : '아니오',
        });
        if (!result.isConfirmed) return;

        await fnAjaxFetch({
            url: URL.BASIC_BROD_CENTER_UPDATE,
            method: 'POST',
            data: { CenterSeq: center.centerId, basicCode: selected.basicCode, checkValue: connect ? 'Y' : 'N' },
        });
        loadCenterList(selected.basicCode);
    }, [selected, loadCenterList]);

    const columnDefs = useMemo(() => ([
        {
            headerName: '방송명', field: 'basicGroupNm', flex: 1, minWidth: 200,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => handleSelect(p.data)}>{p.value}</button>
            ),
        },
        { field: 'basicCode', headerName: '기초방송코드', width: 160 },
        { field: 'basicGroupCnt', headerName: '배포수', width: 100, cellStyle: { textAlign: 'center' } },
        { field: 'lastUpdtPnttm', headerName: '최종수정일', width: 160 },
        {
            headerName: '수정', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-secondary btn-outline__gray btn-sm"
                    onClick={() => handleOpenBrodModal(p.data)}>수정</button>
            ),
        },
    ]), [handleSelect, handleOpenBrodModal]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">기초 방송(템플릿) 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">방송 관리</li>
                        <li className="breadcrumb-item">기초 방송(템플릿) 관리</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <select id="searchCondition" name="searchCondition" value={tempParams.searchCondition}
                            onChange={handleInputChange}>
                            <option value="basicGroupNm">방송명</option>
                        </select>
                        <input type="text" id="searchKeyword" name="searchKeyword" placeholder="방송명을 입력하세요"
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
                            onClick={() => handleOpenBrodModal()}>신규 등록</button>
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
                        rowSelection={{ mode: 'singleSelect' }}
                        onGridReady={(params) => { gridApiRef.current = params.api; onGridReady(params); }}
                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                    />
                </div>
            </div>

            {/* 선택한 방송의 배포 매장 관리 — 그리드 하단 상세 패널 */}
            {selected && (
                <div className="col-12 content-table" style={{
                    marginTop: 20, border: '1px solid #dde2eb', borderRadius: 8,
                    background: 'var(--bs-body-bg, #fff)', overflow: 'hidden',
                }}>
                    <div style={{
                        padding: '14px 20px 12px', borderBottom: '1px solid #dde2eb',
                        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                    }}>
                        <span style={{ fontSize: 15, fontWeight: 700 }}>배포 매장 관리 — {selected.basicGroupNm}</span>
                        <div className="content-search__action">
                            <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                                onClick={() => handleOpenBrodModal(selected)}>이름 수정</button>
                            <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                                onClick={handleCopy}>복사</button>
                            <button type="button" className="btn btn-outline-danger btn-outline__gray"
                                onClick={handleDelete}>삭제</button>
                        </div>
                    </div>
                    <div style={{ padding: 16 }}>
                        <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: 13 }}>
                            <thead>
                                <tr style={{ background: '#f8fafc' }}>
                                    <th style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'left' }}>매장</th>
                                    <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 120 }}>배포여부</th>
                                    <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 100 }} />
                                </tr>
                            </thead>
                            <tbody>
                                {centerList.map((center) => (
                                    <tr key={center.centerId}>
                                        <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>{center.centerNm}</td>
                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>
                                            {center.connected ? '배포중' : '미배포'}
                                        </td>
                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>
                                            {center.connected ? (
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
                                    <tr><td colSpan={3} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>매장 정보가 없습니다.</td></tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            <Suspense fallback={null}>
                {modalOpen && (
                    <BasicBrodFormModal
                        open={modalOpen}
                        form={brodForm}
                        setForm={setBrodForm}
                        onClose={() => setModalOpen(false)}
                        onSubmit={handleSubmit}
                    />
                )}
            </Suspense>
        </div>
    );
}
