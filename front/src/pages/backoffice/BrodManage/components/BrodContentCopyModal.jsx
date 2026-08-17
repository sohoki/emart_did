import { useCallback, useState } from 'react';
import Swal from '@/lib/swal.js';
import '@/style/Modal.css';

const stripDash = (v) => (v || '').replace(/-/g, '');
const dashify = (v) => (v && v.length === 8 ? `${v.slice(0, 4)}-${v.slice(4, 6)}-${v.slice(6, 8)}` : '');

// 음원 콘텐츠(편성) 복사 모달 — 레거시 brodContentView.jsp "음원 콘텐츠 복사"
// (별도 팝업창 brodContentDetailCopy.jsp)를 참고. 다른 방송의 편성을 그대로 가져와
// 현재 방송의 기존 편성을 대체한다.
const BrodContentCopyModal = ({ open, combo, onClose, onSubmit }) => {
    const [prebrodCode, setPrebrodCode] = useState('');
    const [contentStartDay, setContentStartDay] = useState('');
    const [contentEndDay, setContentEndDay] = useState('');

    const handleSubmit = useCallback(async () => {
        if (!prebrodCode) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '복사할 방송을 선택해 주세요.' });
            return;
        }
        if (!contentStartDay || !contentEndDay) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '적용 기간을 입력해 주세요.' });
            return;
        }
        const result = await Swal.fire({
            icon: 'warning', title: '음원 콘텐츠 복사',
            text: '현재 방송의 기존 편성은 모두 삭제되고 선택한 방송의 편성으로 대체됩니다. 계속하시겠습니까?',
            showCancelButton: true, confirmButtonText: '복사', cancelButtonText: '취소',
        });
        if (!result.isConfirmed) return;
        onSubmit({ prebrodCode, contentStartDay, contentEndDay });
    }, [prebrodCode, contentStartDay, contentEndDay, onSubmit]);

    if (!open) return null;

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered"
                    style={{ width: 480, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">음원 콘텐츠 복사</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">
                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        <div className="input-box">
                                            <label className="form-label">복사할 방송<span className="text-danger">*</span></label>
                                            <select className="form-select" value={prebrodCode}
                                                onChange={(e) => setPrebrodCode(e.target.value)}>
                                                <option value="">선택</option>
                                                {combo.map((c) => (
                                                    <option key={c.brodCode} value={c.brodCode}>{c.brodName}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>
                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">적용 시작일<span className="text-danger">*</span></label>
                                            <input type="date" className="form-control"
                                                value={dashify(contentStartDay)}
                                                onChange={(e) => setContentStartDay(stripDash(e.target.value))} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">적용 종료일<span className="text-danger">*</span></label>
                                            <input type="date" className="form-control"
                                                value={dashify(contentEndDay)}
                                                onChange={(e) => setContentEndDay(stripDash(e.target.value))} />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-cancel" onClick={onClose}>취소</button>
                                <button type="button" className="btn btn-primary btn-action__blue" onClick={handleSubmit}>복사</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default BrodContentCopyModal;
