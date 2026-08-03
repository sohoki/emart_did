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
    mode: 'Ins', conSeq: '', conNm: '', conType: '', conScreen: '',
    conWidth: '1080', conHeight: '1980', conMid: '540', conUseYn: 'Y',
};

export default function ContentMutiListPage() {
    const navigate = useNavigate();
    const [rowData, setRowData] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [conTypeOptions, setConTypeOptions] = useState([]);
    const [screenTypeOptions, setScreenTypeOptions] = useState([]);
    const [form, setForm] = useState(emptyForm);
    const [loading, setLoading] = useState(false);

    const columnDefs = useMemo(() => ([
        { field: 'conSeq', headerName: '순번', width: 90 },
        { field: 'conNm', headerName: '콘텐츠명', flex: 1, minWidth: 200 },
        { field: 'codeNm', headerName: '유형', width: 120 },
        { field: 'conWidth', headerName: '가로', width: 80 },
        { field: 'conHeight', headerName: '세로', width: 80 },
        { field: 'schCnt', headerName: '연결 스케줄', width: 100 },
        {
            headerName: '', width: 220,
            cellRenderer: (p) => (
                <>
                    <button type="button" onClick={() => handleEdit(p.data.conSeq)}>수정</button>{' '}
                    <button type="button" onClick={() => navigate(`/backoffice/sub/conManage/muti/edit?conSeq=${p.data.conSeq}`)}>편성</button>{' '}
                    <button type="button" onClick={() => handleDelete(p.data.conSeq)}>삭제</button>
                </>
            ),
        },
        // eslint-disable-next-line react-hooks/exhaustive-deps
    ]), []);

    const loadList = useCallback(async (keyword) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.CON_MUTI_LIST,
                method: 'POST',
                data: { searchCondition: 'conNm', searchKeyword: keyword ?? '', pageIndex: 1, pageUnit: PAGE_UNIT },
                showLoading: false,
            });
            const list = res?.data?.result?.resultList ?? [];
            setRowData(list);
            setTotalCnt(res?.data?.result?.totalCnt ?? list.length);
        } finally {
            setLoading(false);
        }
    }, []);

    const loadFormCombo = useCallback(async () => {
        const res = await fnAjaxFetch({ url: URL.CON_MUTI_FORM_DATA, method: 'GET', param: { mode: 'Ins' }, showLoading: false });
        setConTypeOptions(res?.data?.result?.selectConType ?? []);
        setScreenTypeOptions(res?.data?.result?.selectScreenType ?? []);
    }, []);

    useEffect(() => {
        if (!getCookie('accessToken')) {
            navigate('/login', { replace: true });
            return;
        }
        loadList('');
        loadFormCombo();
    }, [loadList, loadFormCombo, navigate]);

    const handleSearch = (e) => {
        e.preventDefault();
        loadList(searchKeyword);
    };

    const handleNew = () => setForm(emptyForm);

    const handleEdit = async (conSeq) => {
        const res = await fnAjaxFetch({ url: URL.CON_MUTI_FORM_DATA, method: 'GET', param: { mode: 'Edt', conSeq } });
        const detail = res?.data?.result?.regist;
        if (detail) setForm(detail);
    };

    const handleDelete = async (conSeq) => {
        const result = await Swal.fire({
            icon: 'question', title: '콘텐츠 삭제', text: '연결된 파일/상세페이지가 함께 삭제됩니다. 삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;

        await fnAjaxFetch({ url: `${URL.CON_MUTI_INFO}/${conSeq}.do`, method: 'DELETE' });
        loadList(searchKeyword);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!form.conNm || !form.conType) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: '콘텐츠명/유형을 입력해 주세요.' });
            return;
        }
        await fnAjaxFetch({ url: URL.CON_MUTI_UPDATE, method: 'POST', data: form });
        setForm(emptyForm);
        loadList(searchKeyword);
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                <h2 style={{ margin: 0 }}>멀티페이지 콘텐츠 관리</h2>
                <button type="button" onClick={handleNew}>신규 등록</button>
            </div>

            <div style={{ display: 'flex', gap: 16, flex: 1, minHeight: 0 }}>
                <div style={{ flex: 1.4, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
                    <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                        <input type="text" placeholder="콘텐츠명 검색" value={searchKeyword} onChange={(e) => setSearchKeyword(e.target.value)} />
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
                    <h3>{form.mode === 'Ins' ? '신규 등록' : `수정 — ${form.conSeq}`}</h3>
                    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                        <label>
                            콘텐츠명
                            <input type="text" value={form.conNm} onChange={(e) => setForm((p) => ({ ...p, conNm: e.target.value }))} />
                        </label>
                        <label>
                            유형
                            <select value={form.conType} onChange={(e) => setForm((p) => ({ ...p, conType: e.target.value }))}>
                                <option value="">선택</option>
                                {conTypeOptions.map((o) => (
                                    <option key={o.code} value={o.code}>{o.codeNm || o.code}</option>
                                ))}
                            </select>
                        </label>
                        <label>
                            화면분할
                            <select value={form.conScreen || ''} onChange={(e) => setForm((p) => ({ ...p, conScreen: e.target.value }))}>
                                <option value="">선택</option>
                                {screenTypeOptions.map((o) => (
                                    <option key={o.code} value={o.code}>{o.codeNm || o.code}</option>
                                ))}
                            </select>
                        </label>
                        <label>
                            가로(px)
                            <input type="text" value={form.conWidth || ''} onChange={(e) => setForm((p) => ({ ...p, conWidth: e.target.value }))} />
                        </label>
                        <label>
                            세로(px)
                            <input type="text" value={form.conHeight || ''} onChange={(e) => setForm((p) => ({ ...p, conHeight: e.target.value }))} />
                        </label>
                        <label>
                            사용여부
                            <select value={form.conUseYn || 'Y'} onChange={(e) => setForm((p) => ({ ...p, conUseYn: e.target.value }))}>
                                <option value="Y">사용</option>
                                <option value="N">미사용</option>
                            </select>
                        </label>
                        <button type="submit">{form.mode === 'Ins' ? '등록' : '수정'}</button>
                    </form>
                    <p style={{ color: '#94a3b8', fontSize: 12, marginTop: 12 }}>
                        페이지별 파일 배치/편성(순서·재생시간)은 별도 편집 화면에서 진행합니다.
                    </p>
                </div>
            </div>
        </div>
    );
}
