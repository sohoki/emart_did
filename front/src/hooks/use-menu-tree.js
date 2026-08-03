import { useState, useEffect, useCallback, useRef } from 'react';
import Swal from '@/lib/swal.js';

// 메뉴 트리는 관리자(root, level 1) → 대분류(level 2) → 실제 메뉴(level 3) 3단계까지만 허용한다
const MAX_MENU_LEVEL = 3;

// nodes(현재 배열 또는 그 하위)에서 key로 노드를 찾아 { node, parent, siblings(부모의 children 또는 최상위 배열), index }를 반환
const findNodeContext = (nodes, key, parent = null) => {
    for (let i = 0; i < nodes.length; i += 1) {
        const n = nodes[i];
        if (String(n.key) === String(key)) {
            return { node: n, parent, siblings: nodes, index: i };
        }
        if (n.children?.length) {
            const found = findNodeContext(n.children, key, n);
            if (found) return found;
        }
    }
    return null;
};

// node를 기준으로 한 서브트리의 최대 상대 깊이 (자기 자신 = 0)
const getSubtreeDepth = (node) => (
    !node.children?.length ? 0 : 1 + Math.max(...node.children.map(getSubtreeDepth))
);

// targetKey가 node의 하위(자손)에 속하는지 여부 — 자기 자신 하위로 드롭하는 것을 막기 위함
const isDescendantOf = (node, targetKey) => (
    !!node.children?.some((c) => String(c.key) === String(targetKey) || isDescendantOf(c, targetKey))
);

// 트리 깊은 복사 (원본 상태를 직접 변형하지 않기 위함)
const cloneTree = (nodes) => nodes.map((n) => ({ ...n, children: n.children ? cloneTree(n.children) : [] }));

export function useMenuTree(fnAjaxFetch, URL, CODE, buildTree, collectAllKeys, params, orderUpdateUrl) {
    const [treeData, setTreeData] = useState([]);
    const [expandedKeys, setExpandedKeys] = useState([]);
    const [autoExpandParent, setAutoExpandParent] = useState(true);
    const [selectedKey, setSelectedKey] = useState(null);
    const [selectedNode, setSelectedNode] = useState(null);

    // params를 ref로 보관 — 매 렌더마다 새 객체가 와도 loadTree 재생성을 막는다
    const paramsRef = useRef(params);
    useEffect(() => { paramsRef.current = params; }, [params]);

    const loadTree = useCallback(async (resetForm) => {
        try {
            const res = await fnAjaxFetch({
                url: URL,
                method: 'POST',
                data: paramsRef.current,
                withCredentials: true,
            });
            const json = res?.data;
            if (json?.resultCodeInfo !== 'SUCCESS') {
                await Swal.fire({ icon: CODE.WARNING, title: 'ERROR', text: json?.resultMessage || '목록 조회 실패' });
                return;
            }
            const list = json?.result?.resultList || [];
            const tree = buildTree(list);

            // 트리 데이터와 확장 키를 한 번에 설정해 두 번의 렌더를 방지한다
            setTreeData(tree);
            setExpandedKeys(collectAllKeys(tree));
            setAutoExpandParent(true);

            setSelectedKey(null);
            setSelectedNode(null);
            if (resetForm) resetForm();

        } catch (e) {
            if (e?.name !== 'HandledError') {
                await Swal.fire({ icon: CODE.ERROR, title: 'ERROR', text: e?.message || '트리 조회 실패' });
            }
        }
    // params를 의존성에서 제거 — paramsRef로 최신 값을 읽으므로 안전
    }, [fnAjaxFetch, URL, CODE, buildTree, collectAllKeys]);

    useEffect(() => {
        (async () => { await loadTree(); })();
    }, [loadTree]);

    const calcLevel = useCallback((targetKey) => {
        let level = 0;
        const walk = (nodes, target, currentLevel) => {
            for (const n of nodes) {
                if (String(n.key) === String(target)) {
                    level = currentLevel;
                    return true;
                }
                if (n.children?.length && walk(n.children, target, currentLevel + 1)) return true;
            }
            return false;
        };
        walk(treeData, targetKey, 1);
        return level || 1;
    }, [treeData]);

    // ===== 트리 드래그&드롭 이동/순서변경 =====
    // rc-tree의 onDrop 콜백 정보({ dragKey, dropKey, dropPosition, dropToGap })를 그대로 받는다
    const moveMenuNode = useCallback(async ({ dragKey, dropKey, dropPosition, dropToGap }) => {
        const dragCtx = findNodeContext(treeData, dragKey);
        const dropCtx = findNodeContext(treeData, dropKey);
        if (!dragCtx || !dropCtx) return false;

        const { node: dragNode } = dragCtx;
        const { node: dropNode, parent: dropParent } = dropCtx;

        if (!dragNode.data) {
            await Swal.fire({ icon: CODE.WARNING, title: '알림', text: '저장되지 않은 메뉴는 이동할 수 없습니다.' });
            return false;
        }
        if (!dropNode.data) {
            await Swal.fire({ icon: CODE.WARNING, title: '알림', text: '저장되지 않은 메뉴 위치로는 이동할 수 없습니다.' });
            return false;
        }
        if (String(dragNode.key) === String(dropNode.key) || isDescendantOf(dragNode, dropNode.key)) {
            return false; // 자기 자신 / 자기 하위로는 이동 불가
        }

        // 새 부모(= dropToGap이면 드롭 대상의 부모, 아니면 드롭 대상 자체) 기준으로 이동 후 레벨 계산
        const newParentNode = dropToGap ? dropParent : dropNode;
        const newLevel = newParentNode ? calcLevel(newParentNode.key) + 1 : 1;
        const dragDepth = getSubtreeDepth(dragNode);
        if (newLevel + dragDepth > MAX_MENU_LEVEL) {
            await Swal.fire({ icon: CODE.WARNING, title: '알림', text: `메뉴는 최대 ${MAX_MENU_LEVEL}단계까지만 구성할 수 있습니다.` });
            return false;
        }

        const nextTree = cloneTree(treeData);
        const nextDragCtx = findNodeContext(nextTree, dragKey);
        const { node: nextDragNode, siblings: oldSiblings, index: oldIndex } = nextDragCtx;
        oldSiblings.splice(oldIndex, 1); // 기존 위치에서 제거

        let newSiblings;
        if (dropToGap) {
            const nextDropCtx = findNodeContext(nextTree, dropKey);
            newSiblings = nextDropCtx.siblings;
            let insertIndex = nextDropCtx.index;
            // 같은 배열 안에서 뒤로 이동하는 경우 이미 제거되어 인덱스가 하나 당겨진 것을 보정
            if (newSiblings === oldSiblings && oldIndex < insertIndex) insertIndex -= 1;
            if (dropPosition === 1) insertIndex += 1;
            newSiblings.splice(insertIndex, 0, nextDragNode);
        } else {
            const nextDropNode = findNodeContext(nextTree, dropKey).node;
            nextDropNode.children = nextDropNode.children || [];
            newSiblings = nextDropNode.children;
            newSiblings.push(nextDragNode);
        }

        // 순서(menuOrdr) 재계산 — 저장된(비-임시) 노드만 순번 부여
        const renumber = (list) => {
            let ordr = 1;
            list.forEach((n) => {
                if (n.data) {
                    n.data = { ...n.data, menuOrdr: ordr };
                    ordr += 1;
                }
            });
        };
        renumber(newSiblings);
        if (oldSiblings !== newSiblings) renumber(oldSiblings);

        // 상위메뉴(upperMenuNo) 갱신
        nextDragNode.data = {
            ...nextDragNode.data,
            upperMenuNo: newParentNode ? (newParentNode.data?.menuNo ?? null) : null,
        };

        // 서버에 반영할 변경분(이동/순서가 바뀐 실제 노드만) 수집
        const changedPayload = [];
        const collectChanged = (list) => {
            list.forEach((n) => {
                if (n.data) {
                    changedPayload.push({
                        menuNo: String(n.data.menuNo),
                        upperMenuNo: n.data.upperMenuNo != null ? String(n.data.upperMenuNo) : null,
                        menuOrdr: String(n.data.menuOrdr),
                    });
                }
            });
        };
        collectChanged(newSiblings);
        if (oldSiblings !== newSiblings) collectChanged(oldSiblings);

        const prevTree = treeData;
        setTreeData(nextTree); // 낙관적 업데이트 — 실패 시 원복

        try {
            const res = await fnAjaxFetch({
                url: orderUpdateUrl,
                method: 'POST',
                data: changedPayload,
                withCredentials: true,
            });
            const json = res?.data;
            if (json?.resultCodeInfo !== 'SUCCESS') {
                await Swal.fire({ icon: CODE.WARNING, title: '알림', text: json?.resultMessage || '메뉴 이동 저장에 실패했습니다.' });
                setTreeData(prevTree);
                return false;
            }
            return true;
        } catch (e) {
            if (e?.name !== 'HandledError') {
                await Swal.fire({ icon: CODE.ERROR, title: 'ERROR', text: e?.message || '메뉴 이동 중 오류가 발생했습니다.' });
            }
            setTreeData(prevTree);
            return false;
        }
    }, [treeData, calcLevel, fnAjaxFetch, orderUpdateUrl, CODE]);

    return {
        treeData, setTreeData,
        expandedKeys, setExpandedKeys,
        autoExpandParent, setAutoExpandParent,
        selectedKey, setSelectedKey,
        selectedNode, setSelectedNode,
        loadTree, calcLevel,
        moveMenuNode,
    };
}
