import axios from 'axios';
import { getCookie, setCookie } from '@/lib/cookie.jsx';
import config from '@/config/index.jsx';
import URL from '@/constants/URL.jsx';

// 판매사몰 고객(customer) JWT 인증 — 관리자(manager) 로그인과 완전히 별도의 쿠키를 사용한다.
// fnAjaxFetch는 매 요청마다 accessToken(관리자) 쿠키를 무조건 Authorization 헤더로 덮어쓰기 때문에
// 고객 토큰과 충돌한다 — 그래서 fn-refresh.jsx와 동일하게 axios를 직접 사용한다.
const COOKIE = {
  ACCESS: 'customerAccessToken',
  REFRESH: 'customerRefreshToken',
  NAME: 'customerName',
  ID: 'customerId',
  COM_CODE: 'customerComCode',
  PHONE: 'customerPhone',
};

function storeSession({ accessToken, refreshToken, customer }) {
  if (accessToken) setCookie(COOKIE.ACCESS, accessToken, 1);
  if (refreshToken) setCookie(COOKIE.REFRESH, refreshToken, 1);
  if (customer?.userName) setCookie(COOKIE.NAME, customer.userName, 1);
  if (customer?.customerId) setCookie(COOKIE.ID, customer.customerId, 1);
  if (customer?.comCode) setCookie(COOKIE.COM_CODE, customer.comCode, 1);
  if (customer?.userPhone) setCookie(COOKIE.PHONE, customer.userPhone, 1);
}

function clearSession() {
  Object.values(COOKIE).forEach((name) => setCookie(name, '', -1));
}

export function isCustomerLoggedIn() {
  return !!getCookie(COOKIE.ACCESS);
}

export function getCustomerDisplayName() {
  return getCookie(COOKIE.NAME);
}

export function getCustomerPhone() {
  return getCookie(COOKIE.PHONE);
}

export function getCustomerAuthHeader() {
  const token = getCookie(COOKIE.ACCESS);
  return token ? { Authorization: `Bearer ${token}` } : {};
}

// 로그인 — cus/login.do(POST), 아이디/비밀번호/comCode를 검증한다.
export async function customerLogin(customerId, comCode, userPassword) {
  try {
    const res = await axios.post(
      `${config.REACT_APP_API_URL}${URL.CUSTOMER_LOGIN}`,
      { cusomterId: customerId, comCode, userPassword },
      {
        headers: { 'AJAX': 'true',
                'Content-Type': 'application/json; charset=utf-8'
                },
        withCredentials: true,
        validateStatus: () => true,
      }
    );
    const result = res.data;

    if (result?.resultCodeInfo === 'SUCCESS') {
      const customer = result?.result?.resultVO;
      storeSession({
        accessToken: result?.result?.jToken,
        refreshToken: result?.result?.refreshToken,
        customer,
      });
      return { success: true, message: result?.resultMessage, customer };
    }

    return { success: false, message: result?.resultMessage || '로그인에 실패했습니다.' };
  } catch (e) {
    return { success: false, message: e?.message || '로그인 처리 중 오류가 발생했습니다.' };
  }
}

// 리프레시 토큰으로 액세스 토큰 재발급
export async function customerRefresh() {
  const refreshToken = getCookie(COOKIE.REFRESH);
  if (!refreshToken) return false;

  try {
    const res = await axios.get(`${config.REACT_APP_API_URL}${URL.CUSTOMER_REFRESH}`, {
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
      storeSession({ accessToken: result?.result?.jToken, refreshToken: result?.result?.refreshToken });
      return true;
    }

    clearSession();
    return false;
  } catch {
    return false;
  }
}

// 로그아웃 — 서버 호출 실패와 무관하게 로컬 세션(쿠키)은 항상 정리한다.
export async function customerLogout() {
  const token = getCookie(COOKIE.ACCESS);
  try {
    if (token) {
      await axios.get(`${config.REACT_APP_API_URL}${URL.CUSTOMER_LOGOUT}`, {
        headers: { 'AJAX': 'true', Authorization: `Bearer ${token}` },
        withCredentials: true,
        validateStatus: () => true,
      });
    }
  } catch {
    // 네트워크 오류가 나도 로컬 세션은 정리한다
  } finally {
    clearSession();
  }
}
