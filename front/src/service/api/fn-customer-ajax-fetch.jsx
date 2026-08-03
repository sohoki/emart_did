import axios from 'axios';
import { getCookie } from '@/lib/cookie.jsx';
import { customerRefresh } from '@/service/api/fn-customer-auth.jsx';
import Swal from '@/lib/swal.js';
import appConfig from '@/config/index.jsx';

// 판매사몰 고객(customer) 전용 fnAjaxFetch — 관리자용 fnAjaxFetch(fn-ajax-fetch.jsx)와 동일한 구조이되,
// Authorization 토큰을 customerAccessToken 쿠키에서 읽고, 401/403 시 customerRefresh()로 재시도한다.
// 두 유틸을 분리한 이유: 관리자용 fnAjaxFetch는 accessToken(관리자) 쿠키를 매 요청마다 무조건 덮어써서
// 고객 토큰과 섞어 쓸 수 없기 때문 (circuit breaker/로딩 오버레이 상태도 서로 독립적으로 둔다).

// ━━━━━━━━━━ Circuit Breaker (고객용, 관리자용과 별도 상태) ━━━━━━━━━━
const CIRCUIT = {
    MAX_FAILURES: 3,
    RESET_TIMEOUT: 30_000,
    failures: 0,
    open: false,
    resetTimer: null,
};

function circuitBreak() {
    CIRCUIT.open = true;
    clearTimeout(CIRCUIT.resetTimer);
    CIRCUIT.resetTimer = setTimeout(() => {
        CIRCUIT.open = false;
        CIRCUIT.failures = 0;
    }, CIRCUIT.RESET_TIMEOUT);

    Swal.fire({
        icon: 'error',
        title: '서버 연결 실패',
        html: `연속 ${CIRCUIT.MAX_FAILURES}회 오류가 발생했습니다.<br>잠시 후 자동으로 재시도합니다.`,
        timer: CIRCUIT.RESET_TIMEOUT,
        timerProgressBar: true,
        allowOutsideClick: false,
        showConfirmButton: true,
        confirmButtonText: '지금 재시도',
    }).then((result) => {
        if (result.isConfirmed) {
            CIRCUIT.open = false;
            CIRCUIT.failures = 0;
            clearTimeout(CIRCUIT.resetTimer);
        }
    });
}

// ━━━━━━━━━━ 로딩 오버레이 (관리자용과 별도 DOM id) ━━━━━━━━━━
const LOADING_ID = '__customer-api-loading-overlay__';

const LOADING_HTML = `
<div id="${LOADING_ID}" style="
    position:fixed;inset:0;
    display:flex;align-items:center;justify-content:center;
    background:rgba(15,23,42,0.4);
    backdrop-filter:blur(6px) saturate(120%);
    -webkit-backdrop-filter:blur(6px) saturate(120%);
    z-index:99999;
    animation:_custApiFadeIn .15s ease-out;
">
    <svg viewBox="0 0 50 50" width="56" height="56"
         style="animation:_custApiRotate 1.2s linear infinite;filter:drop-shadow(0 2px 10px rgba(66,165,245,0.55))">
        <circle cx="25" cy="25" r="20" fill="none"
            stroke="rgba(255,255,255,0.35)" stroke-width="4"/>
        <circle cx="25" cy="25" r="20" fill="none"
            stroke="#42a5f5" stroke-width="4"
            stroke-linecap="round"
            stroke-dasharray="90px,126px" stroke-dashoffset="0"
            style="animation:_custApiDash 1.4s ease-in-out infinite"/>
    </svg>
    <style>
        @keyframes _custApiFadeIn{from{opacity:0}to{opacity:1}}
        @keyframes _custApiRotate{100%{transform:rotate(360deg)}}
        @keyframes _custApiDash{
            0%{stroke-dasharray:1px,126px;stroke-dashoffset:0}
            50%{stroke-dasharray:90px,126px;stroke-dashoffset:-15px}
            100%{stroke-dasharray:90px,126px;stroke-dashoffset:-125px}
        }
    </style>
</div>`;

function showApiLoading() {
    if (document.getElementById(LOADING_ID)) return;
    const div = document.createElement('div');
    div.innerHTML = LOADING_HTML;
    document.body.appendChild(div.firstElementChild);
}

function hideApiLoading() {
    document.getElementById(LOADING_ID)?.remove();
}

class HandledError extends Error {
    constructor(message) {
        super(message);
        this.name = 'HandledError';
    }
}

export async function fnCustomerAjaxFetch(options) {
    if (CIRCUIT.open) {
        throw new HandledError('Circuit breaker open - request blocked');
    }

    const {
        url,
        method,
        param,
        data,
        headers: customHeaders,
        signal,
        responseType,
        done_callback,
        fail_callback,
        redirectOnNoAuth = false,
        withCredentials = true,
        showLoading = true,
        suppressErrorHandling = false,
        _retryCount = 0,
    } = options;

    let loadingOpen = false;
    const closeLoading = () => {
        if (loadingOpen) {
            hideApiLoading();
            loadingOpen = false;
        }
    };

    if (showLoading) {
        loadingOpen = true;
        showApiLoading();
    }

    try {
        const headers = {
            'AJAX': 'true',
            ...customHeaders,
        };
        if (!Object.keys(headers).some(k => k.toLowerCase() === 'content-type')) {
            headers['Content-Type'] = 'application/json; charset=utf-8';
        }
        if (data instanceof FormData) {
            delete headers['Content-Type'];
        }

        const customerAccessToken = getCookie('customerAccessToken');
        if (customerAccessToken) {
            headers['Authorization'] = `Bearer ${customerAccessToken}`;
        }

        const axiosConfig = {
            url: `${appConfig.REACT_APP_API_URL}${url}`,
            method,
            headers,
            withCredentials,
            signal,
            responseType: responseType ?? 'json',
            ...(String(method).toUpperCase() === 'GET'
                ? (param ? { params: param } : {})
                : (data ? { data } : (param ? { data: param } : {}))
            ),
            validateStatus: () => true,
        };

        const res = await axios(axiosConfig);
        closeLoading();

        CIRCUIT.failures = 0;

        if (res.status >= 200 && res.status < 300) {
            const resData = res.data;

            if (
                resData &&
                typeof resData === 'object' &&
                resData.resultCodeInfo === 'FAIL' &&
                String(resData.resultCode) === '401'
            ) {
                return await handleUnauthorized(options, _retryCount, fail_callback, redirectOnNoAuth, suppressErrorHandling);
            }

            if (typeof done_callback === 'function') done_callback(resData);
            return res;

        } else if (res.status === 401 || res.status === 403) {
            return await handleUnauthorized(options, _retryCount, fail_callback, redirectOnNoAuth, suppressErrorHandling);

        } else if (res.status === 404) {
            if (!suppressErrorHandling) Swal.fire({ icon: 'error', title: '404 Not Found', text: '없는 페이지입니다.' });
            throw new HandledError('HTTP 404 Not Found');

        } else if (res.status === 500) {
            if (!suppressErrorHandling) {
                if (typeof fail_callback === 'function') fail_callback(res);
                const msg = res.data?.resultMessage || res.data?.message || '서버 내부 오류가 발생했습니다.';
                Swal.fire({ icon: 'error', title: '서버 오류 (500)', text: msg });
            }
            throw new HandledError('HTTP 500 Internal Server Error');

        } else {
            if (!suppressErrorHandling) {
                Swal.fire({
                    icon: 'error',
                    title: 'Network Error',
                    text: `네트워크 에러 입니다. 접속 URL을 확인하세요. (HTTP ${res.status})`,
                    timer: 10000,
                    timerProgressBar: true,
                    allowOutsideClick: false,
                    allowEscapeKey: false,
                });
            }
            throw new HandledError(`HTTP ${res.status}`);
        }

    } catch (error) {
        closeLoading();
        if (error.name === 'CanceledError' || error.name === 'AbortError') {
            console.log('요청이 취소되었습니다.');
            return;
        }
        if (error.name === 'HandledError') throw error;

        CIRCUIT.failures += 1;
        if (CIRCUIT.failures >= CIRCUIT.MAX_FAILURES) {
            circuitBreak();
            throw new HandledError('Circuit breaker triggered');
        }

        console.error('네트워크 에러:', error);
        if (!suppressErrorHandling) {
            Swal.fire({
                icon: 'error',
                title: 'Network Error',
                text: `네트워크 에러 입니다. 접속 URL을 확인하세요.\n${error.message}`,
                timer: 10000,
                timerProgressBar: true,
                allowOutsideClick: false,
                allowEscapeKey: false,
            });
        }
        throw error;
    }
}

async function handleUnauthorized(options, _retryCount, fail_callback, redirectOnNoAuth, suppressErrorHandling) {
    if (_retryCount < 1 && getCookie('customerRefreshToken')) {
        const refreshed = await customerRefresh();
        if (refreshed) {
            return fnCustomerAjaxFetch({ ...options, _retryCount: _retryCount + 1 });
        }
    }

    if (typeof fail_callback === 'function') fail_callback();

    // redirectOnNoAuth 기본값 false — comCode를 모르는 공용 유틸이라 리다이렉트는 호출부에서 처리 권장
    // suppressErrorHandling이면 redirectOnNoAuth를 명시했어도 리다이렉트 없이 조용히 throw
    if (!redirectOnNoAuth || suppressErrorHandling) {
        throw new HandledError('Unauthorized');
    }

    Swal.fire({
        icon: 'error',
        title: '인증 에러',
        text: '로그인이 만료되어 다시 로그인해 주세요.',
        timer: 3000,
        timerProgressBar: true,
        allowOutsideClick: false,
        allowEscapeKey: false,
    }).then(() => { window.location.href = redirectOnNoAuth; });

    throw new HandledError('Unauthorized');
}
