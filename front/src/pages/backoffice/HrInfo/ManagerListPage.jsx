import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import URL from '@/constants/URL.jsx';

const PAGE_UNIT = 20;

export default function ManagerListPage() {
    const navigate = useNavigate();
    const [rowData, setRowData] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [loading, setLoading] = useState(false);

    const columnDefs = useMemo(() => ([
        { field: 'managerId', headerName: '아이디', width: 140 },
        { field: 'managerName', headerName: '이름', width: 120 },
        { field: 'partNm', headerName: '부서', flex: 1, minWidth: 160 },
        { field: 'roleName', headerName: '권한', width: 140 },
        { field: 'centerId', headerName: '매장 스코프', width: 120, valueFormatter: p => p.value || '전체' },
        { field: 'managerStatus', headerName: '상태', width: 100 },
        { field: 'useYn', headerName: '사용유무', width: 90 },
        { field: 'managerEmail', headerName: '이메일', flex: 1, minWidth: 180 },
        { field: 'managerTel', headerName: '연락처', width: 130 },
        { field: 'lastUpdtPnttm', headerName: '최종수정일', width: 150 },
    ]), []);

    const loadList = useCallback(async (keyword) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.MANAGER_LIST,
                method: 'POST',
                data: {
                    searchKeyword: keyword ?? '',
                    pageIndex: 1,
                    pageUnit: PAGE_UNIT,
                },
                showLoading: false,
            });
            const list = res?.data?.result?.resultList ?? [];
            const cnt = res?.data?.result?.totalCnt ?? list.length;
            setRowData(list);
            setTotalCnt(cnt);
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

    const handleSearch = (e) => {
        e.preventDefault();
        loadList(searchKeyword);
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100%', padding: 16, boxSizing: 'border-box' }}>
            <h2 style={{ margin: '0 0 12px' }}>관리자 리스트</h2>

            <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                <input
                    type="text"
                    placeholder="아이디/이름 검색"
                    value={searchKeyword}
                    onChange={(e) => setSearchKeyword(e.target.value)}
                />
                <button type="submit">검색</button>
                <span style={{ marginLeft: 'auto', alignSelf: 'center', color: '#64748b' }}>
                    총 {totalCnt}건{loading ? ' (조회 중...)' : ''}
                </span>
            </form>

            <div style={{ flex: 1, minHeight: 0 }}>
                <AppAgGrid
                    theme={gridTheme}
                    rowData={rowData}
                    columnDefs={columnDefs}
                    defaultColDef={{ sortable: true, resizable: true }}
                    pagination
                    paginationPageSize={PAGE_UNIT}
                />
            </div>
        </div>
    );
}
