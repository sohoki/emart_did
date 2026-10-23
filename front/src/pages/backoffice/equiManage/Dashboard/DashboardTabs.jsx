import { NavLink } from 'react-router-dom';

const TABS = [
    { to: '/backoffice/sub/equiManage/dashboard', label: '통합 모니터링', end: true },
    { to: '/backoffice/sub/equiManage/dashboard/did', label: '단말기 모니터링' },
    { to: '/backoffice/sub/equiManage/dashboard/brod', label: '음원방송 모니터링' },
];

// 모니터링 3개 화면 상단 탭 — 레거시 dashboardState*.jsp의 topMenu와 동일한 구성
export default function DashboardTabs() {
    return (
        <div className="col-12" style={{ display: 'flex', gap: 4, padding: '0 15px', borderBottom: '2px solid #dde2eb' }}>
            {TABS.map((tab) => (
                <NavLink key={tab.to} to={tab.to} end={tab.end}
                    style={({ isActive }) => ({
                        padding: '10px 18px', fontSize: 14, textDecoration: 'none', marginBottom: -2,
                        color: isActive ? '#1d4ed8' : '#475569',
                        fontWeight: isActive ? 700 : 500,
                        borderBottom: isActive ? '2px solid #1d4ed8' : '2px solid transparent',
                    })}>
                    {tab.label}
                </NavLink>
            ))}
        </div>
    );
}
