import { useCallback } from 'react';
import UseSwitch from '@/components/Common/IosSwitch.jsx';
import '@/style/Modal.css';

// 레거시 conMutiDetail.jsp의 ViewPage() — 화면타입(conScreen) 선택 시 가로/세로 방향
// 코드(conType)/사이즈/분할사이즈를 자동으로 채워주던 로직을 그대로 재현.
const SCREEN_TYPE_DEFAULTS = {
    W11: { conType: 'DIDTYPE01', conWidth: '1920', conHeight: '1080', conMid: '0' },
    H11: { conType: 'DIDTYPE02', conWidth: '1080', conHeight: '1920', conMid: '0' },
    W21: { conType: 'DIDTYPE01', conWidth: '1920', conHeight: '1080', conMid: '960' },
    H21: { conType: 'DIDTYPE02', conWidth: '1080', conHeight: '1920', conMid: '960' },
};

// 화면 구성(멀티페이지 콘텐츠) 등록/수정 모달 — 레거시 conMutiDetail.jsp 참고.
// "편성"(페이지별 파일 배치)은 별도 대형 편집 화면(ContentDetailEditorPage)에서 그대로 진행하며
// 이 모달은 화면(콘텐츠) 자체의 기본 속성만 다룬다.
const ContentMutiFormModal = ({
    open,
    form,
    setForm,
    conTypeOptions,
    screenTypeOptions,
    playTypeOptions,
    nextSeqOptions,
    onClose,
    onSubmit,
}) => {
    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, [setForm]);

    const handleScreenChange = useCallback((conScreen) => {
        const defaults = SCREEN_TYPE_DEFAULTS[conScreen];
        updateForm(defaults ? { conScreen, ...defaults } : { conScreen });
    }, [updateForm]);

    if (!open) return null;

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable"
                    style={{ width: 700, maxWidth: '90%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">{form.mode === 'Ins' ? '화면 구성 등록' : '화면 구성 수정'}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">화면명<span className="text-danger">*</span></label>
                                            <input type="text" id="conNm" name="conNm" className="form-control"
                                                value={form.conNm}
                                                onChange={(e) => updateForm({ conNm: e.target.value })} />
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">화면타입<span className="text-danger">*</span></label>
                                            <select id="conScreen" name="conScreen" className="form-select"
                                                value={form.conScreen} onChange={(e) => handleScreenChange(e.target.value)}>
                                                <option value="">선택</option>
                                                {screenTypeOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">가로/세로</label>
                                            <select id="conType" name="conType" className="form-select"
                                                value={form.conType} onChange={(e) => updateForm({ conType: e.target.value })}>
                                                <option value="">선택</option>
                                                {conTypeOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">다음시퀀스</label>
                                            <select id="conNextSeq" name="conNextSeq" className="form-select"
                                                value={form.conNextSeq} onChange={(e) => updateForm({ conNextSeq: e.target.value })}>
                                                <option value="">선택</option>
                                                {nextSeqOptions.map((o) => (
                                                    <option key={o.conSeq} value={o.conSeq}>{o.conNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">화면 사이즈(px)</label>
                                            <div className="input-group">
                                                <input type="text" id="conWidth" name="conWidth" className="form-control" placeholder="가로"
                                                    value={form.conWidth}
                                                    onChange={(e) => updateForm({ conWidth: e.target.value })} />
                                                <input type="text" id="conHeight" name="conHeight" className="form-control" placeholder="세로"
                                                    value={form.conHeight}
                                                    onChange={(e) => updateForm({ conHeight: e.target.value })} />
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">분할사이즈(px)<span style={{ fontSize: 12 }}>(가로: 좌측, 세로: 상단 기준)</span></label>
                                            <input type="text" id="conMid" name="conMid" className="form-control"
                                                value={form.conMid}
                                                onChange={(e) => updateForm({ conMid: e.target.value })} />
                                        </div>
                                    </div>
                                </div>

                                <div className="row input-box-wrap">
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">사용유무</label>
                                            <div className="input-group align-items-center">
                                                <UseSwitch
                                                    value={form.conUseYn}
                                                    name="conUseYn"
                                                    onChange={updateForm}
                                                    onText="사용"
                                                    offText="사용안함"
                                                />
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-6">
                                        <div className="input-box">
                                            <label className="form-label">분할재생기준</label>
                                            <select id="conPlayType" name="conPlayType" className="form-select"
                                                value={form.conPlayType} onChange={(e) => updateForm({ conPlayType: e.target.value })}>
                                                <option value="">선택</option>
                                                {playTypeOptions.map((o) => (
                                                    <option key={o.code} value={o.code}>{o.codeNm}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-action__lightblue" onClick={onClose}>취소</button>
                                <button type="button" className="btn btn-primary btn-action__blue" onClick={onSubmit}>
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

export default ContentMutiFormModal;
