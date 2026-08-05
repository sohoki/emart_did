import { useCallback, useMemo, useRef, useState, Suspense, lazy } from 'react';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { useCustomReqDataCombo } from '@/hooks/use-combo-data.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { useResetForm } from '@/hooks/use-form.jsx';
import { useCommonDelete } from '@/hooks/use-common-delete.js';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';
import { useCommonCodeData } from '@/hooks/use-combo-data.js';

const CenterFormModal = lazy(() => import('./components/CenterFormModal.jsx'));
const CenterAnniModal = lazy(() => import('./components/CenterAnniModal.jsx'));

const GROUP_MAPPING = { id: 'groupId', text: 'groupNm' };
const BROD_MAPPING = { id: 'brodCode', text: 'brodName' };

const INITIAL_SEARCH_FORM = {
    searchCondition: '',
    searchKeyword: '',
};

const EMPTY_CENTER_FORM = {
    mode: 'Ins',
    centerId: '',
    centerNm: '',
    roleCode: '',
    centerStartTime: '',
    centerEndTime: '',
    centerZipcode1: '',
    centerZipcode2: '',
    centerAddr1: '',
    centerAddr2: '',
    centerGubun: '',
    brodCode: '',
    centerUseYn: 'Y',
    centerImgFile: null,
};

export default function CenterListPage() {

    //지점 구분 combo 만들기
    const { options: centerCodeOptions } = useCommonCodeData('EMT022');

    const gridApiRef = useRef(null);
    const fileInputRef = useRef(null);

    const [modalOpen, setModalOpen] = useState(false);
    const [centerForm, setCenterForm] = useState(EMPTY_CENTER_FORM);

    const [anniModalOpen, setAnniModalOpen] = useState(false);
    const [anniCenterId, setAnniCenterId] = useState('');

    const { options: groupOptions } = useCustomReqDataCombo({
        url: URL.GROUP_COMBO, method: 'GET', params: {}, mapping: GROUP_MAPPING,
    });
    const { options: brodOptions } = useCustomReqDataCombo({
        url: URL.BROD_CONTENT_COMBO, method: 'GET', params: {}, mapping: BROD_MAPPING,
    });

    const fetchCenterList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: URL.CENTER_LIST, method: 'POST', data: query });
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
        fetchApi: fetchCenterList,
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

    const handleExcelUploadClick = () => {
        fileInputRef.current?.click();
    };

    const handleExcelFileChange = async (e) => {
        const file = e.target.files?.[0];
        e.target.value = ''; // 같은 파일 재선택 가능하도록 초기화
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);

        const res = await fnAjaxFetch({
            url: URL.CENTER_EXCEL_UPLOAD,
            method: 'POST',
            data: formData,
        });

        const { successCount, failCount } = res?.data?.result?.result ?? {};
        await Swal.fire({
            icon: 'success',
            title: '엑셀 업로드 완료',
            text: `성공 ${successCount ?? 0}건, 실패 ${failCount ?? 0}건`,
        });
        refreshGrid();
    };

    const handleOpenCenterModal = useCallback(async (centerId) => {
        if (!centerId) {
            setCenterForm(EMPTY_CENTER_FORM);
            setModalOpen(true);
            return;
        }
        const res = await fnAjaxFetch({ url: `${URL.CENTER_INFO}/${centerId}.do`, method: 'GET' });
        const obj = res?.data?.result?.result || null;
        if (obj) {
            setCenterForm({
                mode: 'Edt',
                centerId: obj.centerId || '',
                centerNm: obj.centerNm || '',
                roleCode: obj.roleCode || '',
                centerStartTime: obj.centerStartTime || '',
                centerEndTime: obj.centerEndTime || '',
                centerZipcode1: obj.centerZipcode1 || '',
                centerZipcode2: obj.centerZipcode2 || '',
                centerAddr1: obj.centerAddr1 || '',
                centerAddr2: obj.centerAddr2 || '',
                centerGubun: obj.centerGubun || '',
                brodCode: obj.brodCode || '',
                centerUseYn: obj.centerUseYn || 'Y',
                centerImgFile: null,
            });
            setModalOpen(true);
        }
    }, []);

    const handleSubmit = useCallback(async () => {
        if (!centerForm.centerNm) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '지점명을 입력해 주세요.' });
            return;
        }
        if (centerForm.centerStartTime && centerForm.centerEndTime
            && centerForm.centerStartTime >= centerForm.centerEndTime) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '영업 시작 시간이 종료 시간보다 빠르거나 같습니다.' });
            return;
        }



        
        const action = centerForm.mode === 'Ins' ? '등록' : '수정';
        const ok = await Swal.fire({
            icon: 'question', title: `매장 ${action}`,
            html: `<b>${centerForm.centerNm}</b> ${action} 하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        // 백엔드가 MultipartRequest + 폼 바인딩(CenterInfo vo)을 쓰므로 JSON이 아니라 FormData로 전송
        const formData = new FormData();
        Object.entries(centerForm).forEach(([key, value]) => {
            if (key === 'centerImgFile') return;
            if (value !== null && value !== undefined) formData.append(key, value);
        });
        if (centerForm.centerImgFile) {
            formData.append('centerImg', centerForm.centerImgFile);
        }

        const res = await fnAjaxFetch({ url: URL.CENTER_UPDATE, method: 'POST', data: formData });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || `${action}되었습니다.` });
            setModalOpen(false);
            refreshGrid();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || `${action} 중 오류가 발생했습니다.` });
        }
    }, [centerForm, refreshGrid]);

    const { handleDelete } = useCommonDelete({
        gridApiRef,
        URL: URL.CENTER_INFO,
        MESSAGE: '매장 정보',
        reloadFunction: 'grid',
    });

    const columnDefs = useMemo(() => ([
        {
            field: 'centerId', headerName: '매장 ID', width: 130,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => handleOpenCenterModal(p.data?.centerId)}>{p.value}</button>
            ),
        },
        { field: 'centerNm', headerName: '매장명', flex: 1, minWidth: 200 },
        { field: 'codeNm', headerName: '구분', width: 110 },
        { field: 'centerStartTime', headerName: '운영시작', width: 100 },
        { field: 'centerEndTime', headerName: '운영종료', width: 100 },
        { field: 'centerUseYn', headerName: '사용유무', width: 90 },
        { field: 'centerRegdate', headerName: '등록일', width: 150 },
        {
            headerName: '기념일', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                    onClick={() => { setAnniCenterId(p.data.centerId); setAnniModalOpen(true); }}
                >기념일</button>
            ),
        },
        {
            headerName: '삭제', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={() => handleDelete({ code: p.data?.centerId, name: p.data?.centerNm })}
                >삭제</button>
            ),
        },
    ]), [handleOpenCenterModal, handleDelete]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">매장(센터) 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">기초 관리</li>
                        <li className="breadcrumb-item">매장(센터) 관리</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <select id="searchCondition" name="searchCondition"
                            value={tempParams.searchCondition} onChange={handleInputChange}>
                            <option value="">선택</option>
                            <option value="centerId">매장 ID</option>
                            <option value="centerNm">매장명</option>
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
                        <input
                            ref={fileInputRef}
                            type="file"
                            accept=".xls,.xlsx"
                            style={{ display: 'none' }}
                            onChange={handleExcelFileChange}
                        />
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={handleExcelUploadClick}>엑셀 일괄등록</button>
                        <button type="button" className="btn btn-primary btn-default__blue"
                            onClick={() => handleOpenCenterModal()}>매장 등록</button>
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
                    <CenterFormModal
                        open={modalOpen}
                        form={centerForm}
                        setForm={setCenterForm}
                        groupOptions={groupOptions}
                        brodOptions={brodOptions}
                        centerCodeOptions={centerCodeOptions}
                        onClose={() => setModalOpen(false)}
                        onSubmit={handleSubmit}
                    />
                )}
            </Suspense>

            <Suspense fallback={null}>
                {anniModalOpen && (
                    <CenterAnniModal
                        open={anniModalOpen}
                        centerId={anniCenterId}
                        onClose={() => setAnniModalOpen(false)}
                    />
                )}
            </Suspense>
        </div>
    );
}
