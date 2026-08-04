const AuthHeader = () => {
    return (
        <header className="auth-header">
            <img src="/resource/img/emartLogo.png" alt="로고" className="auth-header-logo" />
            <div className="dropdown"  style={{ position: 'relative' }}>
                <div
                    className="header-user-info"
                    style={{ cursor: 'pointer' }}
                  
                >
                </div>
            </div>
        </header>
    );
};

export default AuthHeader;
