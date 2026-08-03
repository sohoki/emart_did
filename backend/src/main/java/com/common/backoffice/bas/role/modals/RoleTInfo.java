package com.common.backoffice.bas.role.modals;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TB_ROLEINFO") // 실제 DB 테이블명으로 수정하세요
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleTInfo {

    @Id
    @Column(name = "ROLE_ID", length = 50)
    private String roleId;

    @Column(name = "ROLE_NAME", length = 30)
    private String roleName;

    @Column(name = "ROLE_DC", length = 255)
    private String roleDc;

    @Column(name = "ROLE_USEYN", length = 1, columnDefinition = "char(1)")
    private String roleUseyn;

    @CreatedDate
    @Column(name = "FRST_REGIST_PNTTM",
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP",
            updatable = false)
    private LocalDateTime frstRegistPnttm; // 최초등록시점

    @Column(name = "FRST_REGISTER_ID", length = 50)
    private String frstRegisterId; // 최초등록자ID

    @LastModifiedDate
    @Column(name = "LAST_UPDT_PNTTM" ,columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime lastUpdtPnttm; // 최종수정시점

    @Column(name = "LAST_UPDUSR_ID", length = 50)
    private String lastUpdusrId; // 최종수정자ID
}
