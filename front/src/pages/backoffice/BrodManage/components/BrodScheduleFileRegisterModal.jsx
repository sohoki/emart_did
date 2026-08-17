import { useCallback, useEffect, useMemo, useState } from 'react';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';
import '@/style/Modal.css';

const stripDash = (v) => (v || '').replace(/-/g, '');
const dashify = (v) => (v && v.length === 8 ? `${v.slice(0, 4)}-${v.slice(4, 6)}-${v.slice(6, 8)}` : '');
const ORDER_OPTIONS = Array.from({ length: 10 }, (_, i) => String(i + 1).padStart(2, '0'));
const MINUTE_OPTIONS = ['00', '10', '20', '30', '40', '50'];
const HOUR_OPTIONS = Array.from({ length: 24 }, (_, i) => String(i + 1).padStart(2, '0'));

const EMPTY_FORM = {
    regGubun: 'D', // D=콘텐츠(일반편성)등록, A=특정방송등록
    intervalSection: '', contentOrder: '',
    anniverName: '', anniversaryGubun: '', anniversaryTime: '', anniversaryStartTime: '',
    anniversaryTimeHour: '', anniversaryTimeTime: '', anniverOrder: '',
    contentStartDay: '', contentEndDay: '',
};

// 스케줄 음원 관리(brodContentPlayList.jsp) 우측 패널의 "등록" 버튼 → 레거시 팝업
// brodContentCopy.jsp를 모달로 재구성. 체크된 매장(brodCode 여러 개)에 같은 음원 파일을
// 일반편성(콘텐츠등록) 또는 특정방송(기념일)으로 한 번에 등록한다.
const BrodScheduleFileRegisterModal = ({ open, atchFileId, orignlFileNm, brodCodes, onClose, onSubmit }) => {
    const [form, setForm] = useState(EMPTY_FORM);
    const [comboData, setComboData] = useState({ anniversaryGubun: [], timeInfo1: [], timeInfo: [] });
    const [submitting, setSubmitting] = useState(false);

    // 이 모달은 부모가 `{registerModalOpen && <BrodScheduleFileRegisterModal .../>}` 형태로
    // 조건부 마운트한다(다른 모달들과 동일 관례) — 열릴 때마다 새로 마운트되므로 form은
    // useState(EMPTY_FORM) 초기값으로 이미 리셋된 상태다. 콤보 데이터만 마운트 시 1회 조회.
    useEffect(() => {
        fnAjaxFetch({
            url: URL.BROD_CONTENT_REG_POPUP_DATA, method: 'GET',
            param: { atchFileId }, showLoading: false,
        }).then((res) => {
            setComboData({
                anniversaryGubun: res?.data?.result?.anniversaryGubun ?? [],
                timeInfo1: res?.data?.result?.timeInfo1 ?? [],
                timeInfo: res?.data?.result?.timeInfo ?? [],
            });
        });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, []);

    const insertBrodCode = useMemo(() => Array.from(new Set(brodCodes)).join(','), [brodCodes]);

    const handleSubmit = useCallback(async () => {
        if (!form.contentStartDay || !form.contentEndDay) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '콘텐츠운영날짜를 입력해 주세요.' });
            return;
        }
        if (form.contentStartDay > form.contentEndDay) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '종료일이 시작일보다 빠를 수 없습니다.' });
            return;
        }

        setSubmitting(true);
        try {
            if (form.regGubun === 'D') {
                if (!form.intervalSection) {
                    await Swal.fire({ icon: 'warning', title: '입력 오류', text: '운영스케줄(재생위치)을 선택해 주세요.' });
                    return;
                }
                const res = await fnAjaxFetch({
                    url: URL.BROD_CONTENT_DETAIL_CENTER_UPDATE, method: 'POST',
                    data: {
                        insert_brodCode: insertBrodCode, atchFileId,
                        intervalSection: form.intervalSection, contentOrder: form.contentOrder,
                        contentStartDay: form.contentStartDay, contentEndDay: form.contentEndDay,
                    },
                });
                if (res?.data?.resultCodeInfo !== 'SUCCESS') {
                    await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || '등록 중 오류가 발생했습니다.' });
                    return;
                }
            } else {
                if (!form.anniversaryGubun) {
                    await Swal.fire({ icon: 'warning', title: '입력 오류', text: '특정방송구분을 선택해 주세요.' });
                    return;
                }
                const res = await fnAjaxFetch({
                    url: URL.BROD_CONTENT_ANN_DETAIL_CENTER_UPDATE, method: 'POST',
                    data: {
                        insert_brodCode: insertBrodCode, atchFileId,
                        anniverName: form.anniverName, anniversaryGubun: form.anniversaryGubun,
                        anniversaryTime: form.anniversaryTime, anniversaryStartTime: form.anniversaryStartTime,
                        anniversaryTimeHour: form.anniversaryTimeHour, anniversaryTimeTime: form.anniversaryTimeTime,
                        anniverOrder: form.anniverOrder,
                        contentStartDay: form.contentStartDay, contentEndDay: form.contentEndDay,
                    },
                });
                if (res?.data?.resultCodeInfo !== 'SUCCESS') {
                    await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || '등록 중 오류가 발생했습니다.' });
                    return;
                }
            }
            await Swal.fire({ icon: 'success', title: '완료', text: `${brodCodes.length}개 매장에 등록되었습니다.` });
            onSubmit();
        } finally {
            setSubmitting(false);
        }
    }, [form, insertBrodCode, atchFileId, brodCodes.length, onSubmit]);

    if (!open) return null;

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered"
                    style={{ width: 560, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">음원 등록</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">
                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">콘텐츠명</label>
                                            <div>{orignlFileNm}</div>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">대상 매장</label>
                                            <div style={{ color: '#64748b', fontSize: 13 }}>{brodCodes.length}개 매장 선택됨</div>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">등록구분</label>
                                            <div className="input-group align-items-center" style={{ gap: 12 }}>
                                                <label>
                                                    <input type="radio" name="regGubun" checked={form.regGubun === 'D'}
                                                        onChange={() => updateForm({ regGubun: 'D' })} /> 콘텐츠등록
                                                </label>
                                                <label>
                                                    <input type="radio" name="regGubun" checked={form.regGubun === 'A'}
                                                        onChange={() => updateForm({ regGubun: 'A' })} /> 특정방송등록
                                                </label>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                {form.regGubun === 'D' ? (
                                    <div className="row input-box-wrap">
                                        <div className="col-6">
                                            <div className="input-box">
                                                <label className="form-label">운영스케줄(재생위치)<span className="text-danger">*</span></label>
                                                <select className="form-select" value={form.intervalSection}
                                                    onChange={(e) => updateForm({ intervalSection: e.target.value })}>
                                                    <option value="">--선택하세요--</option>
                                                    {comboData.timeInfo.map((t) => (
                                                        <option key={t.timeCode} value={t.timeCode}>{t.timeCode}</option>
                                                    ))}
                                                </select>
                                            </div>
                                        </div>
                                        <div className="col-6">
                                            <div className="input-box">
                                                <label className="form-label">정렬순서</label>
                                                <select className="form-select" value={form.contentOrder}
                                                    onChange={(e) => updateForm({ contentOrder: e.target.value })}>
                                                    <option value="">--선택하세요--</option>
                                                    {ORDER_OPTIONS.map((o) => <option key={o} value={o}>{o}</option>)}
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                ) : (
                                    <>
                                        <div className="row input-box-wrap">
                                            <div className="col-12">
                                                <div className="input-box">
                                                    <label className="form-label">스케줄명</label>
                                                    <input type="text" className="form-control" value={form.anniverName}
                                                        onChange={(e) => updateForm({ anniverName: e.target.value })} />
                                                </div>
                                            </div>
                                        </div>
                                        <div className="row input-box-wrap">
                                            <div className="col-6">
                                                <div className="input-box">
                                                    <label className="form-label">특정방송구분<span className="text-danger">*</span></label>
                                                    <select className="form-select" value={form.anniversaryGubun}
                                                        onChange={(e) => updateForm({ anniversaryGubun: e.target.value })}>
                                                        <option value="">--선택하세요--</option>
                                                        {comboData.anniversaryGubun.map((g) => (
                                                            <option key={g.code} value={g.code}>{g.codeNm}</option>
                                                        ))}
                                                    </select>
                                                </div>
                                            </div>
                                            <div className="col-6">
                                                <div className="input-box">
                                                    <label className="form-label">정렬순서</label>
                                                    <select className="form-select" value={form.anniverOrder}
                                                        onChange={(e) => updateForm({ anniverOrder: e.target.value })}>
                                                        <option value="">--선택하세요--</option>
                                                        {ORDER_OPTIONS.map((o) => <option key={o} value={o}>{o}</option>)}
                                                    </select>
                                                </div>
                                            </div>
                                        </div>

                                        {form.anniversaryGubun === 'ANNGUBUN01' ? (
                                            <div className="row input-box-wrap">
                                                <div className="col-12">
                                                    <div className="input-box">
                                                        <label className="form-label">시간별주기</label>
                                                        <div className="input-group align-items-center" style={{ gap: 6 }}>
                                                            <select className="form-select" style={{ width: 100 }}
                                                                value={form.anniversaryTime}
                                                                onChange={(e) => updateForm({ anniversaryTime: e.target.value })}>
                                                                <option value="">선택</option>
                                                                {comboData.timeInfo1.map((t) => (
                                                                    <option key={t.timeCode} value={t.timeCode}>{t.timeCode}</option>
                                                                ))}
                                                            </select>
                                                            <span>시간당</span>
                                                            <select className="form-select" style={{ width: 90 }}
                                                                value={form.anniversaryStartTime}
                                                                onChange={(e) => updateForm({ anniversaryStartTime: e.target.value })}>
                                                                <option value="">선택</option>
                                                                {MINUTE_OPTIONS.map((m) => <option key={m} value={m}>{m}</option>)}
                                                            </select>
                                                            <span>분</span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        ) : (
                                            <div className="row input-box-wrap">
                                                <div className="col-12">
                                                    <div className="input-box">
                                                        <label className="form-label">방송시간</label>
                                                        <div className="input-group align-items-center" style={{ gap: 6 }}>
                                                            <select className="form-select" style={{ width: 90 }}
                                                                value={form.anniversaryTimeHour}
                                                                onChange={(e) => updateForm({ anniversaryTimeHour: e.target.value })}>
                                                                <option value="">선택</option>
                                                                {HOUR_OPTIONS.map((h) => <option key={h} value={h}>{h}</option>)}
                                                            </select>
                                                            <span>시</span>
                                                            <select className="form-select" style={{ width: 90 }}
                                                                value={form.anniversaryTimeTime}
                                                                onChange={(e) => updateForm({ anniversaryTimeTime: e.target.value })}>
                                                                <option value="">선택</option>
                                                                {MINUTE_OPTIONS.map((m) => <option key={m} value={m}>{m}</option>)}
                                                            </select>
                                                            <span>분</span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        )}
                                    </>
                                )}

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">콘텐츠운영날짜(시작일)<span className="text-danger">*</span></label>
                                            <input type="date" className="form-control"
                                                value={dashify(form.contentStartDay)}
                                                onChange={(e) => updateForm({ contentStartDay: stripDash(e.target.value) })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">콘텐츠운영날짜(종료일)<span className="text-danger">*</span></label>
                                            <input type="date" className="form-control"
                                                value={dashify(form.contentEndDay)}
                                                onChange={(e) => updateForm({ contentEndDay: stripDash(e.target.value) })} />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-cancel" onClick={onClose}>취소</button>
                                <button type="button" className="btn btn-primary btn-action__blue"
                                    onClick={handleSubmit} disabled={submitting}>
                                    {submitting ? '등록 중...' : '등록'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default BrodScheduleFileRegisterModal;
