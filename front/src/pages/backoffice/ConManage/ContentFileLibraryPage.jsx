import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import Swal from '@/lib/swal.js';
import config from '@/config/index.jsx';
import URL from '@/constants/URL.jsx';

const PAGE_UNIT = 24;

export default function ContentFileLibraryPage() {
    const navigate = useNavigate();
    const [list, setList] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [mediaType, setMediaType] = useState('');
    const [searchKeyword, setSearchKeyword] = useState('');
    const [loading, setLoading] = useState(false);

    const loadList = useCallback(async (params) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.CON_FILE_LIST,
                method: 'POST',
                data: {
                    mediaType: params?.mediaType ?? '',
                    searchCondition: 'orignlFileNm',
                    searchKeyword: params?.searchKeyword ?? '',
                    pageIndex: 1,
                    pageUnit: PAGE_UNIT,
                },
                showLoading: false,
            });
            const resultList = res?.data?.result?.resultList ?? [];
            setList(resultList);
            setTotalCnt(res?.data?.result?.totalCnt ?? resultList.length);
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
        loadList({ mediaType, searchKeyword });
    };

    const handleUseYnToggle = async (atchFileId, currentUseYn) => {
        await fnAjaxFetch({
            url: URL.CON_FILE_USEYN_BULK,
            method: 'POST',
            data: { atchFileIds: [atchFileId], useYn: currentUseYn === 'Y' ? 'N' : 'Y' },
        });
        loadList({ mediaType, searchKeyword });
    };

    const handleDelete = async (atchFileId) => {
        const result = await Swal.fire({
            icon: 'question', title: '파일 삭제', text: '이 파일을 삭제하시겠습니까? 사용 중인 콘텐츠가 있으면 먼저 확인해 주세요.',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;

        const connRes = await fnAjaxFetch({ url: URL.CON_FILE_CONN_CHECK, method: 'GET', param: { atchFileId } });
        const connList = connRes?.data?.result?.resultList ?? [];
        if (connList.length > 0) {
            Swal.fire({ icon: 'warning', title: '삭제 불가', text: `${connList.length}개의 콘텐츠에서 사용 중입니다.` });
            return;
        }

        await fnAjaxFetch({ url: `${URL.CON_FILE_INFO}/${atchFileId}.do`, method: 'DELETE' });
        loadList({ mediaType, searchKeyword });
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <h2 style={{ margin: '0 0 12px' }}>콘텐츠 파일 라이브러리</h2>

            <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, marginBottom: 12, alignItems: 'center' }}>
                <select value={mediaType} onChange={(e) => setMediaType(e.target.value)}>
                    <option value="">전체</option>
                    <option value="IMAGE">이미지</option>
                    <option value="MEDIA">영상</option>
                    <option value="MUSIC">음원</option>
                </select>
                <input type="text" placeholder="파일명 검색" value={searchKeyword} onChange={(e) => setSearchKeyword(e.target.value)} />
                <button type="submit">검색</button>
                <span style={{ color: '#64748b' }}>총 {totalCnt}건{loading ? ' (조회 중...)' : ''}</span>
            </form>

            <div style={{ flex: 1, overflowY: 'auto', display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: 12 }}>
                {list.map((file) => (
                    <div key={file.atchFileId} style={{ border: '1px solid #e2e8f0', borderRadius: 8, padding: 8 }}>
                        {file.mediaType === 'IMAGE' ? (
                            <img
                                src={`${config.REACT_APP_IMG_URL}${(file.fileStreCours || '').replace(/^\//, '')}${file.streFileNm}`}
                                alt={file.orignlFileNm}
                                style={{ width: '100%', height: 110, objectFit: 'cover', borderRadius: 4, background: '#f1f5f9' }}
                            />
                        ) : (
                            <div style={{ width: '100%', height: 110, display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f1f5f9', borderRadius: 4, color: '#94a3b8' }}>
                                {file.mediaType || '파일'}
                            </div>
                        )}
                        <div style={{ marginTop: 6, fontSize: 12, wordBreak: 'break-all' }}>{file.orignlFileNm}</div>
                        <div style={{ display: 'flex', gap: 6, marginTop: 6 }}>
                            <button type="button" onClick={() => handleUseYnToggle(file.atchFileId, file.useYn)}>
                                {file.useYn === 'Y' ? '사용중지' : '사용'}
                            </button>
                            <button type="button" onClick={() => handleDelete(file.atchFileId)}>삭제</button>
                        </div>
                    </div>
                ))}
                {list.length === 0 && !loading && (
                    <div style={{ color: '#94a3b8', padding: 24 }}>조회된 파일이 없습니다.</div>
                )}
            </div>
        </div>
    );
}
