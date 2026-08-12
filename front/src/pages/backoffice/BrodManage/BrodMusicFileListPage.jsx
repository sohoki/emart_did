import { useCallback, useEffect, useState, Suspense, lazy } from 'react';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import config from '@/config/index.jsx';
import URL from '@/constants/URL.jsx';

const ContentFileUploadModal = lazy(() => import('../ConManage/components/ContentFileUploadModal.jsx'));

const PAGE_UNIT = 24;

// ContentFileLibraryPage의 fileThumbSrc와 동일한 규칙으로 실 파일 경로를 만든다.
const musicFileSrc = (f) => `${config.REACT_APP_IMG_URL}${(f.fileStreCours || '').replace(/^\//, '')}${f.streFileNm}`;

// 음원 파일 전용 관리 화면 — 레거시 playContentList.jsp("음원파일관리" 탭) 참고.
// 콘텐츠 파일 라이브러리(ContentFileLibraryPage)와 동일한 API(mediaType=MUSIC 고정)를
// 쓰되, 미디어 종류 선택 없이 음원만 다루는 전용 화면으로 분리함(썸네일 갤러리라
// content-table 영역은 AppAgGrid 대신 카드형 그리드를 그대로 쓴다 — ContentFileLibraryPage와
// 동일한 골격 예외 케이스).
export default function BrodMusicFileListPage() {
    const [list, setList] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [loading, setLoading] = useState(false);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [uploadModalOpen, setUploadModalOpen] = useState(false);
    const [previewId, setPreviewId] = useState(null); // 현재 미리듣기 재생 중인 atchFileId(한 번에 하나만)

    const loadList = useCallback(async (keyword) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.CON_FILE_LIST,
                method: 'POST',
                data: {
                    mediaType: 'MUSIC',
                    searchCondition: 'orignlFileNm',
                    searchKeyword: keyword ?? '',
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
        loadList('');
    }, [loadList]);

    const onSearch = useCallback(() => {
        loadList(searchKeyword);
    }, [loadList, searchKeyword]);

    const onSearchKeyDown = useCallback((e) => {
        if (e.key === 'Enter') onSearch();
    }, [onSearch]);

    const handleReset = useCallback(() => {
        setSearchKeyword('');
        loadList('');
    }, [loadList]);

    const handleUseYnToggle = async (atchFileId, currentUseYn) => {
        await fnAjaxFetch({
            url: URL.CON_FILE_USEYN_BULK,
            method: 'POST',
            data: { atchFileIds: [atchFileId], useYn: currentUseYn === 'Y' ? 'N' : 'Y' },
        });
        loadList(searchKeyword);
    };

    const handleDelete = async (atchFileId) => {
        const result = await Swal.fire({
            icon: 'question', title: '음원 파일 삭제', text: '이 파일을 삭제하시겠습니까? 사용 중인 콘텐츠가 있으면 먼저 확인해 주세요.',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;

        const connRes = await fnAjaxFetch({ url: URL.CON_FILE_CONN_CHECK, method: 'GET', param: { atchFileId } });
        const connList = connRes?.data?.result?.resultList ?? [];
        if (connList.length > 0) {
            await Swal.fire({ icon: 'warning', title: '삭제 불가', text: `${connList.length}개의 콘텐츠에서 사용 중입니다.` });
            return;
        }

        await fnAjaxFetch({ url: `${URL.CON_FILE_INFO}/${atchFileId}.do`, method: 'DELETE' });
        setPreviewId((prev) => (prev === atchFileId ? null : prev));
        loadList(searchKeyword);
    };

    const handleTogglePreview = (atchFileId) => {
        setPreviewId((prev) => (prev === atchFileId ? null : atchFileId));
    };

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">음원 파일 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">방송 관리</li>
                        <li className="breadcrumb-item">음원 파일 관리</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <input type="text" id="searchKeyword" name="searchKeyword" placeholder="파일명 검색"
                            value={searchKeyword}
                            onChange={(e) => setSearchKeyword(e.target.value)}
                            onKeyDown={onSearchKeyDown}
                        />
                    </div>
                    <div className="col-auto content-search__action">
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={onSearch}>검색</button>
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={handleReset}>검색 초기화</button>
                        <button type="button" className="btn btn-primary btn-default__blue"
                            onClick={() => setUploadModalOpen(true)}>음원 파일 등록</button>
                    </div>
                </div>
            </div>

            <div className="col-12 content-table content-table__main">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                    <span style={{ color: '#64748b' }}>총 {totalCnt}건{loading ? ' (조회 중...)' : ''}</span>
                </div>
                {/* 배경을 명시하지 않으면(투명) 목록이 길어질 때 마지막 줄 아래 빈 영역이
                    브라우저/OS의 다크모드 강제 렌더링으로 검게 보이는 문제가 있어, 카드와
                    래퍼 모두에 명시적 배경색을 준다(ContentFileLibraryPage와 동일). */}
                <div style={{
                    display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
                    gap: 12, minHeight: 400, background: 'var(--bs-body-bg, #fff)',
                }}>
                    {list.map((file) => (
                        <div key={file.atchFileId} style={{
                            border: '1px solid #e2e8f0', borderRadius: 8, padding: 8,
                            background: 'var(--bs-body-bg, #fff)',
                        }}>
                            <button type="button" onClick={() => handleTogglePreview(file.atchFileId)} style={{
                                width: '100%', height: 110, display: 'flex', flexDirection: 'column',
                                alignItems: 'center', justifyContent: 'center', gap: 4,
                                background: previewId === file.atchFileId ? '#e0e7ff' : '#f1f5f9',
                                borderRadius: 4, color: '#64748b', border: 'none', cursor: 'pointer', padding: 0,
                            }}>
                                <svg width="28" height="28" viewBox="0 0 24 24" fill="currentColor">
                                    {previewId === file.atchFileId ? (
                                        <path d="M6 5h4v14H6zM14 5h4v14h-4z" />
                                    ) : (
                                        <path d="M8 5v14l11-7z" />
                                    )}
                                </svg>
                                <span style={{ fontSize: 11 }}>{previewId === file.atchFileId ? '재생 중' : '미리듣기'}</span>
                            </button>
                            {previewId === file.atchFileId && (
                                <audio
                                    controls autoPlay src={musicFileSrc(file)}
                                    onEnded={() => setPreviewId(null)}
                                    style={{ width: '100%', height: 32, marginTop: 6 }}
                                >
                                    브라우저가 오디오 재생을 지원하지 않습니다.
                                </audio>
                            )}
                            <div style={{ marginTop: 6, fontSize: 12, wordBreak: 'break-all' }}>{file.orignlFileNm}</div>
                            <div style={{ display: 'flex', gap: 6, marginTop: 6 }}>
                                <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                                    style={{ fontSize: 12 }}
                                    onClick={() => handleUseYnToggle(file.atchFileId, file.useYn)}>
                                    {file.useYn === 'Y' ? '사용중지' : '사용'}
                                </button>
                                <button type="button" className="btn btn-outline-danger btn-outline__gray btn-sm"
                                    style={{ fontSize: 12 }}
                                    onClick={() => handleDelete(file.atchFileId)}>삭제</button>
                            </div>
                        </div>
                    ))}
                    {list.length === 0 && !loading && (
                        <div style={{ color: '#94a3b8', padding: 24 }}>조회된 음원 파일이 없습니다.</div>
                    )}
                </div>
            </div>

            <Suspense fallback={null}>
                {uploadModalOpen && (
                    <ContentFileUploadModal
                        open={uploadModalOpen}
                        title="음원 파일 등록"
                        dragText="음원 파일(mp3/wav/mid)을 클릭하거나 끌어놓으세요 (여러 개 선택 가능)"
                        accept={{ 'audio/mpeg': ['.mp3'], 'audio/wav': ['.wav'], 'audio/midi': ['.mid'] }}
                        onClose={() => setUploadModalOpen(false)}
                        onUploaded={() => { setUploadModalOpen(false); loadList(searchKeyword); }}
                    />
                )}
            </Suspense>
        </div>
    );
}
