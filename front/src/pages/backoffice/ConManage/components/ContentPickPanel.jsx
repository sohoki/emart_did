import { useCallback, useEffect, useState } from 'react';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const PAGE_UNIT = 10;

// 화면(콘텐츠) 1페이지에 파일 1건 추가 — 편성 화면(ContentDetailEditorPage)의 handleAddFile과 같은 규칙.
// 목록 API의 conScreen은 코드명이라 상세 조회로 코드값을 받아 1페이지(detailSeq)를 찾는다.
// 반환값: 'ADDED' | 'DUPLICATE'(이미 배치됨) | 'NO_PAGE'(페이지 없음)
const addFileToFirstPage = async (conSeq, file) => {
    const infoRes = await fnAjaxFetch({ url: `${URL.CON_MUTI_INFO}/${conSeq}.do`, method: 'GET', showLoading: false });
    const conScreen = infoRes?.data?.result?.result?.conScreen;

    const pageRes = await fnAjaxFetch({
        url: URL.CON_MUTI_DETAIL_COMBO, method: 'GET',
        param: { code: conScreen, conSeq }, showLoading: false,
    });
    const detailSeq = pageRes?.data?.result?.resultList?.[0]?.detailSeq;
    if (!detailSeq) return 'NO_PAGE';

    const fileRes = await fnAjaxFetch({
        url: URL.CON_DETAIL_FILE_LIST, method: 'GET',
        param: { conSeq, detailSeq }, showLoading: false,
    });
    const fileList = fileRes?.data?.result?.resultList ?? [];
    if (fileList.some((f) => f.atchFileId === file.atchFileId)) return 'DUPLICATE';

    // 이미지는 기본 10초, 동영상/음원은 파일 재생시간을 송출시간으로 사용
    const timeInterval = file.mediaType === 'IMAGE' ? '10' : (file.playTime || '');
    await fnAjaxFetch({
        url: URL.CON_DETAIL_FILE_INSERT, method: 'POST', showLoading: false,
        data: {
            conSeq, detailSeq, atchFileId: file.atchFileId, fileOrder: String(fileList.length + 1),
            ...(timeInterval ? { timeInterval } : {}),
        },
    });
    return 'ADDED';
};

// 파일 상세정보 모달 우측 — 콘텐츠(화면 구성) 검색 후 체크한 화면들의 1페이지에 이 파일을 추가한다.
const ContentPickPanel = ({ file }) => {
    const [keyword, setKeyword] = useState('');
    const [list, setList] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [pageIndex, setPageIndex] = useState(1);
    const [checked, setChecked] = useState({});   // { conSeq: conNm }
    const [registering, setRegistering] = useState(false);

    const loadList = useCallback(async (page = 1, searchKeyword = '') => {
        const res = await fnAjaxFetch({
            url: URL.CON_MUTI_LIST, method: 'POST', showLoading: false,
            data: {
                searchCondition: searchKeyword ? 'conNm' : '',
                searchKeyword,
                pageIndex: String(page),
                pageUnit: String(PAGE_UNIT),
            },
        });
        setList(res?.data?.result?.resultList ?? []);
        setTotalCnt(res?.data?.result?.totalCnt ?? 0);
        setPageIndex(page);
    }, []);

    useEffect(() => {
        (async () => {
            await loadList(1, '');
        })();
    }, [loadList]);

    const handleSearch = (e) => {
        e.preventDefault();
        loadList(1, keyword);
    };

    const toggleCheck = (row) => {
        setChecked((prev) => {
            const next = { ...prev };
            if (next[row.conSeq]) delete next[row.conSeq];
            else next[row.conSeq] = row.conNm;
            return next;
        });
    };

    const handleRegister = async () => {
        const targets = Object.entries(checked);
        if (targets.length === 0) {
            await Swal.fire({ icon: 'warning', title: '선택 필요', text: '파일을 추가할 콘텐츠를 체크해 주세요.' });
            return;
        }
        const confirm = await Swal.fire({
            icon: 'question', title: '콘텐츠에 추가',
            text: `선택한 ${targets.length}개 콘텐츠의 1페이지에 이 파일을 추가하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!confirm.isConfirmed) return;

        setRegistering(true);
        const result = { ADDED: [], DUPLICATE: [], NO_PAGE: [], FAIL: [] };
        try {
            for (const [conSeq, conNm] of targets) {
                try {
                    result[await addFileToFirstPage(conSeq, file)].push(conNm);
                } catch {
                    result.FAIL.push(conNm);
                }
            }
        } finally {
            setRegistering(false);
        }

        setChecked({});
        const lines = [
            `추가 완료: ${result.ADDED.length}건`,
            result.DUPLICATE.length ? `이미 배치됨(건너뜀): ${result.DUPLICATE.join(', ')}` : '',
            result.NO_PAGE.length ? `페이지 없음: ${result.NO_PAGE.join(', ')}` : '',
            result.FAIL.length ? `실패: ${result.FAIL.join(', ')}` : '',
        ].filter(Boolean);
        await Swal.fire({
            icon: result.FAIL.length ? 'warning' : 'success', title: '콘텐츠 추가 결과',
            html: lines.join('<br>'),
        });
    };

    const totalPages = Math.max(1, Math.ceil(totalCnt / PAGE_UNIT));
    const checkedCnt = Object.keys(checked).length;

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
            <h3 style={{ fontSize: 14, margin: '0 0 8px' }}>콘텐츠 검색</h3>
            <form onSubmit={handleSearch} style={{ display: 'flex', gap: 6, marginBottom: 8 }}>
                <input type="text" className="form-control" placeholder="화면명" value={keyword}
                    onChange={(e) => setKeyword(e.target.value)} />
                <button type="submit" className="btn btn-outline-dark btn-outline__gray" style={{ whiteSpace: 'nowrap' }}>검색</button>
            </form>

            <ul style={{ listStyle: 'none', padding: 0, margin: 0, flex: 1, overflowY: 'auto', maxHeight: 360,
                border: '1px solid #cbd5e1', borderRadius: 6 }}>
                {list.map((row) => (
                    <li key={row.conSeq}>
                        <label style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 10px', margin: 0,
                            fontSize: 13, cursor: 'pointer', borderBottom: '1px solid #e2e8f0',
                            background: checked[row.conSeq] ? '#eff6ff' : undefined }}>
                            <input type="checkbox" checked={!!checked[row.conSeq]} onChange={() => toggleCheck(row)} />
                            <span style={{ flex: 1, wordBreak: 'break-all' }}>{row.conNm}</span>
                            <span style={{ color: '#64748b', whiteSpace: 'nowrap', fontSize: 12 }}>{row.conScreen}</span>
                        </label>
                    </li>
                ))}
                {list.length === 0 && <li style={{ padding: 16, color: '#94a3b8', textAlign: 'center' }}>검색 결과가 없습니다.</li>}
            </ul>

            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 8, margin: '8px 0' }}>
                <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                    onClick={() => loadList(pageIndex - 1, keyword)} disabled={pageIndex <= 1}>‹</button>
                <span style={{ fontSize: 12, color: '#64748b' }}>{pageIndex} / {totalPages} (총 {totalCnt}건)</span>
                <button type="button" className="btn btn-outline-secondary btn-outline__gray btn-sm"
                    onClick={() => loadList(pageIndex + 1, keyword)} disabled={pageIndex >= totalPages}>›</button>
            </div>

            <button type="button" className="btn btn-primary btn-default__blue"
                onClick={handleRegister} disabled={registering || checkedCnt === 0}>
                {registering ? '추가 중...' : `체크한 콘텐츠에 추가 (${checkedCnt})`}
            </button>
        </div>
    );
};

export default ContentPickPanel;
