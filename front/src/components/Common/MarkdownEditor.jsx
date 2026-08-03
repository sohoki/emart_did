import React, { useCallback } from 'react';
import MDEditor from '@uiw/react-md-editor';
import '@uiw/react-md-editor/markdown-editor.css';

/**
 * 공통 마크다운 에디터 컴포넌트 (react-md-editor 래퍼)
 *
 * Props:
 *   value        — 현재 마크다운 문자열
 *   onChange     — (value: string) => void
 *   height       — 에디터 높이 (기본: 300)
 *   preview      — 'live' | 'edit' | 'preview' (기본: 'live')
 *   placeholder  — placeholder 텍스트
 *   readOnly     — true면 뷰어(preview) 모드로 고정
 *   hideToolbar  — true면 툴바 숨김
 *   label        — 상단 라벨 텍스트
 *   required     — 라벨 * 표시
 *   id           — input id (라벨 htmlFor 연결)
 */
const MarkdownEditor = ({
    value = '',
    onChange,
    height = 300,
    preview = 'live',
    placeholder = '내용을 입력해주세요.',
    readOnly = false,
    hideToolbar = false,
    label,
    required = false,
    id,
    ...rest
}) => {
    const handleChange = useCallback((val) => {
        if (onChange) onChange(val ?? '');
    }, [onChange]);

    return (
        <div className="markdown-editor-wrap" data-color-mode="light">
            {label && (
                <label
                    htmlFor={id}
                    className="form-label"
                    style={{ display: 'block', marginBottom: '4px' }}
                >
                    {label}
                    {required && <span className="text-danger ms-1">*</span>}
                </label>
            )}
            <MDEditor
                id={id}
                value={value}
                onChange={handleChange}
                height={height}
                preview={readOnly ? 'preview' : preview}
                hideToolbar={readOnly || hideToolbar}
                visibleDragbar={false}
                textareaProps={{ placeholder }}
                {...rest}
            />
        </div>
    );
};

export default MarkdownEditor;
