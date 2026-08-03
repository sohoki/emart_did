import { Navigate, Route, Routes } from 'react-router-dom';
import LoginPage from '@/pages/Login/LoginPage.jsx';
import ManagerListPage from '@/pages/backoffice/HrInfo/ManagerListPage.jsx';
import CenterListPage from '@/pages/backoffice/BasicManage/CenterListPage.jsx';
import CenterAnniListPage from '@/pages/backoffice/BasicManage/CenterAnniListPage.jsx';
import XmlListPage from '@/pages/backoffice/OperManage/XmlListPage.jsx';
import SendMsgListPage from '@/pages/backoffice/OperManage/SendMsgListPage.jsx';
import DidPicListPage from '@/pages/backoffice/OperManage/DidPicListPage.jsx';
import MhsRoomManagePage from '@/pages/backoffice/RoomManage/MhsRoomManagePage.jsx';
import ContentFileLibraryPage from '@/pages/backoffice/ConManage/ContentFileLibraryPage.jsx';
import ContentMutiListPage from '@/pages/backoffice/ConManage/ContentMutiListPage.jsx';
import ContentDetailEditorPage from '@/pages/backoffice/ConManage/ContentDetailEditorPage.jsx';
import ContentMessageListPage from '@/pages/backoffice/ConManage/ContentMessageListPage.jsx';
import BrodAnniversaryListPage from '@/pages/backoffice/BrodManage/BrodAnniversaryListPage.jsx';
import BrodScheduleStatusPage from '@/pages/backoffice/BrodManage/BrodScheduleStatusPage.jsx';
import BrodContentListPage from '@/pages/backoffice/BrodManage/BrodContentListPage.jsx';
import BasicBrodListPage from '@/pages/backoffice/BrodManage/BasicBrodListPage.jsx';
import './App.css';

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/backoffice/hr/manager" element={<ManagerListPage />} />
      <Route path="/backoffice/sub/basicManage/cnt" element={<CenterListPage />} />
      <Route path="/backoffice/sub/basicManage/cnt/anni" element={<CenterAnniListPage />} />
      <Route path="/backoffice/sub/operManage/xml" element={<XmlListPage />} />
      <Route path="/backoffice/sub/operManage/snd" element={<SendMsgListPage />} />
      <Route path="/backoffice/sub/equiManage/pic" element={<DidPicListPage />} />
      <Route path="/backoffice/sub/roomManage/mhs" element={<MhsRoomManagePage />} />
      <Route path="/backoffice/sub/conManage/file" element={<ContentFileLibraryPage />} />
      <Route path="/backoffice/sub/conManage/muti" element={<ContentMutiListPage />} />
      <Route path="/backoffice/sub/conManage/muti/edit" element={<ContentDetailEditorPage />} />
      <Route path="/backoffice/sub/equiManage/message" element={<ContentMessageListPage />} />
      <Route path="/backoffice/sub/brodManage/anniver" element={<BrodAnniversaryListPage />} />
      <Route path="/backoffice/sub/brodManage/schedule" element={<BrodScheduleStatusPage />} />
      <Route path="/backoffice/sub/brodManage/content" element={<BrodContentListPage />} />
      <Route path="/backoffice/sub/brodManage/basic" element={<BasicBrodListPage />} />
    </Routes>
  );
}

export default App;
