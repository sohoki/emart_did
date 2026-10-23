import { useCallback, useMemo, useState } from 'react';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import URL from '@/constants/URL.jsx';
import DashboardTabs from './DashboardTabs.jsx';

const PAGE_UNIT = 20;

// did: 단말기(DIDRMODELTYPE01/02), brod: 음원방송(DIDRMODELTYPE03)
const KIND_CONFIG = {
    did: {
        title: '단말기 모니터링',
        desc: '총 단말기 모니터링현황입니다.',
        url: URL.DASHBOARD_STATE_DID,
        summaryKey: 'resultListDid',
    },
    brod: {
        title: '음원방송 모니터링',
        desc: '총 음원방송 모니터링현황입니다.',
        url: URL.DASHBOARD_STATE_BROD,
        summaryKey: 'resultListBrod',
    },
};

const STATUS_STYLE = {
    OFFLINE: { color: '#475569', background: '#e2e8f0' },
    ERROR: { color: '#b45309', background: '#fef3c7' },
};

const INITIAL_SEARCH_FORM = {};

// 단말기/음원방송 모니터링 — 레거시 dashboardState1.jsp / dashboardState2.jsp 참고.
// 목록은 정상(DID_STTUS='Y')이 아닌 단말만 최종 접속시간 역순으로 보여준다.
export default function DashboardStatusListPage({ kind }) {
    const config = KIND_CONFIG[kind];
    const [summary, setSummary] = useState(null);
    const [totalCnt, setTotalCnt] = useState(0);

    const fetchList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: config.url, method: 'GET', param: query, showLoading: false });
        const result = res?.data?.result;
        setSummary(result?.[config.summaryKey] ?? null);
        setTotalCnt(result?.totalCnt ?? 0);
        return {
            rows: result?.resultList || [],
            total: result?.totalCnt || 0,
        };
    }, [config]);

    const { onGridReady, defaultColDef, handleSearch } = useGridInfinite({
        fetchApi: fetchList,
        pageUnit: PAGE_UNIT,
        initialFilters: INITIAL_SEARCH_FORM,
    });

    const columnDefs = useMemo(() => ([
        {
            field: 'didStatus', headerName: '상태', width: 120,
            cellRenderer: (p) => (
                <span style={{ ...STATUS_STYLE[p.value], padding: '2px 10px', borderRadius: 10, fontSize: 12, fontWeight: 600 }}>
                    {p.value}
                </span>
            ),
        },
        { field: 'centerNm', headerName: '지점', width: 200 },
        { headerName: '단말수', width: 100, valueGetter: () => '1대' },
        {
            headerName: '단말명', flex: 1, minWidth: 220,
            valueGetter: (p) => (p.data ? `${p.data.didNm ?? ''} : ${p.data.didId ?? ''}` : ''),
        },
        { field: 'didEndContime', headerName: '최종 접속시간', width: 200 },
    ]), []);

    // 레거시 dashboardState2.jsp는 음원방송 오프라인 수를 요약값 대신 목록 건수(totalCnt)로 표시했다
    const offlineCnt = kind === 'brod' ? totalCnt : (summary?.offCnt ?? 0);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">{config.title}</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">장비 관리</li>
                        <li className="breadcrumb-item">모니터링</li>
                        <li className="breadcrumb-item">{config.title}</li>
                    </ol>
                </div>
            </div>

            <DashboardTabs />

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between align-items-center">
                    <div className="col-auto" style={{ display: 'flex', gap: 20, fontSize: 14, color: '#334155' }}>
                        <span>{config.desc}</span>
                        <span>총 단말 <b>{summary?.didCnt ?? 0}</b>대</span>
                        <span>장애 <b>0</b>대</span>
                        <span>오프라인 <b>{offlineCnt}</b>대</span>
                    </div>
                    <div className="col-auto content-search__action">
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={() => handleSearch(1)}>새로고침</button>
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
                        onGridReady={onGridReady}
                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                    />
                </div>
            </div>
        </div>
    );
}
