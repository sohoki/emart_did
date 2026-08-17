import { useCallback, useMemo, useRef, useState, Suspense, lazy } from 'react';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const BrodAnniversaryFormModal = lazy(() => import('./components/BrodAnniversaryFormModal.jsx'));

const PAGE_UNIT = 20;

const INITIAL_SEARCH_FORM = {
    brodCode: '',
    brodDay: '',
};

const EMPTY_ANNIVER_FORM = {
    mode: 'Ins', brodAnnSeq: '', brodCode: '', atchFileId: '', anniverName: '', anniversaryGubun: '',
    anniverStartDay: '', anniverEndDay: '', anniversaryTime: '', anniverOrder: '1',
};

// 방송 기념일 관리 — 레거시 brodAnniverList.jsp 참고. 등록/수정 우측 인라인 폼이었던
// 것을 다른 목록 화면들과 동일한 모달로 교체. 방송 코드가 있어야 조회되는 화면 특성상
// 표준 골격의 select+keyword 검색 대신 방송코드/기준일 두 입력을 그대로 둔다.
export default function BrodAnniversaryListPage() {
    const gridApiRef = useRef(null);

    const [modalOpen, setModalOpen] = useState(false);
    const [anniverForm, setAnniverForm] = useState(EMPTY_ANNIVER_FORM);
    const [gubunOptions, setGubunOptions] = useState([]);
    const [fileOptions, setFileOptions] = useState([]);

    const fetchAnniverList = useCallback(async (query) => {
        if (!query.brodCode) {
            return { rows: [], total: 0 };
        }
        const res = await fnAjaxFetch({ url: URL.BROD_ANNIVER_LIST, method: 'POST', data: query });
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
        fetchApi: fetchAnniverList,
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

    // ===== 등록/수정 모달 =====
    // 콘텐츠명 검색(음원 파일) — 특정방송 모달에서 공용으로 쓴다.
    const handleFileSearch = useCallback(async (keyword) => {
        const res = await fnAjaxFetch({
            url: URL.BROD_CONTENT_DETAIL_FILE_SEARCH, method: 'GET',
            param: { orgFileNm: keyword ?? '' }, showLoading: false,
        });
        setFileOptions(res?.data?.result?.resultList ?? []);
    }, []);

    const handleOpenAnniverModal = useCallback(async (brodAnnSeq) => {
        // 특정방송여부 콤보(EMT020)는 content/copyPopupData.do가 함께 내려준다.
        const popupRes = await fnAjaxFetch({
            url: URL.BROD_CONTENT_REG_POPUP_DATA, method: 'GET', param: { atchFileId: '' }, showLoading: false,
        });
        setGubunOptions(popupRes?.data?.result?.anniversaryGubun ?? []);

        if (!brodAnnSeq) {
            if (!tempParams.brodCode) {
                await Swal.fire({ icon: 'warning', title: '입력 필요', text: '방송 코드를 먼저 입력해 주세요.' });
                return;
            }
            setAnniverForm({ ...EMPTY_ANNIVER_FORM, brodCode: tempParams.brodCode });
            setModalOpen(true);
            return;
        }
        const res = await fnAjaxFetch({ url: URL.BROD_ANNIVER_DETAIL, method: 'POST', data: { brodAnnSeq } });
        const detail = res?.data?.result?.result;
        if (!detail) {
            await Swal.fire({ icon: 'error', title: '조회 실패', text: res?.data?.resultMessage || '특정방송 상세 조회에 실패했습니다.' });
            return;
        }
        setAnniverForm({ ...EMPTY_ANNIVER_FORM, ...detail, mode: 'Edt' });
        setModalOpen(true);
    }, [tempParams.brodCode]);

    const handleSubmit = useCallback(async () => {
        const isInsert = anniverForm.mode === 'Ins';
        const res = await fnAjaxFetch({ url: URL.BROD_ANNIVER_UPDATE, method: 'POST', data: anniverForm });
        if (res?.data?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: `${isInsert ? '등록' : '수정'}되었습니다.` });
            setModalOpen(false);
            refreshGrid();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || `${isInsert ? '등록' : '수정'} 중 오류가 발생했습니다.` });
        }
    }, [anniverForm, refreshGrid]);

    const handleDelete = useCallback(async (brodAnnSeq) => {
        const result = await Swal.fire({
            icon: 'question', title: '기념일 삭제', text: '삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        await fnAjaxFetch({ url: `${URL.BROD_ANNIVER_DELETE}/${brodAnnSeq}.do`, method: 'DELETE' });
        refreshGrid();
    }, [refreshGrid]);

    const columnDefs = useMemo(() => ([
        {
            headerName: '기념일명', field: 'anniverName', flex: 1, minWidth: 160,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => handleOpenAnniverModal(p.data?.brodAnnSeq)}>{p.value}</button>
            ),
        },
        { field: 'codeNm', headerName: '구분', width: 120 },
        { field: 'anniverStartDay', headerName: '시작일', width: 120 },
        { field: 'anniverEndDay', headerName: '종료일', width: 120 },
        {
            headerName: '삭제', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={() => handleDelete(p.data?.brodAnnSeq)}>삭제</button>
            ),
        },
    ]), [handleOpenAnniverModal, handleDelete]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">방송 기념일 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">방송 관리</li>
                        <li className="breadcrumb-item">방송 기념일 관리</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <input type="text" name="brodCode" placeholder="방송 코드(BROD_...)"
                            value={tempParams.brodCode}
                            onChange={handleInputChange}
                            onKeyDown={onSearchKeyDown}
                        />
                        <input type="text" name="brodDay" placeholder="기준일(YYYYMMDD, 선택)"
                            value={tempParams.brodDay}
                            onChange={handleInputChange}
                            onKeyDown={onSearchKeyDown}
                        />
                    </div>
                    <div className="col-auto content-search__action">
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={() => onSearch(1)}>조회</button>
                        <button type="button" className="btn btn-primary btn-default__blue"
                            onClick={() => handleOpenAnniverModal()}>신규 등록</button>
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
                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>방송 코드를 입력해서 조회해 주세요.</span>"
                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                    />
                </div>
            </div>

            <Suspense fallback={null}>
                {modalOpen && (
                    <BrodAnniversaryFormModal
                        open={modalOpen}
                        form={anniverForm}
                        setForm={setAnniverForm}
                        gubunOptions={gubunOptions}
                        fileOptions={fileOptions}
                        onFileSearch={handleFileSearch}
                        onClose={() => setModalOpen(false)}
                        onSubmit={handleSubmit}
                    />
                )}
            </Suspense>
        </div>
    );
}
