import XLSX from 'xlsx-js-style';

export const XLSX_HEADER_STYLE = {
    fill: { fgColor: { rgb: 'E2E2E2' } },
    font: { bold: true },
    alignment: { horizontal: 'center' },
    border: { top: { style: 'thin' }, bottom: { style: 'thin' }, left: { style: 'thin' }, right: { style: 'thin' } },
};

const XLSX_SUBTOTAL_STYLE = { fill: { fgColor: { rgb: 'F2F2F2' } }, font: { bold: true } };
const XLSX_NUM_FORMAT = '#,##0';

export const xlsxHeaderRow = (headers) => headers.map((h) => ({ v: h, s: XLSX_HEADER_STYLE }));

export const xlsxDataRow = (values, isSubTotal = false) => values.map((val) => {
    const style = isSubTotal ? { ...XLSX_SUBTOTAL_STYLE } : { font: {} };
    if (typeof val === 'number' && val < 0) {
        style.font = { ...style.font, color: { rgb: 'FF0000' } };
    }
    return {
        v: val,
        s: style,
        t: typeof val === 'number' ? 'n' : 's',
        z: typeof val === 'number' ? XLSX_NUM_FORMAT : undefined,
    };
});

export const xlsxTitleRow = (title, size = 14) => [{ v: title, s: { font: { bold: true, size } } }];

export const downloadXlsxSheet = (wsData, colWidths, sheetName, fileName) => {
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet(wsData);
    if (colWidths) ws['!cols'] = colWidths.map((wch) => ({ wch }));
    XLSX.utils.book_append_sheet(wb, ws, sheetName);
    XLSX.writeFile(wb, fileName);
};
