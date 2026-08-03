import {
    DecoratorNode,
    $getNodeByKey,
    $getSelection,
    $isNodeSelection,
    CLICK_COMMAND,
    COMMAND_PRIORITY_LOW,
    KEY_BACKSPACE_COMMAND,
    KEY_DELETE_COMMAND,
} from 'lexical';
import React, { useRef, useState, useCallback, useEffect } from 'react';
import { useLexicalComposerContext } from '@lexical/react/LexicalComposerContext';
import { useLexicalNodeSelection } from '@lexical/react/useLexicalNodeSelection';
import { mergeRegister } from '@lexical/utils';

// ── 리사이즈 핸들 방향 ───────────────────────────────────────────────────────
const HANDLES = [
    { dir: 'nw', style: { top: -4, left: -4, cursor: 'nw-resize' } },
    { dir: 'n',  style: { top: -4, left: '50%', transform: 'translateX(-50%)', cursor: 'n-resize' } },
    { dir: 'ne', style: { top: -4, right: -4, cursor: 'ne-resize' } },
    { dir: 'e',  style: { top: '50%', right: -4, transform: 'translateY(-50%)', cursor: 'e-resize' } },
    { dir: 'se', style: { bottom: -4, right: -4, cursor: 'se-resize' } },
    { dir: 's',  style: { bottom: -4, left: '50%', transform: 'translateX(-50%)', cursor: 's-resize' } },
    { dir: 'sw', style: { bottom: -4, left: -4, cursor: 'sw-resize' } },
    { dir: 'w',  style: { top: '50%', left: -4, transform: 'translateY(-50%)', cursor: 'w-resize' } },
];

const HANDLE_BASE = {
    position: 'absolute', width: 8, height: 8,
    background: '#0d6efd', border: '1px solid #fff',
    borderRadius: 2, zIndex: 10,
};

// ── 이미지 렌더 컴포넌트 ────────────────────────────────────────────────────
function ImageComponent({ src, altText, initWidth, initHeight, nodeKey }) {
    const [editor] = useLexicalComposerContext();
    const [isSelected, setSelected, clearSelection] = useLexicalNodeSelection(nodeKey);
    const [dims, setDims] = useState({
        width:  initWidth  || null,
        height: initHeight || null,
    });
    const imgRef    = useRef(null);
    const curDims   = useRef(dims);   // stale closure 방지

    // Del/Backspace로 선택된 이미지 삭제
    const onDelete = useCallback((event) => {
        if (!isSelected || !$isNodeSelection($getSelection())) return false;
        event.preventDefault();
        const node = $getNodeByKey(nodeKey);
        if ($isImageNode(node)) node.remove();
        return true;
    }, [isSelected, nodeKey]);

    // 이미지 클릭 → 실제 Lexical NodeSelection으로 등록(Del/Backspace 삭제가 동작하려면 필요),
    // 이미지 바깥 클릭 시에는 CLICK_COMMAND가 안 걸려서 자연스럽게 다른 선택으로 대체됨
    useEffect(() => {
        return mergeRegister(
            editor.registerCommand(
                CLICK_COMMAND,
                (event) => {
                    if (event.target === imgRef.current) {
                        event.preventDefault();
                        if (!event.shiftKey) clearSelection();
                        setSelected(!isSelected);
                        return true;
                    }
                    return false;
                },
                COMMAND_PRIORITY_LOW,
            ),
            editor.registerCommand(KEY_DELETE_COMMAND, onDelete, COMMAND_PRIORITY_LOW),
            editor.registerCommand(KEY_BACKSPACE_COMMAND, onDelete, COMMAND_PRIORITY_LOW),
        );
    }, [editor, isSelected, setSelected, clearSelection, onDelete]);

    // ── 리사이즈 드래그 ────────────────────────────────────────────────────
    const startResize = useCallback((e, dir) => {
        e.preventDefault();
        e.stopPropagation();

        const rect = imgRef.current.getBoundingClientRect();
        const startX = e.clientX;
        const startY = e.clientY;
        const startW = rect.width;
        const startH = rect.height;

        const onMove = (moveE) => {
            const dx = moveE.clientX - startX;
            const dy = moveE.clientY - startY;
            let w = startW, h = startH;

            if (dir.includes('e')) w = Math.max(60, startW + dx);
            if (dir.includes('w')) w = Math.max(60, startW - dx);
            if (dir.includes('s')) h = Math.max(40, startH + dy);
            if (dir.includes('n')) h = Math.max(40, startH - dy);

            w = Math.round(w);
            h = Math.round(h);
            curDims.current = { width: w, height: h };
            setDims({ width: w, height: h });
        };

        const onUp = () => {
            // Lexical 노드에 치수 저장
            editor.update(() => {
                const node = $getNodeByKey(nodeKey);
                if ($isImageNode(node)) {
                    const writable = node.getWritable();
                    writable.__width  = curDims.current.width;
                    writable.__height = curDims.current.height;
                }
            });
            window.removeEventListener('mousemove', onMove);
            window.removeEventListener('mouseup', onUp);
        };

        window.addEventListener('mousemove', onMove);
        window.addEventListener('mouseup', onUp);
    }, [editor, nodeKey]);

    const imgStyle = {
        display: 'block',
        maxWidth: '100%',
        width:  dims.width  ? `${dims.width}px`  : 'auto',
        height: dims.height ? `${dims.height}px` : 'auto',
        outline: isSelected ? '2px solid #0d6efd' : '2px solid transparent',
        transition: 'outline 0.1s',
        userSelect: 'none',
        cursor: 'default',
    };

    return (
        <span style={{ display: 'inline-block', position: 'relative', lineHeight: 0 }}>
            <img ref={imgRef} src={src} alt={altText} draggable={false} style={imgStyle}
                onError={(e) => { e.currentTarget.style.outline = '2px dashed #ef4444'; }} />

            {/* 리사이즈 핸들 — 선택 시에만 표시 */}
            {isSelected && HANDLES.map(({ dir, style }) => (
                <span
                    key={dir}
                    style={{ ...HANDLE_BASE, ...style }}
                    onMouseDown={(e) => startResize(e, dir)}
                />
            ))}
        </span>
    );
}

// ── Lexical ImageNode (DecoratorNode) ───────────────────────────────────────
export class ImageNode extends DecoratorNode {
    __src;
    __altText;
    __width;
    __height;

    static getType() { return 'image'; }

    static clone(node) {
        return new ImageNode(node.__src, node.__altText, node.__width, node.__height, node.__key);
    }

    static importJSON(s) {
        return new ImageNode(s.src, s.altText || '', s.width || null, s.height || null);
    }

    constructor(src, altText = '', width = null, height = null, key) {
        super(key);
        this.__src     = src;
        this.__altText = altText;
        this.__width   = width;
        this.__height  = height;
    }

    exportJSON() {
        return { type: 'image', src: this.__src, altText: this.__altText, width: this.__width, height: this.__height, version: 1 };
    }

    createDOM() {
        const span = document.createElement('span');
        span.style.display = 'inline-block';
        return span;
    }

    updateDOM() { return false; }
    isInline()  { return true; }

    decorate() {
        return (
            <ImageComponent
                src={this.__src}
                altText={this.__altText}
                initWidth={this.__width}
                initHeight={this.__height}
                nodeKey={this.__key}
            />
        );
    }
}

export function $createImageNode(src, altText = '', width = null, height = null) {
    return new ImageNode(src, altText, width, height);
}

export function $isImageNode(node) {
    return node instanceof ImageNode;
}
