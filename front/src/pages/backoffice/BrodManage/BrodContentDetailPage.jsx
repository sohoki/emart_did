import { useCallback, useEffect, useMemo, useState, Suspense, lazy } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const BrodContentFormModal = lazy(() => import('./components/BrodContentFormModal.jsx'));
const BrodAnniversaryFormModal = lazy(() => import('./components/BrodAnniversaryFormModal.jsx'));
const BrodContentDetailFormModal = lazy(() => import('./components/BrodContentDetailFormModal.jsx'));
const BrodContentCopyModal = lazy(() => import('./components/BrodContentCopyModal.jsx'));

const EMPTY_BROD_FORM = { mode: 'Ins', basicCode: '', brodCode: '', brodName: '', brodInterval: '', basicBrodCode: '', brodUseYn: 'Y' };
const EMPTY_ANNIVER_FORM = {
    mode: 'Ins', brodAnnSeq: '', brodCode: '', atchFileId: '', anniverName: '', anniversaryGubun: '',
    anniverStartDay: '', anniverEndDay: '', anniversaryTime: '', anniverOrder: '1',
};
const EMPTY_DETAIL_FORM = {
    mode: 'Ins', brodSeq: '', atchFileId: '',
    timeCode: '', // 수정(Edt) 모드 — 단일 시간대
    startTimeCode: '000', intervalMinutes: '010', insertCount: '1', // 등록(Ins) 모드 — 운영스케줄(자동 배정)
    contentOrder: '1',
    contentStartDay: '', contentEndDay: '',
};

const secToMinSec = (totalSec) => {
    const s = Number(totalSec) || 0;
    const min = Math.floor(s / 60);
    const sec = s - min * 60;
    return `${min}:${String(sec).padStart(2, '0')}`;
};

// 방송(음원) 콘텐츠 상세(편성) 화면 — 레거시 brodContentView.jsp 참고. 시간대별 음원
// 배치, 특정방송(기념일), 배치 적용, 음원 콘텐츠 복사를 이 화면 하나에서 다룬다.
// 레거시의 "편성표생성"/"방송표보기"는 백엔드 이관 과정에서 의도적으로 제외된
// 기능(UniSelectInfoManageService 미존재/FN_CENTERBRODINFO 미존재로 인한 SQL
// 인젝션·오류 위험)이라 이 화면에서도 준비 중 안내로만 남겨둔다.
export default function BrodContentDetailPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const brodCode = searchParams.get('brodCode') ?? '';

    const [detail, setDetail] = useState(null);
    const [anniverList, setAnniverList] = useState([]);
    const [slotContents, setSlotContents] = useState({}); // { [intervalSection]: BrodContentDetailVO[] }
    const [loading, setLoading] = useState(false);

    const [brodModalOpen, setBrodModalOpen] = useState(false);
    const [brodForm, setBrodForm] = useState(EMPTY_BROD_FORM);
    const [intervalOptions, setIntervalOptions] = useState([]);
    const [basicOptions, setBasicOptions] = useState([]);

    const [anniverModalOpen, setAnniverModalOpen] = useState(false);
    const [anniverForm, setAnniverForm] = useState(EMPTY_ANNIVER_FORM);
    const [gubunOptions, setGubunOptions] = useState([]);

    const [detailModalOpen, setDetailModalOpen] = useState(false);
    const [detailForm, setDetailForm] = useState(EMPTY_DETAIL_FORM);
    const [timeOptions, setTimeOptions] = useState([]);
    const [fileOptions, setFileOptions] = useState([]);

    const [copyModalOpen, setCopyModalOpen] = useState(false);
    const [copyCombo, setCopyCombo] = useState([]);

    // 재생간격(codeDc, 분)을 10분 단위 슬롯으로 나눈다 — 60분 간격이면 00/10/20/30/40/50분 6칸.
    const slots = useMemo(() => {
        const cnt = Math.floor((Number(detail?.codeDc) || 0) / 10);
        return Array.from({ length: cnt }, (_, i) => (i === 0 ? '0' : `${i}0`));
    }, [detail?.codeDc]);

    const loadSlot = useCallback(async (intervalSection) => {
        const res = await fnAjaxFetch({
            url: URL.BROD_CONTENT_TIME_LIST, method: 'GET',
            param: { brodCode, timeInterval: intervalSection }, showLoading: false,
        });
        return res?.data?.result?.resultList ?? [];
    }, [brodCode]);

    const loadAllSlots = useCallback(async (slotList) => {
        const entries = await Promise.all(slotList.map(async (s) => [s, await loadSlot(s)]));
        setSlotContents(Object.fromEntries(entries));
    }, [loadSlot]);

    const loadDetail = useCallback(async () => {
        if (!brodCode) return;
        setLoading(true);
        try {
            const res = await fnAjaxFetch({ url: `${URL.BROD_CONTENT_VIEW}/${brodCode}.do`, method: 'GET' });
            const d = res?.data?.result?.regist ?? null;
            setDetail(d);
            setAnniverList(res?.data?.result?.brodAnniver ?? []);
            const slotList = Array.from(
                { length: Math.floor((Number(d?.codeDc) || 0) / 10) },
                (_, i) => (i === 0 ? '0' : `${i}0`),
            );
            if (slotList.length > 0) await loadAllSlots(slotList);
        } finally {
            setLoading(false);
        }
    }, [brodCode, loadAllSlots]);

    useEffect(() => { loadDetail(); }, [loadDetail]);

    // ===== 콘텐츠(방송) 정보 수정 =====
    const handleOpenBrodModal = useCallback(async () => {
        const res = await fnAjaxFetch({
            url: URL.BROD_CONTENT_FORM_DATA, method: 'GET', param: { mode: 'Edt', brodCode },
        });
        setIntervalOptions(res?.data?.result?.brodInterval ?? []);
        setBasicOptions(res?.data?.result?.basicInfo ?? []);
        const obj = res?.data?.result?.regist;
        if (!obj) return;
        setBrodForm({
            mode: 'Edt', brodCode: obj.brodCode || '', brodName: obj.brodName || '',
            brodInterval: obj.brodInterval || obj.code || '', basicBrodCode: obj.basicBrodCode || '',
            brodUseYn: obj.brodUseYn || 'Y',
        });
        setBrodModalOpen(true);
    }, [brodCode]);

    const handleBrodSubmit = useCallback(async () => {
        const res = await fnAjaxFetch({ url: URL.BROD_CONTENT_UPDATE, method: 'POST', data: brodForm });
        if (res?.data?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: '수정되었습니다.' });
            setBrodModalOpen(false);
            loadDetail();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || '수정 중 오류가 발생했습니다.' });
        }
    }, [brodForm, loadDetail]);

    // ===== 기본음원 리스트 미리보기 =====
    const handlePreviewBasic = useCallback(async () => {
        const basicCode = detail?.basicFileId;
        if (!basicCode || basicCode === 'FILE_000000000000001') {
            await Swal.fire({ icon: 'info', title: '기본음원 없음', text: '연동된 기초 방송이 없습니다.' });
            return;
        }
        const res = await fnAjaxFetch({ url: URL.BASIC_BROD_FILE_LIST, method: 'GET', param: { basicCode } });
        const list = res?.data?.result?.resultList ?? [];
        const html = list.length > 0
            ? `<table style="width:100%;font-size:13px;"><tbody>${list.map((f, i) => `<tr><td style="padding:4px;">${i + 1}</td><td style="padding:4px;text-align:left;">${f.orignlFileNm}</td><td style="padding:4px;">${f.basicOrder ?? ''}</td></tr>`).join('')}</tbody></table>`
            : '<div style="color:#94a3b8;">등록된 음원이 없습니다.</div>';
        await Swal.fire({ icon: 'info', title: '기본 음원 리스트 (랜덤재생)', html });
    }, [detail?.basicFileId]);

    // ===== 시간대별 편성(콘텐츠) 등록/수정/삭제 =====
    const handleOpenDetailModal = useCallback(async (item) => {
        const res = await fnAjaxFetch({
            url: URL.BROD_CONTENT_DETAIL_FORM, method: 'GET',
            param: { brodCode, brodSeq: item?.brodSeq ?? '', mode: item ? 'Edt' : 'Ins' },
        });
        setTimeOptions(res?.data?.result?.timeInfo ?? []);
        setFileOptions(res?.data?.result?.fileInfo ?? []);
        if (item) {
            const obj = res?.data?.result?.regist ?? item;
            setDetailForm({
                ...EMPTY_DETAIL_FORM,
                mode: 'Edt',
                brodSeq: obj.brodSeq || item.brodSeq || '',
                atchFileId: obj.atchFileId || '',
                timeCode: String(Number(obj.intervalSection ?? item.intervalSection ?? 0)).padStart(3, '0'),
                contentOrder: obj.contentOrder || item.contentOrder || '1',
                contentStartDay: obj.contentStartDay || '',
                contentEndDay: obj.contentEndDay || '',
            });
        } else {
            setDetailForm(EMPTY_DETAIL_FORM);
        }
        setDetailModalOpen(true);
    }, [brodCode]);

    // 등록(Ins) 화면의 "콘텐츠명" 검색 — contentDetail/fileSearch.do로 음원 파일을 검색해서
    // 선택 콤보를 갱신한다.
    const handleFileSearch = useCallback(async (keyword) => {
        const res = await fnAjaxFetch({
            url: URL.BROD_CONTENT_DETAIL_FILE_SEARCH, method: 'GET',
            param: { orgFileNm: keyword ?? '' }, showLoading: false,
        });
        setFileOptions(res?.data?.result?.resultList ?? []);
    }, []);

    const handleDetailSubmit = useCallback(async () => {
        const isInsert = detailForm.mode === 'Ins';

        if (isInsert) {
            // 1) 자동 시간대 배정 — "N분부터 M분 간격으로 K번 입력"을 겹치지 않는 시간대로 확정한다.
            const checkRes = await fnAjaxFetch({
                url: URL.BROD_CONTENT_DETAIL_TIME_CHECK, method: 'POST',
                data: {
                    brodCode,
                    timeInterval: detailForm.startTimeCode,
                    timeIntervalInsertCnt: detailForm.insertCount,
                    contentInsertInterval: detailForm.intervalMinutes,
                    contentStartDay: detailForm.contentStartDay,
                    contentEndDay: detailForm.contentEndDay,
                    atchFileId: detailForm.atchFileId,
                },
            });
            const timeIntervalResult = checkRes?.data?.result?.result || '';
            if (!timeIntervalResult) {
                await Swal.fire({ icon: 'warning', title: '배정 불가', text: '운영스케줄에 맞는 빈 시간대를 찾지 못했습니다. 시작시간/간격/횟수를 조정해 주세요.' });
                return;
            }

            const res = await fnAjaxFetch({
                url: URL.BROD_CONTENT_DETAIL_UPDATE, method: 'POST',
                data: {
                    mode: 'Ins', brodCode,
                    atchFileId: detailForm.atchFileId,
                    contentStartDay: detailForm.contentStartDay,
                    contentEndDay: detailForm.contentEndDay,
                    contentOrder: detailForm.contentOrder,
                    timeIntervalResult,
                },
            });
            if (res?.data?.resultCodeInfo === 'SUCCESS') {
                await Swal.fire({ icon: 'success', title: '완료', text: '등록되었습니다.' });
                setDetailModalOpen(false);
                loadDetail();
            } else {
                await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || '등록 중 오류가 발생했습니다.' });
            }
            return;
        }

        // 수정(Edt) — 확정된 편성 1건의 시간대/정렬순서/운영날짜를 그대로 업데이트한다.
        const res = await fnAjaxFetch({
            url: URL.BROD_CONTENT_DETAIL_UPDATE, method: 'POST',
            data: {
                mode: 'Edt', brodCode,
                brodSeq: detailForm.brodSeq,
                atchFileId: detailForm.atchFileId,
                contentStartDay: detailForm.contentStartDay,
                contentEndDay: detailForm.contentEndDay,
                contentOrder: detailForm.contentOrder,
                intervalSection: detailForm.timeCode,
            },
        });
        if (res?.data?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: '수정되었습니다.' });
            setDetailModalOpen(false);
            loadDetail();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || '수정 중 오류가 발생했습니다.' });
        }
    }, [detailForm, brodCode, loadDetail]);

    const handleDeleteDetail = useCallback(async (brodSeq) => {
        const result = await Swal.fire({
            icon: 'question', title: '편성 삭제', text: '이 편성을 삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        await fnAjaxFetch({
            url: `${URL.BROD_CONTENT_DETAIL_INFO}/${brodSeq}.do?brodCode=${encodeURIComponent(brodCode)}`,
            method: 'DELETE',
        });
        loadDetail();
    }, [brodCode, loadDetail]);

    // ===== 특정방송(기념일) 등록/수정/삭제 =====
    const handleOpenAnniverModal = useCallback(async (brodAnnSeq) => {
        // 특정방송여부 콤보(EMT020)는 content/copyPopupData.do가 함께 내려준다(atchFileId는
        // 파일명 표시용이라 없어도 되지만 required 파라미터라 빈 문자열로 넘긴다).
        const popupRes = await fnAjaxFetch({
            url: URL.BROD_CONTENT_REG_POPUP_DATA, method: 'GET', param: { atchFileId: '' }, showLoading: false,
        });
        setGubunOptions(popupRes?.data?.result?.anniversaryGubun ?? []);

        if (!brodAnnSeq) {
            setAnniverForm({ ...EMPTY_ANNIVER_FORM, brodCode });
            setAnniverModalOpen(true);
            return;
        }
        const res = await fnAjaxFetch({ url: URL.BROD_ANNIVER_DETAIL, method: 'POST', data: { brodAnnSeq } });
        const obj = res?.data?.result?.result;
        if (!obj) {
            await Swal.fire({ icon: 'error', title: '조회 실패', text: res?.data?.resultMessage || '특정방송 상세 조회에 실패했습니다.' });
            return;
        }
        setAnniverForm({ ...EMPTY_ANNIVER_FORM, ...obj, mode: 'Edt' });
        setAnniverModalOpen(true);
    }, [brodCode]);

    const handleAnniverSubmit = useCallback(async () => {
        const isInsert = anniverForm.mode === 'Ins';
        const res = await fnAjaxFetch({ url: URL.BROD_ANNIVER_UPDATE, method: 'POST', data: anniverForm });
        if (res?.data?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: `${isInsert ? '등록' : '수정'}되었습니다.` });
            setAnniverModalOpen(false);
            loadDetail();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || `${isInsert ? '등록' : '수정'} 중 오류가 발생했습니다.` });
        }
    }, [anniverForm, loadDetail]);

    const handleDeleteAnniver = useCallback(async (brodAnnSeq) => {
        const result = await Swal.fire({
            icon: 'question', title: '특정방송 삭제', text: '삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        await fnAjaxFetch({ url: `${URL.BROD_ANNIVER_DELETE}/${brodAnnSeq}.do`, method: 'DELETE' });
        loadDetail();
    }, [loadDetail]);

    // ===== 음원 콘텐츠 복사 =====
    const handleOpenCopyModal = useCallback(async () => {
        const res = await fnAjaxFetch({ url: `${URL.BROD_CONTENT_DETAIL_COPY_COMBO}/${brodCode}.do`, method: 'GET' });
        setCopyCombo(res?.data?.result?.resultList ?? []);
        setCopyModalOpen(true);
    }, [brodCode]);

    const handleCopySubmit = useCallback(async ({ prebrodCode, contentStartDay, contentEndDay }) => {
        const res = await fnAjaxFetch({
            url: URL.BROD_CONTENT_DETAIL_COPY_INSERT, method: 'POST',
            data: { brodCode, prebrodCode, contentStartDay, contentEndDay },
        });
        if (res?.data?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: '복사되었습니다.' });
            setCopyModalOpen(false);
            loadDetail();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || '복사 중 오류가 발생했습니다.' });
        }
    }, [brodCode, loadDetail]);

    // ===== 배치 적용 =====
    const handleScheduleConfirm = useCallback(async () => {
        const res = await fnAjaxFetch({
            url: `${URL.BROD_CONTENT_SCHEDULE_CONFIRM}?brodCode=${encodeURIComponent(brodCode)}`, method: 'POST',
        });
        const applied = res?.data?.result?.result;
        await Swal.fire({
            icon: applied ? 'success' : 'warning', title: '배치 적용',
            text: applied ? '배포 현황에 변경 내역이 정상적으로 저장되었습니다.' : '배포 현황에 반영할 변경 내역(연결된 지점)이 없습니다.',
        });
    }, [brodCode]);

    const handleNotReady = useCallback((label) => {
        Swal.fire({ icon: 'info', title: label, text: '준비 중인 기능입니다.' });
    }, []);

    if (!brodCode) {
        return <div style={{ padding: 16 }}>brodCode가 없습니다. 방송(음원) 콘텐츠 관리 목록에서 방송명 링크로 진입해 주세요.</div>;
    }

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">방송(음원) 콘텐츠 상세</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">방송 관리</li>
                        <li className="breadcrumb-item">방송(음원) 콘텐츠 관리</li>
                        <li className="breadcrumb-item">상세</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search__action" style={{ padding: '0 0 12px', flexWrap: 'wrap', display: 'flex', gap: 8 }}>
                <button type="button" className="btn btn-outline-dark btn-outline__gray"
                    onClick={() => navigate('/backoffice/sub/brodManage/content')}>목록</button>
                <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                    onClick={handleOpenBrodModal}>수정</button>
                <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                    onClick={() => handleOpenDetailModal(null)}>콘텐츠 등록</button>
                <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                    onClick={() => handleOpenAnniverModal(null)}>특정 방송 등록</button>
                <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                    onClick={handleOpenCopyModal}>음원 콘텐츠 복사</button>
                <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                    onClick={handleScheduleConfirm}>배치 적용</button>
                <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                    onClick={() => handleNotReady('편성표생성')}>편성표생성</button>
                <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                    onClick={() => handleNotReady('방송표보기')}>방송표보기</button>
            </div>

            <div className="col-12" style={{ padding: '12px 15px', borderBottom: '1px solid #dde2eb' }}>
                {!detail ? (
                    <div style={{ padding: 16, color: '#94a3b8' }}>{loading ? '조회 중...' : '데이터가 없습니다.'}</div>
                ) : (
                    <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse' }}>
                        <tbody>
                            <tr>
                                <th style={{ width: 100, textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>콘텐츠코드</th>
                                <td style={{ padding: '4px 8px' }}>{detail.brodCode}</td>
                                <th style={{ width: 100, textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>콘텐츠명</th>
                                <td style={{ padding: '4px 8px' }}>{detail.brodName}</td>
                                <th style={{ width: 100, textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>이벤트유무</th>
                                <td style={{ padding: '4px 8px' }}>{detail.secGubun === 'SECGUBUN01' ? '일반스케줄' : '이벤트스케줄'}</td>
                            </tr>
                            <tr>
                                <th style={{ textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>사용유무</th>
                                <td style={{ padding: '4px 8px' }}>{detail.brodUseYn === 'Y' ? '사용' : '사용 안함'}</td>
                                <th style={{ textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>반복재생간격</th>
                                <td style={{ padding: '4px 8px' }}>{detail.codeNm}</td>
                                <th style={{ textAlign: 'left', color: '#64748b', padding: '4px 8px' }}>기본음원</th>
                                <td style={{ padding: '4px 8px' }}>
                                    <button type="button" className="btn btn-outline-dark btn-outline__gray btn-sm"
                                        style={{ fontSize: 12 }} onClick={handlePreviewBasic}>리스트 미리보기</button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                )}
            </div>

            {slots.length > 0 && (
                <div className="col-12" style={{ padding: '12px 15px', borderBottom: '1px solid #dde2eb', overflowX: 'auto' }}>
                    <table style={{ width: '100%', fontSize: 12, borderCollapse: 'collapse', minWidth: slots.length * 160 }}>
                        <thead>
                            <tr style={{ background: '#f8fafc' }}>
                                {slots.map((s) => (
                                    <th key={s} style={{ border: '1px solid #e2e8f0', padding: 6 }}>{Number(s)}분</th>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                {slots.map((s) => {
                                    const items = slotContents[s] ?? [];
                                    const totalPlayTime = items.reduce((sum, it) => sum + (Number(it.playTime) || 0), 0);
                                    return (
                                        <td key={s} style={{ border: '1px solid #e2e8f0', padding: 6, verticalAlign: 'top' }}>
                                            {items.map((it) => (
                                                <div key={it.brodSeq} style={{ marginBottom: 6 }}>
                                                    <button className="btn btn-link p-0" style={{ fontSize: 12 }}
                                                        onClick={() => handleOpenDetailModal(it)}>{it.orignlFileNm}</button>
                                                    <div style={{ color: '#94a3b8' }}>재생시간: {it.playTime}</div>
                                                    <div style={{ color: '#94a3b8' }}>{it.contentStartday || it.contentStartDay}~{it.contentEndday || it.contentEndDay}</div>
                                                    <button type="button" className="btn btn-outline-danger btn-outline__gray btn-sm"
                                                        style={{ fontSize: 11, marginTop: 2 }}
                                                        onClick={() => handleDeleteDetail(it.brodSeq)}>삭제</button>
                                                </div>
                                            ))}
                                            <div style={{ background: '#D2E1FF', padding: 4 }}>총재생시간: {secToMinSec(totalPlayTime)}</div>
                                        </td>
                                    );
                                })}
                            </tr>
                        </tbody>
                    </table>
                </div>
            )}

            <div className="col-12" style={{ padding: '12px 15px' }}>
                <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse' }}>
                    <thead>
                        <tr style={{ background: '#f8fafc' }}>
                            <th style={{ border: '1px solid #e2e8f0', padding: 8 }}>콘텐츠ID</th>
                            <th style={{ border: '1px solid #e2e8f0', padding: 8 }}>콘텐츠명</th>
                            <th style={{ border: '1px solid #e2e8f0', padding: 8 }}>특정방송여부</th>
                            <th style={{ border: '1px solid #e2e8f0', padding: 8 }}>적용기간</th>
                            <th style={{ border: '1px solid #e2e8f0', padding: 8 }}>반복재생</th>
                            <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 90 }}>삭제</th>
                        </tr>
                    </thead>
                    <tbody>
                        {anniverList.map((a) => (
                            <tr key={a.brodAnnSeq}>
                                <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>{a.brodAnnSeq}</td>
                                <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>
                                    <button className="btn btn-link p-0" onClick={() => handleOpenAnniverModal(a.brodAnnSeq)}>{a.anniverName}</button>
                                </td>
                                <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>{a.codeNm}</td>
                                <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>{a.anniverStartday || a.anniverStartDay}~{a.anniverEndday || a.anniverEndDay}</td>
                                <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>
                                    {a.anniversaryTime}
                                    {a.anniversaryStartTime ? ` 간격으로 ${a.anniversaryStartTime}분 마다 재생` : ''}
                                </td>
                                <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>
                                    <button type="button" className="btn btn-outline-danger btn-outline__gray btn-sm"
                                        style={{ fontSize: 12 }} onClick={() => handleDeleteAnniver(a.brodAnnSeq)}>삭제</button>
                                </td>
                            </tr>
                        ))}
                        {anniverList.length === 0 && (
                            <tr><td colSpan={6} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>등록된 특정방송이 없습니다.</td></tr>
                        )}
                    </tbody>
                </table>
            </div>

            <Suspense fallback={null}>
                {brodModalOpen && (
                    <BrodContentFormModal
                        open={brodModalOpen}
                        form={brodForm}
                        setForm={setBrodForm}
                        intervalCombo={intervalOptions}
                        basicCombo={basicOptions}
                        onClose={() => setBrodModalOpen(false)}
                        onSubmit={handleBrodSubmit}
                    />
                )}
                {anniverModalOpen && (
                    <BrodAnniversaryFormModal
                        open={anniverModalOpen}
                        form={anniverForm}
                        setForm={setAnniverForm}
                        gubunOptions={gubunOptions}
                        fileOptions={fileOptions}
                        onFileSearch={handleFileSearch}
                        onClose={() => setAnniverModalOpen(false)}
                        onSubmit={handleAnniverSubmit}
                    />
                )}
                {detailModalOpen && (
                    <BrodContentDetailFormModal
                        open={detailModalOpen}
                        form={detailForm}
                        setForm={setDetailForm}
                        timeOptions={timeOptions}
                        fileOptions={fileOptions}
                        onFileSearch={handleFileSearch}
                        onClose={() => setDetailModalOpen(false)}
                        onSubmit={handleDetailSubmit}
                    />
                )}
                {copyModalOpen && (
                    <BrodContentCopyModal
                        open={copyModalOpen}
                        combo={copyCombo}
                        onClose={() => setCopyModalOpen(false)}
                        onSubmit={handleCopySubmit}
                    />
                )}
            </Suspense>
        </div>
    );
}
