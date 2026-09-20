import { useCallback, useEffect, useMemo, useState, Suspense, lazy } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const BrodContentFormModal = lazy(() => import('./components/BrodContentFormModal.jsx'));
const BrodAnniversaryFormModal = lazy(() => import('./components/BrodAnniversaryFormModal.jsx'));
const BrodContentDetailFormModal = lazy(() => import('./components/BrodContentDetailFormModal.jsx'));
const BrodContentCopyModal = lazy(() => import('./components/BrodContentCopyModal.jsx'));
const BrodContentOrganizationModal = lazy(() => import('./components/BrodContentOrganizationModal.jsx'));

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

// DidDetailPage/MhsMonitorDetailPage와 동일한 "정보 박스"(3열 라벨/값 그리드) 스타일 —
// 이 앱의 상세 화면 공통 관례를 그대로 따른다.
const sectionTitleStyle = { textAlign: 'center', fontWeight: 700, padding: 10, background: '#eef1f5', border: '1px solid #e2e8f0', borderBottom: 'none' };
const labelCellStyle = { width: '16.6%', textAlign: 'center', color: '#64748b', padding: '10px 8px', background: '#f8fafc', border: '1px solid #e2e8f0' };
const valueCellStyle = { width: '16.6%', textAlign: 'center', padding: '10px 8px', border: '1px solid #e2e8f0' };

const BADGE_TONE = {
    green: { background: '#dcfce7', color: '#15803d' },
    gray: { background: '#f1f5f9', color: '#64748b' },
    blue: { background: '#dbeafe', color: '#1d4ed8' },
    purple: { background: '#ede9fe', color: '#6d28d9' },
};
const Badge = ({ tone = 'gray', children }) => (
    <span style={{
        display: 'inline-block', padding: '2px 10px', borderRadius: 999, fontSize: 12, fontWeight: 600,
        ...BADGE_TONE[tone],
    }}>{children}</span>
);

// 방송(음원) 콘텐츠 상세(편성) 화면 — 레거시 brodContentView.jsp 참고. 시간대별 음원
// 배치, 특정방송(기념일), 배치 적용, 음원 콘텐츠 복사, 편성표생성/방송표보기를 이
// 화면 하나에서 다룬다.
// "편성표생성"/"방송표보기"는 이 화면(centerId 없이 브로드코드 단독)에서 쓰는 "일반
// 편성표" 케이스만 백엔드에 포팅돼 있다. 지점(centerId) 기준 배포 케이스는 원본이
// 의존하는 FN_CENTERBRODINFO DB 함수가 did_emart에 없어 계속 제외 상태(백엔드
// BrodContentInfoManageController 상단 주석 참고).
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

    const [orgModalOpen, setOrgModalOpen] = useState(false);
    const [orgList, setOrgList] = useState([]);

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

    // ===== 방송표보기 =====
    const handleOpenOrgModal = useCallback(async () => {
        const res = await fnAjaxFetch({
            url: `${URL.BROD_CONTENT_ORGANIZATION_VIEW}/${brodCode}/organization.do`, method: 'GET',
        });
        setOrgList(res?.data?.result?.resultList ?? []);
        setOrgModalOpen(true);
    }, [brodCode]);

    // ===== 편성표생성 =====
    const handleGenerateSchedule = useCallback(async () => {
        const result = await Swal.fire({
            icon: 'warning', title: '편성표생성',
            text: '현재 방송표를 지우고 특정방송/일반 편성 기준으로 새로 생성합니다. 계속하시겠습니까?',
            showCancelButton: true, confirmButtonText: '생성', cancelButtonText: '취소',
        });
        if (!result.isConfirmed) return;

        const res = await fnAjaxFetch({
            url: `${URL.BROD_CONTENT_SCHEDULE_GENERATE}?brodCode=${encodeURIComponent(brodCode)}`, method: 'POST',
        });
        const applied = res?.data?.result?.result;
        await Swal.fire({
            icon: applied ? 'success' : 'warning', title: '편성표생성',
            text: applied ? '방송표가 정상적으로 생성되었습니다.' : '방송표 생성에 실패했습니다. 반복재생간격 설정을 확인해 주세요.',
        });
    }, [brodCode]);

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

            <div className="col-12 content-search__action" style={{
                padding: '0 0 16px', flexWrap: 'wrap', display: 'flex', justifyContent: 'space-between',
                alignItems: 'center', gap: 8, borderBottom: '1px solid #e2e8f0', marginBottom: 16,
            }}>
                <button type="button" className="btn btn-outline-dark btn-outline__gray"
                    onClick={() => navigate('/backoffice/sub/brodManage/content')}>← 목록</button>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                        onClick={handleOpenBrodModal}>수정</button>
                    <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                        onClick={handleOpenCopyModal}>음원 콘텐츠 복사</button>
                    <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                        onClick={handleScheduleConfirm}>배치 적용</button>
                    <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                        onClick={handleGenerateSchedule}>편성표생성</button>
                    <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                        onClick={handleOpenOrgModal}>방송표보기</button>
                    <button type="button" className="btn btn-primary btn-default__blue"
                        onClick={() => handleOpenDetailModal(null)}>+ 콘텐츠 등록</button>
                    <button type="button" className="btn btn-primary btn-default__blue"
                        onClick={() => handleOpenAnniverModal(null)}>+ 특정 방송 등록</button>
                </div>
            </div>

            {!detail ? (
                <div className="col-12" style={{ padding: 40, textAlign: 'center', color: '#94a3b8' }}>{loading ? '조회 중...' : '데이터가 없습니다.'}</div>
            ) : (
                <div className="col-12" style={{ marginBottom: 24 }}>
                    <div style={sectionTitleStyle}>콘텐츠 정보</div>
                    <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse' }}>
                        <tbody>
                            <tr>
                                <th style={labelCellStyle}>콘텐츠코드</th>
                                <td style={valueCellStyle}>{detail.brodCode}</td>
                                <th style={labelCellStyle}>콘텐츠명</th>
                                <td style={valueCellStyle}>{detail.brodName}</td>
                                <th style={labelCellStyle}>구분</th>
                                <td style={valueCellStyle}>
                                    <Badge tone={detail.secGubun === 'SECGUBUN01' ? 'blue' : 'purple'}>
                                        {detail.secGubun === 'SECGUBUN01' ? '일반스케줄' : '이벤트스케줄'}
                                    </Badge>
                                </td>
                            </tr>
                            <tr>
                                <th style={labelCellStyle}>사용유무</th>
                                <td style={valueCellStyle}>
                                    <Badge tone={detail.brodUseYn === 'Y' ? 'green' : 'gray'}>
                                        {detail.brodUseYn === 'Y' ? '사용' : '사용 안함'}
                                    </Badge>
                                </td>
                                <th style={labelCellStyle}>반복재생간격</th>
                                <td style={valueCellStyle}>{detail.codeNm}</td>
                                <th style={labelCellStyle}>기본음원</th>
                                <td style={valueCellStyle}>
                                    <button type="button" className="btn btn-outline-dark btn-outline__gray btn-sm"
                                        style={{ fontSize: 12 }} onClick={handlePreviewBasic}>리스트 미리보기</button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            )}

            {slots.length > 0 && (
                <div className="col-12" style={{ marginBottom: 24, overflowX: 'auto' }}>
                    <div style={sectionTitleStyle}>시간대별 음원 배치</div>
                    <div style={{ display: 'flex', minWidth: slots.length * 190, border: '1px solid #e2e8f0', borderTop: 'none' }}>
                        {slots.map((s) => {
                            const items = slotContents[s] ?? [];
                            const totalPlayTime = items.reduce((sum, it) => sum + (Number(it.playTime) || 0), 0);
                            return (
                                <div key={s} style={{ flex: '1 0 190px', borderRight: '1px solid #e2e8f0', display: 'flex', flexDirection: 'column' }}>
                                    <div style={{
                                        textAlign: 'center', fontWeight: 600, fontSize: 13, color: '#475569',
                                        background: '#f8fafc', padding: '8px 6px', borderBottom: '1px solid #e2e8f0',
                                    }}>{Number(s)}분</div>
                                    <div style={{ flex: 1, padding: 8, fontSize: 12 }}>
                                        {items.length === 0 && (
                                            <div style={{ color: '#cbd5e1', textAlign: 'center', padding: '12px 0' }}>배치된 음원 없음</div>
                                        )}
                                        {items.map((it, idx) => (
                                            <div key={it.brodSeq} style={{
                                                marginBottom: 8, paddingBottom: 8,
                                                borderBottom: idx < items.length - 1 ? '1px dashed #e2e8f0' : 'none',
                                            }}>
                                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 4 }}>
                                                    <button className="btn btn-link p-0" style={{ fontSize: 12, textAlign: 'left' }}
                                                        onClick={() => handleOpenDetailModal(it)}>{it.orignlFileNm}</button>
                                                    <button type="button" title="삭제" onClick={() => handleDeleteDetail(it.brodSeq)}
                                                        style={{
                                                            border: 'none', background: 'transparent', color: '#cbd5e1', cursor: 'pointer',
                                                            fontSize: 14, lineHeight: 1, padding: 0, flexShrink: 0,
                                                        }}
                                                        onMouseEnter={(e) => { e.currentTarget.style.color = '#ef4444'; }}
                                                        onMouseLeave={(e) => { e.currentTarget.style.color = '#cbd5e1'; }}
                                                    >✕</button>
                                                </div>
                                                <div style={{ color: '#94a3b8' }}>재생시간: {it.playTime}</div>
                                                <div style={{ color: '#94a3b8' }}>{it.contentStartday || it.contentStartDay}~{it.contentEndday || it.contentEndDay}</div>
                                            </div>
                                        ))}
                                    </div>
                                    <div style={{
                                        margin: 8, marginTop: 0, padding: '4px 10px', borderRadius: 999,
                                        background: '#eef2ff', color: '#4338ca', fontSize: 11, fontWeight: 600, textAlign: 'center',
                                    }}>총재생시간 {secToMinSec(totalPlayTime)}</div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            )}

            <div className="col-12">
                <div style={sectionTitleStyle}>특정방송(기념일) 목록</div>
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
                        {anniverList.map((a, idx) => (
                            <tr key={a.brodAnnSeq} style={{ background: idx % 2 === 1 ? '#fafbfc' : undefined }}>
                                <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center', color: '#94a3b8' }}>{a.brodAnnSeq}</td>
                                <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>
                                    <button className="btn btn-link p-0" onClick={() => handleOpenAnniverModal(a.brodAnnSeq)}>{a.anniverName}</button>
                                </td>
                                <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}><Badge tone="blue">{a.codeNm}</Badge></td>
                                <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'center' }}>{a.anniverStartday || a.anniverStartDay}~{a.anniverEndday || a.anniverEndDay}</td>
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
                            <tr><td colSpan={6} style={{ padding: 24, textAlign: 'center', color: '#94a3b8', border: '1px solid #e2e8f0' }}>등록된 특정방송이 없습니다.</td></tr>
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
                {orgModalOpen && (
                    <BrodContentOrganizationModal
                        open={orgModalOpen}
                        brodName={detail?.brodName}
                        list={orgList}
                        onClose={() => setOrgModalOpen(false)}
                    />
                )}
            </Suspense>
        </div>
    );
}
