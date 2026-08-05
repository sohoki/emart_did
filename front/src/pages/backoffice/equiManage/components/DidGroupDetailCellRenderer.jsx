import { useCallback, useEffect, useMemo, useState } from 'react';
import Select from 'react-select';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const SUB_DEFAULT_COL_DEF = { resizable: true, sortable: true, filter: false, flex: 1 };

const DID_SELECT_STYLES = {
    container: (base) => ({ ...base, flex: '1 1 200px', minWidth: 160, maxWidth: 400, fontSize: '13px' }),
    control: (base) => ({ ...base, minHeight: 32, height: 32 }),
    valueContainer: (base) => ({ ...base, height: 32, padding: '0 8px' }),
    input: (base) => ({ ...base, margin: 0, padding: 0 }),
    indicatorsContainer: (base) => ({ ...base, height: 32 }),
    menu: (base) => ({ ...base, fontSize: '13px' }),
    menuPortal: (base) => ({ ...base, zIndex: 9999 }),
};

// 레거시(did_groupList.jsp)가 "음원방송" 단말은 그룹 등록 콤보에서 제외하던 규칙 유지
const isMusicDid = (nm) => (nm || '').startsWith('음원방송');

/**
 * DIdGroupInfo 전용 서브 그리드 렌더러 — 그룹에 속한 DID 단말기 목록만 표시하며,
 * 등록(그룹에 없는 단말 콤보에서 선택 후 추가)/삭제 기능만 제공한다(단말 자체의 수정은 없음).
 * AG Grid master-detail의 detailCellRenderer로 사용.
 */
const DidGroupDetailCellRenderer = (props) => {
    const { data } = props;
    const groupCode = data?.groupCode;

    const [rowData, setRowData] = useState([]);
    const [comboOptions, setComboOptions] = useState([]);
    const [selectedDidId, setSelectedDidId] = useState('');

    const loadMembers = useCallback(async () => {
        if (!groupCode) return;
        const res = await fnAjaxFetch({
            url: `${URL.DID_GROUP_MEMBER_LIST}/${encodeURIComponent(groupCode)}.do`,
            method: 'GET', showLoading: false,
        });
        setRowData(res?.data?.result?.didLst || []);
    }, [groupCode]);

    const loadCombo = useCallback(async () => {
        const res = await fnAjaxFetch({ url: URL.DID_GROUP_MEMBER_COMBO, method: 'GET', showLoading: false });
        const list = res?.data?.result?.didCmbLst || [];
        setComboOptions(list.filter((o) => !isMusicDid(o.didNm)));
    }, []);

    useEffect(() => {
        loadMembers();
        loadCombo();
    }, [loadMembers, loadCombo]);

    const handleAdd = useCallback(async () => {
        if (!selectedDidId) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '등록할 단말기를 선택해 주세요.' });
            return;
        }
        const res = await fnAjaxFetch({
            url: URL.DID_GROUP_MEMBER_INSERT, method: 'POST',
            data: { groupCode, didId: selectedDidId },
        });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            setSelectedDidId('');
            await loadMembers();
            await loadCombo();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || '등록 중 오류가 발생했습니다.' });
        }
    }, [selectedDidId, groupCode, loadMembers, loadCombo]);

    const handleRemove = useCallback(async (didId) => {
        const ok = await Swal.fire({
            icon: 'question', title: '단말기 삭제', text: '그룹에서 삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const res = await fnAjaxFetch({
            url: `${URL.DID_GROUP_MEMBER_DELETE}/${encodeURIComponent(groupCode)}.do?didId=${encodeURIComponent(didId)}`,
            method: 'DELETE',
        });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            setRowData(json?.result?.didLst || []);
            await loadCombo();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || '삭제 중 오류가 발생했습니다.' });
        }
    }, [groupCode, loadCombo]);

    const didOptions = useMemo(() => (
        comboOptions.map((o) => ({ value: o.didId, label: o.didNm }))
    ), [comboOptions]);

    const selectedOption = useMemo(() => (
        didOptions.find((o) => o.value === selectedDidId) || null
    ), [didOptions, selectedDidId]);

    const colDefs = useMemo(() => ([
        { headerName: '단말ID', field: 'didId', cellStyle: { textAlign: 'center' } },
        { headerName: '단말명', field: 'didNm', cellStyle: { textAlign: 'center' } },
        {
            headerName: '삭제', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={() => handleRemove(p.data?.didId)}
                >삭제</button>
            ),
        },
    ]), [handleRemove]);

    return (
        <div style={{ width: '100%', backgroundColor: '#fff', padding: '0 0 16px', boxSizing: 'border-box' }}>
            <div style={{
                fontWeight: 'bold',
                padding: '6px 15px',
                fontSize: '13px',
                backgroundColor: '#f8f9fa',
                borderBottom: '1px solid #dde2eb',
                lineHeight: '1.2',
            }}>
                그룹 내 단말기 목록
            </div>
            <div style={{ width: '100%', boxSizing: 'border-box', height: '220px' }}>
                <AppAgGrid
                    rowData={rowData}
                    columnDefs={colDefs}
                    defaultColDef={SUB_DEFAULT_COL_DEF}
                    theme={gridTheme}
                    headerHeight={32}
                    rowHeight={30}
                    overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>등록된 단말기가 없습니다.</span>"
                    overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                />
            </div>
            <div style={{
                display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: 8,
                padding: '8px 15px', borderTop: '1px solid #dde2eb',
            }}>
                <Select
                    inputId="groupDidSelect"
                    name="groupDidSelect"
                    options={didOptions}
                    value={selectedOption}
                    onChange={(opt) => setSelectedDidId(opt?.value || '')}
                    placeholder="단말기를 선택하세요"
                    isClearable
                    isSearchable
                    noOptionsMessage={() => '등록 가능한 단말기가 없습니다'}
                    styles={DID_SELECT_STYLES}
                    menuPortalTarget={document.body}
                    menuPlacement="auto"
                />
                <button type="button" className="btn btn-primary btn-default__blue"
                    style={{ fontSize: '13px', padding: '6px 12px', whiteSpace: 'nowrap', flexShrink: 0 }}
                    onClick={handleAdd}>단말기 등록</button>
            </div>
        </div>
    );
};

export default DidGroupDetailCellRenderer;
