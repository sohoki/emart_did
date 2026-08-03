import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const PAGE_UNIT = 20;
const emptyForm = { centerAnniStartDay: '', centerAnniEndDay: '', startTime: '', endTime: '', brodCode: '' };

export default function CenterAnniListPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const centerId = searchParams.get('centerId') || '';

    const [rowData, setRowData] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [brodOptions, setBrodOptions] = useState([]);
    const [form, setForm] = useState(emptyForm);
    const [loading, setLoading] = useState(false);

    const columnDefs = useMemo(() => ([
        { field: 'centerAnniStartDay', headerName: '시작일', width: 110 },
        { field: 'centerAnniEndDay', headerName: '종료일', width: 110 },
        { field: 'startTime', headerName: '시작시간', width: 100 },
        { field: 'endTime', headerName: '종료시간', width: 100 },
        { field: 'brodName', headerName: '방송콘텐츠', flex: 1, minWidth: 160 },
        {
            headerName: '', width: 90,
            cellRenderer: (p) => (
                <button type="button" onClick={() => handleDelete(p.data.centerAnniday)}>삭제</button>
            ),
        },
        // eslint-disable-next-line react-hooks/exhaustive-deps
    ]), []);

    const loadList = useCallback(async () => {
        if (!centerId) return;
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.CENTER_ANNI_LIST,
                method: 'POST',
                data: { centerId, pageIndex: 1, pageUnit: PAGE_UNIT },
                showLoading: false,
            });
            const list = res?.data?.result?.resultList ?? [];
            const cnt = res?.data?.result?.totalCnt ?? list.length;
            setRowData(list);
            setTotalCnt(cnt);
        } finally {
            setLoading(false);
        }
    }, [centerId]);

    const loadBrodCombo = useCallback(async () => {
        const res = await fnAjaxFetch({ url: URL.CENTER_ANNI_BROD_COMBO, method: 'GET', showLoading: false });
        setBrodOptions(res?.data?.result?.resultList ?? []);
    }, []);

    useEffect(() => {
        if (!getCookie('accessToken')) {
            navigate('/login', { replace: true });
            return;
        }
        if (!centerId) return;
        loadList();
        loadBrodCombo();
    }, [centerId, loadList, loadBrodCombo, navigate]);

    const handleDelete = async (centerAnniday) => {
        const result = await Swal.fire({
            icon: 'question', title: '기념일 삭제', text: '삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;

        await fnAjaxFetch({ url: `${URL.CENTER_ANNI_INFO}/${centerAnniday}.do`, method: 'DELETE' });
        loadList();
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!form.centerAnniStartDay || !form.centerAnniEndDay) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: '시작일/종료일을 입력해 주세요.' });
            return;
        }

        const checkRes = await fnAjaxFetch({
            url: URL.CENTER_ANNI_CNT_CHECK,
            method: 'POST',
            data: { centerId, centerAnniStartDay: form.centerAnniStartDay, centerAnniEndDay: form.centerAnniEndDay },
        });
        const overlapCnt = Number(checkRes?.data?.result?.result ?? 0);
        if (overlapCnt > 0) {
            Swal.fire({ icon: 'warning', title: '기간 중복', text: '기존 기념일과 기간이 겹칩니다.' });
            return;
        }

        await fnAjaxFetch({
            url: URL.CENTER_ANNI_UPDATE,
            method: 'POST',
            data: { ...form, centerId, mode: 'Ins' },
        });
        setForm(emptyForm);
        loadList();
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                <h2 style={{ margin: 0 }}>매장 기념일 관리 — {centerId}</h2>
                <button type="button" onClick={() => navigate('/backoffice/sub/basicManage/cnt')}>매장 목록으로</button>
            </div>

            <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8, marginBottom: 12, flexWrap: 'wrap', alignItems: 'center' }}>
                <input type="text" placeholder="시작일(YYYYMMDD)" value={form.centerAnniStartDay}
                    onChange={(e) => setForm((p) => ({ ...p, centerAnniStartDay: e.target.value }))} />
                <input type="text" placeholder="종료일(YYYYMMDD)" value={form.centerAnniEndDay}
                    onChange={(e) => setForm((p) => ({ ...p, centerAnniEndDay: e.target.value }))} />
                <input type="text" placeholder="시작시간(HH:mm)" value={form.startTime}
                    onChange={(e) => setForm((p) => ({ ...p, startTime: e.target.value }))} />
                <input type="text" placeholder="종료시간(HH:mm)" value={form.endTime}
                    onChange={(e) => setForm((p) => ({ ...p, endTime: e.target.value }))} />
                <select value={form.brodCode} onChange={(e) => setForm((p) => ({ ...p, brodCode: e.target.value }))}>
                    <option value="">방송콘텐츠 선택</option>
                    {brodOptions.map((o) => (
                        <option key={o.brodCode} value={o.brodCode}>{o.brodNm || o.brodCode}</option>
                    ))}
                </select>
                <button type="submit">기념일 등록</button>
                <span style={{ marginLeft: 'auto', color: '#64748b' }}>
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
