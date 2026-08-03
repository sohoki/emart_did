package com.common.backoffice.bas.menu.modals;


import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;


@Entity // 👈 이 명찰이 반드시 있어야 합니다.
@Table(name = "COMTNMENUINFO")
@Getter
@Setter // 필요에 따라 Setter는 제거하고 Builder 패턴을 사용하셔도 됩니다.
@NoArgsConstructor // 앞서 발생했던 에러를 방지하기 위해 기본 생성자 추가
@AllArgsConstructor
@Builder
public class ContMenuInfo {

    @Id
    @Column(name = "MENU_NO", precision = 20, scale = 0, nullable = false)
    private BigDecimal menuNo; // 메뉴 번호 (PK로 추정됨)

    @Column(name = "MENU_NM", length = 60)
    private String menuNm; // 메뉴 명

    @Column(name = "PROGRM_FILE_NM", length = 60)
    private String progrmFileNm; // 프로그램명

    @Column(name = "UPPER_MENU_NO", precision = 20, scale = 0)
    private BigDecimal upperMenuNo; // 상위 메뉴 정보

    @Column(name = "RELATE_IMAGE_PATH", length = 100)
    private String relateImagePath; // 메뉴 이미지 경로

    @Column(name = "RELATE_IMAGE_NM", length = 60)
    private String relateImageNm; // 메뉴 이미지 명

    @Column(name = "MENU_PAGE_TARGET", length = 30)
    private String menuPageTarget; // 메뉴 페이지 연결 여부

    @Column(name = "MENU_POPUPNFO", length = 255)
    private String menuPopupnfo; // 팝업 정보

    @Column(name = "MENU_ORDR", precision = 5, scale = 0)
    private BigDecimal menuOrdr; // 메뉴 정렬 순서

    @Column(name = "MENU_USEYN", length = 1, columnDefinition = "char(1)")
    private String menuUseyn; // 사용유무 (char(1) 이지만 String으로 매핑하는 것이 일반적입니다)

    @Column(name = "MENU_DC", length = 250)
    private String menuDc; // 메뉴 상세 설명

    @Column(name = "MENU_PRIVACY", columnDefinition = "char(1)")
    private String menuPrivacy; // 개인정보 여부

    @Column(name = "MENU_CLASS", length = 30)
    private String menuClass; // (주석 없음, 클래스 관련 정보로 추정)

    @Column(name = "MENU_ICON_TYPE", length = 20)
    private String menuIconType; // (주석 없음, 클래스 관련 정보로 추정)

    @CreatedDate
    @Column(name = "FRST_REGIST_PNTTM",
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP",
            updatable = false)
    private LocalDateTime frstRegistPnttm; // 최초등록시점

    @Column(name = "FRST_REGISTER_ID", length = 20)
    private String frstRegisterId; // 최초등록자ID

    @LastModifiedDate
    @Column(name = "LAST_UPDT_PNTTM",columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime lastUpdtPnttm; // 최종수정시점

    @Column(name = "LAST_UPDUSR_ID", length = 20)
    private String lastUpdusrId; // 최종수정자ID


}
