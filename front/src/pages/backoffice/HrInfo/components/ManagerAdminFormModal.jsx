import { useCallback } from 'react';
import { useIdCheck } from '@/hooks/use-id-check.js';
import { useCustomReqDataCombo } from '@/hooks/use-combo-data.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import UseSwitch from '@/components/Common/IosSwitch.jsx';
import URL from '@/constants/URL.jsx';
import '@/style/Modal.css';

const ROLE_MAPPING = { id: 'roleId', text: 'roleName' };

// 다른 관리자를 등록/수정하는 관리자 관리 화면 전용 모달(항상 mode: 'Ins'|'Edt').
// 헤더 프로필 드롭다운의 "내 정보 수정"은 별개 컴포넌트(ManagerFormModal.jsx, 본인 수정 전용).
// adminStateOptions/centerOptions/groupOptions는 모달을 열 때마다 다시 조회하지 않도록
// ManagerListPage에서 한 번만 조회해서 onData로 내려받는다.
const ManagerAdminFormModal = ({
    open,
    form,
    setForm,
    onClose,
    setModalOpen,
    onSearch,
    onData,
}) => {
    const { adminStateOptions = [], centerOptions = [], groupOptions = [] } = onData || {};
    const { handleIdCheck } = useIdCheck(URL.MANAGER_ID_CHECK, '아이디');
    const { options: roleOptions } = useCustomReqDataCombo({
        url: URL.ROLE_COMBO, params: {}, mapping: ROLE_MAPPING,
    });

    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, [setForm]);

    const onIdCheck = useCallback(async () => {
        await handleIdCheck(form.managerId, setForm);
    }, [form.managerId, setForm, handleIdCheck]);

    const handleSubmit = useCallback(async () => {
        if (!form.managerId) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '아이디를 입력해 주세요.' });
            return;
        }
        if (form.mode === 'Ins' && form.idCheck !== 'Y') {
            await Swal.fire({ icon: 'warning', title: '확인 필요', text: '아이디 중복체크가 안되었습니다.' });
            return;
        }
        if (!form.managerName) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '이름을 입력해 주세요.' });
            return;
        }
        if (form.mode === 'Ins' && !form.managerPassword) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '비밀번호를 입력해 주세요.' });
            return;
        }

        const action = form.mode === 'Ins' ? '등록' : '수정';
        const ok = await Swal.fire({
            icon: 'question', title: `관리자 ${action}`,
            html: `<b>${form.managerName}</b> ${action} 하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const payload = {
            mode: form.mode,
            managerId: form.managerId,
            idCheck: form.idCheck,
            managerName: form.managerName,
            managerPassword: form.managerPassword,
            partId: form.partId,
            roleId: form.roleId,
            centerId: form.centerId,
            managerEmail: form.managerEmail,
            managerTel: form.managerTel,
            managerPosition: form.managerPosition,
            managerStatus: form.managerStatus,
            useYn: form.useYn,
        };

        const res = await fnAjaxFetch({ url: URL.MANAGER_UPDATE, method: 'POST', data: payload });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || `${action}되었습니다.` });
            setModalOpen(false);
            onSearch?.(1);
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || `${action} 중 오류가 발생했습니다.` });
        }
    }, [form, setModalOpen, onSearch]);

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
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '관리자 등록' : '관리자 수정'}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">아이디<span className="text-danger">*</span></label>
                                            <div className="input-group">
                                                <input type="text" id="managerId" name="managerId" className="form-control"
                                                    value={form.managerId}
                                                    readOnly={form.mode !== 'Ins'}
                                                    onChange={(e) => updateForm({ managerId: e.target.value, idCheck: 'N' })}
                                                />
                                                {form.mode === 'Ins' && (
                                                    <button type="button" className="btn btn-primary btn-default__blue" onClick={onIdCheck}>
                                                        중복확인
                                                    </button>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">이름<span className="text-danger">*</span></label>
                                            <input type="text" id="managerName" name="managerName" className="form-control"
                                                value={form.managerName}
                                                onChange={(e) => updateForm({ managerName: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">부서</label>
                                            <select id="partId" name="partId" className="form-select"
                                                value={form.partId} onChange={(e) => updateForm({ partId: e.target.value })}>
                                                <option value="">선택</option>
                                                {groupOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">권한</label>
                                            <select id="roleId" name="roleId" className="form-select"
                                                value={form.roleId} onChange={(e) => updateForm({ roleId: e.target.value })}>
                                                <option value="">선택</option>
                                                {roleOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">매장 스코프</label>
                                            <select id="centerId" name="centerId" className="form-select"
                                                value={form.centerId} onChange={(e) => updateForm({ centerId: e.target.value })}>
                                                <option value="">전체</option>
                                                {centerOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">비밀번호{form.mode === 'Ins' && <span className="text-danger">*</span>}</label>
                                            <input type="password" id="managerPassword" name="managerPassword" className="form-control"
                                                placeholder={form.mode === 'Edt' ? '변경 시에만 입력' : ''}
                                                value={form.managerPassword}
                                                onChange={(e) => updateForm({ managerPassword: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">직급</label>
                                            <input type="text" id="managerPosition" name="managerPosition" className="form-control"
                                                value={form.managerPosition}
                                                onChange={(e) => updateForm({ managerPosition: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">연락처</label>
                                            <input type="text" id="managerTel" name="managerTel" className="form-control"
                                                placeholder="010-0000-0000"
                                                value={form.managerTel}
                                                onChange={(e) => updateForm({ managerTel: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">이메일</label>
                                            <input type="text" id="managerEmail" name="managerEmail" className="form-control"
                                                value={form.managerEmail}
                                                onChange={(e) => updateForm({ managerEmail: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">상태</label>
                                            <select id="managerStatus" name="managerStatus" className="form-select"
                                                value={form.managerStatus} onChange={(e) => updateForm({ managerStatus: e.target.value })}>
                                                <option value="">선택</option>
                                                {adminStateOptions.map((o) => (
                                                    <option key={o.codeDetailId || o.code} value={o.codeDetailId || o.code}>
                                                        {o.codeDetailNm || o.codeNm}
                                                    </option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">사용유무</label>
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

                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-action__lightblue" onClick={onClose}>취소</button>
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

export default ManagerAdminFormModal;
