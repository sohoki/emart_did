import { useCallback, useEffect, useState } from 'react';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import URL from '@/constants/URL.jsx';
import DashboardTabs from './DashboardTabs.jsx';

const STATUS_COLORS = { 정상: '#22c55e', 장애: '#f59e0b', 오프라인: '#94a3b8' };

// 레거시와 동일하게 장애 건수는 별도 집계가 없어 0으로 표시한다
const toChartData = (state) => ([
    { name: '정상', value: Number(state?.onCnt ?? 0) },
    { name: '장애', value: 0 },
    { name: '오프라인', value: Number(state?.offCnt ?? 0) },
]);

const StatusPieCard = ({ title, state }) => {
    const data = toChartData(state);
    const total = data.reduce((sum, d) => sum + d.value, 0);

    return (
        <div style={{ flex: 1, minWidth: 320, border: '1px solid #cbd5e1', borderRadius: 8, padding: 16, background: '#fff' }}>
            <h3 style={{ fontSize: 15, fontWeight: 700, margin: '0 0 8px' }}>{title}</h3>
            <div style={{ display: 'flex', gap: 16, fontSize: 13, color: '#334155', marginBottom: 8 }}>
                <span>총 단말 <b>{state?.didCnt ?? total}</b>대</span>
                {data.map((d) => (
                    <span key={d.name}>
                        <span style={{ color: STATUS_COLORS[d.name] }}>●</span> {d.name} <b>{d.value}</b>대
                    </span>
                ))}
            </div>
            <div style={{ width: '100%', height: 300 }}>
                {total > 0 ? (
                    <ResponsiveContainer>
                        <PieChart>
                            <Pie data={data} dataKey="value" nameKey="name" innerRadius={60} outerRadius={110}
                                label={({ name, value }) => (value > 0 ? `${name} ${value}` : '')}>
                                {data.map((d) => <Cell key={d.name} fill={STATUS_COLORS[d.name]} />)}
                            </Pie>
                            <Tooltip formatter={(v) => `${v}대`} />
                            <Legend />
                        </PieChart>
                    </ResponsiveContainer>
                ) : (
                    <div style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#94a3b8' }}>
                        데이터가 없습니다.
                    </div>
                )}
            </div>
        </div>
    );
};

// 단말기 및 음원방송 통합 모니터링 — 레거시 dashboardState.jsp 참고.
// state.do는 조회 전에 최종 접속시간 기준으로 단말 상태(DID_STTUS)를 갱신한 뒤 요약을 반환한다.
export default function DashboardStatePage() {
    const [didState, setDidState] = useState(null);
    const [brodState, setBrodState] = useState(null);
    const [loading, setLoading] = useState(false);

    const loadState = useCallback(async () => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({ url: URL.DASHBOARD_STATE, method: 'GET', showLoading: false });
            setDidState(res?.data?.result?.resultListDid ?? null);
            setBrodState(res?.data?.result?.resultListBrod ?? null);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        (async () => {
            await loadState();
        })();
    }, [loadState]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">통합 모니터링</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">장비 관리</li>
                        <li className="breadcrumb-item">모니터링</li>
                        <li className="breadcrumb-item">통합 모니터링</li>
                    </ol>
                </div>
            </div>

            <DashboardTabs />

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between align-items-center">
                    <div className="col-auto" style={{ color: '#334155' }}>
                        단말기 및 음원방송 통합 모니터링현황입니다.{loading ? ' (조회 중...)' : ''}
                    </div>
                    <div className="col-auto content-search__action">
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={loadState} disabled={loading}>새로고침</button>
                    </div>
                </div>
            </div>

            <div className="col-12 content-table content-table__main">
                <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
                    <StatusPieCard title="단말기 모니터링현황" state={didState} />
                    <StatusPieCard title="음원방송 모니터링현황" state={brodState} />
                </div>
            </div>
        </div>
    );
}
