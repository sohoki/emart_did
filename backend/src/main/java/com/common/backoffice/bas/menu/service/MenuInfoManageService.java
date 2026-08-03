package com.common.backoffice.bas.menu.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.bas.menu.service.repository.MenuInfoRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.common.backoffice.bas.menu.mapper.MenuInfoManageMapper;
import com.common.backoffice.bas.menu.modals.MenuInfo;
import com.common.backoffice.bas.menu.modals.dto.MenuInfoRequestDto;
import com.common.backoffice.bas.menu.modals.dto.MenuOrderReqDto;
import com.common.backoffice.bas.program.mapper.ProgrameChangeManageMapper;
import com.common.backoffice.bas.program.mapper.ProgrmInfoManageMapper;
import com.common.backoffice.bas.program.modals.ProgrmInfo;
import com.common.backoffice.bas.uni.service.UtilInfoService;

import egovframework.com.cmm.service.Globals;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Transactional(value = "txManager", readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class MenuInfoManageService {


	private final MenuInfoManageMapper menuMapper;
	private final ProgrmInfoManageMapper progrmMapper;
	private final ProgrameChangeManageMapper programChnageMapper;
    private final MenuInfoRepository menuRepository;
	/*
	@Resource(name = "excelZipService")
    private EgovExcelService excelZipService;
	*/
	
	public Map<String, Object> selectMenuManage(String menuNo) throws Exception {
		// 메뉴목록을 조회
		return menuMapper.selectMenuManage_D(menuNo);
	}
	public List<Map<String, Object>> selectMenuManageList(Map<String, Object> vo) throws Exception {
		return  menuMapper.selectMenuManageList_D(vo);
	}
	
	public List<ProgrmInfo> selectSystemProgramInfo(String roleId) throws Exception {
		return  menuMapper.selectSystemProgramInfo(roleId);
	}
	public List<Map<String, Object>> selectSystemMenuInfo(String roleId) throws Exception {
		return  menuMapper.selectSystemMenuInfo(roleId);
	}

	
	public int selectMenuNoByPk(Map<String, Object> params) throws Exception {
		return menuMapper.selectMenuNoByPk(params);
	}

	
	public int selectUpperMenuNoByPk(Map<String, Object> params) throws Exception {
		return menuMapper.selectUpperMenuNoByPk(params);
	}
	
	

	@Transactional(readOnly = false)
	public int updateMenuManage(MenuInfoRequestDto menuInfo) throws Exception {
		return  menuInfo.getMode().equals(Globals.SAVE_MODE_INSERT) ? 
				menuMapper.insertMenuManage_S(menuInfo) : menuMapper.updateMenuManage_S(menuInfo);
	}

	@Transactional(readOnly = false)
	public int updateMenuOrder(List<MenuOrderReqDto> list) throws Exception {
		return menuMapper.updateMenuOrder(list);
	}

	@Transactional(readOnly = false)
	public int deleteMenuManage(String menuNo) throws Exception {
		return menuMapper.deleteMenuManage_S(menuNo);
	}

	/**
	 * 화면에 조회된 메뉴 목록 정보를 데이터베이스에서 삭제
	 * @param checkedMenuNoForDel String
	 * @exception Exception
	 */
	//@Transactional(rollbackFro = {RuntimeException.class})
	
	@Transactional(readOnly = false)
	public int deleteMenuManageList(String checkedMenuNoForDel) throws Exception {
		int ret = 0;
		try {
			List<String> menuNoList = UtilInfoService.dotToList(checkedMenuNoForDel);
			//menuMapper.deleteMenuManage_L(menuNoList)
			int delCnt = menuNoList.size();
			int realDelCnt = 0;
			Collections.sort(menuNoList, Collections.reverseOrder());
			MenuInfo  menuinfo = new MenuInfo();
			
			for (String menuNo : menuNoList) {
				menuinfo.setMenuNo(menuNo);
				menuMapper.deleteMenuNo(menuinfo);
				realDelCnt +=  (menuinfo.getCnt() == 0)? 1 : 0;
			}
			
			ret = delCnt != realDelCnt ? -1 : 1;
			if (delCnt != realDelCnt) {
				throw new RuntimeException("Exception for rollback");
			}
		}catch (RuntimeException e1) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			log.error("deleteMenuManageList RuntimeException error:" + e1.toString());
			ret = -1 ;
		}catch(Exception e) {
			log.error("deleteMenuManageList Exception error:" + e.toString());
			ret = -1 ;
		}
		log.error("ret:" + ret);
		return ret;
		
	}

	
	public List<Map<String, Object>> selectMenuList() throws Exception {
		return menuMapper.selectMenuListT_D();
	}

	public List<Map<String, Object>> selectMainMenuHead(String empNo) throws Exception {
		return menuMapper.selectMainMenuHead(empNo);
	}

    public boolean existsByMenuNm(String menuNo) {
        BigDecimal bdMenuNo = new BigDecimal(menuNo);
        return menuRepository.existsByMenuNo( bdMenuNo);
    }
	
	@Cacheable(value = "findLeftMenu", key = "#empNo", condition = "#empNo != null") // managerId 대신 실제 파라미터명인 empNo 사용
	@Transactional(readOnly = true)
	public List<Map<String, Object>> selectMainMenuLeft(String empNo, String roleId, String url) throws Exception {
		try {
			return menuMapper.selectMainMenuLeft(roleId, url);
		}catch(Exception e) {
			log.error("selectMainMenuLeft error:" + e.toString());
			return menuMapper.selectMainMenuLeft(roleId, url);
		}
		
	}

	/**
	 * MainMenu Head MenuURL 조회
	 * @param  iMenuNo  int
	 * @param  sUniqId  String
	 * @return String
	 * @exception Exception
	 */
	
	public String selectLastMenuURL(int iMenuNo, String sUniqId) throws Exception {
		
		return menuMapper.selectLastMenuURL( String.valueOf( selectLastMenuNo(iMenuNo, sUniqId) ) );
	}
	
	/**
	 * MainMenu Head Menu MenuNo 조회
	 * @param  iMenuNo  int
	 * @param  sUniqId  String
	 * @return String
	 * @exception Exception
	 */
	private int selectLastMenuNo(int iMenuNo, String sUniqId) throws Exception {
		int chkMenuNo = iMenuNo;
		int cntMenuNo = 0;
		for (; chkMenuNo > -1;) {
			chkMenuNo = selectLastMenuNoChk(chkMenuNo, sUniqId);
			if (chkMenuNo > 0) {
				cntMenuNo = chkMenuNo;
			}
		}
		return cntMenuNo;
	}

	/**
	 * MainMenu Head Menu Last MenuNo 조회
	 * @param  iMenuNo  int
	 * @param  sUniqId  String
	 * @return String
	 * @exception Exception
	 */
	private int selectLastMenuNoChk(int iMenuNo, String sUniqId) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		
		params.put("menuNo", iMenuNo);
		params.put("empNo", sUniqId);
		
		int chkMenuNo = 0;
		int cntMenuNo = 0;
		cntMenuNo = menuMapper.selectLastMenuNo(params);
		if (cntMenuNo > 0) {
			chkMenuNo = menuMapper.selectLastMenuNo(params);
		} else {
			chkMenuNo = -1;
		}
		return chkMenuNo;
	}
	/*### 일괄처리 프로세스 ###*/
	/**
	 * 메뉴일괄초기화 프로세스 메뉴목록테이블, 프로그램 목록테이블 전체 삭제
	 * @return boolean
	 * @exception Exception
	 */
	
	public boolean menuBndeAllDelete() throws Exception {
		if (deleteAllMenuList() < 1) {
			return false;
		} // 메뉴정보 테이블
		/*
		if (deleteAllProgrm() < 1) {
			return false;
		} 
		*/
		// 프로그램목록 테이블
		return true;
	}
	/**
	 * 메뉴정보 전체데이타 초기화
	 * @return boolean
	 * @exception Exception
	 */
	private int deleteAllMenuList() throws Exception {
		return menuMapper.deleteAllMenuList();
	}
	/**
	 * 프로그램 정보 전체데이타 초기화
	 * @return boolean
	 * @exception Exception
	 */
	private int deleteAllProgrm() throws Exception {
		return progrmMapper.deleteAllProgrm();
	}
	/**
	 * 메뉴일괄등록 프로세스
	 * @param  vo MenuManageVO
	 * @param  inputStream InputStream
	 * @exception Exception
	 */
	
	public String menuBndeRegist(MenuInfo vo, InputStream inputStream) throws Exception {
		String message = bndeRegist(inputStream);
		String sMessage = null;

		switch (Integer.parseInt(message)) {
			case 99:
				log.debug("프로그램목록/메뉴정보테이블 데이타 존재오류 - 초기화 하신 후 다시 처리하세요.");
				sMessage = "프로그램목록/메뉴정보테이블 데이타 존재오류 - 초기화 하신 후 다시 처리하세요.";
				break;
			case 90:
				log.debug("파일존재하지 않음.");
				sMessage = "파일존재하지 않음.";
				break;
			case 91:
				log.debug("프로그램시트의 cell 갯수 오류.");
				sMessage = "프로그램시트의 cell 갯수 오류.";
				break;
			case 92:
				log.debug("메뉴정보시트의 cell 갯수 오류.");
				sMessage = "메뉴정보시트의 cell 갯수 오류.";
				break;
			case 93:
				log.debug("엑셀 시트갯수 오류.");
				sMessage = "엑셀 시트갯수 오류.";
				break;
			case 95:
				log.debug("메뉴정보 입력시 에러.");
				sMessage = "메뉴정보 입력시 에러.";
				break;
			case 96:
				log.debug("프로그램목록입력시 에러.");
				sMessage = "프로그램목록입력시 에러.";
				break;
			default:
				log.debug("일괄배치처리 완료.");
				sMessage = "일괄배치처리 완료.";
				break;
		}
		log.debug(message);
		return sMessage;
	}
	/**
	 * 메뉴목록_프로그램목록 일괄생성
	 * @param  inputStream InputStream
	 * @return  String
	 * @exception Exception
	 */
	private String bndeRegist(InputStream inputStream) throws Exception {
		boolean success = false;
		String requestValue = null;
		//char FILE_SEPARATOR     = File.separatorChar;
		int progrmSheetRowCnt = 0;
		int menuSheetRowCnt = 0;
		//String xlsFile = null;
		try {
			/*
			오류 메세지 정보
			message = "99";	//프로그램목록테이블 데이타 존재오류.
			message = "99";	//메뉴정보테이블 데이타 존재오류.
			message = "90";	//파일존재하지 않음.
			message = "91";	//프로그램시트의 cell 갯수 오류
			message = "92";	//메뉴정보시트의 cell 갯수 오류
			message = "93";	//엑셀 시트갯수 오류
			message = "95";	//메뉴정보 입력시 에러
			message = "96";	//프로그램목록입력시 에러
			message = "0";	//일괄배치처리 완료
			*/

			if (progrmMapper.selectProgrmListTotCnt() > 1) {
				return requestValue = "99";
			} //프로그램목록테이블 데이타 존재오류.
			if (menuMapper.selectMenuListTotCnt() > 1) {
				return requestValue = "99";
			} //메뉴정보테이블 데이타 존재오류.
			//Workbook hssfWB = excelZipService.loadWorkbook(inputStream);
			
			//log.debug("hssfWB:::::"+hssfWB);
			// 엑셀 파일 시트 갯수 확인 sheet = 2  첫번째시트 = 프로그램목록  두번째시트 = 메뉴목록
			/*
			if (hssfWB.getNumberOfSheets() == 2) {
				Sheet progrmSheet = hssfWB.getSheetAt(0); //프로그램목록 시트 가져오기
				Sheet menuSheet = hssfWB.getSheetAt(1); //메뉴정보 시트 가져오기
				Row progrmRow = progrmSheet.getRow(1); //프로그램 row 가져오기
				Row menuRow = menuSheet.getRow(1); //메뉴정보 row 가져오기
				progrmSheetRowCnt = progrmRow.getPhysicalNumberOfCells(); //프로그램 cell Cnt
				menuSheetRowCnt = menuRow.getPhysicalNumberOfCells(); //메뉴정보 cell Cnt

				// 프로그램 시트 파일 데이타 검증 cell = 5개
				if (progrmSheetRowCnt != 5) {
					return requestValue = "91"; //프로그램시트의 cell 갯수 오류
				}

				// 메뉴목록 시트 파일 데이타 검증  cell = 8개
				if (menuSheetRowCnt != 8) {
					return requestValue = "92"; //메뉴정보시트의 cell 갯수 오류
				}

				
				success = progrmRegist(progrmSheet);
				if (success) {
					success = menuRegist(menuSheet);
					if (success) {
						return requestValue = "0"; // 일괄배치처리 완료
					} else {
						deleteAllProgrmDtls();
						deleteAllProgrm();
						deleteAllMenuList();
						return requestValue = "95"; // 메뉴정보 입력시 에러
					}
				} else {
					deleteAllProgrmDtls();
					deleteAllProgrm();
					return requestValue = "96"; // 프로그램목록입력시 에러
				}
			} else {
				return requestValue = "93"; // 엑셀 시트갯수 오류
			}
			*/
		}catch (RuntimeException e1) {
			log.debug("{}", e1);
			requestValue = "99";
		} catch (Exception e) {
			log.debug("{}", e);

			requestValue = "99";
		}
		return requestValue;
	}
	/**
	 * 프로그램목록 일괄등록
	 * @param  progrmSheet HSSFSheet
	 * @return  boolean
	 * @exception Exception

	private boolean progrmRegist(Sheet progrmSheet) throws Exception {
		int count = 0;
		boolean success = false;
		try {
			int rows = progrmSheet.getPhysicalNumberOfRows(); //행 갯수 가져오기
			for (int j = 1; j < rows; j++) { //row 루프
				ProgrmInfo vo = new ProgrmInfo();
				Row row = progrmSheet.getRow(j); //row 가져오기
				if (row != null) {
					//int cells=row.getPhysicalNumberOfCells(); //cell 갯수 가져오기

					Cell cell = null;
					cell = row.getCell(0); //프로그램명
					if (cell != null) {
						vo.setProgrmFileNm("" + cell.getStringCellValue());
					}
					cell = row.getCell(1); //프로그램한글명
					if (cell != null) {
						vo.setProgrmKoreannm("" + cell.getStringCellValue());
					}
					cell = row.getCell(2); //프로그램저장경로
					if (cell != null) {
						vo.setProgrmStrePath("" + cell.getStringCellValue());
					}
					cell = row.getCell(3); //프로그램 URL
					if (cell != null) {
						vo.setUrl("" + cell.getStringCellValue());
					}
					cell = row.getCell(4); //프로그램설명
					if (cell != null) {
						vo.setProgrmDc("" + cell.getStringCellValue());
					}
				}
				if (insertProgrm(vo) > 0) {
					count++;
				}
			}
			if (count == rows - 1) {
				success = true;
			} else {
				success = false;
			}
		}catch (RuntimeException e1) {
			log.debug("{}", e1);
			success = false;
		}catch (Exception e) {
			log.debug("{}", e);

			success = false;
		}
		return success;
	}
     */
	/**
	 * 메뉴정보 일괄등록
	 * @param menuSheet HSSFSheet
	 * @return boolean
	 * @exception Exception
	
	private boolean menuRegist(Sheet menuSheet) throws Exception {
		boolean success = false;
		int count = 0;
		try {
			int rows = menuSheet.getPhysicalNumberOfRows(); //행 갯수 가져오기
			for (int j = 1; j < rows; j++) { //row 루프
				MenuInfoRequestDto vo = new MenuInfoRequestDto();
				vo.setMode("Ins");
				Row row = menuSheet.getRow(j); //row 가져오기
				if (row != null) {
					//int cells=row.getPhysicalNumberOfCells(); //cell 갯수 가져오기
					Cell cell = null;
					cell = row.getCell(0); //메뉴번호
					if (cell != null) {
						Double doubleCell = new Double(cell.getNumericCellValue());
						vo.setMenuNo("" + doubleCell.longValue());
					}
					cell = row.getCell(1); //메뉴순서
					if (cell != null) {
						Double doubleCell = new Double(cell.getNumericCellValue());
						vo.setMenuOrdr("" + doubleCell.longValue());
					}
					cell = row.getCell(2); //메뉴명
					if (cell != null) {
						vo.setMenuNm("" + cell.getStringCellValue());
					}
					cell = row.getCell(3); //상위메뉴번호
					if (cell != null) {
						Double doubleCell = new Double(cell.getNumericCellValue());
						vo.setUpperMenuNo("" + doubleCell.longValue());
					}
					cell = row.getCell(4); //메뉴설명
					if (cell != null) {
						vo.setMenuDc("" + cell.getStringCellValue());
					}
					cell = row.getCell(5); //관련이미지경로
					if (cell != null) {
						vo.setRelateImagePath("" + cell.getStringCellValue());
					}
					cell = row.getCell(6); //관련이미지명
					if (cell != null) {
						vo.setRelateImageNm("" + cell.getStringCellValue());
					}
					cell = row.getCell(7); //프로그램파일명
					if (cell != null) {
						vo.setProgrmFileNm("" + cell.getStringCellValue());
					}
				}
				if (insertMenuManageBind(vo) > 0) {
					count++;
				}
			}
			if (count == rows - 1) {
				success = true;
			} else {
				
			}
		} catch (Exception e) {
			log.debug("{}", e);
			
			success = false;

		}
		return success;
	}
	 */
	/**
	 * 프로그램 정보를 등록
	 * @param  vo ProgrmManageVO
	 * @return boolean
	 * @exception Exception
	 */
	private int insertProgrm(ProgrmInfo vo) throws Exception {
		return progrmMapper.insertProgrmInfo(vo);
	}

	/**
	 * 메뉴정보를 일괄 등록
	 * @param  vo MenuManageVO
	 * @return boolean
	 * @exception Exception
	 */
	private int insertMenuManageBind(MenuInfoRequestDto vo) throws Exception {
		return menuMapper.insertMenuManage_S(vo);
	}

	
	/**
	 * 프로그램변경내역 정보 전체데이타 초기화
	 * @return boolean
	 * @exception Exception
	 */
	private int deleteAllProgrmDtls() throws Exception {
		return programChnageMapper.deleteAllProgrmDtls();
	}
}
