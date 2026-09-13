import { useCallback, useEffect, useMemo, useRef, useState, Suspense, lazy } from 'react';
import { useSearchParams } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { useResetForm } from '@/hooks/use-form.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const ScheduleFormModal = lazy(() => import('./components/ScheduleFormModal.jsx'));

const INITIAL_SEARCH_FORM = {
    searchCondition: '',
    searchKeyword: '',
};

const EMPTY_SCH_FORM = {
    mode: 'Ins', schCode: '', schName: '', schStartDay: '', schEndDay: '',
    groupCode: '', contentCode: '', schEmerGubun: 'N', schUseYn: 'Y',
};

// 발송 스케줄(방송 예약) 관리 — 레거시 schList.jsp/schDetail.jsp/schView.jsp 참고.
export default function ScheduleListPage() {
    const [searchParams] = useSearchParams();
    const gridApiRef = useRef(null);

    const [modalOpen, setModalOpen] = useState(false);
    const [schForm, setSchForm] = useState(EMPTY_SCH_FORM);
    const [groupOptions, setGroupOptions] = useState([]);
    const [contentOptions, setContentOptions] = useState([]);

    const fetchScheduleList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: URL.SCH_LIST, method: 'POST', data: query });
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
        fetchApi: fetchScheduleList,
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

    // ===== 등록/수정 모달 =====
    const loadGroupOptions = useCallback(async (keyword = '') => {
        const res = await fnAjaxFetch({
            url: URL.SCH_FORM_DATA, method: 'GET', param: { groupSearchKeyword: keyword }, showLoading: false,
        });
        setGroupOptions(res?.data?.result?.selectGroup || []);
    }, []);

    const loadContentOptions = useCallback(async (keyword = '') => {
        const res = await fnAjaxFetch({
            url: URL.SCH_CONTENT_SEARCH, method: 'GET', param: { searchKeyword: keyword }, showLoading: false,
        });
        setContentOptions(res?.data?.result?.resultList || []);
    }, []);

    // initialGroupCode: 단말기 상세(DidDetailPage)의 "스케줄 등록하기"에서 넘어올 때, 해당
    // 단말이 속한 그룹을 미리 선택해 둔 채로 등록 폼을 연다(레거시는 그런 프리필 없이 빈 폼만
    // 열었지만, 어차피 그룹을 골라야 하는 화면이라 넘어온 컨텍스트를 활용하는 쪽이 자연스러움).
    const handleOpenScheduleModal = useCallback(async (schCode, initialGroupCode) => {
        const res = await fnAjaxFetch({
            url: URL.SCH_FORM_DATA, method: 'GET',
            param: schCode ? { mode: 'Edt', schCode } : { mode: 'Ins' },
        });
        const result = res?.data?.result || {};
        setGroupOptions(result.selectGroup || []);
        setContentOptions(result.selectContent || []);
        if (schCode) {
            const obj = result.regist;
            if (!obj) return;
            setSchForm({
                mode: 'Edt',
                schCode: obj.schCode || '',
                schName: obj.schName || '',
                schStartDay: obj.schStartDay || '',
                schEndDay: obj.schEndDay || '',
                groupCode: obj.groupCode || '',
                contentCode: obj.contentCode || '',
                schEmerGubun: obj.schEmerGubun || 'N',
                schUseYn: obj.schUseYn || 'Y',
            });
        } else {
            setSchForm({ ...EMPTY_SCH_FORM, groupCode: initialGroupCode || '' });
        }
        setModalOpen(true);
    }, []);

    // 단말기 상세 화면에서 "스케줄 등록하기"로 넘어오면(?openInsert=1&groupCode=xxx) 목록
    // 진입과 동시에 등록 모달을 자동으로 연다(DidInfoList의 ?editDidId= 자동오픈과 동일 패턴).
    useEffect(() => {
        if (searchParams.get('openInsert')) {
            handleOpenScheduleModal(undefined, searchParams.get('groupCode') || '');
        }
    }, [searchParams, handleOpenScheduleModal]);

    const handleSubmit = useCallback(async () => {
        const action = schForm.mode === 'Ins' ? '등록' : '수정';
        const ok = await Swal.fire({
            icon: 'question', title: `스케줄 ${action}`,
            html: `<b>${schForm.schName}</b> ${action} 하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const res = await fnAjaxFetch({ url: URL.SCH_UPDATE, method: 'POST', data: schForm });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || `${action}되었습니다.` });
            setModalOpen(false);
            refreshGrid();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || `${action} 중 오류가 발생했습니다.` });
        }
    }, [schForm, refreshGrid]);

    // 그룹 내 단말이 존재하면 삭제를 막는다(레거시 check_del()/del_Sch()의 확인 로직 그대로).
    const groupHasDid = useCallback(async (groupCode) => {
        const res = await fnAjaxFetch({
            url: `${URL.DID_GROUP_MEMBER_LIST}/${encodeURIComponent(groupCode)}.do`, method: 'GET', showLoading: false,
        });
        return (res?.data?.result?.didLst || []).length > 0;
    }, []);

    const handleDeleteOne = useCallback(async () => {
        if (await groupHasDid(schForm.groupCode)) {
            await Swal.fire({
                icon: 'warning', title: '삭제 불가',
                text: '그룹 내 단말이 존재합니다. 그룹 내 단말이 존재하면 스케줄을 삭제할 수 없습니다.',
            });
            return;
        }
        const result = await Swal.fire({
            icon: 'question', title: '스케줄 삭제', text: '스케줄을 삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;

        const res = await fnAjaxFetch({ url: `${URL.SCH_INFO}/${schForm.schCode}.do`, method: 'DELETE' });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || '삭제되었습니다.' });
            setModalOpen(false);
            refreshGrid();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || '삭제 중 오류가 발생했습니다.' });
        }
    }, [schForm, groupHasDid, refreshGrid]);

    // ===== 일괄 삭제(체크박스) =====
    const handleBulkDelete = useCallback(async () => {
        const selected = gridApiRef.current?.getSelectedRows() || [];
        if (selected.length === 0) {
            await Swal.fire({ icon: 'warning', title: '선택 필요', text: '체크 하신 스케줄이 없습니다.' });
            return;
        }
        const result = await Swal.fire({
            icon: 'question', title: '스케줄 일괄 삭제',
            html: `선택한 <b>${selected.length}건</b>을 삭제하시겠습니까?<br><span style="font-size:12px;color:#94a3b8;">그룹 내 단말이 존재하는 스케줄은 삭제되지 않습니다.</span>`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;

        let deleteSuccess = 0;
        let deleteFail = 0;
        const okCodes = [];
        for (const row of selected) {
            // eslint-disable-next-line no-await-in-loop
            if (await groupHasDid(row.groupCode)) {
                deleteFail += 1;
            } else {
                okCodes.push(row.schCode);
            }
        }
        if (okCodes.length > 0) {
            const res = await fnAjaxFetch({
                url: URL.SCH_DELETE_BULK, method: 'DELETE', data: { schCode: okCodes.join(',') },
            });
            if (res?.data?.resultCodeInfo === 'SUCCESS') {
                deleteSuccess = okCodes.length;
            } else {
                deleteFail += okCodes.length;
            }
        }
        await Swal.fire({ icon: 'info', title: '삭제 결과', text: `삭제 완료: ${deleteSuccess}개, 삭제 거부: ${deleteFail}개` });
        if (deleteSuccess > 0) refreshGrid();
    }, [groupHasDid, refreshGrid]);

    // 레거시 preview_group() — 그룹 내 단말 리스트를 간단히 보여준다.
    const handlePreviewGroup = useCallback(async (groupCode, groupNm) => {
        const res = await fnAjaxFetch({
            url: `${URL.DID_GROUP_MEMBER_LIST}/${encodeURIComponent(groupCode)}.do`, method: 'GET', showLoading: false,
        });
        const didLst = res?.data?.result?.didLst || [];
        const html = didLst.length > 0
            ? `<table style="width:100%;font-size:13px;"><tbody>${didLst.map((d, i) => `<tr><td style="padding:4px;">${i + 1}</td><td style="padding:4px;text-align:left;">${d.didNm}</td></tr>`).join('')}</tbody></table>`
            : '<div style="color:#94a3b8;">그룹 내 단말이 없습니다.</div>';
        await Swal.fire({ icon: 'info', title: `[${groupNm}] 그룹 단말 리스트`, html });
    }, []);

    const columnDefs = useMemo(() => ([
        {
            headerName: '스케줄명', field: 'schName', flex: 1, minWidth: 160,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => handleOpenScheduleModal(p.data?.schCode)}>{p.value}</button>
            ),
        },
        {
            headerName: '기간', width: 180,
            valueGetter: (p) => `${p.data?.schStartDay ?? ''} ~ ${p.data?.schEndDay ?? ''}`,
        },
        {
            headerName: 'DID Group', field: 'groupNm', width: 160,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => handlePreviewGroup(p.data?.groupCode, p.value)}>{p.value}</button>
            ),
        },
        {
            headerName: '콘텐츠명', field: 'conNm', flex: 1, minWidth: 160,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0"
                    onClick={() => window.open(`/backoffice/sub/conManage/muti/edit?conSeq=${p.data?.contentCode}`, '_blank')}>
                    {p.value}
                </button>
            ),
        },
        {
            field: 'schEmerGubun', headerName: '긴급', width: 90, cellStyle: { textAlign: 'center' },
            valueFormatter: (p) => (p.value === 'Y' ? '긴급' : '일반'),
        },
        {
            field: 'schUseYn', headerName: '사용유무', width: 100, cellStyle: { textAlign: 'center' },
            valueFormatter: (p) => (p.value === 'Y' ? '사용' : '사용 안함'),
        },
    ]), [handleOpenScheduleModal, handlePreviewGroup]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">발송 스케줄 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">장비 관리</li>
                        <li className="breadcrumb-item">발송 스케줄 관리</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <select id="searchCondition" name="searchCondition" value={tempParams.searchCondition}
                            onChange={handleInputChange}>
                            <option value="">선택</option>
                            <option value="schName">스케줄명</option>
                            <option value="schCode">스케줄코드</option>
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
                        <button type="button" className="btn btn-outline-danger btn-outline__gray"
                            onClick={handleBulkDelete}>선택 삭제</button>
                        <button type="button" className="btn btn-primary btn-default__blue"
                            onClick={() => handleOpenScheduleModal()}>등록</button>
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
                        rowSelection={{ mode: 'multiRow', checkboxes: true, headerCheckbox: true }}
                        onGridReady={(params) => { gridApiRef.current = params.api; onGridReady(params); }}
                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                    />
                </div>
            </div>

            <Suspense fallback={null}>
                {modalOpen && (
                    <ScheduleFormModal
                        open={modalOpen}
                        form={schForm}
                        setForm={setSchForm}
                        groupOptions={groupOptions}
                        contentOptions={contentOptions}
                        onGroupSearch={loadGroupOptions}
                        onContentSearch={loadContentOptions}
                        onClose={() => setModalOpen(false)}
                        onSubmit={handleSubmit}
                        onDelete={handleDeleteOne}
                    />
                )}
            </Suspense>
        </div>
    );
}
