import { useCallback } from 'react';
import UseSwitch from '@/components/Common/IosSwitch.jsx';
import '@/style/Modal.css';

// 매장(센터) 등록/수정 모달.
// 레거시 참고: emart_cms3.2.1/.../basicManage/centerDetail.jsp
// centerImgMap(지점 이미지맵)은 레거시에서도 실사용이 확인 안 돼 제외함(필요 시 후속 추가).
const CenterFormModal = ({
    open,
    form,
    setForm,
    groupOptions,
    brodOptions,
    centerCodeOptions,
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
                    style={{ width: 700, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '매장 등록' : '매장 수정'}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">지점명<span className="text-danger">*</span></label>
                                            <input type="text" id="centerNm" name="centerNm" className="form-control"
                                                value={form.centerNm}
                                                onChange={(e) => updateForm({ centerNm: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">관리 부서</label>
                                            <select id="roleCode" name="roleCode" className="form-select"
                                                value={form.roleCode} onChange={(e) => updateForm({ roleCode: e.target.value })}>
                                                <option value="">선택</option>
                                                {groupOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">영업 시작</label>
                                            <input type="time" id="centerStartTime" name="centerStartTime" className="form-control"
                                                value={form.centerStartTime}
                                                onChange={(e) => updateForm({ centerStartTime: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">영업 종료</label>
                                            <input type="time" id="centerEndTime" name="centerEndTime" className="form-control"
                                                value={form.centerEndTime}
                                                onChange={(e) => updateForm({ centerEndTime: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">지점구분</label>
                                            <select id="centerGubun" name="centerGubun" className="form-select"
                                                value={form.centerGubun} onChange={(e) => updateForm({ centerGubun: e.target.value })}>
                                                <option value="">선택</option>
                                                { centerCodeOptions && centerCodeOptions.length > 0 && centerCodeOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>

                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">우편번호</label>
                                            <div className="input-group">
                                                <input type="text" id="centerZipcode1" name="centerZipcode1" className="form-control" maxLength={3}
                                                    value={form.centerZipcode1}
                                                    onChange={(e) => updateForm({ centerZipcode1: e.target.value })} />
                                                <input type="text" id="centerZipcode2" name="centerZipcode2" className="form-control" maxLength={3}
                                                    value={form.centerZipcode2}
                                                    onChange={(e) => updateForm({ centerZipcode2: e.target.value })} />
                                            </div>
                                        </div>
                                    </div>
                                    
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">주소1</label>
                                            <input type="text" id="centerAddr1" name="centerAddr1" className="form-control"
                                                value={form.centerAddr1}
                                                onChange={(e) => updateForm({ centerAddr1: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">주소2</label>
                                            <input type="text" id="centerAddr2" name="centerAddr2" className="form-control"
                                                value={form.centerAddr2}
                                                onChange={(e) => updateForm({ centerAddr2: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                {form.centerGubun === 'BRANCH01' && (
                                    <div className="row input-box-wrap">
                                        <div className="col-6">
                                            <div className="input-box">
                                                <label className="form-label">기본음원</label>
                                                <select id="brodCode" name="brodCode" className="form-select"
                                                    value={form.brodCode} onChange={(e) => updateForm({ brodCode: e.target.value })}>
                                                    <option value="">선택</option>
                                                    {brodOptions.map((o) => (
                                                        <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                    ))}
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                )}

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">지점이미지</label>
                                            <input type="file" id="centerImgFile" name="centerImgFile" className="form-control" accept="image/*"
                                                onChange={(e) => updateForm({ centerImgFile: e.target.files?.[0] || null })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">사용유무</label>
                                            <div className="input-group align-items-center">
                                                <UseSwitch
                                                    value={form.centerUseYn}
                                                    name="centerUseYn"
                                                    onChange={updateForm}
                                                    onText="사용"
                                                    offText="사용안함"
                                                />
                                            </div>
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

export default CenterFormModal;
