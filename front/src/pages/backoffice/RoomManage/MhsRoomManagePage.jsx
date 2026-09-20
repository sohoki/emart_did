import { useCallback, useEffect, useMemo, useState, Suspense, lazy } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { CommonSearchSelect } from '@/components/Common/Select.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { useCommonDelete } from '@/hooks/use-common-delete.js';
import { useResetForm } from '@/hooks/use-form.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';
import '@/style/RoomManage.css';

const MhsMonitorFormModal = lazy(() => import('./components/MhsMonitorFormModal.jsx'));
const MhsClassFormModal = lazy(() => import('./components/MhsClassFormModal.jsx'));
const MhsMonitorPreviewModal = lazy(() => import('./components/MhsMonitorPreviewModal.jsx'));

const PAGE_UNIT = 20;

// 강의 시작/종료일(yyyyMMdd)·시간(HHmm) 원본 값을 그리드에 보기 좋게 표시하기 위한 포맷터
// (저장/폼 입력값은 그대로 원본 포맷 유지 — MhsClassFormModal의 dashify/colonify와 동일 규칙)
const formatYmd = (v) => (v?.length === 8 ? `${v.slice(0, 4)}-${v.slice(4, 6)}-${v.slice(6, 8)}` : (v || ''));
const formatHm = (v) => (v?.length === 4 ? `${v.slice(0, 2)}:${v.slice(2, 4)}` : (v || ''));

const MONITOR_SEARCH_INITIAL = { searchCondition: '', searchKeyword: '' };
const EMPTY_MONITOR_FORM = {
    mode: 'Ins', mhsMonitorcd: '', mhsMonitornm: '', mhsBrandcd: '', mhsCentercd: '',
    mhsMviewtype: '1', mhsMonitorstatus: 'Y', mhsRemark: '',
};
const EMPTY_CLASS_FORM = {
    mode: 'Ins', mhsClasscd: '', mhsBrandcd: '', mhsCentercd: '', mhsClassroomnm: '', mhsClassnm: '', mhsTeachernm: '',
    mhsClassstartday: '', mhsClassendday: '', mhsClassdayofweek: '', mhsClassstarttime: '', mhsClassendtime: '',
    mhsClassintro: '',
};
const emptyConnForm = { mhsMonitorcd: '', mhsClasscd: '' };

const TABS = [
    { key: 'monitor', label: '모니터 관리' },
    { key: 'class', label: '강의 관리' },
    { key: 'viewConn', label: '편성표' },
];

// front_common VendorDetailInfo.jsx의 탭 스타일을 그대로 참고(프로젝트 공통 탭 UI 관례)
const tabBtnStyle = (isActive) => ({
    padding: '10px 20px',
    fontSize: '14px',
    border: isActive ? '1px solid #0d6efd' : '1px solid #dee2e6',
    borderBottom: isActive ? 'none' : '1px solid #dee2e6',
    background: isActive ? '#0d6efd' : '#f8f9fa',
    color: isActive ? '#fff' : '#6c757d',
    fontWeight: isActive ? '700' : 'normal',
    cursor: 'pointer',
    whiteSpace: 'nowrap',
    borderRadius: '6px 6px 0 0',
    transition: 'all 0.15s',
    marginBottom: isActive ? '-1px' : '0',
    position: 'relative',
});

const tabContainerStyle = {
    display: 'flex',
    alignItems: 'flex-end',
    gap: '4px',
    borderBottom: '1px solid #dee2e6',
    paddingBottom: '0',
    marginBottom: '0',
};

export default function MhsRoomManagePage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [tab, setTab] = useState('monitor');

    const [brandList, setBrandList] = useState([]);
    const [centerList, setCenterList] = useState([]);
    const [mhsBrandcd, setMhsBrandcd] = useState('');
    const [mhsCentercd, setMhsCentercd] = useState('');

    // 모니터 관리
    const [monitorModalOpen, setMonitorModalOpen] = useState(false);
    const [monitorForm, setMonitorForm] = useState(EMPTY_MONITOR_FORM);
    const [previewModalOpen, setPreviewModalOpen] = useState(false);
    const [previewMonitorcd, setPreviewMonitorcd] = useState('');

    // 강의 관리
    const [classList, setClassList] = useState([]);
    const [classTotalCnt, setClassTotalCnt] = useState(0);
    const [classLoading, setClassLoading] = useState(false);
    const [classModalOpen, setClassModalOpen] = useState(false);
    const [classForm, setClassForm] = useState(EMPTY_CLASS_FORM);
    const [classSearchKeyword, setClassSearchKeyword] = useState('');
    const [classSearchCondition, setClassSearchCondition] = useState('');

    // 편성표
    const [monitorCombo, setMonitorCombo] = useState([]);
    const [classCombo, setClassCombo] = useState([]);
    const [connForm, setConnForm] = useState(emptyConnForm);
    const [connList, setConnList] = useState([]);
    const [connLoading, setConnLoading] = useState(false);

    const loadBrandList = useCallback(async () => {
        const res = await fnAjaxFetch({ url: URL.MHS_BRAND_LIST, method: 'GET', showLoading: false });
        setBrandList(res?.data?.result?.resultList ?? []);
    }, []);

    const loadCenterList = useCallback(async (brandcd) => {
        if (!brandcd) {
            setCenterList([]);
            return;
        }
        const res = await fnAjaxFetch({
            url: URL.MHS_CENTER_LIST, method: 'GET', param: { mhsBrandcd: brandcd }, showLoading: false,
        });
        setCenterList(res?.data?.result?.resultList ?? []);
    }, []);

    useEffect(() => {
        if (!getCookie('accessToken')) {
            navigate('/login', { replace: true });
            return;
        }
        loadBrandList();
    }, [loadBrandList, navigate]);

    const handleBrandChange = (value) => {
        setMhsBrandcd(value);
        setMhsCentercd('');
        loadCenterList(value);
    };

    // 매장 수가 많아 검색이 필요 — CommonSearchSelect 형식({ code, codeNm })으로 변환
    // (MhsClassFormModal의 centerSelectOptions와 동일 규칙)
    const centerSelectOptions = useMemo(
        () => centerList.map((c) => ({ code: c.mhsCentercd, codeNm: c.mhsCenternm })),
        [centerList],
    );

    // ===== 모니터 관리 =====
    // ROLE_MHS_USER(개별 매장 담당자)는 백엔드가 로그인 사용자의 groupId/centerId 기준으로 권한
    // 스코프를 강제 적용하므로(CultureDisInfoManageController.selectMhsMonitorListByPagination)
    // 상단 브랜드/매장 선택과 무관하게 자기 매장만 보인다. 그 외 권한(통합관리자 등)은 여기서
    // 보내는 mhsBrandcd/mhsCentercd가 그대로 검색 필터로 반영된다.
    const fetchMonitorList = useCallback(async (query) => {
        const res = await fnAjaxFetch({
            url: URL.MHS_MONITOR_LIST, method: 'POST', data: { ...query, mhsBrandcd, mhsCentercd },
        });
        const data = res?.data;
        return {
            rows: data?.result?.resultList ?? [],
            total: data?.result?.totalCnt ?? data?.result?.resultList?.length ?? 0,
        };
    }, [mhsBrandcd, mhsCentercd]);

    const {
        gridApiRef: monitorGridApiRef,
        onGridReady: onMonitorGridReady,
        defaultColDef: monitorDefaultColDef,
        tempParams: monitorTempParams,
        setTempParams: setMonitorTempParams,
        handleSearch: handleMonitorSearch,
        refreshGrid: refreshMonitorGrid,
    } = useGridInfinite({
        fetchApi: fetchMonitorList,
        pageUnit: PAGE_UNIT,
        initialFilters: MONITOR_SEARCH_INITIAL,
    });

    // 상단 브랜드/매장 선택이 바뀌면 "모니터 관리" 탭도 즉시 재조회(강의 관리 탭과 동일 패턴).
    useEffect(() => {
        if (tab === 'monitor') refreshMonitorGrid({ keepPage: false });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [tab, mhsBrandcd, mhsCentercd]);

    const handleMonitorInputChange = useCallback((e) => {
        const { name, value } = e.target;
        setMonitorTempParams((prev) => ({ ...prev, [name]: value }));
    }, [setMonitorTempParams]);

    const onMonitorSearch = useCallback((pageIndex) => {
        handleMonitorSearch(pageIndex || 1);
    }, [handleMonitorSearch]);

    const onMonitorSearchKeyDown = useCallback((e) => {
        if (e.key === 'Enter') onMonitorSearch(1);
    }, [onMonitorSearch]);

    const { handleReset: handleMonitorReset } = useResetForm(setMonitorTempParams, MONITOR_SEARCH_INITIAL);

    const handleOpenMonitorModal = useCallback(async (mhsMonitorcd) => {
        if (!mhsMonitorcd) {
            setMonitorForm(EMPTY_MONITOR_FORM);
            setMonitorModalOpen(true);
            return;
        }
        const res = await fnAjaxFetch({ url: `${URL.MHS_MONITOR_INFO}/${mhsMonitorcd}.do`, method: 'GET' });
        const detail = res?.data?.result?.result;
        if (detail) {
            setMonitorForm({ ...EMPTY_MONITOR_FORM, ...detail, mode: 'Edt' });
            setMonitorModalOpen(true);
        }
    }, []);

    // 모니터 상세(MhsMonitorDetailPage)의 "정보 수정" 버튼에서 ?editMonitorcd=xxx로 넘어오면
    // 목록 진입과 동시에 해당 모니터의 수정 모달을 자동으로 연다(DidInfoList의 editDidId와 동일 패턴).
    useEffect(() => {
        const editMonitorcd = searchParams.get('editMonitorcd');
        if (editMonitorcd) handleOpenMonitorModal(editMonitorcd);
    }, [searchParams, handleOpenMonitorModal]);

    const handleMonitorSubmit = useCallback(async () => {
        const action = monitorForm.mode === 'Ins' ? '등록' : '수정';
        const ok = await Swal.fire({
            icon: 'question', title: `모니터 ${action}`,
            html: `<b>${monitorForm.mhsMonitornm}</b> ${action} 하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const res = await fnAjaxFetch({ url: URL.MHS_MONITOR_UPDATE, method: 'POST', data: monitorForm });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || `${action}되었습니다.` });
            setMonitorModalOpen(false);
            refreshMonitorGrid();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || `${action} 중 오류가 발생했습니다.` });
        }
    }, [monitorForm, refreshMonitorGrid]);

    const { handleDelete: handleMonitorDelete } = useCommonDelete({
        gridApiRef: monitorGridApiRef,
        URL: URL.MHS_MONITOR_INFO,
        MESSAGE: 'MHS 모니터',
        reloadFunction: 'grid',
    });

    // 레거시 preView.jsp(회의실알림이 DID 실제 송출 화면) 디자인을 참고한 모달로 표시.
    const handleMonitorPreview = useCallback((mhsMonitorcd) => {
        setPreviewMonitorcd(mhsMonitorcd);
        setPreviewModalOpen(true);
    }, []);

    const monitorColumnDefs = useMemo(() => ([
        {
            field: 'mhsBrandnm', headerName: '조직명', width: 140,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => handleOpenMonitorModal(p.data?.mhsMonitorcd)}>{p.value}</button>
            ),
        },
        { field: 'mhsCenternm', headerName: '점포명', width: 140 },
        {
            field: 'mhsMonitornm', headerName: '단말명', flex: 1, minWidth: 160,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => navigate(`/backoffice/sub/roomManage/mhs/monitor/view?mhsMonitorcd=${p.data?.mhsMonitorcd}`)}>
                    {p.value} ({p.data?.mhsMonitorcd})
                </button>
            ),
        },
        {
            headerName: '네트워크 정보', width: 160, sortable: false,
            valueGetter: (p) => `${p.data?.mhsIpaddr ?? ''} / ${p.data?.mhsMacaddr ?? ''}`,
        },
        {
            field: 'mhsLastconn', headerName: '연결 상태', width: 100, sortable: false, filter: false,
            cellStyle: { textAlign: 'center' },
            cellRenderer: (p) => (
                <span
                    title={p.value === 'ON' ? '연결됨' : '연결 안됨'}
                    style={{
                        display: 'inline-block', width: 12, height: 12, borderRadius: '50%',
                        background: p.value === 'ON' ? '#0d6efd' : '#94a3b8',
                    }}
                />
            ),
        },
        {
            headerName: '미리보기', width: 100, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                    onClick={() => handleMonitorPreview(p.data?.mhsMonitorcd)}>미리보기</button>
            ),
        },
        {
            headerName: '삭제', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={() => handleMonitorDelete({ code: p.data?.mhsMonitorcd, name: p.data?.mhsMonitornm })}
                >삭제</button>
            ),
        },
    ]), [handleOpenMonitorModal, handleMonitorPreview, handleMonitorDelete, navigate]);

    // ===== 강의 관리 =====
    const loadClassList = useCallback(async () => {
        setClassLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.MHS_CLASS_LIST, method: 'POST',
                data: {
                    searchMhsBramdCd: mhsBrandcd, searchMhsCenterCd: mhsCentercd,
                    searchCondition: classSearchCondition, searchKeyword: classSearchKeyword,
                    pageIndex: 1, pageUnit: PAGE_UNIT,
                },
                showLoading: false,
            });
            const list = res?.data?.result?.resultList ?? [];
            setClassList(list);
            setClassTotalCnt(res?.data?.result?.totalCnt ?? list.length);
        } finally {
            setClassLoading(false);
        }
    }, [mhsBrandcd, mhsCentercd, classSearchCondition, classSearchKeyword]);

    // 브랜드/매장 선택은 강의 목록의 선택적 필터일 뿐, 백엔드(selectMhsClassList)는 비어 있으면
    // 필터 없이 전체를 조회한다(authorCode == 'ROLE_MHS_USER'일 때는 서버가 자동으로 소속 매장
    // 범위로 제한). 그래서 브랜드/매장을 고르지 않아도 탭 진입 시 바로 전체 목록을 보여준다.
    useEffect(() => {
        if (tab === 'class') loadClassList();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [tab, mhsBrandcd, mhsCentercd]);

    const onClassSearch = useCallback(() => {
        if (tab === 'class') loadClassList();
    }, [tab, loadClassList]);

    const onClassSearchKeyDown = useCallback((e) => {
        if (e.key === 'Enter') onClassSearch();
    }, [onClassSearch]);

    const handleClassReset = useCallback(() => {
        setClassSearchCondition('');
        setClassSearchKeyword('');
    }, []);

    // 조직명/점포명은 모달 자체에서 선택하므로(MhsClassFormModal) 상단 검색용 브랜드/매장 선택과
    // 무관하게 항상 등록 모달을 열 수 있다. 다만 상단에서 이미 매장을 필터링해 둔 상태라면 그
    // 값을 등록 모달의 기본값으로 미리 채워준다(수정은 불가하지 않음 — 모달 안에서 자유롭게 변경 가능).
    const handleOpenClassModal = useCallback(async (mhsClasscd) => {
        if (!mhsClasscd) {
            setClassForm({ ...EMPTY_CLASS_FORM, mhsBrandcd, mhsCentercd });
            setClassModalOpen(true);
            return;
        }
        const res = await fnAjaxFetch({ url: `${URL.MHS_CLASS_INFO}/${mhsClasscd}.do`, method: 'GET' });
        const detail = res?.data?.result?.result;
        if (detail) {
            setClassForm({ ...EMPTY_CLASS_FORM, ...detail, mode: 'Edt' });
            setClassModalOpen(true);
        }
    }, [mhsBrandcd, mhsCentercd]);

    const handleClassDelete = useCallback(async (mhsClasscd) => {
        const result = await Swal.fire({
            icon: 'question', title: '강의 삭제', text: '삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        await fnAjaxFetch({ url: `${URL.MHS_CLASS_INFO}/${mhsClasscd}.do`, method: 'DELETE' });
        loadClassList();
    }, [loadClassList]);

    const handleClassSubmit = useCallback(async () => {
        const action = classForm.mode === 'Ins' ? '등록' : '수정';
        const ok = await Swal.fire({
            icon: 'question', title: `강의 ${action}`,
            html: `<b>${classForm.mhsClassnm}</b> ${action} 하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        // 조직명/점포명은 이제 classForm 자체에 들어있음(MhsClassFormModal에서 직접 선택) — 상단
        // 검색용 mhsBrandcd/mhsCentercd로 덮어쓰지 않는다.
        const res = await fnAjaxFetch({
            url: URL.MHS_CLASS_UPDATE, method: 'POST',
            data: classForm,
        });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || `${action}되었습니다.` });
            setClassModalOpen(false);
            loadClassList();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || `${action} 중 오류가 발생했습니다.` });
        }
    }, [classForm, loadClassList]);

    const classColumnDefs = useMemo(() => ([
        {
            field: 'mhsClassnm', headerName: '강의명', flex: 1, minWidth: 200,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => handleOpenClassModal(p.data?.mhsClasscd)}>{p.value}</button>
            ),
        },
        { field: 'mhsMonitornm', headerName: '강의실', width: 140 },
        { field: 'mhsTeachernm', headerName: '강사명', width: 120 },
        { field: 'mhsClassstartday', headerName: '시작일', width: 120, valueFormatter: (p) => formatYmd(p.value) },
        { field: 'mhsClassendday', headerName: '종료일', width: 120, valueFormatter: (p) => formatYmd(p.value) },
        { field: 'mhsClassstarttime', headerName: '시작시간', width: 100, valueFormatter: (p) => formatHm(p.value) },
        { field: 'mhsClassendtime', headerName: '종료시간', width: 100, valueFormatter: (p) => formatHm(p.value) },
        {
            headerName: '삭제', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={() => handleClassDelete(p.data?.mhsClasscd)}>삭제</button>
            ),
        },
    ]), [handleOpenClassModal, handleClassDelete]);

    // ===== 편성표 =====
    const loadCombosForConn = useCallback(async () => {
        if (!mhsCentercd) return;
        const monRes = await fnAjaxFetch({ url: URL.MHS_MONITOR_COMBO, method: 'GET', param: { mhsCentercd }, showLoading: false });
        setMonitorCombo(monRes?.data?.result?.resultList ?? []);
        const clsRes = await fnAjaxFetch({
            url: URL.MHS_CLASS_COMBO, method: 'POST', data: { mhsBrandcd, mhsCentercd }, showLoading: false,
        });
        setClassCombo(clsRes?.data?.result?.resultList ?? []);
    }, [mhsBrandcd, mhsCentercd]);

    const loadConnList = useCallback(async (mhsMonitorcd) => {
        if (!mhsMonitorcd) {
            setConnList([]);
            return;
        }
        setConnLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.MHS_VIEWCONN_LIST, method: 'POST', data: { mhsMonitorcd }, showLoading: false,
            });
            setConnList(res?.data?.result?.resultList ?? []);
        } finally {
            setConnLoading(false);
        }
    }, []);

    useEffect(() => {
        if (tab === 'viewConn' && mhsCentercd) loadCombosForConn();
    }, [tab, mhsCentercd, loadCombosForConn]);

    const handleConnMonitorChange = (value) => {
        setConnForm((p) => ({ ...p, mhsMonitorcd: value }));
        loadConnList(value);
    };

    const handleConnInsert = async (e) => {
        e.preventDefault();
        if (!connForm.mhsMonitorcd || !connForm.mhsClasscd) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: '모니터/강의를 선택해 주세요.' });
            return;
        }
        await fnAjaxFetch({ url: URL.MHS_VIEWCONN_INSERT, method: 'POST', data: connForm });
        loadConnList(connForm.mhsMonitorcd);
    };

    const handleConnDelete = useCallback(async (mhsConnSeq) => {
        const result = await Swal.fire({
            icon: 'question', title: '편성 삭제', text: '삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        await fnAjaxFetch({ url: `${URL.MHS_VIEWCONN_INFO}/${mhsConnSeq}.do`, method: 'DELETE' });
        loadConnList(connForm.mhsMonitorcd);
    }, [connForm.mhsMonitorcd, loadConnList]);

    const connColumnDefs = useMemo(() => ([
        { field: 'mhsClassnm', headerName: '강의명', flex: 1, minWidth: 160 },
        { field: 'mhsTeachernm', headerName: '강사명', width: 120 },
        {
            headerName: '시작~종료', width: 140,
            valueGetter: (p) => `${formatHm(p.data?.mhsClassstarttime)} ~ ${formatHm(p.data?.mhsClassendtime)}`,
        },
        { field: 'mhsClassdayofweek', headerName: '요일', width: 120 },
        {
            headerName: '삭제', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={() => handleConnDelete(p.data?.mhsConnSeq)}>삭제</button>
            ),
        },
    ]), [handleConnDelete]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">문화센터(MHS) 룸 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">룸 관리</li>
                        <li className="breadcrumb-item">{TABS.find((t) => t.key === tab)?.label}</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <select value={mhsBrandcd} onChange={(e) => handleBrandChange(e.target.value)}>
                            <option value="">브랜드 선택</option>
                            {brandList.map((b) => (
                                <option key={b.mhsBrandcd} value={b.mhsBrandcd}>{'  '.repeat(Math.max(0, Number(b.mhsBrandlv) - 1))}{b.mhsBrandnm}</option>
                            ))}
                        </select>

                        
                        {/* 매장이 많아 검색이 필요 — 순수 <select> 대신 검색 가능한 CommonSearchSelect 사용
                            (모달 안의 점포명 select와 동일 패턴). 폭은 옆의 브랜드 select와 맞춰 고정. */}
                        <div style={{ width: 200 }} className="col-auto content-search__option">
                            <CommonSearchSelect
                                comboId="mhsCentercd"
                                comboData={centerSelectOptions}
                                placeholder="매장 선택"
                                value={mhsCentercd}
                                onChange={(e) => setMhsCentercd(e.target.value)}
                                disabled={!mhsBrandcd}
                                className="mhs-center-search-select"
                            />
                        </div>
                    </div>
                    {tab === 'viewConn' && !mhsCentercd && (
                        <div className="col-auto" style={{ alignSelf: 'center', color: '#94a3b8', fontSize: 13 }}>
                            편성표 탭은 브랜드와 매장을 선택해야 조회됩니다. (강의 관리는 선택 없이도 전체 목록이 조회됩니다)
                        </div>
                    )}
                </div>
            </div>

            <div className="col-12" style={{ paddingTop: 16 }}>
                <ul className="nav" role="tablist" style={tabContainerStyle}>
                    {TABS.map((t) => (
                        <li key={t.key} className="nav-item me-2" role="presentation">
                            <button
                                type="button"
                                className={`nav-link${tab === t.key ? ' active' : ''}`}
                                style={tabBtnStyle(tab === t.key)}
                                data-bs-toggle="tab"
                                role="tab"
                                aria-selected={tab === t.key}
                                onClick={() => setTab(t.key)}
                            >
                                {t.label}
                            </button>
                        </li>
                    ))}
                </ul>
            </div>

            <div className="col-12">
                <div className="tab-content">
                    {tab === 'monitor' && (
                        <div className="row g-0">
                            <div className="col-12 content-search">
                                <div className="row g-0 w-100 justify-content-between">
                                    <div className="col-auto content-search__option">
                                        <select id="searchCondition" name="searchCondition"
                                            value={monitorTempParams.searchCondition} onChange={handleMonitorInputChange}>
                                            <option value="">선택</option>
                                            <option value="A.MHS_MONITORNM">단말명</option>
                                            <option value="A.MHS_MONITORCD">단말ID</option>
                                            <option value="A.MHS_IPADDR">IP</option>
                                            <option value="A.MHS_MACADDR">MAC</option>
                                        </select>
                                        <input type="text" id="searchKeyword" name="searchKeyword" placeholder="검색어를 입력하세요"
                                            value={monitorTempParams.searchKeyword}
                                            onChange={handleMonitorInputChange}
                                            onKeyDown={onMonitorSearchKeyDown}
                                        />
                                    </div>
                                    <div className="col-auto content-search__action">
                                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                                            onClick={() => onMonitorSearch(1)}>검색</button>
                                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                                            onClick={handleMonitorReset}>검색 초기화</button>
                                        <button type="button" className="btn btn-primary btn-default__blue"
                                            onClick={() => handleOpenMonitorModal()}>등록</button>
                                    </div>
                                </div>
                            </div>

                            <div className="col-12 content-table content-table__main">
                                <div className="ag-theme-material" style={{ height: 640, width: '100%' }}>
                                    <AppAgGrid
                                        columnDefs={monitorColumnDefs}
                                        theme={gridTheme}
                                        defaultColDef={monitorDefaultColDef}
                                        rowModelType="infinite"
                                        pagination={true}
                                        paginationPageSize={PAGE_UNIT}
                                        cacheBlockSize={PAGE_UNIT}
                                        maxBlocksInCache={2}
                                        onGridReady={onMonitorGridReady}
                                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                                    />
                                </div>
                            </div>

                            <Suspense fallback={null}>
                                {monitorModalOpen && (
                                    <MhsMonitorFormModal
                                        open={monitorModalOpen}
                                        form={monitorForm}
                                        setForm={setMonitorForm}
                                        onClose={() => setMonitorModalOpen(false)}
                                        onSubmit={handleMonitorSubmit}
                                    />
                                )}
                                {previewModalOpen && (
                                    <MhsMonitorPreviewModal
                                        open={previewModalOpen}
                                        mhsMonitorcd={previewMonitorcd}
                                        onClose={() => setPreviewModalOpen(false)}
                                    />
                                )}
                            </Suspense>
                        </div>
                    )}

                    {tab === 'class' && (
                        <div className="row g-0">
                            <div className="col-12 content-search">
                                <div className="row g-0 w-100 justify-content-between">
                                    <div className="col-auto content-search__option">
                                        <select value={classSearchCondition} onChange={(e) => setClassSearchCondition(e.target.value)}>
                                            <option value="">선택</option>
                                            <option value="MHS_CLASSNM">강의명</option>
                                            <option value="MHS_TEACHERNM">강사명</option>
                                            <option value="MHS_CLASSROOMNM">강의실</option>
                                        </select>
                                        <input type="text" placeholder="검색어를 입력하세요"
                                            value={classSearchKeyword}
                                            onChange={(e) => setClassSearchKeyword(e.target.value)}
                                            onKeyDown={onClassSearchKeyDown}
                                        />
                                    </div>
                                    <div className="col-auto content-search__action">
                                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                                            onClick={onClassSearch}>검색</button>
                                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                                            onClick={handleClassReset}>검색 초기화</button>
                                        <button type="button" className="btn btn-primary btn-default__blue"
                                            onClick={() => handleOpenClassModal()}>등록</button>
                                    </div>
                                </div>
                            </div>

                            <div className="col-12 content-table content-table__main">
                                <div style={{ marginBottom: 8, color: '#64748b' }}>총 {classTotalCnt}건{classLoading ? ' (조회 중...)' : ''}</div>
                                <div className="ag-theme-material" style={{ height: 560, width: '100%' }}>
                                    <AppAgGrid
                                        theme={gridTheme}
                                        rowData={classList}
                                        columnDefs={classColumnDefs}
                                        defaultColDef={{ sortable: true, resizable: true }}
                                        pagination
                                        paginationPageSize={PAGE_UNIT}
                                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                                    />
                                </div>
                            </div>

                            <Suspense fallback={null}>
                                {classModalOpen && (
                                    <MhsClassFormModal
                                        open={classModalOpen}
                                        form={classForm}
                                        setForm={setClassForm}
                                        onClose={() => setClassModalOpen(false)}
                                        onSubmit={handleClassSubmit}
                                    />
                                )}
                            </Suspense>
                        </div>
                    )}

                    {tab === 'viewConn' && (
                        <div className="row g-0">
                            <div className="col-12 content-search">
                                <div className="row g-0 w-100 justify-content-between">
                                    <div className="col-auto content-search__option">
                                        <select value={connForm.mhsMonitorcd} onChange={(e) => handleConnMonitorChange(e.target.value)}>
                                            <option value="">모니터 선택</option>
                                            {monitorCombo.map((m) => (
                                                <option key={m.mhsMonitorcd} value={m.mhsMonitorcd}>{m.mhsMonitornm}</option>
                                            ))}
                                        </select>
                                        <select value={connForm.mhsClasscd} onChange={(e) => setConnForm((p) => ({ ...p, mhsClasscd: e.target.value }))}>
                                            <option value="">강의 선택</option>
                                            {classCombo.map((c) => (
                                                <option key={c.mhsClasscd} value={c.mhsClasscd}>{c.mhsClassnm}</option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="col-auto content-search__action">
                                        <button type="button" className="btn btn-primary btn-default__blue"
                                            onClick={handleConnInsert}>편성 등록</button>
                                    </div>
                                </div>
                            </div>

                            <div className="col-12 content-table content-table__main">
                                <div style={{ marginBottom: 8, color: '#64748b' }}>{connLoading ? '조회 중...' : `총 ${connList.length}건`}</div>
                                <div className="ag-theme-material" style={{ height: 560, width: '100%' }}>
                                    <AppAgGrid
                                        theme={gridTheme}
                                        rowData={connList}
                                        columnDefs={connColumnDefs}
                                        defaultColDef={{ sortable: true, resizable: true }}
                                        pagination
                                        paginationPageSize={PAGE_UNIT}
                                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>모니터를 선택해 주세요.</span>"
                                    />
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
