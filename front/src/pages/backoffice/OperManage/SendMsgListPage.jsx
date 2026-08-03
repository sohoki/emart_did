import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import URL from '@/constants/URL.jsx';

const PAGE_UNIT = 30;

export default function SendMsgListPage() {
    const navigate = useNavigate();
    const [rowData, setRowData] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [centerId, setCenterId] = useState('');
    const [xmlProcessName, setXmlProcessName] = useState('');
    const [centerOptions, setCenterOptions] = useState([]);
    const [processOptions, setProcessOptions] = useState([]);
    const [loading, setLoading] = useState(false);

    const columnDefs = useMemo(() => ([
        { field: 'msgSeq', headerName: '순번', width: 100 },
        { field: 'didNm', headerName: 'DID명', width: 150 },
        { field: 'groupNm', headerName: '그룹', width: 130 },
        { field: 'xmlProcessName', headerName: '명령어', width: 160 },
        { field: 'processRemark', headerName: '명령어 설명', flex: 1, minWidth: 160 },
        { field: 'sendResult', headerName: '결과', width: 90 },
        { field: 'sendRegDate', headerName: '전송일시', width: 160 },
        { field: 'didIpAddr', headerName: 'IP', width: 130 },
        { field: 'errorMessage', headerName: '오류메시지', flex: 1, minWidth: 160 },
    ]), []);

    const loadList = useCallback(async (params) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.SND_LIST,
                method: 'POST',
                data: {
                    searchKeyword: params?.searchKeyword ?? '',
                    centerId: params?.centerId ?? '',
                    xmlProcessName: params?.xmlProcessName ?? '',
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

    const loadCombos = useCallback(async () => {
        const [centerRes, processRes] = await Promise.all([
            fnAjaxFetch({ url: URL.SND_CENTER_COMBO, method: 'GET', showLoading: false }),
            fnAjaxFetch({ url: URL.SND_PROCESS_COMBO, method: 'GET', showLoading: false }),
        ]);
        setCenterOptions(centerRes?.data?.result?.resultList ?? []);
        setProcessOptions(processRes?.data?.result?.resultList ?? []);
    }, []);

    useEffect(() => {
        if (!getCookie('accessToken')) {
            navigate('/login', { replace: true });
            return;
        }
        loadList({});
        loadCombos();
    }, [loadList, loadCombos, navigate]);

    const handleSearch = (e) => {
        e.preventDefault();
        loadList({ searchKeyword, centerId, xmlProcessName });
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                <h2 style={{ margin: 0 }}>DID 발송 이력</h2>
            </div>

            <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, marginBottom: 12, flexWrap: 'wrap' }}>
                <select value={centerId} onChange={(e) => setCenterId(e.target.value)}>
                    <option value="">전체 매장</option>
                    {centerOptions.map((o) => (
                        <option key={o.centerId} value={o.centerId}>{o.centerNm || o.centerId}</option>
                    ))}
                </select>
                <select value={xmlProcessName} onChange={(e) => setXmlProcessName(e.target.value)}>
                    <option value="">전체 명령어</option>
                    {processOptions.map((o) => (
                        <option key={o.xmlProcessName} value={o.xmlProcessName}>{o.processRemark || o.xmlProcessName}</option>
                    ))}
                </select>
                <input
                    type="text"
                    placeholder="DID ID 검색"
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
