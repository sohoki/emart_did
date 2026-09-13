import '@/style/Modal.css';

// 방송 편성표 보기 모달 — 레거시 brodContentView.jsp "방송표보기"(팝업창 brodPageLst.jsp) 참고.
// ContentBrodConfirm.do(편성표생성)로 확정된 TB_BRODORGANIZATION 결과를 시간순으로 보여준다.
// 레거시의 "EXCEL DOWN" 버튼(ContentBrodExcel.do)은 did_emart 백엔드에 아직 없어 이번 범위에서 제외.
const BrodContentOrganizationModal = ({ open, brodName, list, onClose }) => {
    if (!open) return null;

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered"
                    style={{ width: 560, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">방송 편성표{brodName ? ` - ${brodName}` : ''}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active" style={{ maxHeight: 480, overflowY: 'auto' }}>
                                <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse' }}>
                                    <thead>
                                        <tr style={{ background: '#f8fafc' }}>
                                            <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 100 }}>재생시간</th>
                                            <th style={{ border: '1px solid #e2e8f0', padding: 8 }}>재생콘텐츠</th>
                                            <th style={{ border: '1px solid #e2e8f0', padding: 8, width: 90 }}>구분</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {list.map((item, idx) => (
                                            <tr key={`${item.brodTime}-${idx}`}>
                                                <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>{item.brodTime}</td>
                                                <td style={{ border: '1px solid #e2e8f0', padding: 8, textAlign: 'left' }}>{item.orignlFileNm}</td>
                                                <td style={{ border: '1px solid #e2e8f0', padding: 8 }}>{item.brodSeq === '0' ? '특정방송' : '일반방송'}</td>
                                            </tr>
                                        ))}
                                        {list.length === 0 && (
                                            <tr><td colSpan={3} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>편성표가 없습니다. 먼저 "편성표생성"을 실행해 주세요.</td></tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-cancel" onClick={onClose}>닫기</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default BrodContentOrganizationModal;
