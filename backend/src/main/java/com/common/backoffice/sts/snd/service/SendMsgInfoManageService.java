package com.common.backoffice.sts.snd.service;



import java.util.List;

import javax.annotation.Resource;

import com.common.backoffice.sts.snd.mapper.SendMsgInfoManagerMapper;
import com.common.backoffice.sts.snd.modals.SendMsgInfo;
import com.common.backoffice.sts.snd.modals.SendMsgInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class SendMsgInfoManageService {

    
	private final SendMsgInfoManagerMapper sendMsgInfoManagerMapper;
	
	
	public List<SendMsgInfoVO> selectSendMsgInfoManageListByPagination(
			SendMsgInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return sendMsgInfoManagerMapper.selectSendMsgInfoManageListByPagination(searchVO);
	}

	
	public int selectSendMsgInfoManageListTotCnt_S(SendMsgInfoVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return sendMsgInfoManagerMapper.selectSendMsgInfoManageListTotCnt_S(searchVO);
	}

    public int selectDIDScheduleCount(SendMsgInfo vo)
            throws Exception {
        // TODO Auto-generated method stub

        return sendMsgInfoManagerMapper.selectDIDScheduleCount(vo);
    }


    public int selectDIDOrderCount(SendMsgInfo vo) throws Exception {
        // TODO Auto-generated method stub
        return sendMsgInfoManagerMapper.selectDIDOrderCount(vo);
    }


    public List<SendMsgInfoVO> selectDIDOrderLst(SendMsgInfoVO searchVO)
            throws Exception {
        // TODO Auto-generated method stub
        return sendMsgInfoManagerMapper.selectDIDOrderLst(searchVO);
    }

    public int selectDIDMessageCount(SendMsgInfo vo) throws Exception {
        // TODO Auto-generated method stub
        return sendMsgInfoManagerMapper.selectDIDMessageCount(vo);
    }

    public int selectSendDidIDCheckCnt(String sendDidId) throws Exception {
        // TODO Auto-generated method stub
        return sendMsgInfoManagerMapper.selectSendDidIDCheckCnt(sendDidId);
    }


    public String selectSendDidIDMsgSeq(String sendDidId) throws Exception {
        // TODO Auto-generated method stub
        return sendMsgInfoManagerMapper.selectSendDidIDMsgSeq(sendDidId);
    }

    @Transactional(readOnly = false)
	public int insertSendMsgInfoManage(SendMsgInfo vo) throws Exception {
		return sendMsgInfoManagerMapper.insertSendMsgInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateSendMsgInfoManage(SendMsgInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return sendMsgInfoManagerMapper.updateSendMsgInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int insertSendMsgInfoManageState(SendMsgInfo vo)
			throws Exception {
		return  sendMsgInfoManagerMapper.insertSendMsgInfoManageState(vo);
	}

    @Transactional(readOnly = false)
	public int insertSendMessageInsertManage(SendMsgInfo vo) throws Exception {
		return sendMsgInfoManagerMapper.insertSendMessageInsertManage(vo);
	}

}
