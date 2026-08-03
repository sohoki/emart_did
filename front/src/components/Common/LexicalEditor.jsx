import { useCallback, useEffect, useRef, useState } from 'react';
import { LexicalComposer } from '@lexical/react/LexicalComposer';
import { RichTextPlugin } from '@lexical/react/LexicalRichTextPlugin';
import { ContentEditable } from '@lexical/react/LexicalContentEditable';
import { HistoryPlugin } from '@lexical/react/LexicalHistoryPlugin';
import { OnChangePlugin } from '@lexical/react/LexicalOnChangePlugin';
import { ListPlugin } from '@lexical/react/LexicalListPlugin';
import { LinkPlugin } from '@lexical/react/LexicalLinkPlugin';
import { TablePlugin } from '@lexical/react/LexicalTablePlugin';
import { LexicalErrorBoundary } from '@lexical/react/LexicalErrorBoundary';
import { useLexicalComposerContext } from '@lexical/react/LexicalComposerContext';
import {
    $getRoot, $createParagraphNode, $createTextNode, $getSelection,
    FORMAT_TEXT_COMMAND, UNDO_COMMAND, REDO_COMMAND,
} from 'lexical';
import { $patchStyleText, $getSelectionStyleValueForProperty } from '@lexical/selection';
import {
    INSERT_ORDERED_LIST_COMMAND, INSERT_UNORDERED_LIST_COMMAND,
    ListNode, ListItemNode,
} from '@lexical/list';
import {
    TableNode, TableCellNode, TableRowNode, INSERT_TABLE_COMMAND,
} from '@lexical/table';
import { LinkNode, TOGGLE_LINK_COMMAND } from '@lexical/link';
import { HeadingNode, QuoteNode } from '@lexical/rich-text';
import { CodeNode } from '@lexical/code';
import { $generateHtmlFromNodes, $generateNodesFromDOM } from '@lexical/html';

import { ImageNode } from './lexical/ImageNode.jsx';
import { ImagePlugin, INSERT_IMAGE_COMMAND } from './lexical/ImagePlugin.jsx';
import { DragDropPlugin } from './lexical/DragDropPlugin.jsx';
import { TableCellResizer } from './lexical/TableCellResizer.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import config from '@/config/index.jsx';

// ─────────────────────────────────────────────────────────────────────────────
// 초기값 복원 플러그인
// ─────────────────────────────────────────────────────────────────────────────
// Lexical JSON 여부 판별
function isLexicalJson(str) {
    if (!str) return false;
    try { const p = JSON.parse(str); return !!p?.root; } catch { return false; }
}

// root.children이 하나도 없는 "완전히 빈" Lexical JSON인지 판별
// (과거에 내용을 전부 지우고 저장된 문서 등) — Lexical은 root가 자식 노드 없이 비어있는
// EditorState를 setEditorState()에 넣으면 "the editor state is empty" 에러를 던지며 크래시함
function isEmptyLexicalRoot(str) {
    try {
        const p = JSON.parse(str);
        return !Array.isArray(p?.root?.children) || p.root.children.length === 0;
    } catch { return true; }
}

// initialConfig.editorState 에 넣을 값 결정
// 1) 깨끗한 Lexical JSON → 문자열 그대로 전달 (단, root가 비어있으면 undefined — Lexical 기본 빈 상태 사용)
// 2) &quot; 등 entity 인코딩된 Lexical JSON → 디코딩 후 전달 (DB 오염 데이터 복구)
// 3) HTML / 순수 텍스트 → undefined (InitialValuePlugin 에서 처리)
function resolveEditorState(val) {
    if (!val) return undefined;
    if (isLexicalJson(val)) return isEmptyLexicalRoot(val) ? undefined : val;
    // entity 인코딩 여부 확인 후 디코딩
    if (/&[a-z#0-9]+;/i.test(val)) {
        try {
            const txt = document.createElement('textarea');
            txt.innerHTML = val;
            const decoded = txt.value;
            if (isLexicalJson(decoded)) return isEmptyLexicalRoot(decoded) ? undefined : decoded;
        } catch {
            // 디코딩 실패 시 원본 값을 그대로 사용 (아래 return undefined)
        }
    }
    return undefined;
}

// HTML/텍스트 전용 초기값 플러그인 (Lexical JSON은 initialConfig 가 처리)
function InitialValuePlugin({ value }) {
    const [editor] = useLexicalComposerContext();
    const initialized = useRef(false);

    useEffect(() => {
        if (initialized.current || !value || isLexicalJson(value)) return;
        initialized.current = true;

        const trimmed = value.trim();
        const hasEntities = /&[a-z#0-9]+;/i.test(trimmed);

        // HtmlCharacterEscapes로 "→&quot; 치환된 Lexical JSON 복구 시도
        // (백엔드 재시작 전 임시 호환 처리)
        if (hasEntities) {
            const txt = document.createElement('textarea');
            txt.innerHTML = trimmed;
            const decoded = txt.value;
            if (isLexicalJson(decoded)) {
                try {
                    editor.setEditorState(editor.parseEditorState(decoded));
                    return;
                } catch { /* 실패 시 HTML fallback */ }
            }
        }

        const isHtml = trimmed.startsWith('<') || hasEntities;

        editor.update(() => {
            const root = $getRoot();
            root.clear();
            if (isHtml) {
                const txt = document.createElement('textarea');
                txt.innerHTML = trimmed;
                const dom = new DOMParser().parseFromString(txt.value, 'text/html');
                $generateNodesFromDOM(editor, dom).forEach(n => {
                    if (n.getType() === 'text') {
                        const p = $createParagraphNode(); p.append(n); root.append(p);
                    } else { root.append(n); }
                });
            } else {
                const p = $createParagraphNode();
                p.append($createTextNode(value));
                root.append(p);
            }
        });
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return null;
}

// ─────────────────────────────────────────────────────────────────────────────
// HTML 모드 플러그인
// ─────────────────────────────────────────────────────────────────────────────
function HtmlPlugin({ htmlMode, html, onHtmlChange }) {
    const [editor] = useLexicalComposerContext();
    const prevMode = useRef(htmlMode);

    useEffect(() => {
        // Rich → HTML 전환: 현재 에디터 상태를 HTML로 직렬화
        if (htmlMode && !prevMode.current) {
            editor.read(() => {
                onHtmlChange($generateHtmlFromNodes(editor, null));
            });
            editor.setEditable(false);
        }
        // HTML → Rich 전환: HTML 파싱 후 에디터 갱신
        if (!htmlMode && prevMode.current) {
            editor.setEditable(true);
            editor.update(() => {
                const parser = new DOMParser();
                const dom = parser.parseFromString(html, 'text/html');
                const nodes = $generateNodesFromDOM(editor, dom);
                const root = $getRoot();
                root.clear();
                nodes.forEach((n) => root.append(n));
            });
        }
        prevMode.current = htmlMode;
    }, [htmlMode]);  // eslint-disable-line react-hooks/exhaustive-deps

    return null;
}

// ─────────────────────────────────────────────────────────────────────────────
// 테이블 크기 선택 팝업
// ─────────────────────────────────────────────────────────────────────────────
const MAX_ROWS = 8;
const MAX_COLS = 8;

function TablePicker({ onSelect, onClose }) {
    const [hover, setHover] = useState({ row: 0, col: 0 });

    return (
        <div
            style={{
                position: 'absolute', top: '100%', left: 0, zIndex: 999,
                background: '#fff', border: '1px solid #d1d5db', borderRadius: 6,
                padding: 8, boxShadow: '0 4px 12px rgba(0,0,0,0.12)',
            }}
            onMouseLeave={onClose}
        >
            <div style={{ fontSize: 11, color: '#6b7280', marginBottom: 6, textAlign: 'center' }}>
                {hover.row > 0 ? `${hover.row} × ${hover.col}` : '테이블 크기 선택'}
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: `repeat(${MAX_COLS}, 18px)`, gap: 2 }}>
                {Array.from({ length: MAX_ROWS * MAX_COLS }, (_, i) => {
                    const r = Math.floor(i / MAX_COLS) + 1;
                    const c = (i % MAX_COLS) + 1;
                    const active = r <= hover.row && c <= hover.col;
                    return (
                        <div
                            key={i}
                            style={{ width: 18, height: 18, border: '1px solid', cursor: 'pointer', borderRadius: 2, transition: 'background 0.1s',
                                borderColor: active ? '#0d6efd' : '#d1d5db',
                                background: active ? '#dbeafe' : '#f9fafb',
                            }}
                            onMouseEnter={() => setHover({ row: r, col: c })}
                            onClick={() => onSelect(r, c)}
                        />
                    );
                })}
            </div>
        </div>
    );
}

// ─────────────────────────────────────────────────────────────────────────────
// 툴바
// ─────────────────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
// 링크 팝업
// ─────────────────────────────────────────────────────────────────────────────
function LinkPopup({ editorId, onInsert, onRemove, onClose }) {
    const [url, setUrl] = useState('');
    const [target, setTarget] = useState('_self');
    const inputRef = useRef(null);

    useEffect(() => { inputRef.current?.focus(); }, []);

    return (
        <div
            style={{
                position: 'absolute', top: '100%', left: 0, zIndex: 999, minWidth: 300,
                background: '#fff', border: '1px solid #d1d5db', borderRadius: 6,
                padding: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.12)',
            }}
        >
            <div style={{ marginBottom: 8 }}>
                <input
                    ref={inputRef}
                    id={editorId ? `${editorId}_linkUrl` : 'linkUrl'}
                    name={editorId ? `${editorId}_linkUrl` : 'linkUrl'}
                    type="text"
                    placeholder="https://example.com"
                    value={url}
                    onChange={(e) => setUrl(e.target.value)}
                    onKeyDown={(e) => { if (e.key === 'Enter') onInsert(url, target); if (e.key === 'Escape') onClose(); }}
                    style={{ width: '100%', padding: '4px 8px', border: '1px solid #d1d5db', borderRadius: 4, fontSize: 13, boxSizing: 'border-box' }}
                />
            </div>
            <div style={{ display: 'flex', gap: 12, marginBottom: 10, fontSize: 13 }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}>
                    <input type="radio" id={editorId ? `${editorId}_linkTargetSelf` : 'linkTargetSelf'} name={editorId ? `${editorId}_linkTarget` : 'linkTarget'} value="_self" checked={target === '_self'} onChange={() => setTarget('_self')} />
                    직접 연결
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}>
                    <input type="radio" id={editorId ? `${editorId}_linkTargetBlank` : 'linkTargetBlank'} name={editorId ? `${editorId}_linkTarget` : 'linkTarget'} value="_blank" checked={target === '_blank'} onChange={() => setTarget('_blank')} />
                    신규 창
                </label>
            </div>
            <div style={{ display: 'flex', gap: 6 }}>
                <button type="button" style={{ flex: 1, padding: '4px 0', fontSize: 12, background: '#0d6efd', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}
                    onClick={() => onInsert(url, target)}>삽입</button>
                <button type="button" style={{ flex: 1, padding: '4px 0', fontSize: 12, background: '#f3f4f6', border: '1px solid #d1d5db', borderRadius: 4, cursor: 'pointer' }}
                    onClick={onRemove}>링크 제거</button>
                <button type="button" style={{ padding: '4px 8px', fontSize: 12, background: '#f3f4f6', border: '1px solid #d1d5db', borderRadius: 4, cursor: 'pointer' }}
                    onClick={onClose}>✕</button>
            </div>
        </div>
    );
}

// ─────────────────────────────────────────────────────────────────────────────
// 폰트 색상 팝업
// ─────────────────────────────────────────────────────────────────────────────
const PRESET_COLORS = [
    '#000000','#374151','#6b7280','#d1d5db',
    '#ef4444','#f97316','#eab308','#22c55e',
    '#3b82f6','#8b5cf6','#ec4899','#ffffff',
];

function ColorPopup({ editorId, current, onSelect, onClose }) {
    return (
        <div
            style={{
                position: 'absolute', top: '100%', left: 0, zIndex: 999,
                background: '#fff', border: '1px solid #d1d5db', borderRadius: 6,
                padding: 8, boxShadow: '0 4px 12px rgba(0,0,0,0.12)', minWidth: 148,
            }}
            onMouseLeave={onClose}
        >
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(6, 20px)', gap: 3, marginBottom: 6 }}>
                {PRESET_COLORS.map((c) => (
                    <div
                        key={c}
                        onClick={() => { onSelect(c); onClose(); }}
                        style={{
                            width: 20, height: 20, borderRadius: 3, cursor: 'pointer',
                            background: c,
                            border: c === current ? '2px solid #0d6efd' : '1px solid #d1d5db',
                            boxSizing: 'border-box',
                        }}
                    />
                ))}
            </div>
            <input
                id={editorId ? `${editorId}_fontColor` : 'fontColor'}
                name={editorId ? `${editorId}_fontColor` : 'fontColor'}
                type="color"
                value={current || '#000000'}
                onChange={(e) => onSelect(e.target.value)}
                style={{ width: '100%', height: 24, padding: 0, border: '1px solid #d1d5db', borderRadius: 3, cursor: 'pointer' }}
            />
        </div>
    );
}

// ─────────────────────────────────────────────────────────────────────────────
// 툴바
// ─────────────────────────────────────────────────────────────────────────────
const FONT_FAMILIES = [
    { label: '기본 폰트', value: '' },
    { label: '맑은 고딕', value: "'맑은 고딕', 'Malgun Gothic', sans-serif" },
    { label: '굴림',      value: 'Gulim, 굴림, sans-serif' },
    { label: '돋움',      value: 'Dotum, 돋움, sans-serif' },
    { label: '나눔고딕',  value: "'NanumGothic', '나눔고딕', sans-serif" },
    { label: 'Arial',     value: 'Arial, Helvetica, sans-serif' },
    { label: 'Georgia',   value: "Georgia, 'Times New Roman', serif" },
    { label: 'Courier',   value: "'Courier New', Courier, monospace" },
];

function ToolbarPlugin({ imageUploadUrl, htmlMode, onToggleHtml, editorId }) {
    const [editor] = useLexicalComposerContext();
    const fileRef = useRef(null);
    const [showTablePicker, setShowTablePicker] = useState(false);
    const [showLinkPopup, setShowLinkPopup]     = useState(false);
    const [showColorPopup, setShowColorPopup]   = useState(false);
    const [fontFamily, setFontFamily]           = useState('');
    const [fontColor, setFontColor]             = useState('#000000');

    // 선택 영역 변경 시 현재 스타일 읽기
    useEffect(() => {
        return editor.registerUpdateListener(({ editorState }) => {
            editorState.read(() => {
                const sel = $getSelection();
                if (!sel) return;
                setFontFamily($getSelectionStyleValueForProperty(sel, 'font-family', '') || '');
                setFontColor($getSelectionStyleValueForProperty(sel, 'color', '#000000') || '#000000');
            });
        });
    }, [editor]);

    const applyFontFamily = useCallback((family) => {
        editor.update(() => {
            const sel = $getSelection();
            if (sel) $patchStyleText(sel, { 'font-family': family || null });
        });
        setFontFamily(family);
    }, [editor]);

    const applyFontColor = useCallback((color) => {
        editor.update(() => {
            const sel = $getSelection();
            if (sel) $patchStyleText(sel, { color });
        });
        setFontColor(color);
    }, [editor]);

    const fmt = useCallback((f) => editor.dispatchCommand(FORMAT_TEXT_COMMAND, f), [editor]);

    const handleImageChange = useCallback(async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;
        e.target.value = '';
        try {
            let src;
            if (imageUploadUrl) {
                const fd = new FormData();
                fd.append('file', file);
                const res = await fnAjaxFetch({ url: imageUploadUrl, method: 'POST', data: fd, headers: { 'Content-Type': 'multipart/form-data' } });
                const json = res?.data;
                if (json?.resultCodeInfo !== 'SUCCESS') throw new Error(json?.resultMessage);
                const path = json?.result?.result || json?.result?.filePath || json?.result;
                src = `${config.REACT_APP_IMG_URL || ''}${path}`;
            } else {
                src = await new Promise((res, rej) => { const r = new FileReader(); r.onload = (e) => res(e.target.result); r.onerror = rej; r.readAsDataURL(file); });
            }
            editor.dispatchCommand(INSERT_IMAGE_COMMAND, { src, altText: file.name });
        } catch (err) { console.error('이미지 업로드 실패:', err); }
    }, [editor, imageUploadUrl]);

    const insertTable = useCallback((rows, cols) => {
        editor.dispatchCommand(INSERT_TABLE_COMMAND, { rows: String(rows), columns: String(cols), includeHeaders: { rows: true, columns: false } });
        setShowTablePicker(false);
    }, [editor]);

    const insertLink = useCallback((url, target) => {
        if (!url) return;
        const href = url.startsWith('http') ? url : `https://${url}`;
        editor.dispatchCommand(TOGGLE_LINK_COMMAND, { url: href, target, rel: target === '_blank' ? 'noopener noreferrer' : undefined });
        setShowLinkPopup(false);
    }, [editor]);

    const removeLink = useCallback(() => {
        editor.dispatchCommand(TOGGLE_LINK_COMMAND, null);
        setShowLinkPopup(false);
    }, [editor]);

    const btn = { padding: '3px 8px', fontSize: 13, border: '1px solid #d1d5db', borderRadius: 4, background: '#f9fafb', cursor: 'pointer', marginRight: 2 };
    const sep = { margin: '0 4px', color: '#d1d5db' };

    return (
        <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: 2, padding: '6px 8px', borderBottom: '1px solid #e5e7eb', background: 'var(--bs-body-bg, #f8fafc)' }}>
            {!htmlMode && <>
                <button type="button" style={btn} onClick={() => editor.dispatchCommand(UNDO_COMMAND, undefined)} title="실행 취소">↩</button>
                <button type="button" style={btn} onClick={() => editor.dispatchCommand(REDO_COMMAND, undefined)} title="다시 실행">↪</button>
                <span style={sep}>|</span>
                {/* 폰트 선택 */}
                <select
                    id={editorId ? `${editorId}_fontFamily` : 'fontFamily'}
                    name={editorId ? `${editorId}_fontFamily` : 'fontFamily'}
                    value={fontFamily}
                    onChange={(e) => applyFontFamily(e.target.value)}
                    style={{ fontSize: 12, border: '1px solid #d1d5db', borderRadius: 4, padding: '2px 4px', height: 26, background: 'var(--bs-body-bg, #fff)', color: 'inherit', cursor: 'pointer' }}
                    title="폰트 선택"
                >
                    {FONT_FAMILIES.map((f) => (
                        <option key={f.value} value={f.value}>{f.label}</option>
                    ))}
                </select>
                {/* 폰트 색상 */}
                <div style={{ position: 'relative', display: 'inline-block' }}>
                    <button
                        type="button"
                        style={{ ...btn, display: 'flex', alignItems: 'center', gap: 3, padding: '3px 6px' }}
                        onClick={() => setShowColorPopup((v) => !v)}
                        title="글자 색상"
                    >
                        <span style={{ fontWeight: 700, fontSize: 13, color: fontColor, textShadow: fontColor === '#ffffff' ? '0 0 1px #aaa' : undefined }}>A</span>
                        <span style={{ display: 'block', width: 14, height: 3, background: fontColor, borderRadius: 1, border: '1px solid #d1d5db' }} />
                    </button>
                    {showColorPopup && (
                        <ColorPopup
                            editorId={editorId}
                            current={fontColor}
                            onSelect={applyFontColor}
                            onClose={() => setShowColorPopup(false)}
                        />
                    )}
                </div>
                <span style={sep}>|</span>
                <button type="button" style={{ ...btn, fontWeight: 700 }} onClick={() => fmt('bold')} title="굵게"><b>B</b></button>
                <button type="button" style={{ ...btn, fontStyle: 'italic' }} onClick={() => fmt('italic')} title="기울임"><i>I</i></button>
                <button type="button" style={{ ...btn, textDecoration: 'underline' }} onClick={() => fmt('underline')} title="밑줄"><u>U</u></button>
                <button type="button" style={{ ...btn, textDecoration: 'line-through' }} onClick={() => fmt('strikethrough')} title="취소선">S̶</button>
                <span style={sep}>|</span>
                <button type="button" style={btn} onClick={() => editor.dispatchCommand(INSERT_UNORDERED_LIST_COMMAND, undefined)} title="목록">• 목록</button>
                <button type="button" style={btn} onClick={() => editor.dispatchCommand(INSERT_ORDERED_LIST_COMMAND, undefined)} title="번호 목록">1. 목록</button>
                <span style={sep}>|</span>
                {/* 테이블 삽입 */}
                <div style={{ position: 'relative', display: 'inline-block' }}>
                    <button type="button" style={btn} onClick={() => setShowTablePicker((v) => !v)} title="테이블 삽입">⊞ 테이블</button>
                    {showTablePicker && (
                        <TablePicker onSelect={insertTable} onClose={() => setShowTablePicker(false)} />
                    )}
                </div>
                <span style={sep}>|</span>
                {/* 링크 삽입 */}
                <div style={{ position: 'relative', display: 'inline-block' }}>
                    <button type="button" style={btn} onClick={() => setShowLinkPopup((v) => !v)} title="링크 삽입">🔗 링크</button>
                    {showLinkPopup && (
                        <LinkPopup
                            editorId={editorId}
                            onInsert={insertLink}
                            onRemove={removeLink}
                            onClose={() => setShowLinkPopup(false)}
                        />
                    )}
                </div>
                <span style={sep}>|</span>
                {/* 이미지 삽입 */}
                <button type="button" style={btn} onClick={() => fileRef.current?.click()} title="이미지 삽입 (드래그&드롭도 가능)">🖼 이미지</button>
                <input ref={fileRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={handleImageChange} />
                <span style={sep}>|</span>
            </>}
            {/* HTML 편집 토글 */}
            <button
                type="button"
                style={{ ...btn, background: htmlMode ? '#0d6efd' : undefined, color: htmlMode ? '#fff' : undefined, borderColor: htmlMode ? '#0d6efd' : undefined }}
                onClick={onToggleHtml}
                title="HTML 소스 편집"
            >&lt;/&gt;</button>
        </div>
    );
}

// ─────────────────────────────────────────────────────────────────────────────
// 테마 & 노드 목록
// ─────────────────────────────────────────────────────────────────────────────
const THEME = {
    text: { bold: 'lx-bold', italic: 'lx-italic', underline: 'lx-underline', strikethrough: 'lx-strikethrough' },
    list: { ul: 'lx-ul', ol: 'lx-ol', listitem: 'lx-listitem' },
    table: 'lx-table',
    tableCell: 'lx-table-cell',
    tableCellHeader: 'lx-table-cell-header',
    tableRow: 'lx-table-row',
};

const NODES = [HeadingNode, QuoteNode, CodeNode, ListNode, ListItemNode, LinkNode, ImageNode, TableNode, TableCellNode, TableRowNode];

const EDITOR_CSS = `
.lexical-editor-wrap .lx-bold { font-weight: bold; }
.lexical-editor-wrap .lx-italic { font-style: italic; }
.lexical-editor-wrap .lx-underline { text-decoration: underline; }
.lexical-editor-wrap .lx-strikethrough { text-decoration: line-through; }
.lexical-editor-wrap .lx-ul { list-style: disc; padding-left: 20px; margin: 4px 0; }
.lexical-editor-wrap .lx-ol { list-style: decimal; padding-left: 20px; margin: 4px 0; }
.lexical-editor-wrap .lx-listitem { margin: 2px 0; }
.lexical-editor-content:focus { outline: none; }
.lexical-editor-content p { margin: 0 0 4px; }
.lexical-editor-placeholder { position: absolute; top: 10px; left: 12px; color: #9ca3af; pointer-events: none; user-select: none; font-size: 14px; }
.lexical-editor-wrap .lx-table { border-collapse: collapse; width: 100%; min-width: 100px; margin: 8px 0; }
.lexical-editor-wrap .lx-table-cell { border: 1px solid #d1d5db; padding: 6px 10px; min-width: 60px; position: relative; }
.lexical-editor-wrap .lx-table-cell-header { border: 1px solid #d1d5db; padding: 6px 10px; background: #f3f4f6; font-weight: 600; }
.lexical-editor-wrap .lx-table-row {}
.lexical-editor-drop-active { outline: 2px dashed #0d6efd !important; background: #eff6ff !important; }
/* 테이블 컬럼 리사이즈 핸들 */
.lexical-editor-wrap .TableCellResizer__resizer { background-color: #0d6efd; }
.lexical-editor-wrap td:hover, .lexical-editor-wrap th:hover { position: relative; }
/* 링크 스타일 */
.lexical-editor-wrap a { color: #0d6efd; text-decoration: underline; cursor: pointer; }
.lexical-editor-wrap a:hover { color: #0a58ca; }
`;

// ─────────────────────────────────────────────────────────────────────────────
// 메인 컴포넌트
// ─────────────────────────────────────────────────────────────────────────────
/**
 * 공통 Rich Text 에디터 (Lexical 기반)
 *
 * Props:
 *   value          — Lexical JSON 직렬화 문자열
 *   onChange       — (jsonString) => void
 *   height         — 에디터 최소 높이 (기본 200)
 *   placeholder
 *   readOnly
 *   label / required / id
 *   imageUploadUrl — 이미지 서버 업로드 URL (없으면 base64)
 *
 * 기능:
 *   - 텍스트 서식 (Bold/Italic/Underline/Strikethrough)
 *   - 목록 (순서/비순서)
 *   - 테이블 삽입 (크기 팝업으로 선택)
 *   - 이미지 삽입 (툴바 버튼 + 드래그&드롭)
 *   - 이미지 리사이즈 (8방향 핸들 드래그)
 *   - HTML 소스 편집 모드 토글
 */
const LexicalEditor = ({
    value = '',
    onChange,
    height = 200,
    scrollable = false,
    placeholder = '내용을 입력하세요.',
    readOnly = false,
    label,
    required = false,
    id,
    imageUploadUrl,
}) => {
    const [htmlMode, setHtmlMode] = useState(false);
    const [htmlSource, setHtmlSource] = useState('');

    const initialConfig = {
        namespace: id || 'LexicalEditor',
        theme: THEME,
        nodes: NODES,
        editable: !readOnly,
        // Lexical JSON은 여기서 직접 복원 (parseEditorState 내부 처리)
        editorState: resolveEditorState(value),
        onError: (e) => console.error('LexicalEditor error:', e),
    };

    const handleChange = useCallback((editorState) => {
        if (onChange && !htmlMode) onChange(JSON.stringify(editorState.toJSON()));
    }, [onChange, htmlMode]);

    return (
        <div className="lexical-editor-wrap">
            {label && (
                <label htmlFor={id} className="form-label" style={{ display: 'block', marginBottom: 4 }}>
                    {label}{required && <span className="text-danger ms-1">*</span>}
                </label>
            )}
            <style>{EDITOR_CSS}</style>
            <LexicalComposer initialConfig={initialConfig}>
                <div style={{ border: '1px solid #d1d5db', borderRadius: 6, overflow: 'hidden' }}>
                    {!readOnly && (
                        <ToolbarPlugin
                            imageUploadUrl={imageUploadUrl}
                            htmlMode={htmlMode}
                            onToggleHtml={() => setHtmlMode((v) => !v)}
                            editorId={id}
                        />
                    )}

                    {/* HTML 소스 편집 영역 */}
                    {htmlMode && (
                        <textarea
                            id={id ? `${id}_htmlSource` : 'htmlSource'}
                            name={id ? `${id}_htmlSource` : 'htmlSource'}
                            value={htmlSource}
                            onChange={(e) => setHtmlSource(e.target.value)}
                            style={{
                                width: '100%', padding: '10px 12px',
                                ...(scrollable
                                    ? { height, resize: 'none', overflowY: 'auto' }
                                    : { minHeight: height, resize: 'vertical' }),
                                fontFamily: 'monospace', fontSize: 13, lineHeight: 1.6,
                                border: 'none', outline: 'none',
                                background: 'var(--bs-body-bg, #fff)', color: 'inherit',
                                boxSizing: 'border-box',
                            }}
                            spellCheck={false}
                        />
                    )}

                    {/* Rich Text 에디터 영역 */}
                    <div style={{
                        position: 'relative', padding: '10px 12px',
                        background: 'var(--bs-body-bg, #fff)',
                        display: htmlMode ? 'none' : undefined,
                        ...(scrollable ? { height, overflowY: 'auto' } : { minHeight: height }),
                    }}>
                        <RichTextPlugin
                            contentEditable={
                                <ContentEditable
                                    id={id}
                                    name={id}
                                    className="lexical-editor-content"
                                    style={{ minHeight: height, outline: 'none', fontSize: 14, lineHeight: 1.6 }}
                                />
                            }
                            placeholder={<div className="lexical-editor-placeholder">{placeholder}</div>}
                            ErrorBoundary={LexicalErrorBoundary}
                        />
                        <HistoryPlugin />
                        <ListPlugin />
                        <LinkPlugin />
                        <TablePlugin hasCellMerge hasCellBackgroundColor={false} hasTabHandler />
                        <TableCellResizer />
                        <ImagePlugin />
                        <DragDropPlugin imageUploadUrl={imageUploadUrl} />
                        <HtmlPlugin htmlMode={htmlMode} html={htmlSource} onHtmlChange={setHtmlSource} />
                        <OnChangePlugin onChange={handleChange} ignoreSelectionChange />
                        <InitialValuePlugin value={value} />
                    </div>
                </div>
            </LexicalComposer>
        </div>
    );
};

export default LexicalEditor;
