import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const PAGE_UNIT = 20;

const emptyMonitorForm = { mode: 'Ins', mhsMonitorcd: '', mhsMonitornm: '', mhsMviewtype: '', mhsRemark: '' };
const emptyClassForm = {
    mode: 'Ins', mhsClasscd: '', mhsClassroomnm: '', mhsClassnm: '', mhsTeachernm: '',
    mhsClassstartday: '', mhsClassendday: '', mhsClassdayofweek: '', mhsClassstarttime: '', mhsClassendtime: '',
    mhsClassintro: '',
};
const emptyConnForm = { mhsMonitorcd: '', mhsClasscd: '' };

const TABS = [
    { key: 'monitor', label: '모니터 관리' },
    { key: 'class', label: '강의 관리' },
    { key: 'viewConn', label: '편성표' },
];

export default function MhsRoomManagePage() {
    const navigate = useNavigate();
    const [tab, setTab] = useState('monitor');

    const [brandList, setBrandList] = useState([]);
    const [centerList, setCenterList] = useState([]);
    const [mhsBrandcd, setMhsBrandcd] = useState('');
    const [mhsCentercd, setMhsCentercd] = useState('');

    // 모니터 관리
    const [monitorList, setMonitorList] = useState([]);
    const [monitorTotalCnt, setMonitorTotalCnt] = useState(0);
    const [monitorForm, setMonitorForm] = useState(emptyMonitorForm);
    const [monitorLoading, setMonitorLoading] = useState(false);

    // 강의 관리
    const [classList, setClassList] = useState([]);
    const [classTotalCnt, setClassTotalCnt] = useState(0);
    const [classForm, setClassForm] = useState(emptyClassForm);
    const [classLoading, setClassLoading] = useState(false);

    // 편성표
    const [monitorCombo, setMonitorCombo] = useState([]);
    const [classCombo, setClassCombo] = useState([]);
    const [connForm, setConnForm] = useState(emptyConnForm);
    const [connList, setConnList] = useState([]);
    const [connLoading, setConnLoading] = useState(false);

    const loadBrandList = useCallback(async () => {
        const res = await fnAjaxFetch({ url: URL.MHS_BRAND_LIST, method: 'GET', showLoading: false });
        setBrandList(res?.data?.result?.resultList ?? []);
    }, []);

    const loadCenterList = useCallback(async (brandcd) => {
        if (!brandcd) {
            setCenterList([]);
            return;
        }
        const res = await fnAjaxFetch({
            url: URL.MHS_CENTER_LIST, method: 'GET', param: { mhsBrandcd: brandcd }, showLoading: false,
        });
        setCenterList(res?.data?.result?.resultList ?? []);
    }, []);

    useEffect(() => {
        if (!getCookie('accessToken')) {
            navigate('/login', { replace: true });
            return;
        }
        loadBrandList();
    }, [loadBrandList, navigate]);

    const handleBrandChange = (value) => {
        setMhsBrandcd(value);
        setMhsCentercd('');
        loadCenterList(value);
    };

    // ===== 모니터 관리 =====
    const loadMonitorList = useCallback(async () => {
        setMonitorLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.MHS_MONITOR_LIST, method: 'POST',
                data: { mhsBrandcd, mhsCentercd, pageIndex: 1, pageUnit: PAGE_UNIT },
                showLoading: false,
            });
            const list = res?.data?.result?.resultList ?? [];
            setMonitorList(list);
            setMonitorTotalCnt(res?.data?.result?.totalCnt ?? list.length);
        } finally {
            setMonitorLoading(false);
        }
    }, [mhsBrandcd, mhsCentercd]);

    useEffect(() => {
        if (tab === 'monitor' && mhsCentercd) loadMonitorList();
    }, [tab, mhsCentercd, loadMonitorList]);

    const handleMonitorEdit = async (mhsMonitorcd) => {
        const res = await fnAjaxFetch({ url: `${URL.MHS_MONITOR_INFO}/${mhsMonitorcd}.do`, method: 'GET' });
        const detail = res?.data?.result?.result;
        if (detail) setMonitorForm({ ...detail, mode: 'Edt' });
    };

    const handleMonitorDelete = async (mhsMonitorcd) => {
        const result = await Swal.fire({
            icon: 'question', title: '모니터 삭제', text: '삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        await fnAjaxFetch({ url: `${URL.MHS_MONITOR_INFO}/${mhsMonitorcd}.do`, method: 'DELETE' });
        loadMonitorList();
    };

    const handleMonitorSubmit = async (e) => {
        e.preventDefault();
        if (!mhsCentercd) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: '브랜드/매장을 먼저 선택해 주세요.' });
            return;
        }
        if (!monitorForm.mhsMonitornm) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: '모니터명을 입력해 주세요.' });
            return;
        }
        await fnAjaxFetch({
            url: URL.MHS_MONITOR_UPDATE, method: 'POST',
            data: { ...monitorForm, mhsBrandcd, mhsCentercd },
        });
        setMonitorForm(emptyMonitorForm);
        loadMonitorList();
    };

    const monitorColumnDefs = useMemo(() => ([
        { field: 'mhsMonitorcd', headerName: '모니터코드', width: 140 },
        { field: 'mhsMonitornm', headerName: '모니터명', width: 160 },
        { field: 'mhsLastconn', headerName: '연결상태', width: 100 },
        { field: 'mhsRemark', headerName: '비고', flex: 1, minWidth: 160 },
        {
            headerName: '', width: 160,
            cellRenderer: (p) => (
                <>
                    <button type="button" onClick={() => handleMonitorEdit(p.data.mhsMonitorcd)}>수정</button>{' '}
                    <button type="button" onClick={() => handleMonitorDelete(p.data.mhsMonitorcd)}>삭제</button>
                </>
            ),
        },
        // eslint-disable-next-line react-hooks/exhaustive-deps
    ]), []);

    // ===== 강의 관리 =====
    const loadClassList = useCallback(async () => {
        setClassLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.MHS_CLASS_LIST, method: 'POST',
                data: { searchMhsBramdCd: mhsBrandcd, searchMhsCenterCd: mhsCentercd, pageIndex: 1, pageUnit: PAGE_UNIT },
                showLoading: false,
            });
            const list = res?.data?.result?.resultList ?? [];
            setClassList(list);
            setClassTotalCnt(res?.data?.result?.totalCnt ?? list.length);
        } finally {
            setClassLoading(false);
        }
    }, [mhsBrandcd, mhsCentercd]);

    useEffect(() => {
        if (tab === 'class' && mhsCentercd) loadClassList();
    }, [tab, mhsCentercd, loadClassList]);

    const handleClassEdit = async (mhsClasscd) => {
        const res = await fnAjaxFetch({ url: `${URL.MHS_CLASS_INFO}/${mhsClasscd}.do`, method: 'GET' });
        const detail = res?.data?.result?.result;
        if (detail) setClassForm({ ...detail, mode: 'Edt' });
    };

    const handleClassDelete = async (mhsClasscd) => {
        const result = await Swal.fire({
            icon: 'question', title: '강의 삭제', text: '삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        await fnAjaxFetch({ url: `${URL.MHS_CLASS_INFO}/${mhsClasscd}.do`, method: 'DELETE' });
        loadClassList();
    };

    const handleClassSubmit = async (e) => {
        e.preventDefault();
        if (!mhsCentercd) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: '브랜드/매장을 먼저 선택해 주세요.' });
            return;
        }
        if (!classForm.mhsClassnm || !classForm.mhsClassroomnm) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: '강의실(모니터코드)/강의명을 입력해 주세요.' });
            return;
        }
        await fnAjaxFetch({
            url: URL.MHS_CLASS_UPDATE, method: 'POST',
            data: { ...classForm, mhsBrandcd, mhsCentercd },
        });
        setClassForm(emptyClassForm);
        loadClassList();
    };

    const classColumnDefs = useMemo(() => ([
        { field: 'mhsClassnm', headerName: '강의명', width: 160 },
        { field: 'mhsTeachernm', headerName: '강사명', width: 120 },
        { field: 'mhsClassstartday', headerName: '시작일', width: 100 },
        { field: 'mhsClassendday', headerName: '종료일', width: 100 },
        { field: 'mhsClassstarttime', headerName: '시작시간', width: 90 },
        { field: 'mhsClassendtime', headerName: '종료시간', width: 90 },
        {
            headerName: '', width: 160,
            cellRenderer: (p) => (
                <>
                    <button type="button" onClick={() => handleClassEdit(p.data.mhsClasscd)}>수정</button>{' '}
                    <button type="button" onClick={() => handleClassDelete(p.data.mhsClasscd)}>삭제</button>
                </>
            ),
        },
        // eslint-disable-next-line react-hooks/exhaustive-deps
    ]), []);

    // ===== 편성표 =====
    const loadCombosForConn = useCallback(async () => {
        if (!mhsCentercd) return;
        const monRes = await fnAjaxFetch({ url: URL.MHS_MONITOR_COMBO, method: 'GET', param: { mhsCentercd }, showLoading: false });
        setMonitorCombo(monRes?.data?.result?.resultList ?? []);
        const clsRes = await fnAjaxFetch({
            url: URL.MHS_CLASS_COMBO, method: 'POST', data: { mhsBrandcd, mhsCentercd }, showLoading: false,
        });
        setClassCombo(clsRes?.data?.result?.resultList ?? []);
    }, [mhsBrandcd, mhsCentercd]);

    const loadConnList = useCallback(async (mhsMonitorcd) => {
        if (!mhsMonitorcd) {
            setConnList([]);
            return;
        }
        setConnLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.MHS_VIEWCONN_LIST, method: 'POST', data: { mhsMonitorcd }, showLoading: false,
            });
            setConnList(res?.data?.result?.resultList ?? []);
        } finally {
            setConnLoading(false);
        }
    }, []);

    useEffect(() => {
        if (tab === 'viewConn' && mhsCentercd) loadCombosForConn();
    }, [tab, mhsCentercd, loadCombosForConn]);

    const handleConnMonitorChange = (value) => {
        setConnForm((p) => ({ ...p, mhsMonitorcd: value }));
        loadConnList(value);
    };

    const handleConnInsert = async (e) => {
        e.preventDefault();
        if (!connForm.mhsMonitorcd || !connForm.mhsClasscd) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: '모니터/강의를 선택해 주세요.' });
            return;
        }
        await fnAjaxFetch({ url: URL.MHS_VIEWCONN_INSERT, method: 'POST', data: connForm });
        loadConnList(connForm.mhsMonitorcd);
    };

    const handleConnDelete = async (mhsConnSeq) => {
        const result = await Swal.fire({
            icon: 'question', title: '편성 삭제', text: '삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        await fnAjaxFetch({ url: `${URL.MHS_VIEWCONN_INFO}/${mhsConnSeq}.do`, method: 'DELETE' });
        loadConnList(connForm.mhsMonitorcd);
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <h2 style={{ margin: '0 0 12px' }}>문화센터(MHS) 룸/강의/편성 관리</h2>

            <div style={{ display: 'flex', gap: 8, marginBottom: 12, alignItems: 'center' }}>
                <label>
                    브랜드
                    <select value={mhsBrandcd} onChange={(e) => handleBrandChange(e.target.value)} style={{ marginLeft: 4 }}>
                        <option value="">선택</option>
                        {brandList.map((b) => (
                            <option key={b.mhsBrandcd} value={b.mhsBrandcd}>{'  '.repeat(Math.max(0, Number(b.mhsBrandlv) - 1))}{b.mhsBrandnm}</option>
                        ))}
                    </select>
                </label>
                <label>
                    매장
                    <select value={mhsCentercd} onChange={(e) => setMhsCentercd(e.target.value)} style={{ marginLeft: 4 }} disabled={!mhsBrandcd}>
                        <option value="">선택</option>
                        {centerList.map((c) => (
                            <option key={c.mhsCentercd} value={c.mhsCentercd}>{c.mhsCenternm}</option>
                        ))}
                    </select>
                </label>
                {!mhsCentercd && <span style={{ color: '#94a3b8' }}>브랜드와 매장을 선택하면 아래 목록이 조회됩니다.</span>}
            </div>

            <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                {TABS.map((t) => (
                    <button
                        key={t.key}
                        type="button"
                        onClick={() => setTab(t.key)}
                        style={{ fontWeight: tab === t.key ? 'bold' : 'normal', textDecoration: tab === t.key ? 'underline' : 'none' }}
                    >
                        {t.label}
                    </button>
                ))}
            </div>

            {tab === 'monitor' && (
                <div style={{ display: 'flex', gap: 16, flex: 1, minHeight: 0 }}>
                    <div style={{ flex: 1.4, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
                        <div style={{ marginBottom: 8, color: '#64748b' }}>총 {monitorTotalCnt}건{monitorLoading ? ' (조회 중...)' : ''}</div>
                        <div style={{ flex: 1, minHeight: 0 }}>
                            <AppAgGrid
                                theme={gridTheme}
                                rowData={monitorList}
                                columnDefs={monitorColumnDefs}
                                defaultColDef={{ sortable: true, resizable: true }}
                                pagination
                                paginationPageSize={PAGE_UNIT}
                            />
                        </div>
                    </div>
                    <div style={{ flex: 1, borderLeft: '1px solid #e2e8f0', paddingLeft: 16, overflowY: 'auto' }}>
                        <h3>{monitorForm.mode === 'Ins' ? '신규 등록' : `수정 — ${monitorForm.mhsMonitorcd}`}</h3>
                        <form onSubmit={handleMonitorSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                            <label>
                                모니터명
                                <input type="text" value={monitorForm.mhsMonitornm}
                                    onChange={(e) => setMonitorForm((p) => ({ ...p, mhsMonitornm: e.target.value }))} />
                            </label>
                            <label>
                                뷰타입
                                <input type="text" value={monitorForm.mhsMviewtype || ''}
                                    onChange={(e) => setMonitorForm((p) => ({ ...p, mhsMviewtype: e.target.value }))} />
                            </label>
                            <label>
                                비고
                                <textarea rows={2} value={monitorForm.mhsRemark || ''}
                                    onChange={(e) => setMonitorForm((p) => ({ ...p, mhsRemark: e.target.value }))} />
                            </label>
                            <div style={{ display: 'flex', gap: 8 }}>
                                <button type="submit">{monitorForm.mode === 'Ins' ? '등록' : '수정'}</button>
                                <button type="button" onClick={() => setMonitorForm(emptyMonitorForm)}>취소</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {tab === 'class' && (
                <div style={{ display: 'flex', gap: 16, flex: 1, minHeight: 0 }}>
                    <div style={{ flex: 1.4, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
                        <div style={{ marginBottom: 8, color: '#64748b' }}>총 {classTotalCnt}건{classLoading ? ' (조회 중...)' : ''}</div>
                        <div style={{ flex: 1, minHeight: 0 }}>
                            <AppAgGrid
                                theme={gridTheme}
                                rowData={classList}
                                columnDefs={classColumnDefs}
                                defaultColDef={{ sortable: true, resizable: true }}
                                pagination
                                paginationPageSize={PAGE_UNIT}
                            />
                        </div>
                    </div>
                    <div style={{ flex: 1, borderLeft: '1px solid #e2e8f0', paddingLeft: 16, overflowY: 'auto' }}>
                        <h3>{classForm.mode === 'Ins' ? '신규 등록' : `수정 — ${classForm.mhsClasscd}`}</h3>
                        <form onSubmit={handleClassSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                            <label>
                                강의실(모니터코드)
                                <input type="text" value={classForm.mhsClassroomnm}
                                    onChange={(e) => setClassForm((p) => ({ ...p, mhsClassroomnm: e.target.value }))} />
                            </label>
                            <label>
                                강의명
                                <input type="text" value={classForm.mhsClassnm}
                                    onChange={(e) => setClassForm((p) => ({ ...p, mhsClassnm: e.target.value }))} />
                            </label>
                            <label>
                                강사명
                                <input type="text" value={classForm.mhsTeachernm || ''}
                                    onChange={(e) => setClassForm((p) => ({ ...p, mhsTeachernm: e.target.value }))} />
                            </label>
                            <label>
                                시작일(YYYYMMDD)
                                <input type="text" value={classForm.mhsClassstartday || ''}
                                    onChange={(e) => setClassForm((p) => ({ ...p, mhsClassstartday: e.target.value }))} />
                            </label>
                            <label>
                                종료일(YYYYMMDD)
                                <input type="text" value={classForm.mhsClassendday || ''}
                                    onChange={(e) => setClassForm((p) => ({ ...p, mhsClassendday: e.target.value }))} />
                            </label>
                            <label>
                                요일(콤마구분, 1=일~7=토)
                                <input type="text" value={classForm.mhsClassdayofweek || ''}
                                    onChange={(e) => setClassForm((p) => ({ ...p, mhsClassdayofweek: e.target.value }))} />
                            </label>
                            <label>
                                시작시간(HHmm)
                                <input type="text" value={classForm.mhsClassstarttime || ''}
                                    onChange={(e) => setClassForm((p) => ({ ...p, mhsClassstarttime: e.target.value }))} />
                            </label>
                            <label>
                                종료시간(HHmm)
                                <input type="text" value={classForm.mhsClassendtime || ''}
                                    onChange={(e) => setClassForm((p) => ({ ...p, mhsClassendtime: e.target.value }))} />
                            </label>
                            <label>
                                소개
                                <textarea rows={2} value={classForm.mhsClassintro || ''}
                                    onChange={(e) => setClassForm((p) => ({ ...p, mhsClassintro: e.target.value }))} />
                            </label>
                            <div style={{ display: 'flex', gap: 8 }}>
                                <button type="submit">{classForm.mode === 'Ins' ? '등록' : '수정'}</button>
                                <button type="button" onClick={() => setClassForm(emptyClassForm)}>취소</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {tab === 'viewConn' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 12, flex: 1, minHeight: 0, overflowY: 'auto' }}>
                    <form onSubmit={handleConnInsert} style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                        <label>
                            모니터
                            <select value={connForm.mhsMonitorcd} onChange={(e) => handleConnMonitorChange(e.target.value)} style={{ marginLeft: 4 }}>
                                <option value="">선택</option>
                                {monitorCombo.map((m) => (
                                    <option key={m.mhsMonitorcd} value={m.mhsMonitorcd}>{m.mhsMonitornm}</option>
                                ))}
                            </select>
                        </label>
                        <label>
                            강의
                            <select value={connForm.mhsClasscd} onChange={(e) => setConnForm((p) => ({ ...p, mhsClasscd: e.target.value }))} style={{ marginLeft: 4 }}>
                                <option value="">선택</option>
                                {classCombo.map((c) => (
                                    <option key={c.mhsClasscd} value={c.mhsClasscd}>{c.mhsClassnm}</option>
                                ))}
                            </select>
                        </label>
                        <button type="submit">편성 등록</button>
                        <span style={{ color: '#64748b' }}>{connLoading ? '조회 중...' : `총 ${connList.length}건`}</span>
                    </form>

                    <table style={{ borderCollapse: 'collapse', width: '100%' }}>
                        <thead>
                            <tr style={{ background: '#f8fafc' }}>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>강의명</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>강사명</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>시작~종료</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>요일</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}></th>
                            </tr>
                        </thead>
                        <tbody>
                            {connList.map((c) => (
                                <tr key={c.mhsConnSeq}>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{c.mhsClassnm}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{c.mhsTeachernm}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{c.mhsClassstarttime} ~ {c.mhsClassendtime}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{c.mhsClassdayofweek}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>
                                        <button type="button" onClick={() => handleConnDelete(c.mhsConnSeq)}>삭제</button>
                                    </td>
                                </tr>
                            ))}
                            {connList.length === 0 && (
                                <tr><td colSpan={5} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>모니터를 선택해 주세요.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
