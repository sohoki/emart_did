import React, { useCallback, useEffect, useMemo, useRef, useState, lazy, Suspense } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import SideBar from '@/components/Layout/SideBar.jsx';
import Header from '@/components/Layout/Header.jsx';
import Footer from '@/components/Layout/Footer.jsx';
import UserBookmarks from '@/components/Layout/UserBookmarks.jsx';
import { getCookie } from '@/lib/cookie.jsx';
import { fnAjaxFetch } from '@/service/api/fn-ajax-fetch.jsx';
import API_URL from '@/constants/URL.jsx';
import CODE from '@/constants/CODE.jsx';
import Swal from 'sweetalert2';
import { alert } from '@/lib/alert.js';
import { useCommonCodeData, useCustomReqDataCombo } from '@/hooks/use-combo-data.js';
import { useCommonSubmit } from '@/hooks/use-common-submit.js';

const ROLE_MAPPING = { id: 'roleId', text: 'roleName' };
// 거래처 계정의 comGubun에 따라 조회할 권한 구분이 다르다 (ManagerTapInfo.jsx와 동일 규칙)
const ROLE_GUBUN_BY_COM_GUBUN = {
    COM_GUBUN_1: 'ROLE_GUBUN_2', // 공급사
    COM_GUBUN_2: 'ROLE_GUBUN_3', // 판매사
};


const ManagerFormModal = lazy(() => import('@/pages/backoffice/HrInfo/components/ManagerFormModal'));
const ManagerPwdModal = lazy(() => import('@/pages/backoffice/HrInfo/components/ManagerPwdModal'));
const VendorUserFormModal = lazy(() => import('@/pages/backoffice/Vendor/components/VendorUserFormModal'));
const VendorPwdModal = lazy(() => import('@/pages/backoffice/Vendor/components/VendorPwdModal'));

export default function AppLayout() {
    const navigate = useNavigate();
    const profileRef = useRef(null);
    // comCode 쿠키가 있으면 거래처(판매사/공급사) 로그인 계정 — 정보수정/비밀번호 변경을 거래처용으로 분기한다
    const vendorComCode = getCookie('comCode');

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
    const [showVendorModal, setShowVendorModal] = useState(false);
    const [vendorForm, setVendorForm] = useState(null);
    const [showPwdModal, setShowPwdModal] = useState(false);

    // ManagerFormModal이 요구하는 옵션 목록 — ManagerInfo.jsx와 동일한 소스로 맞춘다.
    const { options: adminStateOptions } = useCommonCodeData('USER_STATE');
    const { options: roleOptions } = useCustomReqDataCombo({
        url: API_URL.ROLE_COMBO, params: {}, mapping: ROLE_MAPPING,
    });
    // VendorUserFormModal용 권한 콤보 — comGubun에 따라 조회 구분이 다름
    const vendorRoleParams = useMemo(() => {
        const searchRoleGubun = ROLE_GUBUN_BY_COM_GUBUN[getCookie('comGubun')];
        return searchRoleGubun ? { searchRoleGubun, useYn: 'Y' } : { useYn: 'Y' };
    }, []);
    const { options: vendorRoleOptions } = useCustomReqDataCombo({
        url: API_URL.ROLE_COMBO, method: 'GET', params: vendorRoleParams, mapping: ROLE_MAPPING,
    });

    const { handleSubmit: handleVendorSubmit } = useCommonSubmit({
        form: vendorForm || {},
        type: 'json',
        checkField: [
            { inputId: 'comUserId',     type: CODE.TEXT,   message: '아이디를 입력해주세요.' },
            { inputId: 'comUserName',   type: CODE.TEXT,   message: '관리자명을 입력해주세요.' },
            { inputId: 'comUserRoleid', type: CODE.SELECT, message: '권한을 선택해주세요.' },
            { inputId: 'comUserPhone',  type: CODE.TEXT,   message: '전화번호를 입력해주세요.' },
            { inputId: 'comUserEmail',  type: CODE.EMAIL,  message: '이메일을 입력해주세요.' },
        ],
        compareField: [
            { primaryId: 'comPassword', secondaryId: 'comPasswordConfirm', operator: '==', message: '비밀번호가 일치하지 않습니다.' },
        ],
        confirmMessage: `관리자 ${vendorForm?.comUserName || ''}`,
        setModalOpen: setShowVendorModal,
        URL: API_URL.VENDOR_USER_UPDATE,
        reloadFunction: () => {},
    });

    // VendorUserFormModal의 "비밀번호 초기화" 버튼(관리자 관리 탭과 동일 동작)
    const handleVendorPasswordReset = useCallback(async () => {
        if (!vendorForm?.comUserId) return;
        const result = await Swal.fire({
            icon: 'question',
            title: '비밀번호 초기화',
            text: `${vendorForm.comUserName} 관리자의 비밀번호를 초기화 하시겠습니까?`,
            showCancelButton: true,
            confirmButtonText: '예',
            cancelButtonText: '아니오',
            focusCancel: true,
        });
        if (!result.isConfirmed) return;
        try {
            const res = await fnAjaxFetch({
                url: `${API_URL.VENDOR_USER_PWD_RESET}/${vendorForm.comUserId}.do`,
                method: 'GET',
                withCredentials: true,
            });
            const json = res?.data;
            if (json?.resultCodeInfo === 'SUCCESS') {
                await Swal.fire({ icon: 'success', title: '완료', text: json.resultMessage || '비밀번호가 초기화되었습니다.' });
            } else {
                await Swal.fire({ icon: 'warning', title: '실패', text: json?.resultMessage || '' });
            }
        } catch (e) {
            await Swal.fire({ icon: 'error', title: '오류', text: e?.message || '초기화 중 오류' });
        }
    }, [vendorForm]);

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

    // 거래처(판매사/공급사) 계정 로그인 — VendorUserFormModal로 본인 정보 수정
    const handleOpenVendorEditModal = async () => {
        const comUserId = managerInfo?.managerId || '';
        try {
            const res = await fnAjaxFetch({
                url: `${API_URL.VENDOR_USER_INFO}/${comUserId}.do`,
                method: 'GET',
                withCredentials: true,
            });
            const obj = res?.data?.result?.result || null;
            if (obj) {
                setVendorForm({
                    mode: 'Edt',
                    idCheck: 'Y',
                    comCode: obj.comCode || vendorComCode || '',
                    comUserId: obj.comUserId || '',
                    comUserName: obj.comUserName || '',
                    comPassword: '',
                    comPasswordConfirm: '',
                    comUserRoleid: obj.comUserRoleid || '',
                    comUserStatus: obj.comUserStatus || '',
                    comUserPhone: obj.comUserPhone || '',
                    comUserEmail: obj.comUserEmail || '',
                    comUserUseyn: obj.comUserUseyn || 'Y',
                    lastUpdtPnttm: obj.lastUpdtPnttm || '',
                });
                setShowVendorModal(true);
            } else {
                await Swal.fire({ icon: 'warning', title: '조회 실패', text: '거래처 사용자 정보를 불러오지 못했습니다.' });
            }
        } catch (e) {
            await Swal.fire({ icon: 'error', title: '오류', text: e?.message || '상세 조회 중 오류' });
        }
    };

    const handleOpenEditModal = async () => {
        // 거래처 로그인 계정은 거래처 사용자 수정 모달로 분기
        if (vendorComCode) {
            await handleOpenVendorEditModal();
            return;
        }
        //관리자 정보 수정 모달 열기 — ManagerFormModal.jsx에서 mode: 'Edt'로 세팅
        const managerId = managerInfo?.managerId || '';

        try {
            const res = await fnAjaxFetch({ 
                url: `${API_URL.MANAGER_DETAIL}/${managerId}.do`, 
                method: 'GET' 
            });
            const obj = res?.data?.result.result || null;
            
            if (obj) {
                setManagerForm({
                    mode: "Edt",
                    managerId: obj.managerId || '',
                    partId: obj.partId || '',
                    managerName: obj.managerName || '',
                    managerTel: obj.managerTel || '',
                    managerPic: null,
                    managerPosition: obj.managerPosition || '',
                    managerPassword: obj.managerPassword || '',
                    managerPasswordConfirm: obj.managerPassword || '',
                    passwordHint: obj.passwordHint || '',
                    passwordCnsr: obj.passwordCnsr || '',
                    managerEmail: obj.managerEmail || '',
                    roleId: obj.roleId || '',
                    managerStatus: obj.managerStatus || 'USER_STATE_1' ,
                    useYn: obj.useYn || 'N',
                    idCheck: 'Y',
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
                        onData={{ adminStateOptions, roleOptions }}
                    />
                </Suspense>
            )}

            {showVendorModal && vendorForm && (
                <Suspense fallback={null}>
                    <VendorUserFormModal
                        open={showVendorModal}
                        form={vendorForm}
                        setForm={setVendorForm}
                        onClose={() => setShowVendorModal(false)}
                        comboData={{ statusOptions: adminStateOptions, roleOptions: vendorRoleOptions }}
                        onPasswordReset={handleVendorPasswordReset}
                        onSubmit={handleVendorSubmit}
                    />
                </Suspense>
            )}

            {showPwdModal && (
                <Suspense fallback={null}>
                    {vendorComCode ? (
                        <VendorPwdModal
                            open={showPwdModal}
                            managerName={managerInfo?.managerName}
                            onClose={() => setShowPwdModal(false)}
                        />
                    ) : (
                        <ManagerPwdModal
                            open={showPwdModal}
                            managerId={managerInfo?.managerId}
                            managerName={managerInfo?.managerName}
                            onClose={() => setShowPwdModal(false)}
                        />
                    )}
                </Suspense>
            )}
        </div>
    );
}
