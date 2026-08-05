import { useCallback, useEffect, useMemo, useRef, useState, Suspense, lazy } from 'react';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { useCommonDelete } from '@/hooks/use-common-delete.js';
import { useCustomReqDataCombo } from '@/hooks/use-combo-data.js';
import { useResetForm } from '@/hooks/use-form.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const GroupFormModal = lazy(() => import('./components/GroupFormModal.jsx'));

const GROUP_MAPPING = { id: 'groupId', text: 'groupNm' };

const INITIAL_SEARCH_FORM = {
    searchCondition: '',
    searchKeyword: '',
};

const EMPTY_GROUP_FORM = {
    mode: 'Ins',
    groupId: '',
    groupNm: '',
    groupDc: '',
    parentGroupId: '0',
    useYn: 'Y',
};

// LETTNAUTHORGROUPINFO 계층 전체를 한 번에 내려주는 API라 useGridInfinite(서버 offset 페이징)가
// 아니라 클라이언트 사이드 그리드로 처리한다(백엔드가 pageIndex/pageUnit을 실제로 반영하지 않음).
export default function GroupListPage() {
    const gridApiRef = useRef(null);
    const [tempParams, setTempParams] = useState(INITIAL_SEARCH_FORM);
    const [rowData, setRowData] = useState([]);
    const [loading, setLoading] = useState(false);

    const [modalOpen, setModalOpen] = useState(false);
    const [groupForm, setGroupForm] = useState(EMPTY_GROUP_FORM);

    const { options: groupOptions, refetch: refetchGroupCombo } = useCustomReqDataCombo({
        url: URL.GROUP_COMBO, method: 'GET', params: {}, mapping: GROUP_MAPPING,
    });

    const loadList = useCallback(async (query) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({ url: URL.GROUP_LIST, method: 'POST', data: query, showLoading: false });
            setRowData(res?.data?.result?.resultList || []);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { loadList(INITIAL_SEARCH_FORM); }, [loadList]);

    const handleInputChange = useCallback((e) => {
        const { name, value } = e.target;
        setTempParams((prev) => ({ ...prev, [name]: value }));
    }, []);

    const onSearch = useCallback(() => loadList(tempParams), [loadList, tempParams]);
    const onSearchKeyDown = useCallback((e) => { if (e.key === 'Enter') onSearch(); }, [onSearch]);
    const { handleReset } = useResetForm(setTempParams, INITIAL_SEARCH_FORM);

    const reload = useCallback(() => {
        loadList(tempParams);
        refetchGroupCombo();
    }, [loadList, tempParams, refetchGroupCombo]);

    const handleOpenGroupModal = useCallback((row) => {
        if (!row) {
            setGroupForm(EMPTY_GROUP_FORM);
        } else {
            setGroupForm({
                mode: 'Edt',
                groupId: row.groupId,
                groupNm: row.groupNm || '',
                groupDc: row.groupDc || '',
                parentGroupId: row.parentGroupId || '0',
                useYn: row.useYn || 'Y',
            });
        }
        setModalOpen(true);
    }, []);

    const handleSubmit = useCallback(async () => {
        if (!groupForm.groupNm) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '부서명을 입력해 주세요.' });
            return;
        }
        const action = groupForm.mode === 'Ins' ? '등록' : '수정';
        const ok = await Swal.fire({
            icon: 'question', title: `부서 ${action}`,
            html: `<b>${groupForm.groupNm}</b> ${action} 하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const res = await fnAjaxFetch({ url: URL.GROUP_UPDATE, method: 'POST', data: groupForm });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || `${action}되었습니다.` });
            setModalOpen(false);
            reload();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || `${action} 중 오류가 발생했습니다.` });
        }
    }, [groupForm, reload]);

    const { handleDelete } = useCommonDelete({
        gridApiRef,
        URL: URL.GROUP_INFO,
        MESSAGE: '부서 정보',
        reloadFunction: reload,
    });

    const columnDefs = useMemo(() => ([
        {
            headerName: '부서명', field: 'groupNm', flex: 1, minWidth: 160,
            cellRenderer: (p) => {
                const raw = p.value || '';
                // PartInfo — 부서명 트리 구조 시각화
                const name  = raw.replace(/^[└─\u3000\s]+/, '');
                const level = p.data?.lv || 0;
                const parentGroupId = p.data?.parentGroupId || '0';
                if ( parentGroupId === "0") {
                    return (
                        <span style={{ fontWeight: 600, color: 'var(--ipcc-text-primary)', display: 'flex', alignItems: 'center', gap: 5 }}>
                            <span style={{ color: '#3b82f6', fontSize: 10 }}>{'●'}</span>
                            {name}
                        </span>
                    );
                }
                return (
                    <span style={{ paddingLeft: (level - 1) * 16, display: 'flex', alignItems: 'center', gap: 4 }}>
                        <span style={{ color: 'var(--ipcc-text-placeholder)', flexShrink: 0, fontFamily: 'monospace', fontSize: 13, letterSpacing: -1 }}>{'└─'}</span>
                        <button className="btn btn-link p-0" onClick={() => handleOpenGroupModal(p.data)}>{p.value}</button>
                    </span>
                );
                
            },
        },
        { headerName: '상위부서', field: 'parentGroupNm', width: 160 },
        { headerName: '단계', field: 'lv', width: 80 },
        { headerName: '사용유무', field: 'useYn', width: 100 },
        {
            headerName: '수정', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-secondary btn-outline__gray btn-sm" onClick={() => handleOpenGroupModal(p.data)}>수정</button>
            ),
        },
        {
            headerName: '삭제', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={() => handleDelete({ code: p.data?.groupId, name: p.data?.groupNm })}
                >삭제</button>
            ),
        },
    ]), [handleOpenGroupModal, handleDelete]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">부서 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">인사 관리</li>
                        <li className="breadcrumb-item">부서 관리</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <select id="searchCondition" name="searchCondition"
                            value={tempParams.searchCondition} onChange={handleInputChange}>
                            <option value="">선택</option>
                            <option value="GROUP_ID">부서코드</option>
                            <option value="GROUP_NM">부서명</option>
                        </select>
                        <input type="text" id="searchKeyword" name="searchKeyword" placeholder="검색어를 입력하세요"
                            value={tempParams.searchKeyword}
                            onChange={handleInputChange}
                            onKeyDown={onSearchKeyDown}
                        />
                    </div>
                    <div className="col-auto content-search__action">
                        <button type="button" className="btn btn-outline-dark btn-outline__gray" onClick={onSearch}>검색</button>
                        <button type="button" className="btn btn-outline-dark btn-outline__gray" onClick={handleReset}>검색 초기화</button>
                        <button type="button" className="btn btn-primary btn-default__blue" onClick={() => handleOpenGroupModal()}>부서 등록</button>
                        <span style={{ marginLeft: 8, alignSelf: 'center', color: '#64748b' }}>
                            총 {rowData.length}건{loading ? ' (조회 중...)' : ''}
                        </span>
                    </div>
                </div>
            </div>

            <div className="col-12 content-table content-table__main">
                <div className="ag-theme-material" style={{ height: 760, width: '100%' }}>
                    <AppAgGrid
                        columnDefs={columnDefs}
                        rowData={rowData}
                        theme={gridTheme}
                        defaultColDef={{ resizable: true, sortable: true, filter: false }}
                        onGridReady={(params) => { gridApiRef.current = params.api; }}
                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                    />
                </div>
            </div>

            <Suspense fallback={null}>
                {modalOpen && (
                    <GroupFormModal
                        open={modalOpen}
                        form={groupForm}
                        setForm={setGroupForm}
                        groupOptions={groupOptions}
                        onClose={() => setModalOpen(false)}
                        onSubmit={handleSubmit}
                    />
                )}
            </Suspense>
        </div>
    );
}
