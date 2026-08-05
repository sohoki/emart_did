import { useCallback } from 'react';
import UseSwitch from '@/components/Common/IosSwitch.jsx';
import '@/style/Modal.css';

// DID 단말기 등록/수정 모달.
// 레거시 참고: emart_cms3.2.1/.../equiManage/didDetail.jsp
// 시리얼포트/모니터개수/Agent버전 등 레거시에서도 항상 숨김 처리되어 고정값으로만 저장되던
// 항목(didMonitercnt/didSerialtype/didSerialport/didSerialjavascript/didTimeInterval)은 제외함
// — 제출 시 부모(DidInfoList)에서 레거시와 동일한 고정값을 채워 보낸다.
const DidFormModal = ({
    open,
    form,
    setForm,
    roleOptions,
    centerOptions,
    groupOptions,
    typeOptions,
    resolutionOptions,
    ipTypeOptions,
    modelTypeOptions,
    osOptions,
    onClose,
    onSubmit,
}) => {
    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, [setForm]);

    // 해상도 코드명이 "가로*세로"(예: 1920*1080) 형태면 그 값으로 가로/세로를 자동 채우고
    // 직접 입력을 막는다. "기타"처럼 그 형태가 아닌 코드만 직접 입력 가능(레거시 didDetail.jsp의
    // DIDRES00~04 분기와 동일한 동작을 코드값 하드코딩 없이 codeNm 패턴으로 재현)
    const handleResolutionChange = useCallback((code) => {
        const opt = resolutionOptions.find((o) => o.code === code);
        const match = opt?.codeNm?.match(/^(\d+)\*(\d+)$/);
        if (match) {
            updateForm({ didResolution: code, didWidth: match[1], didHeight: match[2] });
        } else {
            updateForm({ didResolution: code });
        }
    }, [resolutionOptions, updateForm]);

    const selectedResolutionOpt = resolutionOptions.find((o) => o.code === form.didResolution);
    const isFixedResolution = !!selectedResolutionOpt?.codeNm?.match(/^(\d+)\*(\d+)$/);

    if (!open) return null;

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable"
                    style={{ width: 760, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '단말기 등록' : '단말기 수정'}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">단말명<span className="text-danger">*</span></label>
                                            <input type="text" id="didNm" name="didNm" className="form-control"
                                                value={form.didNm}
                                                onChange={(e) => updateForm({ didNm: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">단말 ID</label>
                                            <input type="text" id="didId" name="didId" className="form-control"
                                                value={form.didId} placeholder="자동할당" readOnly disabled />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">단말 MAC</label>
                                            <input type="text" id="didMac" name="didMac" className="form-control"
                                                value={form.didMac} placeholder="자동등록" readOnly disabled />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">단말 IP</label>
                                            <input type="text" id="didIpaddr" name="didIpaddr" className="form-control"
                                                value={form.didIpaddr} placeholder="자동등록" readOnly disabled />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">관리 부서<span className="text-danger">*</span></label>
                                            <select id="roleCode" name="roleCode" className="form-select"
                                                value={form.roleCode} onChange={(e) => updateForm({ roleCode: e.target.value })}>
                                                <option value="">선택</option>
                                                {roleOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">설치지점<span className="text-danger">*</span></label>
                                            <select id="centerId" name="centerId" className="form-select"
                                                value={form.centerId} onChange={(e) => updateForm({ centerId: e.target.value })}>
                                                <option value="">선택</option>
                                                {centerOptions.map((o) => (
                                                    <option key={o.centerId} value={o.centerId}>{o.centerNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">그룹정보<span style={{ fontSize: 12 }}>(단말타입이 음원방송이 아닐 경우 필수)</span></label>
                                            <select id="groupId" name="groupId" className="form-select"
                                                value={form.groupId} onChange={(e) => updateForm({ groupId: e.target.value })}>
                                                <option value="">선택안함</option>
                                                {groupOptions.map((o) => (
                                                    <option key={o.groupCode} value={o.groupCode}>{o.groupNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">단말 모델타입</label>
                                            <select id="didModelType" name="didModelType" className="form-select"
                                                value={form.didModelType} onChange={(e) => updateForm({ didModelType: e.target.value })}>
                                                <option value="">선택</option>
                                                {modelTypeOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">단말타입</label>
                                            <select id="didType" name="didType" className="form-select"
                                                value={form.didType} onChange={(e) => updateForm({ didType: e.target.value })}>
                                                <option value="">선택</option>
                                                {typeOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">단말 OS</label>
                                            <select id="didOs" name="didOs" className="form-select"
                                                value={form.didOs} onChange={(e) => updateForm({ didOs: e.target.value })}>
                                                <option value="">선택</option>
                                                {osOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">IP타입</label>
                                            <select id="didIptype" name="didIptype" className="form-select"
                                                value={form.didIptype} onChange={(e) => updateForm({ didIptype: e.target.value })}>
                                                <option value="">선택</option>
                                                {ipTypeOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">단말 해상도</label>
                                            <select id="didResolution" name="didResolution" className="form-select"
                                                value={form.didResolution} onChange={(e) => handleResolutionChange(e.target.value)}>
                                                <option value="">선택</option>
                                                {resolutionOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">가로 (px)</label>
                                            <input type="text" id="didWidth" name="didWidth" className="form-control" placeholder="1920"
                                                value={form.didWidth} readOnly={isFixedResolution}
                                                onChange={(e) => updateForm({ didWidth: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">세로 (px)</label>
                                            <input type="text" id="didHeight" name="didHeight" className="form-control" placeholder="1080"
                                                value={form.didHeight} readOnly={isFixedResolution}
                                                onChange={(e) => updateForm({ didHeight: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">운영 시작</label>
                                            <input type="time" id="didStartTime" name="didStartTime" className="form-control"
                                                value={form.didStartTime}
                                                onChange={(e) => updateForm({ didStartTime: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">운영 종료</label>
                                            <input type="time" id="didEndTime" name="didEndTime" className="form-control"
                                                value={form.didEndTime}
                                                onChange={(e) => updateForm({ didEndTime: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">사용유무</label>
                                            <div className="input-group align-items-center">
                                                <UseSwitch
                                                    value={form.didUseYn}
                                                    name="didUseYn"
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

export default DidFormModal;
