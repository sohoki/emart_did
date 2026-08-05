import { useCallback, useState } from 'react';
import UseSwitch from '@/components/Common/IosSwitch.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';
import '@/style/Modal.css';

const EMPTY_FORM = {
    sendMessage: '',
    sendMessageStartDay: '',
    sendMessageEndDay: '',
    sendMessageStartTime: '',
    sendMessageEndTime: '',
    sendFontType: '',
    sendUseYn: 'Y',
};

// YYYY-MM-DD(네이티브 date input) → YYYYMMDD(DB 저장 형식)로 변환
const stripDash = (v) => (v || '').replace(/-/g, '');

// 레거시 popup/SendMessageReg.jsp에 대응 — DidSendMessageModal에서 그룹/단말기를 고른 뒤
// "메시지 등록"을 누르면 여기서 실제 메시지 내용/기간을 입력해 전송한다.
const DidSendMessageFormModal = ({ open, groupCode, didIds, onClose, onSubmitted }) => {
    const [form, setForm] = useState(EMPTY_FORM);

    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, []);

    const handleSubmit = useCallback(async () => {
        if (!form.sendMessage) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '메시지 내용을 입력해 주세요.' });
            return;
        }
        const startDay = stripDash(form.sendMessageStartDay);
        const endDay = stripDash(form.sendMessageEndDay);
        if (!startDay || !endDay) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '시작일/종료일을 입력해 주세요.' });
            return;
        }
        if (startDay > endDay) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '시작일이 종료일보다 늦을 수 없습니다.' });
            return;
        }
        if (!form.sendMessageStartTime || !form.sendMessageEndTime) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '시작시간/종료시간을 입력해 주세요.' });
            return;
        }

        const ok = await Swal.fire({
            icon: 'question', title: '메시지 등록',
            html: `선택한 단말기 <b>${didIds.length}대</b>에 메시지를 전송하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const [startHour, startMin] = form.sendMessageStartTime.split(':');
        const [endHour, endMin] = form.sendMessageEndTime.split(':');

        const res = await fnAjaxFetch({
            url: URL.CON_MESSAGE_UPDATE, method: 'POST',
            data: {
                mode: 'Ins',
                groupCode,
                didIds,
                sendMessage: form.sendMessage,
                sendMessageStartDay: startDay,
                sendMessageEndDay: endDay,
                sendMessageStartHour: startHour,
                sendMessageStartMin: startMin,
                sendMessageEndHour: endHour,
                sendMessageEndMin: endMin,
                sendFontType: form.sendFontType,
                sendUseYn: form.sendUseYn,
            },
        });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || '메시지가 등록되었습니다.' });
            onSubmitted?.();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || '등록 중 오류가 발생했습니다.' });
        }
    }, [form, groupCode, didIds, onSubmitted]);

    if (!open) return null;

    return (
        <>
            {/* 부모(DidSendMessageModal)의 backdrop/dialog와 z-index(2000/2001)가 같아서
                이 중첩 모달을 열어도 뒤의 "그룹/단말기 선택" 모달이 옆으로 비쳐 보이던 문제 —
                이 중첩 모달만 한 단계 위로 올려서 부모 모달까지 완전히 덮는다. */}
            <div className="modal-backdrop-custom" onClick={onClose} style={{ zIndex: 2010 }} />
            <div className="modal-custom" style={{ zIndex: 2011 }}>
                <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable"
                    style={{ width: 560, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">메시지 등록 — 단말기 {didIds.length}대</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">

                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">메시지 내용<span className="text-danger">*</span></label>
                                            <textarea id="sendMessage" name="sendMessage" className="form-control" rows={3}
                                                value={form.sendMessage}
                                                onChange={(e) => updateForm({ sendMessage: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">시작일<span className="text-danger">*</span></label>
                                            <input type="date" id="sendMessageStartDay" name="sendMessageStartDay" className="form-control"
                                                value={form.sendMessageStartDay}
                                                onChange={(e) => updateForm({ sendMessageStartDay: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">종료일<span className="text-danger">*</span></label>
                                            <input type="date" id="sendMessageEndDay" name="sendMessageEndDay" className="form-control"
                                                value={form.sendMessageEndDay}
                                                onChange={(e) => updateForm({ sendMessageEndDay: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">시작시간<span className="text-danger">*</span></label>
                                            <input type="time" id="sendMessageStartTime" name="sendMessageStartTime" className="form-control"
                                                value={form.sendMessageStartTime}
                                                onChange={(e) => updateForm({ sendMessageStartTime: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">종료시간<span className="text-danger">*</span></label>
                                            <input type="time" id="sendMessageEndTime" name="sendMessageEndTime" className="form-control"
                                                value={form.sendMessageEndTime}
                                                onChange={(e) => updateForm({ sendMessageEndTime: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">글자크기<span style={{ fontSize: 12 }}>(선택)</span></label>
                                            <input type="text" id="sendFontType" name="sendFontType" className="form-control"
                                                value={form.sendFontType}
                                                onChange={(e) => updateForm({ sendFontType: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">사용유무</label>
                                            <div className="input-group align-items-center">
                                                <UseSwitch
                                                    value={form.sendUseYn}
                                                    name="sendUseYn"
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
                                <button type="button" className="btn btn-primary btn-action__blue" onClick={handleSubmit}>전송</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default DidSendMessageFormModal;
