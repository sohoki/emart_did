import { useCallback, useEffect, useState } from 'react';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import config from '@/config/index.jsx';
import URL from '@/constants/URL.jsx';

const PAGE_UNIT = 24;

const INITIAL_SEARCH_FORM = {
    strDate: '',
    endDate: '',
};

// DID 모니터링 캡처화면 — 이미지 갤러리라 목록형 화면의 그리드(AppAgGrid) 대신
// content-table 영역에 카드형 그리드를 쓴다(11절 골격의 검색/헤더 3단 구성은 그대로 따름).
export default function DidPicListPage() {
    const [list, setList] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [loading, setLoading] = useState(false);
    const [tempParams, setTempParams] = useState(INITIAL_SEARCH_FORM);

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
        loadList({});
    }, [loadList]);

    const handleInputChange = useCallback((e) => {
        const { name, value } = e.target;
        setTempParams((prev) => ({ ...prev, [name]: value }));
    }, []);

    const onSearch = useCallback(() => {
        loadList(tempParams);
    }, [loadList, tempParams]);

    const onSearchKeyDown = useCallback((e) => {
        if (e.key === 'Enter') onSearch();
    }, [onSearch]);

    const handleReset = useCallback(() => {
        setTempParams(INITIAL_SEARCH_FORM);
        loadList({});
    }, [loadList]);

    const handleUpload = useCallback(async (e) => {
        e.preventDefault();
        const form = e.target;
        const didId = form.didId.value.trim();
        const didMac = form.didMac.value.trim();
        const file = form.didFile.files?.[0];

        if (!didId || !file) {
            await Swal.fire({ icon: 'warning', title: '입력 확인', text: 'DID ID와 파일을 선택해 주세요.' });
            return;
        }

        const formData = new FormData();
        formData.append('didId', didId);
        formData.append('didMac', didMac);
        formData.append('didFile', file);

        await fnAjaxFetch({ url: URL.DID_PIC_UPLOAD, method: 'POST', data: formData });
        form.reset();
        loadList(tempParams);
    }, [loadList, tempParams]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">DID 모니터링 캡처화면</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">운영 관리</li>
                        <li className="breadcrumb-item">DID 캡처화면</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <input type="text" id="strDate" name="strDate" placeholder="시작일(YYYYMMDD)"
                            value={tempParams.strDate}
                            onChange={handleInputChange}
                            onKeyDown={onSearchKeyDown}
                        />
                        <input type="text" id="endDate" name="endDate" placeholder="종료일(YYYYMMDD)"
                            value={tempParams.endDate}
                            onChange={handleInputChange}
                            onKeyDown={onSearchKeyDown}
                        />
                    </div>
                    <div className="col-auto content-search__action">
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={onSearch}>검색</button>
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={handleReset}>검색 초기화</button>
                        <form onSubmit={handleUpload} style={{ display: 'inline-flex', gap: 8, alignItems: 'center' }}>
                            <input type="text" name="didId" placeholder="DID ID" style={{ width: 110 }} />
                            <input type="text" name="didMac" placeholder="DID MAC" style={{ width: 130 }} />
                            <input type="file" name="didFile" accept="image/*" />
                            <button type="submit" className="btn btn-primary btn-default__blue">수동 등록</button>
                        </form>
                    </div>
                </div>
            </div>

            <div className="col-12 content-table content-table__main">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                    <span style={{ color: '#64748b' }}>총 {totalCnt}건{loading ? ' (조회 중...)' : ''}</span>
                </div>
                <div style={{
                    display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                    gap: 12, minHeight: 400,
                }}>
                    {list.map((pic) => (
                        <div key={`${pic.didId}-${pic.didRegDate}-${pic.didFileNm}`}
                            style={{ border: '1px solid #e2e8f0', borderRadius: 8, padding: 8 }}>
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
        </div>
    );
}
