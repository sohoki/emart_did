import { useEffect, useState, useRef, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import URL from '@/constants/URL.jsx';

const WEEK_LABEL = ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일'];

// hhmm(4자리) -> "hh:mm"
const timeSplit = (v) => (v && v.length >= 4 ? `${v.slice(0, 2)}:${v.slice(2, 4)}` : (v || ''));

const isActive = (nowHHmm, cls) => (
    Number(nowHHmm) >= Number(cls.mhsClassstarttime) && Number(cls.mhsClassendtime) > Number(nowHHmm)
);

const REFETCH_MS = 60 * 1000; // 레거시 preView.jsp와 동일 — 1분마다 편성 재조회
const RELOAD_MS = 60 * 60 * 1000; // 레거시와 동일 — 1시간마다 전체 새로고침(장시간 켜둔 브라우저 메모리 누수 방지 목적으로 추정)

// 문화센터(MHS) 룸 단말(안드로이드 키오스크) 실제 화면 — 레거시 emart_cms3.2.1의
// roomManage/preView.jsp를 did_emart로 승격한 버전. AppLayout/ProtectedRoute를 거치지 않는
// 독립 라우트(routes/index.jsx 참고)이고, 데이터도 로그인이 필요 없는 공개 API
// (URL.MHS_DEVICE_PREVIEW, 백엔드 SecurityConfig.AUTH_GET_WHITELIST 등록됨)를 사용한다 —
// 단말은 로그인 세션이 없는 키오스크 브라우저이기 때문.
// 실제 장비 화면(preView.do)과 동일하게 진행중/예정 강의가 있으면 시간표, 없으면 "강의 없음"
// 화면 하나만 보여준다(스크롤 없음).
export default function MhsMonitorDevicePage() {
    const [searchParams] = useSearchParams();
    const mhsMonitorcd = searchParams.get('mhsMonitorcd') ?? '';

    const [now, setNow] = useState(new Date());
    const [data, setData] = useState(null);
    const reloadElapsedRef = useRef(0);

    const loadPreview = useCallback(async () => {
        if (!mhsMonitorcd) return;
        const res = await fnAjaxFetch({
            url: URL.MHS_DEVICE_PREVIEW, method: 'GET', param: { mhsMonitorcd },
            showLoading: false, suppressErrorHandling: true, redirectOnNoAuth: null,
        });
        setData(res?.data?.result ?? null);
    }, [mhsMonitorcd]);

    useEffect(() => { loadPreview(); }, [loadPreview]);

    // 1초마다 시계 갱신 + 1분마다 편성 재조회 + 1시간마다 전체 새로고침(레거시 day_view() 동일 동작)
    useEffect(() => {
        const timer = setInterval(() => {
            setNow(new Date());
            reloadElapsedRef.current += 1000;
            if (reloadElapsedRef.current % REFETCH_MS === 0) loadPreview();
            if (reloadElapsedRef.current >= RELOAD_MS) window.location.reload();
        }, 1000);
        return () => clearInterval(timer);
    }, [loadPreview]);

    if (!mhsMonitorcd) {
        return <div style={{ padding: 40, fontSize: 24 }}>mhsMonitorcd 파라미터가 없습니다.</div>;
    }

    const nowHHmm = `${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`;
    const todayList = data?.resultList ?? [];
    const pageInfo = data?.pageInfo ?? [];
    const nextList = pageInfo.filter((p) => p.mhsNowgubun === 'NEXT');
    // 진행중/예정 강의(종료된 강의 제외) — 있으면 시간표 화면, 없으면 "강의 없음" 화면(preView.do와 동일)
    const remainList = todayList.filter((c) => Number(c.mhsClassendtime) > Number(nowHHmm));

    return (
        <div style={{
            width: '100vw', height: '100vh', display: 'flex', flexDirection: 'column',
            background: '#fff', fontFamily: 'inherit', overflow: 'hidden',
        }}>
            {/* 헤더 — 레거시 .header */}
            <div style={{
                padding: '45px 60px', borderBottom: '2px solid #d8d9db',
                display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', flexShrink: 0,
            }}>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: 40 }}>
                    <span style={{ fontSize: 63, fontWeight: 700, lineHeight: 0.8 }}>{data?.monitorInfo?.mhsMonitornm ?? '-'}</span>
                    <span style={{ fontSize: 43, color: '#bfbfbf' }}>{data?.monitorInfo?.mhsCenternm ?? '-'}</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: 30 }}>
                    <div style={{ fontSize: 30, color: '#333', textAlign: 'right', lineHeight: 1 }}>
                        <div>{now.getMonth() + 1}월 {now.getDate()}일</div>
                        <div>{WEEK_LABEL[now.getDay()]}</div>
                    </div>
                    <div style={{ fontSize: 80, fontWeight: 300, lineHeight: 0.6 }}>{now.getHours()}시 {now.getMinutes()}분</div>
                </div>
            </div>

            {/* 본문 — 화면 높이 안에서만 표시(스크롤 없음) */}
            <div style={{ flex: 1, minHeight: 0, overflow: 'hidden', padding: '0 60px' }}>
                {remainList.length === 0 ? (
                    /* 레거시 slide02 — 진행중인 강의 없음: 회색 배지(현재시각~다음 강의 시작 또는 23:00) + 다음 강의 */
                    <div style={{ marginTop: 90 }}>
                        <div style={{ fontSize: 56, color: '#919191', lineHeight: 1.5 }}>강의 제목</div>
                        <h3 style={{ margin: '15px 0 55px', fontSize: 84, fontWeight: 600, color: '#3d3d3d', lineHeight: 1.2 }}>현재 진행중인 강의가 없습니다</h3>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 30, flexWrap: 'wrap' }}>
                            <div style={{
                                padding: '49px 80px', borderRadius: 100,
                                background: '#a0a0a0', color: '#fff', fontSize: 41, fontWeight: 600,
                            }}>
                                {timeSplit(nowHHmm)}~{nextList.length > 0 ? timeSplit(nextList[0].mhsClassstarttime) : '23:00'}
                            </div>
                            <div style={{
                                display: 'inline-flex', gap: 20, background: '#2e2e2e', color: '#d6d6d6',
                                borderRadius: 15, padding: '17px 31px', fontSize: 37, fontWeight: 200,
                            }}>
                                <span style={{ borderRight: '2px solid #808080', paddingRight: 20 }}>다음 강의</span>
                                <span>
                                    {nextList.length > 0
                                        ? `${timeSplit(nextList[0].mhsClassstarttime)}~${timeSplit(nextList[0].mhsClassendtime)} ${nextList[0].mhsTeachernm}`
                                        : '예정된 강의가 없습니다.'}
                                </span>
                            </div>
                        </div>
                    </div>
                ) : (
                    /* 레거시 slide01 — 문화센터 강의 현황(진행중 강의는 빨간 배지) */
                    <>
                        <h2 style={{ color: '#5f5f5f', fontSize: 42, fontWeight: 500, padding: '61px 0 45px', margin: 0 }}>문화센터 강의 현황</h2>
                        <table style={{
                            width: '100%', borderCollapse: 'collapse', color: '#898989', lineHeight: 2.4,
                            textAlign: 'center', borderTop: '5px solid #5f5f5f', borderBottom: '3px solid #cdcdcd',
                        }}>
                            <thead>
                                <tr>
                                    <th style={{ fontSize: 30, padding: '15px 10px', fontWeight: 500, borderBottom: '2px solid #cdcdcd' }}>시간</th>
                                    <th style={{ fontSize: 30, padding: '15px 10px', fontWeight: 500, borderBottom: '2px solid #cdcdcd' }}>강의명</th>
                                    <th style={{ fontSize: 30, padding: '15px 10px', fontWeight: 500, borderBottom: '2px solid #cdcdcd' }}>강사명</th>
                                    <th style={{ fontSize: 30, padding: '15px 10px', fontWeight: 500, borderBottom: '2px solid #cdcdcd' }}>비고</th>
                                </tr>
                            </thead>
                            <tbody>
                                {remainList.map((c, idx) => {
                                    const active = isActive(nowHHmm, c);
                                    return (
                                        <tr key={idx} style={{ color: active ? '#262626' : '#b9b9b9', fontWeight: active ? 500 : 400 }}>
                                            <td style={{ fontSize: 26, padding: '14px 10px', borderBottom: '1px solid #e0e0e0' }}>
                                                {timeSplit(c.mhsClassstarttime)}~{timeSplit(c.mhsClassendtime)}
                                            </td>
                                            <td style={{ fontSize: 26, padding: '14px 10px', borderBottom: '1px solid #e0e0e0' }}>
                                                {active
                                                    ? <span style={{ display: 'inline-block', borderRadius: 60, background: '#c20b0b', color: '#fff', padding: '4px 0', width: 350, fontWeight: 400 }}>{c.mhsClassnm}</span>
                                                    : c.mhsClassnm}
                                            </td>
                                            <td style={{ fontSize: 26, padding: '14px 10px', borderBottom: '1px solid #e0e0e0' }}>{c.mhsTeachernm}</td>
                                            <td style={{ fontSize: 26, padding: '14px 10px', borderBottom: '1px solid #e0e0e0' }}>{c.mhsClassintro || '-'}</td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </>
                )}
            </div>

            {/* 푸터 — 레거시 .footer */}
            <div style={{ background: '#2e2e2e', padding: '25px 0', textAlign: 'center', flexShrink: 0 }}>
                <img src="/resource/img/cc_logo.png" alt="Culture Club" style={{ height: 100 }} />
            </div>
        </div>
    );
}
