import React, { useMemo } from 'react';
import { gridTheme } from '@/constants/agGridTheme.js';
import AppAgGrid from '@/components/Common/AppAgGrid.jsx';
import { createGlobalStyle } from 'styled-components';
import {
    MasterDetailModule,
    ModuleRegistry,
    ClientSideRowModelModule,
    ValidationModule,
    PaginationModule,
} from 'ag-grid-enterprise';

// 전역 등록 + 인스턴스 전달을 함께 사용하여 확실히 활성화
ModuleRegistry.registerModules([
    ClientSideRowModelModule,
    ValidationModule,
    PaginationModule,
    MasterDetailModule,
]);

const GRID_MODULES = [
    ClientSideRowModelModule,
    ValidationModule,
    PaginationModule,
    MasterDetailModule,
];

const GlobalDetailStyle = createGlobalStyle`
    .ag-details-row-auto-height.ag-details-row {
        padding: 0 !important;
    }
`;

/**
 * 범용 Master-Detail AG Grid 래퍼.
 *
 * Props:
 *   columnDefs          — 메인 그리드 컬럼 정의
 *   rowData             — 데이터 배열
 *   getRowId            — (params) => string  행 고유 ID 함수
 *   isRowMaster         — (data) => bool  마스터 행 여부 (기본: () => true)
 *   detailCellRenderer  — detail 행을 렌더링할 React 컴포넌트
 *   detailRowHeight     — detail 행 높이 (기본: 250)
 *   context             — 모든 셀 렌더러에 전달할 공유 객체
 *   onGridReady         — (params) => void  gridApi 노출 콜백
 *   pageSize            — 페이지 크기 (기본: 20)
 *   ...rest             — AgGridReact에 그대로 전달
 */
const MasterDetailGrid = ({
    columnDefs,
    rowData = [],
    getRowId,
    isRowMaster = () => true,
    detailCellRenderer,
    detailRowHeight = 250,
    context,
    onGridReady,
    pageSize = 20,
    ...rest
}) => {
    const defaultColDef = useMemo(() => ({
        resizable: true,
        sortable: true,
        filter: false,
        suppressHeaderMenuButton: true,
    }), []);

    return (
        <>
            <GlobalDetailStyle />
            {/* domLayout="autoHeight"는 행이 많아질 때(약 20건 이상) 그리드 높이 계산이
                어긋나 하단에 배경색 없는 빈 영역(검은 화면)이 노출되는 문제가 있어,
                다른 화면들과 동일하게 고정 높이로 맞춘다. */}
            <div className="ag-theme-material" style={{ height: 760, width: '100%' }}>
                <AppAgGrid
                    modules={GRID_MODULES}
                    getRowId={getRowId}
                    masterDetail={true}
                    keepDetailRows={true}
                    pagination={true}
                    paginationPageSize={pageSize}
                    paginationPageSizeSelector={[10, 20, 50, 100]}
                    columnDefs={columnDefs}
                    theme={gridTheme}
                    defaultColDef={defaultColDef}
                    rowData={rowData}
                    isRowMaster={isRowMaster}
                    detailCellRenderer={detailCellRenderer}
                    detailRowHeight={detailRowHeight}
                    rowSelection={{ mode: 'multiRow', checkboxes: false, headerCheckbox: false }}
                    overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                    overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
                    context={context}
                    onGridReady={onGridReady}
                    {...rest}
                />
            </div>
        </>
    );
};

export default MasterDetailGrid;
