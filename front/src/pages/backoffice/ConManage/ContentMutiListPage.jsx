import { useCallback, useEffect, useMemo, useRef, useState, Suspense, lazy } from 'react';
import { useNavigate } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { useResetForm } from '@/hooks/use-form.jsx';
import { useCommonDelete } from '@/hooks/use-common-delete.js';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const ContentMutiFormModal = lazy(() => import('./components/ContentMutiFormModal.jsx'));

const INITIAL_SEARCH_FORM = {
    searchCondition: '',
    searchKeyword: '',
};

const EMPTY_MUTI_FORM = {
    mode: 'Ins',
    conSeq: '',
    conNm: '',
    conScreen: '',
    conType: '',
    conUseYn: 'Y',
    conWidth: '1080',
    conHeight: '1980',
    conMid: '540',
    conNextSeq: '',
    conPlayType: '',
};

// 화면 구성(멀티페이지 콘텐츠) 관리 — 레거시 conMutiList.jsp 참고.
export default function ContentMutiListPage() {
    const navigate = useNavigate();
    const gridApiRef = useRef(null);

    const [modalOpen, setModalOpen] = useState(false);
    const [mutiForm, setMutiForm] = useState(EMPTY_MUTI_FORM);
    const [conTypeOptions, setConTypeOptions] = useState([]);
    const [screenTypeOptions, setScreenTypeOptions] = useState([]);
    const [playTypeOptions, setPlayTypeOptions] = useState([]);
    const [nextSeqOptions, setNextSeqOptions] = useState([]);

    // 화면타입/가로세로/분할재생기준 콤보는 페이지 최초 진입 시 한 번만 조회한다.
    useEffect(() => {
        let active = true;
        (async () => {
            const res = await fnAjaxFetch({
                url: URL.CON_MUTI_FORM_DATA, method: 'GET', param: { mode: 'Ins' }, showLoading: false,
            });
            if (!active) return;
            const result = res?.data?.result || {};
            setConTypeOptions(result.selectConType || []);
            setScreenTypeOptions(result.selectScreenType || []);
            setPlayTypeOptions(result.selectPlayType || []);
        })();
        return () => { active = false; };
    }, []);

    const fetchMutiList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: URL.CON_MUTI_LIST, method: 'POST', data: query });
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
        fetchApi: fetchMutiList,
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

    const handleOpenMutiModal = useCallback(async (conSeq) => {
        if (!conSeq) {
            setNextSeqOptions([]);
            setMutiForm(EMPTY_MUTI_FORM);
            setModalOpen(true);
            return;
        }
        const res = await fnAjaxFetch({
            url: URL.CON_MUTI_FORM_DATA, method: 'GET', param: { mode: 'Edt', conSeq },
        });
        const result = res?.data?.result || {};
        const obj = result.regist;
        if (obj) {
            setNextSeqOptions(result.selectNextSeq || []);
            setMutiForm({
                mode: 'Edt',
                conSeq: obj.conSeq || '',
                conNm: obj.conNm || '',
                conScreen: obj.conScreen || '',
                conType: obj.conType || '',
                conUseYn: obj.conUseYn || 'Y',
                conWidth: obj.conWidth || '',
                conHeight: obj.conHeight || '',
                conMid: obj.conMid || '',
                conNextSeq: obj.conNextSeq || '',
                conPlayType: obj.conPlayType || '',
            });
            setModalOpen(true);
        }
    }, []);

    const handleSubmit = useCallback(async () => {
        if (!mutiForm.conNm) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '화면명을 입력해 주세요.' });
            return;
        }
        if (!mutiForm.conType) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '가로/세로 타입을 선택해 주세요.' });
            return;
        }

        const action = mutiForm.mode === 'Ins' ? '등록' : '수정';
        const ok = await Swal.fire({
            icon: 'question', title: `화면 구성 ${action}`,
            html: `<b>${mutiForm.conNm}</b> ${action} 하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const res = await fnAjaxFetch({ url: URL.CON_MUTI_UPDATE, method: 'POST', data: mutiForm });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || `${action}되었습니다.` });
            setModalOpen(false);
            refreshGrid();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || `${action} 중 오류가 발생했습니다.` });
        }
    }, [mutiForm, refreshGrid]);

    const { handleDelete } = useCommonDelete({
        gridApiRef,
        URL: URL.CON_MUTI_INFO,
        MESSAGE: '화면 구성(연결된 파일/상세페이지가 함께 삭제됩니다)',
        reloadFunction: 'grid',
    });

    const columnDefs = useMemo(() => ([
        { field: 'conSeq', headerName: '순번', width: 90 },
        {
            field: 'conNm', headerName: '화면명', flex: 1, minWidth: 180,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => handleOpenMutiModal(p.data?.conSeq)}>{p.value}</button>
            ),
        },
        { field: 'codeNm', headerName: '화면타입', width: 120 },
        { field: 'conWidth', headerName: '가로', width: 90 },
        { field: 'conHeight', headerName: '세로', width: 90 },
        {
            field: 'schCnt', headerName: '연결 스케줄', width: 110,
            valueFormatter: (p) => `${p.value ?? 0}개`,
        },
        { field: 'frstRegistPnttm', headerName: '등록일', width: 150 },
        {
            headerName: '편성', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                    onClick={() => navigate(`/backoffice/sub/conManage/muti/edit?conSeq=${p.data.conSeq}`)}
                >편성</button>
            ),
        },
        {
            headerName: '삭제', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={() => handleDelete({ code: p.data?.conSeq, name: p.data?.conNm })}
                >삭제</button>
            ),
        },
    ]), [navigate, handleOpenMutiModal, handleDelete]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">화면 구성 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">콘텐츠 관리</li>
                        <li className="breadcrumb-item">화면 구성 관리</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <select id="searchCondition" name="searchCondition"
                            value={tempParams.searchCondition} onChange={handleInputChange}>
                            <option value="">선택</option>
                            <option value="conNm">콘텐츠명</option>
                            <option value="conSeq">순번</option>
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
                            onClick={() => handleOpenMutiModal()}>화면 등록</button>
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
                {modalOpen && (
                    <ContentMutiFormModal
                        open={modalOpen}
                        form={mutiForm}
                        setForm={setMutiForm}
                        conTypeOptions={conTypeOptions}
                        screenTypeOptions={screenTypeOptions}
                        playTypeOptions={playTypeOptions}
                        nextSeqOptions={nextSeqOptions}
                        onClose={() => setModalOpen(false)}
                        onSubmit={handleSubmit}
                    />
                )}
            </Suspense>
        </div>
    );
}
