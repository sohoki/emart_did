import React, { useEffect, useRef, useState, lazy, Suspense } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import SideBar from '@/components/Layout/SideBar.jsx';
import Header from '@/components/Layout/Header.jsx';
import Footer from '@/components/Layout/Footer.jsx';
import UserBookmarks from '@/components/Layout/UserBookmarks.jsx';
import { getCookie, removeCookie } from '@/lib/cookie.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import API_URL from '@/constants/URL.jsx';
import Swal from '@/lib/swal.js';
import { alert } from '@/lib/alert.js';

const ManagerFormModal = lazy(() => import('@/pages/backoffice/HrInfo/components/ManagerFormModal'));
const ManagerPwdModal = lazy(() => import('@/pages/backoffice/HrInfo/components/ManagerPwdModal'));

export default function AppLayout() {
    const navigate = useNavigate();
    const profileRef = useRef(null);

    // 쿠키는 동기 읽기 → lazy initializer 로 첫 렌더에 바로 세팅, useEffect 불필요
    const [managerInfo] = useState(() => ({
        managerName: getCookie('userName')  || '사용자',
        managerId:   getCookie('userId')    || 'user',
        managerEmail: getCookie('userEmail') || '',
        managerPic:   getCookie('userPic')   || '',
    }));
    // loginText 는 managerInfo 파생값 → 별도 state 불필요
    const loginText = `${managerInfo.managerName} (${managerInfo.managerId})`;

    const [showProfile, setShowProfile] = useState(false);
    const [showManagerModal, setShowManagerModal] = useState(false);
    const [managerForm, setManagerForm] = useState(null);
    const [showPwdModal, setShowPwdModal] = useState(false);

    useEffect(() => {
        if (!showProfile) return;
        const handleClickOutside = (e) => {
            if (profileRef.current && !profileRef.current.contains(e.target)) {
                setShowProfile(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [showProfile]);

    const handleOpenEditModal = async () => {
        //관리자 정보 수정 모달 열기 — ManagerFormModal.jsx에서 mode: 'Edt'로 세팅
        const managerId = managerInfo?.managerId || '';

        try {
            const res = await fnAjaxFetch({
                url: `${API_URL.MANAGER_DETAIL}/${managerId}.do`,
                method: 'GET'
            });
            const obj = res?.data?.result?.result || null;

            if (obj) {
                setManagerForm({
                    mode: 'Edt',
                    managerId: obj.managerId || '',
                    managerName: obj.managerName || '',
                    managerTel: obj.managerTel || '',
                    managerPosition: obj.managerPosition || '',
                    // 기존 비밀번호(해시)는 화면에 노출/재전송하지 않음 — 빈 값이면 변경 안 함
                    managerPassword: '',
                    managerPasswordConfirm: '',
                    passwordHint: obj.passwordHint || '',
                    passwordCnsr: obj.passwordCnsr || '',
                    managerEmail: obj.managerEmail || '',
                });
                setShowManagerModal(true);
            }
        } catch (e) {
            await Swal.fire({ icon: 'error', title: '오류', text: e?.message || '상세 조회 중 오류' });
        }
    };

    const handleOpenPwdModal = () => {
        setShowPwdModal(true);
    };

    const handleLogout = async () => {
        try {


            console.log("API_URL.LOGOUT_PROCESS:" +API_URL.LOGOUT_PROCESS);

            const res = await fnAjaxFetch({
                url: API_URL.LOGOUT_PROCESS,
                method: 'GET',
                withCredentials: true,
            });
            const partId = res?.data?.result?.partId;

            // 서버는 JWT라 상태를 갖지 않아 refreshToken 무효화만 하고 클라이언트 쿠키는
            // 지워주지 않는다 — 여기서 지우지 않으면 accessToken이 브라우저에 그대로 남아
            // 로그아웃이 안 된 것처럼 보인다(다른 프로젝트와 같은 호스트에서 같이 띄워두면
            // 쿠키 이름이 겹쳐 증상이 더 헷갈리게 나타날 수 있음). savedManagerId(아이디 저장)는
            // 로그인 편의 기능이라 그대로 둔다.
            ['accessToken', 'refreshToken', 'userId', 'userName', 'roleId', 'partId'].forEach(removeCookie);

            navigate(partId === 'VENDOR' ? '/parterLogin' : '/login');
        } catch (error) {
            // HandledError: fnAjaxFetch 내부에서 이미 Swal 처리됨 → 중복 표시 방지
            if (error.name === 'HandledError') return;
            await alert.error(`로그아웃 중 오류가 발생했습니다. ${error}`, '로그아웃 실패');
        }
    };

    return (
        <div className="wrapper">
            <Header
                profileRef={profileRef}
                showProfile={showProfile}
                setShowProfile={setShowProfile}
                loginText={loginText}
                managerInfo={managerInfo}
                handleLogout={handleLogout}
                handleOpenEditModal={handleOpenEditModal}
                handleOpenPwdModal={handleOpenPwdModal}
            />
            <section>
                <SideBar />
                <div className="app-content-col">
                    <UserBookmarks />
                    <Outlet />
                </div>
            </section>
            <Footer />

            {showManagerModal && managerForm && (
                <Suspense fallback={null}>
                    <ManagerFormModal
                        open={showManagerModal}
                        form={managerForm}
                        setForm={setManagerForm}
                        onClose={() => setShowManagerModal(false)}
                        onSearch={() => {}}
                        setModalOpen={setShowManagerModal}
                    />
                </Suspense>
            )}

            {showPwdModal && (
                <Suspense fallback={null}>
                    <ManagerPwdModal
                        open={showPwdModal}
                        managerId={managerInfo?.managerId}
                        managerName={managerInfo?.managerName}
                        onClose={() => setShowPwdModal(false)}
                    />
                </Suspense>
            )}
        </div>
    );
}
