import { useCallback, useEffect } from 'react';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import API_URL from '@/constants/URL.jsx';
import { usePwdValidate } from '@/hooks/use-pwd-validate.js';
import Swal from '@/lib/swal.js';
import '@/style/Modal.css';

const RuleItem = ({ passed, label }) => (
    <span style={{
        display: 'inline-flex', alignItems: 'center', gap: 4,
        fontSize: 11, color: passed ? '#22c55e' : '#94a3b8', marginRight: 10,
    }}>
        <span style={{ fontSize: 13 }}>{passed ? '✓' : '✕'}</span>
        {label}
    </span>
);

// 헤더 프로필 드롭다운에서 여는 "내 정보 수정" 모달(항상 mode: 'Edt').
// 다른 관리자를 등록/수정하는 화면이 아니므로 부서/권한/사용유무/상태 같은 관리 항목은 다루지 않음.
const ManagerFormModal = ({
    open,
    form,
    setForm,
    onClose,
    onSearch,
    setModalOpen,
}) => {
    const { rules: pwdRules, isValid: pwdValid } = usePwdValidate(form.managerPassword);
    const pwdMatch   = form.managerPasswordConfirm !== '' && form.managerPassword === form.managerPasswordConfirm;
    const pwdNoMatch = form.managerPasswordConfirm !== '' && form.managerPassword !== form.managerPasswordConfirm;

    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, [setForm]);

    const phoneValid = !form.managerTel || /^01[016789]-\d{3,4}-\d{4}$/.test(form.managerTel);
    const emailValid = !form.managerEmail || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.managerEmail);

    const handlePhoneChange = useCallback((e) => {
        const d = e.target.value.replace(/\D/g, '').slice(0, 11);
        let formatted = d;
        if (d.length > 7) formatted = `${d.slice(0, 3)}-${d.slice(3, 7)}-${d.slice(7)}`;
        else if (d.length > 3) formatted = `${d.slice(0, 3)}-${d.slice(3)}`;
        updateForm({ managerTel: formatted });
    }, [updateForm]);

    // 모달 열릴 때마다 비밀번호 입력은 항상 빈 값으로 시작(기존 해시를 화면에 노출/재전송하지 않음)
    useEffect(() => {
        if (open) updateForm({ managerPassword: '', managerPasswordConfirm: '' });
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [open]);

    const handleSubmit = useCallback(async () => {
        if (!form.managerName) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '이름을 입력해 주세요.' });
            return;
        }
        if (form.managerTel && !phoneValid) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '올바른 전화번호 형식이 아닙니다.' });
            return;
        }
        if (form.managerEmail && !emailValid) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '올바른 이메일 형식이 아닙니다.' });
            return;
        }
        if (form.managerPassword && !pwdValid) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '비밀번호는 8자리 이상, 영문/숫자/특수문자를 포함해야 합니다.' });
            return;
        }
        if (form.managerPassword && form.managerPassword !== form.managerPasswordConfirm) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '비밀번호가 일치하지 않습니다.' });
            return;
        }

        const ok = await Swal.fire({
            icon: 'question', title: '정보 수정',
            html: `<b>${form.managerName}</b> 정보를 수정 하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const payload = {
            mode: 'Edt',
            managerId: form.managerId,
            managerName: form.managerName,
            managerPosition: form.managerPosition,
            managerTel: form.managerTel,
            managerEmail: form.managerEmail,
            managerPassword: form.managerPassword,
            passwordHint: form.passwordHint,
            passwordCnsr: form.passwordCnsr,
        };

        const res = await fnAjaxFetch({ url: API_URL.MANAGER_UPDATE, method: 'POST', data: payload });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || '수정되었습니다.' });
            setModalOpen(false);
            onSearch?.(1);
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || '수정 중 오류가 발생했습니다.' });
        }
    }, [form, setModalOpen, onSearch, phoneValid, emailValid, pwdValid]);

    if (!open) return null;

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom" style={{ alignItems: 'center', justifyContent: 'center', overflowY: 'auto', paddingTop: '4vh', paddingBottom: '4vh' }}>
                <div className="modal-dialog modal-dialog-scrollable"
                    style={{ width: 640, maxWidth: '95%', backgroundColor: '#fff', maxHeight: '92vh', display: 'flex', flexDirection: 'column', margin: 'auto' }}>
                    <div className="modal-content" style={{ display: 'flex', flexDirection: 'column', minHeight: 0, flex: 1 }}>
                        <div className="modal-header" style={{ flexShrink: 0 }}>
                            <div className="modal-title">
                                <h2 className="modal-title__title">내 정보 수정</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content" style={{ overflowY: 'auto', flex: 1, minHeight: 0 }}>
                            <div className="modal-body__content tab-pane show active">

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">아이디</label>
                                            <input type="text" className="form-control" value={form.managerId || ''} readOnly
                                                style={{ background: '#f8fafc', color: '#64748b' }} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">이름</label>
                                            <input type="text" className="form-control"
                                                id="managerName" name="managerName"
                                                value={form.managerName || ''}
                                                onChange={(e) => updateForm({ managerName: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">직급</label>
                                            <input type="text" className="form-control"
                                                id="managerPosition" name="managerPosition"
                                                value={form.managerPosition || ''}
                                                onChange={(e) => updateForm({ managerPosition: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">연락처</label>
                                            <input type="text" className={`form-control${form.managerTel ? (phoneValid ? ' is-valid' : ' is-invalid') : ''}`}
                                                id="managerTel" name="managerTel"
                                                placeholder="010-0000-0000"
                                                value={form.managerTel || ''}
                                                onChange={handlePhoneChange} />
                                            {form.managerTel && !phoneValid && (
                                                <small className="text-danger mt-1 d-block">올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)</small>
                                            )}
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">이메일</label>
                                            <input type="text" className={`form-control${form.managerEmail ? (emailValid ? ' is-valid' : ' is-invalid') : ''}`}
                                                id="managerEmail" name="managerEmail"
                                                placeholder="example@domain.com"
                                                value={form.managerEmail || ''}
                                                onChange={(e) => updateForm({ managerEmail: e.target.value })} />
                                            {form.managerEmail && !emailValid && (
                                                <small className="text-danger mt-1 d-block">올바른 이메일 형식이 아닙니다.</small>
                                            )}
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">비밀번호 힌트</label>
                                            <input type="text" className="form-control"
                                                id="passwordHint" name="passwordHint"
                                                value={form.passwordHint || ''}
                                                onChange={(e) => updateForm({ passwordHint: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">비밀번호 힌트 답변</label>
                                            <input type="text" className="form-control"
                                                id="passwordCnsr" name="passwordCnsr"
                                                value={form.passwordCnsr || ''}
                                                onChange={(e) => updateForm({ passwordCnsr: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <hr />
                                <p style={{ margin: '0 0 8px', fontSize: 13, color: '#94a3b8' }}>
                                    비밀번호를 변경하려면 아래에 새 비밀번호를 입력하세요. 비워두면 기존 비밀번호가 유지됩니다.
                                </p>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">새 비밀번호</label>
                                            <input type="password"
                                                id="managerPassword" name="managerPassword"
                                                className={`form-control${form.managerPassword ? (pwdValid ? ' is-valid' : ' is-invalid') : ''}`}
                                                value={form.managerPassword || ''}
                                                onChange={(e) => updateForm({ managerPassword: e.target.value })} />
                                            {form.managerPassword && (
                                                <div style={{ marginTop: 4, display: 'flex', flexWrap: 'wrap', rowGap: 2 }}>
                                                    {pwdRules.map((r) => (
                                                        <RuleItem key={r.key} passed={r.passed} label={r.label} />
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">새 비밀번호 확인</label>
                                            <input type="password"
                                                id="managerPasswordConfirm" name="managerPasswordConfirm"
                                                className={`form-control${form.managerPasswordConfirm ? (pwdMatch ? ' is-valid' : ' is-invalid') : ''}`}
                                                value={form.managerPasswordConfirm || ''}
                                                onChange={(e) => updateForm({ managerPasswordConfirm: e.target.value })} />
                                            {pwdNoMatch && <small style={{ color: '#ef4444', marginTop: 4, display: 'block' }}>불일치</small>}
                                            {pwdMatch && <small style={{ color: '#22c55e', marginTop: 4, display: 'block' }}>일치</small>}
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>

                        <div className="modal-footer" style={{ flexShrink: 0 }}>
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-action__lightblue" onClick={onClose}>취소</button>
                                <button type="button" className="btn btn-primary btn-action__blue" onClick={handleSubmit}>수정</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default ManagerFormModal;
