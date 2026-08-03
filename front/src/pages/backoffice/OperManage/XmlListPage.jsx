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
    mode: 'Ins',
    xmlSeq: '',
    workGubun: '',
    xmlProcessName: '',
    processRemark: '',
    xmlInputParam: '',
    xmlOutputParam: '',
    xmlInputParamSample: '',
    xmlExplain: '',
};

export default function XmlListPage() {
    const navigate = useNavigate();
    const [rowData, setRowData] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [workGubunOptions, setWorkGubunOptions] = useState([]);
    const [form, setForm] = useState(emptyForm);
    const [preview, setPreview] = useState('');
    const [loading, setLoading] = useState(false);

    const columnDefs = useMemo(() => ([
        { field: 'xmlProcessName', headerName: '명령어(Process Name)', width: 200 },
        { field: 'codeNm', headerName: '업무구분', width: 130 },
        { field: 'processRemark', headerName: '설명', flex: 1, minWidth: 200 },
        {
            headerName: '', width: 160,
            cellRenderer: (p) => (
                <>
                    <button type="button" onClick={() => handleEdit(p.data.xmlSeq)}>수정</button>{' '}
                    <button type="button" onClick={() => handleDelete(p.data.xmlSeq)}>삭제</button>
                </>
            ),
        },
        // eslint-disable-next-line react-hooks/exhaustive-deps
    ]), []);

    const loadList = useCallback(async (keyword) => {
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.XML_LIST,
                method: 'POST',
                data: { searchKeyword: keyword ?? '', pageIndex: 1, pageUnit: PAGE_UNIT },
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

    const loadWorkGubunCombo = useCallback(async () => {
        const res = await fnAjaxFetch({ url: URL.XML_WORK_GUBUN_COMBO, method: 'GET', showLoading: false });
        setWorkGubunOptions(res?.data?.result?.resultList ?? []);
    }, []);

    useEffect(() => {
        if (!getCookie('accessToken')) {
            navigate('/login', { replace: true });
            return;
        }
        loadList('');
        loadWorkGubunCombo();
    }, [loadList, loadWorkGubunCombo, navigate]);

    const handleSearch = (e) => {
        e.preventDefault();
        loadList(searchKeyword);
    };

    const handleEdit = async (xmlSeq) => {
        const res = await fnAjaxFetch({ url: `${URL.XML_INFO}/${xmlSeq}.do`, method: 'GET' });
        const detail = res?.data?.result?.result;
        if (detail) {
            setForm({ ...detail, mode: 'Edt' });
            setPreview('');
        }
    };

    const handleNew = () => {
        setForm(emptyForm);
        setPreview('');
    };

    const handleDelete = async (xmlSeq) => {
        const result = await Swal.fire({
            icon: 'question', title: 'XML 정보 삭제', text: '삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;

        await fnAjaxFetch({ url: `${URL.XML_INFO}/${xmlSeq}.do`, method: 'DELETE' });
        loadList(searchKeyword);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!form.workGubun || !form.xmlProcessName) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: '업무구분/명령어(Process Name)를 입력해 주세요.' });
            return;
        }

        if (form.mode === 'Ins') {
            const checkRes = await fnAjaxFetch({
                url: URL.XML_PROCESS_CHECK,
                method: 'GET',
                param: { xmlProcessName: form.xmlProcessName },
            });
            const dupCnt = Number(checkRes?.data?.result?.result ?? 0);
            if (dupCnt > 0) {
                Swal.fire({ icon: 'warning', title: '중복', text: '이미 등록된 명령어입니다.' });
                return;
            }
        }

        await fnAjaxFetch({ url: URL.XML_UPDATE, method: 'POST', data: form });
        setForm(emptyForm);
        loadList(searchKeyword);
    };

    const handlePreview = async (type) => {
        if (!form.xmlSeq) {
            Swal.fire({ icon: 'warning', title: '미리보기 불가', text: '저장된 항목만 미리보기가 가능합니다.' });
            return;
        }
        const base = type === 'json' ? URL.XML_PREVIEW_JSON : URL.XML_PREVIEW_XML;
        const res = await fnAjaxFetch({ url: `${base}/${form.xmlSeq}.do`, method: 'GET' });
        setPreview(res?.data?.result?.result ?? '');
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                <h2 style={{ margin: 0 }}>XML(장비 통신 명령) 정보 관리</h2>
                <button type="button" onClick={handleNew}>신규 등록</button>
            </div>

            <div style={{ display: 'flex', gap: 16, flex: 1, minHeight: 0 }}>
                <div style={{ flex: 1.4, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
                    <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                        <input
                            type="text"
                            placeholder="명령어/설명 검색"
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

                <div style={{ flex: 1, borderLeft: '1px solid #e2e8f0', paddingLeft: 16, overflowY: 'auto' }}>
                    <h3>{form.mode === 'Ins' ? '신규 등록' : `수정 — ${form.xmlSeq}`}</h3>
                    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                        <label>
                            업무구분
                            <select value={form.workGubun} onChange={(e) => setForm((p) => ({ ...p, workGubun: e.target.value }))}>
                                <option value="">선택</option>
                                {workGubunOptions.map((o) => (
                                    <option key={o.code} value={o.code}>{o.codeNm || o.code}</option>
                                ))}
                            </select>
                        </label>
                        <label>
                            명령어(Process Name)
                            <input type="text" value={form.xmlProcessName} disabled={form.mode !== 'Ins'}
                                onChange={(e) => setForm((p) => ({ ...p, xmlProcessName: e.target.value }))} />
                        </label>
                        <label>
                            설명
                            <input type="text" value={form.processRemark || ''}
                                onChange={(e) => setForm((p) => ({ ...p, processRemark: e.target.value }))} />
                        </label>
                        <label>
                            입력 파라미터(콤마로 구분, 예: DID_ID,DID_MAC)
                            <textarea rows={2} value={form.xmlInputParam || ''}
                                onChange={(e) => setForm((p) => ({ ...p, xmlInputParam: e.target.value }))} />
                        </label>
                        <label>
                            입력 파라미터 샘플값(콤마로 구분)
                            <textarea rows={2} value={form.xmlInputParamSample || ''}
                                onChange={(e) => setForm((p) => ({ ...p, xmlInputParamSample: e.target.value }))} />
                        </label>
                        <label>
                            출력 파라미터
                            <textarea rows={2} value={form.xmlOutputParam || ''}
                                onChange={(e) => setForm((p) => ({ ...p, xmlOutputParam: e.target.value }))} />
                        </label>
                        <label>
                            비고
                            <textarea rows={2} value={form.xmlExplain || ''}
                                onChange={(e) => setForm((p) => ({ ...p, xmlExplain: e.target.value }))} />
                        </label>
                        <div style={{ display: 'flex', gap: 8 }}>
                            <button type="submit">{form.mode === 'Ins' ? '등록' : '수정'}</button>
                            <button type="button" onClick={() => handlePreview('json')}>JSON 미리보기</button>
                            <button type="button" onClick={() => handlePreview('xml')}>XML 미리보기</button>
                        </div>
                    </form>
                    {preview && (
                        <pre style={{ marginTop: 12, background: '#f8fafc', padding: 8, whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>
                            {preview}
                        </pre>
                    )}
                </div>
            </div>
        </div>
    );
}
