import { useCallback, useEffect, useState } from 'react';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import config from '@/config/index.jsx';
import URL from '@/constants/URL.jsx';
import ContentPickPanel from './ContentPickPanel.jsx';
import '@/style/Modal.css';

const EMPTY_FORM = { fileWidth: '', fileHeight: '', playTime: '' };

const fileSrc = (f, name = f?.streFileNm) => `${config.REACT_APP_IMG_URL}${(f?.fileStreCours || '').replace(/^\//, '')}${name || ''}`;

// 미리보기 상태별 안내 문구 — 브라우저가 메타데이터를 읽어야 자동 입력이 가능하다
const PREVIEW_STATUS_MSG = {
    loading: { color: '#64748b', text: '* 미리보기를 불러오는 중입니다...' },
    loaded: { color: '#16a34a', text: '* 브라우저가 읽은 실제 파일 기준으로 가로/세로/재생시간을 자동 입력했습니다. 저장해야 반영됩니다.' },
    error: {
        color: '#dc2626',
        text: '* 브라우저에서 재생할 수 없는 파일입니다(avi/mpeg 등 미지원 코덱 또는 파일 없음). 저장된 값을 표시하며 직접 입력할 수 있습니다.',
    },
};

const formatFileSize = (size) => {
    const n = Number(size);
    if (!n) return '-';
    if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`;
    return `${(n / 1024 / 1024).toFixed(1)} MB`;
};

const TH_STYLE = { width: 110, padding: '8px 12px', background: '#f1f5f9', border: '1px solid #cbd5e1', fontWeight: 600, whiteSpace: 'nowrap' };
const TD_STYLE = { padding: '8px 12px', border: '1px solid #cbd5e1', wordBreak: 'break-all' };

// 콘텐츠 파일 상세정보 모달 — 레거시 fileView.do(파일 미리보기 + 상세정보 저장)를 대체.
// 미리보기(img/video/audio)가 로드되면 브라우저에서 측정한 가로/세로/재생시간을 폼에 채우고,
// 저장 시 /detailUpdate.do로 LETTNFILEDETAIL의 FILE_WIDTH/FILE_HEIGHT/PLAY_TIME을 갱신한다.
// mediaType은 상세 조회 쿼리가 음원(MUSIC)을 판별하지 않아 목록에서 넘겨받은 값을 쓴다.
// "콘텐츠 검색" 버튼으로 우측 패널(ContentPickPanel)을 열어 체크한 화면들의 1페이지에 이 파일을 추가할 수 있다.
const ContentFileDetailModal = ({ open, atchFileId, mediaType, onClose, onSaved }) => {
    const [detail, setDetail] = useState(null);
    const [form, setForm] = useState(EMPTY_FORM);
    const [saving, setSaving] = useState(false);
    const [pickOpen, setPickOpen] = useState(false);
    const [previewStatus, setPreviewStatus] = useState('loading');

    useEffect(() => {
        if (!open || !atchFileId) return;
        (async () => {
            const res = await fnAjaxFetch({ url: `${URL.CON_FILE_INFO}/${atchFileId}.do`, method: 'GET' });
            // ResultHelper.setSuccess(resultVO, detail, "result") → { result: { result: detail } } 구조
            const result = res?.data?.result?.result;
            setPreviewStatus('loading');
            setDetail(result ?? null);
            setForm({
                fileWidth: result?.fileWidth ?? '',
                fileHeight: result?.fileHeight ?? '',
                playTime: result?.playTime ?? '',
            });
        })();
    }, [open, atchFileId]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    // 미리보기 로드 시 실제 파일 기준 값으로 자동 채움
    const handleImageLoad = (e) => {
        const { naturalWidth, naturalHeight } = e.currentTarget;
        setForm((prev) => ({ ...prev, fileWidth: String(naturalWidth), fileHeight: String(naturalHeight) }));
        setPreviewStatus('loaded');
    };

    // 영상은 videoWidth/videoHeight/duration, 음원은 duration만 채운다(값이 없으면 기존 값 유지)
    const handleMediaLoad = (e) => {
        const { videoWidth, videoHeight, duration } = e.currentTarget;
        setForm((prev) => ({
            ...prev,
            ...(videoWidth ? { fileWidth: String(videoWidth), fileHeight: String(videoHeight) } : {}),
            ...(Number.isFinite(duration) ? { playTime: String(Math.floor(duration)) } : {}),
        }));
        setPreviewStatus('loaded');
    };

    const handlePreviewError = () => setPreviewStatus('error');

    const handleSave = useCallback(async () => {
        setSaving(true);
        try {
            const res = await fnAjaxFetch({
                url: `${URL.CON_FILE_INFO}/detailUpdate.do`, method: 'POST',
                data: { atchFileId, ...form },
            });
            if (res?.data?.resultCodeInfo === 'SUCCESS') {
                await Swal.fire({ icon: 'success', title: '완료', text: '파일 상세정보가 저장되었습니다.' });
                onSaved?.();
            } else {
                await Swal.fire({ icon: 'error', title: '오류', text: res?.data?.resultMessage || '저장 중 오류가 발생했습니다.' });
            }
        } finally {
            setSaving(false);
        }
    }, [atchFileId, form, onSaved]);

    if (!open) return null;

    const type = mediaType || detail?.mediaType;

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable"
                    style={{ width: pickOpen ? 1120 : 720, maxWidth: '95%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">파일 상세정보</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active"
                                style={{ display: 'flex', gap: 16, alignItems: 'stretch' }}>
                                <div style={{ flex: 1, minWidth: 0 }}>
                                {detail ? (
                                    <>
                                        <div style={{ display: 'flex', justifyContent: 'center', background: '#0f172a',
                                            borderRadius: 6, marginBottom: 12, padding: type === 'MUSIC' ? 16 : 0 }}>
                                            {type === 'IMAGE' && (
                                                <img src={fileSrc(detail)} alt={detail.orignlFileNm}
                                                    onLoad={handleImageLoad} onError={handlePreviewError}
                                                    style={{ maxWidth: '100%', maxHeight: 360, objectFit: 'contain' }} />
                                            )}
                                            {/* 메타데이터만 먼저 받아 자동 입력, 재생 전에는 업로드 시 생성한 썸네일을 표시 */}
                                            {type === 'MEDIA' && (
                                                <video src={fileSrc(detail)} controls preload="metadata"
                                                    poster={detail.fileThumnail ? fileSrc(detail, detail.fileThumnail) : undefined}
                                                    onLoadedMetadata={handleMediaLoad} onError={handlePreviewError}
                                                    style={{ width: '100%', maxHeight: 360 }} />
                                            )}
                                            {type === 'MUSIC' && (
                                                <audio src={fileSrc(detail)} controls preload="metadata"
                                                    onLoadedMetadata={handleMediaLoad} onError={handlePreviewError}
                                                    style={{ width: '100%' }} />
                                            )}
                                        </div>

                                        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
                                            <tbody>
                                                <tr>
                                                    <th style={TH_STYLE}>파일명</th>
                                                    <td style={TD_STYLE} colSpan={3}>{detail.orignlFileNm}</td>
                                                </tr>
                                                <tr>
                                                    <th style={TH_STYLE}>파일 ID</th>
                                                    <td style={TD_STYLE}>{detail.atchFileId}</td>
                                                    <th style={TH_STYLE}>확장자 / 크기</th>
                                                    <td style={TD_STYLE}>{detail.fileExtsn} / {formatFileSize(detail.fileSize)}</td>
                                                </tr>
                                                <tr>
                                                    <th style={TH_STYLE}>가로 x 세로</th>
                                                    <td style={TD_STYLE}>
                                                        <input type="text" name="fileWidth" value={form.fileWidth}
                                                            onChange={handleChange} style={{ width: 70, textAlign: 'center' }} />
                                                        {' x '}
                                                        <input type="text" name="fileHeight" value={form.fileHeight}
                                                            onChange={handleChange} style={{ width: 70, textAlign: 'center' }} />
                                                    </td>
                                                    <th style={TH_STYLE}>재생시간(초)</th>
                                                    <td style={TD_STYLE}>
                                                        <input type="text" name="playTime" value={form.playTime}
                                                            onChange={handleChange} style={{ width: 70, textAlign: 'center' }} />
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                        <div style={{ marginTop: 8, fontSize: 12, color: PREVIEW_STATUS_MSG[previewStatus].color }}>
                                            {PREVIEW_STATUS_MSG[previewStatus].text}
                                        </div>
                                    </>
                                ) : (
                                    <div style={{ padding: 24, textAlign: 'center', color: '#94a3b8' }}>조회 중...</div>
                                )}
                                </div>
                                {pickOpen && detail && (
                                    <div style={{ width: 340, flexShrink: 0, borderLeft: '1px solid #e2e8f0', paddingLeft: 16 }}>
                                        <ContentPickPanel file={{ atchFileId, mediaType: type, playTime: form.playTime }} />
                                    </div>
                                )}
                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-outline-dark btn-outline__gray"
                                    onClick={() => setPickOpen((v) => !v)} disabled={!detail}>
                                    {pickOpen ? '콘텐츠 검색 닫기' : '콘텐츠 검색'}
                                </button>
                                <button type="button" className="btn btn-action__lightblue" onClick={onClose}>닫기</button>
                                <button type="button" className="btn btn-primary btn-action__blue"
                                    onClick={handleSave} disabled={saving || !detail}>
                                    {saving ? '저장 중...' : '저장'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default ContentFileDetailModal;
