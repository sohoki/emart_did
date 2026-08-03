import { useEffect } from 'react';
import { useLexicalComposerContext } from '@lexical/react/LexicalComposerContext';
import { INSERT_IMAGE_COMMAND } from './ImagePlugin.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import config from '@/config/index.jsx';

/**
 * 에디터 영역에 이미지를 드래그 & 드롭하면 자동으로 삽입하는 플러그인.
 *
 * Props:
 *   imageUploadUrl — 서버 업로드 엔드포인트 (없으면 base64 사용)
 */
export function DragDropPlugin({ imageUploadUrl }) {
    const [editor] = useLexicalComposerContext();

    useEffect(() => {
        const root = editor.getRootElement();
        if (!root) return;

        const onDragOver = (e) => {
            const types = e.dataTransfer?.types || [];
            if (types.includes('Files')) {
                e.preventDefault();
                e.dataTransfer.dropEffect = 'copy';
            }
        };

        const onDrop = async (e) => {
            const files = Array.from(e.dataTransfer?.files || [])
                .filter((f) => f.type.startsWith('image/'));
            if (files.length === 0) return;

            e.preventDefault();
            e.stopPropagation();

            for (const file of files) {
                try {
                    let src;
                    if (imageUploadUrl) {
                        const fd = new FormData();
                        fd.append('file', file);
                        const res = await fnAjaxFetch({
                            url: imageUploadUrl,
                            method: 'POST',
                            data: fd,
                            headers: { 'Content-Type': 'multipart/form-data' },
                        });
                        const json = res?.data;
                        if (json?.resultCodeInfo !== 'SUCCESS') continue;
                        const path = json?.result?.result || json?.result?.filePath || json?.result;
                        src = `${config.REACT_APP_IMG_URL || ''}${path}`;
                    } else {
                        src = await readAsBase64(file);
                    }
                    editor.dispatchCommand(INSERT_IMAGE_COMMAND, { src, altText: file.name });
                } catch (err) {
                    console.error('[DragDropPlugin] 이미지 처리 실패:', err);
                }
            }
        };

        root.addEventListener('dragover', onDragOver);
        root.addEventListener('drop', onDrop);
        return () => {
            root.removeEventListener('dragover', onDragOver);
            root.removeEventListener('drop', onDrop);
        };
    }, [editor, imageUploadUrl]);

    return null;
}

function readAsBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload  = (e) => resolve(e.target.result);
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}
