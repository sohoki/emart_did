export const fmtDate     = (v) => (!v ? '-' : String(v).replace(/(\d{4})(\d{2})(\d{2})/, '$1-$2-$3'));
export const fmtDateTime = (v) => (!v ? '-' : String(v).replace(/(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})/, '$1-$2-$3 $4:$5:$6'));
export const fmtPhone    = (v) => (!v ? '' : String(v).replace(/(\d{3})(\d{3,4})(\d{4})/, '$1-$2-$3'));
export const fmtComma    = (v) => (!v ? '0' : Number(v).toLocaleString());
export const todayStr    = () => new Date().toISOString().slice(0, 10).replace(/-/g, '');
//달력 년/월
export const formatMonth = (date) => {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    return `${y}${m}`;
};
// 숫자 천 단위 콤마
export function formatNumber(value) {
  if (value === null || value === undefined) return '';
  const str = String(value);
  return str.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}
//숫자믈 문자로 

export const unformatNumber = (v) =>
  v === null || v === undefined || v === ''  ? '' : String(v).replace(/,/g, '');

export const NVL = (reqValue) => {
  return reqValue === undefined || reqValue === null || reqValue === "" ? "" : reqValue;
};

// 전화번호 입력 포맷 (입력 중 자동 하이픈)
// 대표번호(1XXX~) / 02 지역번호 / 3자리 지역번호·휴대폰 세 가지 패턴 지원
export const formatTel = (val) => {
  const raw = val.replace(/\D/g, '');
  if (!raw.startsWith('0')) {
    const d = raw.slice(0, 8);
    if (d.length > 4) return `${d.slice(0, 4)}-${d.slice(4)}`;
    return d;
  }
  if (raw.startsWith('02')) {
    const d = raw.slice(0, 10);
    if (d.length > 6) return `${d.slice(0, 2)}-${d.slice(2, d.length - 4)}-${d.slice(-4)}`;
    if (d.length > 2) return `${d.slice(0, 2)}-${d.slice(2)}`;
    return d;
  }
  const d = raw.slice(0, 11);
  if (d.length > 7) return `${d.slice(0, 3)}-${d.slice(3, d.length - 4)}-${d.slice(-4)}`;
  if (d.length > 3) return `${d.slice(0, 3)}-${d.slice(3)}`;
  return d;
};



