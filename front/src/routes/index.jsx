import { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import ScrollToTop from '@/components/Utils/ScrollToTop.jsx';
import ProtectedRoute from '@/routes/ProtectedRoute.jsx';
import AppLayout from '@/layouts/AppLayout.jsx';
import AppTopDownLayout from '@/layouts/AppTopDownLayout.jsx';
import PageLoading from '@/components/Common/PageLoading.jsx';

const LoginPage = lazy(() => import('@/pages/Login/LoginPage.jsx'));

{/* 기초 관리 (front_common 공유 템플릿에서 이식 — did_emart 백엔드가 이미 동일 계약으로 존재) */}
const CodeInfo = lazy(() => import('@/pages/backoffice/Basic/CodeInfo.jsx'));
const MenuInfo = lazy(() => import('@/pages/backoffice/Basic/MenuInfo.jsx'));
const RoleInfo = lazy(() => import('@/pages/backoffice/Basic/RoleInfo.jsx'));
const ProgrameInfo = lazy(() => import('@/pages/backoffice/Basic/ProgrameInfo.jsx'));

{/* 인사 관리 */}
const ManagerListPage = lazy(() => import('@/pages/backoffice/HrInfo/ManagerListPage.jsx'));
const GroupListPage = lazy(() => import('@/pages/backoffice/HrInfo/GroupListPage.jsx'));

{/* 기초(매장) 관리 */}
const CenterListPage = lazy(() => import('@/pages/backoffice/BasicManage/CenterListPage.jsx'));

{/* 운영 관리 */}
const XmlListPage = lazy(() => import('@/pages/backoffice/OperManage/XmlListPage.jsx'));
const SendMsgListPage = lazy(() => import('@/pages/backoffice/OperManage/SendMsgListPage.jsx'));
const DidPicListPage = lazy(() => import('@/pages/backoffice/OperManage/DidPicListPage.jsx'));

{/* 장비 관리 */}
const DidInfoList = lazy(() => import('@/pages/backoffice/equiManage/DidInfoList.jsx'));
const DIdGroupInfo = lazy(() => import('@/pages/backoffice/equiManage/DIdGroupInfo.jsx'));
const DidSendMessageList = lazy(() => import('@/pages/backoffice/equiManage/DidSendMessageList.jsx'));
const ContentMessageListPage = lazy(() => import('@/pages/backoffice/ConManage/ContentMessageListPage.jsx'));

{/* 문화센터 관리 */}
const MhsRoomManagePage = lazy(() => import('@/pages/backoffice/RoomManage/MhsRoomManagePage.jsx'));

{/* 콘텐츠 관리 */}
const ContentFileLibraryPage = lazy(() => import('@/pages/backoffice/ConManage/ContentFileLibraryPage.jsx'));
const ContentMutiListPage = lazy(() => import('@/pages/backoffice/ConManage/ContentMutiListPage.jsx'));
const ContentDetailEditorPage = lazy(() => import('@/pages/backoffice/ConManage/ContentDetailEditorPage.jsx'));

{/* 방송 관리 */}
const BrodAnniversaryListPage = lazy(() => import('@/pages/backoffice/BrodManage/BrodAnniversaryListPage.jsx'));
const BrodScheduleStatusPage = lazy(() => import('@/pages/backoffice/BrodManage/BrodScheduleStatusPage.jsx'));
const BrodContentListPage = lazy(() => import('@/pages/backoffice/BrodManage/BrodContentListPage.jsx'));
const BasicBrodListPage = lazy(() => import('@/pages/backoffice/BrodManage/BasicBrodListPage.jsx'));

export default function RouterConfig() {
    return (
        <>
            <ScrollToTop />
            <Suspense fallback={<PageLoading />}>
                <Routes>
                    <Route path="/" element={<Navigate to="/login" replace />} />

                    {/* 인증이 필요한 라우트 */}
                    <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
                        {/* 기초 관리 */}
                        <Route path="/sub/bas/codeInfo" element={<CodeInfo />} />
                        <Route path="/sub/bas/menuInfo" element={<MenuInfo />} />
                        <Route path="/sub/bas/roleInfo" element={<RoleInfo />} />
                        <Route path="/sub/bas/programeInfo" element={<ProgrameInfo />} />

                        {/* 인사 관리 */}
                        <Route path="/backoffice/hr/manager" element={<ManagerListPage />} />
                        <Route path="/backoffice/sub/basicManage/group" element={<GroupListPage />} />

                        {/* 기초(매장) 관리 */}
                        <Route path="/backoffice/sub/basicManage/cnt" element={<CenterListPage />} />

                        {/* 운영 관리 */}
                        <Route path="/backoffice/sub/operManage/xml" element={<XmlListPage />} />
                        <Route path="/backoffice/sub/operManage/snd" element={<SendMsgListPage />} />

                        {/* 장비 관리 */}
                        <Route path="/backoffice/sub/equiManage/did" element={<DidInfoList />} />
                        <Route path="/backoffice/sub/equiManage/did_group" element={<DIdGroupInfo />} />
                        <Route path="/backoffice/sub/equiManage/message_list" element={<DidSendMessageList />} />
                        { /* did 모니터 캡처 화면 */}
                        <Route path="/backoffice/sub/equiManage/pic" element={<DidPicListPage />} />
                        <Route path="/backoffice/sub/equiManage/message" element={<ContentMessageListPage />} />

                        {/* 문화센터 관리 */}
                        <Route path="/backoffice/sub/roomManage/mhs" element={<MhsRoomManagePage />} />

                        {/* 콘텐츠 관리 */}
                        <Route path="/backoffice/sub/conManage/file" element={<ContentFileLibraryPage />} />
                        <Route path="/backoffice/sub/conManage/muti" element={<ContentMutiListPage />} />
                        <Route path="/backoffice/sub/conManage/muti/edit" element={<ContentDetailEditorPage />} />

                        {/* 방송 관리 */}
                        <Route path="/backoffice/sub/brodManage/anniver" element={<BrodAnniversaryListPage />} />
                        <Route path="/backoffice/sub/brodManage/schedule" element={<BrodScheduleStatusPage />} />
                        <Route path="/backoffice/sub/brodManage/content" element={<BrodContentListPage />} />
                        <Route path="/backoffice/sub/brodManage/basic" element={<BasicBrodListPage />} />
                    </Route>

                    {/* 인증이 필요 없는 라우트 */}
                    <Route element={<AppTopDownLayout />}>
                        <Route path="/login" element={<LoginPage />} />
                    </Route>

                    {/* 미정의 경로는 로그인으로 이동 */}
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </Suspense>
        </>
    );
}
