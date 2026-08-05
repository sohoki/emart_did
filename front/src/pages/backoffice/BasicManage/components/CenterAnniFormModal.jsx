import { useCallback } from 'react';
import '@/style/Modal.css';

// 매장 기념일 등록 모달(기간 중복 체크는 부모의 onSubmit에서 처리).
const CenterAnniFormModal = ({
    open,
    form,
    setForm,
    brodOptions,
    onClose,
    onSubmit,
}) => {
    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, [setForm]);

    if (!open) return null;

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable"
                    style={{ width: 560, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">기념일 등록</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">시작일<span className="text-danger">*</span></label>
                                            <input type="date" id="centerAnniStartDay" name="centerAnniStartDay" className="form-control"
                                                value={form.centerAnniStartDay}
                                                onChange={(e) => updateForm({ centerAnniStartDay: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">종료일<span className="text-danger">*</span></label>
                                            <input type="date" id="centerAnniEndDay" name="centerAnniEndDay" className="form-control"
                                                value={form.centerAnniEndDay}
                                                onChange={(e) => updateForm({ centerAnniEndDay: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">오픈시간</label>
                                            <input type="time" id="startTime" name="startTime" className="form-control"
                                                value={form.startTime}
                                                onChange={(e) => updateForm({ startTime: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">폐점시간</label>
                                            <input type="time" id="endTime" name="endTime" className="form-control"
                                                value={form.endTime}
                                                onChange={(e) => updateForm({ endTime: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">방송콘텐츠</label>
                                            <select id="brodCode" name="brodCode" className="form-select"
                                                value={form.brodCode} onChange={(e) => updateForm({ brodCode: e.target.value })}>
                                                <option value="">선택</option>
                                                {brodOptions.map((o) => (
                                                    <option key={o.brodCode} value={o.brodCode}>{o.brodNm || o.brodCode}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-action__lightblue" onClick={onClose}>취소</button>
                                <button type="button" className="btn btn-primary btn-action__blue" onClick={onSubmit}>등록</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default CenterAnniFormModal;
