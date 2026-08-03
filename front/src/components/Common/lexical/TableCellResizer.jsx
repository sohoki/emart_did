import { useCallback, useEffect, useRef, useState } from 'react';
import { useLexicalComposerContext } from '@lexical/react/LexicalComposerContext';
import { $getNodeByKey } from 'lexical';
import { $isTableCellNode } from '@lexical/table';

const MIN_COL_WIDTH   = 40;
const COL_ZONE        = 6;
const CORNER_SIZE     = 16;
const MIN_TABLE_WIDTH = 80;

function getLexicalKey(el, editor) {
    return el[`__lexicalKey_${editor._key}`];
}

export function TableCellResizer() {
    const [editor] = useLexicalComposerContext();

    // ── 컬럼 리사이즈 ─────────────────────────────────────────────────────────
    const [colIndicator, setColIndicator] = useState(null);
    const colResizing = useRef(null);
    const hoverCell   = useRef(null);

    // ── 테이블 전체 리사이즈 ──────────────────────────────────────────────────
    const [cornerPos, setCornerPos] = useState(null);   // { x, y } 핸들 위치
    const tableResizing = useRef(null);                  // { startX, startW, tableEl }
    const activeTable   = useRef(null);                  // 핸들 표시 중인 table element

    // ── 마우스 이동: 테이블 코너 & 컬럼 경계 감지 ────────────────────────────
    const onMouseMove = useCallback((e) => {
        if (colResizing.current || tableResizing.current) return;

        const el    = document.elementFromPoint(e.clientX, e.clientY);
        const td    = el?.closest('td, th');
        const table = td?.closest('table') || el?.closest('table');

        // 테이블 우하단 코너 감지
        if (table) {
            const tr = table.getBoundingClientRect();
            const nearCorner =
                e.clientX >= tr.right  - CORNER_SIZE &&
                e.clientY >= tr.bottom - CORNER_SIZE;

            if (nearCorner) {
                activeTable.current = table;
                hoverCell.current   = null;
                document.body.style.cursor = 'se-resize';
                setColIndicator(null);
                setCornerPos({ x: tr.right, y: tr.bottom });
                return;
            }
        }

        // 코너 감지 아님 → 핸들은 유지하되 cursor 초기화
        // (마우스가 핸들 위로 이동해도 activeTable 유지)
        if (!td) {
            hoverCell.current = null;
            document.body.style.cursor = '';
            setColIndicator(null);
            if (!cornerPos) activeTable.current = null;
            return;
        }

        // 컬럼 경계 감지
        const rect      = td.getBoundingClientRect();
        const nearRight = e.clientX >= rect.right - COL_ZONE &&
                          e.clientX <= rect.right + COL_ZONE;

        if (nearRight) {
            hoverCell.current = { el: td, rect };
            activeTable.current = null;
            setCornerPos(null);
            document.body.style.cursor = 'col-resize';
            setColIndicator({ x: rect.right, top: rect.top, height: rect.height });
        } else {
            hoverCell.current = null;
            document.body.style.cursor = '';
            setColIndicator(null);
        }
    }, [cornerPos]);

    // ── 코너 핸들 mousedown: 핸들 div에 직접 연결 ────────────────────────────
    const onCornerMouseDown = useCallback((e) => {
        const table = activeTable.current;
        if (!table) return;
        const rect = table.getBoundingClientRect();
        tableResizing.current = { startX: e.clientX, startW: rect.width, tableEl: table };
        e.preventDefault();
        e.stopPropagation();
    }, []);

    // ── 에디터 root mousedown: 컬럼 리사이즈 시작 ────────────────────────────
    const onMouseDown = useCallback((e) => {
        if (!hoverCell.current) return;
        const { el, rect } = hoverCell.current;
        const cellKey = getLexicalKey(el, editor);
        if (!cellKey) return;

        const table   = el.closest('table');
        const colIdx  = Array.from(el.parentElement?.children || []).indexOf(el);
        const colCells = [];

        if (table && colIdx >= 0) {
            table.querySelectorAll('tr').forEach((row) => {
                const cell = row.children[colIdx];
                if (cell) {
                    const k = getLexicalKey(cell, editor);
                    if (k) colCells.push({ key: k, el: cell });
                }
            });
        }

        colResizing.current = {
            startX: e.clientX, startW: rect.width, cellKey,
            colCells: colCells.length ? colCells : [{ key: cellKey, el }],
        };
        e.preventDefault();
        e.stopPropagation();
    }, [editor]);

    // ── 문서 mousemove: 두 리사이즈 모드 모두 처리 ──────────────────────────
    const onDocMouseMove = useCallback((e) => {
        // 테이블 전체 리사이즈
        if (tableResizing.current) {
            const { startX, startW, tableEl } = tableResizing.current;
            const newW = Math.max(MIN_TABLE_WIDTH, Math.round(startW + (e.clientX - startX)));
            tableEl.style.width    = `${newW}px`;
            tableEl.style.minWidth = `${newW}px`;
            const tr = tableEl.getBoundingClientRect();
            setCornerPos({ x: tr.right, y: tr.bottom });
            return;
        }

        // 컬럼 리사이즈
        if (!colResizing.current) return;
        const { startX, startW, cellKey, colCells } = colResizing.current;
        const newW = Math.max(MIN_COL_WIDTH, Math.round(startW + (e.clientX - startX)));
        const anchorEl = editor.getElementByKey(cellKey);
        if (anchorEl) {
            const r = anchorEl.getBoundingClientRect();
            setColIndicator({ x: r.left + newW, top: r.top, height: r.height });
        }
        colCells.forEach(({ el }) => { if (el) el.style.width = `${newW}px`; });
    }, [editor]);

    // ── 문서 mouseup: 리사이즈 확정 ─────────────────────────────────────────
    const onDocMouseUp = useCallback((e) => {
        // 테이블 전체 리사이즈 확정
        if (tableResizing.current) {
            const { startX, startW, tableEl } = tableResizing.current;
            const newW = Math.max(MIN_TABLE_WIDTH, Math.round(startW + (e.clientX - startX)));
            tableEl.style.width    = `${newW}px`;
            tableEl.style.minWidth = `${newW}px`;
            tableResizing.current  = null;
            document.body.style.cursor = '';
            // 핸들 위치 갱신
            const tr = tableEl.getBoundingClientRect();
            setCornerPos({ x: tr.right, y: tr.bottom });
            return;
        }

        // 컬럼 리사이즈 확정
        if (!colResizing.current) return;
        const { startX, startW, colCells } = colResizing.current;
        const newW = Math.max(MIN_COL_WIDTH, Math.round(startW + (e.clientX - startX)));

        editor.update(() => {
            colCells.forEach(({ key }) => {
                const node = $getNodeByKey(key);
                if ($isTableCellNode(node)) node.getWritable().setWidth(newW);
            });
        });
        colCells.forEach(({ el }) => { if (el) el.style.width = `${newW}px`; });

        colResizing.current        = null;
        hoverCell.current          = null;
        document.body.style.cursor = '';
        setColIndicator(null);
    }, [editor]);

    // ── 이벤트 등록 ──────────────────────────────────────────────────────────
    useEffect(() => {
        document.addEventListener('mousemove', onDocMouseMove);
        document.addEventListener('mouseup',   onDocMouseUp);

        const unregister = editor.registerRootListener((rootEl, prevRootEl) => {
            prevRootEl?.removeEventListener('mousemove', onMouseMove);
            prevRootEl?.removeEventListener('mousedown', onMouseDown);
            rootEl?.addEventListener('mousemove', onMouseMove);
            rootEl?.addEventListener('mousedown', onMouseDown);
        });

        return () => {
            unregister();
            document.removeEventListener('mousemove', onDocMouseMove);
            document.removeEventListener('mouseup',   onDocMouseUp);
            document.body.style.cursor = '';
        };
    }, [editor, onMouseMove, onMouseDown, onDocMouseMove, onDocMouseUp]);

    return (
        <>
            {/* 컬럼 리사이즈 세로 인디케이터 */}
            {colIndicator && (
                <div style={{
                    position: 'fixed', zIndex: 9999, pointerEvents: 'none',
                    top: colIndicator.top, left: colIndicator.x - 1,
                    width: 2, height: colIndicator.height, background: '#0d6efd',
                }} />
            )}

            {/* 테이블 리사이즈 코너 핸들 — onMouseDown 직접 연결 */}
            {cornerPos && (
                <div
                    onMouseDown={onCornerMouseDown}
                    style={{
                        position: 'fixed', zIndex: 9999,
                        top:    cornerPos.y - 7,
                        left:   cornerPos.x - 7,
                        width:  14, height: 14,
                        background: '#0d6efd',
                        borderRadius: 2,
                        border: '2px solid #fff',
                        cursor: 'se-resize',
                        boxShadow: '0 0 4px rgba(0,0,0,0.35)',
                    }}
                />
            )}
        </>
    );
}
