import { useCallback, useEffect, useMemo } from 'react';
import { useDropzone } from 'react-dropzone';

// 갤러리 썸네일 1개 — 이미지는 img, 영상은 첫 프레임(video), 그 외(음원 등)는 확장자 박스로 표시
// (영상/음원을 img로 그리면 깨진 이미지 + alt 파일명이 박스 밖으로 넘쳐 겹쳐 보임)
const renderGalleryThumb = (file, url) => {
    const style = { width: '100%', height: '100%', objectFit: 'cover', borderRadius: 4, border: '1px solid rgba(128,128,128,0.2)' };
    if (file.type?.startsWith('image/')) {
        return <img src={url} alt={file.name} style={style} />;
    }
    if (file.type?.startsWith('video/')) {
        // #t=0.1 — 0초 프레임이 비어 보이는 브라우저가 있어 살짝 뒤 프레임을 표시
        return <video src={`${url}#t=0.1`} muted preload="metadata" style={{ ...style, background: '#000' }} />;
    }
    const ext = file.name.includes('.') ? file.name.split('.').pop().toUpperCase() : 'FILE';
    return (
        <div style={{ ...style, display: 'flex', alignItems: 'center', justifyContent: 'center',
            background: '#f1f5f9', color: '#64748b', fontSize: 11, fontWeight: 600 }}>
            {ext}
        </div>
    );
};

// Drag and Drop File Input
export const useFileUpload = ({
    fieldName,           // 파일 필드명
    updateForm,          // 부모의 updateForm 함수
    fileValue,           // 현재 파일 값 (단일: File | null, 멀티: File[])
    accept,              // 허용 파일 타입
    multiUse = false,    // true: 다중 파일 누적, false: 단일 파일
}) => {
    const onDrop = useCallback((acceptedFiles) => {
        if (!acceptedFiles || acceptedFiles.length === 0) return;

        if (multiUse) {
            // 멀티: 기존 파일 배열에 추가
            const prev = Array.isArray(fileValue) ? fileValue : [];
            updateForm({ [fieldName]: [...prev, ...acceptedFiles] });
        } else {
            // 단일: 첫 번째 파일만 전달
            updateForm({ [fieldName]: acceptedFiles[0] });
        }
    }, [fieldName, updateForm, multiUse, fileValue]);

    // accept를 넘기지 않으면 react-dropzone 기본 동작대로 파일 종류 제한 없음.
    // (과거엔 여기서 엑셀/CSV 전용 기본값을 강제했는데, 게시판 첨부파일처럼 임의 파일을 받아야 하는
    //  곳에서 accept를 깜빡하면 조용히 모든 비-엑셀 파일이 걸러지는 버그가 있었음 — 제한이 필요한
    //  호출부는 반드시 accept를 명시적으로 넘기도록 통일)
    const { getRootProps, getInputProps, isDragActive } = useDropzone({
        onDrop,
        accept,
        multiple: multiUse,
    });

    // 단일 파일 제거
    const clearFile = (e) => {
        e.stopPropagation();
        updateForm({ [fieldName]: multiUse ? [] : null });
    };

    // 멀티 파일에서 특정 인덱스 제거
    const removeFileAt = useCallback((e, index) => {
        e.stopPropagation();
        if (!multiUse || !Array.isArray(fileValue)) return;
        const next = fileValue.filter((_, i) => i !== index);
        updateForm({ [fieldName]: next });
    }, [fieldName, updateForm, multiUse, fileValue]);

    // 멀티 파일 미리보기 URL — 파일 배열이 바뀔 때만 재생성, 언마운트/변경 시 해제 (multiUse 전용)
    const galleryPreviews = useMemo(() => {
        if (!multiUse) return [];
        return (Array.isArray(fileValue) ? fileValue : []).map((file) => ({ file, url: window.URL.createObjectURL(file) }));
    }, [multiUse, fileValue]);

    useEffect(() => {
        return () => { galleryPreviews.forEach((p) => window.URL.revokeObjectURL(p.url)); };
    }, [galleryPreviews]);

    // 갤러리형 드롭존 렌더러 (multiUse 전용) — 드롭존 + 가로 스크롤 썸네일 목록 + 개별 삭제(✕) 버튼
    // 옵션: label(라벨 텍스트), dragText(안내 문구), minHeight(드롭존 높이), width(드롭존 너비),
    //       thumbSize(썸네일 크기), gap(썸네일 간격)
    const renderGalleryDropzone = useCallback(({
        label,
        dragText = '클릭하거나 파일을 드래그하세요 (여러 장 선택 가능)',
        minHeight = 80,
        width,
        thumbSize = 56,
        gap = 8,
    } = {}) => (
        <div className="input-box">
            {label && <label className="form-label">{label}</label>}
            <div
                {...getRootProps()}
                className={`dropzone-box${isDragActive ? ' active' : ''}${galleryPreviews.length ? ' has-file' : ''}`}
                style={{ minHeight, width }}
            >
                <input {...getInputProps()} />
                <div className="placeholder-content">
                    <div style={{ fontSize: 12, color: 'inherit', opacity: 0.6 }}>
                        {isDragActive ? '여기에 놓으세요' : dragText}
                    </div>
                </div>
            </div>
            {galleryPreviews.length > 0 && (
                <div style={{ display: 'flex', flexWrap: 'nowrap', gap, marginTop: 10, overflowX: 'auto', paddingTop: 6, paddingBottom: 4, width }}>
                    {galleryPreviews.map(({ file, url }, idx) => (
                        <div key={`${file.name}-${idx}`} title={file.name}
                            style={{ position: 'relative', width: thumbSize, height: thumbSize, flexShrink: 0 }}>
                            {renderGalleryThumb(file, url)}
                            <button
                                type="button"
                                className="btn btn-sm btn-outline-danger"
                                style={{ position: 'absolute', top: -6, right: -6, borderRadius: '50%', width: 18, height: 18, padding: 0, fontSize: 9, lineHeight: 1 }}
                                onClick={(e) => removeFileAt(e, idx)}
                            >✕</button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    ), [getRootProps, getInputProps, isDragActive, galleryPreviews, removeFileAt]);

    return {
        getRootProps,
        getInputProps,
        isDragActive,
        file: fileValue,
        clearFile,
        removeFileAt,
        renderGalleryDropzone,
    };
};
