import { useNavigate } from 'react-router-dom';
import '@/style/Modal.css';

// 파일 삭제 시 해당 파일을 사용 중인 콘텐츠 목록 모달 — 항목을 클릭하면 그 콘텐츠의
// 편성 화면(ContentDetailEditorPage)으로 이동해서 파일을 먼저 제외할 수 있게 한다.
const ContentFileConnModal = ({ open, fileName, connList = [], onClose }) => {
    const navigate = useNavigate();

    if (!open) return null;

    const handleMove = (conSeq) => {
        onClose?.();
        navigate(`/backoffice/sub/conManage/muti/edit?conSeq=${conSeq}`);
    };

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable"
                    style={{ width: 520, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">삭제 불가 — 사용 중인 콘텐츠</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">
                                <div style={{ marginBottom: 10, fontSize: 13, color: '#334155' }}>
                                    <b>{fileName}</b> 파일이 아래 {connList.length}개 콘텐츠에서 사용 중입니다.<br />
                                    콘텐츠를 클릭하면 편성 화면으로 이동합니다. 파일을 제외한 뒤 다시 삭제해 주세요.
                                </div>
                                <ul style={{ listStyle: 'none', padding: 0, margin: 0, maxHeight: 360, overflowY: 'auto' }}>
                                    {connList.map((c) => (
                                        <li key={c.conSeq}>
                                            <button type="button" onClick={() => handleMove(c.conSeq)}
                                                style={{
                                                    width: '100%', display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                                                    padding: '10px 12px', marginBottom: 6, fontSize: 13, textAlign: 'left',
                                                    border: '1px solid #cbd5e1', borderRadius: 6, background: '#fff', cursor: 'pointer',
                                                }}>
                                                <span>{c.conNm}</span>
                                                <span style={{ color: '#64748b', whiteSpace: 'nowrap' }}>
                                                    {c.connCount}건 배치 ›
                                                </span>
                                            </button>
                                        </li>
                                    ))}
                                </ul>
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

export default ContentFileConnModal;
