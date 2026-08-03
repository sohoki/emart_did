import { useEffect } from 'react';

// 인증 없이 접근하는 고객용 팝업(CusResList/CusResForm)에서 사용 — 관리자 화면의
// 다크모드 설정(localStorage 공유)과 무관하게 마운트되어 있는 동안은 항상 라이트로 표시하고,
// 언마운트 시 원래 테마로 되돌린다.
export function useForceLightTheme() {
    useEffect(() => {
        const root = document.documentElement;
        const prevTheme = root.getAttribute('data-theme');
        root.setAttribute('data-theme', 'light');
        return () => {
            if (prevTheme) root.setAttribute('data-theme', prevTheme);
            else root.removeAttribute('data-theme');
        };
    }, []);
}
