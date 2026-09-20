import { useCallback, useEffect, useMemo, useState } from 'react';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { CommonSearchSelect } from '@/components/Common/Select.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';
import '@/style/Modal.css';

const stripDash = (v) => (v || '').replace(/-/g, '');
const dashify = (v) => (v && v.length === 8 ? `${v.slice(0, 4)}-${v.slice(4, 6)}-${v.slice(6, 8)}` : '');
const stripColon = (v) => (v || '').replace(/:/g, '');
const colonify = (v) => (v && v.length === 4 ? `${v.slice(0, 2)}:${v.slice(2, 4)}` : '');

const DAY_OPTIONS = [
    { value: '1', label: '일' }, { value: '2', label: '월' }, { value: '3', label: '화' },
    { value: '4', label: '수' }, { value: '5', label: '목' }, { value: '6', label: '금' }, { value: '7', label: '토' },
];

// MHS 강의 등록/수정 모달 — 레거시 classList.jsp 팝업처럼 조직명(부서명)/점포명을 모달 안에서
// 직접 선택한다(목록 상단의 검색용 브랜드/매장 선택과는 무관 — MhsMonitorFormModal과 동일 패턴).
// 강의실은 그 점포에 소속된 모니터 중에서 고른다(레거시도 강의실=모니터코드였음).
const MhsClassFormModal = ({ open, form, setForm, onClose, onSubmit }) => {
    const [brandList, setBrandList] = useState([]);
    const [centerList, setCenterList] = useState([]);
    const [roomOptions, setRoomOptions] = useState([]);

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

    useEffect(() => {
        if (!open || !form.mhsCentercd) {
            setRoomOptions([]);
            return;
        }
        (async () => {
            const res = await fnAjaxFetch({ url: URL.MHS_MONITOR_COMBO, method: 'GET', param: { mhsCentercd: form.mhsCentercd }, showLoading: false });
            setRoomOptions(res?.data?.result?.resultList ?? []);
        })();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [open, form.mhsCentercd]);

    // 점포명은 매장 수가 많아 검색이 필요 — CommonSearchSelect 형식({ code, codeNm })으로 변환
    const centerSelectOptions = useMemo(
        () => centerList.map((c) => ({ code: c.mhsCentercd, codeNm: c.mhsCenternm })),
        [centerList],
    );

    const handleBrandChange = (value) => {
        updateForm({ mhsBrandcd: value, mhsCentercd: '', mhsClassroomnm: '' });
    };

    const handleCenterChange = (value) => {
        updateForm({ mhsCentercd: value, mhsClassroomnm: '' });
    };

    const toggleDay = (value) => {
        const days = (form.mhsClassdayofweek || '').split(',').filter(Boolean);
        const next = days.includes(value) ? days.filter((d) => d !== value) : [...days, value].sort();
        updateForm({ mhsClassdayofweek: next.join(',') });
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
        if (!form.mhsClassroomnm) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '강의실을 선택해 주세요.' });
            return;
        }
        if (!form.mhsClassnm) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '강의명을 입력해 주세요.' });
            return;
        }
        if (!form.mhsClassstartday || !form.mhsClassendday) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '시작일/종료일을 입력해 주세요.' });
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
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '강의 등록' : '강의 수정'}</h2>
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
                                            <CommonSearchSelect
                                                comboId="mhsCentercd"
                                                comboData={centerSelectOptions}
                                                placeholder="선택하세요"
                                                value={form.mhsCentercd}
                                                onChange={(e) => handleCenterChange(e.target.value)}
                                                disabled={!form.mhsBrandcd}
                                            />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">강의실<span className="text-danger">*</span></label>
                                            <select id="mhsClassroomnm" name="mhsClassroomnm" className="form-select"
                                                value={form.mhsClassroomnm} onChange={(e) => updateForm({ mhsClassroomnm: e.target.value })}
                                                disabled={!form.mhsCentercd}>
                                                <option value="">선택하세요</option>
                                                {roomOptions.map((r) => (
                                                    <option key={r.mhsMonitorcd} value={r.mhsMonitorcd}>{r.mhsMonitornm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">강의명<span className="text-danger">*</span></label>
                                            <input type="text" id="mhsClassnm" name="mhsClassnm" className="form-control"
                                                value={form.mhsClassnm}
                                                onChange={(e) => updateForm({ mhsClassnm: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">강사명</label>
                                            <input type="text" id="mhsTeachernm" name="mhsTeachernm" className="form-control"
                                                value={form.mhsTeachernm || ''}
                                                onChange={(e) => updateForm({ mhsTeachernm: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">요일</label>
                                            <div style={{ display: 'flex', gap: 4 }}>
                                                {DAY_OPTIONS.map((d) => {
                                                    const checked = (form.mhsClassdayofweek || '').split(',').includes(d.value);
                                                    return (
                                                        <button key={d.value} type="button"
                                                            className={`btn btn-sm ${checked ? 'btn-primary btn-default__blue' : 'btn-outline-secondary btn-outline__gray'}`}
                                                            onClick={() => toggleDay(d.value)}
                                                        >{d.label}</button>
                                                    );
                                                })}
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">시작일<span className="text-danger">*</span></label>
                                            <input type="date" id="mhsClassstartday" name="mhsClassstartday" className="form-control"
                                                value={dashify(form.mhsClassstartday)}
                                                onChange={(e) => updateForm({ mhsClassstartday: stripDash(e.target.value) })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">종료일<span className="text-danger">*</span></label>
                                            <input type="date" id="mhsClassendday" name="mhsClassendday" className="form-control"
                                                value={dashify(form.mhsClassendday)}
                                                onChange={(e) => updateForm({ mhsClassendday: stripDash(e.target.value) })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">시작시간</label>
                                            <input type="time" id="mhsClassstarttime" name="mhsClassstarttime" className="form-control"
                                                value={colonify(form.mhsClassstarttime)}
                                                onChange={(e) => updateForm({ mhsClassstarttime: stripColon(e.target.value) })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">종료시간</label>
                                            <input type="time" id="mhsClassendtime" name="mhsClassendtime" className="form-control"
                                                value={colonify(form.mhsClassendtime)}
                                                onChange={(e) => updateForm({ mhsClassendtime: stripColon(e.target.value) })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">소개</label>
                                            <textarea id="mhsClassintro" name="mhsClassintro" className="form-control" rows={3}
                                                value={form.mhsClassintro || ''}
                                                onChange={(e) => updateForm({ mhsClassintro: e.target.value })} />
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

export default MhsClassFormModal;
