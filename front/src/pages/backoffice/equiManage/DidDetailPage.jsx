import { Fragment, useCallback, useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { useCommonDelete } from '@/hooks/use-common-delete.js';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

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

// 단말기 상세 화면 — 레거시 equiManage/didView.jsp 참고. "단말기 정보" 박스(3열 그리드) +
// "연결 스케줄 정보" 박스로 구성. 값 수정은 목록 화면의 단말기 등록/수정 모달(DidFormModal)을
// 그대로 재사용한다(목록으로 이동 후 ?editDidId=로 자동 오픈).
export default function DidDetailPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const didId = searchParams.get('didId') ?? '';

    const [detail, setDetail] = useState(null);
    const [schList, setSchList] = useState([]);
    const [loading, setLoading] = useState(false);

    const loadDetail = useCallback(async () => {
        if (!didId) return;
        setLoading(true);
        try {
            const [viewRes, schRes] = await Promise.all([
                fnAjaxFetch({ url: `${URL.DID_INFO}/${didId}/view.do`, method: 'GET' }),
                fnAjaxFetch({ url: `${URL.DID_INFO}/${didId}/schList.do`, method: 'GET' }),
            ]);
            setDetail(viewRes?.data?.result?.result ?? null);
            setSchList(schRes?.data?.result?.result ?? []);
        } finally {
            setLoading(false);
        }
    }, [didId]);

    useEffect(() => { loadDetail(); }, [loadDetail]);

    const { handleDelete } = useCommonDelete({
        URL: URL.DID_INFO,
        MESSAGE: '단말기 정보',
        callback: () => navigate('/backoffice/sub/equiManage/did'),
    });

    // 레거시 send_DID(code) — 재부팅/캡처 등 단말 명령 발송. 결과 자체는 실제 단말이 처리해서
    // 콜백 응답으로 오지 않고 큐(TB_SENDMSGINFO)에 쌓이므로, 여기서는 "요청 접수" 여부만 알려준다.
    const sendCommand = useCallback(async (url, label) => {
        if (!detail) return;
        const ok = await Swal.fire({
            icon: 'question', title: label, html: `<b>${detail.didNm}</b>에 ${label}을(를) 요청하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        // reboot.do/capture.do는 @RequestParam(폼 파라미터)으로 받으므로 JSON 바디가 아니라
        // 쿼리스트링으로 전송한다(DidInfoList의 restart.do 호출과 동일한 방식).
        const query = new URLSearchParams({ didId: detail.didId, didMac: detail.didMac || '' }).toString();
        const res = await fnAjaxFetch({ url: `${url}?${query}`, method: 'POST' });
        if (res?.data?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: `${label} 요청을 보냈습니다.` });
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || `${label} 요청 중 오류가 발생했습니다.` });
        }
    }, [detail]);

    // 레거시 view_DidTime() — 장비통신 이력조회 팝업(pop_sendLst.do) 대신, did_emart에 이미
    // 있는 DID 전문통신현황 화면(SendMsgListPage)을 같은 크기의 팝업창으로 띄운다.
    const handleOpenCommHistory = useCallback(() => {
        window.open('/backoffice/sub/operManage/snd', '전문현황', 'width=900,height=600,top=100,left=400,scrollbars=yes');
    }, []);

    if (!didId) {
        return <div style={{ padding: 16 }}>didId가 없습니다. 단말기 관리 목록에서 콘텐츠 보기 링크로 진입해 주세요.</div>;
    }

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">단말기 상세</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">장비 관리</li>
                        <li className="breadcrumb-item">단말기 관리</li>
                        <li className="breadcrumb-item">상세</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search__action" style={{ padding: '0 0 12px', display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                <button type="button" className="btn btn-outline-dark btn-outline__gray"
                    onClick={() => navigate('/backoffice/sub/equiManage/did')}>목록</button>
                <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                    onClick={() => navigate(`/backoffice/sub/equiManage/did?editDidId=${didId}`)}>수정</button>
                <button type="button" className="btn btn-outline-danger btn-outline__gray" disabled={!detail}
                    onClick={() => detail && handleDelete({ code: detail.didId, name: detail.didNm })}>삭제</button>
            </div>

            {!detail ? (
                <div className="col-12" style={{ padding: 16, color: '#94a3b8' }}>{loading ? '조회 중...' : '데이터가 없습니다.'}</div>
            ) : (
                <>
                    <div className="col-12" style={{ marginBottom: 20 }}>
                        <div style={{ textAlign: 'center', fontWeight: 700, padding: 10, background: '#eef1f5', border: '1px solid #e2e8f0', borderBottom: 'none' }}>단말기 정보</div>
                        <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse' }}>
                            <tbody>
                                {infoRow([['단말명', detail.didNm], ['단말 ID', detail.didId], ['단말 MAC', detail.didMac]])}
                                {infoRow([['단말 IP', detail.didIpaddr], ['IP타입', detail.didIptype], ['단말 OS', detail.didOs]])}
                                {infoRow([['관리부서', detail.roleNm], ['설치지점', detail.centerNM], ['그룹정보', detail.groupNm]])}
                                {infoRow([['단말타입', detail.didModelType], ['단말형태', detail.didType], ['운영시간', `${detail.didStartTime || ''} ~ ${detail.didEndTime || ''}`]])}
                                {infoRow([['단말해상도', `가로:${detail.didWidth || 0} 세로:${detail.didHeight || 0}`], ['마지막 접속일자', detail.didEndContime], ['사용유무', detail.didUseYn === 'Y' ? '사용' : '사용 안함']])}
                                <tr>
                                    <th style={labelCellStyle}>재부팅 처리</th>
                                    <td style={valueCellStyle}>
                                        <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                                            onClick={() => sendCommand(URL.DID_REBOOT, '장비 재부팅')}>장비 재부팅</button>
                                    </td>
                                    <th style={labelCellStyle}>단말화면확인</th>
                                    <td style={valueCellStyle}>
                                        {detail.didOs === '윈도우' ? (
                                            // 레거시 didView.jsp에도 실제 구현은 없었음 — 화면엔 send_break()
                                            // (서비스 점검 중 알림)만 연결돼 있었고, 진짜 원격지원 커맨드(ReM)는
                                            // 주석 처리된 채 "협의중입니다" 알림으로만 남아있던 미구현 기능.
                                            // 연결할 기존 화면 자체가 없어 준비 중 안내를 그대로 유지함.
                                            <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                                                onClick={() => notReady('단말 원격지원')}>단말 원격지원</button>
                                        ) : (
                                            <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                                                onClick={() => sendCommand(URL.DID_CAPTURE, '단말 화면캡처')}>단말 화면캡처</button>
                                        )}
                                    </td>
                                    <th style={labelCellStyle}>단말 통신정보</th>
                                    <td style={valueCellStyle}>
                                        <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                                            onClick={handleOpenCommHistory}>장비통신 이력조회</button>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div className="col-12">
                        <div style={{ textAlign: 'center', fontWeight: 700, padding: 10, background: '#eef1f5', border: '1px solid #e2e8f0', borderBottom: 'none' }}>연결 스케줄 정보</div>
                        <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse', marginBottom: 12 }}>
                            <tbody>
                                <tr>
                                    <th style={labelCellStyle}>현재 송출 스케줄(콘텐츠)</th>
                                    <td style={valueCellStyle}>
                                        {schList.length > 0 ? `${schList[0].schNm} (${schList[0].conNm})` : (
                                            <>
                                                <button type="button" className="btn btn-outline-dark btn-outline__gray btn-sm" style={{ marginRight: 6 }}
                                                    onClick={() => navigate(`/backoffice/sub/equiManage/sch?openInsert=1&groupCode=${encodeURIComponent(detail.groupCode || '')}`)}
                                                >스케줄 등록하기</button>
                                                <button type="button" className="btn btn-outline-dark btn-outline__gray btn-sm"
                                                    onClick={() => navigate('/backoffice/sub/conManage/muti?openInsert=1')}
                                                >콘텐츠 등록하기</button>
                                            </>
                                        )}
                                    </td>
                                    <th style={labelCellStyle}>송출 예정 스케줄</th>
                                    <td style={valueCellStyle}>{detail.schCnt ?? schList.length} 개</td>
                                </tr>
                            </tbody>
                        </table>

                        <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse' }}>
                            <thead>
                                <tr style={{ background: '#f8fafc' }}>
                                    <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 60 }}>순번</th>
                                    <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 200 }}>송출기간</th>
                                    <th style={{ border: '1px solid #e2e8f0', padding: 8 }}>스케줄</th>
                                    <th style={{ border: '1px solid #e2e8f0', padding: 8 }}>콘텐츠</th>
                                </tr>
                            </thead>
                            <tbody>
                                {schList.map((s, idx) => (
                                    <tr key={`${s.schCode}-${s.conSeq}-${idx}`}>
                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>{idx + 1}</td>
                                        <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>{s.schStartday} ~ {s.schEndday}</td>
                                        <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>{s.schNm}</td>
                                        <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>{s.conNm}</td>
                                    </tr>
                                ))}
                                {schList.length === 0 && (
                                    <tr><td colSpan={4} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>연결된 스케줄(콘텐츠)이 없습니다.</td></tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </>
            )}
        </div>
    );
}
