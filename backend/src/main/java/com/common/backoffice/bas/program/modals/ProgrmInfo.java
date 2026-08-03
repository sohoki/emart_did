package com.common.backoffice.bas.program.modals;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "COMTNPROGRMLIST") // 대소문자 구분 확인 필요
@Getter
@NoArgsConstructor
public class ProgrmInfo {

    @Id // 기본키(PK) 지정 (실제 DB의 PK에 맞게 수정 필요 시 변경하세요)
    @Column(name = "PROGRM_FILE_NM", length = 60, nullable = false)
    private String progrmFileNm;    // 프로그램 명

    @Column(name = "PROGRM_STRE_PATH", length = 100)
    private String progrmStrePath;  // 프로그램 패스

    @Column(name = "PROGRM_KOREAN_NM", length = 60)
    private String progrmKoreanNm;  // 프로그램 한글명

    @Column(name = "PROGRM_ENG_NM", length = 60)
    private String progrmEngNm;     // 프로그램 영문명

    @Column(name = "PROGRM_DC", length = 200)
    private String progrmDc;        // 프로그램 상세 설명

    @Column(name = "URL", length = 100)
    private String url;             // 접속 주소

    @Column(name = "TEST_URL", length = 100)
    private String testUrl;


    // 생성자 레벨에 @Builder 적용 (객체 생성 시 안전하고 가독성 좋게 생성하기 위함)
    @Builder
    public ProgrmInfo(String progrmFileNm, String progrmStrePath, String progrmKoreanNm,
                      String progrmEngNm, String progrmDc, String url, String testUrl) {
        this.progrmFileNm = progrmFileNm;
        this.progrmStrePath = progrmStrePath;
        this.progrmKoreanNm = progrmKoreanNm;
        this.progrmEngNm = progrmEngNm;
        this.progrmDc = progrmDc;
        this.url = url;
        this.testUrl = testUrl;
    }
    // ==========================================
    // 💡 여기에 업데이트용 메서드를 꼭 추가해 주셔야 합니다!
    // ==========================================
    public void updateProgrm(String progrmStrePath, String progrmKoreanNm, String progrmDc, String url, String testUrl) {
        this.progrmStrePath = progrmStrePath;
        this.progrmKoreanNm = progrmKoreanNm;
        this.progrmDc = progrmDc;
        this.url = url;
        this.testUrl = testUrl;
    }
}

