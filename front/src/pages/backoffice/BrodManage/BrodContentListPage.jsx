import { useCallback, useMemo, useRef, useState, Suspense, lazy } from 'react';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { useResetForm } from '@/hooks/use-form.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const BrodContentFormModal = lazy(() => import('./components/BrodContentFormModal.jsx'));

const PAGE_UNIT = 20;

const INITIAL_SEARCH_FORM = {
    searchCondition: 'brodName',
    searchKeyword: '',
};

const EMPTY_BROD_FORM = {
    mode: 'Ins', brodCode: '', brodName: '', brodTotalTime: '',
    brodInterval: '', basicBrodCode: '', brodUseYn: 'Y',
};

// 방송(음원) 콘텐츠 관리 — 레거시 brodContentList.jsp 참고. 등록/수정 우측 인라인
// 폼이었던 것을 다른 목록 화면들과 동일한 모달로 교체.
export default function BrodContentListPage() {
    const gridApiRef = useRef(null);

    const [modalOpen, setModalOpen] = useState(false);
    const [brodForm, setBrodForm] = useState(EMPTY_BROD_FORM);
    const [intervalCombo, setIntervalCombo] = useState([]);
    const [basicCombo, setBasicCombo] = useState([]);

    const fetchBrodContentList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: URL.BROD_CONTENT_LIST, method: 'POST', data: query });
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
        fetchApi: fetchBrodContentList,
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

    // ===== 등록/수정 모달 =====
    const loadFormData = useCallback(async (brodCode, mode) => {
        const res = await fnAjaxFetch({
            url: URL.BROD_CONTENT_FORM_DATA, method: 'GET',
            param: { brodCode: brodCode ?? '', mode }, showLoading: false,
        });
        setIntervalCombo(res?.data?.result?.brodInterval ?? []);
        setBasicCombo(res?.data?.result?.basicInfo ?? []);
        return res?.data?.result?.regist;
    }, []);

    const handleOpenBrodModal = useCallback(async (row) => {
        if (row) {
            const detail = await loadFormData(row.brodCode, 'Edt');
            setBrodForm({ ...EMPTY_BROD_FORM, ...(detail ?? row), mode: 'Edt' });
        } else {
            await loadFormData('', 'Ins');
            setBrodForm(EMPTY_BROD_FORM);
        }
        setModalOpen(true);
    }, [loadFormData]);

    const handleSubmit = useCallback(async () => {
        const isInsert = brodForm.mode === 'Ins';
        const res = await fnAjaxFetch({ url: URL.BROD_CONTENT_UPDATE, method: 'POST', data: brodForm });
        if (res?.data?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: `${isInsert ? '등록' : '수정'}되었습니다.` });
            setModalOpen(false);
            refreshGrid();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || `${isInsert ? '등록' : '수정'} 중 오류가 발생했습니다.` });
        }
    }, [brodForm, refreshGrid]);

    // ===== 일괄 삭제(체크박스) =====
    const handleBulkDelete = useCallback(async () => {
        const selected = gridApiRef.current?.getSelectedRows() || [];
        if (selected.length === 0) {
            await Swal.fire({ icon: 'warning', title: '선택 필요', text: '체크 하신 콘텐츠가 없습니다.' });
            return;
        }
        const result = await Swal.fire({
            icon: 'warning', title: '방송(음원) 콘텐츠 삭제',
            text: `선택한 ${selected.length}건을 삭제하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '삭제', cancelButtonText: '취소',
        });
        if (!result.isConfirmed) return;

        await fnAjaxFetch({
            url: URL.BROD_CONTENT_DELETE_BULK, method: 'POST',
            data: { delBrodCode: selected.map((r) => r.brodCode).join(',') },
        });
        refreshGrid();
    }, [refreshGrid]);

    const columnDefs = useMemo(() => ([
        {
            headerName: '방송명', field: 'brodName', flex: 1, minWidth: 160,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => handleOpenBrodModal(p.data)}>{p.value}</button>
            ),
        },
        { field: 'brodCode', headerName: '방송코드', width: 160 },
        { field: 'codeNm', headerName: '재생간격', width: 110 },
        { field: 'brodTotalTime', headerName: '총재생시간(초)', width: 130, cellStyle: { textAlign: 'center' } },
        { field: 'orignlFileNm', headerName: '기초방송', width: 160 },
        {
            field: 'brodUseYn', headerName: '사용여부', width: 100, cellStyle: { textAlign: 'center' },
            valueFormatter: (p) => (p.value === 'Y' ? '사용' : '미사용'),
        },
    ]), [handleOpenBrodModal]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">방송(음원) 콘텐츠 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">방송 관리</li>
                        <li className="breadcrumb-item">방송(음원) 콘텐츠 관리</li>
                    </ol>
                </div>
            </div>

            <div className="col-12" style={{ padding: '0 0 12px', color: '#94a3b8', fontSize: 13 }}>
                파일 편성(시간대별 음원 배치)은 <a href="/backoffice/sub/conManage/muti/edit">콘텐츠 편성 화면</a>에서
                진행해 주세요. 편성표 생성/엑셀 다운로드는 후속 작업으로 예정되어 있습니다.
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <select id="searchCondition" name="searchCondition" value={tempParams.searchCondition}
                            onChange={handleInputChange}>
                            <option value="brodName">방송명</option>
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
                        <button type="button" className="btn btn-outline-danger btn-outline__gray"
                            onClick={handleBulkDelete}>선택 삭제</button>
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
                        rowSelection={{ mode: 'multiRow', checkboxes: true, headerCheckbox: true }}
                        onGridReady={(params) => { gridApiRef.current = params.api; onGridReady(params); }}
                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                    />
                </div>
            </div>

            <Suspense fallback={null}>
                {modalOpen && (
                    <BrodContentFormModal
                        open={modalOpen}
                        form={brodForm}
                        setForm={setBrodForm}
                        intervalCombo={intervalCombo}
                        basicCombo={basicCombo}
                        onClose={() => setModalOpen(false)}
                        onSubmit={handleSubmit}
                    />
                )}
            </Suspense>
        </div>
    );
}
