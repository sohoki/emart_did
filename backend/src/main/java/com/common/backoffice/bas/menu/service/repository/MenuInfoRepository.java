package com.common.backoffice.bas.menu.service.repository;


import java.math.BigDecimal;

import com.common.backoffice.bas.menu.modals.ContMenuInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MenuInfoRepository extends JpaRepository<ContMenuInfo, String>{

    // 💡 1. 중복 확인: 메뉴 이름(MENU_NM)이 이미 존재하는지 확인
    // 반환값: 존재하면 true, 없으면 false
    boolean existsByMenuNo( BigDecimal menuNo );

    // 💡 (응용) 특정 상위 메뉴 안에서 똑같은 이름이 있는지 중복 확인
    boolean existsByMenuNmAndUpperMenuNo(String menuNm, BigDecimal upperMenuNo);

    // 💡 2. 삭제: 이름으로 메뉴 삭제하기 (PK가 아닌 조건으로 삭제할 때)
    void deleteByMenuNo(BigDecimal menuNo);

    // ⛔ 참고: PK(메뉴 번호)로 삭제하거나 중복을 체크하는 메서드는
    // JpaRepository에 이미 있으므로 여기에 적지 않아도 됩니다!
    // (기본 내장 메서드: existsById(), deleteById())
}

