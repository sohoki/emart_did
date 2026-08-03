import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

export default function ContentDetailEditorPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const conSeq = searchParams.get('conSeq') ?? '';

    const [contentInfo, setContentInfo] = useState(null);
    const [pages, setPages] = useState([]);
    const [selectedDetailSeq, setSelectedDetailSeq] = useState('');
    const [fileList, setFileList] = useState([]);
    const [sumTime, setSumTime] = useState('0');
    const [loading, setLoading] = useState(false);

    const [searchKeyword, setSearchKeyword] = useState('');
    const [mediaType, setMediaType] = useState('');
    const [searchResult, setSearchResult] = useState([]);

    const loadContentInfo = useCallback(async () => {
        const res = await fnAjaxFetch({ url: `${URL.CON_MUTI_INFO}/${conSeq}.do`, method: 'GET', showLoading: false });
        const detail = res?.data?.result?.result;
        setContentInfo(detail);
        return detail;
    }, [conSeq]);

    const loadPages = useCallback(async (conType) => {
        const res = await fnAjaxFetch({
            url: URL.CON_MUTI_DETAIL_COMBO, method: 'GET',
            param: { code: conType, conSeq }, showLoading: false,
        });
        const list = res?.data?.result?.resultList ?? [];
        setPages(list);
        if (list.length > 0) {
            setSelectedDetailSeq(list[0].detailSeq);
        }
    }, [conSeq]);

    const loadFileList = useCallback(async (detailSeq) => {
        if (!detailSeq) {
            setFileList([]);
            return;
        }
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.CON_DETAIL_FILE_LIST, method: 'GET',
                param: { conSeq, detailSeq }, showLoading: false,
            });
            setFileList(res?.data?.result?.resultList ?? []);

            const sumRes = await fnAjaxFetch({ url: URL.CON_DETAIL_FILE_SUM_TIME, method: 'GET', param: { detailSeq }, showLoading: false });
            setSumTime(sumRes?.data?.result?.result ?? '0');
        } finally {
            setLoading(false);
        }
    }, [conSeq]);

    useEffect(() => {
        if (!getCookie('accessToken')) {
            navigate('/login', { replace: true });
            return;
        }
        if (!conSeq) return;
        (async () => {
            const detail = await loadContentInfo();
            if (detail?.conType) {
                await loadPages(detail.conType);
            }
        })();
    }, [conSeq, loadContentInfo, loadPages, navigate]);

    useEffect(() => {
        loadFileList(selectedDetailSeq);
    }, [selectedDetailSeq, loadFileList]);

    const handleFileSearch = async (e) => {
        e.preventDefault();
        const res = await fnAjaxFetch({
            url: URL.CON_FILE_LIST, method: 'POST',
            data: { mediaType, searchCondition: 'orignlFileNm', searchKeyword, pageIndex: 1, pageUnit: 30 },
        });
        setSearchResult(res?.data?.result?.resultList ?? []);
    };

    const handleAddFile = async (atchFileId) => {
        if (!selectedDetailSeq) {
            Swal.fire({ icon: 'warning', title: '선택 필요', text: '먼저 페이지 탭을 선택해 주세요.' });
            return;
        }
        const nextOrder = fileList.length + 1;
        await fnAjaxFetch({
            url: URL.CON_DETAIL_FILE_INSERT, method: 'POST',
            data: { conSeq, detailSeq: selectedDetailSeq, atchFileId, fileOrder: String(nextOrder) },
        });
        loadFileList(selectedDetailSeq);
    };

    const handleTimeChange = async (fileSeq, timeInterval) => {
        await fnAjaxFetch({
            url: URL.CON_DETAIL_FILE_TIME_UPDATE, method: 'POST',
            data: { fileSeq, detailSeq: selectedDetailSeq, timeInterval },
        });
        loadFileList(selectedDetailSeq);
    };

    const handleMove = async (index, direction) => {
        const targetIndex = index + direction;
        if (targetIndex < 0 || targetIndex >= fileList.length) return;

        const current = fileList[index];
        const target = fileList[targetIndex];
        await fnAjaxFetch({ url: URL.CON_DETAIL_FILE_ORDER_UPDATE, method: 'POST', data: { fileSeq: current.fileSeq, fileOrder: target.fileOrder } });
        await fnAjaxFetch({ url: URL.CON_DETAIL_FILE_ORDER_UPDATE, method: 'POST', data: { fileSeq: target.fileSeq, fileOrder: current.fileOrder } });
        loadFileList(selectedDetailSeq);
    };

    const handleDelete = async (fileSeq) => {
        const result = await Swal.fire({
            icon: 'question', title: '파일 제거', text: '이 페이지에서 파일을 제거하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        await fnAjaxFetch({ url: `${URL.CON_DETAIL_FILE_INFO}/${fileSeq}.do`, method: 'DELETE', param: { detailSeq: selectedDetailSeq } });
        loadFileList(selectedDetailSeq);
    };

    if (!conSeq) {
        return <div style={{ padding: 16 }}>conSeq가 없습니다. 콘텐츠 목록에서 "편성" 버튼으로 진입해 주세요.</div>;
    }

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <h2 style={{ margin: '0 0 12px' }}>콘텐츠 편성 — {contentInfo?.conNm} ({conSeq})</h2>

            <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                {pages.map((p) => (
                    <button
                        key={p.detailSeq}
                        type="button"
                        onClick={() => setSelectedDetailSeq(p.detailSeq)}
                        style={{ fontWeight: selectedDetailSeq === p.detailSeq ? 'bold' : 'normal', textDecoration: selectedDetailSeq === p.detailSeq ? 'underline' : 'none' }}
                    >
                        페이지 {p.detailOrder}
                    </button>
                ))}
                <span style={{ marginLeft: 'auto', color: '#64748b' }}>총 재생시간(초): {sumTime}{loading ? ' (조회 중...)' : ''}</span>
            </div>

            <div style={{ display: 'flex', gap: 16, flex: 1, minHeight: 0 }}>
                <div style={{ flex: 1.3, overflowY: 'auto' }}>
                    <h3>배치된 파일</h3>
                    <table style={{ borderCollapse: 'collapse', width: '100%' }}>
                        <thead>
                            <tr style={{ background: '#f8fafc' }}>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>순서</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>파일명</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>재생시간(초)</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}></th>
                            </tr>
                        </thead>
                        <tbody>
                            {fileList.map((f, idx) => (
                                <tr key={f.fileSeq}>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>
                                        {f.fileOrder}{' '}
                                        <button type="button" onClick={() => handleMove(idx, -1)} disabled={idx === 0}>↑</button>
                                        <button type="button" onClick={() => handleMove(idx, 1)} disabled={idx === fileList.length - 1}>↓</button>
                                    </td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{f.orignlFileNm}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>
                                        <input
                                            type="text"
                                            defaultValue={f.timeInterval}
                                            style={{ width: 60 }}
                                            onBlur={(e) => {
                                                if (e.target.value !== f.timeInterval) handleTimeChange(f.fileSeq, e.target.value);
                                            }}
                                        />
                                    </td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>
                                        <button type="button" onClick={() => handleDelete(f.fileSeq)}>제거</button>
                                    </td>
                                </tr>
                            ))}
                            {fileList.length === 0 && (
                                <tr><td colSpan={4} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>배치된 파일이 없습니다.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>

                <div style={{ flex: 1, borderLeft: '1px solid #e2e8f0', paddingLeft: 16, overflowY: 'auto' }}>
                    <h3>파일 검색/추가</h3>
                    <form onSubmit={handleFileSearch} style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                        <select value={mediaType} onChange={(e) => setMediaType(e.target.value)}>
                            <option value="">전체</option>
                            <option value="IMAGE">이미지</option>
                            <option value="MEDIA">영상</option>
                            <option value="MUSIC">음원</option>
                        </select>
                        <input type="text" placeholder="파일명 검색" value={searchKeyword} onChange={(e) => setSearchKeyword(e.target.value)} />
                        <button type="submit">검색</button>
                    </form>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))', gap: 8 }}>
                        {searchResult.map((f) => (
                            <div key={f.atchFileId} style={{ border: '1px solid #e2e8f0', borderRadius: 6, padding: 6, fontSize: 12 }}>
                                <div style={{ wordBreak: 'break-all', marginBottom: 4 }}>{f.orignlFileNm}</div>
                                <button type="button" onClick={() => handleAddFile(f.atchFileId)}>추가</button>
                            </div>
                        ))}
                        {searchResult.length === 0 && <div style={{ color: '#94a3b8' }}>검색 결과가 없습니다.</div>}
                    </div>
                </div>
            </div>
        </div>
    );
}
