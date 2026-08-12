import axios from 'axios';
import { getCookie, setCookie } from '@/lib/cookie.jsx';
import config from '@/config/index.jsx';

// 리프레시 성공 여부만 반환한다 — 실패 시 사용자 안내/로그인 페이지 이동은 호출부
// (fn-ajax-fetch.jsx의 handleUnauthorized, ProtectedRoute)에서 한 곳으로만 처리한다.
// 예전엔 여기서도 자체적으로 Swal + window.location.href('/login')를 실행해서, 호출부의
// 리다이렉트와 중복 실행되어 로그인 페이지로 두 번 이동하거나 화면이 깜박이는 문제가 있었다.
export async function fn_Refresh() {
    const refreshToken = getCookie('refreshToken');

    if (!refreshToken) return false;

    try {
        const res = await axios.get(`${config.REACT_APP_API_URL}/uat/uia/actionRefreshToken.do`, {
            headers: {
                'AJAX': 'true',
                'refreshToken': refreshToken,
                'Content-Type': 'application/json; charset=utf-8',
            },
            withCredentials: true,
            validateStatus: () => true,
        });

        const result = res.data;

        if (String(result?.resultCode) === '200') {
            const accessToken = result?.result?.jToken;
            const newRefreshToken = result?.result?.refreshToken;

            if (accessToken) setCookie('accessToken', accessToken, 1);
            if (newRefreshToken) setCookie('refreshToken', newRefreshToken, 1);

            return true;
        }

        return false;

    } catch (error) {
        console.error('토큰 갱신 실패:', error);
        return false;
    }
}
