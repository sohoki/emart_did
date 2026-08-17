import { useCallback, useMemo, useState } from 'react';
import { CommonSearchSelect } from '@/components/Common/Select.jsx';
import Swal from '@/lib/swal.js';
import '@/style/Modal.css';

const stripDash = (v) => (v || '').replace(/-/g, '');
const dashify = (v) => (v && v.length === 8 ? `${v.slice(0, 4)}-${v.slice(4, 6)}-${v.slice(6, 8)}` : '');
const HOUR_OPTIONS = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0'));
const MIN_OPTIONS = Array.from({ length: 60 }, (_, i) => String(i).padStart(2, '0'));

// 방송 기념일(특정방송) 등록/수정 모달 — 레거시 brodContentView.jsp "특정 방송 등록"
// (별도 팝업창 ContentSpReg.do)을 참고.
const BrodAnniversaryFormModal = ({
    open, form, setForm, gubunOptions, fileOptions, onFileSearch, onClose, onSubmit,
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

    // 방송시간(시/분)은 anniversaryTime에 "HHmm" 4자리로 합쳐서 저장한다.
    const hour = (form.anniversaryTime || '').padStart(4, '0').slice(0, 2);
    const min = (form.anniversaryTime || '').padStart(4, '0').slice(2, 4);
    const updateTime = (h, m) => updateForm({ anniversaryTime: `${h}${m}` });

    const handleFileSearch = useCallback(async () => {
        setSearching(true);
        try {
            await onFileSearch(fileKeyword);
        } finally {
            setSearching(false);
        }
    }, [fileKeyword, onFileSearch]);

    const handleSubmit = useCallback(async () => {
        if (!form.anniverName) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '스케줄명을 입력해 주세요.' });
            return;
        }
        if (!form.atchFileId) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '콘텐츠명(음원 파일)을 선택해 주세요.' });
            return;
        }
        if (!form.anniverStartDay || !form.anniverEndDay) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '콘텐츠운영날짜를 입력해 주세요.' });
            return;
        }
        onSubmit();
    }, [form, onSubmit]);

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
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '특정방송 등록' : '특정방송 수정'}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">
                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">스케줄명<span className="text-danger">*</span></label>
                                            <input type="text" className="form-control"
                                                value={form.anniverName || ''}
                                                onChange={(e) => updateForm({ anniverName: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

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

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">콘텐츠운영날짜(시작일)<span className="text-danger">*</span></label>
                                            <input type="date" className="form-control"
                                                value={dashify(form.anniverStartDay)}
                                                onChange={(e) => updateForm({ anniverStartDay: stripDash(e.target.value) })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">콘텐츠운영날짜(종료일)<span className="text-danger">*</span></label>
                                            <input type="date" className="form-control"
                                                value={dashify(form.anniverEndDay)}
                                                onChange={(e) => updateForm({ anniverEndDay: stripDash(e.target.value) })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">특정방송여부</label>
                                            <select className="form-select" value={form.anniversaryGubun || ''}
                                                onChange={(e) => updateForm({ anniversaryGubun: e.target.value })}>
                                                <option value="">선택</option>
                                                {gubunOptions.map((g) => (
                                                    <option key={g.code} value={g.code}>{g.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">방송시간</label>
                                            <div className="input-group align-items-center" style={{ gap: 6 }}>
                                                <select className="form-select" style={{ width: 80 }}
                                                    value={hour} onChange={(e) => updateTime(e.target.value, min)}>
                                                    {HOUR_OPTIONS.map((h) => <option key={h} value={h}>{h}</option>)}
                                                </select>
                                                <span>시</span>
                                                <select className="form-select" style={{ width: 80 }}
                                                    value={min} onChange={(e) => updateTime(hour, e.target.value)}>
                                                    {MIN_OPTIONS.map((m) => <option key={m} value={m}>{m}</option>)}
                                                </select>
                                                <span>분</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">정렬순서</label>
                                            <select className="form-select" value={form.anniverOrder || '1'}
                                                onChange={(e) => updateForm({ anniverOrder: e.target.value })}>
                                                {Array.from({ length: 10 }, (_, i) => i + 1).map((o) => (
                                                    <option key={o} value={o}>{o}</option>
                                                ))}
                                            </select>
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

export default BrodAnniversaryFormModal;
