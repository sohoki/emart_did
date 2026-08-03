import React from 'react';

const Footer = () => {
  return (
    <footer className="footer-wrap">
      <div className="footer-inner">
        <div className="footer-divider" />

        <div className="footer-info">
          <p>
            <span>(주)에이텐시스템</span>
            <span className="footer-sep">|</span>
            <span>서울특별시 금천구 가산디지털2로 67, 203호 (가산동, 에이스하이엔드7차)</span>
          </p>
          <p>
            <span>Tel. 02-862-3360</span>
            <span className="footer-sep">|</span>
            <span>Fax. 02-6280-3430</span>
          </p>
          <p className="footer-copy">Copyright © 2026 에이텐시스템 All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
