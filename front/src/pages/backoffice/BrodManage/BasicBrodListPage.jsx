import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const PAGE_UNIT = 20;

export default function BasicBrodListPage() {
    const navigate = useNavigate();
    const [list, setList] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [selected, setSelected] = useState(null);
    const [centerList, setCenterList] = useState([]);
    const [loading, setLoading] = useState(false);

    const columnDefs = useMemo(() => ([
        { field: 'basicCode', headerName: '기초방송코드', width: 160 },
        { field: 'basicGroupNm', headerName: '방송명', flex: 1, minWidth: 180 },
        { field: 'basicGroupCnt', headerName: '배포수', width: 100 },
        { field: 'lastUpdtPnttm', headerName: '최종수정일', width: 160 },
        // eslint-disable-next-line react-hooks/exhaustive-deps
    ]), []);

    const loadList = useCallback(async (keyword) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.BASIC_BROD_LIST,
                method: 'POST',
                data: { searchCondition: 'basicGroupNm', searchKeyword: keyword ?? '', pageIndex: 1, pageUnit: PAGE_UNIT },
                showLoading: false,
            });
            const resultList = res?.data?.result?.resultList ?? [];
            setList(resultList);
            setTotalCnt(res?.data?.result?.totalCnt ?? resultList.length);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        if (!getCookie('accessToken')) {
            navigate('/login', { replace: true });
            return;
        }
        loadList('');
    }, [loadList, navigate]);

    const loadCenterList = useCallback(async (basicCode) => {
        const res = await fnAjaxFetch({
            url: URL.BASIC_BROD_CENTER_LIST, method: 'GET',
            param: { centerGubun: '' }, showLoading: false,
        });
        const centers = res?.data?.result?.centerInfo ?? [];
        setCenterList(centers.map((c) => ({ ...c, connected: c.basicCode === basicCode })));
    }, []);

    const handleSearch = (e) => {
        e.preventDefault();
        loadList(searchKeyword);
    };

    const handleSelect = (row) => {
        setSelected(row);
        loadCenterList(row.basicCode);
    };

    const handleCreate = async () => {
        const { value: basicGroupNm } = await Swal.fire({
            title: '기초 방송 신규 등록', input: 'text', inputLabel: '방송명',
            showCancelButton: true, confirmButtonText: '등록', cancelButtonText: '취소',
        });
        if (!basicGroupNm) return;

        const res = await fnAjaxFetch({
            url: URL.BASIC_BROD_UPDATE, method: 'POST',
            data: { mode: 'Ins', basicGroupNm },
        });
        if (res?.data?.resultCode === '00') {
            loadList(searchKeyword);
        }
    };

    const handleRename = async () => {
        if (!selected) return;
        const { value: basicGroupNm } = await Swal.fire({
            title: '방송명 수정', input: 'text', inputValue: selected.basicGroupNm,
            showCancelButton: true, confirmButtonText: '수정', cancelButtonText: '취소',
        });
        if (!basicGroupNm) return;

        await fnAjaxFetch({
            url: URL.BASIC_BROD_UPDATE, method: 'POST',
            data: { mode: 'Upd', basicCode: selected.basicCode, basicGroupNm },
        });
        loadList(searchKeyword);
    };

    const handleCopy = async () => {
        if (!selected) return;
        const result = await Swal.fire({
            icon: 'question', title: '기초 방송 복사', text: `'${selected.basicGroupNm}'을(를) 복사하시겠습니까? (편성 파일도 함께 복사됩니다)`,
            showCancelButton: true, confirmButtonText: '복사', cancelButtonText: '취소',
        });
        if (!result.isConfirmed) return;

        await fnAjaxFetch({
            url: URL.BASIC_BROD_UPDATE, method: 'POST',
            data: { mode: 'Cpy', basicCode: selected.basicCode, basicGroupNm: `${selected.basicGroupNm}_복사` },
        });
        loadList(searchKeyword);
    };

    const handleDelete = async () => {
        if (!selected) return;
        const result = await Swal.fire({
            icon: 'warning', title: '기초 방송 삭제', text: `'${selected.basicGroupNm}'을(를) 삭제하시겠습니까? 배포중인 매장이 있으면 함께 해제됩니다.`,
            showCancelButton: true, confirmButtonText: '삭제', cancelButtonText: '취소',
        });
        if (!result.isConfirmed) return;

        await fnAjaxFetch({
            url: URL.BASIC_BROD_DELETE, method: 'DELETE',
            data: { delBasicSeq: selected.basicCode },
        });
        setSelected(null);
        setCenterList([]);
        loadList(searchKeyword);
    };

    const handleToggle = async (center, connect) => {
        if (!selected) return;
        if (connect) {
            const result = await Swal.fire({
                icon: 'question', title: '배포', text: `${center.centerNm}에 '${selected.basicGroupNm}'을(를) 배포하시겠습니까?`,
                showCancelButton: true, confirmButtonText: '배포', cancelButtonText: '취소',
            });
            if (!result.isConfirmed) return;
        } else {
            const result = await Swal.fire({
                icon: 'question', title: '배포 해제', text: `${center.centerNm}의 배포를 해제하시겠습니까?`,
                showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
            });
            if (!result.isConfirmed) return;
        }

        await fnAjaxFetch({
            url: URL.BASIC_BROD_CENTER_UPDATE,
            method: 'POST',
            data: {
                CenterSeq: center.centerId,
                basicCode: selected.basicCode,
                checkValue: connect ? 'Y' : 'N',
            },
        });
        loadCenterList(selected.basicCode);
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <h2 style={{ margin: '0 0 12px' }}>기초 방송(템플릿) 관리</h2>

            <div style={{ display: 'flex', gap: 16, flex: 1, minHeight: 0 }}>
                <div style={{ flex: 1.2, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
                    <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                        <input type="text" placeholder="방송명 검색" value={searchKeyword} onChange={(e) => setSearchKeyword(e.target.value)} />
                        <button type="submit">검색</button>
                        <button type="button" onClick={handleCreate} style={{ marginLeft: 'auto' }}>신규 등록</button>
                        <span style={{ alignSelf: 'center', color: '#64748b' }}>
                            총 {totalCnt}건{loading ? ' (조회 중...)' : ''}
                        </span>
                    </form>
                    <div style={{ flex: 1, minHeight: 0 }}>
                        <AppAgGrid
                            theme={gridTheme}
                            rowData={list}
                            columnDefs={columnDefs}
                            defaultColDef={{ sortable: true, resizable: true }}
                            pagination
                            paginationPageSize={PAGE_UNIT}
                            onRowClicked={(e) => handleSelect(e.data)}
                        />
                    </div>
                </div>

                <div style={{ flex: 1, borderLeft: '1px solid #e2e8f0', paddingLeft: 16, overflowY: 'auto' }}>
                    <h3>{selected ? `배포 매장 — ${selected.basicGroupNm}` : '기초 방송을 선택해 주세요'}</h3>
                    {selected && (
                        <>
                            <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                                <button type="button" onClick={handleRename}>이름 수정</button>
                                <button type="button" onClick={handleCopy}>복사</button>
                                <button type="button" onClick={handleDelete} style={{ marginLeft: 'auto', color: '#dc2626' }}>삭제</button>
                            </div>
                            <table style={{ borderCollapse: 'collapse', width: '100%' }}>
                                <thead>
                                    <tr style={{ background: '#f8fafc' }}>
                                        <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>매장</th>
                                        <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>배포여부</th>
                                        <th style={{ border: '1px solid #e2e8f0', padding: 6 }}></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {centerList.map((center) => (
                                        <tr key={center.centerId}>
                                            <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{center.centerNm}</td>
                                            <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{center.connected ? '배포중' : '미배포'}</td>
                                            <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>
                                                {center.connected ? (
                                                    <button type="button" onClick={() => handleToggle(center, false)}>해제</button>
                                                ) : (
                                                    <button type="button" onClick={() => handleToggle(center, true)}>배포</button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                    {centerList.length === 0 && (
                                        <tr><td colSpan={3} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>매장 정보가 없습니다.</td></tr>
                                    )}
                                </tbody>
                            </table>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}
