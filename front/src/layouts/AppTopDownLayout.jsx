import { Outlet } from 'react-router-dom';
import AuthHeader from '@/components/Layout/AuthHeader.jsx';
import Footer from '@/components/Layout/Footer.jsx';

const AppTopDownLayout = () => {
    return (
        <div className="wrapper">
            <AuthHeader />
            <main style={{ minHeight: 'calc(100vh - 120px)' }}>
                <Outlet />
            </main>
            <Footer />
        </div>
    );
};

export default AppTopDownLayout;
