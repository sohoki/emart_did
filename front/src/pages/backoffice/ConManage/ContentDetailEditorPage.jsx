import { useCallback, useEffect, useMemo, useState, Suspense, lazy } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import config from '@/config/index.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const ContentMutiFormModal = lazy(() => import('./components/ContentMutiFormModal.jsx'));

const MEDIA_PAGE_UNIT = 20;

const EMPTY_MUTI_FORM = {
    mode: 'Ins', 
    conSeq: '', 
    conNm: '', 
    conScreen: '', 
    conType: '', 
    conUseYn: 'Y',
    conWidth: '', 
    conHeight: '', 
    conMid: '', 
    conNextSeq: '', 
    conPlayType: '',
};

// 파일 썸네일 — ContentFileLibraryPage와 동일한 규칙(이미지만 실제 썸네일, 그 외는 타입 라벨 박스)
const fileThumbSrc = (f) => {
    if (f.mediaType === 'IMAGE') {
        return `${config.REACT_APP_IMG_URL}${(f.fileStreCours || '').replace(/^\//, '')}${f.streFileNm}`;
    }
    if (f.mediaType === 'MEDIA' && f.fileThumnail) {
        return `${config.REACT_APP_IMG_URL}${(f.fileStreCours || '').replace(/^\//, '')}${f.fileThumnail}`;
    }
    return null;
};

const Thumb = ({ file, size = 60 }) => {
    const src = fileThumbSrc(file);
    if (src) {
        return (
            <img src={src} alt={file.orignlFileNm} style={{ width: size, height: size, objectFit: 'cover', borderRadius: 4, background: '#f1f5f9' }} />
        );
    }
    return (
        <div style={{
            width: size, height: size, display: 'flex', alignItems: 'center', justifyContent: 'center',
            background: '#f1f5f9', borderRadius: 4, color: '#94a3b8', fontSize: 11,
        }}>
            {file.mediaType || '파일'}
        </div>
    );
};

// 화면 구성(멀티페이지 콘텐츠) 편성 화면 — 레거시 conMutiView_back.jsp 참고.
// "화면생성"(미리보기)/"스케줄전송"은 ContentDetailFileInfoManageController의
// preViewCheck.do/contentPreview.do/contentScheduleSend.do에 연결됨. contentPreview.do는
// EMART_DID/did/upload/{yyyyMM}/ 아래에 정적 HTML을 생성하고, DB(conFile)에는 varchar(30)
// 제약 때문에 파일명만 저장 — 실제 미리보기에 필요한 전체 상대경로는 응답의
// result.fileUrl로 내려줘서 그걸로 /upload/ 경로를 연다.
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
    const [timeInputs, setTimeInputs] = useState({});

    const [searchKeyword, setSearchKeyword] = useState('');
    const [mediaType, setMediaType] = useState('');
    const [searchResult, setSearchResult] = useState([]);
    const [mediaPageIndex, setMediaPageIndex] = useState(1);
    const [mediaTotalCnt, setMediaTotalCnt] = useState(0);
    const [dragIndex, setDragIndex] = useState(null);

    const [editModalOpen, setEditModalOpen] = useState(false);
    const [editForm, setEditForm] = useState(EMPTY_MUTI_FORM);
    const [conTypeOptions, setConTypeOptions] = useState([]);
    const [screenTypeOptions, setScreenTypeOptions] = useState([]);
    const [playTypeOptions, setPlayTypeOptions] = useState([]);
    const [nextSeqOptions, setNextSeqOptions] = useState([]);

    const loadContentInfo = useCallback(async () => {
        const res = await fnAjaxFetch({ url: `${URL.CON_MUTI_INFO}/${conSeq}.do`, method: 'GET', showLoading: false });
        const detail = res?.data?.result?.result;
        setContentInfo(detail);
        return detail;
    }, [conSeq]);

    // 레거시 conMutiView_back.jsp의 conMutiSelect.do 호출과 동일하게 CON_SCREEN 코드로 조회한다
    // (CON_TYPE 이름 문자열이 아님 — 예전 코드가 conType을 잘못 넘기던 버그를 여기서 같이 바로잡음).
    const loadPages = useCallback(async (conScreen) => {
        const res = await fnAjaxFetch({
            url: URL.CON_MUTI_DETAIL_COMBO, method: 'GET',
            param: { code: conScreen, conSeq }, showLoading: false,
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
            const list = res?.data?.result?.resultList ?? [];
            setFileList(list);
            setTimeInputs(Object.fromEntries(list.map((f) => [f.fileSeq, f.timeInterval ?? ''])));

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
            if (detail?.conScreen) {
                await loadPages(detail.conScreen);
            }
        })();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [conSeq]);

    useEffect(() => {
        loadFileList(selectedDetailSeq);
    }, [selectedDetailSeq, loadFileList]);

    const loadMediaSearch = useCallback(async (pageIndex = 1) => {
        const res = await fnAjaxFetch({
            url: URL.CON_FILE_LIST, method: 'POST',
            data: { mediaType, searchCondition: 'orignlFileNm', searchKeyword, pageIndex, pageUnit: MEDIA_PAGE_UNIT },
            showLoading: false,
        });
        setSearchResult(res?.data?.result?.resultList ?? []);
        setMediaTotalCnt(res?.data?.result?.totalCnt ?? 0);
        setMediaPageIndex(pageIndex);
    }, [mediaType, searchKeyword]);

    // 레거시 conMutiView_back.jsp의 contentLst() — 검색 없이도 페이지 진입 시 미디어 리스트를
    // 바로 보여준다(기존엔 "조회" 버튼을 눌러야만 나왔음).
    useEffect(() => {
        if (!conSeq) return;
        loadMediaSearch(1);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [conSeq]);

    const handleFileSearch = async (e) => {
        e.preventDefault();
        loadMediaSearch(1);
    };

    const mediaTotalPages = Math.max(1, Math.ceil(mediaTotalCnt / MEDIA_PAGE_UNIT));

    const handleAddFile = useCallback(async (file) => {
        if (!selectedDetailSeq) {
            await Swal.fire({ icon: 'warning', title: '선택 필요', text: '먼저 페이지 탭을 선택해 주세요.' });
            return;
        }
        const nextOrder = fileList.length + 1;
        // 이미지는 재생시간을 자동으로 알 수 없어 기본 10초로 채우고, 동영상/음원은 실제 파일의
        // 재생시간(playTime)을 그대로 송출시간으로 채워서 등록한다.
        const timeInterval = file.mediaType === 'IMAGE' ? '10' : (file.playTime || '');
        await fnAjaxFetch({
            url: URL.CON_DETAIL_FILE_INSERT, method: 'POST',
            data: {
                conSeq, detailSeq: selectedDetailSeq, atchFileId: file.atchFileId, fileOrder: String(nextOrder),
                ...(timeInterval ? { timeInterval } : {}),
            },
        });
        loadFileList(selectedDetailSeq);
    }, [conSeq, selectedDetailSeq, fileList.length, loadFileList]);

    const handleTimeApply = useCallback(async (fileSeq) => {
        const timeInterval = timeInputs[fileSeq];
        if (!timeInterval) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '송출시간을 입력해 주세요.' });
            return;
        }
        await fnAjaxFetch({
            url: URL.CON_DETAIL_FILE_TIME_UPDATE, method: 'POST',
            data: { fileSeq, detailSeq: selectedDetailSeq, timeInterval },
        });
        loadFileList(selectedDetailSeq);
    }, [selectedDetailSeq, timeInputs, loadFileList]);

    // 드래그로 순서를 옮기면 영향받는 파일들의 fileOrder를 0부터 다시 순서대로 매긴다
    // (레거시도 드래그 후 dragHandle 기준으로 전체 순번을 재계산하는 방식이었음).
    const handleDropReorder = useCallback(async (fromIndex, toIndex) => {
        if (fromIndex === null || fromIndex === toIndex) return;
        const reordered = [...fileList];
        const [moved] = reordered.splice(fromIndex, 1);
        reordered.splice(toIndex, 0, moved);

        await Promise.all(reordered.map((f, idx) => (
            String(idx) === f.fileOrder ? null : fnAjaxFetch({
                url: URL.CON_DETAIL_FILE_ORDER_UPDATE, method: 'POST',
                data: { fileSeq: f.fileSeq, fileOrder: String(idx) },
            })
        )));
        loadFileList(selectedDetailSeq);
    }, [fileList, selectedDetailSeq, loadFileList]);

    const handleRemoveFile = useCallback(async (fileSeq) => {
        const result = await Swal.fire({
            icon: 'question', title: '파일 제외', text: '이 페이지에서 파일을 제외하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        // fnAjaxFetch는 GET이 아닌 메서드에 param을 주면 쿼리스트링이 아니라 요청 바디로
        // 보내는데, 백엔드는 detailSeq를 @RequestParam(쿼리스트링)으로 받는다 — 바디로 보내면
        // "Required request parameter 'detailSeq' ... is not present" 400 에러가 남. 그래서
        // URL에 직접 쿼리스트링으로 붙인다.
        await fnAjaxFetch({
            url: `${URL.CON_DETAIL_FILE_INFO}/${fileSeq}.do?detailSeq=${encodeURIComponent(selectedDetailSeq)}`,
            method: 'DELETE',
        });
        loadFileList(selectedDetailSeq);
    }, [selectedDetailSeq, loadFileList]);

    const handleOpenEditModal = useCallback(async () => {
        const res = await fnAjaxFetch({
            url: URL.CON_MUTI_FORM_DATA, method: 'GET', param: { mode: 'Edt', conSeq },
        });
        const result = res?.data?.result || {};
        setConTypeOptions(result.selectConType || []);
        setScreenTypeOptions(result.selectScreenType || []);
        setPlayTypeOptions(result.selectPlayType || []);
        setNextSeqOptions(result.selectNextSeq || []);
        const obj = result.regist;
        if (obj) {
            setEditForm({
                mode: 'Edt',
                conSeq: obj.conSeq || '',
                conNm: obj.conNm || '',
                conScreen: obj.conScreen || '',
                conType: obj.conType || '',
                conUseYn: obj.conUseYn || 'Y',
                conWidth: obj.conWidth || '',
                conHeight: obj.conHeight || '',
                conMid: obj.conMid || '',
                conNextSeq: obj.conNextSeq || '',
                conPlayType: obj.conPlayType || '',
            });
            setEditModalOpen(true);
        }
    }, [conSeq]);

    const handleEditSubmit = useCallback(async () => {
        if (!editForm.conNm) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '화면명을 입력해 주세요.' });
            return;
        }
        const ok = await Swal.fire({
            icon: 'question', title: '화면 구성 수정',
            html: `<b>${editForm.conNm}</b> 수정 하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const res = await fnAjaxFetch({ url: URL.CON_MUTI_UPDATE, method: 'POST', data: editForm });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || '수정되었습니다.' });
            setEditModalOpen(false);
            const detail = await loadContentInfo();
            if (detail?.conScreen) await loadPages(detail.conScreen);
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || '수정 중 오류가 발생했습니다.' });
        }
    }, [editForm, loadContentInfo, loadPages]);

    // 레거시 conMutiView_back.jsp의 preView()/sendSchedule() 참고.
    const checkTimeIntervalReady = useCallback(async () => {
        const res = await fnAjaxFetch({
            url: URL.CON_DETAIL_PREVIEW_CHECK, method: 'GET', param: { conSeq }, showLoading: false,
        });
        return res?.data?.resultCodeInfo === 'SUCCESS';
    }, [conSeq]);

    const handlePreview = useCallback(async () => {
        const ready = await checkTimeIntervalReady();
        if (!ready) {
            await Swal.fire({ icon: 'warning', title: '입력 확인', text: '송출시간을 입력하지 않은 콘텐츠가 있습니다.' });
            return;
        }
        const res = await fnAjaxFetch({
            url: URL.CON_DETAIL_CONTENT_PREVIEW, method: 'GET', param: { conSeq },
        });
        const fileUrl = res?.data?.result?.fileUrl;
        if (res?.data?.resultCodeInfo !== 'SUCCESS' || !fileUrl) {
            await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || '화면 생성 중 오류가 발생했습니다.' });
            return;
        }
        const url = `${config.REACT_APP_IMG_URL}${fileUrl}`;
        window.open(url, '화면 미리보기', `width=${contentInfo?.conWidth},height=${contentInfo?.conHeight},top=50,left=50,scrollbars=auto`);
    }, [checkTimeIntervalReady, conSeq, contentInfo]);

    const handleSendSchedule = useCallback(async () => {
        const ready = await checkTimeIntervalReady();
        if (!ready) {
            await Swal.fire({ icon: 'warning', title: '입력 확인', text: '이미지 중 타임값을 넣지 않은 콘텐츠가 있습니다.' });
            return;
        }
        const res = await fnAjaxFetch({
            url: URL.CON_DETAIL_SCHEDULE_SEND, method: 'GET', param: { conSeq },
        });
        if (res?.data?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: '스케줄이 정상적으로 등록되었습니다.' });
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || '스케줄 등록 도중 문제가 발생하였습니다.' });
        }
    }, [checkTimeIntervalReady, conSeq]);

    const handleDeleteContent = useCallback(async () => {
        if (Number(contentInfo?.schCnt) > 0) {
            await Swal.fire({
                icon: 'warning', title: '삭제 불가',
                text: '송출 중이거나 예정인 연결된 스케줄이 존재합니다. 스케줄을 먼저 삭제/연결 해제해 주세요.',
            });
            return;
        }
        const result = await Swal.fire({
            icon: 'question', title: '화면 구성 삭제', text: '본 콘텐츠를 삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        const res = await fnAjaxFetch({ url: `${URL.CON_MUTI_INFO}/${conSeq}.do`, method: 'DELETE' });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            navigate('/backoffice/sub/conManage/muti');
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || '삭제 중 오류가 발생했습니다.' });
        }
    }, [conSeq, contentInfo, navigate]);

    const searchResultToRender = useMemo(() => searchResult, [searchResult]);

    if (!conSeq) {
        return <div style={{ padding: 16 }}>conSeq가 없습니다. 콘텐츠 목록에서 "편성" 버튼으로 진입해 주세요.</div>;
    }

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">화면 상세 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">콘텐츠 관리</li>
                        <li className="breadcrumb-item">화면 구성 관리</li>
                        <li className="breadcrumb-item">편성</li>
                    </ol>
                </div>
            </div>

            <div className="col-12" style={{ padding: '12px 15px', borderBottom: '1px solid #dde2eb' }}>
                <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse' }}>
                    <tbody>
                        <tr>
                            <th style={{ width: 90, textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>화면명</th>
                            <td style={{ padding: '4px 8px' }}>{contentInfo?.conNm}</td>
                            <th style={{ width: 90, textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>화면타입</th>
                            <td style={{ padding: '4px 8px' }}>{contentInfo?.codeNm}</td>
                            <th style={{ width: 90, textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>등록일</th>
                            <td style={{ padding: '4px 8px' }}>{contentInfo?.frstRegistPnttm}</td>
                            <th style={{ width: 90, textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>이용스케줄</th>
                            <td style={{ padding: '4px 8px' }}>{contentInfo?.schCnt ?? 0}개</td>
                        </tr>
                        <tr>
                            <th style={{ textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>총 송출 시간</th>
                            <td style={{ padding: '4px 8px' }}>{contentInfo?.conTime ?? 0}초</td>
                            <th style={{ textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>사용여부</th>
                            <td style={{ padding: '4px 8px' }}>{contentInfo?.conUseYn === 'Y' ? '사용' : '사용 안함'}</td>
                            <th style={{ textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>화면 사이즈</th>
                            <td style={{ padding: '4px 8px' }}>
                                가로: {contentInfo?.conWidth} 세로: {contentInfo?.conHeight} 분할: {contentInfo?.conMid}
                            </td>
                            <th style={{ textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>연결콘텐츠</th>
                            <td style={{ padding: '4px 8px' }}>
                                {contentInfo?.conNextTitle === 'NO DATA' ? '연결 정보 없음' : contentInfo?.conNextTitle}
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div className="col-12" style={{ padding: '8px 15px', display: 'flex', alignItems: 'center', gap: 12,
                borderBottom: '1px solid #dde2eb' }}>
                <span style={{ color: '#64748b' }}>페이지 구분</span>
                <select value={selectedDetailSeq} onChange={(e) => setSelectedDetailSeq(e.target.value)} style={{ minWidth: 140 }}>
                    {pages.map((p) => (
                        <option key={p.detailSeq} value={p.detailSeq}>{Number(p.detailOrder) + 1}페이지</option>
                    ))}
                    {pages.length === 0 && <option value="">페이지 없음</option>}
                </select>
                <span style={{ color: '#64748b' }}>이 페이지 재생시간(초): {sumTime}{loading ? ' (조회 중...)' : ''}</span>
                <div style={{ marginLeft: 'auto', display: 'flex', gap: 6 }} className="col-auto content-search__action">
                    <button type="button" className="btn btn-outline-dark btn-outline__gray"
                        onClick={handlePreview}>화면생성</button>
                    <button type="button" className="btn btn-outline-dark btn-outline__gray"
                        onClick={handleSendSchedule}>스케줄전송</button>
                    <button type="button" className="btn btn-outline-dark btn-outline__gray"
                        onClick={() => navigate('/backoffice/sub/conManage/muti')}>목록</button>
                    <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                        onClick={handleOpenEditModal}>수정</button>
                    <button type="button" className="btn btn-outline-danger btn-outline__gray"
                        onClick={handleDeleteContent}>삭제</button>
                </div>
            </div>

            <div className="col-12 content-table content-table__main" style={{ display: 'flex', gap: 16, alignItems: 'flex-start' }}>
                <div style={{ flex: 1.7 }}>
                    <h3 style={{ fontSize: 14, margin: '4px 0 8px' }}>배치된 파일</h3>
                    <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: 13 }}>
                        <thead>
                            <tr style={{ background: '#f8fafc' }}>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>순번</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>썸네일</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>파일명</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>송출시간</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>관리</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>상세정보</th>
                            </tr>
                        </thead>
                        <tbody>
                            {fileList.map((f, idx) => (
                                <tr key={f.fileSeq}
                                    draggable
                                    onDragStart={() => setDragIndex(idx)}
                                    onDragOver={(e) => e.preventDefault()}
                                    onDrop={() => { handleDropReorder(dragIndex, idx); setDragIndex(null); }}
                                    onDragEnd={() => setDragIndex(null)}
                                    style={{ background: dragIndex === idx ? '#eff6ff' : undefined, cursor: 'grab' }}
                                >
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6, textAlign: 'center' }}>
                                        <span style={{ color: '#94a3b8', marginRight: 6 }} title="드래그해서 순서 변경">⠿</span>
                                        {f.fileOrder}
                                    </td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6, textAlign: 'center' }}>
                                        <Thumb file={f} />
                                    </td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6, wordBreak: 'break-all' }}>{f.orignlFileNm}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>
                                        <input
                                            type="text"
                                            value={timeInputs[f.fileSeq] ?? ''}
                                            readOnly={f.mediaType !== 'IMAGE'}
                                            onChange={(e) => setTimeInputs((prev) => ({ ...prev, [f.fileSeq]: e.target.value }))}
                                            style={{ width: 50, textAlign: 'center' }}
                                        /> 초{' '}
                                        <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                                            onClick={() => handleTimeApply(f.fileSeq)}>적용</button>
                                    </td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6, textAlign: 'center' }}>
                                        <button type="button" className="btn btn-outline-danger btn-outline__gray btn-sm"
                                            onClick={() => handleRemoveFile(f.fileSeq)}>제외</button>
                                    </td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6, textAlign: 'center', whiteSpace: 'nowrap' }}>
                                        {f.fileWidth}x{f.fileHeight}
                                    </td>
                                </tr>
                            ))}
                            {fileList.length === 0 && (
                                <tr><td colSpan={6} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>배치된 파일이 없습니다.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>

                <div style={{ flex: 0.9, borderLeft: '1px solid #e2e8f0', paddingLeft: 16 }}>
                    <h3 style={{ fontSize: 14, margin: '4px 0 8px' }}>미디어 리스트</h3>
                    <form onSubmit={handleFileSearch} style={{ display: 'flex', flexWrap: 'nowrap', alignItems: 'center', gap: 8, marginBottom: 10 }}>
                        <div className="content-search__option" style={{ flexWrap: 'nowrap' }}>
                            <select value={mediaType} onChange={(e) => setMediaType(e.target.value)} style={{ minWidth: 100 }}>
                                <option value="">콘텐츠전체</option>
                                <option value="IMAGE">이미지</option>
                                <option value="MEDIA">동영상</option>
                                <option value="MUSIC">음원POP</option>
                            </select>
                            <input type="text" placeholder="콘텐츠명" value={searchKeyword}
                                onChange={(e) => setSearchKeyword(e.target.value)} style={{ width: 140 }} />
                        </div>
                        <div className="content-search__action">
                            <button type="submit" className="btn btn-outline-dark btn-outline__gray">조회</button>
                        </div>
                    </form>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 8, maxHeight: 640, overflowY: 'auto' }}>
                        {searchResultToRender.map((f) => (
                            <div key={f.atchFileId} style={{
                                display: 'flex', alignItems: 'center', gap: 10,
                                border: '1px solid #e2e8f0', borderRadius: 6, padding: 8, fontSize: 12,
                            }}>
                                <Thumb file={f} size={60} />
                                <div style={{ flex: 1, wordBreak: 'break-all' }}>{f.orignlFileNm}</div>
                                <button type="button" className="btn btn-primary btn-default__blue btn-sm"
                                    onClick={() => handleAddFile(f)}>추가</button>
                            </div>
                        ))}
                        {searchResultToRender.length === 0 && <div style={{ color: '#94a3b8', gridColumn: '1 / -1' }}>검색 결과가 없습니다.</div>}
                    </div>
                    {mediaTotalCnt > 0 && (
                        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 8, marginTop: 10 }}>
                            <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                                onClick={() => loadMediaSearch(mediaPageIndex - 1)} disabled={mediaPageIndex <= 1}>‹ 이전</button>
                            <span style={{ fontSize: 13, color: '#64748b' }}>{mediaPageIndex} / {mediaTotalPages} 페이지 (총 {mediaTotalCnt}건)</span>
                            <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                                onClick={() => loadMediaSearch(mediaPageIndex + 1)} disabled={mediaPageIndex >= mediaTotalPages}>다음 ›</button>
                        </div>
                    )}
                </div>
            </div>

            <Suspense fallback={null}>
                {editModalOpen && (
                    <ContentMutiFormModal
                        open={editModalOpen}
                        form={editForm}
                        setForm={setEditForm}
                        conTypeOptions={conTypeOptions}
                        screenTypeOptions={screenTypeOptions}
                        playTypeOptions={playTypeOptions}
                        nextSeqOptions={nextSeqOptions}
                        onClose={() => setEditModalOpen(false)}
                        onSubmit={handleEditSubmit}
                    />
                )}
            </Suspense>
        </div>
    );
}
