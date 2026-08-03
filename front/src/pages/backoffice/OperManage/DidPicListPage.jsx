import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import Swal from '@/lib/swal.js';
import config from '@/config/index.jsx';
import URL from '@/constants/URL.jsx';

const PAGE_UNIT = 24;

export default function DidPicListPage() {
    const navigate = useNavigate();
    const [list, setList] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [strDate, setStrDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [loading, setLoading] = useState(false);

    const loadList = useCallback(async (params) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.DID_PIC_LIST,
                method: 'POST',
                data: {
                    strDate: params?.strDate || null,
                    endDate: params?.endDate || null,
                    pageIndex: 1,
                    pageUnit: PAGE_UNIT,
                },
                showLoading: false,
            });
            const resultList = res?.data?.result?.resultList ?? [];
            const cnt = res?.data?.result?.totalCnt ?? resultList.length;
            setList(resultList);
            setTotalCnt(cnt);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        if (!getCookie('accessToken')) {
            navigate('/login', { replace: true });
            return;
        }
        loadList({});
    }, [loadList, navigate]);

    const handleSearch = (e) => {
        e.preventDefault();
        loadList({ strDate, endDate });
    };

    const handleUpload = async (e) => {
        e.preventDefault();
        const form = e.target;
        const didId = form.didId.value.trim();
        const didMac = form.didMac.value.trim();
        const file = form.didFile.files?.[0];

        if (!didId || !file) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: 'DID ID와 파일을 선택해 주세요.' });
            return;
        }

        const formData = new FormData();
        formData.append('didId', didId);
        formData.append('didMac', didMac);
        formData.append('didFile', file);

        await fnAjaxFetch({ url: URL.DID_PIC_UPLOAD, method: 'POST', data: formData });
        form.reset();
        loadList({ strDate, endDate });
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <h2 style={{ margin: '0 0 12px' }}>DID 모니터링 캡처화면</h2>

            <div style={{ display: 'flex', gap: 16, marginBottom: 12, flexWrap: 'wrap' }}>
                <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                    <input type="text" placeholder="시작일(YYYYMMDD)" value={strDate} onChange={(e) => setStrDate(e.target.value)} style={{ width: 130 }} />
                    <span>~</span>
                    <input type="text" placeholder="종료일(YYYYMMDD)" value={endDate} onChange={(e) => setEndDate(e.target.value)} style={{ width: 130 }} />
                    <button type="submit">검색</button>
                    <span style={{ color: '#64748b' }}>총 {totalCnt}건{loading ? ' (조회 중...)' : ''}</span>
                </form>

                <form onSubmit={handleUpload} style={{ display: 'flex', gap: 8, alignItems: 'center', marginLeft: 'auto' }}>
                    <input type="text" name="didId" placeholder="DID ID" style={{ width: 110 }} />
                    <input type="text" name="didMac" placeholder="DID MAC" style={{ width: 130 }} />
                    <input type="file" name="didFile" accept="image/*" />
                    <button type="submit">수동 등록</button>
                </form>
            </div>

            <div style={{ flex: 1, overflowY: 'auto', display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 12 }}>
                {list.map((pic) => (
                    <div key={`${pic.didId}-${pic.didRegDate}-${pic.didFileNm}`} style={{ border: '1px solid #e2e8f0', borderRadius: 8, padding: 8 }}>
                        <img
                            src={`${config.REACT_APP_IMG_URL}didpic/${pic.didFileNm}`}
                            alt={pic.didNm || pic.didId}
                            style={{ width: '100%', height: 140, objectFit: 'cover', borderRadius: 4, background: '#f1f5f9' }}
                        />
                        <div style={{ marginTop: 6, fontSize: 13 }}>
                            <div><strong>{pic.didNm || pic.didId}</strong></div>
                            <div style={{ color: '#64748b' }}>{pic.didId} / {pic.didMac}</div>
                            <div style={{ color: '#64748b' }}>{pic.didRegDate}</div>
                        </div>
                    </div>
                ))}
                {list.length === 0 && !loading && (
                    <div style={{ color: '#94a3b8', padding: 24 }}>조회된 캡처화면이 없습니다.</div>
                )}
            </div>
        </div>
    );
}
