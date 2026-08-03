import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import LoginForm from '@/components/Auth/LoginForm.jsx';
import Swal from '@/lib/swal.js';
import { getCookie, setCookie } from '@/lib/cookie.jsx';
import config from '@/config/index.jsx';
import URL from '@/constants/URL.jsx';

const ID_COOKIE = 'savedManagerId';

export default function LoginPage() {
    const navigate = useNavigate();
    const idRef = useRef(null);

    const [userInfo, setUserInfo] = useState({
        userId: getCookie(ID_COOKIE) || '',
        userPwd: '',
    });
    const [saveIDFlag, setSaveIDFlag] = useState(!!getCookie(ID_COOKIE));
    const [loginButtonActive, setLoginButtonActive] = useState(true);

    useEffect(() => {
        // 이미 로그인된 상태면 로그인 화면을 건너뛴다
        if (getCookie('accessToken')) {
            navigate('/backoffice/hr/manager', { replace: true });
            return;
        }
        idRef.current?.focus();
    }, [navigate]);

    const onChangeUserInfo = useCallback((e) => {
        const { name, value } = e.target;
        setUserInfo((prev) => ({ ...prev, [name]: value }));
    }, []);

    const handleSaveIDFlag = useCallback((e) => {
        setSaveIDFlag(e.target.checked);
    }, []);

    const handleIdFind = useCallback(() => {
        Swal.fire({ icon: 'info', title: '아이디 찾기', text: '준비 중인 기능입니다.' });
    }, []);

    const handlePasswordFind = useCallback(() => {
        Swal.fire({ icon: 'info', title: '비밀번호 찾기', text: '준비 중인 기능입니다.' });
    }, []);

    const submitFormHandler = useCallback(async (e) => {
        e.preventDefault();

        if (!userInfo.userId || !userInfo.userPwd) {
            Swal.fire({ icon: 'warning', title: '입력 확인', text: '아이디와 비밀번호를 입력해 주세요.' });
            return;
        }

        setLoginButtonActive(false);
        try {
            const res = await axios.post(
                `${config.REACT_APP_API_URL}${URL.LOGIN_PROCESS}`,
                { userId: userInfo.userId, userPwd: userInfo.userPwd },
                {
                    headers: { 'AJAX': 'true', 'Content-Type': 'application/json; charset=utf-8' },
                    withCredentials: true,
                    validateStatus: () => true,
                }
            );

            const body = res.data;
            if (String(body?.resultCode) !== '200') {
                Swal.fire({ icon: 'error', title: '로그인 실패', text: body?.resultMessage || '아이디 또는 비밀번호를 확인해 주세요.' });
                return;
            }

            const { resultVO, jToken, refreshToken } = body.result || {};
            if (!jToken) {
                Swal.fire({ icon: 'error', title: '로그인 실패', text: '토큰 발급에 실패했습니다.' });
                return;
            }

            setCookie('accessToken', jToken, 1);
            setCookie('refreshToken', refreshToken, 1);
            setCookie('userId', resultVO?.managerId, 1);
            setCookie('userName', resultVO?.managerName, 1);
            setCookie('roleId', resultVO?.roleId, 1);
            setCookie('partId', resultVO?.partId, 1);

            if (saveIDFlag) {
                setCookie(ID_COOKIE, userInfo.userId, 30);
            } else {
                setCookie(ID_COOKIE, '', -1);
            }

            navigate('/backoffice/hr/manager', { replace: true });
        } catch (error) {
            Swal.fire({ icon: 'error', title: '로그인 오류', text: error?.message || '로그인 중 오류가 발생했습니다.' });
        } finally {
            setLoginButtonActive(true);
        }
    }, [userInfo, saveIDFlag, navigate]);

    return (
        <div className="login-center">
            <LoginForm
                userInfo={userInfo}
                saveIDFlag={saveIDFlag}
                loginButtonActive={loginButtonActive}
                idRef={idRef}
                onChangeUserInfo={onChangeUserInfo}
                handleSaveIDFlag={handleSaveIDFlag}
                handleIdFind={handleIdFind}
                handlePasswordFind={handlePasswordFind}
                submitFormHandler={submitFormHandler}
            />
        </div>
    );
}
