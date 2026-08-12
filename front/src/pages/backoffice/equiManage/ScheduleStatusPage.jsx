import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import URL from '@/constants/URL.jsx';

const dashify = (v) => (v && v.length === 8 ? `${v.slice(0, 4)}-${v.slice(4, 6)}-${v.slice(6, 8)}` : (v || ''));

// 발송 스케줄 현황(읽기전용 상세) — 레거시 schDetail.jsp/schView.jsp 참고.
// ScheduleListPage 그리드의 콘텐츠명 링크에서 진입한다.
export default function ScheduleStatusPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const schCode = searchParams.get('schCode') ?? '';

    const [info, setInfo] = useState(null);

    const loadDetail = useCallback(async () => {
        if (!schCode) return;
        const res = await fnAjaxFetch({ url: `${URL.SCH_INFO}/${schCode}/view.do`, method: 'GET' });
        setInfo(res?.data?.result?.result || null);
    }, [schCode]);

    useEffect(() => { loadDetail(); }, [loadDetail]);

    if (!schCode) {
        return <div style={{ padding: 16 }}>schCode가 없습니다. 발송 스케줄 관리 목록에서 콘텐츠명 링크로 진입해 주세요.</div>;
    }

    const thStyle = { width: 110, textAlign: 'left', color: '#64748b', padding: '8px' };
    const tdStyle = { padding: '8px' };

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">발송 스케줄 현황</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">장비 관리</li>
                        <li className="breadcrumb-item">발송 스케줄 관리</li>
                        <li className="breadcrumb-item">스케줄 현황</li>
                    </ol>
                </div>
            </div>

            <div className="col-12" style={{ padding: '12px 15px', borderBottom: '1px solid #dde2eb' }}>
                {!info ? (
                    <div style={{ padding: 16, color: '#94a3b8' }}>데이터가 없습니다.</div>
                ) : (
                    <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse' }}>
                        <tbody>
                            <tr>
                                <th style={thStyle}>스케줄 ID</th>
                                <td style={tdStyle}>{info.schCode}</td>
                                <th style={thStyle}>스케줄명</th>
                                <td style={tdStyle}>{info.schName}</td>
                            </tr>
                            <tr>
                                <th style={thStyle}>송출 기간</th>
                                <td style={tdStyle} colSpan={3}>{dashify(info.schStartDay)} ~ {dashify(info.schEndDay)}</td>
                            </tr>
                            <tr>
                                <th style={thStyle}>적용 그룹</th>
                                <td style={tdStyle}>{info.groupNm}</td>
                                <th style={thStyle}>연동 콘텐츠</th>
                                <td style={tdStyle}>{info.conNm}</td>
                            </tr>
                            <tr>
                                <th style={thStyle}>긴급 송출</th>
                                <td style={tdStyle}>{info.schEmerGubun === 'Y' ? '적용' : '일반'}</td>
                                <th style={thStyle}>사용유무</th>
                                <td style={tdStyle}>{info.schUseYn === 'N' ? '사용안함' : '사용'}</td>
                            </tr>
                        </tbody>
                    </table>
                )}
            </div>

            <div className="col-12 content-search__action" style={{ padding: '12px 15px', textAlign: 'right' }}>
                <button type="button" className="btn btn-outline-dark btn-outline__gray"
                    onClick={() => navigate('/backoffice/sub/equiManage/sch')}>목록</button>
            </div>
        </div>
    );
}
