import { useCallback, useState } from 'react';
import Swal from '@/lib/swal.js';
import '@/style/Modal.css';

const stripDash = (v) => (v || '').replace(/-/g, '');
const dashify = (v) => (v && v.length === 8 ? `${v.slice(0, 4)}-${v.slice(4, 6)}-${v.slice(6, 8)}` : '');

// 발송 스케줄 등록/수정 모달 — 레거시 schDetail.jsp/schView.jsp 참고.
const ScheduleFormModal = ({
    open, form, setForm, groupOptions, contentOptions,
    onGroupSearch, onContentSearch, onClose, onSubmit, onDelete,
}) => {
    const [groupKeyword, setGroupKeyword] = useState('');
    const [contentKeyword, setContentKeyword] = useState('');

    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, [setForm]);

    const handleSubmit = useCallback(async () => {
        if (!form.schName) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '스케줄명을 입력해 주세요.' });
            return;
        }
        if (!form.schStartDay || !form.schEndDay) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '송출 기간을 입력해 주세요.' });
            return;
        }
        if (form.schStartDay > form.schEndDay) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '시작일이 종료일보다 늦을 수 없습니다.' });
            return;
        }
        if (!form.groupCode) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '적용 그룹을 선택해 주세요.' });
            return;
        }
        if (!form.contentCode) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '연동 콘텐츠를 선택해 주세요.' });
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
                    style={{ width: 620, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '스케줄 등록' : '스케줄 수정'}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">스케줄 ID</label>
                                            <input type="text" className="form-control" value={form.schCode || '자동할당'} readOnly
                                                style={{ backgroundColor: '#e9ecef', color: '#6c757d' }} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">스케줄명<span className="text-danger">*</span></label>
                                            <input type="text" id="schName" name="schName" className="form-control"
                                                value={form.schName}
                                                onChange={(e) => updateForm({ schName: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">시작일<span className="text-danger">*</span></label>
                                            <input type="date" id="schStartDay" name="schStartDay" className="form-control"
                                                value={dashify(form.schStartDay)}
                                                onChange={(e) => updateForm({ schStartDay: stripDash(e.target.value) })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">종료일<span className="text-danger">*</span></label>
                                            <input type="date" id="schEndDay" name="schEndDay" className="form-control"
                                                value={dashify(form.schEndDay)}
                                                onChange={(e) => updateForm({ schEndDay: stripDash(e.target.value) })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">적용 그룹<span className="text-danger">*</span></label>
                                            <div className="input-group">
                                                <input type="text" placeholder="그룹명 검색" style={{ maxWidth: 160 }}
                                                    value={groupKeyword} onChange={(e) => setGroupKeyword(e.target.value)}
                                                    onKeyDown={(e) => { if (e.key === 'Enter') onGroupSearch(groupKeyword); }} />
                                                <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                                                    onClick={() => onGroupSearch(groupKeyword)}>검색</button>
                                                <select className="form-select" value={form.groupCode}
                                                    onChange={(e) => updateForm({ groupCode: e.target.value })}>
                                                    <option value="">--선택하세요--</option>
                                                    {groupOptions.map((g) => (
                                                        <option key={g.groupCode} value={g.groupCode}>{g.groupNm}</option>
                                                    ))}
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">연동 콘텐츠<span className="text-danger">*</span></label>
                                            <div className="input-group">
                                                <input type="text" placeholder="콘텐츠명 검색" style={{ maxWidth: 160 }}
                                                    value={contentKeyword} onChange={(e) => setContentKeyword(e.target.value)}
                                                    onKeyDown={(e) => { if (e.key === 'Enter') onContentSearch(contentKeyword); }} />
                                                <button type="button" className="btn btn-outline-secondary btn-outline__gray"
                                                    onClick={() => onContentSearch(contentKeyword)}>검색</button>
                                                <select className="form-select" value={form.contentCode}
                                                    onChange={(e) => updateForm({ contentCode: e.target.value })}>
                                                    <option value="">--선택하세요--</option>
                                                    {contentOptions.map((c) => (
                                                        <option key={c.conSeq} value={c.conSeq}>{c.conNm}</option>
                                                    ))}
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">긴급 송출</label>
                                            <div className="input-group align-items-center" style={{ gap: 12 }}>
                                                <label><input type="radio" name="schEmerGubun" checked={form.schEmerGubun === 'Y'}
                                                    onChange={() => updateForm({ schEmerGubun: 'Y' })} /> 적용</label>
                                                <label><input type="radio" name="schEmerGubun" checked={form.schEmerGubun !== 'Y'}
                                                    onChange={() => updateForm({ schEmerGubun: 'N' })} /> 일반</label>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">사용유무</label>
                                            <div className="input-group align-items-center" style={{ gap: 12 }}>
                                                <label><input type="radio" name="schUseYn" checked={form.schUseYn !== 'N'}
                                                    onChange={() => updateForm({ schUseYn: 'Y' })} /> 사용</label>
                                                <label><input type="radio" name="schUseYn" checked={form.schUseYn === 'N'}
                                                    onChange={() => updateForm({ schUseYn: 'N' })} /> 사용안함</label>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                {form.mode !== 'Ins' && (
                                    <button type="button" className="btn btn-outline-danger btn-outline__gray"
                                        style={{ marginRight: 'auto' }}
                                        onClick={onDelete}>삭제</button>
                                )}
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

export default ScheduleFormModal;
