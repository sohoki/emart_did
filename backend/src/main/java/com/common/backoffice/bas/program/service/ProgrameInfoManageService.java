package com.common.backoffice.bas.program.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.common.backoffice.bas.program.service.repository.ComtnprogrmlistRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.backoffice.bas.program.mapper.ProgrmInfoManageMapper;
import com.common.backoffice.bas.program.modals.ProgrmInfo;
import com.common.backoffice.bas.program.modals.dto.ProgrmInfoDto;
import com.common.backoffice.bas.uni.mapper.UniUtilManageMapper;
import com.common.backoffice.bas.uni.service.UtilInfoService;

import egovframework.com.cmm.service.Globals;
import lombok.RequiredArgsConstructor;

@Transactional(value = "txManager", readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class ProgrameInfoManageService {


	private final ProgrmInfoManageMapper progrmMapper;
    private final ComtnprogrmlistRepository repository;

	
	public List<ProgrmInfoDto> selectProgrmInfoList(Map<String, Object> params) throws Exception {
		return progrmMapper.selectProgrmInfoList(params);
	}

	
	public Optional<ProgrmInfoDto> selectProgrmInfoDetail(String progrmFileNm) throws Exception {
		return progrmMapper.selectProgrmInfoDetail(progrmFileNm);
	}
    public int isPresentPrograme(String progrmFileNm) {
        int ret = repository.existsById(progrmFileNm) == true ? 0 : 1;
        return ret;
    }
	/*
	@Transactional(readOnly = false)
	public int insertProgrmInfo(ProgrmInfoDto progrmInfoDto) throws Exception {
		ProgrmInfo progrmInfo = ProgrmInfo.builder()
				.progrmFileNm(progrmInfoDto.getProgrmFileNm())
				.progrmStrePath(progrmInfoDto.getProgrmStrePath())
				.progrmKoreannm(progrmInfoDto.getProgrmKoreannm())
				.progrmDc(progrmInfoDto.getProgrmDc())
				.url(progrmInfoDto.getUrl())
				.testUrl(progrmInfoDto.getTestUrl())
				.build();
		return (uniMapper.selectIdDoubleCheckString("PROGRM_FILE_NM", "COMTNPROGRMLIST", "PROGRM_FILE_NM = ["+ progrmInfo.getProgrmFileNm() + "[" ) > 0) ? -1 :  progrmMapper.insertProgrmInfo(progrmInfo);
	}
	*/
	@Transactional(readOnly = false)
	public int updateProgrmInfo(ProgrmInfoDto progrmInfoDto) throws Exception {
		int ret = 0;
		ProgrmInfo progrmInfo = ProgrmInfo.builder()
				.progrmFileNm(progrmInfoDto.getProgrmFileNm())
				.progrmStrePath(progrmInfoDto.getProgrmStrePath())
                .progrmKoreanNm(progrmInfoDto.getProgrmKoreannm())
				.progrmDc(progrmInfoDto.getProgrmDc())
				.url(progrmInfoDto.getUrl())
				.testUrl(progrmInfoDto.getTestUrl())
				.build();
		return progrmInfoDto.getMode().equals(Globals.SAVE_MODE_INSERT) ? progrmMapper.insertProgrmInfo(progrmInfo) : 
																		progrmMapper.updateProgrmInfo(progrmInfo);
	}

    @Transactional("jpaTransactionManager")
	public int deleteProgrmInfo(String progrmFileNm) throws Exception {
        try {
            repository.deleteById(progrmFileNm);
            return 1;
        }catch(Exception e) {
            log.error("deleteProgrmInfo error" + e.toString());
            return -1;
        }
	}

	@Transactional(readOnly = false)
	public int deleteProgrmManageList(String checkedProgrmFileNmForDel) throws Exception {
		List<String> programFiles = UtilInfoService.dotToList(checkedProgrmFileNmForDel);
		return progrmMapper.deleteProgrmManageList(programFiles);
	}
}
