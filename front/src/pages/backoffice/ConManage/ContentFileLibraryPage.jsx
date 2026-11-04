import { useCallback, useEffect, useState, Suspense, lazy } from 'react';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import config from '@/config/index.jsx';
import URL from '@/constants/URL.jsx';

const ContentFileUploadModal = lazy(() => import('./components/ContentFileUploadModal.jsx'));
const ContentFileDetailModal = lazy(() => import('./components/ContentFileDetailModal.jsx'));
const ContentFileConnModal = lazy(() => import('./components/ContentFileConnModal.jsx'));

const PAGE_UNIT = 24;

const INITIAL_SEARCH_FORM = {
    mediaType: '',
    searchKeyword: '',
};

// 페이징 아이콘(|< < > >|) — AG Grid 페이징 패널 아이콘 모양을 SVG로 재현
const PAGE_ICON_PATHS = {
    first: 'M11 5l-5 5 5 5M5 5v10',
    prev: 'M12 5l-5 5 5 5',
    next: 'M8 5l5 5-5 5',
    last: 'M9 5l5 5-5 5M15 5v10',
};

function PageIconButton({ icon, title, disabled, onClick }) {
    return (
        <button type="button" title={title} aria-label={title} disabled={disabled} onClick={onClick}
            style={{
                display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                width: 28, height: 28, padding: 0, border: 'none', background: 'transparent',
                color: disabled ? '#cbd5e1' : 'var(--bs-body-color, #1e293b)',
                cursor: disabled ? 'default' : 'pointer',
            }}>
            <svg width="18" height="18" viewBox="0 0 20 20" fill="none" stroke="currentColor"
                strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <path d={PAGE_ICON_PATHS[icon]} />
            </svg>
        </button>
    );
}

// 콘텐츠 파일(이미지/영상/음원) 라이브러리 — 썸네일 갤러리라 content-table 영역은
// AppAgGrid 대신 카드형 그리드를 그대로 쓴다(DidPicListPage와 동일한 골격 예외 케이스).
// embedded=true면 다른 화면의 탭 안에 넣어 쓰도록 제목/breadcrumb 영역을 생략한다(문화센터 룸 관리 > 콘텐츠 등록 탭).
export default function ContentFileLibraryPage({ embedded = false }) {
    const [list, setList] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [pageIndex, setPageIndex] = useState(1);
    const [loading, setLoading] = useState(false);
    const [tempParams, setTempParams] = useState(INITIAL_SEARCH_FORM);
    // 페이지 이동 시에는 마지막으로 "검색"한 조건을 유지한다(입력 중인 tempParams가 아님)
    const [appliedParams, setAppliedParams] = useState(INITIAL_SEARCH_FORM);
    const [uploadModalOpen, setUploadModalOpen] = useState(false);
    // 파일 상세정보 모달 대상 { atchFileId, mediaType } / 삭제 불가 시 사용 중 콘텐츠 모달 { fileName, connList }
    const [detailTarget, setDetailTarget] = useState(null);
    const [connTarget, setConnTarget] = useState(null);

    const loadList = useCallback(async (params, page = 1) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.CON_FILE_LIST,
                method: 'POST',
                data: {
                    mediaType: params?.mediaType ?? '',
                    searchCondition: 'orignlFileNm',
                    searchKeyword: params?.searchKeyword ?? '',
                    pageIndex: page,
                    pageUnit: PAGE_UNIT,
                },
                showLoading: false,
            });
            const resultList = res?.data?.result?.resultList ?? [];
            setList(resultList);
            setTotalCnt(res?.data?.result?.totalCnt ?? resultList.length);
            setAppliedParams(params ?? INITIAL_SEARCH_FORM);
            setPageIndex(page);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadList({});
    }, [loadList]);

    const totalPages = Math.max(1, Math.ceil(totalCnt / PAGE_UNIT));
    // 사용/삭제/상세 저장 후에는 보고 있던 페이지를 다시 조회
    const reloadCurrentPage = useCallback(() => {
        loadList(appliedParams, pageIndex);
    }, [loadList, appliedParams, pageIndex]);

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

    const handleUseYnToggle = async (atchFileId, currentUseYn) => {
        await fnAjaxFetch({
            url: URL.CON_FILE_USEYN_BULK,
            method: 'POST',
            data: { atchFileIds: [atchFileId], useYn: currentUseYn === 'Y' ? 'N' : 'Y' },
        });
        reloadCurrentPage();
    };

    const handleDelete = async (atchFileId, fileName) => {
        const result = await Swal.fire({
            icon: 'question', title: '파일 삭제', text: '이 파일을 삭제하시겠습니까? 사용 중인 콘텐츠가 있으면 먼저 확인해 주세요.',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;

        const connRes = await fnAjaxFetch({ url: URL.CON_FILE_CONN_CHECK, method: 'GET', param: { atchFileId } });
        const connList = connRes?.data?.result?.resultList ?? [];
        if (connList.length > 0) {
            setConnTarget({ fileName, connList });
            return;
        }

        await fnAjaxFetch({ url: `${URL.CON_FILE_INFO}/${atchFileId}.do`, method: 'DELETE' });
        // 마지막 페이지의 마지막 1건을 지우면 빈 페이지가 되므로 한 페이지 앞으로
        const page = list.length === 1 && pageIndex > 1 ? pageIndex - 1 : pageIndex;
        loadList(appliedParams, page);
    };

    return (
        <div className={embedded ? 'row g-0' : 'row g-0 main-contents'}>
            {!embedded && (
                <div className="col-12 content-header">
                    <div className="content-header__title">콘텐츠 파일 라이브러리</div>
                    <div className="content-header__breadcrumb">
                        <ol className="breadcrumb">
                            <li className="breadcrumb-item">콘텐츠 관리</li>
                            <li className="breadcrumb-item">콘텐츠 파일 라이브러리</li>
                        </ol>
                    </div>
                </div>
            )}

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <select id="mediaType" name="mediaType"
                            value={tempParams.mediaType} onChange={handleInputChange}>
                            <option value="">전체</option>
                            <option value="IMAGE">이미지</option>
                            <option value="MEDIA">영상</option>
                            <option value="MUSIC">음원</option>
                        </select>
                        <input type="text" id="searchKeyword" name="searchKeyword" placeholder="파일명 검색"
                            value={tempParams.searchKeyword}
                            onChange={handleInputChange}
                            onKeyDown={onSearchKeyDown}
                        />
                    </div>
                    <div className="col-auto content-search__action">
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={onSearch}>검색</button>
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={handleReset}>검색 초기화</button>
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={() => Swal.fire({ icon: 'info', title: '미디어 파일정리', text: '준비 중인 기능입니다.' })}
                        >미디어 파일정리</button>
                        <button type="button" className="btn btn-primary btn-default__blue"
                            onClick={() => setUploadModalOpen(true)}>미디어 파일 등록</button>
                    </div>
                </div>
            </div>

            <div className="col-12 content-table content-table__main">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                    <span style={{ color: '#64748b' }}>총 {totalCnt}건{loading ? ' (조회 중...)' : ''}</span>
                </div>
                {/* 배경을 명시하지 않으면(투명) 목록이 길어질 때 마지막 줄 아래 빈 영역이
                    브라우저/OS의 다크모드 강제 렌더링으로 검게 보이는 문제가 있어, 카드와
                    래퍼 모두에 명시적 배경색을 준다. */}
                <div style={{
                    display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
                    gap: 12, minHeight: 400, background: 'var(--bs-body-bg, #fff)',
                }}>
                    {list.map((file) => (
                        <div key={file.atchFileId}
                            onClick={() => setDetailTarget({ atchFileId: file.atchFileId, mediaType: file.mediaType })}
                            title="클릭하면 파일 상세정보를 확인/저장할 수 있습니다"
                            style={{
                                border: '1px solid #e2e8f0', borderRadius: 8, padding: 8,
                                background: 'var(--bs-body-bg, #fff)', cursor: 'pointer',
                            }}>
                            {/* 이미지는 원본, 동영상은 업로드 시 생성된 썸네일(fileThumnail)을 표시 */}
                            {(file.mediaType === 'IMAGE' || (file.mediaType === 'MEDIA' && file.fileThumnail)) ? (
                                <img
                                    src={`${config.REACT_APP_IMG_URL}${(file.fileStreCours || '').replace(/^\//, '')}${file.mediaType === 'IMAGE' ? file.streFileNm : file.fileThumnail}`}
                                    alt={file.orignlFileNm}
                                    style={{ width: '100%', height: 110, objectFit: 'cover', borderRadius: 4, background: '#f1f5f9' }}
                                />
                            ) : (
                                <div style={{
                                    width: '100%', height: 110, display: 'flex', alignItems: 'center', justifyContent: 'center',
                                    background: '#f1f5f9', borderRadius: 4, color: '#94a3b8',
                                }}>
                                    {file.mediaType || '파일'}
                                </div>
                            )}
                            <div style={{ marginTop: 6, fontSize: 12, wordBreak: 'break-all' }}>{file.orignlFileNm}</div>
                            {/* 버튼 클릭이 카드 onClick(상세 모달)까지 전파되지 않도록 차단 */}
                            <div style={{ display: 'flex', gap: 6, marginTop: 6 }} onClick={(e) => e.stopPropagation()}>
                                <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                                    style={{ fontSize: 12 }}
                                    onClick={() => handleUseYnToggle(file.atchFileId, file.useYn)}>
                                    {file.useYn === 'Y' ? '사용중지' : '사용'}
                                </button>
                                <button type="button" className="btn btn-outline-danger btn-outline__gray btn-sm"
                                    style={{ fontSize: 12 }}
                                    onClick={() => handleDelete(file.atchFileId, file.orignlFileNm)}>삭제</button>
                            </div>
                        </div>
                    ))}
                    {list.length === 0 && !loading && (
                        <div style={{ color: '#94a3b8', padding: 24 }}>조회된 파일이 없습니다.</div>
                    )}
                </div>
                {/* 서버 페이징 — 다른 탭 그리드(AG Grid) 하단 페이징 패널과 같은 모양 */}
                {totalCnt > 0 && (
                    <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: 4, marginTop: 16, fontSize: 14 }}>
                        <PageIconButton icon="first" title="처음" disabled={loading || pageIndex <= 1}
                            onClick={() => loadList(appliedParams, 1)} />
                        <PageIconButton icon="prev" title="이전" disabled={loading || pageIndex <= 1}
                            onClick={() => loadList(appliedParams, pageIndex - 1)} />
                        <span style={{ margin: '0 8px', color: 'var(--bs-body-color, #1e293b)' }}>
                            Page <b>{pageIndex}</b> of <b>{totalPages}</b>
                        </span>
                        <PageIconButton icon="next" title="다음" disabled={loading || pageIndex >= totalPages}
                            onClick={() => loadList(appliedParams, pageIndex + 1)} />
                        <PageIconButton icon="last" title="마지막" disabled={loading || pageIndex >= totalPages}
                            onClick={() => loadList(appliedParams, totalPages)} />
                    </div>
                )}
            </div>

            <Suspense fallback={null}>
                {uploadModalOpen && (
                    <ContentFileUploadModal
                        open={uploadModalOpen}
                        onClose={() => setUploadModalOpen(false)}
                        onUploaded={() => { setUploadModalOpen(false); loadList(appliedParams, 1); }}
                    />
                )}
                {detailTarget && (
                    <ContentFileDetailModal
                        open={!!detailTarget}
                        atchFileId={detailTarget.atchFileId}
                        mediaType={detailTarget.mediaType}
                        onClose={() => setDetailTarget(null)}
                        onSaved={() => { setDetailTarget(null); reloadCurrentPage(); }}
                    />
                )}
                {connTarget && (
                    <ContentFileConnModal
                        open={!!connTarget}
                        fileName={connTarget.fileName}
                        connList={connTarget.connList}
                        onClose={() => setConnTarget(null)}
                    />
                )}
            </Suspense>
        </div>
    );
}
