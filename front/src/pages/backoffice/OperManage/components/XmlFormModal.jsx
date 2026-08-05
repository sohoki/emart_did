import { useCallback } from 'react';
import UseSwitch from '@/components/Common/IosSwitch.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';
import '@/style/Modal.css';

// XML(장비 통신 명령) 정보 등록/수정 모달 — 레거시 xmlDetail.jsp 참고.
const XmlFormModal = ({
    open,
    form,
    setForm,
    workGubunOptions,
    onClose,
    onSubmit,
}) => {
    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, [setForm]);

    const onIdCheck = useCallback(async () => {
        if (!form.xmlProcessName) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '명령어(Process Name)를 입력해 주세요.' });
            return;
        }
        const res = await fnAjaxFetch({
            url: URL.XML_PROCESS_CHECK, method: 'GET', param: { xmlProcessName: form.xmlProcessName },
        });
        const dupCnt = Number(res?.data?.result?.result ?? 0);
        if (dupCnt > 0) {
            await Swal.fire({ icon: 'error', title: '중복 확인', text: '이미 등록된 명령어입니다.' });
            updateForm({ idCheck: 'N' });
        } else {
            await Swal.fire({ icon: 'success', title: '중복 확인', text: '사용 가능한 명령어입니다.' });
            updateForm({ idCheck: 'Y' });
        }
    }, [form.xmlProcessName, updateForm]);

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
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '전문 등록' : '전문 수정'}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">전문구분<span className="text-danger">*</span></label>
                                            <select id="workGubun" name="workGubun" className="form-select"
                                                value={form.workGubun} onChange={(e) => updateForm({ workGubun: e.target.value })}>
                                                <option value="">선택</option>
                                                {workGubunOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">명령어(Process Name)<span className="text-danger">*</span></label>
                                            <div className="input-group">
                                                <input type="text" id="xmlProcessName" name="xmlProcessName" className="form-control"
                                                    value={form.xmlProcessName}
                                                    readOnly={form.mode !== 'Ins'}
                                                    onChange={(e) => updateForm({ xmlProcessName: e.target.value, idCheck: 'N' })}
                                                />
                                                {form.mode === 'Ins' && (
                                                    <button type="button" className="btn btn-primary btn-default__blue" onClick={onIdCheck}>
                                                        중복확인
                                                    </button>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">전문 설명</label>
                                            <input type="text" id="processRemark" name="processRemark" className="form-control"
                                                value={form.processRemark}
                                                onChange={(e) => updateForm({ processRemark: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">입력 파라미터<span style={{ fontSize: 12 }}>(콤마로 구분)</span></label>
                                            <textarea id="xmlInputParam" name="xmlInputParam" className="form-control" rows={3}
                                                placeholder="예: DID_ID,DID_MAC"
                                                value={form.xmlInputParam}
                                                onChange={(e) => updateForm({ xmlInputParam: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">입력 파라미터 샘플값<span style={{ fontSize: 12 }}>(콤마로 구분)</span></label>
                                            <textarea id="xmlInputParamSample" name="xmlInputParamSample" className="form-control" rows={3}
                                                value={form.xmlInputParamSample}
                                                onChange={(e) => updateForm({ xmlInputParamSample: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">출력 파라미터</label>
                                            <textarea id="xmlOutputParam" name="xmlOutputParam" className="form-control" rows={2}
                                                value={form.xmlOutputParam}
                                                onChange={(e) => updateForm({ xmlOutputParam: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">비고</label>
                                            <textarea id="xmlExplain" name="xmlExplain" className="form-control" rows={2}
                                                value={form.xmlExplain}
                                                onChange={(e) => updateForm({ xmlExplain: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">확인 여부</label>
                                            <div className="input-group align-items-center">
                                                <UseSwitch
                                                    value={form.testOk}
                                                    name="testOk"
                                                    onChange={updateForm}
                                                    onText="확인"
                                                    offText="미확인"
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

export default XmlFormModal;
