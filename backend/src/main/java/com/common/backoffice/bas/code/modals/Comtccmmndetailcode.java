package com.common.backoffice.bas.code.modals;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "COMTCCMMNDETAILCODE")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)  // ← 이게 없으면 @CreatedDate/@LastModifiedDate 무시됨
@NoArgsConstructor
public class Comtccmmndetailcode {


    @Column(name = "CODE_ID", length = 20, nullable = false)
    private String codeId; // 코드ID

    @Id // 복합키 1
    @Column(name = "CODE", length = 25, nullable = false)
    private String code; // 코드

    @Column(name = "CODE_NM", length = 60)
    private String codeNm; // 코드명

    @Column(name = "CODE_DC", length = 200)
    private String codeDc; // 코드설명

    @Column(name = "USE_AT", columnDefinition = "CHAR(1)")
    private String useAt; // 사용여부

    @Column(name = "CODE_ETC1", length = 100)
    private String codeEtc1; // 비고1

    @Column(name = "CODE_ETC2", length = 100)
    private String codeEtc2; // 비고2

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
