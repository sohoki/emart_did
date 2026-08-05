import { useCallback, useMemo, useRef, useState, Suspense, lazy } from 'react';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { useResetForm } from '@/hooks/use-form.jsx';
import { useCommonDelete } from '@/hooks/use-common-delete.js';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';
import { useCommonCodeData } from '@/hooks/use-combo-data.js';

const XmlFormModal = lazy(() => import('./components/XmlFormModal.jsx'));
const XmlPreviewModal = lazy(() => import('./components/XmlPreviewModal.jsx'));

const INITIAL_SEARCH_FORM = {
    searchCondition: '',
    searchKeyword: '',
};

const EMPTY_XML_FORM = {
    mode: 'Ins',
    xmlSeq: '',
    workGubun: '',
    xmlProcessName: '',
    processRemark: '',
    xmlInputParam: '',
    xmlInputParamSample: '',
    xmlOutputParam: '',
    xmlExplain: '',
    testOk: 'N',
    idCheck: 'N',
};

// XML(장비 통신 명령) 정보 관리 — 레거시 xmlList.jsp 참고.
export default function XmlListPage() {
    const gridApiRef = useRef(null);

    const [modalOpen, setModalOpen] = useState(false);
    const [xmlForm, setXmlForm] = useState(EMPTY_XML_FORM);

    const [previewOpen, setPreviewOpen] = useState(false);
    const [previewTitle, setPreviewTitle] = useState('');
    const [previewContent, setPreviewContent] = useState('');

    const { options: workGubunOptions } = useCommonCodeData('EMT006');

    const fetchXmlList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: URL.XML_LIST, method: 'POST', data: query });
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
        fetchApi: fetchXmlList,
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

    const handleOpenXmlModal = useCallback(async (xmlSeq) => {
        if (!xmlSeq) {
            setXmlForm(EMPTY_XML_FORM);
            setModalOpen(true);
            return;
        }
        const res = await fnAjaxFetch({ url: `${URL.XML_INFO}/${xmlSeq}.do`, method: 'GET' });
        const obj = res?.data?.result?.result;
        if (obj) {
            setXmlForm({
                mode: 'Edt',
                xmlSeq: obj.xmlSeq || '',
                workGubun: obj.workGubun || '',
                xmlProcessName: obj.xmlProcessName || '',
                processRemark: obj.processRemark || '',
                xmlInputParam: obj.xmlInputParam || '',
                xmlInputParamSample: obj.xmlInputParamSample || '',
                xmlOutputParam: obj.xmlOutputParam || '',
                xmlExplain: obj.xmlExplain || '',
                testOk: obj.testOk || 'N',
                idCheck: 'Y',
            });
            setModalOpen(true);
        }
    }, []);

    const handleSubmit = useCallback(async () => {
        if (!xmlForm.workGubun) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '전문구분을 선택해 주세요.' });
            return;
        }
        if (!xmlForm.xmlProcessName) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '명령어(Process Name)를 입력해 주세요.' });
            return;
        }
        if (xmlForm.mode === 'Ins' && xmlForm.idCheck !== 'Y') {
            await Swal.fire({ icon: 'warning', title: '확인 필요', text: '명령어 중복확인이 안되었습니다.' });
            return;
        }

        const action = xmlForm.mode === 'Ins' ? '등록' : '수정';
        const ok = await Swal.fire({
            icon: 'question', title: `전문 ${action}`,
            html: `<b>${xmlForm.xmlProcessName}</b> ${action} 하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const res = await fnAjaxFetch({ url: URL.XML_UPDATE, method: 'POST', data: xmlForm });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || `${action}되었습니다.` });
            setModalOpen(false);
            refreshGrid();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || `${action} 중 오류가 발생했습니다.` });
        }
    }, [xmlForm, refreshGrid]);

    const { handleDelete } = useCommonDelete({
        gridApiRef,
        URL: URL.XML_INFO,
        MESSAGE: '전문 정보',
        reloadFunction: 'grid',
    });

    const handlePreview = useCallback(async (type, xmlSeq, xmlProcessName) => {
        const base = type === 'json' ? URL.XML_PREVIEW_JSON : URL.XML_PREVIEW_XML;
        const res = await fnAjaxFetch({ url: `${base}/${xmlSeq}.do`, method: 'GET' });
        setPreviewTitle(`${type === 'json' ? 'JSON' : 'XML'} 미리보기 — ${xmlProcessName}`);
        setPreviewContent(res?.data?.result?.result ?? '');
        setPreviewOpen(true);
    }, []);

    const columnDefs = useMemo(() => ([
        { field: 'codeNm', headerName: '구분', width: 110 },
        {
            field: 'xmlProcessName', headerName: '프로세스 ID', flex: 1, minWidth: 200,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => handleOpenXmlModal(p.data?.xmlSeq)}>{p.value}</button>
            ),
        },
        { field: 'processRemark', headerName: '프로세스 업무', flex: 1, minWidth: 200 },
        {
            headerName: 'JSON 미리보기', width: 130, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                    onClick={() => handlePreview('json', p.data?.xmlSeq, p.data?.xmlProcessName)}
                >미리보기</button>
            ),
        },
        {
            headerName: 'XML 미리보기', width: 130, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                    onClick={() => handlePreview('xml', p.data?.xmlSeq, p.data?.xmlProcessName)}
                >미리보기</button>
            ),
        },
        {
            field: 'testOk', headerName: '확인', width: 90,
            valueFormatter: (p) => (p.value === 'Y' ? '확인' : '미확인'),
        },
        {
            headerName: '삭제', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={() => handleDelete({ code: p.data?.xmlSeq, name: p.data?.xmlProcessName })}
                >삭제</button>
            ),
        },
    ]), [handleOpenXmlModal, handlePreview, handleDelete]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">전문 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">운영 관리</li>
                        <li className="breadcrumb-item">전문 관리</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <select id="searchCondition" name="searchCondition"
                            value={tempParams.searchCondition} onChange={handleInputChange}>
                            <option value="">선택</option>
                            <option value="XML_PROCESS_NAME">전문명</option>
                            <option value="processRemark">전문설명</option>
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
                            onClick={() => handleOpenXmlModal()}>등록</button>
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
                    <XmlFormModal
                        open={modalOpen}
                        form={xmlForm}
                        setForm={setXmlForm}
                        workGubunOptions={workGubunOptions}
                        onClose={() => setModalOpen(false)}
                        onSubmit={handleSubmit}
                    />
                )}
            </Suspense>

            <Suspense fallback={null}>
                {previewOpen && (
                    <XmlPreviewModal
                        open={previewOpen}
                        title={previewTitle}
                        content={previewContent}
                        onClose={() => setPreviewOpen(false)}
                    />
                )}
            </Suspense>
        </div>
    );
}
