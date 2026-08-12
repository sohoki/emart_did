import { useCallback, useEffect, useState } from 'react';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';
import '@/style/Modal.css';

// MHS 모니터 등록/수정 모달 — 레거시 monitorList.jsp 하단 #moniter_pop 팝업 참고.
const MhsMonitorFormModal = ({ open, form, setForm, onClose, onSubmit }) => {
    const [brandList, setBrandList] = useState([]);
    const [centerList, setCenterList] = useState([]);

    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, [setForm]);

    useEffect(() => {
        if (!open) return;
        (async () => {
            const res = await fnAjaxFetch({ url: URL.MHS_BRAND_LIST, method: 'GET', showLoading: false });
            setBrandList(res?.data?.result?.resultList ?? []);
        })();
    }, [open]);

    useEffect(() => {
        if (!open || !form.mhsBrandcd) {
            setCenterList([]);
            return;
        }
        (async () => {
            const res = await fnAjaxFetch({
                url: URL.MHS_CENTER_LIST, method: 'GET', param: { mhsBrandcd: form.mhsBrandcd }, showLoading: false,
            });
            setCenterList(res?.data?.result?.resultList ?? []);
        })();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [open, form.mhsBrandcd]);

    const handleBrandChange = (value) => {
        updateForm({ mhsBrandcd: value, mhsCentercd: '' });
    };

    const handleSubmit = useCallback(async () => {
        if (!form.mhsBrandcd) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '부서명을 선택해 주세요.' });
            return;
        }
        if (!form.mhsCentercd) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '점포명을 선택해 주세요.' });
            return;
        }
        if (!form.mhsMonitornm) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '모니터명을 입력해 주세요.' });
            return;
        }
        onSubmit();
    }, [form, onSubmit]);

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
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '모니터 등록' : '모니터 수정'}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">부서명<span className="text-danger">*</span></label>
                                            <select id="mhsBrandcd" name="mhsBrandcd" className="form-select"
                                                value={form.mhsBrandcd} onChange={(e) => handleBrandChange(e.target.value)}>
                                                <option value="">선택하세요</option>
                                                {brandList.map((b) => (
                                                    <option key={b.mhsBrandcd} value={b.mhsBrandcd}>
                                                        {'  '.repeat(Math.max(0, Number(b.mhsBrandlv) - 1))}{b.mhsBrandnm}
                                                    </option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">점포명<span className="text-danger">*</span></label>
                                            <select id="mhsCentercd" name="mhsCentercd" className="form-select"
                                                value={form.mhsCentercd} onChange={(e) => updateForm({ mhsCentercd: e.target.value })}
                                                disabled={!form.mhsBrandcd}>
                                                <option value="">선택하세요</option>
                                                {centerList.map((c) => (
                                                    <option key={c.mhsCentercd} value={c.mhsCentercd}>{c.mhsCenternm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">모니터명<span className="text-danger">*</span></label>
                                            <input type="text" id="mhsMonitornm" name="mhsMonitornm" className="form-control"
                                                value={form.mhsMonitornm}
                                                onChange={(e) => updateForm({ mhsMonitornm: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">송출타입<span className="text-danger">*</span></label>
                                            <select id="mhsMviewtype" name="mhsMviewtype" className="form-select"
                                                value={form.mhsMviewtype} onChange={(e) => updateForm({ mhsMviewtype: e.target.value })}>
                                                <option value="1">단일</option>
                                                <option value="2">분할</option>
                                                <option value="3">리스트</option>
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">모니터 상태</label>
                                            <select id="mhsMonitorstatus" name="mhsMonitorstatus" className="form-select"
                                                value={form.mhsMonitorstatus} onChange={(e) => updateForm({ mhsMonitorstatus: e.target.value })}>
                                                <option value="Y">정상</option>
                                                <option value="N">사용안함</option>
                                            </select>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">비고</label>
                                            <input type="text" id="mhsRemark" name="mhsRemark" className="form-control"
                                                value={form.mhsRemark || ''}
                                                onChange={(e) => updateForm({ mhsRemark: e.target.value })} />
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

export default MhsMonitorFormModal;
