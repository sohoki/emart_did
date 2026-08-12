import { useEffect, useState } from 'react';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import URL from '@/constants/URL.jsx';
import '@/style/Modal.css';

const WEEK_LABEL = ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일'];

// hhmm(4자리) -> "hh:mm"
const timeSplit = (v) => (v && v.length >= 4 ? `${v.slice(0, 2)}:${v.slice(2, 4)}` : (v || ''));

// 지금 시각(HHmm)이 강의 시작~종료 구간 안에 있는지
const isActive = (nowHHmm, cls) => (
    Number(nowHHmm) >= Number(cls.mhsClassstarttime) && Number(cls.mhsClassendtime) > Number(nowHHmm)
);

// MHS 모니터 미리보기 모달 — 레거시 preView.jsp(회의실알림이 DID 실제 송출 화면) 디자인 참고.
// 실제 장비는 스와이프 슬라이더로 여러 화면을 순환하지만, 관리자 미리보기 모달에서는
// 슬라이드 내용을 한 화면에 세로로 이어붙여 보여준다(자동 순환/1분 재조회/1시간 새로고침 등
// 장비 전용 동작은 재현하지 않음).
const MhsMonitorPreviewModal = ({ open, mhsMonitorcd, onClose }) => {
    const [now, setNow] = useState(new Date());
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!open) return undefined;
        const timer = setInterval(() => setNow(new Date()), 1000);
        return () => clearInterval(timer);
    }, [open]);

    useEffect(() => {
        if (!open || !mhsMonitorcd) return;
        setLoading(true);
        (async () => {
            const res = await fnAjaxFetch({
                url: URL.MHS_VIEWCONN_PREVIEW, method: 'GET', param: { mhsMonitorcd }, showLoading: false,
            });
            setData(res?.data?.result ?? null);
            setLoading(false);
        })();
    }, [open, mhsMonitorcd]);

    if (!open) return null;

    const nowHHmm = `${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`;
    const todayList = data?.resultList ?? [];
    const pageInfo = data?.pageInfo ?? [];
    const current = pageInfo.find((p) => p.mhsNowgubun === 'NOW');
    const nextList = pageInfo.filter((p) => p.mhsNowgubun === 'NEXT');

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable"
                    style={{ width: 640, maxWidth: '95%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">미리보기</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active" style={{ padding: 0 }}>

                                {/* 헤더 — 레거시 .header (룸명/센터명 + 날짜/요일/시각) */}
                                <div style={{
                                    background: '#1e293b', color: '#fff', padding: '16px 20px',
                                    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                                }}>
                                    <div>
                                        <div style={{ fontSize: 20, fontWeight: 700 }}>{data?.monitorInfo?.mhsMonitornm ?? '-'}</div>
                                        <div style={{ fontSize: 13, color: '#cbd5e1' }}>{data?.monitorInfo?.mhsCenternm ?? '-'}</div>
                                    </div>
                                    <div style={{ textAlign: 'right' }}>
                                        <div style={{ fontSize: 13, color: '#cbd5e1' }}>
                                            {now.getMonth() + 1}월 {now.getDate()}일 {WEEK_LABEL[now.getDay()]}
                                        </div>
                                        <div style={{ fontSize: 22, fontWeight: 700 }}>{timeSplit(nowHHmm)}</div>
                                    </div>
                                </div>

                                {/* 현재 강의 카드 — 레거시 slide02/04 */}
                                <div style={{ padding: '16px 20px', borderBottom: '1px solid #e2e8f0' }}>
                                    <div style={{ fontSize: 12, color: '#94a3b8' }}>강의 제목</div>
                                    {current ? (
                                        <>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                                                <h3 style={{ margin: '4px 0', fontSize: 18 }}>{current.mhsClassnm}</h3>
                                                <span style={{ fontSize: 13, color: '#2563eb', fontWeight: 600 }}>
                                                    {timeSplit(current.mhsClassstarttime)}~{timeSplit(current.mhsClassendtime)}
                                                </span>
                                            </div>
                                            <div style={{ fontSize: 13, color: '#64748b' }}>{current.mhsClassintro || ''}</div>
                                            <div style={{ fontSize: 13, fontWeight: 600, marginTop: 2 }}>{current.mhsTeachernm}</div>
                                        </>
                                    ) : (
                                        <h3 style={{ margin: '4px 0', fontSize: 18, color: '#94a3b8' }}>현재 진행중인 강의가 없습니다</h3>
                                    )}
                                    <div style={{
                                        marginTop: 10, paddingTop: 10, borderTop: '1px dashed #e2e8f0',
                                        display: 'flex', justifyContent: 'space-between', fontSize: 13,
                                    }}>
                                        <span style={{ color: '#94a3b8' }}>다음 강의</span>
                                        <span>
                                            {nextList.length > 0
                                                ? `${timeSplit(nextList[0].mhsClassstarttime)}~${timeSplit(nextList[0].mhsClassendtime)} ${nextList[0].mhsClassnm} · ${nextList[0].mhsTeachernm}`
                                                : '예정된 강의가 없습니다.'}
                                        </span>
                                    </div>
                                </div>

                                {/* 오늘 강의 목록 — 레거시 slide01/03 */}
                                <div style={{ padding: '16px 20px' }}>
                                    <h4 style={{ margin: '0 0 8px', fontSize: 14 }}>문화센터 강의 현황</h4>
                                    <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
                                        <thead>
                                            <tr style={{ background: '#f8fafc' }}>
                                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>시간</th>
                                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>강의명</th>
                                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>강사명</th>
                                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>비고</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {todayList.map((c, idx) => {
                                                const active = isActive(nowHHmm, c);
                                                return (
                                                    <tr key={idx} style={active ? { background: '#fff7ed' } : undefined}>
                                                        <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>
                                                            {timeSplit(c.mhsClassstarttime)}~{timeSplit(c.mhsClassendtime)}
                                                        </td>
                                                        <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>
                                                            {active
                                                                ? <span style={{ color: '#ea580c', fontWeight: 700 }}>{c.mhsClassnm}</span>
                                                                : c.mhsClassnm}
                                                        </td>
                                                        <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{c.mhsTeachernm}</td>
                                                        <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{c.mhsClassintro || '-'}</td>
                                                    </tr>
                                                );
                                            })}
                                            {!loading && todayList.length === 0 && (
                                                <tr><td colSpan={4} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>오늘 예정된 강의가 없습니다.</td></tr>
                                            )}
                                        </tbody>
                                    </table>
                                </div>

                                {/* 푸터 — 레거시 .footer 로고 */}
                                <div style={{ padding: '12px 20px', borderTop: '1px solid #e2e8f0', textAlign: 'center' }}>
                                    <img src="/resource/img/emartLogo.png" alt="이마트로고" style={{ height: 28 }} />
                                </div>

                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-action__lightblue" onClick={onClose}>닫기</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default MhsMonitorPreviewModal;
