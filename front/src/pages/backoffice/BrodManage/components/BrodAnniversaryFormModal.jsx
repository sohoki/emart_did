import { useCallback } from 'react';
import Swal from '@/lib/swal.js';
import '@/style/Modal.css';

// 방송 기념일 등록/수정 모달 — 기존엔 우측 인라인 폼이었던 것을 다른 목록 화면들과
// 동일한 모달 패턴으로 교체.
const BrodAnniversaryFormModal = ({
    open, form, setForm, onClose, onSubmit,
}) => {
    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, [setForm]);

    const handleSubmit = useCallback(async () => {
        if (!form.brodCode || !form.anniverName) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '방송코드/기념일명을 입력해 주세요.' });
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
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '방송 기념일 등록' : '방송 기념일 수정'}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">
                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">방송 코드<span className="text-danger">*</span></label>
                                            <input type="text" className="form-control"
                                                value={form.brodCode || ''} readOnly
                                                style={{ backgroundColor: '#e9ecef', color: '#6c757d' }} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">기념일명<span className="text-danger">*</span></label>
                                            <input type="text" className="form-control"
                                                value={form.anniverName || ''}
                                                onChange={(e) => updateForm({ anniverName: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">시작일(YYYYMMDD)</label>
                                            <input type="text" className="form-control" placeholder="20260101"
                                                value={form.anniverStartday || ''}
                                                onChange={(e) => updateForm({ anniverStartday: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">종료일(YYYYMMDD)</label>
                                            <input type="text" className="form-control" placeholder="20261231"
                                                value={form.anniverEndday || ''}
                                                onChange={(e) => updateForm({ anniverEndday: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">시작시간(HHmm)</label>
                                            <input type="text" className="form-control" placeholder="0900"
                                                value={form.anniversaryStartTime || ''}
                                                onChange={(e) => updateForm({ anniversaryStartTime: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">재생시간(초)</label>
                                            <input type="text" className="form-control" placeholder="60"
                                                value={form.anniversaryTime || ''}
                                                onChange={(e) => updateForm({ anniversaryTime: e.target.value })} />
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
