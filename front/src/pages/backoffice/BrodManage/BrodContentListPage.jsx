import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const PAGE_UNIT = 20;
const emptyForm = { mode: 'Ins', brodCode: '', brodName: '', brodTotalTime: '', brodInterval: '', basicBrodCode: '', brodUseYn: 'Y' };

export default function BrodContentListPage() {
    const navigate = useNavigate();
    const [list, setList] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [form, setForm] = useState(emptyForm);
    const [intervalCombo, setIntervalCombo] = useState([]);
    const [basicCombo, setBasicCombo] = useState([]);
    const [selectedRows, setSelectedRows] = useState([]);
    const [loading, setLoading] = useState(false);

    const columnDefs = useMemo(() => ([
        { field: 'brodCode', headerName: '방송코드', width: 160, checkboxSelection: true, headerCheckboxSelection: true },
        { field: 'brodName', headerName: '방송명', flex: 1, minWidth: 160 },
        { field: 'codeNm', headerName: '재생간격', width: 110 },
        { field: 'brodTotalTime', headerName: '총재생시간(초)', width: 120 },
        { field: 'orignlFileNm', headerName: '기초방송', width: 160 },
        { field: 'brodUseYn', headerName: '사용여부', width: 90 },
        // eslint-disable-next-line react-hooks/exhaustive-deps
    ]), []);

    const loadList = useCallback(async (keyword) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.BROD_CONTENT_LIST,
                method: 'POST',
                data: { searchCondition: 'brodName', searchKeyword: keyword ?? '', pageIndex: 1, pageUnit: PAGE_UNIT },
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

    const loadFormData = useCallback(async (brodCode, mode) => {
        const res = await fnAjaxFetch({
            url: URL.BROD_CONTENT_FORM_DATA, method: 'GET',
            param: { brodCode: brodCode ?? '', mode }, showLoading: false,
        });
        setIntervalCombo(res?.data?.result?.brodInterval ?? []);
        setBasicCombo(res?.data?.result?.basicInfo ?? []);
        return res?.data?.result?.regist;
    }, []);

    const handleSearch = (e) => {
        e.preventDefault();
        loadList(searchKeyword);
    };

    const handleNew = async () => {
        await loadFormData('', 'Ins');
        setForm(emptyForm);
    };

    const handleEdit = async (row) => {
        const detail = await loadFormData(row.brodCode, 'Edt');
        setForm({ ...(detail ?? row), mode: 'Edt' });
    };

    const handleDelete = async () => {
        if (selectedRows.length === 0) return;
        const result = await Swal.fire({
            icon: 'warning', title: '방송(음원) 콘텐츠 삭제', text: `선택한 ${selectedRows.length}건을 삭제하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '삭제', cancelButtonText: '취소',
        });
        if (!result.isConfirmed) return;

        await fnAjaxFetch({
            url: URL.BROD_CONTENT_DELETE_BULK, method: 'POST',
            data: { delBrodCode: selectedRows.map((r) => r.brodCode).join(',') },
        });
        setSelectedRows([]);
        loadList(searchKeyword);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!form.brodName || !form.brodInterval) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: '방송명/재생간격을 입력해 주세요.' });
            return;
        }
        await fnAjaxFetch({ url: URL.BROD_CONTENT_UPDATE, method: 'POST', data: form });
        setForm(emptyForm);
        loadList(searchKeyword);
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <h2 style={{ margin: '0 0 12px' }}>방송(음원) 콘텐츠 관리</h2>
            <p style={{ margin: '0 0 12px', color: '#94a3b8', fontSize: 13 }}>
                파일 편성(시간대별 음원 배치)은 <a href="/backoffice/sub/conManage/muti/edit">콘텐츠 편성 화면</a>에서 진행해 주세요. 편성표 생성/엑셀 다운로드는 후속 작업으로 예정되어 있습니다.
            </p>

            <div style={{ display: 'flex', gap: 16, flex: 1, minHeight: 0 }}>
                <div style={{ flex: 1.4, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
                    <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                        <input type="text" placeholder="방송명 검색" value={searchKeyword} onChange={(e) => setSearchKeyword(e.target.value)} />
                        <button type="submit">검색</button>
                        <button type="button" onClick={handleNew}>신규 등록</button>
                        <button type="button" onClick={handleDelete} disabled={selectedRows.length === 0} style={{ color: '#dc2626' }}>선택 삭제</button>
                        <span style={{ marginLeft: 'auto', alignSelf: 'center', color: '#64748b' }}>
                            총 {totalCnt}건{loading ? ' (조회 중...)' : ''}
                        </span>
                    </form>
                    <div style={{ flex: 1, minHeight: 0 }}>
                        <AppAgGrid
                            theme={gridTheme}
                            rowData={list}
                            columnDefs={columnDefs}
                            defaultColDef={{ sortable: true, resizable: true }}
                            rowSelection="multiple"
                            pagination
                            paginationPageSize={PAGE_UNIT}
                            onRowClicked={(e) => handleEdit(e.data)}
                            onSelectionChanged={(e) => setSelectedRows(e.api.getSelectedRows())}
                        />
                    </div>
                </div>

                <div style={{ flex: 1, borderLeft: '1px solid #e2e8f0', paddingLeft: 16, overflowY: 'auto' }}>
                    <h3>{form.mode === 'Ins' ? '신규 등록' : `수정 — ${form.brodCode}`}</h3>
                    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                        <label>
                            방송명
                            <input type="text" value={form.brodName || ''} onChange={(e) => setForm((p) => ({ ...p, brodName: e.target.value }))} />
                        </label>
                        <label>
                            재생간격
                            <select value={form.brodInterval || ''} onChange={(e) => setForm((p) => ({ ...p, brodInterval: e.target.value }))}>
                                <option value="">선택</option>
                                {intervalCombo.map((c) => (
                                    <option key={c.code} value={c.code}>{c.codeNm}</option>
                                ))}
                            </select>
                        </label>
                        <label>
                            기초 방송
                            <select value={form.basicBrodCode || ''} onChange={(e) => setForm((p) => ({ ...p, basicBrodCode: e.target.value }))}>
                                <option value="">선택 안 함</option>
                                {basicCombo.map((c) => (
                                    <option key={c.basicCode} value={c.basicCode}>{c.basicGroupNm}</option>
                                ))}
                            </select>
                        </label>
                        <label>
                            사용 여부
                            <select value={form.brodUseYn || 'Y'} onChange={(e) => setForm((p) => ({ ...p, brodUseYn: e.target.value }))}>
                                <option value="Y">사용</option>
                                <option value="N">미사용</option>
                            </select>
                        </label>
                        <button type="submit">{form.mode === 'Ins' ? '등록' : '수정'}</button>
                    </form>
                </div>
            </div>
        </div>
    );
}
