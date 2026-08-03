// 이미지가 없을 때 보여줄 공통 placeholder (data URI, 네트워크 요청 없이 즉시 렌더링됨)
export const NO_IMG_SRC = `data:image/svg+xml,${encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">' +
    '<rect width="24" height="24" rx="4" fill="#f1f5f9"/>' +
    '<rect x="4" y="4" width="16" height="16" rx="2" fill="none" stroke="#cbd5e1" stroke-width="1.5"/>' +
    '<circle cx="9" cy="9" r="1.5" fill="#94a3b8"/>' +
    '<path d="M4 17l4-4 4 4 2-2 4 3" fill="none" stroke="#cbd5e1" stroke-width="1.5" stroke-linejoin="round" stroke-linecap="round"/>' +
    '</svg>'
)}`;
