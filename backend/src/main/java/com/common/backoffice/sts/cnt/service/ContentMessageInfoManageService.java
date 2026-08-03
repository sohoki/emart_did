package com.common.backoffice.sts.cnt.service;

import java.util.List;

import com.common.backoffice.sts.cnt.mapper.ContentMessageInfoManagerMapper;
import com.common.backoffice.sts.cnt.modals.ContentMessageInfo;
import com.common.backoffice.sts.cnt.modals.ContentMessageInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class ContentMessageInfoManageService {


	private final ContentMessageInfoManagerMapper messageInfo;

	
	public List<ContentMessageInfoVO> selectContentMessageInfoListByPagination(
			ContentMessageInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return messageInfo.selectContentMessageInfoListByPagination(searchVO);
	}

	
	public int selectContentMessageInfoListTotCnt_S(
			ContentMessageInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return messageInfo.selectContentMessageInfoListTotCnt_S(searchVO);
	}

	
	public ContentMessageInfo selectContentMessageInfoDetail(String sendMsgId)
			throws Exception {
		// TODO Auto-generated method stub
		return messageInfo.selectContentMessageInfoDetail(sendMsgId);
	}

    public List<ContentMessageInfo> selectContentMessageInfoDidList(String didId)
            throws Exception {
        // TODO Auto-generated method stub
        return messageInfo.selectContentMessageInfoDidList(didId);
    }

	/**
	 * 신규 DID 메시지(SEND_MSGID) 채번. 원본은 EgovIdGnrService(egovMsgIdGnrService) 빈을
	 * 사용했으나 did_emart에는 해당 빈이 구성되어 있지 않아 애플리케이션 레벨로 대체 채번함.
	 * 패턴: MSG_ + 10자리 zero-pad 일련번호(MAX+1).
	 */
	@Transactional(readOnly = false)
	public String generateSendMsgId() throws Exception {
		String maxId = messageInfo.selectMaxSendMsgId();
		long nextSeq = 1L;
		if (maxId != null && maxId.startsWith("MSG_")) {
			nextSeq = Long.parseLong(maxId.substring("MSG_".length())) + 1;
		}
		return "MSG_" + String.format("%010d", nextSeq);
	}

	/**
	 * 신규 DID 발송건(SEND_DIDID) 채번. DB 함수 FN_DIDMESSAGEINFO(didId)를 그대로 사용함
	 * (didId + 현재시각(yyMMddHHmmss)를 DB에서 채번).
	 */
	public String generateSendDidId(String didId) throws Exception {
		return messageInfo.selectSendDidId(didId);
	}

    @Transactional(readOnly = false)
	public int insertContentMessageInfo(ContentMessageInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return messageInfo.insertContentMessageInfo(vo);
	}

    @Transactional(readOnly = false)
	public int updateContentMessageInfo(ContentMessageInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return messageInfo.updateContentMessageInfo(vo);
	}

    @Transactional(readOnly = false)
	public int updateContentMessageInfoClientManage(String sendMsgId)
			throws Exception {
		// TODO Auto-generated method stub
		return messageInfo.updateContentMessageInfoClientManage(sendMsgId);
	}

    @Transactional(readOnly = false)
	public int deleteContentMessageInfo(String sendDidId) throws Exception {
		// TODO Auto-generated method stub
		return messageInfo.deleteContentMessageInfo(sendDidId);
	}

    @Transactional(readOnly = false)
	public int updateContentMessageInfoMsgId(ContentMessageInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return messageInfo.updateContentMessageInfoMsgId(vo);
	}

    @Transactional(readOnly = false)
	public int deleteContentMessageInfoMsgId(String sendMsgId) throws Exception {
		// TODO Auto-generated method stub
		return messageInfo.deleteContentMessageInfoMsgId(sendMsgId);
	}

	


	
	
	
	

}
