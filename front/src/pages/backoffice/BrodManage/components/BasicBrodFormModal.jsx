import { useCallback } from 'react';
import Swal from '@/lib/swal.js';
import '@/style/Modal.css';

// 기초 방송(템플릿) 등록/수정 모달 — 기존엔 Swal.fire({input:'text'}) 프롬프트였던 것을
// 다른 목록 화면들과 동일한 모달 패턴으로 교체.
const BasicBrodFormModal = ({
    open, form, setForm, onClose, onSubmit,
}) => {
    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, [setForm]);

    const handleSubmit = useCallback(async () => {
        if (!form.basicGroupNm?.trim()) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '방송명을 입력해 주세요.' });
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
                    style={{ width: 480, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '기초 방송 등록' : '방송명 수정'}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">
                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">방송명<span className="text-danger">*</span></label>
                                            <input type="text" className="form-control"
                                                value={form.basicGroupNm}
                                                onChange={(e) => updateForm({ basicGroupNm: e.target.value })} />
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

export default BasicBrodFormModal;
