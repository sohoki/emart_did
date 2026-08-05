import { useCallback, useEffect, useState, Suspense, lazy } from 'react';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';
import '@/style/Modal.css';

const DidSendMessageFormModal = lazy(() => import('./DidSendMessageFormModal.jsx'));

// 레거시 didSendMessage.jsp — 좌측 그룹 목록에서 그룹을 고르면 우측에 그룹 내 단말기가
// 체크박스와 함께 표시되고, 체크 후 "메시지 등록"을 누르면 별도 팝업(현재는 중첩 모달)에서
// 메시지 내용/기간을 입력해 전송한다.
const DidSendMessageModal = ({ open, onClose, onSent }) => {
    const [groupList, setGroupList] = useState([]);
    const [selectedGroupCode, setSelectedGroupCode] = useState('');
    const [didList, setDidList] = useState([]);
    const [checkedDidIds, setCheckedDidIds] = useState([]);
    const [formModalOpen, setFormModalOpen] = useState(false);

    useEffect(() => {
        if (!open) return;
        (async () => {
            const res = await fnAjaxFetch({
                url: URL.DID_GROUP_LIST, method: 'POST',
                data: { searchCondition: '', searchKeyword: '', pageIndex: '1', pageUnit: '100' },
                showLoading: false,
            });
            setGroupList(res?.data?.result?.resultList || []);
        })();
    }, [open]);

    const handleSelectGroup = useCallback(async (groupCode) => {
        setSelectedGroupCode(groupCode);
        setCheckedDidIds([]);
        const res = await fnAjaxFetch({
            url: `${URL.DID_GROUP_MEMBER_LIST}/${encodeURIComponent(groupCode)}.do`,
            method: 'GET', showLoading: false,
        });
        setDidList(res?.data?.result?.didLst || []);
    }, []);

    const toggleDidChecked = useCallback((didId) => {
        setCheckedDidIds((prev) => (
            prev.includes(didId) ? prev.filter((id) => id !== didId) : [...prev, didId]
        ));
    }, []);

    const toggleCheckAll = useCallback((checked) => {
        setCheckedDidIds(checked ? didList.map((d) => d.didId) : []);
    }, [didList]);

    const handleOpenFormModal = useCallback(async () => {
        if (!selectedGroupCode) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '그룹을 먼저 선택해 주세요.' });
            return;
        }
        if (checkedDidIds.length === 0) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '메시지를 보낼 단말기를 하나 이상 선택해 주세요.' });
            return;
        }
        setFormModalOpen(true);
    }, [selectedGroupCode, checkedDidIds]);

    if (!open) return null;

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable"
                    style={{ width: 860, maxWidth: '95%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">메시지 등록 — 그룹/단말기 선택</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active" style={{ display: 'flex', gap: 12 }}>
                                <div style={{ flex: 1, border: '1px solid #dde2eb', borderRadius: 4 }}>
                                    <div style={{ fontWeight: 'bold', padding: '6px 12px', fontSize: 13, backgroundColor: '#f8f9fa', borderBottom: '1px solid #dde2eb' }}>
                                        그룹 목록
                                    </div>
                                    <div style={{ height: 360, overflowY: 'auto' }}>
                                        <table style={{ width: '100%', fontSize: 13 }}>
                                            <thead>
                                                <tr>
                                                    <th style={{ padding: '6px 8px', textAlign: 'left' }}>그룹코드</th>
                                                    <th style={{ padding: '6px 8px', textAlign: 'left' }}>그룹명</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {groupList.map((g) => (
                                                    <tr key={g.groupCode}
                                                        onClick={() => handleSelectGroup(g.groupCode)}
                                                        style={{
                                                            cursor: 'pointer',
                                                            backgroundColor: g.groupCode === selectedGroupCode ? '#e6f1fb' : 'transparent',
                                                        }}
                                                    >
                                                        <td style={{ padding: '6px 8px' }}>{g.groupCode}</td>
                                                        <td style={{ padding: '6px 8px' }}>{g.groupNm}</td>
                                                    </tr>
                                                ))}
                                                {groupList.length === 0 && (
                                                    <tr><td colSpan={2} style={{ padding: 12, textAlign: 'center', color: '#94a3b8' }}>등록된 그룹이 없습니다.</td></tr>
                                                )}
                                            </tbody>
                                        </table>
                                    </div>
                                </div>

                                <div style={{ flex: 1, border: '1px solid #dde2eb', borderRadius: 4 }}>
                                    <div style={{ fontWeight: 'bold', padding: '6px 12px', fontSize: 13, backgroundColor: '#f8f9fa', borderBottom: '1px solid #dde2eb' }}>
                                        그룹 내 단말기 목록{selectedGroupCode ? ` — ${selectedGroupCode}` : ''}
                                    </div>
                                    <div style={{ height: 360, overflowY: 'auto' }}>
                                        <table style={{ width: '100%', fontSize: 13 }}>
                                            <thead>
                                                <tr>
                                                    <th style={{ padding: '6px 8px' }}>
                                                        <input type="checkbox"
                                                            checked={didList.length > 0 && checkedDidIds.length === didList.length}
                                                            onChange={(e) => toggleCheckAll(e.target.checked)}
                                                        />
                                                    </th>
                                                    <th style={{ padding: '6px 8px', textAlign: 'left' }}>단말ID</th>
                                                    <th style={{ padding: '6px 8px', textAlign: 'left' }}>단말명</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {didList.map((d) => (
                                                    <tr key={d.didId}>
                                                        <td style={{ padding: '6px 8px' }}>
                                                            <input type="checkbox"
                                                                checked={checkedDidIds.includes(d.didId)}
                                                                onChange={() => toggleDidChecked(d.didId)}
                                                            />
                                                        </td>
                                                        <td style={{ padding: '6px 8px' }}>{d.didId}</td>
                                                        <td style={{ padding: '6px 8px' }}>{d.didNm}</td>
                                                    </tr>
                                                ))}
                                                {selectedGroupCode && didList.length === 0 && (
                                                    <tr><td colSpan={3} style={{ padding: 12, textAlign: 'center', color: '#94a3b8' }}>그룹에 등록된 단말기가 없습니다.</td></tr>
                                                )}
                                                {!selectedGroupCode && (
                                                    <tr><td colSpan={3} style={{ padding: 12, textAlign: 'center', color: '#94a3b8' }}>좌측에서 그룹을 선택하세요.</td></tr>
                                                )}
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <span style={{ marginRight: 'auto', alignSelf: 'center', color: '#64748b' }}>
                                    선택된 단말기 {checkedDidIds.length}개
                                </span>
                                <button type="button" className="btn btn-action__lightblue" onClick={onClose}>취소</button>
                                <button type="button" className="btn btn-primary btn-action__blue" onClick={handleOpenFormModal}>메시지 등록</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <Suspense fallback={null}>
                {formModalOpen && (
                    <DidSendMessageFormModal
                        open={formModalOpen}
                        groupCode={selectedGroupCode}
                        didIds={checkedDidIds}
                        onClose={() => setFormModalOpen(false)}
                        onSubmitted={() => { setFormModalOpen(false); onSent?.(); }}
                    />
                )}
            </Suspense>
        </>
    );
};

export default DidSendMessageModal;
