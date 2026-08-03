import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const PAGE_UNIT = 20;

export default function CenterListPage() {
    const navigate = useNavigate();
    const fileInputRef = useRef(null);
    const [rowData, setRowData] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [loading, setLoading] = useState(false);

    const columnDefs = useMemo(() => ([
        { field: 'centerId', headerName: '매장 ID', width: 130 },
        { field: 'centerNm', headerName: '매장명', flex: 1, minWidth: 200 },
        { field: 'centerGubun', headerName: '구분', width: 110 },
        { field: 'centerStartTime', headerName: '운영시작', width: 100 },
        { field: 'centerEndTime', headerName: '운영종료', width: 100 },
        { field: 'centerUseYn', headerName: '사용유무', width: 90 },
        { field: 'centerRegdate', headerName: '등록일', width: 150 },
        {
            headerName: '기념일', width: 100,
            cellRenderer: (p) => (
                <button type="button" onClick={() => navigate(`/backoffice/sub/basicManage/cnt/anni?centerId=${p.data.centerId}`)}>
                    기념일
                </button>
            ),
        },
        // eslint-disable-next-line react-hooks/exhaustive-deps
    ]), [navigate]);

    const loadList = useCallback(async (keyword) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.CENTER_LIST,
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

    const handleExcelUploadClick = () => {
        fileInputRef.current?.click();
    };

    const handleExcelFileChange = async (e) => {
        const file = e.target.files?.[0];
        e.target.value = ''; // 같은 파일 재선택 가능하도록 초기화
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);

        const res = await fnAjaxFetch({
            url: URL.CENTER_EXCEL_UPLOAD,
            method: 'POST',
            data: formData,
        });

        const { successCount, failCount } = res?.data?.result?.result ?? {};
        await Swal.fire({
            icon: 'success',
            title: '엑셀 업로드 완료',
            text: `성공 ${successCount ?? 0}건, 실패 ${failCount ?? 0}건`,
        });
        loadList(searchKeyword);
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                <h2 style={{ margin: 0 }}>매장(센터) 리스트</h2>
                <div>
                    <input
                        ref={fileInputRef}
                        type="file"
                        accept=".xls,.xlsx"
                        style={{ display: 'none' }}
                        onChange={handleExcelFileChange}
                    />
                    <button type="button" onClick={handleExcelUploadClick}>엑셀 일괄등록</button>
                </div>
            </div>

            <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                <input
                    type="text"
                    placeholder="매장 ID/매장명 검색"
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
