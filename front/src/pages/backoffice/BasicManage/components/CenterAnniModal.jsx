import { useCallback, useEffect, useMemo, useState, Suspense, lazy } from 'react';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';
import { gridDateFormatter } from '@/lib/formatters.js';
import '@/style/Modal.css';

const CenterAnniFormModal = lazy(() => import('./CenterAnniFormModal.jsx'));

const PAGE_UNIT = 20;
const EMPTY_FORM = { centerAnniStartDay: '', centerAnniEndDay: '', startTime: '', endTime: '', brodCode: '' };

// YYYY-MM-DD(네이티브 date input) → YYYYMMDD(DB 저장 형식)로 변환
const stripDash = (v) => (v || '').replace(/-/g, '');

// 매장 기념일 관리 — 레거시(centerDetail.jsp의 center_Anniver() 팝업)를 참고해서
// 별도 페이지 이동 없이 매장 리스트 화면 위에 모달로 띄운다.
const CenterAnniModal = ({ open, centerId, onClose }) => {
    const [rowData, setRowData] = useState([]);
    const [totalCnt, setTotalCnt] = useState(0);
    const [brodOptions, setBrodOptions] = useState([]);
    const [loading, setLoading] = useState(false);

    const [formModalOpen, setFormModalOpen] = useState(false);
    const [form, setForm] = useState(EMPTY_FORM);

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

    const handleDelete = useCallback(async (centerAnniday) => {
        const result = await Swal.fire({
            icon: 'question', title: '기념일 삭제', text: '삭제하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!result.isConfirmed) return;

        await fnAjaxFetch({ url: `${URL.CENTER_ANNI_INFO}/${centerAnniday}.do`, method: 'DELETE' });
        loadList();
    }, [loadList]);

    const columnDefs = useMemo(() => ([
        { field: 'centerAnniStartDay', headerName: '시작일', width: 120, valueFormatter: gridDateFormatter },
        { field: 'centerAnniEndDay', headerName: '종료일', width: 120, valueFormatter: gridDateFormatter },
        { field: 'startTime', headerName: '시작시간', width: 100 },
        { field: 'endTime', headerName: '종료시간', width: 100 },
        { field: 'brodName', headerName: '방송콘텐츠', flex: 1, minWidth: 160 },
        {
            headerName: '삭제', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button type="button" className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={() => handleDelete(p.data.centerAnniday)}>삭제</button>
            ),
        },
    ]), [handleDelete]);

    const loadBrodCombo = useCallback(async () => {
        const res = await fnAjaxFetch({ url: URL.CENTER_ANNI_BROD_COMBO, method: 'GET', showLoading: false });
        setBrodOptions(res?.data?.result?.resultList ?? []);
    }, []);

    useEffect(() => {
        if (!open || !centerId) return;
        loadList();
        loadBrodCombo();
    }, [open, centerId, loadList, loadBrodCombo]);

    const handleOpenFormModal = useCallback(() => {
        setForm(EMPTY_FORM);
        setFormModalOpen(true);
    }, []);

    const handleSubmit = useCallback(async () => {
        const startDay = stripDash(form.centerAnniStartDay);
        const endDay = stripDash(form.centerAnniEndDay);

        if (!startDay || !endDay) {
            await Swal.fire({ icon: 'warning', title: '입력 확인', text: '시작일/종료일을 입력해 주세요.' });
            return;
        }
        if (startDay > endDay) {
            await Swal.fire({ icon: 'warning', title: '입력 확인', text: '시작일이 종료일보다 늦을 수 없습니다.' });
            return;
        }

        const checkRes = await fnAjaxFetch({
            url: URL.CENTER_ANNI_CNT_CHECK,
            method: 'POST',
            data: { centerId, centerAnniStartDay: startDay, centerAnniEndDay: endDay },
        });
        const overlapCnt = Number(checkRes?.data?.result?.result ?? 0);
        if (overlapCnt > 0) {
            await Swal.fire({ icon: 'warning', title: '기간 중복', text: '기존 기념일과 기간이 겹칩니다.' });
            return;
        }

        const ok = await Swal.fire({
            icon: 'question', title: '기념일 등록', text: '등록하시겠습니까?',
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const res = await fnAjaxFetch({
            url: URL.CENTER_ANNI_UPDATE,
            method: 'POST',
            data: { ...form, centerAnniStartDay: startDay, centerAnniEndDay: endDay, centerId, mode: 'Ins' },
        });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || '등록되었습니다.' });
            setFormModalOpen(false);
            loadList();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || '등록 중 오류가 발생했습니다.' });
        }
    }, [form, centerId, loadList]);

    if (!open) return null;

    return (
        <>
            <div className="modal-backdrop-custom" onClick={onClose} />
            <div className="modal-custom">
                <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable"
                    style={{ width: 900, maxWidth: '95%', backgroundColor: 'var(--bs-body-bg, #fff)' }}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <div className="modal-title">
                                <h2 className="modal-title__title">매장 기념일 관리 — {centerId}</h2>
                            </div>
                            <button type="button" className="modal-close" aria-label="Close" onClick={onClose} />
                        </div>

                        <div className="modal-body tab-content">
                            <div className="modal-body__content tab-pane show active">
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                                    <span style={{ color: '#64748b' }}>
                                        총 {totalCnt}건{loading ? ' (조회 중...)' : ''}
                                    </span>
                                    <button type="button" className="btn btn-primary btn-default__blue"
                                        onClick={handleOpenFormModal}>기념일 등록</button>
                                </div>
                                <div className="ag-theme-material" style={{ height: 480, width: '100%' }}>
                                    <AppAgGrid
                                        theme={gridTheme}
                                        rowData={rowData}
                                        columnDefs={columnDefs}
                                        defaultColDef={{ sortable: true, resizable: true }}
                                        pagination
                                        paginationPageSize={PAGE_UNIT}
                                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="modal-footer">
                            <div className="modal-footer__right">
                                <button type="button" className="btn btn-action__lightblue" onClick={onClose}>닫기</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <Suspense fallback={null}>
                {formModalOpen && (
                    <CenterAnniFormModal
                        open={formModalOpen}
                        form={form}
                        setForm={setForm}
                        brodOptions={brodOptions}
                        onClose={() => setFormModalOpen(false)}
                        onSubmit={handleSubmit}
                    />
                )}
            </Suspense>
        </>
    );
};

export default CenterAnniModal;
