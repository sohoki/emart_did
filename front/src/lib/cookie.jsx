export function getCookie(name) {
    const m = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    if (!m) return '';
    let value;
    try {
        value = decodeURIComponent(m[2]);
    } catch (e) {
        // 인코딩이 깨진 경우 원본 값을 반환하거나 빈 값을 반환합니다.
        console.error("쿠키 디코딩 실패:", e);
        value = m[2];
    }
    // 과거 setCookie 버그로 브라우저에 이미 남아있는 문자열 "undefined"/"null" 쿠키를
    // 정상적으로 없는 값(빈 문자열)처럼 취급한다 — setCookie 자체는 고쳤지만 이미 저장된
    // 쿠키까지 자동으로 고쳐지진 않으므로 읽는 쪽에서도 방어한다.
    return (value === 'undefined' || value === 'null') ? '' : value;
}
export function setCookie(name, value, days) {
    let expires = '';
    if (days) {
        const date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toUTCString();
    }
    // value가 undefined/null이면 encodeURIComponent가 문자열 "undefined"/"null"을 그대로
    // 반환해버려서(둘 다 truthy) 쿠키에 그 문자열이 그대로 저장되는 문제를 막는다.
    const safeValue = (value === undefined || value === null) ? '' : value;
    document.cookie = name + "=" + (encodeURIComponent(safeValue) || "") + expires + "; path=/";
}

// 쿠키 삭제 — setCookie와 동일하게 path=/로 만료시켜야 실제로 지워진다(path가 다르면
// 브라우저가 별개 쿠키로 취급해 삭제되지 않는다). 로그아웃 시 인증 쿠키 정리용으로 추가.
export function removeCookie(name) {
    document.cookie = name + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/";
}