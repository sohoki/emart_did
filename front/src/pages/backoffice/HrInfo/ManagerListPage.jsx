import { useCallback, useEffect, useMemo, useRef, useState, Suspense, lazy } from 'react';
import { useNavigate } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import { useCommonDelete } from '@/hooks/use-common-delete.js';
import { useResetForm } from '@/hooks/use-form.jsx';
import URL from '@/constants/URL.jsx';
import { useCustomReqDataCombo } from '@/hooks/use-combo-data.js';
import { useCommonCodeData } from '@/hooks/use-combo-data.js';


const ManagerAdminFormModal = lazy(() => import('./components/ManagerAdminFormModal.jsx'));

const INITIAL_SEARCH_FORM = {
    searchCondition: '',
    searchKeyword: '',
};

const EMPTY_MANAGER_FORM = {
    mode: 'Ins',
    managerId: '',
    managerName: '',
    managerPassword: '',
    partId: '',
    roleId: '',
    centerId: '',
    managerEmail: '',
    managerTel: '',
    managerPosition: '',
    managerStatus: 'STATE_01',
    useYn: 'Y',
    idCheck: 'N',
};
const CENTER_MAPPING = { id: 'centerId', text: 'centerNm' };
const GROUP_MAPPING = { id: 'groupId', text: 'groupNm' };

export default function ManagerListPage() {

    //상태
    // 관리자 상태 공통코드는 COM003(STATE_01=입사/STATE_02=휴직/STATE_03=퇴사)
    const { options: adminStateOptions } = useCommonCodeData('COM003');
    //부서, 센터 
    const { options: centerOptions } = useCustomReqDataCombo({
        url: URL.CENTER_COMBO, params: {}, mapping: CENTER_MAPPING,
    });
    const { options: groupOptions } = useCustomReqDataCombo({
        url: URL.GROUP_COMBO, params: {}, mapping: GROUP_MAPPING,
    });


    


    const navigate = useNavigate();
    const searchRef = useRef(null);
    const [pageUnit] = useState(20);

    const [modalOpen, setModalOpen] = useState(false);
    const [managerForm, setManagerForm] = useState(EMPTY_MANAGER_FORM);

    const fetchManagerList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: URL.MANAGER_LIST, method: 'POST', data: query });
        const data = res?.data;
        return {
            rows: data?.result?.resultList || [],
            total: data?.result?.paginationInfo?.totalRecordCount || 0,
        };
    }, []);

    const {
        gridApiRef,
        onGridReady,
        defaultColDef,
        tempParams,
        setTempParams,
        handleSearch,
    } = useGridInfinite({
        fetchApi: fetchManagerList,
        pageUnit,
        initialFilters: INITIAL_SEARCH_FORM,
    });

    useEffect(() => {
        if (!getCookie('accessToken')) {
            navigate('/login', { replace: true });
        }
    }, [navigate]);

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

    const handleOpenManagerModal = useCallback(async (managerId) => {
        if (!managerId) {
            setManagerForm(EMPTY_MANAGER_FORM);
            setModalOpen(true);
            return;
        }
        const res = await fnAjaxFetch({ url: `${URL.MANAGER_DETAIL}/${managerId}.do`, method: 'GET' });
        const obj = res?.data?.result?.result || null;
        if (obj) {
            setManagerForm({
                mode: 'Edt',
                managerId: obj.managerId || '',
                managerName: obj.managerName || '',
                managerPassword: '',
                partId: obj.partId || '',
                roleId: obj.roleId || '',
                centerId: obj.centerId || '',
                managerEmail: obj.managerEmail || '',
                managerTel: obj.managerTel || '',
                managerPosition: obj.managerPosition || '',
                managerStatus: obj.managerStatus || 'STATE_01',
                useYn: obj.useYn || 'Y',
                idCheck: 'Y',
            });
            setModalOpen(true);
        }
    }, []);

    const { handleDelete } = useCommonDelete({
        gridApiRef,
        URL: URL.MANAGER_DELETE,
        MESSAGE: `<b>${managerForm.managerName || ''}</b>`,
        reloadFunction: () => onSearch(1),
    });

    const columnDefs = useMemo(() => ([
        { headerName: '아이디', field: 'managerId', width: 140,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => handleOpenManagerModal(p.data?.managerId)}>
                    {p.value}
                </button>
            ),
        },
        { headerName: '이름', field: 'managerName', width: 120 },
        { headerName: '부서', field: 'partNm', flex: 1, minWidth: 160 },
        { headerName: '권한', field: 'roleName', width: 140 },
        { headerName: '매장 스코프', field: 'centerId', width: 120, valueFormatter: (p) => p.value || '전체' },
        { headerName: '상태', field: 'managerStatus', width: 100 },
        { headerName: '사용유무', field: 'useYn', width: 90 },
        { headerName: '이메일', field: 'managerEmail', flex: 1, minWidth: 180 },
        { headerName: '연락처', field: 'managerTel', width: 130 },
        { headerName: '최종수정일', field: 'lastUpdtPnttm', width: 150 },
        {
            headerName: '삭제', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={() => handleDelete({ code: p.data?.managerId, name: p.data?.managerName })}
                >삭제</button>
            ),
        },
    ]), [handleOpenManagerModal, handleDelete]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">관리자 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">인사 관리</li>
                        <li className="breadcrumb-item">관리자 관리</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <select id="searchCondition" name="searchCondition" ref={searchRef}
                            value={tempParams.searchCondition} onChange={handleInputChange}>
                            <option value="">선택</option>
                            <option value="managerId">아이디</option>
                            <option value="managerName">이름</option>
                            <option value="managerEmail">이메일</option>
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
                            onClick={() => handleOpenManagerModal()}>관리자 등록</button>
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
                        paginationPageSize={pageUnit}
                        cacheBlockSize={pageUnit}
                        maxBlocksInCache={2}
                        rowSelection={{ mode: 'singleSelect' }}
                        onGridReady={onGridReady}
                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                    />
                </div>
            </div>

            <Suspense fallback={null}>
                {modalOpen && (
                    <ManagerAdminFormModal
                        open={modalOpen}
                        form={managerForm}
                        setForm={setManagerForm}
                        onClose={() => setModalOpen(false)}
                        setModalOpen={setModalOpen}
                        onSearch={onSearch}
                        onData={{ adminStateOptions, centerOptions, groupOptions }}
                    />
                )}
            </Suspense>
        </div>
    );
}
