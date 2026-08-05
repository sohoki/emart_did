import { useCallback, useEffect, useMemo, useRef, useState, Suspense, lazy } from 'react';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { useCommonDelete } from '@/hooks/use-common-delete.js';
import { useResetForm } from '@/hooks/use-form.jsx';
import URL from '@/constants/URL.jsx';

import MasterDetailGrid from '@/components/Common/MasterDetailGrid.jsx';
import DidGroupDetailCellRenderer from './components/DidGroupDetailCellRenderer.jsx';
const DidGroupFormModal = lazy(() => import('./components/DidGroupFormModal.jsx'));



const INITIAL_SEARCH_FORM = {
    searchCondition: '',
    searchKeyword: '',
};

const INITIAL_DID_GROUP_FORM = {
    mode: 'Ins',
    groupCode: '',
    groupNm: '',
    idCheck: 'N',
};

export default function DIdGroupInfo() {
    const gridApiRef = useRef(null);

    const [tempParams, setTempParams] = useState(INITIAL_SEARCH_FORM);
    const [pageUnit] = useState(20);
    const [rowData, setRowData] = useState([]);

    const [groupForm, setGroupForm] = useState(INITIAL_DID_GROUP_FORM);
    const [groupModalOpen, setGroupModalOpen] = useState(false);

    const fetchDidGroupList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: URL.DID_GROUP_LIST, method: 'POST', data: query });
        const data = res?.data;
        return {
            rows: data?.result?.resultList || [],
            total: data?.result?.paginationInfo?.totalRecordCount || 0,
        };
    }, []);

    const onSearch = useCallback(async (pageIndex) => {
        const req = { ...tempParams, pageIndex: String(pageIndex), pageUnit: String(pageUnit) };
        const { rows } = await fetchDidGroupList(req);
        setRowData(rows);
    }, [tempParams, pageUnit, fetchDidGroupList]);

    useEffect(() => {
        fetchDidGroupList({ searchCondition: '', searchKeyword: '', pageIndex: '1', pageUnit: String(pageUnit) })
            .then(({ rows }) => setRowData(rows))
            .catch(() => {});
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    const handleInputChange = useCallback((e) => {
        const { name, value } = e.target;
        setTempParams((prev) => ({ ...prev, [name]: value }));
    }, []);

    const onSearchKeyDown = useCallback((e) => {
        if (e.key === 'Enter') onSearch(1);
    }, [onSearch]);

    const { handleReset } = useResetForm(setTempParams, INITIAL_SEARCH_FORM);

    const openGroupModal = useCallback((groupCode, rawData) => {
        if (!groupCode) {
            setGroupForm({ ...INITIAL_DID_GROUP_FORM, idCheck: 'N' });
        } else {
            setGroupForm({
                mode: 'Edt',
                groupCode,
                groupNm: rawData?.groupNm || '',
                idCheck: 'Y',
            });
        }
        setGroupModalOpen(true);
    }, []);

    const { handleDelete: handleGroupDelete } = useCommonDelete({
        gridApiRef,
        URL: URL.DID_GROUP_DELETE,
        MESSAGE: 'DID 그룹 정보',
        reloadFunction: onSearch,
    });

    const colModel = useMemo(() => [
        {
            headerName: '그룹코드', field: 'groupCode',
            cellStyle: { textAlign: 'left' },
            cellRenderer: 'agGroupCellRenderer',
        },
        { headerName: '그룹명', field: 'groupNm', cellStyle: { textAlign: 'left' }, flex: 1 },
        {
            headerName: '단말기수', field: 'didCnt', width: 100, cellStyle: { textAlign: 'center' },
            valueFormatter: (p) => `${p.value ?? 0}개`,
        },
        { headerName: '사용유무', field: 'groupUseYn', width: 90, cellStyle: { textAlign: 'center' } },
        {
            headerName: '수정', cellStyle: { textAlign: 'center' }, width: 80, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-secondary btn-outline__gray btn-sm"
                    onClick={(e) => { e.preventDefault(); openGroupModal(p.data?.groupCode, p.data); }}
                >수정</button>
            ),
        },
        {
            headerName: '삭제', cellStyle: { textAlign: 'center' }, width: 80, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={(e) => { e.preventDefault();
                        handleGroupDelete({ code: p.data?.groupCode, name: p.data?.groupNm });
                    }}
                >삭제</button>
            ),
        },
    ], [openGroupModal, handleGroupDelete]);

    return (
        <>
            <div className="row g-0 main-contents">
                <div className="col-12 content-header">
                    <div className="content-header__title">단말기 그룹 관리</div>
                    <div className="content-header__breadcrumb">
                        <ol className="breadcrumb">
                            <li className="breadcrumb-item">장비 관리</li>
                            <li className="breadcrumb-item">단말기 그룹 관리</li>
                        </ol>
                    </div>
                </div>
                <div className="col-12 content-search">
                    <div className="row g-0 w-100 justify-content-between">
                        <div className="col-auto content-search__option">
                            <select id="searchCondition" name="searchCondition"
                                value={tempParams.searchCondition} onChange={handleInputChange}>
                                <option value="">선택</option>
                                <option value="GROUP_ID">그룹코드</option>
                                <option value="GROUP_NM">그룹명</option>
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
                                onClick={() => openGroupModal()}>그룹 등록</button>
                        </div>
                    </div>
                </div>
                <div className="col-12 content-table content-table__main">
                    <MasterDetailGrid
                        columnDefs={colModel}
                        rowData={rowData}
                        getRowId={(params) => params.data.groupCode}
                        isRowMaster={(data) => (data ? !!data.groupCode : false)}
                        detailCellRenderer={DidGroupDetailCellRenderer}
                        detailRowHeight={320}
                        pageSize={pageUnit}
                        onGridReady={(params) => { gridApiRef.current = params.api; }}
                    />
                </div>
            </div>
            <Suspense fallback={null}>
                {groupModalOpen && (
                    <DidGroupFormModal
                        open={groupModalOpen}
                        form={groupForm}
                        setForm={setGroupForm}
                        onClose={() => setGroupModalOpen(false)}
                        onSubmit={async () => {
                            setGroupModalOpen(false);
                            await onSearch(1);
                        }}
                    />
                )}
            </Suspense>
        </>
    );
}
