import { useCallback, useState } from 'react';
import '@/style/Modal.css';

// 매장 배포 시간대 입력 모달 — 기존엔 Swal.fire({html: '<input>...'}) 프롬프트였던
// 것을 다른 목록 화면들과 동일한 모달 패턴으로 교체.
const BrodDeployTimeModal = ({ open, centerNm, onClose, onSubmit }) => {
    const [start, setStart] = useState('0000');
    const [end, setEnd] = useState('2359');

    const handleSubmit = useCallback(() => {
        onSubmit({ start, end });
    }, [start, end, onSubmit]);

    if (!open) return null;

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered"
                    style={{ width: 420, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">{centerNm} 배포 시간대</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">
                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">시작(HHmm)</label>
                                            <input type="text" className="form-control" placeholder="0000"
                                                value={start} onChange={(e) => setStart(e.target.value)} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">종료(HHmm)</label>
                                            <input type="text" className="form-control" placeholder="2359"
                                                value={end} onChange={(e) => setEnd(e.target.value)} />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-cancel" onClick={onClose}>취소</button>
                                <button type="button" className="btn btn-primary btn-action__blue" onClick={handleSubmit}>배포</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default BrodDeployTimeModal;
