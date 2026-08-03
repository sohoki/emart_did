/**
 * HTML 엔티티를 일반 문자로 변환한다.
 * textarea DOM을 이용해 &amp; &lt; &middot; &nbsp; 등 모든 named entity를 처리한다.
 */
export const decodeHtml = (str) => {
    if (!str) return '';
    const txt = document.createElement('textarea');
    txt.innerHTML = str;
    return txt.value;
};
