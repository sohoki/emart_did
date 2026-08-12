import { useCallback } from 'react';
import Swal from '@/lib/swal.js';
import '@/style/Modal.css';

// 방송(음원) 콘텐츠 등록/수정 모달 — 기존엔 우측 인라인 폼이었던 것을 다른 목록
// 화면들과 동일한 모달 패턴으로 교체.
const BrodContentFormModal = ({
    open, form, setForm, intervalCombo, basicCombo, onClose, onSubmit,
}) => {
    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, [setForm]);

    const handleSubmit = useCallback(async () => {
        if (!form.brodName) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '방송명을 입력해 주세요.' });
            return;
        }
        if (!form.brodInterval) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '재생간격을 선택해 주세요.' });
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
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '방송(음원) 콘텐츠 등록' : '방송(음원) 콘텐츠 수정'}</h2>
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
                                                value={form.brodName || ''}
                                                onChange={(e) => updateForm({ brodName: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">재생간격<span className="text-danger">*</span></label>
                                            <select className="form-select" value={form.brodInterval || ''}
                                                onChange={(e) => updateForm({ brodInterval: e.target.value })}>
                                                <option value="">선택</option>
                                                {intervalCombo.map((c) => (
                                                    <option key={c.code} value={c.code}>{c.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">기초 방송</label>
                                            <select className="form-select" value={form.basicBrodCode || ''}
                                                onChange={(e) => updateForm({ basicBrodCode: e.target.value })}>
                                                <option value="">선택 안 함</option>
                                                {basicCombo.map((c) => (
                                                    <option key={c.basicCode} value={c.basicCode}>{c.basicGroupNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">사용유무</label>
                                            <div className="input-group align-items-center" style={{ gap: 12 }}>
                                                <label><input type="radio" name="brodUseYn" checked={form.brodUseYn !== 'N'}
                                                    onChange={() => updateForm({ brodUseYn: 'Y' })} /> 사용</label>
                                                <label><input type="radio" name="brodUseYn" checked={form.brodUseYn === 'N'}
                                                    onChange={() => updateForm({ brodUseYn: 'N' })} /> 미사용</label>
                                            </div>
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

export default BrodContentFormModal;
