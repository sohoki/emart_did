import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const PAGE_UNIT = 20;
const emptyForm = {
    mode: 'Ins', didIdsText: '', sendMessage: '',
    sendMessageStartDay: '', sendMessageEndDay: '',
    sendMessageStartHour: '00', sendMessageStartMin: '00',
    sendMessageEndHour: '23', sendMessageEndMin: '59',
};

export default function ContentMessageListPage() {
    const navigate = useNavigate();
    const [rowData, setRowData] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [form, setForm] = useState(emptyForm);
    const [loading, setLoading] = useState(false);

    const columnDefs = useMemo(() => ([
        { field: 'didNm', headerName: 'DID명', width: 140 },
        { field: 'sendMessage', headerName: '메시지', flex: 1, minWidth: 200 },
        { field: 'sendMessageStartDay', headerName: '시작일', width: 100 },
        { field: 'sendMessageEndDay', headerName: '종료일', width: 100 },
        { field: 'sendMessageStartTime', headerName: '시작시간', width: 90 },
        { field: 'sendMessageEndTime', headerName: '종료시간', width: 90 },
        {
            headerName: '', width: 90,
            cellRenderer: (p) => (
                <button type="button" onClick={() => handleDelete(p.data.sendDidId)}>삭제</button>
            ),
        },
        // eslint-disable-next-line react-hooks/exhaustive-deps
    ]), []);

    const loadList = useCallback(async (keyword) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.CON_MESSAGE_LIST,
                method: 'POST',
                data: { searchCondition: 'SEND_MESSAGE', searchKeyword: keyword ?? '', pageIndex: 1, pageUnit: PAGE_UNIT },
                showLoading: false,
            });
            const list = res?.data?.result?.resultList ?? [];
            setRowData(list);
            setTotalCnt(res?.data?.result?.totalCnt ?? list.length);
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

    const handleDelete = async (sendDidId) => {
        const result = await Swal.fire({
            icon: 'question', title: '메시지 삭제', text: '삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;

        await fnAjaxFetch({ url: URL.CON_MESSAGE_DELETE_BULK, method: 'POST', data: { sendDidIds: [sendDidId] } });
        loadList(searchKeyword);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const didIds = form.didIdsText.split(',').map((s) => s.trim()).filter(Boolean);
        if (didIds.length === 0 || !form.sendMessage) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: 'DID ID(콤마로 구분)와 메시지 내용을 입력해 주세요.' });
            return;
        }

        await fnAjaxFetch({
            url: URL.CON_MESSAGE_UPDATE,
            method: 'POST',
            data: { ...form, didIds, mode: 'Ins' },
        });
        setForm(emptyForm);
        loadList(searchKeyword);
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <h2 style={{ margin: '0 0 12px' }}>DID 발송 메시지(자막) 관리</h2>

            <div style={{ display: 'flex', gap: 16, flex: 1, minHeight: 0 }}>
                <div style={{ flex: 1.4, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
                    <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                        <input type="text" placeholder="메시지 내용 검색" value={searchKeyword} onChange={(e) => setSearchKeyword(e.target.value)} />
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

                <div style={{ flex: 1, borderLeft: '1px solid #e2e8f0', paddingLeft: 16, overflowY: 'auto' }}>
                    <h3>신규 메시지 발송</h3>
                    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                        <label>
                            대상 DID ID(콤마로 구분)
                            <input type="text" placeholder="예: C19051701001,C19051701002" value={form.didIdsText}
                                onChange={(e) => setForm((p) => ({ ...p, didIdsText: e.target.value }))} />
                        </label>
                        <label>
                            메시지 내용
                            <textarea rows={3} value={form.sendMessage} onChange={(e) => setForm((p) => ({ ...p, sendMessage: e.target.value }))} />
                        </label>
                        <label>
                            시작일(YYYYMMDD)
                            <input type="text" value={form.sendMessageStartDay} onChange={(e) => setForm((p) => ({ ...p, sendMessageStartDay: e.target.value }))} />
                        </label>
                        <label>
                            종료일(YYYYMMDD)
                            <input type="text" value={form.sendMessageEndDay} onChange={(e) => setForm((p) => ({ ...p, sendMessageEndDay: e.target.value }))} />
                        </label>
                        <div style={{ display: 'flex', gap: 8 }}>
                            <label>
                                시작시간
                                <input type="text" style={{ width: 40 }} value={form.sendMessageStartHour} onChange={(e) => setForm((p) => ({ ...p, sendMessageStartHour: e.target.value }))} />
                                :
                                <input type="text" style={{ width: 40 }} value={form.sendMessageStartMin} onChange={(e) => setForm((p) => ({ ...p, sendMessageStartMin: e.target.value }))} />
                            </label>
                            <label>
                                종료시간
                                <input type="text" style={{ width: 40 }} value={form.sendMessageEndHour} onChange={(e) => setForm((p) => ({ ...p, sendMessageEndHour: e.target.value }))} />
                                :
                                <input type="text" style={{ width: 40 }} value={form.sendMessageEndMin} onChange={(e) => setForm((p) => ({ ...p, sendMessageEndMin: e.target.value }))} />
                            </label>
                        </div>
                        <button type="submit">발송 등록</button>
                    </form>
                </div>
            </div>
        </div>
    );
}
