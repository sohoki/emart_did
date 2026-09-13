import { Fragment, useCallback, useEffect, useMemo, useState, Suspense, lazy } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const MhsMonitorPreviewModal = lazy(() => import('./components/MhsMonitorPreviewModal.jsx'));

const labelCellStyle = { width: '16.6%', textAlign: 'center', color: '#64748b', padding: '10px 8px', background: '#f8fafc', border: '1px solid #e2e8f0' };
const valueCellStyle = { width: '16.6%', textAlign: 'center', padding: '10px 8px', border: '1px solid #e2e8f0' };

const infoRow = (pairs) => (
    <tr>
        {pairs.map(([label, value], idx) => (
            <Fragment key={idx}>
                <th style={labelCellStyle}>{label}</th>
                <td style={valueCellStyle}>{value}</td>
            </Fragment>
        ))}
    </tr>
);

const notReady = (label) => Swal.fire({ icon: 'info', title: label, text: '준비 중인 기능입니다.' });

// 편성표에 표시할 시간대(레거시 monitorDetail.jsp와 동일하게 10시~22시)
const HOURS = Array.from({ length: 13 }, (_, i) => i + 10);

const todayYmd = () => {
    const d = new Date();
    return `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}`;
};
const ymdToDateInput = (ymd) => (ymd?.length === 8 ? `${ymd.slice(0, 4)}-${ymd.slice(4, 6)}-${ymd.slice(6, 8)}` : '');
const dateInputToYmd = (v) => v.replaceAll('-', '');

// 모니터(단말) 상세 화면 — 레거시 roomManage/monitorDetail.jsp 참고. "모니터 정보" 박스 +
// "모니터 송출 상세정보"(시간대별 편성표) 박스로 구성. 값 수정은 목록 화면의 모니터 등록/수정
// 모달(MhsMonitorFormModal)을 그대로 재사용한다(목록으로 이동 후 ?editMonitorcd=로 자동 오픈).
export default function MhsMonitorDetailPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const mhsMonitorcd = searchParams.get('mhsMonitorcd') ?? '';

    const [detail, setDetail] = useState(null);
    const [connList, setConnList] = useState([]);
    const [searchDay, setSearchDay] = useState(todayYmd());
    const [dayInput, setDayInput] = useState(todayYmd());
    const [loading, setLoading] = useState(false);
    const [previewOpen, setPreviewOpen] = useState(false);

    const loadDetail = useCallback(async () => {
        if (!mhsMonitorcd) return;
        const res = await fnAjaxFetch({ url: `${URL.MHS_MONITOR_INFO}/${mhsMonitorcd}.do`, method: 'GET' });
        setDetail(res?.data?.result?.result ?? null);
    }, [mhsMonitorcd]);

    const loadConnList = useCallback(async (day) => {
        if (!mhsMonitorcd) return;
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.MHS_VIEWCONN_LIST, method: 'POST',
                data: { mhsMonitorcd, searchDay: day }, showLoading: false,
            });
            setConnList(res?.data?.result?.resultList ?? []);
        } finally {
            setLoading(false);
        }
    }, [mhsMonitorcd]);

    useEffect(() => { loadDetail(); }, [loadDetail]);
    useEffect(() => { loadConnList(searchDay); }, [loadConnList, searchDay]);

    const onSearchDay = useCallback(() => {
        setSearchDay(dayInput);
    }, [dayInput]);

    const handleDeleteConn = useCallback(async (mhsConnSeq) => {
        const result = await Swal.fire({
            icon: 'question', title: '편성 삭제', text: '삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        await fnAjaxFetch({ url: `${URL.MHS_VIEWCONN_INFO}/${mhsConnSeq}.do`, method: 'DELETE' });
        loadConnList(searchDay);
    }, [searchDay, loadConnList]);

    // 강의별 시작~종료 시각(HHmm)을 시간대 배열의 채워진 셀 인덱스 목록으로 변환
    const rows = useMemo(() => connList.map((c) => {
        const startHour = parseInt(String(c.mhsClassstarttime ?? '').slice(0, 2), 10);
        const endHour = parseInt(String(c.mhsClassendtime ?? '').slice(0, 2), 10);
        return { ...c, filled: HOURS.map((h) => h >= startHour && endHour >= h) };
    }), [connList]);

    if (!mhsMonitorcd) {
        return <div style={{ padding: 16 }}>mhsMonitorcd가 없습니다. 모니터 관리 목록에서 단말명 링크로 진입해 주세요.</div>;
    }

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">모니터 상세</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">룸 관리</li>
                        <li className="breadcrumb-item">모니터 관리</li>
                        <li className="breadcrumb-item">상세</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search__action" style={{ padding: '0 0 12px', display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                <button type="button" className="btn btn-outline-dark btn-outline__gray" disabled={!detail}
                    onClick={() => setPreviewOpen(true)}>미리보기</button>
                {/* 레거시 monitorDetail.jsp에도 클릭 핸들러가 연결돼 있지 않은 미구현 버튼(연동할 재부팅
                    커맨드/화면이 없음) — DidDetailPage의 단말 원격지원 버튼과 동일하게 준비 중 안내만 유지 */}
                <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                    onClick={() => notReady('모니터 재부팅')}>모니터 재부팅</button>
                <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                    onClick={() => navigate(`/backoffice/sub/roomManage/mhs?editMonitorcd=${mhsMonitorcd}`)}>정보 수정</button>
                <button type="button" className="btn btn-outline-dark btn-outline__gray"
                    onClick={() => navigate('/backoffice/sub/roomManage/mhs')}>목록으로</button>
            </div>

            {!detail ? (
                <div className="col-12" style={{ padding: 16, color: '#94a3b8' }}>{loading ? '조회 중...' : '데이터가 없습니다.'}</div>
            ) : (
                <>
                    <div className="col-12" style={{ marginBottom: 20 }}>
                        <div style={{ textAlign: 'center', fontWeight: 700, padding: 10, background: '#eef1f5', border: '1px solid #e2e8f0', borderBottom: 'none' }}>모니터 정보</div>
                        <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse' }}>
                            <tbody>
                                {infoRow([['조직명', detail.mhsBrandnm], ['점포명', detail.mhsCenternm]])}
                                {infoRow([['단말정보', <>{detail.mhsMonitornm}<br />({detail.mhsMonitorcd})</>], ['네트워크정보', <>{detail.mhsIpaddr}<br />{detail.mhsMacaddr}</>]])}
                                {infoRow([['송출방식', detail.mhsMonitorstatus], ['최종통신일자', detail.mhsLastconn]])}
                                <tr>
                                    <th style={labelCellStyle}>비고</th>
                                    <td style={{ ...valueCellStyle, width: '83.4%' }} colSpan={3}>{detail.mhsRemark}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div className="col-12">
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                            <h2 style={{ fontSize: 15, margin: 0 }}>모니터 송출 상세정보</h2>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <span style={{ fontSize: 12, color: '#94a3b8' }}>※ 추가 된 강의를 삭제하려면 삭제 버튼을 클릭하세요.</span>
                                <span>조회일자</span>
                                <input type="date" value={ymdToDateInput(dayInput)} onChange={(e) => setDayInput(dateInputToYmd(e.target.value))} />
                                <button type="button" className="btn btn-outline-dark btn-outline__gray btn-sm" onClick={onSearchDay}>조회</button>
                            </div>
                        </div>

                        <table style={{ width: '100%', fontSize: 12, borderCollapse: 'collapse' }}>
                            <thead>
                                <tr style={{ background: '#1e293b', color: '#fff' }}>
                                    <th style={{ border: '1px solid #334155', padding: 6, width: 120 }}>구분</th>
                                    {HOURS.map((h) => (
                                        <th key={h} style={{ border: '1px solid #334155', padding: 6 }}>{String(h).padStart(2, '0')}:00</th>
                                    ))}
                                    <th style={{ border: '1px solid #334155', padding: 6, width: 60 }}>삭제</th>
                                </tr>
                            </thead>
                            <tbody>
                                {rows.map((r) => (
                                    <tr key={r.mhsConnSeq}>
                                        <th style={{ border: '1px solid #e2e8f0', padding: 6, background: '#f8fafc' }}>{r.mhsClassnm}</th>
                                        {r.filled.map((on, idx) => (
                                            <td key={idx} style={{ border: '1px solid #e2e8f0', height: 28, background: on ? '#4285F5' : undefined }} />
                                        ))}
                                        <td style={{ border: '1px solid #e2e8f0', textAlign: 'center' }}>
                                            <button type="button" className="btn btn-outline-danger btn-outline__gray btn-sm"
                                                onClick={() => handleDeleteConn(r.mhsConnSeq)}>삭제</button>
                                        </td>
                                    </tr>
                                ))}
                                {!loading && rows.length === 0 && (
                                    <tr><td colSpan={HOURS.length + 2} style={{ padding: 16, textAlign: 'center', color: '#94a3b8', border: '1px solid #e2e8f0' }}>해당 일자에 편성된 강의가 없습니다.</td></tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </>
            )}

            <Suspense fallback={null}>
                {previewOpen && (
                    <MhsMonitorPreviewModal open={previewOpen} mhsMonitorcd={mhsMonitorcd} onClose={() => setPreviewOpen(false)} />
                )}
            </Suspense>
        </div>
    );
}
