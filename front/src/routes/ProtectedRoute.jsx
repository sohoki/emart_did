
import { useState, useEffect } from 'react';
import { Navigate, useLocation } from "react-router-dom";
import { fn_Refresh } from "@/service/api/fn-refresh.jsx";
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import URL from '@/constants/URL.jsx';
import PageLoading from '@/components/Common/PageLoading.jsx';

async function jwtAuthentication() {
    try {
        const res = await fnAjaxFetch({
            url: URL.AUTH_CHECK,
            method: 'POST',
            data: {},
            withCredentials: true,
            // fnAjaxFetch 자체의 401 처리(Swal + window.location.href 강제 이동)를 여기서는 막는다.
            // 이 인증확인 호출의 401/refresh-실패 판단은 아래 jwtAuthentication()/ProtectedRoute의
            // <Navigate>가 유일한 리다이렉트 경로여야 함 — 안 그러면 fnAjaxFetch의 하드 리다이렉트와
            // ProtectedRoute의 SPA 리다이렉트가 중복 실행되어 로그인 페이지가 두 번 걸리거나
            // 화면이 깜박이는 현상이 생긴다(리프레시 토큰이 없는 환경에서 특히 매번 발생).
            suppressErrorHandling: true,
        });

        const code = String(res?.data?.resultCode || '');

        if (code === '200') {
            return true;
        }

        // 리프레시 토큰 로직 - 인증 실패 시 재시도
        return await fn_Refresh();
    } catch (e) {
        console.error('Auth check error:', e);
        return false;
    }
}

export default function ProtectedRoute({ children }) {
    const [status, setStatus] = useState('checking');
    const location = useLocation();

    // 인증 상태 확인
    useEffect(() => {
        let isMounted = true;

        jwtAuthentication().then((isAuth) => {
            if (isMounted) {
                setStatus(isAuth ? 'authenticated' : 'unauthenticated');
            }
        });

        return () => {
            isMounted = false;
        };
    }, []);

    // 로딩 상태
    if (status === 'checking') {
        return <PageLoading />;
    }

    // 인증 실패 - 로그인 페이지로 리다이렉트
    if (status === 'unauthenticated') {
        return <Navigate to="/login" replace state={{ from: location }} />;
    }

    // 인증 성공
    return <>{children}</>;
}
