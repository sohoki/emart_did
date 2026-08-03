package com.common.backoffice.bas.code.service.repository;

import com.common.backoffice.bas.code.modals.Comtccmmncode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
@Repository
public interface ComtccmmcodeRepository extends JpaRepository<Comtccmmncode, String> {


    boolean existsByCodeId(String codeId);

    @Transactional
    void deleteByCodeId(String codeId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Comtccmmncode c SET c.useAt = :useAt, c.lastUpdusrId = :userId WHERE c.codeId = :codeId")
    int updateUseAtOnly(@Param("useAt") String useAt,
                        @Param("userId") String userId,
                        @Param("codeId") String codeId);

}
