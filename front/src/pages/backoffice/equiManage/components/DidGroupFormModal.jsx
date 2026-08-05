import { useCallback } from 'react';
import { useIdCheck } from '@/hooks/use-id-check.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';
import '@/style/Modal.css';

// DID 그룹(TB_GROUP) 등록/수정 모달.
// 레거시 참고: emart_cms3.2.1/.../equiManage/did_groupList.jsp 하단 그룹등록 폼
// 레거시 폼에도 사용유무 입력은 없었고(등록 시 항상 'Y' 고정), 백엔드 수정 쿼리도
// GROUP_NM만 갱신하고 GROUP_USEYN은 반영하지 않아 — 동일하게 사용유무 필드는 노출하지 않음
const DidGroupFormModal = ({
    open,
    form,
    setForm,
    onClose,
    onSubmit,
}) => {
    const { handleIdCheck } = useIdCheck(URL.DID_GROUP_ID_CHECK, '그룹코드');

    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, [setForm]);

    const onIdCheck = useCallback(async () => {
        await handleIdCheck(form.groupCode, setForm);
    }, [form.groupCode, setForm, handleIdCheck]);

    const handleSubmit = useCallback(async () => {
        if (!form.groupCode) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '그룹코드를 입력해 주세요.' });
            return;
        }
        if (form.mode === 'Ins' && form.idCheck !== 'Y') {
            await Swal.fire({ icon: 'warning', title: '확인 필요', text: '그룹코드 중복확인이 안되었습니다.' });
            return;
        }
        if (!form.groupNm) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '그룹명을 입력해 주세요.' });
            return;
        }

        const action = form.mode === 'Ins' ? '등록' : '수정';
        const ok = await Swal.fire({
            icon: 'question', title: `그룹 ${action}`,
            html: `<b>${form.groupNm}</b> ${action} 하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const payload = { mode: form.mode, groupCode: form.groupCode, groupNm: form.groupNm };
        const res = await fnAjaxFetch({ url: URL.DID_GROUP_UPDATE, method: 'POST', data: payload });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || `${action}되었습니다.` });
            onSubmit?.();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || `${action} 중 오류가 발생했습니다.` });
        }
    }, [form, onSubmit]);

    if (!open) return null;

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable"
                    style={{ width: 480, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '그룹 등록' : '그룹 수정'}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">

                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">그룹코드<span className="text-danger">*</span></label>
                                            <div className="input-group">
                                                <input type="text" id="groupCode" name="groupCode" className="form-control"
                                                    value={form.groupCode}
                                                    readOnly={form.mode !== 'Ins'}
                                                    style={form.mode !== 'Ins' ? { backgroundColor: '#e9ecef', color: '#6c757d', cursor: 'not-allowed' } : undefined}
                                                    onChange={(e) => updateForm({ groupCode: e.target.value, idCheck: 'N' })}
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
                                            <label className="form-label">그룹명<span className="text-danger">*</span></label>
                                            <input type="text" id="groupNm" name="groupNm" className="form-control"
                                                value={form.groupNm}
                                                onChange={(e) => updateForm({ groupNm: e.target.value })} />
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

export default DidGroupFormModal;
