import { useCallback, useEffect, useMemo, useRef, useState, Suspense, lazy } from 'react';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { useResetForm } from '@/hooks/use-form.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const BrodDeployTimeModal = lazy(() => import('./components/BrodDeployTimeModal.jsx'));
const BrodScheduleFileRegisterModal = lazy(() => import('./components/BrodScheduleFileRegisterModal.jsx'));

const PAGE_UNIT = 20;
const FILE_PAGE_UNIT = 20;

const INITIAL_SEARCH_FORM = {
    searchCondition: 'CON_NM',
    searchKeyword: '',
};

const STATUS_SEARCH_INITIAL = {
    searchCondition: '', // '' = 콘텐츠명(브랜드명), 'CENTER_NM' = 지점명 — 백엔드 selectBrodScheduleStatusLst 분기 값 그대로
    searchKeyword: '',
    createCheck: '', // '' = 전체, 'Y' = 배포, 'N' = 미배포
};

const TABS = [
    { key: 'status', label: '배포 현황' },
    { key: 'deploy', label: '콘텐츠별 배포 관리' },
    { key: 'schedule', label: '스케줄 음원 관리' },
];

// 재생시간(초) → "분:초" — BrodContentDetailPage.jsx의 secToMinSec와 동일
const secToMinSec = (totalSec) => {
    const s = Number(totalSec) || 0;
    const min = Math.floor(s / 60);
    const sec = s - min * 60;
    return `${min}:${String(sec).padStart(2, '0')}`;
};

// 우측 편성 현황 한 행(매장 1곳)의 콘텐츠구분/재생위치 텍스트 — 레거시 print()의 분기 그대로
const scheduleRowInfo = (row) => {
    if (!row.brodSeq) return { gubun: '곡없음', time: '' };
    if (row.tbGubun === 'D') return { gubun: '일반방송', time: `${row.intervalSection}분` };
    const time = row.intervalSection === 'ANNGUBUN01'
        ? `${parseInt(row.anniversaryTime, 10) / 60}시간마다 ${row.anniversaryStartTime}분`
        : `${(row.anniversaryTime || '').slice(0, 2)}:${(row.anniversaryTime || '').slice(2, 4)}`;
    return { gubun: '특정방송', time };
};

// front_common VendorDetailInfo.jsx의 탭 스타일을 그대로 참고(프로젝트 공통 탭 UI 관례) —
// MhsRoomManagePage.jsx와 동일 스타일 상수
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

// "00000000"/빈값이면 미기록 — 그 외는 8자리(YYYYMMDD)를 YYYY-MM-DD로 표기
const formatDay = (v) => {
    if (!v || v === '00000000') return '';
    if (v.length === 8) return `${v.slice(0, 4)}-${v.slice(4, 6)}-${v.slice(6, 8)}`;
    return v;
};

// 레거시 brodScheduleStatus.jsp 참고 — 콘텐츠 배포 현황(읽기전용, brodCode 선택 없이 바로
// 전체 목록 조회)과 음원콘텐츠배포(콘텐츠 선택 → 매장별 배포/해제 토글, 원래 playShedule.do)를
// 별개 탭으로 분리했다. 첫 진입 시 기본 탭은 "배포 현황".
export default function BrodScheduleStatusPage() {
    const [tab, setTab] = useState('status');

    // ===== 배포 현황(읽기전용) =====
    const fetchStatusList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: URL.BROD_SCHEDULE_STATUS_LIST, method: 'POST', data: query });
        const data = res?.data;
        return {
            rows: data?.result?.resultList || [],
            total: data?.result?.totalCnt || 0,
        };
    }, []);

    const {
        onGridReady: onStatusGridReady,
        defaultColDef: statusDefaultColDef,
        tempParams: statusTempParams,
        setTempParams: setStatusTempParams,
        handleSearch: handleStatusSearch,
    } = useGridInfinite({
        fetchApi: fetchStatusList,
        pageUnit: PAGE_UNIT,
        initialFilters: STATUS_SEARCH_INITIAL,
    });

    const handleStatusInputChange = useCallback((e) => {
        const { name, value } = e.target;
        setStatusTempParams((prev) => ({ ...prev, [name]: value }));
    }, [setStatusTempParams]);

    const onStatusSearch = useCallback((pageIndex) => {
        handleStatusSearch(pageIndex || 1);
    }, [handleStatusSearch]);

    const onStatusSearchKeyDown = useCallback((e) => {
        if (e.key === 'Enter') onStatusSearch(1);
    }, [onStatusSearch]);

    const { handleReset: handleStatusReset } = useResetForm(setStatusTempParams, STATUS_SEARCH_INITIAL);

    const statusColumnDefs = useMemo(() => ([
        { field: 'brodName', headerName: '콘텐츠명', flex: 1, minWidth: 180 },
        { field: 'centerNm', headerName: '지점명', width: 140 },
        {
            headerName: '재생일자', width: 110,
            valueGetter: (p) => (p.data?.brodDay === '20991231' ? '평상시' : formatDay(p.data?.brodDay)),
        },
        {
            headerName: '기념일여부', width: 110, cellStyle: { textAlign: 'center' },
            valueGetter: (p) => (p.data?.brodDay === '20991231' ? '일반' : '기념일음원'),
        },
        {
            headerName: '지점운영시간', width: 140, cellStyle: { textAlign: 'center' },
            valueGetter: (p) => (p.data?.centerStartTime ?? '').replace('/', ' ~ '),
        },
        {
            field: 'createCheck', headerName: '배포여부', width: 100, cellStyle: { textAlign: 'center' },
            valueFormatter: (p) => (p.value === 'Y' ? '배포' : '미배포'),
        },
        {
            field: 'createRegDate', headerName: '배포생성일자', width: 130, cellStyle: { textAlign: 'center' },
            valueFormatter: (p) => formatDay(p.value),
        },
        {
            field: 'didDownCheck', headerName: '다운로드여부', width: 110, cellStyle: { textAlign: 'center' },
            valueFormatter: (p) => (p.value === 'Y' ? '다운완료' : '대기중'),
        },
        {
            field: 'didDownLoadDate', headerName: '다운로드일자', width: 160, cellStyle: { textAlign: 'center' },
            valueFormatter: (p) => p.value || '',
        },
    ]), []);

    // ===== 콘텐츠별 배포 관리(브랜치 콘텐츠 생성/삭제를 동반하는 매장 배포/해제 토글) =====
    const gridApiRef = useRef(null);

    const [selectedBrod, setSelectedBrod] = useState(null);
    const [centerList, setCenterList] = useState([]);
    const [rightSearchKeyword, setRightSearchKeyword] = useState('');

    const [deployModalOpen, setDeployModalOpen] = useState(false);
    const [deployTarget, setDeployTarget] = useState(null);

    const fetchContentList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: URL.BROD_SCHEDULE_LEFT_LIST, method: 'POST', data: query });
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
    } = useGridInfinite({
        fetchApi: fetchContentList,
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

    // ===== 배포 매장 패널 =====
    const loadCenterList = useCallback(async (brodCode, keyword) => {
        const res = await fnAjaxFetch({
            url: URL.BROD_SCHEDULE_RIGHT_LIST, method: 'GET',
            param: { brodCode, rightSearchKeyword: keyword ?? '' }, showLoading: false,
        });
        setCenterList(res?.data?.result?.resultList ?? []);
    }, []);

    const handleSelectContent = useCallback((row) => {
        setSelectedBrod(row);
        setRightSearchKeyword('');
        loadCenterList(row.brodCode, '');
    }, [loadCenterList]);

    const handleRightSearch = useCallback(() => {
        if (selectedBrod) loadCenterList(selectedBrod.brodCode, rightSearchKeyword);
    }, [selectedBrod, rightSearchKeyword, loadCenterList]);

    const applyToggle = useCallback(async (center, connect, centerStartTime, centerEndTime) => {
        if (!selectedBrod || !center) return;
        await fnAjaxFetch({
            url: URL.BROD_SCHEDULE_RIGHT_UPDATE,
            method: 'POST',
            data: {
                brodCode: selectedBrod.brodCode,
                checkVal: connect ? 'Y' : 'N',
                centerId: center.centerId,
                centerStartTime,
                centerEndTime,
            },
        });
        loadCenterList(selectedBrod.brodCode, rightSearchKeyword);
    }, [selectedBrod, rightSearchKeyword, loadCenterList]);

    const handleToggle = useCallback(async (center, connect) => {
        if (!selectedBrod) return;

        if (connect) {
            setDeployTarget(center);
            setDeployModalOpen(true);
            return;
        }

        const result = await Swal.fire({
            icon: 'question', title: '배포 해제', text: `${center.centerNm}의 배포를 해제하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        await applyToggle(center, false, center.centerStartTime || '0000', center.centerEndTime || '2359');
    }, [selectedBrod, applyToggle]);

    const handleDeploySubmit = useCallback(async ({ start, end }) => {
        setDeployModalOpen(false);
        await applyToggle(deployTarget, true, start, end);
    }, [deployTarget, applyToggle]);

    const columnDefs = useMemo(() => ([
        {
            headerName: '방송명', field: 'brodName', flex: 1, minWidth: 160,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0" onClick={() => handleSelectContent(p.data)}>{p.value}</button>
            ),
        },
        { field: 'brodCode', headerName: '방송코드', width: 150 },
        { field: 'centerNm', headerName: '매장', width: 120 },
    ]), [handleSelectContent]);

    // ===== 스케줄 음원 관리(음원 파일 하나를 여러 매장의 방송에 한 번에 편성/삭제) =====
    const [fileList, setFileList] = useState([]);
    const [fileTotalCnt, setFileTotalCnt] = useState(0);
    const [fileLoading, setFileLoading] = useState(false);
    const [filePageIndex, setFilePageIndex] = useState(1);
    const [fileSearchCondition, setFileSearchCondition] = useState('orignlFileNm');
    const [fileSearchKeyword, setFileSearchKeyword] = useState('');

    const [selectedFile, setSelectedFile] = useState(null);
    const [scheduleRightList, setScheduleRightList] = useState([]);
    const [scheduleRightLoading, setScheduleRightLoading] = useState(false);
    const [scheduleCenterKeyword, setScheduleCenterKeyword] = useState('');
    const [checkedCenterIds, setCheckedCenterIds] = useState(() => new Set());
    const [scheduleRegisterOpen, setScheduleRegisterOpen] = useState(false);

    const loadFileList = useCallback(async (pageIndex, condition, keyword) => {
        setFileLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.CON_FILE_LIST, method: 'POST',
                data: {
                    mediaType: 'MUSIC', searchCondition: condition, searchKeyword: keyword ?? '',
                    pageIndex, pageUnit: FILE_PAGE_UNIT,
                },
                showLoading: false,
            });
            const list = res?.data?.result?.resultList ?? [];
            setFileList(list);
            setFileTotalCnt(res?.data?.result?.totalCnt ?? list.length);
        } finally {
            setFileLoading(false);
        }
    }, []);

    const onFileSearch = useCallback(() => {
        setFilePageIndex(1);
        loadFileList(1, fileSearchCondition, fileSearchKeyword);
    }, [loadFileList, fileSearchCondition, fileSearchKeyword]);

    const onFileSearchKeyDown = useCallback((e) => {
        if (e.key === 'Enter') onFileSearch();
    }, [onFileSearch]);

    const handleFileReset = useCallback(() => {
        setFileSearchCondition('orignlFileNm');
        setFileSearchKeyword('');
        setFilePageIndex(1);
        loadFileList(1, 'orignlFileNm', '');
    }, [loadFileList]);

    const handleFilePage = useCallback((delta) => {
        const next = filePageIndex + delta;
        if (next < 1) return;
        setFilePageIndex(next);
        loadFileList(next, fileSearchCondition, fileSearchKeyword);
    }, [filePageIndex, loadFileList, fileSearchCondition, fileSearchKeyword]);

    const loadScheduleRight = useCallback(async (atchFileId) => {
        setScheduleRightLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.BROD_CONTENT_RIGHT, method: 'GET',
                param: { atchFileId }, showLoading: false,
            });
            setScheduleRightList(res?.data?.result?.resultList ?? []);
        } finally {
            setScheduleRightLoading(false);
        }
    }, []);

    const handleSelectFile = useCallback((file) => {
        setSelectedFile(file);
        setScheduleCenterKeyword('');
        setCheckedCenterIds(new Set());
        loadScheduleRight(file.atchFileId);
    }, [loadScheduleRight]);

    // 백엔드 right.do가 지점명 서버 필터를 받지 않아(atchFileId만 파라미터) 클라이언트에서 필터링한다.
    const filteredScheduleRightList = useMemo(() => {
        if (!scheduleCenterKeyword) return scheduleRightList;
        return scheduleRightList.filter((r) => (r.centerNm || '').includes(scheduleCenterKeyword));
    }, [scheduleRightList, scheduleCenterKeyword]);

    const toggleCheckCenter = useCallback((centerId) => {
        setCheckedCenterIds((prev) => {
            const next = new Set(prev);
            if (next.has(centerId)) next.delete(centerId); else next.add(centerId);
            return next;
        });
    }, []);

    const toggleCheckAllCenters = useCallback((checked) => {
        setCheckedCenterIds(checked ? new Set(filteredScheduleRightList.map((r) => r.centerId)) : new Set());
    }, [filteredScheduleRightList]);

    // 체크된 매장 중 실제로 방송(brodCode)이 배정된 매장만 등록/삭제 대상이 될 수 있다
    // (LEFT JOIN이라 방송 자체가 없는 매장은 brodCode가 null로 내려옴).
    const checkedRows = useMemo(
        () => filteredScheduleRightList.filter((r) => checkedCenterIds.has(r.centerId) && r.brodCode),
        [filteredScheduleRightList, checkedCenterIds],
    );

    const handleScheduleDelete = useCallback(async () => {
        const deletable = checkedRows.filter((r) => r.brodSeq);
        if (deletable.length === 0) {
            await Swal.fire({ icon: 'warning', title: '선택 확인', text: '삭제할 편성이 있는 매장을 선택해 주세요.' });
            return;
        }
        const result = await Swal.fire({
            icon: 'question', title: '편성 삭제', text: `선택한 ${deletable.length}개 매장의 편성을 삭제하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;

        const insertBrodCode = Array.from(new Set(checkedRows.map((r) => r.brodCode))).join(',');
        const res = await fnAjaxFetch({
            url: URL.BROD_CONTENT_RIGHT_DELETE, method: 'POST',
            data: {
                items: deletable.map((r) => ({ id: r.brodSeq, gubun: r.tbGubun })),
                insertBrodCode,
            },
        });
        if (res?.data?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: '삭제되었습니다.' });
            setCheckedCenterIds(new Set());
            loadScheduleRight(selectedFile.atchFileId);
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || '삭제 중 오류가 발생했습니다.' });
        }
    }, [checkedRows, loadScheduleRight, selectedFile]);

    const handleOpenScheduleRegister = useCallback(async () => {
        if (checkedRows.length === 0) {
            await Swal.fire({ icon: 'warning', title: '선택 확인', text: '등록할 매장을 선택해 주세요(방송이 배정된 매장만 선택 가능합니다).' });
            return;
        }
        setScheduleRegisterOpen(true);
    }, [checkedRows]);

    const handleScheduleRegisterSubmit = useCallback(() => {
        setScheduleRegisterOpen(false);
        setCheckedCenterIds(new Set());
        loadScheduleRight(selectedFile.atchFileId);
    }, [loadScheduleRight, selectedFile]);

    const scheduleRegisterBrodCodes = useMemo(() => checkedRows.map((r) => r.brodCode), [checkedRows]);

    // 탭을 처음 열 때 1회 음원 파일 목록을 불러온다("배포 현황"과 동일하게 클릭 없이 바로 조회).
    const fileListLoadedRef = useRef(false);
    useEffect(() => {
        if (tab === 'schedule' && !fileListLoadedRef.current) {
            fileListLoadedRef.current = true;
            loadFileList(1, fileSearchCondition, fileSearchKeyword);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [tab]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">방송 배포 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">방송 관리</li>
                        <li className="breadcrumb-item">{TABS.find((t) => t.key === tab)?.label}</li>
                    </ol>
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
                    {tab === 'status' && (
                        <div className="row g-0">
                            <div className="col-12 content-search">
                                <div className="row g-0 w-100 justify-content-between">
                                    <div className="col-auto content-search__option">
                                        <select name="searchCondition" value={statusTempParams.searchCondition} onChange={handleStatusInputChange}>
                                            <option value="">콘텐츠명</option>
                                            <option value="CENTER_NM">지점명</option>
                                        </select>
                                        <input type="text" name="searchKeyword" placeholder="검색어를 입력하세요"
                                            value={statusTempParams.searchKeyword}
                                            onChange={handleStatusInputChange}
                                            onKeyDown={onStatusSearchKeyDown}
                                        />
                                        <select name="createCheck" value={statusTempParams.createCheck} onChange={handleStatusInputChange}>
                                            <option value="">배포여부 전체</option>
                                            <option value="Y">배포</option>
                                            <option value="N">미배포</option>
                                        </select>
                                    </div>
                                    <div className="col-auto content-search__action">
                                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                                            onClick={() => onStatusSearch(1)}>검색</button>
                                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                                            onClick={handleStatusReset}>검색 초기화</button>
                                    </div>
                                </div>
                            </div>

                            <div className="col-12 content-table content-table__main">
                                <div className="ag-theme-material" style={{ height: 760, width: '100%' }}>
                                    <AppAgGrid
                                        columnDefs={statusColumnDefs}
                                        theme={gridTheme}
                                        defaultColDef={statusDefaultColDef}
                                        rowModelType="infinite"
                                        pagination={true}
                                        paginationPageSize={PAGE_UNIT}
                                        cacheBlockSize={PAGE_UNIT}
                                        maxBlocksInCache={2}
                                        onGridReady={onStatusGridReady}
                                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                                    />
                                </div>
                            </div>
                        </div>
                    )}

                    {tab === 'deploy' && (
                        <div className="row g-0">
                            <div className="col-12 content-search">
                                <div className="row g-0 w-100 justify-content-between">
                                    <div className="col-auto content-search__option">
                                        <input type="text" name="searchKeyword" placeholder="방송명 검색"
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
                                    </div>
                                </div>
                            </div>

                            {/* 좌측 콘텐츠 목록 / 우측 배포 매장 관리 — 레거시 playShedule.jsp처럼 항상 나란히 보이도록
                                2단 레이아웃(세로로 쌓으면 그리드 높이 때문에 우측 패널이 화면 밖으로 밀려남).
                                row g-0라 gutter가 없어 flex 래퍼로 직접 간격을 준다. */}
                            <div className="col-12" style={{ display: 'flex', gap: 16, flexWrap: 'wrap', alignItems: 'flex-start' }}>
                            <div className="content-table content-table__main" style={{ flex: '1 1 480px', minWidth: 360 }}>
                                <div className="ag-theme-material" style={{ height: 640, width: '100%' }}>
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
                                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                                    />
                                </div>
                            </div>

                            <div className="content-table" style={{
                                flex: '1 1 480px', minWidth: 360,
                                border: '1px solid #dde2eb', borderRadius: 8,
                                background: 'var(--bs-body-bg, #fff)', overflow: 'hidden',
                                height: 640, display: 'flex', flexDirection: 'column',
                            }}>
                                {!selectedBrod ? (
                                    <div style={{
                                        flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center',
                                        color: '#94a3b8', fontSize: 13,
                                    }}>
                                        좌측 목록에서 방송명을 선택하면 배포 매장 목록이 여기에 표시됩니다.
                                    </div>
                                ) : (
                                    <>
                                        <div style={{
                                            padding: '14px 20px 12px', borderBottom: '1px solid #dde2eb',
                                            display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 8,
                                        }}>
                                            <span style={{ fontSize: 15, fontWeight: 700 }}>배포 매장 관리 — {selectedBrod.brodName}</span>
                                            <div className="content-search__action">
                                                <input type="text" placeholder="매장명 검색"
                                                    value={rightSearchKeyword}
                                                    onChange={(e) => setRightSearchKeyword(e.target.value)}
                                                    onKeyDown={(e) => { if (e.key === 'Enter') handleRightSearch(); }}
                                                />
                                                <button type="button" className="btn btn-outline-dark btn-outline__gray"
                                                    onClick={handleRightSearch}>검색</button>
                                            </div>
                                        </div>
                                        <div style={{ padding: 16, overflowY: 'auto', flex: 1 }}>
                                            <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: 13 }}>
                                                <thead>
                                                    <tr style={{ background: '#f8fafc' }}>
                                                        <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 60 }}>적용</th>
                                                        <th style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'left' }}>매장</th>
                                                        <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 160 }}>배포시간</th>
                                                        <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 100 }}>배포여부</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {centerList.map((center) => (
                                                        <tr key={center.centerId}>
                                                            <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>
                                                                {/* 레거시 playShedule.jsp와 동일 — 체크박스 자체가 토글(onChange 즉시 배포/해제 호출),
                                                                    별도 등록 버튼 없음(레거시의 벌크 등록 버튼은 주석 처리된 채 배포된 적 없음) */}
                                                                <input type="checkbox"
                                                                    checked={center.brodCode === 'Y'}
                                                                    onChange={(e) => handleToggle(center, e.target.checked)}
                                                                />
                                                            </td>
                                                            <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>{center.centerNm}</td>
                                                            <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>
                                                                {center.centerStartTime} ~ {center.centerEndTime}
                                                            </td>
                                                            <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>
                                                                {center.brodCode === 'Y' ? '배포중' : '미배포'}
                                                            </td>
                                                        </tr>
                                                    ))}
                                                    {centerList.length === 0 && (
                                                        <tr><td colSpan={4} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>매장 정보가 없습니다.</td></tr>
                                                    )}
                                                </tbody>
                                            </table>
                                        </div>
                                    </>
                                )}
                            </div>
                            </div>

                            <Suspense fallback={null}>
                                {deployModalOpen && (
                                    <BrodDeployTimeModal
                                        open={deployModalOpen}
                                        centerNm={deployTarget?.centerNm}
                                        onClose={() => setDeployModalOpen(false)}
                                        onSubmit={handleDeploySubmit}
                                    />
                                )}
                            </Suspense>
                        </div>
                    )}

                    {tab === 'schedule' && (
                        <div className="row g-0">
                            {/* 좌측 음원 파일 목록 / 우측 편성 현황 — 레거시 brodContentPlayList.jsp와 동일하게
                                2단 레이아웃(row g-0라 gutter가 없어 flex 래퍼로 간격을 준다). */}
                            <div className="col-12" style={{ display: 'flex', gap: 16, flexWrap: 'wrap', alignItems: 'flex-start' }}>
                                <div className="content-table content-table__main" style={{
                                    flex: '1 1 420px', minWidth: 340,
                                    border: '1px solid #dde2eb', borderRadius: 8, overflow: 'hidden',
                                    background: 'var(--bs-body-bg, #fff)', display: 'flex', flexDirection: 'column', height: 640,
                                }}>
                                    <div style={{ padding: '12px 16px', borderBottom: '1px solid #dde2eb' }}>
                                        <div style={{ marginBottom: 8, color: '#64748b', fontSize: 13 }}>
                                            총 {fileTotalCnt}건{fileLoading ? ' (조회 중...)' : ''}
                                        </div>
                                        <div className="content-search__option" style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                                            <select value={fileSearchCondition} onChange={(e) => setFileSearchCondition(e.target.value)}>
                                                <option value="orignlFileNm">음원명</option>
                                                <option value="streFileNm">파일명</option>
                                            </select>
                                            <input type="text" placeholder="검색어를 입력하세요" style={{ flex: 1, minWidth: 120 }}
                                                value={fileSearchKeyword}
                                                onChange={(e) => setFileSearchKeyword(e.target.value)}
                                                onKeyDown={onFileSearchKeyDown}
                                            />
                                            <button type="button" className="btn btn-outline-dark btn-outline__gray"
                                                onClick={onFileSearch}>검색</button>
                                            <button type="button" className="btn btn-outline-dark btn-outline__gray"
                                                onClick={handleFileReset}>초기화</button>
                                        </div>
                                    </div>
                                    <div style={{ flex: 1, overflowY: 'auto' }}>
                                        <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: 13 }}>
                                            <thead>
                                                <tr style={{ background: '#f8fafc', position: 'sticky', top: 0 }}>
                                                    <th style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'left' }}>음원명</th>
                                                    <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 80 }}>재생시간</th>
                                                    <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 140 }}>파일명</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {fileList.map((f) => (
                                                    <tr key={f.atchFileId}
                                                        style={{
                                                            cursor: 'pointer',
                                                            background: selectedFile?.atchFileId === f.atchFileId ? '#fff7e0' : 'transparent',
                                                        }}
                                                        onClick={() => handleSelectFile(f)}>
                                                        <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>{f.orignlFileNm}</td>
                                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>{secToMinSec(f.playTime)}</td>
                                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, wordBreak: 'break-all' }}>{f.streFileNm}</td>
                                                    </tr>
                                                ))}
                                                {fileList.length === 0 && !fileLoading && (
                                                    <tr><td colSpan={3} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>조회된 음원 파일이 없습니다.</td></tr>
                                                )}
                                            </tbody>
                                        </table>
                                    </div>
                                    <div style={{
                                        padding: 8, borderTop: '1px solid #dde2eb',
                                        display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 12,
                                    }}>
                                        <button type="button" className="btn btn-outline-dark btn-outline__gray btn-sm"
                                            disabled={filePageIndex <= 1} onClick={() => handleFilePage(-1)}>이전</button>
                                        <span style={{ fontSize: 13, color: '#64748b' }}>{filePageIndex} 페이지</span>
                                        <button type="button" className="btn btn-outline-dark btn-outline__gray btn-sm"
                                            disabled={filePageIndex * FILE_PAGE_UNIT >= fileTotalCnt} onClick={() => handleFilePage(1)}>다음</button>
                                    </div>
                                </div>

                                <div className="content-table" style={{
                                    flex: '1 1 560px', minWidth: 360,
                                    border: '1px solid #dde2eb', borderRadius: 8, overflow: 'hidden',
                                    background: 'var(--bs-body-bg, #fff)', display: 'flex', flexDirection: 'column', height: 640,
                                }}>
                                    {!selectedFile ? (
                                        <div style={{
                                            flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center',
                                            color: '#94a3b8', fontSize: 13,
                                        }}>
                                            좌측 목록에서 음원 파일을 선택하면 매장별 편성 현황이 여기에 표시됩니다.
                                        </div>
                                    ) : (
                                        <>
                                            <div style={{
                                                padding: '14px 16px 12px', borderBottom: '1px solid #dde2eb',
                                                display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 8,
                                            }}>
                                                <span style={{ fontSize: 15, fontWeight: 700 }}>편성 현황 — {selectedFile.orignlFileNm}</span>
                                                <div className="content-search__action" style={{ display: 'flex', gap: 6 }}>
                                                    <input type="text" placeholder="지점명 검색"
                                                        value={scheduleCenterKeyword}
                                                        onChange={(e) => setScheduleCenterKeyword(e.target.value)}
                                                    />
                                                    <button type="button" className="btn btn-outline-danger btn-outline__gray btn-sm"
                                                        onClick={handleScheduleDelete}>삭제</button>
                                                    <button type="button" className="btn btn-primary btn-default__blue btn-sm"
                                                        onClick={handleOpenScheduleRegister}>등록</button>
                                                </div>
                                            </div>
                                            <div style={{ flex: 1, overflowY: 'auto', padding: '0 16px 16px' }}>
                                                {scheduleRightLoading ? (
                                                    <div style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>조회 중...</div>
                                                ) : (
                                                    <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: 13, marginTop: 12 }}>
                                                        <thead>
                                                            <tr style={{ background: '#f8fafc' }}>
                                                                <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 40 }}>
                                                                    <input type="checkbox"
                                                                        checked={filteredScheduleRightList.length > 0
                                                                            && filteredScheduleRightList.every((r) => checkedCenterIds.has(r.centerId))}
                                                                        onChange={(e) => toggleCheckAllCenters(e.target.checked)}
                                                                    />
                                                                </th>
                                                                <th style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'left' }}>지점명</th>
                                                                <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 100 }}>콘텐츠구분</th>
                                                                <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 150 }}>재생위치</th>
                                                                <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 90 }}>재생시간</th>
                                                                <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 150 }}>콘텐츠운영기간</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            {filteredScheduleRightList.map((row) => {
                                                                const info = scheduleRowInfo(row);
                                                                return (
                                                                    <tr key={row.centerId}>
                                                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>
                                                                            <input type="checkbox"
                                                                                checked={checkedCenterIds.has(row.centerId)}
                                                                                onChange={() => toggleCheckCenter(row.centerId)}
                                                                            />
                                                                        </td>
                                                                        <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>{row.centerNm}</td>
                                                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>{info.gubun}</td>
                                                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>{info.time}</td>
                                                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>
                                                                            {row.brodSeq ? secToMinSec(row.playTime) : ''}
                                                                        </td>
                                                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>
                                                                            {row.brodSeq ? `${formatDay(row.contentStartDay)} ~ ${formatDay(row.contentEndDay)}` : ''}
                                                                        </td>
                                                                    </tr>
                                                                );
                                                            })}
                                                            {filteredScheduleRightList.length === 0 && (
                                                                <tr><td colSpan={6} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>매장 정보가 없습니다.</td></tr>
                                                            )}
                                                        </tbody>
                                                    </table>
                                                )}
                                            </div>
                                        </>
                                    )}
                                </div>
                            </div>

                            <Suspense fallback={null}>
                                {scheduleRegisterOpen && selectedFile && (
                                    <BrodScheduleFileRegisterModal
                                        open={scheduleRegisterOpen}
                                        atchFileId={selectedFile.atchFileId}
                                        orignlFileNm={selectedFile.orignlFileNm}
                                        brodCodes={scheduleRegisterBrodCodes}
                                        onClose={() => setScheduleRegisterOpen(false)}
                                        onSubmit={handleScheduleRegisterSubmit}
                                    />
                                )}
                            </Suspense>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
