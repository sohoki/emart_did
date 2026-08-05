import { useCallback } from 'react';
import UseSwitch from '@/components/Common/IosSwitch.jsx';
import '@/style/Modal.css';

// 부서 등록/수정 모달. groupId는 신규 등록 시 서버에서 자동 채번(FN_GROUPCODE)하므로 입력하지 않음.
const GroupFormModal = ({
    open,
    form,
    setForm,
    groupOptions,
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
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '부서 등록' : '부서 수정'}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">
                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">부서명<span className="text-danger">*</span></label>
                                            <input type="text" id="groupNm" name="groupNm" className="form-control"
                                                value={form.groupNm}
                                                onChange={(e) => updateForm({ groupNm: e.target.value })} />
                                        </div>
                                    </div>
                                </div>
                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">상위부서</label>
                                            <select id="parentGroupId" name="parentGroupId" className="form-select"
                                                value={form.parentGroupId} onChange={(e) => updateForm({ parentGroupId: e.target.value })}>
                                                <option value="0">최상위</option>
                                                {groupOptions
                                                    .filter((o) => o.code !== form.groupId)
                                                    .map((o) => (
                                                        <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                    ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>
                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">사용 유무</label>
                                            <div className="input-group align-items-center">
                                                <UseSwitch
                                                    value={form.useYn}
                                                    name="useYn"
                                                    onChange={updateForm}
                                                    onText="사용"
                                                    offText="사용안함"
                                                />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">설명</label>
                                            <input type="text" id="groupDc" name="groupDc" className="form-control"
                                                value={form.groupDc}
                                                onChange={(e) => updateForm({ groupDc: e.target.value })} />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-action__lightblue" onClick={onClose}>취소</button>
                                <button type="button" className="btn btn-primary btn-action__blue" onClick={onSubmit}>
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

export default GroupFormModal;
