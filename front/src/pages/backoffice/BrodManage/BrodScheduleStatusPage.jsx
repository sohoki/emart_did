import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const PAGE_UNIT = 20;

export default function BrodScheduleStatusPage() {
    const navigate = useNavigate();
    const [contentList, setContentList] = useState([]);
    const [contentTotalCnt, setContentTotalCnt] = useState(0);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [selectedBrod, setSelectedBrod] = useState(null);
    const [centerList, setCenterList] = useState([]);
    const [rightSearchKeyword, setRightSearchKeyword] = useState('');
    const [loading, setLoading] = useState(false);

    const contentColumnDefs = useMemo(() => ([
        { field: 'brodCode', headerName: '방송코드', width: 150 },
        { field: 'brodName', headerName: '방송명', flex: 1, minWidth: 160 },
        { field: 'centerNm', headerName: '매장', width: 120 },
        // eslint-disable-next-line react-hooks/exhaustive-deps
    ]), []);

    const loadContentList = useCallback(async (keyword) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.BROD_SCHEDULE_LEFT_LIST,
                method: 'POST',
                data: { searchCondition: 'CON_NM', searchKeyword: keyword ?? '', pageIndex: 1, pageUnit: PAGE_UNIT },
                showLoading: false,
            });
            const list = res?.data?.result?.resultList ?? [];
            setContentList(list);
            setContentTotalCnt(res?.data?.result?.totalCnt ?? list.length);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        if (!getCookie('accessToken')) {
            navigate('/login', { replace: true });
            return;
        }
        loadContentList('');
    }, [loadContentList, navigate]);

    const loadCenterList = useCallback(async (brodCode, keyword) => {
        const res = await fnAjaxFetch({
            url: URL.BROD_SCHEDULE_RIGHT_LIST, method: 'GET',
            param: { brodCode, rightSearchKeyword: keyword ?? '' }, showLoading: false,
        });
        setCenterList(res?.data?.result?.resultList ?? []);
    }, []);

    const handleSearch = (e) => {
        e.preventDefault();
        loadContentList(searchKeyword);
    };

    const handleSelectContent = (row) => {
        setSelectedBrod(row);
        setRightSearchKeyword('');
        loadCenterList(row.brodCode, '');
    };

    const handleRightSearch = (e) => {
        e.preventDefault();
        if (selectedBrod) loadCenterList(selectedBrod.brodCode, rightSearchKeyword);
    };

    const handleToggle = async (center, connect) => {
        if (!selectedBrod) return;

        let centerStartTime = center.centerStartTime || '0000';
        let centerEndTime = center.centerEndTime || '2359';
        if (connect) {
            const { value: times } = await Swal.fire({
                title: `${center.centerNm} 배포 시간대`,
                html: '<input id="swal-start" class="swal2-input" placeholder="시작(HHmm)" value="0000">' +
                    '<input id="swal-end" class="swal2-input" placeholder="종료(HHmm)" value="2359">',
                showCancelButton: true,
                confirmButtonText: '배포',
                cancelButtonText: '취소',
                preConfirm: () => {
                    const start = document.getElementById('swal-start').value;
                    const end = document.getElementById('swal-end').value;
                    return { start, end };
                },
            });
            if (!times) return;
            centerStartTime = times.start;
            centerEndTime = times.end;
        } else {
            const result = await Swal.fire({
                icon: 'question', title: '배포 해제', text: `${center.centerNm}의 배포를 해제하시겠습니까?`,
                showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
            });
            if (!result.isConfirmed) return;
        }

        await fnAjaxFetch({
            url: URL.BROD_SCHEDULE_RIGHT_UPDATE,
            method: 'POST',
            data: {
                brodCode: selectedBrod.brodCode,
                checkVal: connect ? 'Y' : 'N',
                centerId: center.centerId,
                centerStartTime,
                centerEndTime,
            },
        });
        loadCenterList(selectedBrod.brodCode, rightSearchKeyword);
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <h2 style={{ margin: '0 0 12px' }}>방송 배포 스케줄 관리</h2>

            <div style={{ display: 'flex', gap: 16, flex: 1, minHeight: 0 }}>
                <div style={{ flex: 1.2, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
                    <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                        <input type="text" placeholder="방송명 검색" value={searchKeyword} onChange={(e) => setSearchKeyword(e.target.value)} />
                        <button type="submit">검색</button>
                        <span style={{ marginLeft: 'auto', alignSelf: 'center', color: '#64748b' }}>
                            총 {contentTotalCnt}건{loading ? ' (조회 중...)' : ''}
                        </span>
                    </form>
                    <div style={{ flex: 1, minHeight: 0 }}>
                        <AppAgGrid
                            theme={gridTheme}
                            rowData={contentList}
                            columnDefs={contentColumnDefs}
                            defaultColDef={{ sortable: true, resizable: true }}
                            pagination
                            paginationPageSize={PAGE_UNIT}
                            onRowClicked={(e) => handleSelectContent(e.data)}
                        />
                    </div>
                </div>

                <div style={{ flex: 1, borderLeft: '1px solid #e2e8f0', paddingLeft: 16, overflowY: 'auto' }}>
                    <h3>{selectedBrod ? `배포 매장 — ${selectedBrod.brodName}` : '방송을 선택해 주세요'}</h3>
                    {selectedBrod && (
                        <>
                            <form onSubmit={handleRightSearch} style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                                <input type="text" placeholder="매장명 검색" value={rightSearchKeyword} onChange={(e) => setRightSearchKeyword(e.target.value)} />
                                <button type="submit">검색</button>
                            </form>
                            <table style={{ borderCollapse: 'collapse', width: '100%' }}>
                                <thead>
                                    <tr style={{ background: '#f8fafc' }}>
                                        <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>매장</th>
                                        <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>배포시간</th>
                                        <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>배포여부</th>
                                        <th style={{ border: '1px solid #e2e8f0', padding: 6 }}></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {centerList.map((center) => (
                                        <tr key={center.centerId}>
                                            <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{center.centerNm}</td>
                                            <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{center.centerStartTime} ~ {center.centerEndTime}</td>
                                            <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{center.brodCode === 'Y' ? '배포중' : '미배포'}</td>
                                            <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>
                                                {center.brodCode === 'Y' ? (
                                                    <button type="button" onClick={() => handleToggle(center, false)}>해제</button>
                                                ) : (
                                                    <button type="button" onClick={() => handleToggle(center, true)}>배포</button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                    {centerList.length === 0 && (
                                        <tr><td colSpan={4} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>매장 정보가 없습니다.</td></tr>
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
