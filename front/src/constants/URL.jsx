const URL = {
    MAIN: "/backoffice", //메인페이지

    CODE_LIST: "/backoffice/basic/codelist", //코드목록
    CODEID_LIST:"/api/backoffice/sys/cmm/cca/code/codeList.do",
    CODE_INFO : "/api/backoffice/sys/cmm/cca",
    CODE_LIST_SUB_LIST:"/api/backoffice/sys/cmm/cde/detailList.do",
    CODE_PROCESS_UPDATE:"/api/backoffice/sys/cmm/cca/code/codeUpdate.do",
    CODE_PROCESS_UPDATE_USEYN:"/api/backoffice/sys/cmm/cca/code/codeUseYnUpdate.do",
    
    CODE_LIST_COMBO_CDE: "/api/backoffice/sys/cmm/cde/combo",
    CODE_ID_CHECK:"/api/backoffice/sys/cmm/cca/codeIDCheck",
    //코드 상세
    CODE_DETAIL_UPDATE : "/api/backoffice/sys/cmm/cde/CodeDetailUpdate.do",
    CODE_DETAIL_UPDATE_USEYN : "/api/backoffice/sys/cmm/cde/CodeDetailUpdateYn.do",
    CODE_DETAIL_INFO : "/api/backoffice/sys/cmm/cde",


    PROGRAM_LIST: "/api/backoffice/sys/prog/programList.do", //프로그램목록
    PROGRAME_PROCESS: "/api/backoffice/sys/prog/updateProgrmInfo.do",
    PROGRAM_INFO: "/api/backoffice/sys/prog",
    PROGRAM_ID_CHECK:"/api/backoffice/sys/prog/programIDCheck",

    LOGIN: "/login", //로그인
    LOGIN_PARTER: "/loginPartners",
    ERROR: "/noPage", //로그인

    MENU_LIST : "/api/backoffice/sys/menu/menuListAjax.do",
    MENU_SAVE:"/api/backoffice/sys/menu/menuRegistUpdate.do",
    MENU_ORDER_UPDATE: "/api/backoffice/sys/menu/updateMenuOrder.do",
    MENU_ID_CHECK: "/api/backoffice/sys/menu/menuCheck",
    MENU_DELETE:"/api/backoffice/sys/menu",
    MENU_CREATE_UPDATE:"/api/backoffice/sys/menu/menuCreateUpdateAjax.do", // 권한별 메뉴 생성

    //즐겨찾기 정보
    BOOKMARKS_LIST: "/api/backoffice/bas/books/bookmarksListAjax.do",
    BOOKMARKS_UPDATE: "/api/backoffice/bas/books/updateBookMarksInfo.do",
    BOOKMARKS_ORDER_UPDATE: "/api/backoffice/bas/books/updateBookMarksOrder.do",
    BOOKMARKS_DELETE: "/api/backoffice/bas/books/deleteBookMarksInfo.do",

    

    //게시판 마스터(정의) 관리자 정보
    BOARD_MASTER_LIST: "/api/backoffice/board/master/boardMasterListAjax.do",
    BOARD_MASTER_INFO: "/api/backoffice/board/master",
    BOARD_MASTER_ID_CHECK: "/api/backoffice/board/master/idCheck",
    BOARD_MASTER_UPDATE: "/api/backoffice/board/master/updateBoardMaster.do",
    BOARD_MASTER_USE_AT_UPDATE: "/api/backoffice/board/master/updateBoardMasterUseAt.do",

    //게시물 정보
    BOARD_POST_LIST: "/api/backoffice/board/post/boardListAjax.do",
    BOARD_POST_INFO: "/api/backoffice/board/post",
    BOARD_POST_UPDATE: "/api/backoffice/board/post/updateBoard.do",
    BOARD_POST_REPLY: "/api/backoffice/board/post/replyBoard.do",
    //판매사몰 홈 화면용 공지사항 — 인증 불필요, 최대 5건
    BOARD_POST_MALL_LIST: "/api/backoffice/board/post/cus",

    //게시물 댓글 정보
    BOARD_COMMENT_INFO: "/api/backoffice/board/comment",
    BOARD_COMMENT_UPDATE: "/api/backoffice/board/comment/updateComment.do",

    //FAQ 정보
    FAQ_LIST: "/api/backoffice/board/faq/faqListAjax.do",
    FAQ_INFO: "/api/backoffice/board/faq",
    FAQ_UPDATE: "/api/backoffice/board/faq/updateFaq.do",

    //FAQ 카테고리 정보
    FAQ_CATEGORY_LIST: "/api/backoffice/board/faq/category/listAjax.do",
    FAQ_CATEGORY_COMBO: "/api/backoffice/board/faq/category/combo.do",
    FAQ_CATEGORY_INFO: "/api/backoffice/board/faq/category",
    FAQ_CATEGORY_UPDATE: "/api/backoffice/board/faq/category/updateFaqCategory.do",

    //FAQ 거래처(판매사/공급사) 조회용
    FAQ_VENDOR_LIST: "/api/backoffice/board/faq/vendorListAjax.do",
    //FAQ 판매사 로그인(비로그인) 화면용 — 인증 불필요, 최대 5건
    FAQ_LOGIN_TOP5: "/api/backoffice/board/faq/cus/loginTop5.do",

    //거래처 Q&A 정보
    QNA_LIST: "/api/backoffice/board/qna/qnaListAjax.do",
    QNA_INFO: "/api/backoffice/board/qna",
    QNA_INSERT_QUESTION: "/api/backoffice/board/qna/insertQuestion.do",
    QNA_ANSWER_UPDATE: "/api/backoffice/board/qna/updateAnswer.do",

    //권한 정보
    ROLE_ID_CHECK:"/api/backoffice/uat/role/idCheck",
    ROLE_UPDATE:"/api/backoffice/uat/role/roleUpdate.do",
    //신규
    ROLE_USEYN_UPDATE: "/api/backoffice/uat/role/roleUseynUpdate.do",
    ROLE_DELETE:"/api/backoffice/uat/role",
    ROLE_LIST : "/api/backoffice/uat/role/roleList.do",
    ROLE_MENU : "/api/backoffice/sys/menu/roleMenu",
    ROLE_COMBO: "/api/backoffice/uat/role/roleCombo.do",

    //관리자 
    MANAGER_LIST: "/api/backoffice/hr/manager/empList.do",
    MANAGER_UPDATE: "/api/backoffice/hr/manager/managerUpdate.do",
    MANAGER_ID_CHECK : "/api/backoffice/hr/manager/idCheck",
    MANAGER_DELETE : "/api/backoffice/hr/manager",
    MANAGER_DETAIL : "/api/backoffice/hr/manager",
    MANAGER_DETAIL_PWD : "/api/backoffice/hr/manager/pwd",
    MANAGER_STATE_CHANGE: "/api/backoffice/hr/manager/StateChange",
    MANAGER_UPDATE_USEYN: "/api/backoffice/hr/manager/managerUpdateUseyn.do",
    MANAGER_LOGIN_LIST : "/api/backoffice/uat/uia/loginLogListAjax.do",
    MANAGER_USEYN : "/api/backoffice/hr/manager/useyn",
    MANAGER_PW_UPDATE: "/api/backoffice/hr/manager/passChange.do",

    //매장(센터) 정보
    CENTER_LIST: "/api/backoffice/sub/basicManage/cnt/list.do",
    CENTER_COMBO: "/api/backoffice/sub/basicManage/cnt/combo.do",
    CENTER_INFO: "/api/backoffice/sub/basicManage/cnt",
    CENTER_UPDATE: "/api/backoffice/sub/basicManage/cnt/update.do",
    CENTER_EXCEL_UPLOAD: "/api/backoffice/sub/basicManage/cnt/excelUpload.do",

    //매장(센터) 기념일 정보
    CENTER_ANNI_LIST: "/api/backoffice/sub/basicManage/cnt/anni/list.do",
    CENTER_ANNI_INFO: "/api/backoffice/sub/basicManage/cnt/anni",
    CENTER_ANNI_UPDATE: "/api/backoffice/sub/basicManage/cnt/anni/update.do",
    CENTER_ANNI_CNT_CHECK: "/api/backoffice/sub/basicManage/cnt/anni/cntCheck.do",
    CENTER_ANNI_BROD_COMBO: "/api/backoffice/sub/basicManage/cnt/anni/brodCombo.do",
    CENTER_ANNI_BROD_DAY_INFO: "/api/backoffice/sub/basicManage/cnt/anni/brodDayInfo.do",

    //XML(장비 통신 명령) 정보
    XML_LIST: "/api/backoffice/sub/operManage/xml/list.do",
    XML_INFO: "/api/backoffice/sub/operManage/xml",
    XML_UPDATE: "/api/backoffice/sub/operManage/xml/update.do",
    XML_WORK_GUBUN_COMBO: "/api/backoffice/sub/operManage/xml/workGubunCombo.do",
    XML_PROCESS_CHECK: "/api/backoffice/sub/operManage/xml/processCheck.do",
    XML_PREVIEW_JSON: "/api/backoffice/sub/operManage/xml/preview/json",
    XML_PREVIEW_XML: "/api/backoffice/sub/operManage/xml/preview/xml",

    //DID 발송 이력
    SND_LIST: "/api/backoffice/sub/operManage/snd/list.do",
    SND_CENTER_COMBO: "/api/backoffice/sub/operManage/snd/centerCombo.do",
    SND_PROCESS_COMBO: "/api/backoffice/sub/operManage/snd/processCombo.do",

    //DID 단말기 관리
    DID_LIST: "/api/backoffice/sub/equiManage/did/list.do",
    DID_INFO: "/api/backoffice/sub/equiManage/did",
    DID_UPDATE: "/api/backoffice/sub/equiManage/did/update.do",
    DID_FORM_DATA: "/api/backoffice/sub/equiManage/did/formData.do",
    DID_RESTART: "/api/backoffice/sub/equiManage/did/restart.do",

    //DID 그룹 관리(TB_GROUP + TB_GROUPDID) — 부서 개념인 LETTNAUTHORGROUPINFO(GROUP_COMBO)와는 별개
    DID_GROUP_LIST: "/api/backoffice/sub/equiManage/group/didGroupList.do",
    DID_GROUP_ID_CHECK: "/api/backoffice/sub/equiManage/group/IdCheck",
    DID_GROUP_UPDATE: "/api/backoffice/sub/equiManage/group/didgroupUpdate.do",
    DID_GROUP_DELETE: "/api/backoffice/sub/equiManage/group/del",
    DID_GROUP_MEMBER_LIST: "/api/backoffice/sub/equiManage/group/didgroupLst",
    DID_GROUP_MEMBER_DELETE: "/api/backoffice/sub/equiManage/group/didgroupDel",
    DID_GROUP_MEMBER_COMBO: "/api/backoffice/sub/equiManage/group/didcomboLst.do",
    DID_GROUP_MEMBER_INSERT: "/api/backoffice/sub/equiManage/group/didgroupInsret.do",

    //발송 스케줄(방송 예약) 관리
    SCH_LIST: "/api/backoffice/sub/equiManage/sch/list.do",
    SCH_FORM_DATA: "/api/backoffice/sub/equiManage/sch/formData.do",
    SCH_CONTENT_SEARCH: "/api/backoffice/sub/equiManage/sch/contentSearch.do",
    SCH_INFO: "/api/backoffice/sub/equiManage/sch",
    SCH_DELETE_BULK: "/api/backoffice/sub/equiManage/sch/deleteBulk.do",
    SCH_UPDATE: "/api/backoffice/sub/equiManage/sch/update.do",

    //DID 캡처화면(모니터링 사진)
    DID_PIC_LIST: "/api/backoffice/sub/equiManage/pic/list.do",
    DID_PIC_UPLOAD: "/api/backoffice/sub/equiManage/pic/upload.do",

    //문화센터(MHS) 브랜드/매장 조회(콤보/필터용)
    MHS_BRAND_LIST: "/api/backoffice/sub/roomManage/mhs/brand/list.do",
    MHS_CENTER_LIST: "/api/backoffice/sub/roomManage/mhs/center/list.do",
    //문화센터(MHS) 모니터 관리
    MHS_MONITOR_LIST: "/api/backoffice/sub/roomManage/mhs/monitor/list.do",
    MHS_MONITOR_INFO: "/api/backoffice/sub/roomManage/mhs/monitor",
    MHS_MONITOR_COMBO: "/api/backoffice/sub/roomManage/mhs/monitor/combo.do",
    MHS_MONITOR_UPDATE: "/api/backoffice/sub/roomManage/mhs/monitor/update.do",
    //문화센터(MHS) 강의 관리
    MHS_CLASS_LIST: "/api/backoffice/sub/roomManage/mhs/class/list.do",
    MHS_CLASS_INFO: "/api/backoffice/sub/roomManage/mhs/class",
    MHS_CLASS_COMBO: "/api/backoffice/sub/roomManage/mhs/class/combo.do",
    MHS_CLASS_UPDATE: "/api/backoffice/sub/roomManage/mhs/class/update.do",
    //문화센터(MHS) 편성(모니터-강의 시간표) 관리
    MHS_VIEWCONN_LIST: "/api/backoffice/sub/roomManage/mhs/viewConn/list.do",
    MHS_VIEWCONN_PREVIEW: "/api/backoffice/sub/roomManage/mhs/viewConn/preview.do",
    MHS_VIEWCONN_INSERT: "/api/backoffice/sub/roomManage/mhs/viewConn/insert.do",
    MHS_VIEWCONN_INFO: "/api/backoffice/sub/roomManage/mhs/viewConn",
    //문화센터(MHS) 조직(부서) 관리
    MHS_GROUP_INFO: "/api/backoffice/sub/roomManage/mhs/group",
    MHS_GROUP_UPDATE: "/api/backoffice/sub/roomManage/mhs/group/update.do",

    //콘텐츠 파일(이미지/영상/음원) 라이브러리
    CON_FILE_LIST: "/api/backoffice/sub/conManage/file/list.do",
    CON_FILE_INFO: "/api/backoffice/sub/conManage/file",
    CON_FILE_UPLOAD: "/api/backoffice/sub/conManage/file/upload.do",
    CON_FILE_USEYN_BULK: "/api/backoffice/sub/conManage/file/useYnBulk.do",
    CON_FILE_DELETE_BULK: "/api/backoffice/sub/conManage/file/deleteBulk.do",
    CON_FILE_CONN_CHECK: "/api/backoffice/sub/conManage/file/connCheck.do",

    //멀티페이지 콘텐츠 관리
    CON_MUTI_LIST: "/api/backoffice/sub/conManage/muti/list.do",
    CON_MUTI_INFO: "/api/backoffice/sub/conManage/muti",
    CON_MUTI_UPDATE: "/api/backoffice/sub/conManage/muti/update.do",
    CON_MUTI_FORM_DATA: "/api/backoffice/sub/conManage/muti/formData.do",
    CON_MUTI_DETAIL_COMBO: "/api/backoffice/sub/conManage/muti/detailCombo.do",

    //콘텐츠 상세페이지-파일 연결(편성) 관리
    CON_DETAIL_FILE_LIST: "/api/backoffice/sub/conManage/detailFile/list.do",
    CON_DETAIL_FILE_INSERT: "/api/backoffice/sub/conManage/detailFile/insert.do",
    CON_DETAIL_FILE_INFO: "/api/backoffice/sub/conManage/detailFile",
    CON_DETAIL_FILE_ORDER_UPDATE: "/api/backoffice/sub/conManage/detailFile/orderUpdate.do",
    CON_DETAIL_FILE_TIME_UPDATE: "/api/backoffice/sub/conManage/detailFile/timeIntervalUpdateAndSum.do",
    CON_DETAIL_FILE_SUM_TIME: "/api/backoffice/sub/conManage/detailFile/sumTime.do",
    CON_DETAIL_PREVIEW_CHECK: "/api/backoffice/sub/conManage/detailFile/preViewCheck.do",
    CON_DETAIL_CONTENT_PREVIEW: "/api/backoffice/sub/conManage/detailFile/contentPreview.do",
    CON_DETAIL_SCHEDULE_SEND: "/api/backoffice/sub/conManage/detailFile/contentScheduleSend.do",

    //DID 발송 메시지(자막) 관리
    CON_MESSAGE_LIST: "/api/backoffice/sub/equiManage/message/list.do",
    CON_MESSAGE_INFO: "/api/backoffice/sub/equiManage/message",
    CON_MESSAGE_UPDATE: "/api/backoffice/sub/equiManage/message/update.do",
    CON_MESSAGE_DELETE_BULK: "/api/backoffice/sub/equiManage/message/deleteBulk.do",

    //방송 기념일 관리
    BROD_ANNIVER_LIST: "/api/backoffice/sub/brodManage/anniverList.do",
    BROD_ANNIVER_DETAIL: "/api/backoffice/sub/brodManage/anniverDetail.do",
    BROD_ANNIVER_UPDATE: "/api/backoffice/sub/brodManage/anniverUpdate.do",
    BROD_ANNIVER_DELETE: "/api/backoffice/sub/brodManage/anniver",

    //방송 배포 스케줄 관리
    BROD_SCHEDULE_LEFT_LIST: "/api/backoffice/sub/brodManage/playSchedule/list.do",
    BROD_SCHEDULE_STATUS_LIST: "/api/backoffice/sub/brodManage/playSchedule/statusList.do",
    BROD_SCHEDULE_RIGHT_LIST: "/api/backoffice/sub/brodManage/playSchedule/right.do",
    BROD_SCHEDULE_CENTER_CNT: "/api/backoffice/sub/brodManage/playSchedule/centerCnt.do",
    BROD_SCHEDULE_RIGHT_UPDATE: "/api/backoffice/sub/brodManage/playSchedule/rightUpdate.do",

    //방송 콘텐츠(편성) 관리
    BROD_CONTENT_LIST: "/api/backoffice/sub/brodManage/content/list.do",
    BROD_CONTENT_RIGHT: "/api/backoffice/sub/brodManage/content/right.do",
    BROD_CONTENT_RIGHT_DELETE: "/api/backoffice/sub/brodManage/content/rightDelete.do",
    BROD_CONTENT_VIEW: "/api/backoffice/sub/brodManage/content",
    BROD_CONTENT_FORM_DATA: "/api/backoffice/sub/brodManage/content/formData.do",
    BROD_CONTENT_UPDATE: "/api/backoffice/sub/brodManage/content/update.do",
    BROD_CONTENT_SCHEDULE_CONFIRM: "/api/backoffice/sub/brodManage/content/scheduleConfirm.do",
    BROD_CONTENT_TIME_LIST: "/api/backoffice/sub/brodManage/content/timeList.do",
    BROD_CONTENT_COPY_COUNT: "/api/backoffice/sub/brodManage/content/copyCount.do",
    BROD_CONTENT_COPY_INSERT: "/api/backoffice/sub/brodManage/content/copyInsert.do",
    BROD_CONTENT_DELETE_BULK: "/api/backoffice/sub/brodManage/content/deleteBulk.do",
    BROD_CONTENT_COMBO: "/api/backoffice/sub/brodManage/content/combo.do",
    BROD_CONTENT_REG_POPUP_DATA: "/api/backoffice/sub/brodManage/content/copyPopupData.do",
    // 스케줄 음원 관리(brodContentPlayList.jsp) — 음원 파일 하나를 여러 방송(brodCode)에 한 번에 등록
    BROD_CONTENT_DETAIL_CENTER_UPDATE: "/api/backoffice/sub/brodManage/content/detailCenterUpdate.do",
    BROD_CONTENT_ANN_DETAIL_CENTER_UPDATE: "/api/backoffice/sub/brodManage/content/annDetailCenterUpdate.do",

    //방송 콘텐츠 편성(시간대별 음원 배치) 관리 — brodContentView.jsp 상세 화면에서 사용
    BROD_CONTENT_DETAIL_FORM: "/api/backoffice/sub/brodManage/contentDetail/form.do",
    BROD_CONTENT_DETAIL_UPDATE: "/api/backoffice/sub/brodManage/contentDetail/update.do",
    BROD_CONTENT_DETAIL_INFO: "/api/backoffice/sub/brodManage/contentDetail",
    BROD_CONTENT_DETAIL_COPY_COMBO: "/api/backoffice/sub/brodManage/contentDetail/copy",
    BROD_CONTENT_DETAIL_COPY_INSERT: "/api/backoffice/sub/brodManage/contentDetail/copyInsert.do",
    BROD_CONTENT_DETAIL_FILE_SEARCH: "/api/backoffice/sub/brodManage/contentDetail/fileSearch.do",
    BROD_CONTENT_DETAIL_TIME_CHECK: "/api/backoffice/sub/brodManage/contentDetail/timeCheck.do",

    //기초 방송(템플릿) 관리
    BASIC_BROD_LIST: "/api/backoffice/sub/brodManage/basic/list.do",
    BASIC_BROD_DELETE: "/api/backoffice/sub/brodManage/basic/delete.do",
    BASIC_BROD_UPDATE: "/api/backoffice/sub/brodManage/basic/update.do",
    BASIC_BROD_COMBO: "/api/backoffice/sub/brodManage/basic/combo.do",
    BASIC_BROD_DETAIL: "/api/backoffice/sub/brodManage/basic/detail.do",
    BASIC_BROD_FILE_LIST: "/api/backoffice/sub/brodManage/basic/file-list.do",
    BASIC_BROD_FILE_UPDATE: "/api/backoffice/sub/brodManage/basic/file-update",
    BASIC_BROD_FILE_ALL_UPDATE: "/api/backoffice/sub/brodManage/basic/file-check-update",
    BASIC_BROD_FILE_ALL_DEL: "/api/backoffice/sub/brodManage/basic/file-all-del",
    BASIC_BROD_CENTER_LIST: "/api/backoffice/sub/brodManage/basic/center-list",
    BASIC_BROD_CENTER_UPDATE: "/api/backoffice/sub/brodManage/basic/center-update",
    BASIC_BROD_TIME_DETAIL: "/api/backoffice/sub/brodManage/basic/timeDetail.do",
    BASIC_BROD_TIME_UPDATE: "/api/backoffice/sub/brodManage/basic/timeUpdate.do",
    BASIC_BROD_TIME_DELETE: "/api/backoffice/sub/brodManage/basic/timeDelete.do",
    BASIC_BROD_TIME_FILE_LIST: "/api/backoffice/sub/brodManage/basic/timeFileList.do",
    BASIC_BROD_TIME_FILE_UPDATE: "/api/backoffice/sub/brodManage/basic/timeFileUpdate.do",
    BASIC_BROD_PLAY_INFO: "/api/backoffice/sub/brodManage/basic/playInfo.do",

    //부서 정보
    /*
    PART_LIST: "/api/backoffice/hr/part/partList.do",
    PART_UPDATE: "/api/backoffice/hr/part/partUpdate.do",
    PART_DELETE:"/api/backoffice/hr/part",
    PART_COMBO:"/api/backoffice/hr/part/partCombo.do",
    PART_PARENT_COMBO:"/api/backoffice/hr/part/parentPartCombo.do",
    */
    //부서 관리(LETTNAUTHORGROUPINFO, 레거시 "부서관리"/selectGroupLst.do)
    GROUP_LIST: "/api/backoffice/sub/basicManage/group/list.do",
    GROUP_COMBO: "/api/backoffice/sub/basicManage/group/combo.do",
    GROUP_INFO: "/api/backoffice/sub/basicManage/group",
    GROUP_UPDATE: "/api/backoffice/sub/basicManage/group/update.do",

    //벤더사 정보
    VENDOR_LIST:"/api/backoffice/infra/vendor/vendorListAjax.do",
    VENDOR_ID_CHECK : "/api/backoffice/infra/vendor/idCheck",
    VENDOR_INFO : "/api/backoffice/infra/vendor",
    VENDOR_UPDATE: "/api/backoffice/infra/vendor/updateVendor.do",

    //거래처 고객 정보 (백엔드 CustomerInfoManageController 매핑 경로 변경: /api/backoffice/user → /api/user)
    CUSTOMER_LIST : "/api/user/userList.do",
    CUSTOMER_ID_CHECK : "/api/user/idCheck",
    CUSTOMER_INFO : "/api/user",
    CUSTOMER_UPDATE : "/api/user/userUpdate.do",
    CUSTOMER_SIGNUP : "/api/user/signUp.do",

    //판매사몰 고객 로그인 (JWT) — POST, 비밀번호 검증(cusomterId/comCode/userPassword)
    CUSTOMER_LOGIN : "/api/user/cus/login.do",
    CUSTOMER_REFRESH : "/api/user/cus/actionRefreshToken.do",
    CUSTOMER_LOGOUT : "/api/user/cus/actionCustomerLogoutJWT.do",
    CUSTOMER_UPDATE_SELF : "/api/user/cus/updateCustomer.do", //판매사몰 고객 본인 정보수정(관리자 인증 불필요, CUSTOMER_UPDATE와 별개)

    //공급사 계약 정보
    VENDOR_CONTRACT_LIST : "/api/backoffice/infra/vendor/contract/contractListAjax.do",
    VENDOR_CONTRACT_ID_CHECK : "/api/backoffice/infra/vendor/contract/idCheck",
    VENDOR_CONTRACT_INFO : "/api/backoffice/infra/vendor/contract",
    VENDOR_CONTRACT_UPDATE : "/api/backoffice/infra/vendor/contract/updateVendorContract.do",
    VENDOR_CONTRACT_PROCESS_UPDATE : "/api/backoffice/infra/vendor/contract/updateVendorContractProcess.do",

  
    //hotel 정보
    HOTEL_LIST: "/api/backoffice/infra/hotel/hotelListAjax.do",
    HOTEL_UPDATE: "/api/backoffice/infra/hotel/updateHotel.do",
    HOTEL_ID_CHECK: "/api/backoffice/infra/hotel/idCheck",
    HOTEL_INFO: "/api/backoffice/infra/hotel",
    HOTEL_COMBO: "/api/backoffice/infra/hotel/hotelCombo.do",
    HOTEL_MANAGER_UPDATE : "/api/backoffice/infra/hotel/updateHotelManager.do",
    HOTEL_MANAGER_DELETE : "/api/backoffice/infra/hotel/manager",
    HOTEL_STATE_CHANGE: "/api/backoffice/infra/hotel/StateChange",
    //객실 정보
    ROOM_LIST: "/api/backoffice/infra/room/roomListAjax.do",
    ROOM_UPDATE: "/api/backoffice/infra/room/updateRoom.do",
    ROOM_INFO: "/api/backoffice/infra/room",
    ROOM_STATE_CHANGE: "/api/backoffice/infra/room/StateChange",
    ROOM_COMBO: "/api/backoffice/infra/room/roomCombo.do",
    ROOM_EXCEL_DOWNLOAD: "/api/backoffice/infra/room/roomExcel.do",


    //상품 옵션 정보
    OPTION_LIST: "/api/backoffice/infra/product/option/optionListAjax.do",
    OPTION_UPDATE: "/api/backoffice/infra/product/option/updateOption.do",
    OPTION_INFO: "/api/backoffice/infra/product/option",
    OPTION_COMBO : "/api/backoffice/infra/product/hotel/optionCombo.do",

    //상품 Group 정보
    PRODUCT_GROUP_LIST: "/api/backoffice/infra/product/product/productListAjax.do",
    PRODUCT_GROUP_UPDATE:"/api/backoffice/infra/product/product/updateProduct.do",
    PRODUCT_GROUP_INFO:"/api/backoffice/infra/product/product",
    PRODUCT_GROUP_STATE_UPDATE: "/api/backoffice/infra/product/product/updateProductState.do",
    PRODUCT_GROUP_EXCEL_UPDATE: "/api/backoffice/infra/product/product/updateExcel.do",
    PRODUCT_GROUP_SMS_SEND: "/api/backoffice/infra/product/product/sendProductSms.do",
    PRODUCT_GROUP_COMBO: "/api/backoffice/infra/product/product/productCombo.do",
    //상품 정보 
    PRODUCT_LIST: "/api/backoffice/infra/product/detail/detailListAjax.do",
    PRODUCT_UPDATE: "/api/backoffice/infra/product/detail/updateDetail.do",
    PRODUCT_INFO: "/api/backoffice/infra/product/detail",
    PRODUCT_SELL_ID_CHECK : "/api/backoffice/infra/product/detail/idCheck",
    PRODUCT_COPY: "/api/backoffice/infra/product/detail/updateDetailCopy.do",
    PRODUCT_COMBO : "/api/backoffice/infra/product/detail/detailCombo.do",
    //쿠폰 정보 
    COUPON_LIST: "/api/backoffice/infra/product/detail/couponList.do",
    COUPON_UPDATE:"/api/backoffice/infra/product/detail/updateCoupon.do",
    //상품별 호텔 정보
    PRODUCT_HOTEL_LIST:"/api/backoffice/infra/product/hotel/hotelListAjax.do",
    PRODUCT_HOTEL_UPDATE: "/api/backoffice/infra/product/hotel/updateProductHotel.do",
    PRODUCT_HOTEL_INFO: "/api/backoffice/infra/product/hotel",
    PRODUCT_HOTEL_CANCEL_SETTINGS : "/api/backoffice/infra/product/cancel/cancelListAjax.do",
    //상품별 방정보
    PRODUCT_ROOM_LIST : "/api/backoffice/infra/product/hotel/roomListAjax.do",
    PRODUCT_ROOM_UPDATE:"/api/backoffice/infra/product/hotel/updateProductRoom.do",
    PRODUCT_ROOM_INFO:"/api/backoffice/infra/product/hotel/room",
    PRODUCT_ROOM_COMBO: "/api/backoffice/infra/product/hotel/roomCombo.do",
    //상품 옵션
    PRODUCT_OPTION_UPDTE : "/api/backoffice/infra/product/hotel/updateProductOption.do",
    PRODUCT_OPTION_LIST : "/api/backoffice/infra/product/hotel/optionListAjax.do",
    PRODUCT_OPTION_INFO: "/api/backoffice/infra/product/hotel/option",

    //상품 취소일자 정보
    PRODUCT_CANCEL_DAY_LIST : "/api/backoffice/infra/product/cancel/cancelListAjax.do",
    PRODUCT_CANCEL_DAY_UPDATE : "/api/backoffice/infra/product/cancel/updateCancelDay.do",
    PRODUCT_CANCEL_DAY_INFO : "/api/backoffice/infra/product/cancel",

    //메세지
    PRODUCT_MSG_LIST : "/api/backoffice/infra/product/msg/msgListAjax.do",
    PRODUCT_MSG_UPDATE : "/api/backoffice/infra/product/msg/updateProductMsg.do",
    PRODUCT_MSG_INFO : "/api/backoffice/infra/product/msg",
    PRODUCT_MSG_SEND : "/api/backoffice/infra/product/product/sendProductSms.do",
    PRODUCT_MSG_COMBO : "/api/backoffice/infra/product/msg/msgCombo.do",

    //주문 정보
    ORDER_LIST:      '/api/backoffice/order/orderNewListAjax.do',
    ORDER_SUMMARY:   '/api/backoffice/order/orderNewListSummary.do',
    ORDER_SMS_SEND:  '/api/backoffice/order/smsNewSend.do',
    ORDER_SMS_CHANGE:'/api/backoffice/order/smsSendChange.do',
    ORDER_SMS_CHECK: '/api/backoffice/order/sms/optionSmsCheck.do',
    ORDER_BASIC_UPDATE : "/api/backoffice/order/updateOrderBasicChange.do",

    ORDER_EXCEL_UPLOAD : "/api/backoffice/order/new/updateOrderExcel.do",
    ORDER_EXCEL_DOWNLOAD : "/api/backoffice/order/new/orderExcelDownload.do",
    ORDER_SMS_SEND_LIST : "/api/backoffice/order/smsNewSendList.do",

    ORDER_CS_UPDATE : "/api/backoffice/order/updateOrderCs.do",
    ORDER_CS_LIST : "/api/backoffice/order/orderListCsAjax.do",
    ORDER_CS_INFO : "/api/backoffice/order/cs",
    ORDER_CS_DELETE : "/api/backoffice/order/csSeq",
    
    //쿠폰 정보
    ORDER_COUPON_UPDATE : "/api/backoffice/order/updateCouponStateChange.do",

    //ROOM STATE 정보
    ROOM_CALENDER_STATE_LIST :"/api/backoffice/infra/product/hotel/roomStateCalender.do",
    ROOM_CALENDER_STATE_DAY : "/api/backoffice/infra/product/hotel/roomStateCalenderDay.do",
    ROOM_CALENDER_STATE_UPDATE : "/api/backoffice/infra/product/hotel/updateProductRoomUdate.do",
    



    //Dashboard 정보
    DASHBOARD_LIST : "/api/backoffice/dashboard/productDashListAjax.do",
    DASHBOARD_RES_LIST : "/api/backoffice/dashboard/reservationDashListAjax.do",
    

    //추가 정산 정보
    //EXTRA_DATA_LIST : "/api/backoffice/settle/extraData",
    EXTRA_DATA_UPDATE : "/api/backoffice/settle/extraData/updateSettlement.do",
    EXTRA_DATA_INFO : "/api/backoffice/settle/extraData",
    
    

    // 메세지 리스트
    KAKAO_MSG_RESULT_LIST: "/api/backoffice/infra/product/msg/kakaoMsgResultListAjax.do",
    // 상품 그룹 상세정보
    PRODUCT_CODE_INFO: '/api/backoffice/infra/product/product',  
    // 사용 예: `${PRODUCT_INFO}/${productCode}.do`
    // 개별상품(판매) 상세정보
    SELL_DETAIL: '/api/backoffice/infra/product/detail',
    // 사용 예: `${SELL_DETAIL}/${sellCode}.do`
    // 객실 목록 조회
    ROOM_LIST_AJAX: '/api/backoffice/infra/product/hotel/roomListAjax.do',
    // 객실 수량 현황 조회 (roomGrid)
    ROOM_STATE: '/api/backoffice/infra/product/hotel/roomState.do',
    // 객실 추가요금 현황 조회 (mainGrid)
    ROOM_STATE_LIST: '/api/backoffice/infra/product/hotel/roomStateList.do',
    // 객실 수량 일괄 저장
    UPDATE_ROOM_STATE: '/api/backoffice/infra/product/hotel/updateProductRoomUdate.do',
    // 객실 추가요금 일괄 저장
    UPDATE_ROOM_PRICE: '/api/backoffice/infra/product/hotel/updateProductChangePrice.do',
    // ROOM 엑셀 업로드
    EXCEL_ROOM_UPDATE: '/api/backoffice/infra/product/product/updateExcelRoom.do',
    // ROOM 가격 엑셀 업로드
    EXCEL_ROOM_PRICE_UPDATE: '/api/backoffice/infra/product/product/updateExcelRoomPrice.do',
        
    LOGIN_REFRESH: "/uat/uia/actionRefreshToken.do", //로그인 토큰 리프레쉬
    LOGIN_PROCESS: "/api/loginJwt.do", //로그인 프로세스
    LOGIN_VENDOR_PROCESS: "/api/loginVendorJwt.do", //협력사(파트너) 로그인 프로세스
    LOGOUT_PROCESS: "/uat/uia/actionLogoutJWT.do",
    AUTH_CHECK: "/jwtAuthAPI",
    LEFT_MENU: "/api/backoffice/sys/menu/menuNoLeft.do",


    //아이디 패스워드 찾기
    ID_FIND_PROCESS: "/api/backoffice/uat/uia/idFind.do",
    PWD_FIND_PROCESS: "/api/backoffice/uat/uia/pwFind.do",
    PW_UPDATE: "/api/backoffice/uat/uia/pwUpdate.do",


    IMG_URL: "http://27.96.130.69:7001/upload/", //이미지 URL

    //공통 에디터 이미지 업로드
    EDITOR_IMAGE_UPLOAD: "/api/backoffice/common/upload/editorImage.do",

}
export default URL;