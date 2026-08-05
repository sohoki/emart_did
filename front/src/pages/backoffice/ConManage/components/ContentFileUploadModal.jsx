import { useCallback, useState } from 'react';
import { useFileUpload } from '@/hooks/use-file-upload.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';
import '@/style/Modal.css';
import '@/style/DropZone.css';

const EMPTY_FORM = { files: [] };

// 미디어 파일(이미지/영상/음원) 등록 모달 — 레거시 mediaLst.jsp의 "미디어 파일 등록"
// (별도 팝업창 FileUpload.jsp)를 참고, 페이지 이동 없이 모달 + 다중 드래그앤드롭 업로드로 대체.
const ContentFileUploadModal = ({ open, onClose, onUploaded }) => {
    const [form, setForm] = useState(EMPTY_FORM);
    const [uploading, setUploading] = useState(false);

    const updateForm = useCallback((payload) => {
        setForm((prev) => ({ ...prev, ...payload }));
    }, []);

    const { renderGalleryDropzone } = useFileUpload({
        fieldName: 'files',
        updateForm,
        fileValue: form.files,
        multiUse: true,
    });

    const handleSubmit = useCallback(async () => {
        if (!form.files || form.files.length === 0) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '업로드할 파일을 선택해 주세요.' });
            return;
        }

        const formData = new FormData();
        form.files.forEach((file) => formData.append('files', file));

        setUploading(true);
        try {
            const res = await fnAjaxFetch({ url: URL.CON_FILE_UPLOAD, method: 'POST', data: formData });
            const json = res?.data;
            if (json?.resultCodeInfo === 'SUCCESS') {
                const { successCount, totalCount } = json?.result || {};
                await Swal.fire({
                    icon: 'success', title: '완료',
                    text: `${successCount ?? 0}/${totalCount ?? form.files.length}건 업로드되었습니다.`,
                });
                setForm(EMPTY_FORM);
                onUploaded?.();
            } else {
                await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || '업로드 중 오류가 발생했습니다.' });
            }
        } finally {
            setUploading(false);
        }
    }, [form.files, onUploaded]);

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
                                <h2 className="modal-title__title">미디어 파일 등록</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">
                                <div className="row input-box-wrap">
                                    <div className="col-12">
                                        {renderGalleryDropzone({
                                            label: '업로드 파일',
                                            dragText: '이미지/영상/음원 파일을 클릭하거나 끌어놓으세요 (여러 개 선택 가능)',
                                            minHeight: 120,
                                        })}
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-action__lightblue" onClick={onClose}>취소</button>
                                <button type="button" className="btn btn-primary btn-action__blue"
                                    onClick={handleSubmit} disabled={uploading}>
                                    {uploading ? '업로드 중...' : '업로드'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default ContentFileUploadModal;
