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

// MHS 모니터 미리보기 모달 — 실제 단말(안드로이드)이 그대로 띄우는 화면(레거시
// emart_cms3.2.1의 roomManage/preView.jsp, did.emart.com/backoffice/sub/roomManage/preView.do)의
// 색상/타이포/배지를 그대로 축소 재현한다. 실제 단말은 인증 없이 이 legacy JSP URL을 직접 폴링하는
// 방식이라(같은 DB를 보므로 등록/편성 데이터 자체는 이미 반영됨) 이 모달이 단말 화면을 대체하지는
// 않지만, 관리자가 실제로 어떻게 보이는지 정확히 미리 확인할 수 있도록 디자인을 맞춘다.
// (실제 장비는 슬라이드 01~04를 스와이프로 순환하지만, 여기서는 슬라이드01의 시간표 +
// 슬라이드02의 현재/다음 강의 카드를 한 화면에 이어붙여 보여줌 — 자동 순환/1분 재조회/1시간
// 새로고침 등 장비 전용 동작은 재현하지 않음)
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

                                {/* 헤더 — 레거시 preView.jsp .header(흰 배경 + 하단 경계선) 그대로 재현 */}
                                <div style={{
                                    padding: '16px 20px', borderBottom: '2px solid #d8d9db',
                                    display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end',
                                }}>
                                    <div style={{ display: 'flex', alignItems: 'baseline', gap: 14 }}>
                                        <span style={{ fontSize: 22, fontWeight: 700, lineHeight: 1 }}>{data?.monitorInfo?.mhsMonitornm ?? '-'}</span>
                                        <span style={{ fontSize: 14, color: '#bfbfbf' }}>{data?.monitorInfo?.mhsCenternm ?? '-'}</span>
                                    </div>
                                    <div style={{ display: 'flex', alignItems: 'baseline', gap: 10 }}>
                                        <div style={{ fontSize: 11, color: '#333', textAlign: 'right', lineHeight: 1.3 }}>
                                            <div>{now.getMonth() + 1}월 {now.getDate()}일</div>
                                            <div>{WEEK_LABEL[now.getDay()]}</div>
                                        </div>
                                        <div style={{ fontSize: 28, fontWeight: 300 }}>{now.getHours()}시 {now.getMinutes()}분</div>
                                    </div>
                                </div>

                                {/* 현재/다음 강의 카드 — 레거시 slide02/04 (onMeeting 배지 + meeting_txt) */}
                                <div style={{ padding: '20px 20px 16px' }}>
                                    <div style={{ fontSize: 12, color: '#919191' }}>강의 제목</div>
                                    {current ? (
                                        <>
                                            <div style={{
                                                display: 'inline-block', margin: '6px 0 10px', padding: '6px 16px',
                                                borderRadius: 999, background: '#c70e0e', color: '#fff',
                                                fontSize: 13, fontWeight: 600,
                                            }}>
                                                {timeSplit(current.mhsClassstarttime)}~{timeSplit(current.mhsClassendtime)}
                                            </div>
                                            <h3 style={{ margin: '2px 0 8px', fontSize: 20, fontWeight: 600, color: '#3d3d3d' }}>{current.mhsClassnm}</h3>
                                            <div style={{ fontSize: 12, color: '#3d3d3d' }}>{current.mhsClassintro || ''}</div>
                                            <div style={{ fontSize: 14, fontWeight: 600, color: '#3d3d3d', marginTop: 2 }}>{current.mhsTeachernm}</div>
                                        </>
                                    ) : (
                                        <>
                                            <div style={{
                                                display: 'inline-block', margin: '6px 0 10px', padding: '6px 16px',
                                                borderRadius: 999, background: '#a0a0a0', color: '#fff',
                                                fontSize: 13, fontWeight: 600,
                                            }}>
                                                {String(now.getHours()).padStart(2, '0')}:{String(now.getMinutes()).padStart(2, '0')}~
                                            </div>
                                            <h3 style={{ margin: '2px 0 8px', fontSize: 20, fontWeight: 600, color: '#3d3d3d' }}>현재 진행중인 강의가 없습니다</h3>
                                        </>
                                    )}
                                    <div style={{
                                        marginTop: 10, background: '#2e2e2e', color: '#d6d6d6', borderRadius: 8,
                                        padding: '6px 12px', display: 'inline-flex', fontSize: 11,
                                    }}>
                                        <span style={{ borderRight: '2px solid #808080', paddingRight: 8, marginRight: 8 }}>다음 강의</span>
                                        <span>
                                            {nextList.length > 0
                                                ? `${timeSplit(nextList[0].mhsClassstarttime)}~${timeSplit(nextList[0].mhsClassendtime)} ${nextList[0].mhsTeachernm}`
                                                : '예정된 강의가 없습니다.'}
                                        </span>
                                    </div>
                                </div>

                                {/* 오늘 강의 목록 — 레거시 slide01/03 (.meeting_list) */}
                                <div style={{ padding: '0 20px 16px' }}>
                                    <h4 style={{ margin: '0 0 10px', fontSize: 15, fontWeight: 600, color: '#5f5f5f' }}>문화센터 강의 현황</h4>
                                    <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 11, textAlign: 'center', borderTop: '3px solid #5f5f5f', borderBottom: '2px solid #cdcdcd' }}>
                                        <thead>
                                            <tr>
                                                <th style={{ padding: '8px 6px', fontWeight: 500, borderBottom: '1px solid #cdcdcd' }}>시간</th>
                                                <th style={{ padding: '8px 6px', fontWeight: 500, borderBottom: '1px solid #cdcdcd' }}>강의명</th>
                                                <th style={{ padding: '8px 6px', fontWeight: 500, borderBottom: '1px solid #cdcdcd' }}>강사명</th>
                                                <th style={{ padding: '8px 6px', fontWeight: 500, borderBottom: '1px solid #cdcdcd' }}>비고</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {todayList.map((c, idx) => {
                                                const active = isActive(nowHHmm, c);
                                                return (
                                                    <tr key={idx}>
                                                        <td style={{ padding: '8px 6px', borderBottom: '1px solid #e0e0e0', color: active ? '#262626' : '#b9b9b9', fontWeight: active ? 500 : 400 }}>
                                                            {timeSplit(c.mhsClassstarttime)}~{timeSplit(c.mhsClassendtime)}
                                                        </td>
                                                        <td style={{ padding: '8px 6px', borderBottom: '1px solid #e0e0e0' }}>
                                                            {active
                                                                ? <span style={{ display: 'inline-block', borderRadius: 999, background: '#c20b0b', color: '#fff', padding: '2px 14px' }}>{c.mhsClassnm}</span>
                                                                : <span style={{ color: '#b9b9b9' }}>{c.mhsClassnm}</span>}
                                                        </td>
                                                        <td style={{ padding: '8px 6px', borderBottom: '1px solid #e0e0e0', color: active ? '#262626' : '#b9b9b9', fontWeight: active ? 500 : 400 }}>{c.mhsTeachernm}</td>
                                                        <td style={{ padding: '8px 6px', borderBottom: '1px solid #e0e0e0', color: active ? '#262626' : '#b9b9b9', fontWeight: active ? 500 : 400 }}>{c.mhsClassintro || '-'}</td>
                                                    </tr>
                                                );
                                            })}
                                            {!loading && todayList.length === 0 && (
                                                <tr><td colSpan={4} style={{ padding: 16, textAlign: 'center', color: '#b9b9b9' }}>예정된 강의가 없습니다</td></tr>
                                            )}
                                        </tbody>
                                    </table>
                                </div>

                                {/* 푸터 — 레거시 .footer(짙은 배경 + cc_logo.png) 그대로 재현 */}
                                <div style={{ padding: '12px 20px', background: '#2e2e2e', textAlign: 'center' }}>
                                    <img src="/resource/img/cc_logo.png" alt="Culture Club" style={{ height: 22 }} />
                                </div>

                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                {/* 실제 단말(안드로이드 키오스크)이 띄우는 화면 원본 — 레거시 fn_preview()의
                                    1920x1080 팝업과 동일 취지. 단말 URL을 확인/복사하거나 실제 렌더링을
                                    그대로 확인할 때 사용 */}
                                <button type="button" className="btn btn-outline-dark btn-outline__gray"
                                    onClick={() => window.open(`/backoffice/sub/roomManage/mhs/device?mhsMonitorcd=${mhsMonitorcd}`, '', 'width=1920,height=1080,left=0')}
                                >단말 화면 원본 열기</button>
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
