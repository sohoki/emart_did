package com.common.backoffice.bas.code.modals;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "COMTCCMMNCODE")
@Getter
@Setter
@Builder
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)  // ← 이게 없으면 @CreatedDate/@LastModifiedDate 무시됨
@AllArgsConstructor
public class Comtccmmncode {

    @Id // 복합키 구성요소 1
    @Column(name = "CODE_ID", length = 20, nullable = false)
    private String codeId; // 코드ID

    @Column(name = "CL_CODE", columnDefinition = "CHAR(7)" )
    private String clCode; // 분류코드

    @Column(name = "CODE_ID_NM", length = 60)
    private String codeIdNm; // 코드ID명

    @Column(name = "CODE_ID_DC", length = 200)
    private String codeIdDc; // 코드ID설명

    @Column(name = "USE_AT", columnDefinition = "CHAR(1)")
    private String useAt; // 사용여부

    @CreatedDate
    @Column(name = "FRST_REGIST_PNTTM",
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP",
            updatable = false)   // ← UPDATE 구문에서 이 컬럼을 완전히 제외
    private LocalDateTime frstRegistPnttm;

    @Column(name = "FRST_REGISTER_ID", length = 20)
    private String frstRegisterId; // 최초등록자ID

    @LastModifiedDate
    @Column(name = "LAST_UPDT_PNTTM" ,columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime lastUpdtPnttm; // 최종수정시점

    @Column(name = "LAST_UPDUSR_ID", length = 20)
    private String lastUpdusrId; // 최종수정자ID

}
