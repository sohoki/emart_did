package com.common.backoffice.bas.program.service.repository;

import com.common.backoffice.bas.program.modals.ProgrmInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComtnprogrmlistRepository  extends JpaRepository<ProgrmInfo, String> {
    //중복 체크
    boolean existsById(String progrmFileNm);

    // 2. URL 기준 조회 (영속성 컨텍스트 활용 삭제용)
    Optional<ProgrmInfo> findByProgrmFileNm(String progrmFileNm);

    // 3. URL 기준 즉시 삭제
    void deleteByUrl(String url);

    void deleteById(String progrmFileNm);

    @Modifying
    @Query("DELETE FROM ProgrmInfo p WHERE p.progrmFileNm IN :ids")
    int deleteByProgrmFileNmIn(@Param("ids") List<String> ids);
}
