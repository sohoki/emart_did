import { useCallback, useEffect, useMemo, useRef, useState, Suspense, lazy } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { gridTheme } from '@/constants/agGridTheme.js';
import { useGridInfinite } from '@/hooks/grid/use-grid-infinite.js';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import { useResetForm } from '@/hooks/use-form.jsx';
import { useCommonDelete } from '@/hooks/use-common-delete.js';
import { useCommonCodeData, useCustomReqDataCombo } from '@/hooks/use-combo-data.js';
import Swal from '@/lib/swal.js';
import URL from '@/constants/URL.jsx';

const DidFormModal = lazy(() => import('./components/DidFormModal.jsx'));

const GROUP_MAPPING = { id: 'groupId', text: 'groupNm' };

const INITIAL_SEARCH_FORM = {
    searchCondition: '',
    searchKeyword: '',
    centerId: '',
};

const EMPTY_DID_FORM = {
    mode: 'Ins',
    didId: '',
    didNm: '',
    didMac: '',
    didIpaddr: '',
    roleCode: '',
    centerId: '',
    groupId: '',
    didModelType: '',
    didType: '',
    didOs: '',
    didIptype: '',
    didResolution: '',
    didWidth: '',
    didHeight: '',
    didStartTime: '',
    didEndTime: '',
    didUseYn: 'Y',
};

const EMPTY_COMBOS = {
    selectGroup: [],
    selectCenter: [],
};

// 레거시 didDetail.jsp의 check_form()에서 didModelType이 이 값(음원방송)일 때만
// 그룹정보(groupId) 필수 검증을 건너뜀 — 동일 규칙 유지
const GROUP_OPTIONAL_MODEL_TYPE = 'DIDRMODELTYPE03';

// 레거시 didList.jsp의 OS구분 아이콘 분기(안드로이드/윈도우/그 외→iOS)와 동일 규칙
const getOsIcon = (didOs) => {
    if (didOs === '안드로이드') return '/resource/img/android_icon.png';
    if (didOs === '윈도우') return '/resource/img/windows_icon.png';
    return '/resource/img/ios_icon.png';
};

export default function DidInfoList() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const gridApiRef = useRef(null);

    const [modalOpen, setModalOpen] = useState(false);
    const [didForm, setDidForm] = useState(EMPTY_DID_FORM);
    const [combos, setCombos] = useState(EMPTY_COMBOS);

    // 공통코드(EMT001~EMT011) 기반 콤보는 formData.do 번들 대신 다른 화면(CenterListPage 등)과
    // 동일하게 useCommonCodeData로 직접 조회한다 — 각 훅이 마운트 시 한 번만 호출됨
    const { options: typeOptions } = useCommonCodeData('EMT001');
    const { options: resolutionOptions } = useCommonCodeData('EMT002');
    const { options: ipTypeOptions } = useCommonCodeData('EMT003');
    const { options: modelTypeOptions } = useCommonCodeData('EMT004');
    const { options: osOptions } = useCommonCodeData('EMT011');

    // 관리부서(roleCode)는 결국 부서(LETTNAUTHORGROUPINFO) 정보라, formData.do의 selectRole
    // 대신 다른 화면(CenterFormModal/ManagerAdminFormModal)과 동일하게 GROUP_COMBO를 직접 사용
    const { options: roleOptions } = useCustomReqDataCombo({
        url: URL.GROUP_COMBO, method: 'GET', params: {}, mapping: GROUP_MAPPING,
    });

    // 설치지점/그룹정보 콤보는 공통코드가 아니라 각각 별도 서비스가 조합하는 값이라 여전히
    // formData.do?mode=Ins를 페이지 최초 진입 시 한 번만 호출해서 가져온다.
    useEffect(() => {
        let active = true;
        (async () => {
            const res = await fnAjaxFetch({
                url: URL.DID_FORM_DATA, method: 'GET', param: { mode: 'Ins' }, showLoading: false,
            });
            if (!active) return;
            const result = res?.data?.result || {};
            setCombos({
                selectGroup: result.selectGroup || [],
                selectCenter: result.selectCenter || [],
            });
        })();
        return () => { active = false; };
    }, []);

    const fetchDidList = useCallback(async (query) => {
        const res = await fnAjaxFetch({ url: URL.DID_LIST, method: 'POST', data: query });
        const data = res?.data;
        return {
            rows: data?.result?.resultList || [],
            total: data?.result?.totalCnt || 0,
        };
    }, []);

    const {
        onGridReady,
        defaultColDef,
        tempParams,
        setTempParams,
        handleSearch,
        refreshGrid,
    } = useGridInfinite({
        fetchApi: fetchDidList,
        pageUnit: 20,
        initialFilters: INITIAL_SEARCH_FORM,
    });

    const handleInputChange = useCallback((e) => {
        const { name, value } = e.target;
        setTempParams((prev) => ({ ...prev, [name]: value }));
    }, [setTempParams]);

    const onSearch = useCallback((pageIndex) => {
        handleSearch(pageIndex || 1);
    }, [handleSearch]);

    const onSearchKeyDown = useCallback((e) => {
        if (e.key === 'Enter') onSearch(1);
    }, [onSearch]);

    const { handleReset } = useResetForm(setTempParams, INITIAL_SEARCH_FORM);

    const handleOpenDidModal = useCallback(async (didId) => {
        if (!didId) {
            setDidForm(EMPTY_DID_FORM);
            setModalOpen(true);
            return;
        }
        const res = await fnAjaxFetch({
            url: URL.DID_FORM_DATA, method: 'GET', param: { mode: 'Edt', didId },
        });
        const obj = res?.data?.result?.regist;
        if (obj) {
            setDidForm({
                mode: 'Edt',
                didId: obj.didId || '',
                didNm: obj.didNm || '',
                didMac: obj.didMac || '',
                didIpaddr: obj.didIpaddr || '',
                roleCode: obj.roleCode || '',
                centerId: obj.centerId || '',
                groupId: obj.groupId || '',
                didModelType: obj.didModelType || '',
                didType: obj.didType || '',
                didOs: obj.didOs || '',
                didIptype: obj.didIptype || '',
                didResolution: obj.didResolution || '',
                didWidth: obj.didWidth || '',
                didHeight: obj.didHeight || '',
                didStartTime: obj.didStartTime || '',
                didEndTime: obj.didEndTime || '',
                didUseYn: obj.didUseYn || 'Y',
            });
            setModalOpen(true);
        }
    }, []);

    // 단말기 상세(DidDetailPage)의 "수정" 버튼에서 ?editDidId=xxx로 넘어오면 목록 진입과
    // 동시에 해당 단말기의 수정 모달을 자동으로 연다.
    useEffect(() => {
        const editDidId = searchParams.get('editDidId');
        if (editDidId) handleOpenDidModal(editDidId);
    }, [searchParams, handleOpenDidModal]);

    const handleSubmit = useCallback(async () => {
        if (!didForm.didNm) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '단말명을 입력해 주세요.' });
            return;
        }
        if (!didForm.roleCode) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '관리부서를 선택해 주세요.' });
            return;
        }
        if (!didForm.centerId) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '설치지점을 선택해 주세요.' });
            return;
        }
        if (didForm.didModelType !== GROUP_OPTIONAL_MODEL_TYPE && !didForm.groupId) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '단말 그룹정보를 선택해 주세요.' });
            return;
        }

        const action = didForm.mode === 'Ins' ? '등록' : '수정';
        const ok = await Swal.fire({
            icon: 'question', title: `단말기 ${action}`,
            html: `<b>${didForm.didNm}</b> ${action} 하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        // 시리얼포트/모니터/SW버전 등 레거시 화면에서도 숨겨진 채 고정값으로 저장되던 항목 — 동일하게 고정
        const payload = {
            ...didForm,
            didSwver: 'DIDSW02',
            didMonitercnt: 'Mointer01',
            didSerialtype: 'SERIAL_N',
        };

        const res = await fnAjaxFetch({ url: URL.DID_UPDATE, method: 'POST', data: payload });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: json?.resultMessage || `${action}되었습니다.` });
            setModalOpen(false);
            refreshGrid();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || `${action} 중 오류가 발생했습니다.` });
        }
    }, [didForm, refreshGrid]);

    const { handleDelete } = useCommonDelete({
        gridApiRef,
        URL: URL.DID_INFO,
        MESSAGE: '단말기 정보',
        reloadFunction: 'grid',
    });

    // 레거시 didList.jsp의 Restart_Did(code) — 체크된 단말들에게 콘텐츠재전송(REDOWN)/
    // 재부팅(RESTART) 명령을 일괄 발송. restartInfo는 선행 구분자 1자 + "didId|didMac" 콤마
    // 목록(백엔드가 substring(1)로 선행 문자를 그대로 잘라내므로 legacy와 동일하게 유지)이고,
    // 백엔드가 @RequestParam(폼 파라미터)으로 받기 때문에 JSON 바디가 아니라 쿼리스트링으로 전송한다.
    const handleBulkCommand = useCallback(async (xmlProceNm) => {
        const selectedRows = gridApiRef.current?.getSelectedRows() || [];
        if (selectedRows.length < 1) {
            await Swal.fire({ icon: 'warning', title: '입력 오류', text: '하나 이상의 단말기를 선택해 주세요.' });
            return;
        }

        const label = xmlProceNm === 'RESTART' ? '재부팅' : '콘텐츠재전송';
        const ok = await Swal.fire({
            icon: 'question', title: `${label} 요청`,
            html: `선택한 단말기 <b>${selectedRows.length}대</b>에 ${label}을(를) 요청하시겠습니까?`,
            showCancelButton: true, confirmButtonText: '예', cancelButtonText: '아니오',
        });
        if (!ok.isConfirmed) return;

        const restartInfo = `,${selectedRows.map((r) => `${r.didId}|${r.didMac}`).join(',')}`;
        const query = new URLSearchParams({ restartInfo, xmlProceNm }).toString();
        const res = await fnAjaxFetch({ url: `${URL.DID_RESTART}?${query}`, method: 'POST' });
        const json = res?.data;
        if (json?.resultCodeInfo === 'SUCCESS') {
            await Swal.fire({ icon: 'success', title: '완료', text: `${label} 요청을 보냈습니다.` });
            gridApiRef.current?.deselectAll();
        } else {
            await Swal.fire({ icon: 'error', title: '오류', text: json?.resultMessage || `${label} 요청 중 오류가 발생했습니다.` });
        }
    }, []);

    const columnDefs = useMemo(() => ([
        { field: 'roleNm', headerName: '부서명', width: 140 },
        { field: 'centerNm', headerName: '지점명', width: 140 },
        {
            field: 'didNm', headerName: '단말기명', flex: 1, minWidth: 180,
            cellRenderer: (p) => (
                <button className="btn btn-link p-0 text-start" onClick={() => handleOpenDidModal(p.data?.didId)}>
                    {p.value}
                    <br />
                    <span style={{ fontSize: 11, color: '#94a3b8' }}>{p.data?.didId}</span>
                </button>
            ),
        },
        { field: 'didIpaddr', headerName: 'IP', width: 130 },
        {
            field: 'schCnt', headerName: 'DID스케줄', width: 100,
            valueFormatter: (p) => `${p.value ?? 0}개`,
        },
        {
            field: 'didSttus', headerName: 'ON/OFF', width: 90,
            cellRenderer: (p) => (
                <span style={{ color: p.value === 'ON' ? '#22c55e' : '#94a3b8', fontWeight: 700 }}>{p.value}</span>
            ),
        },
        {
            field: 'didOs', headerName: 'OS구분', width: 110,
            cellRenderer: (p) => (
                <span style={{ display: 'flex', alignItems: 'center', gap: 6, height: '100%' }}>
                    <img src={getOsIcon(p.value)} alt={p.value || ''} width={16} height={16} />
                    {p.value}
                </span>
            ),
        },
        { field: 'didUseYn', headerName: '사용유무', width: 90 },
        {
            headerName: '콘텐츠 보기', width: 110, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-dark btn-outline__gray btn-sm"
                    onClick={() => navigate(`/backoffice/sub/equiManage/did/view?didId=${p.data?.didId}`)}
                >보기</button>
            ),
        },
        {
            headerName: '삭제', width: 90, sortable: false, filter: false,
            cellRenderer: (p) => (
                <button className="btn btn-outline-danger btn-outline__gray btn-sm"
                    onClick={() => handleDelete({ code: p.data?.didId, name: p.data?.didNm })}
                >삭제</button>
            ),
        },
    ]), [handleOpenDidModal, handleDelete, navigate]);

    return (
        <div className="row g-0 main-contents">
            <div className="col-12 content-header">
                <div className="content-header__title">단말기 관리</div>
                <div className="content-header__breadcrumb">
                    <ol className="breadcrumb">
                        <li className="breadcrumb-item">장비 관리</li>
                        <li className="breadcrumb-item">단말기 관리</li>
                    </ol>
                </div>
            </div>

            <div className="col-12 content-search">
                <div className="row g-0 w-100 justify-content-between">
                    <div className="col-auto content-search__option">
                        <select id="centerId" name="centerId"
                            value={tempParams.centerId} onChange={handleInputChange}>
                            <option value="">전체 지점</option>
                            {combos.selectCenter.map((o) => (
                                <option key={o.centerId} value={o.centerId}>{o.centerNm}</option>
                            ))}
                        </select>
                        <select id="searchCondition" name="searchCondition"
                            value={tempParams.searchCondition} onChange={handleInputChange}>
                            <option value="">선택</option>
                            <option value="didNm">단말기명</option>
                            <option value="didId">단말기ID</option>
                        </select>
                        <input type="text" id="searchKeyword" name="searchKeyword" placeholder="검색어를 입력하세요"
                            value={tempParams.searchKeyword}
                            onChange={handleInputChange}
                            onKeyDown={onSearchKeyDown}
                        />
                    </div>
                    <div className="col-auto content-search__action">
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={() => onSearch(1)}>검색</button>
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={handleReset}>검색 초기화</button>
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={() => handleBulkCommand('REDOWN')}>콘텐츠재전송</button>
                        <button type="button" className="btn btn-outline-dark btn-outline__gray"
                            onClick={() => handleBulkCommand('RESTART')}>재부팅</button>
                        <button type="button" className="btn btn-primary btn-default__blue"
                            onClick={() => handleOpenDidModal()}>단말기 등록</button>
                    </div>
                </div>
            </div>

            <div className="col-12 content-table content-table__main">
                <div className="ag-theme-material" style={{ height: 760, width: '100%' }}>
                    <AppAgGrid
                        columnDefs={columnDefs}
                        theme={gridTheme}
                        defaultColDef={defaultColDef}
                        rowModelType="infinite"
                        rowHeight={54}
                        rowSelection={{ mode: 'multiRow', checkboxes: true, headerCheckbox: true }}
                        pagination={true}
                        paginationPageSize={20}
                        cacheBlockSize={20}
                        maxBlocksInCache={2}
                        onGridReady={(params) => { gridApiRef.current = params.api; onGridReady(params); }}
                        overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                        overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                    />
                </div>
            </div>

            <Suspense fallback={null}>
                {modalOpen && (
                    <DidFormModal
                        open={modalOpen}
                        form={didForm}
                        setForm={setDidForm}
                        roleOptions={roleOptions}
                        centerOptions={combos.selectCenter}
                        groupOptions={combos.selectGroup}
                        typeOptions={typeOptions}
                        resolutionOptions={resolutionOptions}
                        ipTypeOptions={ipTypeOptions}
                        modelTypeOptions={modelTypeOptions}
                        osOptions={osOptions}
                        onClose={() => setModalOpen(false)}
                        onSubmit={handleSubmit}
                    />
                )}
            </Suspense>
        </div>
    );
}
