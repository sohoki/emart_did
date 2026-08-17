import { useCallback, useMemo, useState } from 'react';
import { CommonSearchSelect } from '@/components/Common/Select.jsx';
import Swal from '@/lib/swal.js';
import '@/style/Modal.css';

const stripDash = (v) => (v || '').replace(/-/g, '');
const dashify = (v) => (v && v.length === 8 ? `${v.slice(0, 4)}-${v.slice(4, 6)}-${v.slice(6, 8)}` : '');

// 시간대별 음원 편성 등록/수정 모달 — 레거시 brodContentView.jsp "콘텐츠 등록"
// (별도 팝업창 ContentReg.do)을 참고.
// - 등록(Ins): 음원파일 검색 + "몇 분부터 몇 분 간격으로 몇 번 입력"(운영스케줄) +
//   정렬순서 + 운영날짜를 받아서, 부모가 timeCheck.do(자동 시간대 배정) →
//   update.do(등록) 순서로 처리한다.
// - 수정(Edt): 이미 확정된 편성 1건의 시간대/정렬순서/운영날짜만 직접 고쳐서
//   update.do를 바로 호출한다(자동 배정 재실행 없음).
const BrodContentDetailFormModal = ({
    open, form, setForm, timeOptions, fileOptions, onFileSearch, onClose, onSubmit,
}) => {
    const [fileKeyword, setFileKeyword] = useState('');
    const [searching, setSearching] = useState(false);

    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, [setForm]);

    // 음원 파일 검색 select 옵션 — CommonSearchSelect 형식({ code, codeNm })으로 변환.
    const fileSelectOptions = useMemo(
        () => fileOptions.map((f) => ({ code: f.atchFileId, codeNm: f.orignlFileNm })),
        [fileOptions],
    );

    const handleFileSearch = useCallback(async () => {
        setSearching(true);
        try {
            await onFileSearch(fileKeyword);
        } finally {
            setSearching(false);
        }
    }, [fileKeyword, onFileSearch]);

    const handleSubmit = useCallback(async () => {
        if (!form.atchFileId) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '음원 파일을 선택해 주세요.' });
            return;
        }
        if (form.mode !== 'Ins' && !form.timeCode) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '시간대를 선택해 주세요.' });
            return;
        }
        if (!form.contentStartDay || !form.contentEndDay) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '콘텐츠 운영날짜를 입력해 주세요.' });
            return;
        }
        onSubmit();
    }, [form, onSubmit]);

    if (!open) return null;

    const countOptions = Array.from({ length: 6 }, (_, i) => i + 1);

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered"
                    style={{ width: 560, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '콘텐츠 등록' : '콘텐츠 수정'}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">
                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">콘텐츠명(음원 파일)<span className="text-danger">*</span></label>
                                            <div className="input-group align-items-center" style={{ gap: 6, flexWrap: 'nowrap' }}>
                                                <input type="text" placeholder="파일명 검색" style={{ maxWidth: 160 }}
                                                    value={fileKeyword} onChange={(e) => setFileKeyword(e.target.value)}
                                                    onKeyDown={(e) => { if (e.key === 'Enter') handleFileSearch(); }} />
                                                <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                                                    onClick={handleFileSearch} disabled={searching}>
                                                    {searching ? '검색 중...' : '검색'}
                                                </button>
                                                <div style={{ flex: 1, minWidth: 200 }}>
                                                    <CommonSearchSelect
                                                        comboId="atchFileId"
                                                        comboData={fileSelectOptions}
                                                        placeholder="--선택하세요--"
                                                        value={form.atchFileId || ''}
                                                        onChange={(e) => updateForm({ atchFileId: e.target.value })}
                                                    />
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                {form.mode === 'Ins' ? (
                                    <div className="row input-box-wrap">
                                        <div className="col-12">
                                            <div className="input-box">
                                                <label className="form-label">운영스케줄<span className="text-danger">*</span></label>
                                                <div className="input-group align-items-center" style={{ gap: 6 }}>
                                                    <select className="form-select" style={{ width: 90 }}
                                                        value={form.startTimeCode || ''}
                                                        onChange={(e) => updateForm({ startTimeCode: e.target.value })}>
                                                        {timeOptions.map((t) => (
                                                            <option key={t.timeCode} value={t.timeCode}>{t.timeCode}</option>
                                                        ))}
                                                    </select>
                                                    <span>분 부터</span>
                                                    <select className="form-select" style={{ width: 90 }}
                                                        value={form.intervalMinutes || ''}
                                                        onChange={(e) => updateForm({ intervalMinutes: e.target.value })}>
                                                        {timeOptions.filter((t) => Number(t.timeCode) > 0).map((t) => (
                                                            <option key={t.timeCode} value={t.timeCode}>{t.timeCode}</option>
                                                        ))}
                                                    </select>
                                                    <span>간격으로</span>
                                                    <select className="form-select" style={{ width: 70 }}
                                                        value={form.insertCount || '1'}
                                                        onChange={(e) => updateForm({ insertCount: e.target.value })}>
                                                        {countOptions.map((c) => (
                                                            <option key={c} value={c}>{c}</option>
                                                        ))}
                                                    </select>
                                                    <span>번 입력</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                ) : (
                                    <div className="row input-box-wrap">
                                        <div className="col-6">
                                            <div className="input-box">
                                                <label className="form-label">시간대<span className="text-danger">*</span></label>
                                                <select className="form-select" value={form.timeCode || ''}
                                                    onChange={(e) => updateForm({ timeCode: e.target.value })}>
                                                    <option value="">선택</option>
                                                    {timeOptions.map((t) => (
                                                        <option key={t.timeCode} value={t.timeCode}>{Number(t.timeCode)}분</option>
                                                    ))}
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                )}

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">정렬순서</label>
                                            <select className="form-select" value={form.contentOrder || '1'}
                                                onChange={(e) => updateForm({ contentOrder: e.target.value })}>
                                                {Array.from({ length: 10 }, (_, i) => i + 1).map((o) => (
                                                    <option key={o} value={o}>{o}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

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
                                <button type="button" className="btn btn-primary btn-action__blue" onClick={handleSubmit}>
                                    {form.mode === 'Ins' ? '등록' : '수정'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default BrodContentDetailFormModal;
