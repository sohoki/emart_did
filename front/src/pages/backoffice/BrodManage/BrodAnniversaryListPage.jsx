import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const emptyForm = {
    mode: 'Ins', brodAnnSeq: '', brodCode: '', anniverName: '', anniversaryGubun: '',
    anniverStartday: '', anniverEndday: '', anniversaryStartTime: '', anniversaryTime: '',
};

export default function BrodAnniversaryListPage() {
    const navigate = useNavigate();
    const [brodCode, setBrodCode] = useState('');
    const [brodDay, setBrodDay] = useState('');
    const [list, setList] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [form, setForm] = useState(emptyForm);
    const [loading, setLoading] = useState(false);

    const loadList = useCallback(async (code, day) => {
        if (!code) {
            setList([]);
            setTotalCnt(0);
            return;
        }
        setLoading(true);
        try {
            const res = await fnAjaxFetch({
                url: URL.BROD_ANNIVER_LIST,
                method: 'POST',
                data: { brodCode: code, brodDay: day || '' },
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
        }
    }, [navigate]);

    const handleSearch = (e) => {
        e.preventDefault();
        loadList(brodCode, brodDay);
    };

    const handleNew = () => setForm({ ...emptyForm, brodCode });

    const handleEdit = async (brodAnnSeq) => {
        const res = await fnAjaxFetch({ url: URL.BROD_ANNIVER_DETAIL, method: 'POST', data: { brodAnnSeq } });
        const detail = res?.data?.result?.result;
        if (detail) setForm({ ...detail, mode: 'Edt' });
    };

    const handleDelete = async (brodAnnSeq) => {
        const result = await Swal.fire({
            icon: 'question', title: '기념일 삭제', text: '삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;
        await fnAjaxFetch({ url: `${URL.BROD_ANNIVER_DELETE}/${brodAnnSeq}.do`, method: 'DELETE' });
        loadList(brodCode, brodDay);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!form.brodCode || !form.anniverName) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: '방송코드/기념일명을 입력해 주세요.' });
            return;
        }
        await fnAjaxFetch({ url: URL.BROD_ANNIVER_UPDATE, method: 'POST', data: form });
        setForm({ ...emptyForm, brodCode: form.brodCode });
        loadList(form.brodCode, brodDay);
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', padding: 16, boxSizing: 'border-box' }}>
            <h2 style={{ margin: '0 0 12px' }}>방송 기념일 관리</h2>

            <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, marginBottom: 12, alignItems: 'center' }}>
                <input type="text" placeholder="방송 코드(BROD_...)" value={brodCode} onChange={(e) => setBrodCode(e.target.value)} style={{ width: 160 }} />
                <input type="text" placeholder="기준일(YYYYMMDD, 선택)" value={brodDay} onChange={(e) => setBrodDay(e.target.value)} style={{ width: 150 }} />
                <button type="submit">조회</button>
                <button type="button" onClick={handleNew} disabled={!brodCode}>신규 등록</button>
                <span style={{ color: '#64748b' }}>총 {totalCnt}건{loading ? ' (조회 중...)' : ''}</span>
            </form>

            <div style={{ display: 'flex', gap: 16, flex: 1, minHeight: 0 }}>
                <div style={{ flex: 1.4, overflowY: 'auto' }}>
                    <table style={{ borderCollapse: 'collapse', width: '100%' }}>
                        <thead>
                            <tr style={{ background: '#f8fafc' }}>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>기념일명</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>구분</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>시작일</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}>종료일</th>
                                <th style={{ border: '1px solid #e2e8f0', padding: 6 }}></th>
                            </tr>
                        </thead>
                        <tbody>
                            {list.map((item) => (
                                <tr key={item.brodAnnSeq}>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{item.anniverName}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{item.codeNm}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{item.anniverStartday}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>{item.anniverEndday}</td>
                                    <td style={{ border: '1px solid #e2e8f0', padding: 6 }}>
                                        <button type="button" onClick={() => handleEdit(item.brodAnnSeq)}>수정</button>{' '}
                                        <button type="button" onClick={() => handleDelete(item.brodAnnSeq)}>삭제</button>
                                    </td>
                                </tr>
                            ))}
                            {list.length === 0 && (
                                <tr><td colSpan={5} style={{ padding: 16, textAlign: 'center', color: '#94a3b8' }}>방송 코드를 입력해서 조회해 주세요.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>

                <div style={{ flex: 1, borderLeft: '1px solid #e2e8f0', paddingLeft: 16, overflowY: 'auto' }}>
                    <h3>{form.mode === 'Ins' ? '신규 등록' : `수정 — ${form.brodAnnSeq}`}</h3>
                    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                        <label>
                            방송 코드
                            <input type="text" value={form.brodCode} onChange={(e) => setForm((p) => ({ ...p, brodCode: e.target.value }))} disabled={form.mode === 'Ins'} />
                        </label>
                        <label>
                            기념일명
                            <input type="text" value={form.anniverName || ''} onChange={(e) => setForm((p) => ({ ...p, anniverName: e.target.value }))} />
                        </label>
                        <label>
                            시작일(YYYYMMDD)
                            <input type="text" value={form.anniverStartday || ''} onChange={(e) => setForm((p) => ({ ...p, anniverStartday: e.target.value }))} />
                        </label>
                        <label>
                            종료일(YYYYMMDD)
                            <input type="text" value={form.anniverEndday || ''} onChange={(e) => setForm((p) => ({ ...p, anniverEndday: e.target.value }))} />
                        </label>
                        <label>
                            시작시간(HHmm)
                            <input type="text" value={form.anniversaryStartTime || ''} onChange={(e) => setForm((p) => ({ ...p, anniversaryStartTime: e.target.value }))} />
                        </label>
                        <label>
                            재생시간(초)
                            <input type="text" value={form.anniversaryTime || ''} onChange={(e) => setForm((p) => ({ ...p, anniversaryTime: e.target.value }))} />
                        </label>
                        <button type="submit">{form.mode === 'Ins' ? '등록' : '수정'}</button>
                    </form>
                </div>
            </div>
        </div>
    );
}
