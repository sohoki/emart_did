import '@/style/Modal.css';

// 전문(명령) 미리보기 모달 — 레거시에서는 window.open 팝업이었지만 페이지 이동 없이 모달로 표시.
// 백엔드의 preview/json, preview/xml 엔드포인트는 둘 다 "입력 파라미터(xmlInputParam +
// xmlInputParamSample)" 기준으로 JSON/XML 형식만 다르게 만들어주는 것이라(출력 파라미터
// 기준 미리보기는 백엔드에 없음), 레거시의 "요청전문/결과전문" 구분 대신 "JSON 미리보기/
// XML 미리보기"로 표기한다.
const XmlPreviewModal = ({ open, title, content, onClose }) => {
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
                                <h2 className="modal-title__title">{title}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">
                                <pre style={{
                                    background: '#f8fafc', padding: 12, borderRadius: 4,
                                    whiteSpace: 'pre-wrap', wordBreak: 'break-all', fontSize: 13,
                                    minHeight: 200,
                                }}>
                                    {content || '미리보기 내용이 없습니다.'}
                                </pre>
                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-action__lightblue" onClick={onClose}>닫기</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default XmlPreviewModal;
